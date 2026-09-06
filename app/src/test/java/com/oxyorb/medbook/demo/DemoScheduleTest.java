package com.oxyorb.medbook.demo;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * The simulated appointment book.
 *
 * Two properties carry the whole feature and both are asserted here. It has to be
 * reproducible -- the same chamber on the same day gives the same book on every device,
 * or the demo contradicts itself the second time it is opened -- and a booking made on
 * it has to leave everything around it alone, or the grid reshuffles under the person
 * being shown it.
 */
public class DemoScheduleTest {

	/** A Sunday. 1 January 2024 was a Monday, so this is the day before. */
	private static final int SUNDAY = EpochDay.of(2023, 11, 31);

	private static final String KEY = "4210:0";

	private static VisitingHours everyDay() {
		return VisitingHours.parse("10am to 1pm & 5pm to 9pm (Everyday)");
	}

	private static List<DemoSchedule.Slot> slots(int _daysAhead, boolean _verified, int _reviews,
			Set<Integer> _yours) {
		return DemoSchedule.slotsFor(KEY, everyDay(), SUNDAY + _daysAhead, _daysAhead,
			_verified, _reviews, _yours);
	}

	private static Set<Integer> none() {
		return Collections.emptySet();
	}

	// -- reproducibility ----------------------------------------------------------

	@Test
	public void givesTheSameBookTwice() {
		List<DemoSchedule.Slot> _first = slots(0, true, 300, none());
		List<DemoSchedule.Slot> _second = slots(0, true, 300, none());

		assertEquals(_first.size(), _second.size());
		for (int _index = 0; _index < _first.size(); _index++) {
			assertEquals(_first.get(_index).minuteOfDay, _second.get(_index).minuteOfDay);
			assertEquals(_first.get(_index).serial, _second.get(_index).serial);
			assertEquals(_first.get(_index).state, _second.get(_index).state);
		}
	}

	@Test
	public void givesADifferentBookOnADifferentDay() {
		List<DemoSchedule.Slot> _today = slots(0, true, 300, none());
		List<DemoSchedule.Slot> _tomorrow = slots(1, true, 300, none());

		assertEquals(_today.size(), _tomorrow.size());
		boolean _differs = false;
		for (int _index = 0; _index < _today.size(); _index++) {
			if (_today.get(_index).state != _tomorrow.get(_index).state) {
				_differs = true;
			}
		}
		assertTrue("a different day should not produce the same book", _differs);
	}

	@Test
	public void givesADifferentBookToADifferentChamber() {
		List<DemoSchedule.Slot> _one = DemoSchedule.slotsFor("4210:0", everyDay(), SUNDAY, 0,
			true, 300, none());
		List<DemoSchedule.Slot> _two = DemoSchedule.slotsFor("4210:1", everyDay(), SUNDAY, 0,
			true, 300, none());

		boolean _differs = false;
		for (int _index = 0; _index < _one.size(); _index++) {
			if (_one.get(_index).state != _two.get(_index).state) {
				_differs = true;
			}
		}
		assertTrue("two chambers of one doctor should not share a book", _differs);
	}

	// -- shape --------------------------------------------------------------------

	@Test
	public void numbersSerialsAcrossTheWholeDay() {
		List<DemoSchedule.Slot> _slots = slots(0, true, 300, none());

		assertTrue(_slots.size() > 1);
		for (int _index = 0; _index < _slots.size(); _index++) {
			assertEquals(_index + 1, _slots.get(_index).serial);
		}
	}

	@Test
	public void laysSlotsTwentyMinutesApartWithinASession() {
		List<DemoSchedule.Slot> _slots = DemoSchedule.slotsFor(KEY,
			VisitingHours.parse("10am to 1pm (Everyday)"), SUNDAY, 0, false, 0, none());

		assertEquals(9, _slots.size());
		assertEquals(600, _slots.get(0).minuteOfDay);
		assertEquals(620, _slots.get(1).minuteOfDay);
		assertEquals(760, _slots.get(8).minuteOfDay);
	}

	@Test
	public void neverCapsAboveADaysWorthOfSlots() {
		List<DemoSchedule.Slot> _slots = DemoSchedule.slotsFor(KEY,
			VisitingHours.parse("8am to 8pm (Everyday)"), SUNDAY, 0, true, 900, none());

		assertEquals(DemoSchedule.MAX_SLOTS, _slots.size());
	}

	@Test
	public void sitsOnNoSlotsOnADayTheChamberIsClosed() {
		VisitingHours _hours = VisitingHours.parse("5pm to 9pm (Only Monday)");

		assertTrue(DemoSchedule.slotsFor(KEY, _hours, SUNDAY, 0, true, 300, none()).isEmpty());
		assertFalse(DemoSchedule.slotsFor(KEY, _hours, SUNDAY + 1, 1, true, 300, none()).isEmpty());
	}

	@Test
	public void sitsOnNoSlotsWhenTheHoursCouldNotBeRead() {
		assertTrue(DemoSchedule.slotsFor(KEY, null, SUNDAY, 0, true, 300, none()).isEmpty());
	}

	// -- occupancy ----------------------------------------------------------------

	/** The demo has to survive being tapped, so it always leaves something to tap. */
	@Test
	public void alwaysLeavesSlotsOpen() {
		for (int _daysAhead = 0; _daysAhead < 7; _daysAhead++) {
			List<DemoSchedule.Slot> _slots = slots(_daysAhead, true, 5000, none());
			int _free = _slots.size() - DemoSchedule.occupied(_slots);
			assertTrue("day " + _daysAhead + " left " + _free + " open", _free >= 3);
		}
	}

	@Test
	public void fillsTodayFullerThanNextWeek() {
		List<DemoSchedule.Slot> _today = slots(0, true, 300, none());
		List<DemoSchedule.Slot> _sixDaysOut = slots(6, true, 300, none());

		assertTrue(DemoSchedule.occupied(_today) > DemoSchedule.occupied(_sixDaysOut));
	}

	/** A verified doctor with hundreds of reviews and an empty book is the tell. */
	@Test
	public void fillsAWellRegardedDoctorFuller() {
		List<DemoSchedule.Slot> _known = slots(2, true, 900, none());
		List<DemoSchedule.Slot> _unknown = slots(2, false, 0, none());

		assertTrue(DemoSchedule.occupied(_known) > DemoSchedule.occupied(_unknown));
	}

	// -- the user's own bookings --------------------------------------------------

	@Test
	public void marksYourBookingWithoutDisturbingTheRest() {
		List<DemoSchedule.Slot> _before = slots(0, true, 300, none());

		int _mine = -1;
		for (DemoSchedule.Slot _slot : _before) {
			if (_slot.free()) {
				_mine = _slot.minuteOfDay;
				break;
			}
		}
		assertTrue("expected at least one open slot to book", _mine >= 0);

		Set<Integer> _yours = new HashSet<>();
		_yours.add(_mine);
		List<DemoSchedule.Slot> _after = slots(0, true, 300, _yours);

		assertEquals(_before.size(), _after.size());
		for (int _index = 0; _index < _before.size(); _index++) {
			DemoSchedule.Slot _was = _before.get(_index);
			DemoSchedule.Slot _now = _after.get(_index);
			assertEquals(_was.minuteOfDay, _now.minuteOfDay);
			assertEquals(_was.serial, _now.serial);
			if (_was.minuteOfDay == _mine) {
				assertEquals(DemoSchedule.STATE_YOURS, _now.state);
			} else {
				assertEquals("slot at " + _was.minuteOfDay + " moved", _was.state, _now.state);
			}
		}
	}

	@Test
	public void countsYourBookingTowardsUtilisation() {
		List<DemoSchedule.Slot> _before = slots(3, false, 0, none());
		int _mine = -1;
		for (DemoSchedule.Slot _slot : _before) {
			if (_slot.free()) {
				_mine = _slot.minuteOfDay;
				break;
			}
		}

		Set<Integer> _yours = new HashSet<>();
		_yours.add(_mine);
		List<DemoSchedule.Slot> _after = slots(3, false, 0, _yours);

		assertEquals(DemoSchedule.occupied(_before) + 1, DemoSchedule.occupied(_after));
	}
}
