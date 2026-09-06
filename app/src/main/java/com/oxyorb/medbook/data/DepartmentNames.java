package com.oxyorb.medbook.data;

import android.content.res.Resources;

import com.oxyorb.medbook.R;
import com.oxyorb.medbook.data.model.Department;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Department and family names in the language the app is running in.
 *
 * The directory is English by necessity: it is scraped, and no build step can
 * honestly translate the names and chamber addresses of 7,438 doctors. The 45
 * departments and 12 families are the exception, being a closed vocabulary the
 * app owns rather than data it received. That is what departments.key is for.
 *
 * The lookup is a compile-time map, deliberately not Resources.getIdentifier.
 * shrinkResources is on for release builds, and a string reached only by name is
 * invisible to the shrinker, so all 57 would be stripped from release while every
 * debug build still looked perfect. A map also makes a missing key a compile error.
 */
public final class DepartmentNames {

	private static final Map<String, Integer> FAMILIES = new HashMap<>(24);
	private static final Map<String, Integer> DEPARTMENTS = new HashMap<>(64);

	static {
		FAMILIES.put("urgent_critical_care", R.string.family_urgent_critical_care);
		FAMILIES.put("heart_lungs_blood", R.string.family_heart_lungs_blood);
		FAMILIES.put("brain_nerves_mind", R.string.family_brain_nerves_mind);
		FAMILIES.put("digestive_system_liver", R.string.family_digestive_system_liver);
		FAMILIES.put("kidneys_urinary", R.string.family_kidneys_urinary);
		FAMILIES.put("hormones_metabolism", R.string.family_hormones_metabolism);
		FAMILIES.put("bones_joints_movement", R.string.family_bones_joints_movement);
		FAMILIES.put("eyes_ears_teeth_throat", R.string.family_eyes_ears_teeth_throat);
		FAMILIES.put("skin", R.string.family_skin);
		FAMILIES.put("women_children", R.string.family_women_children);
		FAMILIES.put("cancer", R.string.family_cancer);
		FAMILIES.put("general_diagnostic_services", R.string.family_general_diagnostic_services);

		DEPARTMENTS.put("accident_and_emergency", R.string.dept_accident_and_emergency);
		DEPARTMENTS.put("critical_care_units", R.string.dept_critical_care_units);
		DEPARTMENTS.put("anesthesia_and_pain_medicine", R.string.dept_anesthesia_and_pain_medicine);
		DEPARTMENTS.put("cardiology_care_centre", R.string.dept_cardiology_care_centre);
		DEPARTMENTS.put("cardiothoracic_and_vascular_surgery", R.string.dept_cardiothoracic_and_vascular_surgery);
		DEPARTMENTS.put("cardiothoracic_anaesthesia", R.string.dept_cardiothoracic_anaesthesia);
		DEPARTMENTS.put("thoracic_surgery", R.string.dept_thoracic_surgery);
		DEPARTMENTS.put("respiratory_medicine", R.string.dept_respiratory_medicine);
		DEPARTMENTS.put("haematology_and_stem_cell_transplant", R.string.dept_haematology_and_stem_cell_transplant);
		DEPARTMENTS.put("transfusion_medicine", R.string.dept_transfusion_medicine);
		DEPARTMENTS.put("neurology", R.string.dept_neurology);
		DEPARTMENTS.put("neurosurgery", R.string.dept_neurosurgery);
		DEPARTMENTS.put("psychiatry", R.string.dept_psychiatry);
		DEPARTMENTS.put("counselling_centre", R.string.dept_counselling_centre);
		DEPARTMENTS.put("gastroenterology_and_hepatology", R.string.dept_gastroenterology_and_hepatology);
		DEPARTMENTS.put("hepatobiliary_pancreatic_and_liver_transplant", R.string.dept_hepatobiliary_pancreatic_and_liver_transplant);
		DEPARTMENTS.put("general_and_laparoscopic_surgery", R.string.dept_general_and_laparoscopic_surgery);
		DEPARTMENTS.put("nephrology", R.string.dept_nephrology);
		DEPARTMENTS.put("kidney_transplant_program", R.string.dept_kidney_transplant_program);
		DEPARTMENTS.put("urology", R.string.dept_urology);
		DEPARTMENTS.put("lithotripsy", R.string.dept_lithotripsy);
		DEPARTMENTS.put("diabetology_and_endocrinology", R.string.dept_diabetology_and_endocrinology);
		DEPARTMENTS.put("thyroid_and_head_neck_oncosurgery", R.string.dept_thyroid_and_head_neck_oncosurgery);
		DEPARTMENTS.put("dietetics_and_nutrition", R.string.dept_dietetics_and_nutrition);
		DEPARTMENTS.put("orthopaedics", R.string.dept_orthopaedics);
		DEPARTMENTS.put("rheumatology", R.string.dept_rheumatology);
		DEPARTMENTS.put("physical_medicine_and_rehabilitation", R.string.dept_physical_medicine_and_rehabilitation);
		DEPARTMENTS.put("ophthalmology", R.string.dept_ophthalmology);
		DEPARTMENTS.put("ent_and_head_neck_surgery", R.string.dept_ent_and_head_neck_surgery);
		DEPARTMENTS.put("dental_and_maxillofacial_surgery", R.string.dept_dental_and_maxillofacial_surgery);
		DEPARTMENTS.put("dermatology_and_venereology", R.string.dept_dermatology_and_venereology);
		DEPARTMENTS.put("plastic_reconstructive_and_cosmetic_surgery", R.string.dept_plastic_reconstructive_and_cosmetic_surgery);
		DEPARTMENTS.put("obstetrics_and_gynaecology", R.string.dept_obstetrics_and_gynaecology);
		DEPARTMENTS.put("fertility_centre", R.string.dept_fertility_centre);
		DEPARTMENTS.put("paediatrics", R.string.dept_paediatrics);
		DEPARTMENTS.put("paediatrics_and_neonatology", R.string.dept_paediatrics_and_neonatology);
		DEPARTMENTS.put("child_development_centre", R.string.dept_child_development_centre);
		DEPARTMENTS.put("paediatric_surgery_and_urology", R.string.dept_paediatric_surgery_and_urology);
		DEPARTMENTS.put("paediatric_cardiology", R.string.dept_paediatric_cardiology);
		DEPARTMENTS.put("cancer_care_centre", R.string.dept_cancer_care_centre);
		DEPARTMENTS.put("medical_oncology", R.string.dept_medical_oncology);
		DEPARTMENTS.put("radiation_oncology", R.string.dept_radiation_oncology);
		DEPARTMENTS.put("internal_medicine", R.string.dept_internal_medicine);
		DEPARTMENTS.put("diagnostic_and_interventional_radiology", R.string.dept_diagnostic_and_interventional_radiology);
		DEPARTMENTS.put("nuclear_medicine", R.string.dept_nuclear_medicine);
	}

	private DepartmentNames() {
	}

	/**
	 * Each department with the names the current locale calls it by.
	 *
	 * A key with no string falls back to the dataset name. A dataset that adds a
	 * department before values-bn catches up should read as one untranslated row,
	 * not as a crash and not as a raw resource key.
	 */
	public static List<Department> localize(Resources _resources, List<Department> _departments) {
		List<Department> _localized = new ArrayList<>(_departments.size());
		for (Department _department : _departments) {
			_localized.add(_department.withDisplayNames(
				lookup(_resources, DEPARTMENTS, _department.key, _department.name),
				lookup(_resources, FAMILIES, _department.familyKey, _department.familyName)));
		}
		return _localized;
	}

	/** The name for one department key, for callers holding a key and no Department. */
	public static String departmentName(Resources _resources, String _key, String _fallback) {
		return lookup(_resources, DEPARTMENTS, _key, _fallback);
	}

	private static String lookup(Resources _resources, Map<String, Integer> _map,
			String _key, String _fallback) {
		Integer _id = _key == null ? null : _map.get(_key);
		return _id == null ? _fallback : _resources.getString(_id);
	}
}
