package com.oxyorb.medbook.data;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import com.oxyorb.medbook.data.model.Department;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Department search, which has to work in two languages at once.
 *
 * These live in a unit test rather than on a device because a Bengali query cannot
 * be typed from adb: `input text` is ASCII only and the emulator has no clipboard
 * shell command, so the one axis that most needs proving is the one that cannot be
 * tapped. searchDepartments is a static pure function over a list, which is exactly
 * what makes testing it here possible.
 */
public class DepartmentSearchTest {

	/** Cardiology Care Centre, as it arrives from the dataset and as Bengali shows it. */
	private static final Department CARDIOLOGY = department(
		"cardiology_care_centre", "heart_lungs_blood",
		"Cardiology Care Centre", "Heart, lungs & blood",
		"হৃদরোগ কেন্দ্র",
		"হৃদযন্ত্র, ফুসফুস ও রক্ত");

	private static final Department CARDIOTHORACIC = department(
		"cardiothoracic_and_vascular_surgery", "heart_lungs_blood",
		"Cardiothoracic & Vascular Surgery", "Heart, lungs & blood",
		"কার্ডিওথোরাসিক ও ভাস্কুলার সার্জারি",
		"হৃদযন্ত্র, ফুসফুস ও রক্ত");

	private static final Department NEPHROLOGY = department(
		"nephrology", "kidneys_urinary", "Nephrology", "Kidneys & urinary",
		"কিডনি রোগ",
		"কিডনি ও মূত্রতন্ত্র");

	private static final List<Department> BENGALI =
		Arrays.asList(CARDIOLOGY, CARDIOTHORACIC, NEPHROLOGY);

	private static Department department(String _key, String _familyKey, String _name,
			String _familyName, String _displayName, String _displayFamilyName) {
		return new Department(1L, _key, _familyKey, _name, _familyName, 0)
			.withDisplayNames(_displayName, _displayFamilyName);
	}

	private static List<String> keysOf(List<Department> _departments) {
		List<String> _keys = new ArrayList<>();
		for (Department _department : _departments) {
			_keys.add(_department.key);
		}
		return _keys;
	}

	/** The axis that cannot be tapped: a Bengali query against a Bengali display name. */
	@Test
	public void bengaliQueryMatchesTheBengaliName() {
		// "হৃদরোগ" -- the Bengali for cardiology, as the department list shows it.
		String _query = "হৃদরোগ";
		assertEquals(Arrays.asList("cardiology_care_centre"),
			keysOf(DoctorRepository.searchDepartments(BENGALI, _query)));
	}

	/** "কিডনি" is a shared prefix of both the department and its family. */
	@Test
	public void bengaliQueryMatchesAPartialWord() {
		String _query = "কিডনি";
		assertEquals(Arrays.asList("nephrology"),
			keysOf(DoctorRepository.searchDepartments(BENGALI, _query)));
	}

	/**
	 * The reason the English name is kept alongside the Bengali one. Every doctor
	 * behind these headings is still described in English, so someone searching
	 * "cardiology" while the interface is Bengali is being reasonable.
	 */
	@Test
	public void englishQueryStillMatchesWhileTheInterfaceIsBengali() {
		assertEquals(Arrays.asList("cardiology_care_centre", "cardiothoracic_and_vascular_surgery"),
			keysOf(DoctorRepository.searchDepartments(BENGALI, "cardio")));
	}

	/** Ampersand folding has to survive the move off the database. */
	@Test
	public void andStillMatchesAnAmpersand() {
		assertEquals(Arrays.asList("cardiothoracic_and_vascular_surgery"),
			keysOf(DoctorRepository.searchDepartments(BENGALI, "cardiothoracic and vascular")));
	}

	@Test
	public void anEmptyQueryMatchesNothing() {
		assertTrue(DoctorRepository.searchDepartments(BENGALI, "").isEmpty());
		assertTrue(DoctorRepository.searchDepartments(BENGALI, "   ").isEmpty());
	}

	@Test
	public void anUnrelatedQueryMatchesNothing() {
		assertTrue(DoctorRepository.searchDepartments(BENGALI, "dentistry").isEmpty());
		// "চর্ম" -- dermatology, which is not in this list.
		assertTrue(DoctorRepository.searchDepartments(BENGALI, "চর্ম").isEmpty());
	}

	/**
	 * norm() must treat Bengali as word characters rather than separators. If it ever
	 * stops doing so this fails here rather than as "Bengali search finds nothing",
	 * and it is also the property that has to stay byte-identical to the pipeline's
	 * own norm() in build_db.py.
	 */
	@Test
	public void normalisationKeepsBengaliIntact() {
		String _bengali = "হৃদরোগ";
		assertEquals(_bengali, DoctorRepository.norm(_bengali));
		assertEquals("cardiology care centre", DoctorRepository.norm("Cardiology Care Centre"));
	}
}
