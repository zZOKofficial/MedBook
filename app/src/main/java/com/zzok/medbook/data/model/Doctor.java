package com.zzok.medbook.data.model;

import java.util.List;

/** A full doctor profile, as the detail screen needs it. */
public final class Doctor {

	public final long id;
	public final String displayName;
	public final String specialty;
	public final String designation;
	public final String institution;
	public final String degrees;
	public final String experienceText;
	public final String bmdcNumber;
	/**
	 * Null means the source shows no rating at all, which is not the same as a rating
	 * of zero. Callers hide the rating rather than drawing an empty five stars.
	 */
	public final Double averageRating;
	public final Integer reviewCount;
	public final boolean verified;
	public final String city;
	/** Biography as published. The Bengali one is never a translation of the English. */
	public final String about;
	public final String aboutBn;
	public final String portrait;
	public final List<Chamber> chambers;

	public Doctor(long _id, String _displayName, String _specialty, String _designation,
			String _institution, String _degrees, String _experienceText, String _bmdcNumber,
			Double _averageRating, Integer _reviewCount, boolean _verified, String _city,
			String _about, String _aboutBn, String _portrait, List<Chamber> _chambers) {
		id = _id;
		displayName = _displayName;
		specialty = _specialty;
		designation = _designation;
		institution = _institution;
		degrees = _degrees;
		experienceText = _experienceText;
		bmdcNumber = _bmdcNumber;
		averageRating = _averageRating;
		reviewCount = _reviewCount;
		verified = _verified;
		city = _city;
		about = _about;
		aboutBn = _aboutBn;
		portrait = _portrait;
		chambers = _chambers;
	}
}
