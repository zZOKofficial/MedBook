package com.oxyorb.medbook.settings;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.oxyorb.medbook.R;

/**
 * Which label names a stored choice, across the two eras of Android the app supports.
 *
 * The case worth having a test for is the one no reviewer can see by tapping: below
 * API 29 "Follow the device" has nothing to follow, and MedBook is really following the
 * battery saver. The emulator images that far back are the ones least likely to be
 * opened, and the branch is invisible on anything newer, so it is checked here instead.
 *
 * These run off a device for the same reason the department search and visiting-hours
 * tests do: the function is pure, over an int and a string.
 */
public class SettingsLabelsTest {

	/** Android 10, where a system-wide dark setting first exists. */
	private static final int Q = 29;
	private static final int PIE = 28;
	private static final int LOLLIPOP = 21;

	@Test
	public void lightAndDarkReadTheSameOnEveryVersion() {
		for (int _sdk : new int[]{LOLLIPOP, PIE, Q, 36}) {
			assertEquals(R.string.theme_light,
				SettingsLabels.themeLabel(SettingsStore.THEME_LIGHT, _sdk));
			assertEquals(R.string.theme_dark,
				SettingsLabels.themeLabel(SettingsStore.THEME_DARK, _sdk));
		}
	}

	@Test
	public void followTheDeviceFromAndroidTen() {
		assertEquals(R.string.theme_system,
			SettingsLabels.themeLabel(SettingsStore.THEME_SYSTEM, Q));
		assertEquals(R.string.theme_system,
			SettingsLabels.themeLabel(SettingsStore.THEME_SYSTEM, 36));
	}

	@Test
	public void followTheBatterySaverBelowAndroidTen() {
		assertEquals(R.string.theme_system_battery,
			SettingsLabels.themeLabel(SettingsStore.THEME_SYSTEM, PIE));
		assertEquals(R.string.theme_system_battery,
			SettingsLabels.themeLabel(SettingsStore.THEME_SYSTEM, LOLLIPOP));
	}

	/**
	 * A value the store should never hold, but the row still has to say something. It
	 * falls to the system label rather than to nothing, matching nightMode().
	 */
	@Test
	public void anUnknownThemeFallsBackToTheSystem() {
		assertEquals(R.string.theme_system, SettingsLabels.themeLabel(7, Q));
		assertEquals(R.string.theme_system_battery, SettingsLabels.themeLabel(7, PIE));
	}

	@Test
	public void languageLabelsFollowTheStoredTag() {
		assertEquals(R.string.language_english, SettingsLabels.languageLabel("en"));
		assertEquals(R.string.language_bengali, SettingsLabels.languageLabel("bn"));
		assertEquals(R.string.language_system, SettingsLabels.languageLabel(""));
	}

	/**
	 * localeTag() promises never to return null, but the label is also read straight
	 * from a framework locale list on API 33+, so it does not rely on that promise.
	 */
	@Test
	public void anUnknownOrMissingTagFollowsTheDevice() {
		assertEquals(R.string.language_system, SettingsLabels.languageLabel(null));
		assertEquals(R.string.language_system, SettingsLabels.languageLabel("fr"));
	}
}
