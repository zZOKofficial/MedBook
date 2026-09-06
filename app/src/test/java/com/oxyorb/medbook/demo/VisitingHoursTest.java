package com.oxyorb.medbook.demo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Calendar;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * The visiting-hours parser, against the wording the source actually publishes.
 *
 * Every string in the "reads" tests below is a real value from the chambers table, not
 * an invented one. The parser exists to decide which of 9,350 chambers can be offered a
 * slot grid at all, so the cases that matter most are the ones it must refuse: a chamber
 * that says to call has no readable hours, and guessing some for it would put a doctor
 * in a chair at a time nobody agreed to.
 *
 * These run off a device because the parser is pure Java over a string, which is the
 * same reason department search is tested here.
 */
public class VisitingHoursTest {

	private static final int SUN = Calendar.SUNDAY;
	private static final int MON = Calendar.MONDAY;
	private static final int TUE = Calendar.TUESDAY;
	private static final int WED = Calendar.WEDNESDAY;
	private static final int THU = Calendar.THURSDAY;
	private static final int FRI = Calendar.FRIDAY;
	private static final int SAT = Calendar.SATURDAY;

	private static Set<Integer> days(Integer... _days) {
		return new HashSet<>(Arrays.asList(_days));
	}

	private static int at(int _hour, int _minute) {
		return _hour * 60 + _minute;
	}

	// -- the ordinary shapes ------------------------------------------------------

	@Test
	public void readsOneRangeClosedOnFriday() {
		VisitingHours _hours = VisitingHours.parse("10am to 8pm (Closed: Friday)");

		assertNotNull(_hours);
		assertEquals(1, _hours.periods.size());
		assertEquals(at(10, 0), _hours.periods.get(0).startMinute);
		assertEquals(at(20, 0), _hours.periods.get(0).endMinute);
		assertEquals(days(SUN, MON, TUE, WED, THU, SAT), _hours.periods.get(0).days);
		assertFalse(_hours.opensOn(FRI));
		assertTrue(_hours.opensOn(SAT));
	}

	@Test
	public void readsTwoRangesInOneDay() {
		VisitingHours _hours = VisitingHours.parse("10am to 1pm & 4pm to 8pm (Everyday)");

		assertNotNull(_hours);
		assertEquals(2, _hours.periods.size());
		assertEquals(at(10, 0), _hours.periods.get(0).startMinute);
		assertEquals(at(13, 0), _hours.periods.get(0).endMinute);
		assertEquals(at(16, 0), _hours.periods.get(1).startMinute);
		assertEquals(at(20, 0), _hours.periods.get(1).endMinute);
		assertEquals(2, _hours.periodsOn(WED).size());
	}

	@Test
	public void readsADayList() {
		VisitingHours _hours = VisitingHours.parse("3pm to 8pm (Sat, Mon, Tue, Thur)");

		assertNotNull(_hours);
		assertEquals(days(SAT, MON, TUE, THU), _hours.periods.get(0).days);
		assertFalse(_hours.opensOn(SUN));
	}

	@Test
	public void readsOnlyOneDay() {
		VisitingHours _hours = VisitingHours.parse("10am to 1pm (Only Friday)");

		assertNotNull(_hours);
		assertEquals(days(FRI), _hours.periods.get(0).days);
	}

	@Test
	public void readsEveryday() {
		VisitingHours _hours = VisitingHours.parse("3pm to 9pm (Everyday)");

		assertNotNull(_hours);
		assertEquals(7, _hours.periods.get(0).days.size());
	}

	/** Two sessions on different days, which must not be flattened into each other. */
	@Test
	public void readsADifferentSessionPerDay() {
		VisitingHours _hours = VisitingHours.parse("2pm to 10pm (Thu) & 8am to 4pm (Fri)");

		assertNotNull(_hours);
		assertEquals(days(THU), _hours.periods.get(0).days);
		assertEquals(days(FRI), _hours.periods.get(1).days);
		assertEquals(1, _hours.periodsOn(THU).size());
		assertEquals(at(8, 0), _hours.periodsOn(FRI).get(0).startMinute);
	}

	@Test
	public void readsADayRangeThatWrapsPastSaturday() {
		VisitingHours _hours =
			VisitingHours.parse("4pm to 9pm (Sat to Thu) & 9am to 4pm (Friday)");

		assertNotNull(_hours);
		assertEquals(days(SAT, SUN, MON, TUE, WED, THU), _hours.periods.get(0).days);
		assertEquals(days(FRI), _hours.periods.get(1).days);
	}

	@Test
	public void readsHalfHoursWrittenWithAFullStop() {
		VisitingHours _hours = VisitingHours.parse("2.30pm to 7pm (Every Thursday)");

		assertNotNull(_hours);
		assertEquals(at(14, 30), _hours.periods.get(0).startMinute);
		assertEquals(days(THU), _hours.periods.get(0).days);
	}

	@Test
	public void readsNoonAndMidnightTheRightWayRound() {
		VisitingHours _noon = VisitingHours.parse("12pm to 2pm");
		VisitingHours _night = VisitingHours.parse("12am to 3am");

		assertNotNull(_noon);
		assertEquals(at(12, 0), _noon.periods.get(0).startMinute);
		assertNotNull(_night);
		assertEquals(at(0, 0), _night.periods.get(0).startMinute);
	}

	/** Thu, Thur and Thurs all appear in the source, sometimes in neighbouring rows. */
	@Test
	public void treatsEveryThursdayAbbreviationAsTheSameDay() {
		assertEquals(days(THU), VisitingHours.parse("5pm to 8pm (Thu)").periods.get(0).days);
		assertEquals(days(THU), VisitingHours.parse("5pm to 8pm (Thur)").periods.get(0).days);
		assertEquals(days(THU), VisitingHours.parse("5pm to 8pm (Thurs)").periods.get(0).days);
		assertEquals(days(THU), VisitingHours.parse("5pm to 8pm (thursday)").periods.get(0).days);
	}

	/** A clause written once at the end governs the sessions before it. */
	@Test
	public void spreadsATrailingClauseAcrossEarlierSessions() {
		VisitingHours _hours = VisitingHours.parse("10am to 1pm & 5pm to 9pm (Closed: Friday)");

		assertNotNull(_hours);
		assertEquals(2, _hours.periods.size());
		assertFalse(_hours.periods.get(0).days.contains(FRI));
		assertFalse(_hours.periods.get(1).days.contains(FRI));
		assertEquals(0, _hours.periodsOn(FRI).size());
	}

	// -- the refusals -------------------------------------------------------------

	/** 428 chambers say this. It is the source declining to publish, not a parse bug. */
	@Test
	public void refusesWhenTheSourceSaysToCall() {
		assertNull(VisitingHours.parse("Unknown. Please call to know visiting hour"));
		assertNull(VisitingHours.parse("Please Call for Appointment Before 1 Days"));
	}

	@Test
	public void refusesAVagueTimeOfDay() {
		assertNull(VisitingHours.parse("Evening (Closed: Friday)"));
		assertNull(VisitingHours.parse("Morning (Closed: Friday)"));
	}

	@Test
	public void refusesAMistypedTime() {
		assertNull(VisitingHours.parse("4m to 9pm (Closed: Friday)"));
	}

	/** Without am or pm, "10 to 8" could be read two ways, and either would be a guess. */
	@Test
	public void refusesATimeWithNoMeridiem() {
		assertNull(VisitingHours.parse("10 to 8 (Closed: Friday)"));
	}

	@Test
	public void refusesARangeThatEndsBeforeItStarts() {
		assertNull(VisitingHours.parse("9pm to 9am"));
		assertNull(VisitingHours.parse("5pm to 5pm"));
	}

	@Test
	public void refusesADayClauseItCannotFullyRead() {
		assertNull(VisitingHours.parse("5pm to 8pm (Closed: Friday & Govt. Holidays)"));
	}

	@Test
	public void refusesDaysWithNoHours() {
		assertNull(VisitingHours.parse("Saturday to Thursday (Closed: Friday)"));
		assertNull(VisitingHours.parse("Every Friday"));
	}

	@Test
	public void refusesNothingAtAll() {
		assertNull(VisitingHours.parse(null));
		assertNull(VisitingHours.parse(""));
		assertNull(VisitingHours.parse("   "));
	}

	/** A chamber closed every day of the week is a contradiction, not a schedule. */
	@Test
	public void refusesAClauseThatClosesEveryDay() {
		assertNull(VisitingHours.parse(
			"5pm to 8pm (Closed: Sat, Sun, Mon, Tue, Wed, Thu, Fri)"));
	}
}
