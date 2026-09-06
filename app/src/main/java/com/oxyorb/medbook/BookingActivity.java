package com.oxyorb.medbook;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.GridLayoutManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.oxyorb.medbook.data.DoctorRepository;
import com.oxyorb.medbook.data.model.Chamber;
import com.oxyorb.medbook.data.model.Doctor;
import com.oxyorb.medbook.databinding.BookingBinding;
import com.oxyorb.medbook.demo.ChamberKey;
import com.oxyorb.medbook.demo.DemoFormat;
import com.oxyorb.medbook.demo.DemoSchedule;
import com.oxyorb.medbook.demo.DemoStore;
import com.oxyorb.medbook.demo.EpochDay;
import com.oxyorb.medbook.demo.SlotAdapter;
import com.oxyorb.medbook.demo.ThemeColor;
import com.oxyorb.medbook.demo.VisitingHours;
import com.oxyorb.medbook.settings.SettingsStore;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Picking a time at one chamber, in a booking system that does not exist.
 *
 * Everything on this screen is invented except the doctor, the chamber and its published
 * hours, which are the source's. The slots are laid over those hours by DemoSchedule and
 * are the same every time this is opened; the only thing written down is what the person
 * holding the phone taps.
 *
 * The screen carries the loudest warning in the app, in the error colour role that
 * nothing else uses, because the doctor named at the top is a real doctor with a real
 * published number and the failure mode of this feature is somebody believing it.
 */
public class BookingActivity extends AppCompatActivity implements SlotAdapter.Listener {

	private static final String EXTRA_DOCTOR = "doctor_id";
	private static final String EXTRA_SEQ = "chamber_seq";

	/** A week. No pitch has ever needed to look further, and a calendar is a whole screen. */
	private static final int DAYS_SHOWN = 7;

	private static final int COLUMNS = 3;

	private final ExecutorService worker = Executors.newSingleThreadExecutor();

	private BookingBinding binding;
	private SlotAdapter adapter;
	private DemoStore demo;

	private long doctorId;
	private int chamberSeq;
	private String chamberKey;
	private Doctor doctor;
	private VisitingHours hours;

	private int today;
	private int selectedDay;

	public static Intent intentFor(Context _context, long _doctorId, int _chamberSeq) {
		Intent _intent = new Intent(_context, BookingActivity.class);
		_intent.putExtra(EXTRA_DOCTOR, _doctorId);
		_intent.putExtra(EXTRA_SEQ, _chamberSeq);
		return _intent;
	}

	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		binding = BookingBinding.inflate(getLayoutInflater());
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

		adapter = new SlotAdapter(this);
		binding.slotGrid.setLayoutManager(new GridLayoutManager(this, COLUMNS));
		binding.slotGrid.setAdapter(adapter);

		buildDayStrip();
		load();
	}

	/**
	 * Demo mode can be switched off while this screen sits on the back stack, and
	 * somebody pressing back into it afterwards would find a booking screen the app has
	 * otherwise stopped admitting to. Leave rather than let that happen.
	 */
	@Override
	protected void onResume() {
		super.onResume();
		if (!SettingsStore.get(this).demoEnabled()) {
			finish();
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
				DoctorRepository _repository = DoctorRepository.open(BookingActivity.this);
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
		Chamber _chamber = chamber();
		if (_chamber == null) {
			finish();
			return;
		}

		binding.bookingDoctor.setText(_doctor.displayName);
		binding.bookingChamber.setText(_chamber.name);
		hours = VisitingHours.parse(_chamber.visitingHours);
		showDay();
	}

	private Chamber chamber() {
		for (Chamber _chamber : doctor.chambers) {
			if (_chamber.seq == chamberSeq) {
				return _chamber;
			}
		}
		return null;
	}

	/** Seven plain views rather than a RecyclerView, as the chamber cards are. */
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

		adapter.show(_slots, selectedDay);

		if (_slots.isEmpty()) {
			binding.bookingSummary.setText(R.string.booking_closed);
			return;
		}
		int _free = _slots.size() - DemoSchedule.occupied(_slots);
		binding.bookingSummary.setText(
			getResources().getQuantityString(R.plurals.booking_free_count, _free, _free));
	}

	@Override
	public void onSlotSelected(DemoSchedule.Slot _slot) {
		if (_slot.state == DemoSchedule.STATE_TAKEN) {
			return;
		}
		if (_slot.state == DemoSchedule.STATE_YOURS) {
			confirmCancel(_slot);
		} else {
			confirmBooking(_slot);
		}
	}

	/**
	 * The words here are the whole safety argument, so they say what did not happen
	 * rather than what did: nothing is booked, and nobody at the chamber is told.
	 */
	private void confirmBooking(final DemoSchedule.Slot _slot) {
		new MaterialAlertDialogBuilder(this)
			.setTitle(R.string.booking_confirm_title)
			.setMessage(getString(R.string.booking_confirm_message,
				DemoFormat.time(this, selectedDay, _slot.minuteOfDay),
				DemoFormat.day(this, selectedDay)))
			.setNegativeButton(R.string.cancel, null)
			.setPositiveButton(R.string.booking_confirm_action, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface _dialog, int _which) {
					demo.book(doctorId, chamberSeq, selectedDay, _slot.minuteOfDay);
					showDay();
					Toast.makeText(BookingActivity.this,
						getString(R.string.booking_done, _slot.serial), Toast.LENGTH_LONG).show();
				}
			})
			.show();
	}

	private void confirmCancel(final DemoSchedule.Slot _slot) {
		new MaterialAlertDialogBuilder(this)
			.setTitle(R.string.booking_cancel_title)
			.setNegativeButton(R.string.cancel, null)
			.setPositiveButton(R.string.booking_cancel_action, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface _dialog, int _which) {
					demo.cancel(doctorId, chamberSeq, selectedDay, _slot.minuteOfDay);
					showDay();
					Toast.makeText(BookingActivity.this, R.string.booking_cancelled,
						Toast.LENGTH_SHORT).show();
				}
			})
			.show();
	}

	/** As every other screen: from API 35 the system draws edge to edge with no opt-out. */
	private void applyWindowInsets() {
		final int baseLeft = binding.bookingRoot.getPaddingLeft();
		final int baseTop = binding.bookingRoot.getPaddingTop();
		final int baseRight = binding.bookingRoot.getPaddingRight();
		final int baseBottom = binding.bookingRoot.getPaddingBottom();
		ViewCompat.setOnApplyWindowInsetsListener(binding.bookingRoot, new OnApplyWindowInsetsListener() {
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
