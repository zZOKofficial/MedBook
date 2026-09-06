package com.oxyorb.medbook.demo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * A day's appointment book for one chamber, invented but never stored.
 *
 * The whole simulated world is a pure function of the chamber, the date and the doctor's
 * standing. Nothing about it is written down, which is what makes it identical every
 * time the demo is opened and identical on the phone in the rep's hand and the one in
 * the hospital's. Only the user's own bookings are persisted, by DemoStore, and they are
 * laid over the top of this.
 *
 * The occupancy numbers below are design constants, not measurements. There is nothing
 * to measure: the directory is a static snapshot with no bookings in it. They are
 * gathered here so they can be retuned after watching a real pitch.
 *
 * What they are shaped to avoid is the two ways a fake appointment book gives itself
 * away. A verified doctor with nine hundred reviews whose day is empty reads as a dead
 * product, and every day of the week looking equally busy reads as noise rather than a
 * booking system. So standing raises the fill, and the fill falls off sharply with
 * distance -- today is nearly full, next week is nearly empty, which is what a real book
 * looks like from the inside.
 */
public final class DemoSchedule {

	/**
	 * Twenty minutes, uniformly.
	 *
	 * Measured over the 8,563 chambers whose hours parse, this yields a median of 9 slots
	 * a day and 24 at the ninetieth percentile, which is a believable sitting. Ten minutes
	 * would double that into a wall of rows nobody runs a chamber by, and half an hour
	 * would give the shortest published sessions six appointments in an afternoon.
	 */
	public static final int SLOT_MINUTES = 20;

	/** Long enough for any real sitting; the few chambers publishing a 21-hour day get cut. */
	public static final int MAX_SLOTS = 24;

	/**
	 * Slots the simulation always leaves open.
	 *
	 * Not realism -- stagecraft. A chamber shown at a hundred percent has nothing to tap,
	 * and the one thing the demo has to survive is being tapped.
	 */
	private static final int MIN_FREE = 3;

	/** How full the book is this many days out. Index 0 is today. */
	private static final double[] FILL_BY_DAY = {0.82, 0.64, 0.50, 0.39, 0.31, 0.24, 0.18};

	private static final double VERIFIED_BONUS = 0.08;
	private static final double REVIEWS_FOR_FULL_BONUS = 800.0;
	private static final double REVIEWS_BONUS = 0.10;

	/** Early serials go first, and a session fills from its end as the day closes in. */
	private static final double WEIGHT_OPENING = 1.7;
	private static final double WEIGHT_CLOSING = 1.35;
	private static final double WEIGHT_MIDDLE = 1.0;

	public static final int STATE_FREE = 0;
	public static final int STATE_TAKEN = 1;
	public static final int STATE_YOURS = 2;

	/** One appointment slot, with the serial a chamber would actually call out. */
	public static final class Slot {

		public final int minuteOfDay;
		public final int serial;
		public final int state;

		Slot(int _minuteOfDay, int _serial, int _state) {
			minuteOfDay = _minuteOfDay;
			serial = _serial;
			state = _state;
		}

		public boolean free() {
			return state == STATE_FREE;
		}
	}

	private DemoSchedule() {
	}

	/**
	 * The day's slots, in order, with the user's own bookings marked.
	 *
	 * Empty when the chamber does not sit that day. The simulated occupancy is chosen
	 * without reference to yourMinutes, so booking a slot never disturbs the ones around
	 * it -- the demo would lose its point if the grid reshuffled every time it was used.
	 *
	 * @param _daysAhead 0 for today, clamped into the week the demo shows.
	 */
	public static List<Slot> slotsFor(String _chamberKey, VisitingHours _hours, int _epochDay,
			int _daysAhead, boolean _verified, int _reviewCount, Set<Integer> _yourMinutes) {
		if (_hours == null) {
			return Collections.emptyList();
		}

		List<Integer> _minutes = new ArrayList<>(MAX_SLOTS);
		List<Double> _weights = new ArrayList<>(MAX_SLOTS);
		for (VisitingHours.Period _period : _hours.periodsOn(EpochDay.dayOfWeek(_epochDay))) {
			addPeriod(_period, _minutes, _weights);
		}
		if (_minutes.isEmpty()) {
			return Collections.emptyList();
		}

		Random _random = new Random(seed(_chamberKey, _epochDay));
		int _taken = takenCount(_minutes.size(), _daysAhead, _verified, _reviewCount, _random);
		Set<Integer> _occupied = pick(_taken, _weights, _random);
		Set<Integer> _yours = _yourMinutes == null ? Collections.<Integer>emptySet() : _yourMinutes;

		List<Slot> _slots = new ArrayList<>(_minutes.size());
		for (int _index = 0; _index < _minutes.size(); _index++) {
			int _minute = _minutes.get(_index);
			int _state = STATE_FREE;
			if (_yours.contains(_minute)) {
				_state = STATE_YOURS;
			} else if (_occupied.contains(_index)) {
				_state = STATE_TAKEN;
			}
			_slots.add(new Slot(_minute, _index + 1, _state));
		}
		return _slots;
	}

	/** Slots that are not free, which is what a chamber means by utilisation. */
	public static int occupied(List<Slot> _slots) {
		int _count = 0;
		for (Slot _slot : _slots) {
			if (!_slot.free()) {
				_count++;
			}
		}
		return _count;
	}

	/**
	 * Serials run 1..n across the whole day rather than restarting each session, which is
	 * how a chamber actually calls them.
	 */
	private static void addPeriod(VisitingHours.Period _period, List<Integer> _minutes,
			List<Double> _weights) {
		int _length = (_period.endMinute - _period.startMinute) / SLOT_MINUTES;
		if (_length <= 0) {
			return;
		}
		int _closingFrom = _length - Math.max(1, _length / 3);
		for (int _index = 0; _index < _length && _minutes.size() < MAX_SLOTS; _index++) {
			_minutes.add(_period.startMinute + _index * SLOT_MINUTES);
			if (_index < 2) {
				_weights.add(WEIGHT_OPENING);
			} else if (_index >= _closingFrom) {
				_weights.add(WEIGHT_CLOSING);
			} else {
				_weights.add(WEIGHT_MIDDLE);
			}
		}
	}

	private static int takenCount(int _total, int _daysAhead, boolean _verified, int _reviewCount,
			Random _random) {
		int _day = Math.max(0, Math.min(FILL_BY_DAY.length - 1, _daysAhead));
		double _fill = FILL_BY_DAY[_day];
		if (_verified) {
			_fill += VERIFIED_BONUS;
		}
		_fill += Math.min(REVIEWS_BONUS, Math.max(0, _reviewCount) / REVIEWS_FOR_FULL_BONUS * REVIEWS_BONUS);
		_fill += (_random.nextDouble() - 0.5) * 0.10;

		if (_day == 0) {
			_fill = Math.max(_fill, 0.35);
		}
		_fill = Math.max(0.0, Math.min(0.95, _fill));

		int _taken = (int) Math.round(_fill * _total);
		return Math.max(0, Math.min(_taken, _total - MIN_FREE));
	}

	/**
	 * Which slots are gone, weighted so a session fills from its edges.
	 *
	 * StrictMath rather than Math is the point of this method. Math.pow is allowed to
	 * differ by a bit between platforms, and one differing bit here reorders the draw and
	 * hands two phones in the same room two different appointment books. StrictMath is
	 * defined to the bit everywhere, which is the only reason any of this is reproducible.
	 */
	private static Set<Integer> pick(int _count, List<Double> _weights, Random _random) {
		Set<Integer> _chosen = new HashSet<>(Math.max(1, _count * 2));
		if (_count <= 0) {
			return _chosen;
		}

		final double[] _keys = new double[_weights.size()];
		List<Integer> _order = new ArrayList<>(_weights.size());
		for (int _index = 0; _index < _weights.size(); _index++) {
			_keys[_index] = StrictMath.pow(_random.nextDouble(), 1.0 / _weights.get(_index));
			_order.add(_index);
		}
		Collections.sort(_order, new Comparator<Integer>() {
			@Override
			public int compare(Integer _left, Integer _right) {
				return Double.compare(_keys[_right], _keys[_left]);
			}
		});

		for (int _index = 0; _index < _count && _index < _order.size(); _index++) {
			_chosen.add(_order.get(_index));
		}
		return _chosen;
	}

	/**
	 * One seed per chamber per day.
	 *
	 * Mixed rather than concatenated so that neighbouring days do not produce visibly
	 * related books -- a rep flipping through the week would notice the same slots going
	 * first every time, which is exactly the tell this is trying to avoid.
	 */
	private static long seed(String _chamberKey, int _epochDay) {
		long _hash = 0xcbf29ce484222325L;
		String _text = _chamberKey + "|" + _epochDay;
		for (int _index = 0; _index < _text.length(); _index++) {
			_hash ^= _text.charAt(_index) & 0xff;
			_hash *= 0x100000001b3L;
		}
		return mix(_hash);
	}

	private static long mix(long _value) {
		long _mixed = _value;
		_mixed = (_mixed ^ (_mixed >>> 30)) * 0xbf58476d1ce4e5b9L;
		_mixed = (_mixed ^ (_mixed >>> 27)) * 0x94d049bb133111ebL;
		return _mixed ^ (_mixed >>> 31);
	}
}
