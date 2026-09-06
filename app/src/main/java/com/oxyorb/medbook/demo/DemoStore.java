package com.oxyorb.medbook.demo;

import android.content.Context;
import android.content.SharedPreferences;

import com.oxyorb.medbook.data.DatasetUnpacker;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The bookings a person actually made in the demo, which are the only part of it that
 * is written down.
 *
 * Everything else -- who else is in the book, how full the day is, what serial a slot
 * carries -- is recomputed by DemoSchedule from the chamber, the date and the doctor's
 * standing. That is what keeps this file small enough to be a preferences file rather
 * than a database: a sales visit produces a handful of rows, and there is no query here
 * a sort over a few dozen objects does not answer.
 *
 * It is a fourth preferences file rather than a table in medbook.db because that
 * database is opened read-only and is deleted and rebuilt from the packaged asset
 * whenever the dataset changes. A booking written into it would not survive its first
 * dataset refresh, and the file is excluded from backup for the same reason it is
 * excluded elsewhere: this is a rehearsal on one install, not something to carry to a
 * new phone.
 *
 * Records are pipe-delimited, which is the shape DoctorRepository.split already reads,
 * and every field is a number -- there is no free text anywhere in a booking, so there
 * is nothing to escape and no way for a stray delimiter to tear a record in half.
 *
 * A single string rather than a StringSet, deliberately. The set a StringSet hands back
 * must never be mutated in place before being written again, which is a footgun with no
 * upside here: a set has no order, and the console wants these sorted by time anyway.
 */
public final class DemoStore {

	private static final String PREFS = "demo";
	private static final String KEY_BOOKINGS = "bookings";
	/** The dataset build these bookings were made against. See {@link #reconcile}. */
	private static final String KEY_STAMP = "bookings_stamp";

	private static final String RECORD_SEPARATOR = "\n";
	private static final String FIELD_SEPARATOR = "|";
	private static final String VERSION = "1";

	private static DemoStore instance;

	private final SharedPreferences prefs;

	/** One slot a person took in the demo. The serial is not here; it is re-derived. */
	public static final class Booking {

		public final long doctorId;
		public final int seq;
		public final int epochDay;
		public final int minuteOfDay;
		public final long createdAt;

		Booking(long _doctorId, int _seq, int _epochDay, int _minuteOfDay, long _createdAt) {
			doctorId = _doctorId;
			seq = _seq;
			epochDay = _epochDay;
			minuteOfDay = _minuteOfDay;
			createdAt = _createdAt;
		}

		public String chamberKey() {
			return ChamberKey.of(doctorId, seq);
		}

		boolean sameSlotAs(long _doctorId, int _seq, int _epochDay, int _minuteOfDay) {
			return doctorId == _doctorId && seq == _seq
				&& epochDay == _epochDay && minuteOfDay == _minuteOfDay;
		}
	}

	private DemoStore(Context _context) {
		prefs = _context.getApplicationContext()
			.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
		reconcile(_context);
	}

	public static synchronized DemoStore get(Context _context) {
		if (instance == null) {
			instance = new DemoStore(_context);
		}
		return instance;
	}

	/**
	 * Drops every booking when the dataset underneath them has been replaced.
	 *
	 * A booking names a doctor and a chamber by row id, and those ids are assigned in the
	 * order the pipeline happened to read its input. A rebuild moves them, so a record
	 * kept across one would still resolve -- to a different doctor. Dropping them is the
	 * honest answer and costs nothing: this is demonstration data by definition.
	 */
	private void reconcile(Context _context) {
		String _stamp = DatasetUnpacker.stamp(_context);
		if (_stamp == null) {
			return;
		}
		if (!_stamp.equals(prefs.getString(KEY_STAMP, null))) {
			prefs.edit().remove(KEY_BOOKINGS).putString(KEY_STAMP, _stamp).apply();
		}
	}

	/** Every booking, oldest day first, and within a day in the order they are sat. */
	public List<Booking> all() {
		List<Booking> _bookings = decode(prefs.getString(KEY_BOOKINGS, ""));
		Collections.sort(_bookings, new Comparator<Booking>() {
			@Override
			public int compare(Booking _left, Booking _right) {
				if (_left.epochDay != _right.epochDay) {
					return _left.epochDay < _right.epochDay ? -1 : 1;
				}
				return _left.minuteOfDay - _right.minuteOfDay;
			}
		});
		return _bookings;
	}

	public List<Booking> forDay(long _doctorId, int _seq, int _epochDay) {
		List<Booking> _matching = new ArrayList<>();
		for (Booking _booking : all()) {
			if (_booking.doctorId == _doctorId && _booking.seq == _seq
					&& _booking.epochDay == _epochDay) {
				_matching.add(_booking);
			}
		}
		return _matching;
	}

	/** The minutes taken on one day, in the shape DemoSchedule wants them. */
	public Set<Integer> minutesFor(long _doctorId, int _seq, int _epochDay) {
		Set<Integer> _minutes = new HashSet<>(8);
		for (Booking _booking : forDay(_doctorId, _seq, _epochDay)) {
			_minutes.add(_booking.minuteOfDay);
		}
		return _minutes;
	}

	public boolean isEmpty() {
		return prefs.getString(KEY_BOOKINGS, "").isEmpty();
	}

	public void book(long _doctorId, int _seq, int _epochDay, int _minuteOfDay) {
		List<Booking> _bookings = all();
		for (Booking _booking : _bookings) {
			if (_booking.sameSlotAs(_doctorId, _seq, _epochDay, _minuteOfDay)) {
				return;
			}
		}
		_bookings.add(new Booking(_doctorId, _seq, _epochDay, _minuteOfDay,
			System.currentTimeMillis()));
		write(_bookings);
	}

	public void cancel(long _doctorId, int _seq, int _epochDay, int _minuteOfDay) {
		List<Booking> _kept = new ArrayList<>();
		for (Booking _booking : all()) {
			if (!_booking.sameSlotAs(_doctorId, _seq, _epochDay, _minuteOfDay)) {
				_kept.add(_booking);
			}
		}
		write(_kept);
	}

	/**
	 * Clears the bookings and nothing else.
	 *
	 * There is no simulated world to reset. It was never stored, so the day a demo is
	 * reset to is the same day it started from.
	 */
	public void reset() {
		prefs.edit().remove(KEY_BOOKINGS).apply();
	}

	private void write(List<Booking> _bookings) {
		prefs.edit().putString(KEY_BOOKINGS, encode(_bookings)).apply();
	}

	static String encode(List<Booking> _bookings) {
		StringBuilder _text = new StringBuilder();
		for (Booking _booking : _bookings) {
			if (_text.length() > 0) {
				_text.append(RECORD_SEPARATOR);
			}
			_text.append(VERSION).append(FIELD_SEPARATOR)
				.append(_booking.doctorId).append(FIELD_SEPARATOR)
				.append(_booking.seq).append(FIELD_SEPARATOR)
				.append(_booking.epochDay).append(FIELD_SEPARATOR)
				.append(_booking.minuteOfDay).append(FIELD_SEPARATOR)
				.append(_booking.createdAt);
		}
		return _text.toString();
	}

	/**
	 * A record that does not read is skipped, not thrown.
	 *
	 * One unreadable line should cost one booking rather than the whole book. There is no
	 * delete-and-regenerate for something a person typed, which is what the dataset gets
	 * when it is half-written.
	 */
	static List<Booking> decode(String _text) {
		List<Booking> _bookings = new ArrayList<>();
		if (_text == null || _text.isEmpty()) {
			return _bookings;
		}
		for (String _line : _text.split(RECORD_SEPARATOR)) {
			Booking _booking = decodeRecord(_line);
			if (_booking != null) {
				_bookings.add(_booking);
			}
		}
		return _bookings;
	}

	private static Booking decodeRecord(String _line) {
		String[] _fields = _line.split("\\|");
		if (_fields.length != 6 || !VERSION.equals(_fields[0])) {
			return null;
		}
		try {
			return new Booking(Long.parseLong(_fields[1]), Integer.parseInt(_fields[2]),
				Integer.parseInt(_fields[3]), Integer.parseInt(_fields[4]),
				Long.parseLong(_fields[5]));
		} catch (NumberFormatException _e) {
			return null;
		}
	}
}
