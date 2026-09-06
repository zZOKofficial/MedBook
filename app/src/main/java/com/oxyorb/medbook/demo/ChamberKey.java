package com.oxyorb.medbook.demo;

import com.oxyorb.medbook.data.model.Chamber;

/**
 * A name for one chamber, stable enough to seed a schedule and to store a booking under.
 *
 * The directory publishes no chamber id to borrow, so this is the doctor's row id and
 * the chamber's seq: both are real columns, both are already what the repository sorts
 * chambers by, and together they are unique by construction within one dataset.
 *
 * A hash of the chamber's name and address was the other candidate and is worse. It
 * would buy stability across a dataset rebuild, which is stability this does not need --
 * DemoStore drops every booking when the dataset stamp changes anyway, because a doctor
 * row id means nothing after a rebuild either. And it would collide: five doctors in the
 * directory publish the same chamber twice under one name and address, differing only in
 * their visiting hours, and a hash would quietly merge a morning sitting with an evening
 * one and put a booking in the wrong room.
 *
 * Within one installed dataset this is exactly as stable as Doctor.id, which is what the
 * whole directory is already keyed on. medbook.db is opened read-only and never written,
 * so there is no way for seq to move under a running install.
 */
public final class ChamberKey {

	private ChamberKey() {
	}

	public static String of(long _doctorId, Chamber _chamber) {
		return of(_doctorId, _chamber.seq);
	}

	public static String of(long _doctorId, int _seq) {
		return _doctorId + ":" + _seq;
	}
}
