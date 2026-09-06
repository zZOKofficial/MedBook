package com.oxyorb.medbook.demo;

import android.content.Context;

import com.oxyorb.medbook.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Names for the people the demo invents, and the one of them who does not turn up.
 *
 * Nobody real is named here. The directory carries doctors, not patients, and never
 * will. These come from a resource array so they localise, which is a departure from the
 * rule the rest of the app follows -- the doctor directory stays English because the
 * source publishes no Bengali and translating a scraped name would be inventing one.
 * These are already invented. There is no source to be faithful to, and a Bengali
 * chamber list full of Latin names would look like exactly what it is.
 *
 * Chosen by the same seed the schedule uses, so the fifth serial belongs to the same
 * person every time the day is opened.
 */
public final class DemoPatients {

	private DemoPatients() {
	}

	/**
	 * The day's patients, in serial order.
	 *
	 * A shuffle rather than a draw per serial, because drawing independently repeats:
	 * seven names out of twenty-four collide about half the time, and the same person
	 * twice in one afternoon is the first thing a chamber manager would query. The pool
	 * holds exactly as many names as a day holds slots, so within a day nobody repeats.
	 *
	 * Collections.shuffle over a seeded Random is specified down to the element order,
	 * so this is the same list on every device, like everything else here.
	 */
	public static List<String> roster(Context _context, String _chamberKey, int _epochDay) {
		List<String> _names = new ArrayList<>(Arrays.asList(
			_context.getResources().getStringArray(R.array.demo_patient_names)));
		Collections.shuffle(_names, new Random(seed(_chamberKey, _epochDay, 0)));
		return _names;
	}

	/** The patient holding one serial, from a roster built for that day. */
	public static String name(List<String> _roster, int _serial) {
		if (_roster.isEmpty()) {
			return "";
		}
		return _roster.get((_serial - 1) % _roster.size());
	}

	/**
	 * Which serial did not attend, or 0 for none.
	 *
	 * One a day, and only ever a serial whose time has already gone. A no-show in
	 * tomorrow's book would be nonsense, and it is the kind of nonsense a chamber
	 * manager notices in the first thirty seconds.
	 */
	public static int noShowSerial(String _chamberKey, int _epochDay, int _slotCount) {
		if (_slotCount <= 0) {
			return 0;
		}
		Random _random = new Random(seed(_chamberKey, _epochDay, -1));
		return 1 + _random.nextInt(_slotCount);
	}

	private static long seed(String _chamberKey, int _epochDay, int _serial) {
		long _hash = 0xcbf29ce484222325L;
		String _text = _chamberKey + "|" + _epochDay + "|" + _serial;
		for (int _index = 0; _index < _text.length(); _index++) {
			_hash ^= _text.charAt(_index) & 0xff;
			_hash *= 0x100000001b3L;
		}
		return _hash;
	}
}
