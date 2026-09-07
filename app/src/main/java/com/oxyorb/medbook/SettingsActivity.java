package com.oxyorb.medbook;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.oxyorb.medbook.databinding.SettingsBinding;
import com.oxyorb.medbook.demo.DemoStore;
import com.oxyorb.medbook.settings.LocaleController;
import com.oxyorb.medbook.settings.SettingsLabels;
import com.oxyorb.medbook.settings.SettingsStore;

/**
 * MedBook's settings, hand-rolled rather than androidx.preference.
 *
 * The preference library brings its own list, its own row styling and its own
 * ActionBar assumptions, all of which fight a NoActionBar theme whose chrome-free
 * look the rest of the app treats as the point. Rows in a ScrollView need none of
 * that, and skip a dependency the app does not otherwise carry.
 *
 * Theme and language used to sit here as open radio groups -- nine rows, every choice
 * shown at once, at the same visual level as the headings above them. They are now one
 * row each, naming their own current value and opening a single-choice dialog. The
 * screen stops growing by three rows per setting, which is the only reason the change
 * was worth making.
 */
public class SettingsActivity extends AppCompatActivity {

	/** Dialog order for the language picker, and so the order of its labels. */
	private static final String[] LANGUAGE_TAGS = {"", "en", "bn"};

	private SettingsBinding binding;
	private SettingsStore settings;
	/** Guard the listener while the stored value is being reflected into the switch. */
	private boolean bindingDemo;

	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		binding = SettingsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		applyWindowInsets();
		settings = SettingsStore.get(this);

		binding.backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				finish();
			}
		});

		bindTheme();
		bindLanguage();
		bindDemo();
		bindAbout();
	}

	/** Two ways out of settings, both to screens that only ever read. */
	private void bindAbout() {
		binding.aboutRow.rowTitle.setText(R.string.settings_about_app);
		binding.aboutRow.getRoot().setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				startActivity(new Intent(SettingsActivity.this, AboutActivity.class));
			}
		});
		binding.privacyRow.rowTitle.setText(R.string.settings_privacy);
		binding.privacyRow.getRoot().setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				startActivity(new Intent(SettingsActivity.this, PrivacyActivity.class));
			}
		});
	}

	/**
	 * The theme row. There is no listener to guard here any more: the row only ever
	 * displays what is stored, and the dialog is the only thing that writes.
	 */
	private void bindTheme() {
		binding.themeRow.rowTitle.setText(R.string.settings_theme);
		binding.themeRow.rowSummary.setText(
			SettingsLabels.themeLabel(settings.theme(), Build.VERSION.SDK_INT));
		binding.themeRow.rowSummary.setVisibility(View.VISIBLE);
		binding.themeRow.getRoot().setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				showThemeDialog();
			}
		});
	}

	/**
	 * Three labels, the stored one pre-checked, applied on tap.
	 *
	 * Dismiss first, then store, then apply. setSingleChoiceItems does not dismiss
	 * itself -- unlike setItems -- and setDefaultNightMode recreates every live
	 * activity including this one. A dialog still showing when its host is destroyed
	 * is a leaked window, so the order is not cosmetic. Storing before applying is the
	 * older rule and still holds: the recreated activity reads the preference back.
	 */
	private void showThemeDialog() {
		final int _current = settings.theme();
		CharSequence[] _labels = new CharSequence[3];
		for (int _i = 0; _i < _labels.length; _i++) {
			_labels[_i] = getString(SettingsLabels.themeLabel(_i, Build.VERSION.SDK_INT));
		}
		new MaterialAlertDialogBuilder(this)
			.setTitle(R.string.settings_theme)
			.setNegativeButton(R.string.cancel, null)
			.setSingleChoiceItems(_labels, _current, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface _dialog, int _which) {
					_dialog.dismiss();
					if (_which == _current) {
						return;
					}
					settings.setTheme(_which);
					AppCompatDelegate.setDefaultNightMode(settings.nightMode());
				}
			})
			.show();
	}

	private void bindLanguage() {
		binding.languageRow.rowTitle.setText(R.string.settings_language);
		binding.languageRow.rowSummary.setText(
			SettingsLabels.languageLabel(settings.localeTag()));
		binding.languageRow.rowSummary.setVisibility(View.VISIBLE);
		binding.languageRow.getRoot().setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				showLanguageDialog();
			}
		});
	}

	/** As {@link #showThemeDialog()}: dismiss, store, apply, in that order. */
	private void showLanguageDialog() {
		final int _current = indexOfLanguage(settings.localeTag());
		CharSequence[] _labels = new CharSequence[LANGUAGE_TAGS.length];
		for (int _i = 0; _i < _labels.length; _i++) {
			_labels[_i] = getString(SettingsLabels.languageLabel(LANGUAGE_TAGS[_i]));
		}
		new MaterialAlertDialogBuilder(this)
			.setTitle(R.string.settings_language)
			.setNegativeButton(R.string.cancel, null)
			.setSingleChoiceItems(_labels, _current, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface _dialog, int _which) {
					_dialog.dismiss();
					if (_which == _current) {
						return;
					}
					LocaleController.set(settings, LANGUAGE_TAGS[_which]);
				}
			})
			.show();
	}

	private static int indexOfLanguage(String _tag) {
		for (int _i = 0; _i < LANGUAGE_TAGS.length; _i++) {
			if (LANGUAGE_TAGS[_i].equals(_tag)) {
				return _i;
			}
		}
		return 0;
	}

	/**
	 * The demo switch, and the one action that undoes what it produces.
	 *
	 * Turning it on is deliberately a plain visible switch rather than something
	 * hidden, which is what makes the labelling on the demo screens themselves load
	 * bearing: anyone can find this, so nothing behind it may look like a real
	 * booking. Turning it off leaves the bookings alone -- switching back on should
	 * find the demo where it was left, mid-pitch, rather than wiped.
	 */
	private void bindDemo() {
		bindingDemo = true;
		binding.demoSwitch.setChecked(settings.demoEnabled());
		bindingDemo = false;

		binding.demoResetRow.rowTitle.setText(R.string.settings_demo_reset);
		// An action, not a way into another screen, so it carries no chevron.
		binding.demoResetRow.rowChevron.setVisibility(View.GONE);
		binding.demoResetRow.getRoot().setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				confirmReset();
			}
		});
		setResetEnabled(settings.demoEnabled());

		binding.demoSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton _button, boolean _checked) {
				if (bindingDemo) {
					return;
				}
				settings.setDemoEnabled(_checked);
				setResetEnabled(_checked);
			}
		});
	}

	/**
	 * A disabled row rather than a hidden one: with nothing to reset the action is
	 * still worth showing, so that turning the demo on does not appear to add a
	 * control out of nowhere. A disabled View does not dispatch clicks, so the alpha
	 * is the whole of what has to be said.
	 */
	private void setResetEnabled(boolean _enabled) {
		binding.demoResetRow.getRoot().setEnabled(_enabled);
		binding.demoResetRow.getRoot().setAlpha(_enabled ? 1f : 0.38f);
	}

	/**
	 * Clears the bookings and nothing else. There is nothing else to clear: the rest
	 * of the demo is computed from the chamber and the date and was never stored.
	 */
	private void confirmReset() {
		final DemoStore _demo = DemoStore.get(this);
		if (_demo.isEmpty()) {
			Toast.makeText(this, R.string.settings_demo_reset_empty, Toast.LENGTH_SHORT).show();
			return;
		}
		new MaterialAlertDialogBuilder(this)
			.setTitle(R.string.settings_demo_reset)
			.setMessage(R.string.settings_demo_reset_message)
			.setNegativeButton(R.string.cancel, null)
			.setPositiveButton(R.string.settings_demo_reset, new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface _dialog, int _which) {
					_demo.reset();
					Toast.makeText(SettingsActivity.this, R.string.settings_demo_reset_done,
						Toast.LENGTH_SHORT).show();
				}
			})
			.show();
	}

	/**
	 * As every other screen: from API 35 the system draws edge to edge with no
	 * opt-out, so each root insets itself or the content sits under the status bar.
	 */
	private void applyWindowInsets() {
		final int baseLeft = binding.settingsRoot.getPaddingLeft();
		final int baseTop = binding.settingsRoot.getPaddingTop();
		final int baseRight = binding.settingsRoot.getPaddingRight();
		final int baseBottom = binding.settingsRoot.getPaddingBottom();
		ViewCompat.setOnApplyWindowInsetsListener(binding.settingsRoot, new OnApplyWindowInsetsListener() {
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
