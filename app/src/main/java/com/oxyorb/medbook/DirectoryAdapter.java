package com.oxyorb.medbook;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.oxyorb.medbook.data.PortraitLoader;
import com.oxyorb.medbook.data.model.Department;
import com.oxyorb.medbook.data.model.DoctorSummary;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * The home screen list: family headings, department rows, and doctor rows.
 *
 * One flat list of rows rather than a nested structure, because both the grouped
 * department browse and the flat search results are the same shape once a heading is
 * just another row.
 */
public final class DirectoryAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

	private static final int TYPE_HEADER = 0;
	private static final int TYPE_DEPARTMENT = 1;
	private static final int TYPE_DOCTOR = 2;

	/** Callbacks for a tapped row. */
	public interface Listener {
		void onDepartmentSelected(Department _department);

		void onDoctorSelected(DoctorSummary _doctor);
	}

	private static final class Row {
		final int type;
		final String heading;
		final Department department;
		final DoctorSummary doctor;
		/** False on the last department of a family, so no divider runs under a heading. */
		final boolean divided;

		Row(int _type, String _heading, Department _department, DoctorSummary _doctor, boolean _divided) {
			type = _type;
			heading = _heading;
			department = _department;
			doctor = _doctor;
			divided = _divided;
		}
	}

	private final List<Row> rows = new ArrayList<>();
	private final PortraitLoader portraits;
	private final Listener listener;

	public DirectoryAdapter(PortraitLoader _portraits, Listener _listener) {
		portraits = _portraits;
		listener = _listener;
		setHasStableIds(false);
	}

	/** The full browse: every department, under its family heading. */
	public void showDepartments(List<Department> _departments) {
		rows.clear();
		String _family = null;
		for (int _i = 0; _i < _departments.size(); _i++) {
			Department _department = _departments.get(_i);
			if (!_department.displayFamilyName.equals(_family)) {
				_family = _department.displayFamilyName;
				rows.add(new Row(TYPE_HEADER, _family, null, null, false));
			}
			boolean _lastInFamily = _i + 1 == _departments.size()
				|| !_departments.get(_i + 1).displayFamilyName.equals(_family);
			rows.add(new Row(TYPE_DEPARTMENT, null, _department, null, !_lastInFamily));
		}
		notifyDataSetChanged();
	}

	/**
	 * Search results: matching departments first, then matching doctors.
	 *
	 * Departments lead because they are the smaller, more certain answer - 45 curated
	 * names against 7,438 doctors - and because someone typing "cardio" is usually
	 * looking for the department rather than one particular cardiologist.
	 */
	public void showResults(List<Department> _departments, List<DoctorSummary> _doctors,
			String _departmentsHeading, String _doctorsHeading) {
		rows.clear();
		if (!_departments.isEmpty()) {
			rows.add(new Row(TYPE_HEADER, _departmentsHeading, null, null, false));
			for (int _i = 0; _i < _departments.size(); _i++) {
				rows.add(new Row(TYPE_DEPARTMENT, null, _departments.get(_i), null,
					_i + 1 < _departments.size()));
			}
		}
		if (!_doctors.isEmpty()) {
			rows.add(new Row(TYPE_HEADER, _doctorsHeading, null, null, false));
			for (DoctorSummary _doctor : _doctors) {
				rows.add(new Row(TYPE_DOCTOR, null, null, _doctor, false));
			}
		}
		notifyDataSetChanged();
	}

	/** A flat list of doctors, with no headings. Used by the department screen. */
	public void showDoctors(List<DoctorSummary> _doctors) {
		rows.clear();
		for (DoctorSummary _doctor : _doctors) {
			rows.add(new Row(TYPE_DOCTOR, null, null, _doctor, false));
		}
		notifyDataSetChanged();
	}

	/**
	 * Adds the next page.
	 *
	 * notifyItemRangeInserted rather than notifyDataSetChanged, so appending does not
	 * rebind every visible row and interrupt the scroll the user is still in.
	 */
	public void appendDoctors(List<DoctorSummary> _doctors) {
		if (_doctors.isEmpty()) {
			return;
		}
		int _start = rows.size();
		for (DoctorSummary _doctor : _doctors) {
			rows.add(new Row(TYPE_DOCTOR, null, null, _doctor, false));
		}
		notifyItemRangeInserted(_start, _doctors.size());
	}

	@Override
	public int getItemCount() {
		return rows.size();
	}

	@Override
	public int getItemViewType(int _position) {
		return rows.get(_position).type;
	}

	@NonNull
	@Override
	public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup _parent, int _viewType) {
		LayoutInflater _inflater = LayoutInflater.from(_parent.getContext());
		switch (_viewType) {
			case TYPE_HEADER:
				return new HeaderHolder(_inflater.inflate(R.layout.item_family_header, _parent, false));
			case TYPE_DOCTOR:
				return new DoctorHolder(_inflater.inflate(R.layout.item_doctor, _parent, false));
			default:
				return new DepartmentHolder(_inflater.inflate(R.layout.item_department, _parent, false));
		}
	}

	@Override
	public void onBindViewHolder(@NonNull RecyclerView.ViewHolder _holder, int _position) {
		Row _row = rows.get(_position);
		if (_holder instanceof HeaderHolder) {
			((HeaderHolder) _holder).name.setText(_row.heading);
		} else if (_holder instanceof DepartmentHolder) {
			((DepartmentHolder) _holder).bind(_row);
		} else {
			((DoctorHolder) _holder).bind(_row.doctor);
		}
	}

	static final class HeaderHolder extends RecyclerView.ViewHolder {
		final TextView name;

		HeaderHolder(View _view) {
			super(_view);
			name = _view.findViewById(R.id.family_name);
		}
	}

	final class DepartmentHolder extends RecyclerView.ViewHolder {
		final TextView name;
		final TextView count;
		final View divider;

		DepartmentHolder(View _view) {
			super(_view);
			name = _view.findViewById(R.id.department_name);
			count = _view.findViewById(R.id.department_count);
			divider = _view.findViewById(R.id.department_divider);
		}

		void bind(final Row _row) {
			name.setText(_row.department.displayName);
			count.setText(String.format(Locale.getDefault(), "%d", _row.department.doctorCount));
			divider.setVisibility(_row.divided ? View.VISIBLE : View.INVISIBLE);
			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					listener.onDepartmentSelected(_row.department);
				}
			});
		}
	}

	final class DoctorHolder extends RecyclerView.ViewHolder {
		final ImageView portrait;
		final TextView name;
		final TextView specialty;
		final TextView meta;

		DoctorHolder(View _view) {
			super(_view);
			portrait = _view.findViewById(R.id.doctor_portrait);
			name = _view.findViewById(R.id.doctor_name);
			specialty = _view.findViewById(R.id.doctor_specialty);
			meta = _view.findViewById(R.id.doctor_meta);
		}

		void bind(final DoctorSummary _doctor) {
			name.setText(_doctor.displayName);
			setOrHide(specialty, _doctor.specialty);
			setOrHide(meta, metaLine(_doctor));
			portraits.load(portrait, _doctor.portrait, _doctor.displayName);
			itemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					listener.onDoctorSelected(_doctor);
				}
			});
		}
	}

	/** Degrees and city on one line, with whichever of them the source actually has. */
	private static String metaLine(DoctorSummary _doctor) {
		boolean _hasDegrees = _doctor.degrees != null && !_doctor.degrees.isEmpty();
		boolean _hasCity = _doctor.city != null && !_doctor.city.isEmpty();
		if (_hasDegrees && _hasCity) {
			return _doctor.degrees + " · " + _doctor.city;
		}
		if (_hasDegrees) {
			return _doctor.degrees;
		}
		return _hasCity ? _doctor.city : null;
	}

	/**
	 * A missing value is null throughout the dataset, never an empty string, and the
	 * row collapses rather than leaving a blank line where a fact would be.
	 */
	private static void setOrHide(TextView _view, String _text) {
		if (_text == null || _text.isEmpty()) {
			_view.setVisibility(View.GONE);
		} else {
			_view.setVisibility(View.VISIBLE);
			_view.setText(_text);
		}
	}
}
