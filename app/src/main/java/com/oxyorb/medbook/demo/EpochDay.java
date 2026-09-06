package com.oxyorb.medbook.demo;

import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

/**
 * Whole days as plain integers, counted from 1 January 1970.
 *
 * java.time would say this in one line, and is not available: minSdk is 21, LocalDate
 * arrived in the platform at 26, and the build enables no core library desugaring. Adding
 * desugaring for a demo feature would be a real change to how every class in the app is
 * compiled, so the demo does its date arithmetic on ints instead and the build config is
 * left alone.
 *
 * A day here is a calendar date in the device's own timezone, turned into a count. That
 * is the right unit for "which day's appointment book am I looking at" -- it does not
 * drift with the clock, it compares and subtracts with operators, and it is the same
 * number on both sides of a timezone boundary for the same printed date.
 */
public final class EpochDay {

	private static final long MILLIS_PER_DAY = 86400000L;

	private EpochDay() {
	}

	/** Today's date where the device is standing. */
	public static int today() {
		Calendar _local = Calendar.getInstance();
		return of(_local.get(Calendar.YEAR), _local.get(Calendar.MONTH),
			_local.get(Calendar.DAY_OF_MONTH));
	}

	/** A year, a zero-based month and a day of the month, as a day count. */
	public static int of(int _year, int _zeroBasedMonth, int _dayOfMonth) {
		Calendar _utc = utc();
		_utc.set(_year, _zeroBasedMonth, _dayOfMonth);
		return (int) (_utc.getTimeInMillis() / MILLIS_PER_DAY);
	}

	/** Minutes since midnight where the device is standing. */
	public static int minuteOfDayNow() {
		Calendar _local = Calendar.getInstance();
		return _local.get(Calendar.HOUR_OF_DAY) * 60 + _local.get(Calendar.MINUTE);
	}

	/** One of Calendar.SUNDAY through Calendar.SATURDAY. */
	public static int dayOfWeek(int _epochDay) {
		return calendar(_epochDay).get(Calendar.DAY_OF_WEEK);
	}

	/**
	 * The day as a UTC calendar at midnight.
	 *
	 * Anything formatting this must set its own timezone to UTC as well, or a phone west
	 * of Greenwich will print the day before -- the calendar carries a date, not a moment.
	 */
	public static Calendar calendar(int _epochDay) {
		Calendar _utc = utc();
		_utc.setTimeInMillis(_epochDay * MILLIS_PER_DAY);
		return _utc;
	}

	public static Date date(int _epochDay) {
		return calendar(_epochDay).getTime();
	}

	private static Calendar utc() {
		Calendar _utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
		_utc.clear();
		return _utc;
	}
}
