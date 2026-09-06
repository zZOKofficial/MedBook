package com.oxyorb.medbook;

import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.RadioGroup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.oxyorb.medbook.databinding.SettingsBinding;
import com.oxyorb.medbook.settings.LocaleController;
import com.oxyorb.medbook.settings.SettingsStore;

/**
 * MedBook's settings, hand-rolled rather than androidx.preference.
 *
 * The preference library brings its own list, its own row styling and its own
 * ActionBar assumptions, all of which fight a NoActionBar theme whose chrome-free
 * look the rest of the app treats as the point. Three radio buttons in a
 * ScrollView need none of that, and skip a dependency the app does not otherwise
 * carry.
 */
public class SettingsActivity extends AppCompatActivity {

	private SettingsBinding binding;
	private SettingsStore settings;
	/** Guard the listeners while a stored value is being reflected into the UI. */
	private boolean bindingTheme;
	private boolean bindingLanguage;

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
	}

	private void bindTheme() {
		// "Follow the device" is honest from API 29 on. Below it there is no system
		// dark setting, so the choice falls back to the battery saver and the note
		// says so rather than letting the label quietly overpromise.
		binding.themeSystemNote.setVisibility(
			Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q ? View.GONE : View.VISIBLE);

		bindingTheme = true;
		switch (settings.theme()) {
			case SettingsStore.THEME_LIGHT:
				binding.themeGroup.check(R.id.theme_light);
				break;
			case SettingsStore.THEME_DARK:
				binding.themeGroup.check(R.id.theme_dark);
				break;
			default:
				binding.themeGroup.check(R.id.theme_system);
				break;
		}
		bindingTheme = false;

		binding.themeGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup _group, int _checkedId) {
				if (bindingTheme) {
					return;
				}
				int _theme = SettingsStore.THEME_SYSTEM;
				if (_checkedId == R.id.theme_light) {
					_theme = SettingsStore.THEME_LIGHT;
				} else if (_checkedId == R.id.theme_dark) {
					_theme = SettingsStore.THEME_DARK;
				}
				settings.setTheme(_theme);
				// Store first, then apply: this recreates every live activity,
				// including this one, and the choice must already be durable when
				// the new instance reads it back.
				AppCompatDelegate.setDefaultNightMode(settings.nightMode());
			}
		});
	}

	private void bindLanguage() {
		String _tag = settings.localeTag();
		bindingLanguage = true;
		if ("bn".equals(_tag)) {
			binding.languageGroup.check(R.id.language_bengali);
		} else if ("en".equals(_tag)) {
			binding.languageGroup.check(R.id.language_english);
		} else {
			binding.languageGroup.check(R.id.language_system);
		}
		bindingLanguage = false;

		binding.languageGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup _group, int _checkedId) {
				if (bindingLanguage) {
					return;
				}
				String _choice = "";
				if (_checkedId == R.id.language_english) {
					_choice = "en";
				} else if (_checkedId == R.id.language_bengali) {
					_choice = "bn";
				}
				LocaleController.set(settings, _choice);
			}
		});
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
