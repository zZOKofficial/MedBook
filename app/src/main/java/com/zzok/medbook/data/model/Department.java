package com.zzok.medbook.data.model;

/** One of MedBook's departments, with the clinical family it is grouped under. */
public final class Department {

	public final long id;
	public final String key;
	public final String name;
	public final String familyName;
	public final int doctorCount;

	public Department(long _id, String _key, String _name, String _familyName, int _doctorCount) {
		id = _id;
		key = _key;
		name = _name;
		familyName = _familyName;
		doctorCount = _doctorCount;
	}
}
