package com.oxyorb.medbook.settings;

import android.os.Build;

import androidx.annotation.StringRes;

import com.oxyorb.medbook.R;

/**
 * Which label names a stored choice on the settings screen.
 *
 * Pulled out of the activity because it is the one part of the screen with real
 * branching -- three themes crossed with two eras of Android -- and because that makes
 * it a pure function of (choice, API level) that a plain JUnit test can reach. The rest
 * of the screen is view wiring and dialogs, which the project's test setup cannot touch
 * and does not try to.
 */
public final class SettingsLabels {

	private SettingsLabels() {
	}

	/**
	 * The label for a theme choice, both in the row's value line and in the dialog.
	 *
	 * Below API 29 "Follow the device" is a promise Android cannot keep: there is no
	 * system dark setting, and MODE_NIGHT_AUTO_BATTERY is what the choice actually
	 * resolves to. See {@link SettingsStore#nightMode()}. So the option is renamed
	 * rather than footnoted -- the old caption under the radio group said the same
	 * thing in a sentence nobody had to read, and a collapsed row has no room for it.
	 */
	@StringRes
	public static int themeLabel(int _theme, int _sdkInt) {
		switch (_theme) {
			case SettingsStore.THEME_LIGHT:
				return R.string.theme_light;
			case SettingsStore.THEME_DARK:
				return R.string.theme_dark;
			default:
				return _sdkInt >= Build.VERSION_CODES.Q
					? R.string.theme_system
					: R.string.theme_system_battery;
		}
	}

	/** The label for a stored locale tag: "en", "bn", or "" for the device's own. */
	@StringRes
	public static int languageLabel(String _tag) {
		if ("en".equals(_tag)) {
			return R.string.language_english;
		}
		if ("bn".equals(_tag)) {
			return R.string.language_bengali;
		}
		return R.string.language_system;
	}
}
