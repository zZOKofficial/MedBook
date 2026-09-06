package com.oxyorb.medbook.data.model;

/** One of MedBook's departments, with the clinical family it is grouped under. */
public final class Department {

	public final long id;
	public final String key;
	public final String familyKey;
	/**
	 * As the dataset publishes them, always English.
	 *
	 * Kept alongside the display names for two reasons: they are the fallback when a
	 * key has no translation yet, and they are the second axis department search
	 * matches on, so "cardiology" still finds a department while the interface is in
	 * Bengali.
	 */
	public final String name;
	public final String familyName;
	/** What the UI shows: the values-&lt;lang&gt; string for the key, or name. */
	public final String displayName;
	public final String displayFamilyName;
	public final int doctorCount;

	public Department(long _id, String _key, String _familyKey, String _name,
			String _familyName, int _doctorCount) {
		this(_id, _key, _familyKey, _name, _familyName, _name, _familyName, _doctorCount);
	}

	private Department(long _id, String _key, String _familyKey, String _name,
			String _familyName, String _displayName, String _displayFamilyName, int _doctorCount) {
		id = _id;
		key = _key;
		familyKey = _familyKey;
		name = _name;
		familyName = _familyName;
		displayName = _displayName;
		displayFamilyName = _displayFamilyName;
		doctorCount = _doctorCount;
	}

	/** The same department, showing the names the current locale calls it by. */
	public Department withDisplayNames(String _displayName, String _displayFamilyName) {
		return new Department(id, key, familyKey, name, familyName,
			_displayName, _displayFamilyName, doctorCount);
	}
}
