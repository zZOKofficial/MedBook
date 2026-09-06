package com.oxyorb.medbook.demo;

import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

/**
 * Times and dates for the demo screens, in the language the app is running in.
 *
 * Everything here goes through a formatter carrying the app's locale, which is the only
 * reason any of it renders in Bengali numerals. There is no digit conversion anywhere in
 * MedBook and none is needed: java.text does it, given the locale. Build a time by hand
 * as an hour, a colon and a minute and it stays in Latin digits for ever -- which is
 * exactly why the chamber phone numbers do, and they are the one place that is right.
 *
 * Every Calendar the demo produces is a date rather than a moment, so every formatter
 * here is forced to UTC. Left on the device's own zone, a phone west of Greenwich would
 * print the day before the one being booked.
 */
public final class DemoFormat {

	private static final long MILLIS_PER_DAY = 86400000L;
	private static final long MILLIS_PER_MINUTE = 60000L;
	private static final TimeZone UTC = TimeZone.getTimeZone("UTC");

	private DemoFormat() {
	}

	/** A clock time, in the device's own 12 or 24 hour preference. */
	public static String time(Context _context, int _epochDay, int _minuteOfDay) {
		DateFormat _format = android.text.format.DateFormat.getTimeFormat(_context);
		_format.setTimeZone(UTC);
		return _format.format(new Date(
			_epochDay * MILLIS_PER_DAY + _minuteOfDay * MILLIS_PER_MINUTE));
	}

	/** "Sat 12 Sep", or whatever that day is called and ordered in this locale. */
	public static String day(Context _context, int _epochDay) {
		return formatted(_context, "EEEdMMM", _epochDay);
	}

	/** The same day, short enough for a chip in the strip. */
	public static String shortDay(Context _context, int _epochDay) {
		return formatted(_context, "EEEd", _epochDay);
	}

	private static String formatted(Context _context, String _skeleton, int _epochDay) {
		Locale _locale = locale(_context);
		SimpleDateFormat _format = new SimpleDateFormat(
			android.text.format.DateFormat.getBestDateTimePattern(_locale, _skeleton), _locale);
		_format.setTimeZone(UTC);
		return _format.format(EpochDay.date(_epochDay));
	}

	private static Locale locale(Context _context) {
		Configuration _configuration = _context.getResources().getConfiguration();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			return _configuration.getLocales().get(0);
		}
		return _configuration.locale;
	}
}
