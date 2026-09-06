package com.oxyorb.medbook;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.button.MaterialButton;
import com.oxyorb.medbook.data.DoctorRepository;
import com.oxyorb.medbook.data.PortraitLoader;
import com.oxyorb.medbook.data.model.Chamber;
import com.oxyorb.medbook.demo.VisitingHours;
import com.oxyorb.medbook.settings.SettingsStore;
import com.oxyorb.medbook.data.model.Doctor;
import com.oxyorb.medbook.databinding.*;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * One doctor's full profile.
 *
 * Every field here comes straight from the source and is shown as published. Nothing
 * is inferred, reformatted or filled in: an address is verbatim, opening hours are
 * verbatim, and a fact the source does not state simply does not appear.
 */
public class DoctorDetailActivity extends AppCompatActivity {

	private static final String EXTRA_ID = "doctor_id";

	private DoctorDetailBinding binding;
	private final ExecutorService worker = Executors.newSingleThreadExecutor();
	/** Kept because the demo's chamber rows need it long after the profile has loaded. */
	private long doctorId;

	public static Intent intentFor(Context _context, long _doctorId) {
		Intent _intent = new Intent(_context, DoctorDetailActivity.class);
		_intent.putExtra(EXTRA_ID, _doctorId);
		return _intent;
	}

	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		binding = DoctorDetailBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		applyWindowInsets();
		binding.backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				finish();
			}
		});

		final long _id = getIntent().getLongExtra(EXTRA_ID, -1L);
		doctorId = _id;
		worker.execute(new Runnable() {
			@Override
			public void run() {
				final DoctorRepository _repository = DoctorRepository.open(DoctorDetailActivity.this);
				final Doctor _doctor = _repository == null ? null : _repository.doctor(_id);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (isFinishing() || isDestroyed()) {
							return;
						}
						if (_doctor == null) {
							finish();
							return;
						}
						bind(_doctor);
					}
				});
			}
		});
	}

	private void bind(Doctor _doctor) {
		binding.doctorName.setText(_doctor.displayName);
		binding.doctorPortrait.setContentDescription(
			getString(R.string.portrait_of, _doctor.displayName));
		PortraitLoader.get(this).load(binding.doctorPortrait, _doctor.portrait, _doctor.displayName);

		setOrHide(binding.doctorSpecialty, _doctor.specialty);
		setOrHide(binding.doctorDegrees, _doctor.degrees);
		setOrHide(binding.doctorDesignation, _doctor.designation);
		setOrHide(binding.doctorInstitution, _doctor.institution);
		setOrHide(binding.doctorExperience, _doctor.experienceText);
		setOrHide(binding.doctorBmdc, _doctor.bmdcNumber == null
			? null : getString(R.string.doctor_bmdc, _doctor.bmdcNumber));

		// A null rating means the source shows no rating at all, which is not a rating
		// of zero. 120 doctors are in that state, and drawing them an empty score would
		// read as a bad one.
		if (_doctor.averageRating == null) {
			binding.doctorRating.setVisibility(View.GONE);
		} else {
			binding.doctorRating.setVisibility(View.VISIBLE);
			binding.doctorRating.setText(getString(R.string.doctor_rating,
				_doctor.averageRating, _doctor.reviewCount == null ? 0 : _doctor.reviewCount));
		}

		bindChambers(_doctor);

		boolean _hasAbout = notEmpty(_doctor.about) || notEmpty(_doctor.aboutBn);
		binding.aboutHeading.setVisibility(_hasAbout ? View.VISIBLE : View.GONE);
		setOrHide(binding.doctorAbout, _doctor.about);
		setOrHide(binding.doctorAboutBn, _doctor.aboutBn);
	}

	private void bindChambers(Doctor _doctor) {
		binding.chamberContainer.removeAllViews();
		binding.chambersHeading.setVisibility(_doctor.chambers.isEmpty() ? View.GONE : View.VISIBLE);

		LayoutInflater _inflater = LayoutInflater.from(this);
		for (Chamber _chamber : _doctor.chambers) {
			View _card = _inflater.inflate(R.layout.item_chamber, binding.chamberContainer, false);
			setOrHide((TextView) _card.findViewById(R.id.chamber_name), _chamber.name);
			setOrHide((TextView) _card.findViewById(R.id.chamber_address), _chamber.address);
			setOrHide((TextView) _card.findViewById(R.id.chamber_hours), hoursLine(_chamber));

			MaterialButton _call = _card.findViewById(R.id.chamber_call);
			if (_chamber.phones.isEmpty()) {
				_call.setVisibility(View.GONE);
			} else {
				final String _number = _chamber.phones.get(0);
				_call.setVisibility(View.VISIBLE);
				_call.setText(getString(R.string.chamber_call) + " " + _number);
				_call.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View _view) {
						dial(_number);
					}
				});
			}
			bindDemo(_card, _chamber);
			binding.chamberContainer.addView(_card);
		}
	}

	/**
	 * The demo's way in, or nothing at all.
	 *
	 * Two conditions, and both have to hold. Demo mode is off unless someone turned it
	 * on in Settings, and a chamber whose published hours could not be read has no
	 * honest day to offer -- 787 of the 9,350 chambers are in that state, most of them
	 * because the source says to call and ask.
	 */
	private void bindDemo(View _card, Chamber _chamber) {
		View _row = _card.findViewById(R.id.chamber_demo_row);
		if (!SettingsStore.get(this).demoEnabled()
				|| VisitingHours.parse(_chamber.visitingHours) == null) {
			_row.setVisibility(View.GONE);
			return;
		}

		final int _seq = _chamber.seq;
		_row.setVisibility(View.VISIBLE);
		_card.findViewById(R.id.chamber_book).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				startActivity(BookingActivity.intentFor(DoctorDetailActivity.this, doctorId, _seq));
			}
		});
		_card.findViewById(R.id.chamber_console).setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				startActivity(ChamberConsoleActivity.intentFor(
					DoctorDetailActivity.this, doctorId, _seq));
			}
		});
	}

	/**
	 * The verbatim hours, with the closed days appended only when reading the hours
	 * alone would not already tell you them.
	 *
	 * The structured closed_days list is parsed out of the hours string itself, so most
	 * of the time repeating it is pure noise: "3pm to 9pm (except Tuesday & Friday)"
	 * does not need "Closed: Tuesday, Friday" underneath it. It is worth appending only
	 * when the source abbreviated - "(Closed: Fri)" against a parsed "Friday" - or
	 * phrased it somewhere the reader would miss.
	 */
	private String hoursLine(Chamber _chamber) {
		if (!notEmpty(_chamber.visitingHours)) {
			return null;
		}
		if (_chamber.closedDays.isEmpty()) {
			return _chamber.visitingHours;
		}

		String _hours = _chamber.visitingHours.toLowerCase(Locale.ROOT);
		boolean _allNamed = true;
		StringBuilder _days = new StringBuilder();
		for (String _day : _chamber.closedDays) {
			if (!_hours.contains(_day.toLowerCase(Locale.ROOT))) {
				_allNamed = false;
			}
			if (_days.length() > 0) {
				_days.append(", ");
			}
			_days.append(_day);
		}
		if (_allNamed) {
			return _chamber.visitingHours;
		}
		return _chamber.visitingHours + "\n" + getString(R.string.chamber_closed, _days.toString());
	}

	/**
	 * ACTION_DIAL, not ACTION_CALL: it opens the dialer with the number filled in and
	 * lets the user press call, which needs no permission and never places a call the
	 * user did not mean to make.
	 *
	 * The number is used exactly as published. Some hospitals list a four or five digit
	 * hotline rather than a mobile number, and 473 chambers carry one - prefixing those
	 * with a country code would dial a number that does not exist.
	 */
	private void dial(String _number) {
		Intent _intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + _number));
		try {
			startActivity(_intent);
		} catch (ActivityNotFoundException _e) {
			Toast.makeText(this, R.string.no_dialer, Toast.LENGTH_SHORT).show();
		}
	}

	private void applyWindowInsets() {
		final int baseTop = binding.doctorRoot.getPaddingTop();
		final int baseBottom = binding.doctorRoot.getPaddingBottom();
		ViewCompat.setOnApplyWindowInsetsListener(binding.doctorRoot, new OnApplyWindowInsetsListener() {
			@Override
			public WindowInsetsCompat onApplyWindowInsets(View _view, WindowInsetsCompat _insets) {
				Insets _bars = _insets.getInsets(WindowInsetsCompat.Type.systemBars()
					| WindowInsetsCompat.Type.displayCutout());
				_view.setPadding(_bars.left, baseTop + _bars.top, _bars.right, baseBottom + _bars.bottom);
				return _insets;
			}
		});
	}

	private static boolean notEmpty(String _text) {
		return _text != null && !_text.isEmpty();
	}

	/** A fact the source does not state gets no empty row of its own. */
	private static void setOrHide(TextView _view, String _text) {
		if (notEmpty(_text)) {
			_view.setVisibility(View.VISIBLE);
			_view.setText(_text);
		} else {
			_view.setVisibility(View.GONE);
		}
	}

	@Override
	protected void onDestroy() {
		worker.shutdown();
		super.onDestroy();
	}
}
