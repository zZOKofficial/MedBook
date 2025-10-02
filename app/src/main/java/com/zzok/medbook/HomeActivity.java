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
import androidx.fragment.app.DialogFragment;
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
		binding.medbookLogo.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_bold.ttf"), 0);
		binding.searchResultSubtitle.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 0);
		binding.searchDocs.setBackground(new GradientDrawable() { public GradientDrawable getIns(int a, int b) { this.setCornerRadius(a); this.setColor(b); return this; } }.getIns((int)50, SketchwareUtil.getMaterialColor(HomeActivity.this, R.attr.colorSurfaceVariant)));
		binding.accidentAndEmegerency.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.anesthesiaAndPainMedicine.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.cancerCareCentre.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.cardiologyCareCentre.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.cardiothoracicAndVascularSurgery.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.cardiothoracicAnaesthesia.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.childDevelopmentCentre.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.counsellingCentre.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.criticalCareUnits.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.dentalAndMaxillofacialSurgery.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.dermatologyAndVenereology.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.diabetologyAndEndocrinology.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.diagnosticAndInterventionalRadiology.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.dieteticsAndNutrition.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.ENTAndHeadNeckSurgery.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.fertilityCentre.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.gastroenterologyAndHepatology.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.generalAndLaparoscopicSurgery.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.haematologyAndStemCellTransplant.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.hepatobiliaryPancreaticAndLiverTransplant.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.internalMedicine.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.kidneyTransplantProgram.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.lithotripsy.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.medicalOncology.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.neonatology.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.nephrology.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.neurology.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.neurosurgery.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.nuclearMedicine.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.obstetricsAndGynaecology.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.ophthalmology.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.orthopaedics.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.paediatrics.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.paediatricCardiology.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.paediatricSurgeryAndUrology.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.paediatricsAndNeonatology.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.physicalMedicineAndRehabilitation.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.plasticReconstructiveAndCosmeticSurgery.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.psychiatry.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.radiationOncology.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.respiratoryMedicine.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.rheumatology.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.thoracicSurgery.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.thyroidAndHeadNeckOncosurgery.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.transfusionMedicine.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
		binding.urology.setTypeface(Typeface.createFromAsset(getAssets(),"fonts/sf_pro_display_regular.ttf"), 1);
	}
	
}