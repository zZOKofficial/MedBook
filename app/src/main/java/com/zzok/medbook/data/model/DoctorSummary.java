package com.zzok.medbook.data.model;

/** What a doctor row in a list needs, and nothing more. */
public final class DoctorSummary {

	public final long id;
	public final String displayName;
	/**
	 * The site's own unsplit wording. The split specialty terms exist for filtering;
	 * this is the one that is fit to show a reader.
	 */
	public final String specialty;
	public final String degrees;
	public final String city;
	/** Filename in assets/portraits, or null when the doctor has no real portrait. */
	public final String portrait;
	public final boolean verified;

	public DoctorSummary(long _id, String _displayName, String _specialty, String _degrees,
			String _city, String _portrait, boolean _verified) {
		id = _id;
		displayName = _displayName;
		specialty = _specialty;
		degrees = _degrees;
		city = _city;
		portrait = _portrait;
		verified = _verified;
	}
}
