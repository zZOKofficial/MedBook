package com.oxyorb.medbook.demo;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The verbatim visiting hours, read well enough to lay appointment slots over them.
 *
 * Chamber.visitingHours is free text and the authority on when a doctor sits. A demo
 * that invented its own hours would be showing a chamber a working day it does not
 * work, so this reads the published string instead -- and refuses when it cannot.
 *
 * Measured across all 9,350 chambers in the directory: 2 publish nothing at all, 428
 * say some form of "Unknown. Please call to know visiting hour", and 8,920 state real
 * hours. Of those 8,920, this parses 8,563 -- 96.0%. The remaining 357 are a long tail
 * of 311 distinct strings where the source is vague ("Evening (Closed: Friday)"),
 * mistyped ("4m to 9pm"), or names a day without an hour on it ("Every Friday").
 *
 * The parsed chambers yield a median of 9 twenty-minute slots a day and 24 at the 90th
 * percentile. None yield zero, because a period must end after it starts.
 *
 * {@link #parse(String)} returns null rather than guessing, and a chamber it cannot
 * read gets no Book button. Inventing 5pm to 9pm for a chamber that says to call is the
 * same class of mistake as showing an unrated doctor zero stars, which the profile
 * screen already refuses to do.
 *
 * The grammar accepted:
 *
 * <pre>
 * hours   := period ( "&amp;" period )*
 * period  := time "to" time ( "(" clause ")" )?
 * time    := digits ( ":" | "." digits )? ( "am" | "pm" )
 * clause  := "Closed:" days | "Except" days | "Only" days | "Every" days
 *          | "Everyday" | "Open Everyday" | days | day "to" day
 * days    := day ( "," | "&amp;" | "and" ) day ...
 * </pre>
 *
 * am or pm is required. "10 to 8" is not a time range anyone can act on, and reading it
 * as either would be a guess. Days are matched case-insensitively and accept the
 * abbreviations the source actually uses, which include Thu, Thur and Thurs for the
 * same day.
 *
 * Pure Java with no Android on it, which is what lets the whole of it be tested off a
 * device -- the same reason department search moved to a static function.
 */
public final class VisitingHours {

	/** One stretch of a day the doctor sits, on some set of weekdays. */
	public static final class Period {

		/** Minutes from midnight, inclusive. */
		public final int startMinute;
		/** Minutes from midnight, exclusive. Always greater than {@link #startMinute}. */
		public final int endMinute;
		/** Calendar.SUNDAY through Calendar.SATURDAY. Never empty. */
		public final Set<Integer> days;

		Period(int _startMinute, int _endMinute, Set<Integer> _days) {
			startMinute = _startMinute;
			endMinute = _endMinute;
			days = Collections.unmodifiableSet(_days);
		}
	}

	private static final Map<String, Integer> DAYS = new HashMap<>(32);

	static {
		DAYS.put("sun", Calendar.SUNDAY);
		DAYS.put("sunday", Calendar.SUNDAY);
		DAYS.put("mon", Calendar.MONDAY);
		DAYS.put("monday", Calendar.MONDAY);
		DAYS.put("tue", Calendar.TUESDAY);
		DAYS.put("tues", Calendar.TUESDAY);
		DAYS.put("tuesday", Calendar.TUESDAY);
		DAYS.put("wed", Calendar.WEDNESDAY);
		DAYS.put("weds", Calendar.WEDNESDAY);
		DAYS.put("wednesday", Calendar.WEDNESDAY);
		DAYS.put("thu", Calendar.THURSDAY);
		DAYS.put("thur", Calendar.THURSDAY);
		DAYS.put("thurs", Calendar.THURSDAY);
		DAYS.put("thursday", Calendar.THURSDAY);
		DAYS.put("fri", Calendar.FRIDAY);
		DAYS.put("friday", Calendar.FRIDAY);
		DAYS.put("sat", Calendar.SATURDAY);
		DAYS.put("saturday", Calendar.SATURDAY);
	}

	/**
	 * Splits "10am to 1pm & 4pm to 8pm" into its two halves.
	 *
	 * The lookahead matters: an ampersand only separates periods when a time follows it.
	 * "(Closed: Friday & Govt. Holidays)" contains one that does not, and splitting there
	 * would tear a day clause in half.
	 */
	private static final Pattern PERIOD_SPLIT = Pattern.compile(
		"\\s*&\\s*(?=\\d)|\\s*,\\s*(?=\\d{1,2}\\s*(?:am|pm|[:.]))", Pattern.CASE_INSENSITIVE);

	private static final Pattern CLAUSE = Pattern.compile("^(.*?)\\s*\\(([^)]*)\\)\\s*$");

	private static final Pattern RANGE_SPLIT =
		Pattern.compile("\\s+to\\s+|\\s*-\\s*", Pattern.CASE_INSENSITIVE);

	private static final Pattern TIME =
		Pattern.compile("^(\\d{1,2})(?:[:.](\\d{2}))?\\s*(am|pm)$", Pattern.CASE_INSENSITIVE);

	private static final Pattern DAY_RANGE =
		Pattern.compile("^([a-z]+)\\s+to\\s+([a-z]+)$");

	private static final Pattern LEADING_OPEN = Pattern.compile("^opens?\\s+");
	private static final Pattern LEADING_NEGATIVE = Pattern.compile("^(?:closed|except)\\s*:?\\s*");
	private static final Pattern LEADING_POSITIVE = Pattern.compile("^(?:only|every)\\s*:?\\s*");

	public final List<Period> periods;

	private VisitingHours(List<Period> _periods) {
		periods = Collections.unmodifiableList(_periods);
	}

	/**
	 * Reads the source's wording, or returns null if it cannot.
	 *
	 * Null is the ordinary answer for 787 of 9,350 chambers and is not an error: the
	 * source declining to publish its hours is a fact about the chamber, not a fault
	 * here. Callers hide the booking affordance rather than substituting a default.
	 */
	public static VisitingHours parse(String _raw) {
		if (_raw == null) {
			return null;
		}
		String _text = _raw.trim();
		if (_text.isEmpty()) {
			return null;
		}

		List<String> _ranges = new ArrayList<>(2);
		List<String> _clauses = new ArrayList<>(2);
		for (String _part : PERIOD_SPLIT.split(_text)) {
			String _piece = _part.trim();
			if (_piece.isEmpty()) {
				continue;
			}
			Matcher _matcher = CLAUSE.matcher(_piece);
			if (_matcher.matches()) {
				_ranges.add(_matcher.group(1).trim());
				_clauses.add(_matcher.group(2).trim());
			} else {
				_ranges.add(_piece);
				_clauses.add("");
			}
		}
		if (_ranges.isEmpty()) {
			return null;
		}
		spreadTrailingClause(_clauses);

		List<Period> _periods = new ArrayList<>(_ranges.size());
		for (int _index = 0; _index < _ranges.size(); _index++) {
			Period _period = parsePeriod(_ranges.get(_index), _clauses.get(_index));
			if (_period == null) {
				return null;
			}
			_periods.add(_period);
		}
		return new VisitingHours(_periods);
	}

	/**
	 * "10am to 1pm & 4pm to 8pm (Closed: Friday)" closes both halves on Friday, not only
	 * the second. A clause written once at the end governs everything before it.
	 *
	 * Only when nothing else carries one, though: "4pm to 9pm (Sat to Thu) & 9am to 4pm
	 * (Friday)" is a doctor who sits six days in the evening and Friday morning, and
	 * spreading either clause across both would invent a session that does not happen.
	 */
	private static void spreadTrailingClause(List<String> _clauses) {
		int _last = _clauses.size() - 1;
		if (_last < 1 || _clauses.get(_last).isEmpty()) {
			return;
		}
		for (int _index = 0; _index < _last; _index++) {
			if (!_clauses.get(_index).isEmpty()) {
				return;
			}
		}
		for (int _index = 0; _index < _last; _index++) {
			_clauses.set(_index, _clauses.get(_last));
		}
	}

	/** The periods that run on a given Calendar.DAY_OF_WEEK, in the order published. */
	public List<Period> periodsOn(int _calendarDay) {
		List<Period> _open = new ArrayList<>(periods.size());
		for (Period _period : periods) {
			if (_period.days.contains(_calendarDay)) {
				_open.add(_period);
			}
		}
		return _open;
	}

	public boolean opensOn(int _calendarDay) {
		return !periodsOn(_calendarDay).isEmpty();
	}

	private static Period parsePeriod(String _range, String _clause) {
		String[] _ends = RANGE_SPLIT.split(_range);
		if (_ends.length != 2) {
			return null;
		}

		int _start = parseTime(_ends[0]);
		int _end = parseTime(_ends[1]);
		if (_start < 0 || _end < 0 || _end <= _start) {
			return null;
		}

		Set<Integer> _days = parseDays(_clause);
		return _days == null ? null : new Period(_start, _end, _days);
	}

	/**
	 * "2.30pm" to 870, or -1.
	 *
	 * The source writes half hours with a full stop about as often as a colon, and both
	 * appear in the same chamber list, so both are read.
	 */
	private static int parseTime(String _raw) {
		Matcher _matcher = TIME.matcher(_raw.trim());
		if (!_matcher.matches()) {
			return -1;
		}

		int _hour = Integer.parseInt(_matcher.group(1));
		int _minute = _matcher.group(2) == null ? 0 : Integer.parseInt(_matcher.group(2));
		if (_hour < 1 || _hour > 12 || _minute > 59) {
			return -1;
		}

		boolean _pm = _matcher.group(3).toLowerCase(Locale.ROOT).equals("pm");
		if (_pm && _hour != 12) {
			_hour += 12;
		}
		if (!_pm && _hour == 12) {
			_hour = 0;
		}
		return _hour * 60 + _minute;
	}

	/**
	 * The parenthesised day clause, or every day when there is none.
	 *
	 * An absent clause means the source said nothing about days, which the profile
	 * screen already treats as "not mentioned" rather than "closed". Slots follow the
	 * same reading: every day, until the source narrows it.
	 *
	 * Returns null when the clause names something that is not a day, which is how
	 * "(Closed: Friday & Govt. Holidays)" and "(Every Friday morning)" fail rather than
	 * silently losing half their meaning.
	 */
	private static Set<Integer> parseDays(String _clause) {
		String _text = _clause.trim().toLowerCase(Locale.ROOT);
		if (_text.isEmpty()) {
			return allDays();
		}

		_text = LEADING_OPEN.matcher(_text).replaceFirst("");
		if (_text.equals("everyday") || _text.equals("every day")
				|| _text.equals("daily") || _text.equals("all days")) {
			return allDays();
		}

		boolean _negate = false;
		Matcher _negative = LEADING_NEGATIVE.matcher(_text);
		if (_negative.find()) {
			_text = _negative.replaceFirst("");
			_negate = true;
		} else {
			_text = LEADING_POSITIVE.matcher(_text).replaceFirst("");
		}

		Set<Integer> _named = namedDays(_text.replace(" and ", ",").replace("&", ","));
		if (_named == null || _named.isEmpty()) {
			return null;
		}

		if (!_negate) {
			return _named;
		}
		Set<Integer> _open = allDays();
		_open.removeAll(_named);
		return _open.isEmpty() ? null : _open;
	}

	/** "sat, mon, tue" or "sat to thu", or null if any token is not a day. */
	private static Set<Integer> namedDays(String _text) {
		Matcher _range = DAY_RANGE.matcher(_text.trim());
		if (_range.matches()) {
			Integer _from = DAYS.get(_range.group(1));
			Integer _to = DAYS.get(_range.group(2));
			if (_from == null || _to == null) {
				return null;
			}
			Set<Integer> _days = new HashSet<>(8);
			int _day = _from;
			while (true) {
				_days.add(_day);
				if (_day == _to) {
					break;
				}
				_day = _day % 7 + 1;
			}
			return _days;
		}

		Set<Integer> _days = new HashSet<>(8);
		for (String _token : _text.split(",")) {
			String _name = _token.trim().replaceAll("[^a-z]", "");
			if (_name.isEmpty()) {
				continue;
			}
			Integer _day = DAYS.get(_name);
			if (_day == null) {
				return null;
			}
			_days.add(_day);
		}
		return _days;
	}

	private static Set<Integer> allDays() {
		Set<Integer> _days = new HashSet<>(8);
		for (int _day = Calendar.SUNDAY; _day <= Calendar.SATURDAY; _day++) {
			_days.add(_day);
		}
		return _days;
	}
}
