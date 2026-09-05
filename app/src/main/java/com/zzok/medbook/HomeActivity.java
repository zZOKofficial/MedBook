package com.zzok.medbook;

import android.animation.*;
import android.app.*;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.graphics.Typeface;
import android.graphics.drawable.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.text.*;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.webkit.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.zzok.medbook.databinding.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.ArrayList;
import java.util.regex.*;
import org.json.*;

public class HomeActivity extends AppCompatActivity {
	
	private HomeBinding binding;
	
	private ArrayList<String> specialty = new ArrayList<>();
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		binding = HomeBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		initialize(_savedInstanceState);
		initializeLogic();
	}
	
	private void initialize(Bundle _savedInstanceState) {
	}
	
	private void initializeLogic() {
		binding.medbookLogo.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_bold.ttf"), Typeface.NORMAL);
		binding.searchResultSubtitle.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.NORMAL);
		binding.searchDocs.setBackground(new GradientDrawable() { public GradientDrawable getIns(int a, int b) { this.setCornerRadius(a); this.setColor(b); return this; } }.getIns((int)50, SketchwareUtil.getMaterialColor(HomeActivity.this, com.google.android.material.R.attr.colorSurfaceVariant)));
		binding.accidentAndEmegerency.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.anesthesiaAndPainMedicine.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.cancerCareCentre.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.cardiologyCareCentre.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.cardiothoracicAndVascularSurgery.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.cardiothoracicAnaesthesia.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.childDevelopmentCentre.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.counsellingCentre.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.criticalCareUnits.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.dentalAndMaxillofacialSurgery.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.dermatologyAndVenereology.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.diabetologyAndEndocrinology.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.diagnosticAndInterventionalRadiology.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.dieteticsAndNutrition.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.ENTAndHeadNeckSurgery.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.fertilityCentre.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.gastroenterologyAndHepatology.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.generalAndLaparoscopicSurgery.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.haematologyAndStemCellTransplant.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.hepatobiliaryPancreaticAndLiverTransplant.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.internalMedicine.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.kidneyTransplantProgram.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.lithotripsy.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.medicalOncology.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.neonatology.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.nephrology.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.neurology.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.neurosurgery.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.nuclearMedicine.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.obstetricsAndGynaecology.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.ophthalmology.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.orthopaedics.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.paediatrics.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.paediatricCardiology.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.paediatricSurgeryAndUrology.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.paediatricsAndNeonatology.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.physicalMedicineAndRehabilitation.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.plasticReconstructiveAndCosmeticSurgery.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.psychiatry.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.radiationOncology.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.respiratoryMedicine.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.rheumatology.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.thoracicSurgery.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.thyroidAndHeadNeckOncosurgery.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.transfusionMedicine.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
		binding.urology.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), Typeface.BOLD);
	}
	
}
