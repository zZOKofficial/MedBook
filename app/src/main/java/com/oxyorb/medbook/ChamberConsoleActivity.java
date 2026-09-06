package com.oxyorb.medbook;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.oxyorb.medbook.data.DoctorRepository;
import com.oxyorb.medbook.data.model.Chamber;
import com.oxyorb.medbook.data.model.Doctor;
import com.oxyorb.medbook.databinding.ChamberConsoleBinding;
import com.oxyorb.medbook.demo.ChamberKey;
import com.oxyorb.medbook.demo.ConsoleAdapter;
import com.oxyorb.medbook.demo.DemoFormat;
import com.oxyorb.medbook.demo.DemoPatients;
import com.oxyorb.medbook.demo.DemoSchedule;
import com.oxyorb.medbook.demo.DemoStore;
import com.oxyorb.medbook.demo.EpochDay;
import com.oxyorb.medbook.demo.ThemeColor;
import com.oxyorb.medbook.demo.VisitingHours;
import com.oxyorb.medbook.settings.SettingsStore;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The same day, from the chamber's side of the desk.
 *
 * This is the half a hospital is actually being shown. The booking screen proves a
 * patient can take a time; this proves somebody at the chamber would know about it, in
 * the order they would call them, with their own booking sitting in the list among the
 * rest. Utilisation is the number that gets asked about first, so it is the only thing
 * on the screen in the secondary colour.
 *
 * It reads the same generator the patient screen does rather than a stored copy, which
 * is why the two agree: there is no copy to disagree with.
 */
public class ChamberConsoleActivity extends AppCompatActivity {

	private static final String EXTRA_DOCTOR = "doctor_id";
	private static final String EXTRA_SEQ = "chamber_seq";

	private static final int DAYS_SHOWN = 7;

	private final ExecutorService worker = Executors.newSingleThreadExecutor();

	private ChamberConsoleBinding binding;
	private ConsoleAdapter adapter;
	private DemoStore demo;

	private long doctorId;
	private int chamberSeq;
	private String chamberKey;
	private Doctor doctor;
	private VisitingHours hours;

	private int today;
	private int selectedDay;

	public static Intent intentFor(Context _context, long _doctorId, int _chamberSeq) {
		Intent _intent = new Intent(_context, ChamberConsoleActivity.class);
		_intent.putExtra(EXTRA_DOCTOR, _doctorId);
		_intent.putExtra(EXTRA_SEQ, _chamberSeq);
		return _intent;
	}

	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		binding = ChamberConsoleBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		applyWindowInsets();

		doctorId = getIntent().getLongExtra(EXTRA_DOCTOR, -1L);
		chamberSeq = getIntent().getIntExtra(EXTRA_SEQ, -1);
		chamberKey = ChamberKey.of(doctorId, chamberSeq);
		demo = DemoStore.get(this);

		today = EpochDay.today();
		selectedDay = today;

		binding.backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				finish();
			}
		});

		adapter = new ConsoleAdapter();
		binding.consoleList.setLayoutManager(new LinearLayoutManager(this));
		binding.consoleList.setAdapter(adapter);

		buildDayStrip();
		load();
	}

	/** As the booking screen: do not sit on the back stack after demo mode goes off. */
	@Override
	protected void onResume() {
		super.onResume();
		if (!SettingsStore.get(this).demoEnabled()) {
			finish();
			return;
		}
		if (doctor != null) {
			showDay();
		}
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
		worker.shutdown();
	}

	private void load() {
		worker.execute(new Runnable() {
			@Override
			public void run() {
				DoctorRepository _repository = DoctorRepository.open(ChamberConsoleActivity.this);
				final Doctor _doctor = _repository == null ? null : _repository.doctor(doctorId);
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
		doctor = _doctor;
		Chamber _chamber = null;
		for (Chamber _candidate : _doctor.chambers) {
			if (_candidate.seq == chamberSeq) {
				_chamber = _candidate;
			}
		}
		if (_chamber == null) {
			finish();
			return;
		}

		binding.consoleChamber.setText(_chamber.name);
		binding.consoleDoctor.setText(_doctor.displayName);
		hours = VisitingHours.parse(_chamber.visitingHours);
		showDay();
	}

	private void buildDayStrip() {
		LayoutInflater _inflater = LayoutInflater.from(this);
		binding.dayStrip.removeAllViews();
		for (int _offset = 0; _offset < DAYS_SHOWN; _offset++) {
			final int _day = today + _offset;
			TextView _chip = (TextView) _inflater.inflate(R.layout.item_day, binding.dayStrip, false);
			_chip.setText(_offset == 0
				? getString(R.string.booking_today)
				: DemoFormat.shortDay(this, _day));
			_chip.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					selectedDay = _day;
					paintDayStrip();
					showDay();
				}
			});
			binding.dayStrip.addView(_chip);
		}
		paintDayStrip();
	}

	private void paintDayStrip() {
		for (int _index = 0; _index < binding.dayStrip.getChildCount(); _index++) {
			TextView _chip = (TextView) binding.dayStrip.getChildAt(_index);
			boolean _selected = today + _index == selectedDay;
			_chip.setBackgroundColor(ThemeColor.of(this, _selected
				? com.google.android.material.R.attr.colorSecondaryContainer
				: com.google.android.material.R.attr.colorSurfaceContainerLow));
			_chip.setTextColor(ThemeColor.of(this, _selected
				? com.google.android.material.R.attr.colorOnSecondaryContainer
				: com.google.android.material.R.attr.colorOnSurfaceVariant));
		}
	}

	private void showDay() {
		List<DemoSchedule.Slot> _slots = DemoSchedule.slotsFor(chamberKey, hours, selectedDay,
			selectedDay - today, doctor.verified,
			doctor.reviewCount == null ? 0 : doctor.reviewCount,
			demo.minutesFor(doctorId, chamberSeq, selectedDay));

		boolean _closed = _slots.isEmpty();
		binding.consoleEmpty.setVisibility(_closed ? View.VISIBLE : View.GONE);
		binding.consoleEmpty.setText(R.string.booking_closed);
		binding.consoleList.setVisibility(_closed ? View.GONE : View.VISIBLE);

		if (_closed) {
			binding.consoleUtilisation.setText(R.string.console_empty);
			adapter.show(_slots, DemoPatients.roster(this, chamberKey, selectedDay),
				selectedDay, 0);
			return;
		}

		int _taken = DemoSchedule.occupied(_slots);
		binding.consoleUtilisation.setText(getString(R.string.console_utilisation,
			_taken, _slots.size(), _taken * 100 / _slots.size()));
		adapter.show(_slots, DemoPatients.roster(this, chamberKey, selectedDay),
			selectedDay, noShowSerial(_slots));
	}

	/**
	 * The one who did not turn up, if their time has already gone.
	 *
	 * Only ever today, and only ever a slot earlier than the clock. Marking a no-show in
	 * tomorrow's book is the kind of detail a chamber manager catches in the first thirty
	 * seconds, and catching it would cost the rest of the demo its credit.
	 */
	private int noShowSerial(List<DemoSchedule.Slot> _slots) {
		if (selectedDay != today) {
			return 0;
		}
		int _candidate = DemoPatients.noShowSerial(chamberKey, selectedDay, _slots.size());
		int _now = EpochDay.minuteOfDayNow();
		for (DemoSchedule.Slot _slot : _slots) {
			if (_slot.serial == _candidate) {
				boolean _past = _slot.minuteOfDay < _now;
				return _past && _slot.state == DemoSchedule.STATE_TAKEN ? _candidate : 0;
			}
		}
		return 0;
	}

	/** As every other screen: from API 35 the system draws edge to edge with no opt-out. */
	private void applyWindowInsets() {
		final int baseLeft = binding.consoleRoot.getPaddingLeft();
		final int baseTop = binding.consoleRoot.getPaddingTop();
		final int baseRight = binding.consoleRoot.getPaddingRight();
		final int baseBottom = binding.consoleRoot.getPaddingBottom();
		ViewCompat.setOnApplyWindowInsetsListener(binding.consoleRoot, new OnApplyWindowInsetsListener() {
			@NonNull
			@Override
			public WindowInsetsCompat onApplyWindowInsets(@NonNull View _view, @NonNull WindowInsetsCompat _insets) {
				Insets _bars = _insets.getInsets(
					WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
				_view.setPadding(baseLeft + _bars.left, baseTop + _bars.top,
					baseRight + _bars.right, baseBottom + _bars.bottom);
				return _insets;
			}
		});
	}
}
