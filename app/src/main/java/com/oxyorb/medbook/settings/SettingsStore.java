package com.oxyorb.medbook.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;

/**
 * Everything MedBook remembers about a person's choices, in two files.
 *
 * The split is not organisational, it is about what a backup should carry.
 * settings.xml holds what the user chose -- their theme and their language --
 * and restoring that onto a new phone is exactly what they would expect.
 * local.xml holds facts about this install: that the privacy notice has been
 * shown here, and what the last update check found.
 *
 * Those must not travel. A cloud restore can land on a different phone and a
 * different person, and carrying "already saw the privacy notice" across would
 * skip a first-run disclosure for someone who has never seen it. Carrying "you
 * already dismissed 0.3.0" onto a device running 0.1.0 would simply be false.
 * See res/xml/backup_rules.xml, which is where that is actually enforced.
 */
public final class SettingsStore {

	/** Follow the system, as far as the system can be followed. See {@link #nightMode()}. */
	public static final int THEME_SYSTEM = 0;
	public static final int THEME_LIGHT = 1;
	public static final int THEME_DARK = 2;

	/**
	 * Bumped whenever the privacy notice's wording changes materially. Everyone
	 * sees the new text once, which is the entire point of showing it at all -- a
	 * boolean would silently grandfather people onto a notice they never read.
	 */
	public static final int PRIVACY_NOTICE_VERSION = 1;

	/** Backed up: the user's own choices. */
	private static final String PREFS_SETTINGS = "settings";
	/** Not backed up to the cloud: facts about this particular install. */
	private static final String PREFS_LOCAL = "local";

	private static final String KEY_THEME = "theme";
	private static final String KEY_LOCALE_TAG = "locale_tag";
	private static final String KEY_PRIVACY_VERSION = "privacy_notice_version";
	private static final String KEY_UPDATE_DISMISSED = "update_dismissed_tag";
	private static final String KEY_UPDATE_CHECKED_AT = "update_last_checked_at";

	private static SettingsStore instance;

	private final SharedPreferences settings;
	private final SharedPreferences local;

	private SettingsStore(Context _context) {
		Context _application = _context.getApplicationContext();
		settings = _application.getSharedPreferences(PREFS_SETTINGS, Context.MODE_PRIVATE);
		local = _application.getSharedPreferences(PREFS_LOCAL, Context.MODE_PRIVATE);
	}

	public static synchronized SettingsStore get(Context _context) {
		if (instance == null) {
			instance = new SettingsStore(_context);
		}
		return instance;
	}

	// -- theme --------------------------------------------------------------------

	/** One of THEME_SYSTEM, THEME_LIGHT, THEME_DARK. */
	public int theme() {
		return settings.getInt(KEY_THEME, THEME_SYSTEM);
	}

	public void setTheme(int _theme) {
		settings.edit().putInt(KEY_THEME, _theme).apply();
	}

	/**
	 * The stored choice as an AppCompatDelegate mode.
	 *
	 * MODE_NIGHT_FOLLOW_SYSTEM resolves through UiModeManager below API 29, and on
	 * a phone that far back there is no system dark theme to follow -- so "System"
	 * would quietly mean "Light" for every user on Android 5 through 9, which is a
	 * good part of why minSdk is still 21. AUTO_BATTERY is a real behaviour there
	 * (dark while the battery saver is on) and is closer to the promise than
	 * nothing at all. From API 29 the system setting exists and is followed.
	 */
	public int nightMode() {
		switch (theme()) {
			case THEME_LIGHT:
				return AppCompatDelegate.MODE_NIGHT_NO;
			case THEME_DARK:
				return AppCompatDelegate.MODE_NIGHT_YES;
			default:
				return Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
					? AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
					: AppCompatDelegate.MODE_NIGHT_AUTO_BATTERY;
		}
	}

	// -- language -----------------------------------------------------------------

	/** A BCP-47 tag ("en", "bn"), or "" to follow the device. Never null. */
	public String localeTag() {
		return settings.getString(KEY_LOCALE_TAG, "");
	}

	public void setLocaleTag(String _tag) {
		settings.edit().putString(KEY_LOCALE_TAG, _tag == null ? "" : _tag).apply();
	}

	// -- privacy notice -----------------------------------------------------------

	public boolean privacyNoticeAccepted() {
		return local.getInt(KEY_PRIVACY_VERSION, 0) >= PRIVACY_NOTICE_VERSION;
	}

	public void acceptPrivacyNotice() {
		local.edit().putInt(KEY_PRIVACY_VERSION, PRIVACY_NOTICE_VERSION).apply();
	}

	// -- update check -------------------------------------------------------------

	/** The release tag the user has already been offered and waved away, or null. */
	public String dismissedUpdateTag() {
		return local.getString(KEY_UPDATE_DISMISSED, null);
	}

	public void setDismissedUpdateTag(String _tag) {
		local.edit().putString(KEY_UPDATE_DISMISSED, _tag).apply();
	}

	public long lastUpdateCheckAt() {
		return local.getLong(KEY_UPDATE_CHECKED_AT, 0L);
	}

	public void setLastUpdateCheckAt(long _at) {
		local.edit().putLong(KEY_UPDATE_CHECKED_AT, _at).apply();
	}
}
