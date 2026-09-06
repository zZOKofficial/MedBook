package com.oxyorb.medbook.data.model;

import java.util.List;

/** One place a doctor sees patients. A doctor may have up to seven. */
public final class Chamber {

	public final String name;
	/** Verbatim from the source. Never rewritten, reformatted or re-ordered. */
	public final String address;
	public final String city;
	public final String area;
	/**
	 * Verbatim opening hours, and the authority on when the doctor sits.
	 *
	 * {@link #days} is filled only from phrasings the scraper recognised, so it is
	 * often empty even though this is not. A day absent from that list is not a day
	 * the doctor is known to be away - it is a day the source never mentioned.
	 */
	public final String visitingHours;
	public final List<String> days;
	public final List<String> closedDays;
	/**
	 * Appointment numbers as published. Mostly +880 numbers, but some hospitals list
	 * a four or five digit hotline, which is kept exactly as it appears - prefixing
	 * one with a country code would invent a number that does not exist.
	 */
	public final List<String> phones;

	public Chamber(String _name, String _address, String _city, String _area, String _visitingHours,
			List<String> _days, List<String> _closedDays, List<String> _phones) {
		name = _name;
		address = _address;
		city = _city;
		area = _area;
		visitingHours = _visitingHours;
		days = _days;
		closedDays = _closedDays;
		phones = _phones;
	}
}
