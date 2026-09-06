package com.oxyorb.medbook.data;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.util.Log;
import com.oxyorb.medbook.data.model.Chamber;
import com.oxyorb.medbook.data.model.Department;
import com.oxyorb.medbook.data.model.Doctor;
import com.oxyorb.medbook.data.model.DoctorSummary;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Read-only access to the doctor directory.
 *
 * The database is built ahead of time by the dataset pipeline and never written to
 * here, so there are no migrations and no schema to keep in sync at runtime: a new
 * dataset simply replaces the file. That is also why this is hand-written SQL rather
 * than Room - Room's value is migrations and compile-time checking of a schema the app
 * owns, and the app does not own this one.
 */
public final class DoctorRepository {

	private static final String TAG = "DoctorRepository";

	/** Beyond this a query is a browse, not a search, and the list is unreadable anyway. */
	private static final int SEARCH_LIMIT = 60;

	private static DoctorRepository instance;

	private final SQLiteDatabase database;

	private DoctorRepository(SQLiteDatabase _database) {
		database = _database;
	}

	/**
	 * Opens the directory, unpacking it first if needed.
	 *
	 * Returns null when this build ships no directory data, or when the data could not
	 * be opened. Both are states the UI shows as an empty directory rather than an
	 * error - a checkout without dataset.properties is a supported way to build.
	 *
	 * Blocking. Call it off the main thread; the first call has to unpack 12 MB.
	 */
	/**
	 * Whether the directory is already open in this process.
	 *
	 * Lets a recreate -- which is what changing the theme or the language causes --
	 * skip the first-launch spinner, since open() will return the existing instance
	 * without touching the disk. Cheap enough to call on the main thread, which
	 * open() is not.
	 */
	public static synchronized boolean isOpen() {
		return instance != null;
	}

	public static synchronized DoctorRepository open(Context _context) {
		if (instance != null) {
			return instance;
		}
		File _file = DatasetUnpacker.ensureUnpacked(_context.getApplicationContext());
		if (_file == null) {
			return null;
		}
		try {
			SQLiteDatabase _database = SQLiteDatabase.openDatabase(
				_file.getPath(), null, SQLiteDatabase.OPEN_READONLY);
			instance = new DoctorRepository(_database);
			return instance;
		} catch (SQLiteException _e) {
			Log.e(TAG, "could not open the doctor directory", _e);
			return null;
		}
	}

	// -- browsing -----------------------------------------------------------------

	/** Every department, in the layout's own family order. */
	public List<Department> departments() {
		Cursor _cursor = database.rawQuery(
			"SELECT d.id, d.key, d.name, f.name, d.doctor_count"
				+ " FROM departments d JOIN families f ON f.id = d.family_id"
				+ " ORDER BY f.sort_order, d.sort_order", null);
		try {
			List<Department> _departments = new ArrayList<>(_cursor.getCount());
			while (_cursor.moveToNext()) {
				_departments.add(new Department(
					_cursor.getLong(0), _cursor.getString(1), _cursor.getString(2),
					_cursor.getString(3), _cursor.getInt(4)));
			}
			return _departments;
		} finally {
			_cursor.close();
		}
	}

	/**
	 * Doctors in one department, a page at a time.
	 *
	 * Verified profiles come first, then the most reviewed, then alphabetically. A
	 * doctor with no rating sorts below one with any rating rather than above it, which
	 * is what COALESCE guards - review_count is null for the unrated, and null sorts
	 * first in SQLite's DESC ordering.
	 */
	public List<DoctorSummary> doctorsInDepartment(long _departmentId, int _limit, int _offset) {
		Cursor _cursor = database.rawQuery(
			"SELECT d.id, d.display_name, d.specialty_original, d.degrees, d.city,"
				+ " d.portrait, d.is_verified"
				+ " FROM doctors d JOIN doctor_departments x ON x.doctor_id = d.id"
				+ " WHERE x.department_id = ?"
				+ " ORDER BY d.is_verified DESC, COALESCE(d.review_count, -1) DESC, d.sort_name"
				+ " LIMIT ? OFFSET ?",
			new String[] {String.valueOf(_departmentId), String.valueOf(_limit), String.valueOf(_offset)});
		return readSummaries(_cursor);
	}

	// -- one doctor ---------------------------------------------------------------

	public Doctor doctor(long _id) {
		Cursor _cursor = database.rawQuery(
			"SELECT id, display_name, specialty_original, designation, current_institution,"
				+ " degrees, experience_text, bmdc_number, average_rating, review_count,"
				+ " is_verified, city, about, about_bn, portrait"
				+ " FROM doctors WHERE id = ?",
			new String[] {String.valueOf(_id)});
		try {
			if (!_cursor.moveToFirst()) {
				return null;
			}
			return new Doctor(
				_cursor.getLong(0), _cursor.getString(1), _cursor.getString(2), _cursor.getString(3),
				_cursor.getString(4), _cursor.getString(5), _cursor.getString(6), _cursor.getString(7),
				_cursor.isNull(8) ? null : _cursor.getDouble(8),
				_cursor.isNull(9) ? null : _cursor.getInt(9),
				_cursor.getInt(10) != 0, _cursor.getString(11), _cursor.getString(12),
				_cursor.getString(13), _cursor.getString(14),
				chambers(_id));
		} finally {
			_cursor.close();
		}
	}

	public List<Chamber> chambers(long _doctorId) {
		Cursor _cursor = database.rawQuery(
			"SELECT name, address, city, area, visiting_hours, days, closed_days, phones"
				+ " FROM chambers WHERE doctor_id = ? ORDER BY seq",
			new String[] {String.valueOf(_doctorId)});
		try {
			List<Chamber> _chambers = new ArrayList<>(_cursor.getCount());
			while (_cursor.moveToNext()) {
				_chambers.add(new Chamber(
					_cursor.getString(0), _cursor.getString(1), _cursor.getString(2), _cursor.getString(3),
					_cursor.getString(4), split(_cursor.getString(5)), split(_cursor.getString(6)),
					split(_cursor.getString(7))));
			}
			return _chambers;
		} finally {
			_cursor.close();
		}
	}

	// -- search -------------------------------------------------------------------

	/**
	 * Departments whose name contains the query. Cheap: there are only 45.
	 *
	 * "&" and "and" are treated as the same thing, so typing "cardiothoracic and
	 * vascular" still finds "Cardiothoracic &amp; Vascular Surgery". Eleven of the 45
	 * names contain an ampersand, and nobody types one. This is why department search
	 * is a scan here rather than another FTS query: norm() reduces "&" to a separator,
	 * which is right for indexing prose and wrong for matching these names.
	 */
	public List<Department> searchDepartments(String _query) {
		String _normalised = norm(expandAmpersand(_query));
		if (_normalised.isEmpty()) {
			return Collections.emptyList();
		}
		List<Department> _matches = new ArrayList<>();
		for (Department _department : departments()) {
			if (norm(expandAmpersand(_department.name)).contains(_normalised)) {
				_matches.add(_department);
			}
		}
		return _matches;
	}

	private static String expandAmpersand(String _text) {
		return _text == null ? "" : _text.replace("&", " and ");
	}

	/**
	 * Doctors matching a query, across name, specialty, workplace, degrees, city,
	 * chamber venues and both biographies.
	 *
	 * Every token is prefix-matched, so "cardio dha" finds a Dhaka cardiologist while
	 * still narrowing as the user types.
	 *
	 * Ordering puts name matches first. A contentless FTS4 table carries no ranking of
	 * its own, and matchinfo would mean scoring in Java on every keystroke for a list
	 * that is capped at 60 rows anyway.
	 */
	public List<DoctorSummary> searchDoctors(String _query) {
		String _match = matchExpression(_query);
		if (_match == null) {
			return Collections.emptyList();
		}
		Cursor _cursor = database.rawQuery(
			"SELECT d.id, d.display_name, d.specialty_original, d.degrees, d.city,"
				+ " d.portrait, d.is_verified"
				+ " FROM doctor_search s JOIN doctors d ON d.id = s.docid"
				+ " WHERE doctor_search MATCH ?"
				+ " ORDER BY (CASE WHEN d.sort_name LIKE ? THEN 0 ELSE 1 END),"
				+ " d.is_verified DESC, COALESCE(d.review_count, -1) DESC, d.sort_name"
				+ " LIMIT ?",
			new String[] {_match, "%" + norm(_query) + "%", String.valueOf(SEARCH_LIMIT)});
		return readSummaries(_cursor);
	}

	/**
	 * Builds the FTS MATCH expression, or null when there is nothing to search for.
	 *
	 * Tokens need no quoting or escaping because {@link #norm} has already removed
	 * everything FTS treats as syntax - quotes, hyphens, colons, parentheses, NEAR,
	 * OR and the rest survive only as separators.
	 */
	private static String matchExpression(String _query) {
		String _normalised = norm(_query);
		if (_normalised.isEmpty()) {
			return null;
		}
		StringBuilder _builder = new StringBuilder();
		for (String _token : _normalised.split(" ")) {
			if (_token.isEmpty()) {
				continue;
			}
			if (_builder.length() > 0) {
				_builder.append(' ');
			}
			_builder.append(_token).append('*');
		}
		return _builder.length() == 0 ? null : _builder.toString();
	}

	/**
	 * Folds text the same way the pipeline folded it before indexing.
	 *
	 * This must stay byte-identical to norm() in pipeline/build_db.py. If the two ever
	 * disagree, a query tokenises differently from the index and search returns nothing
	 * at all, with no error to explain why. The rule is deliberately blunt for that
	 * reason: lowercase, and anything that is not ASCII alphanumeric or Bengali becomes
	 * a space. No Unicode normalisation, no accent folding, nothing whose edge cases
	 * could drift between Java and Python.
	 *
	 * Locale.ROOT, not the device locale: on a Turkish phone the default toLowerCase
	 * turns "I" into a dotless i, and every query containing one would stop matching.
	 */
	static String norm(String _text) {
		if (_text == null) {
			return "";
		}
		String _lower = _text.toLowerCase(Locale.ROOT);
		StringBuilder _builder = new StringBuilder(_lower.length());
		boolean _pendingSpace = false;
		for (int _i = 0; _i < _lower.length(); _i++) {
			char _c = _lower.charAt(_i);
			boolean _keep = (_c >= 'a' && _c <= 'z')
				|| (_c >= '0' && _c <= '9')
				|| (_c >= 'ঀ' && _c <= '৿');
			if (_keep) {
				if (_pendingSpace && _builder.length() > 0) {
					_builder.append(' ');
				}
				_pendingSpace = false;
				_builder.append(_c);
			} else {
				_pendingSpace = true;
			}
		}
		return _builder.toString();
	}

	// -- plumbing -----------------------------------------------------------------

	private static List<DoctorSummary> readSummaries(Cursor _cursor) {
		try {
			List<DoctorSummary> _doctors = new ArrayList<>(_cursor.getCount());
			while (_cursor.moveToNext()) {
				_doctors.add(new DoctorSummary(
					_cursor.getLong(0), _cursor.getString(1), _cursor.getString(2),
					_cursor.getString(3), _cursor.getString(4), _cursor.getString(5),
					_cursor.getInt(6) != 0));
			}
			return _doctors;
		} finally {
			_cursor.close();
		}
	}

	/** The pipeline joins repeated values with a pipe; null means the list was empty. */
	private static List<String> split(String _packed) {
		if (_packed == null || _packed.isEmpty()) {
			return Collections.emptyList();
		}
		return Arrays.asList(_packed.split("\\|"));
	}
}
