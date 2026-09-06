package com.oxyorb.medbook.settings;

import android.os.Build;

import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.os.LocaleListCompat;

/**
 * Applies the stored language, and keeps it reconciled with the system's own.
 *
 * appcompat can store the locale itself, via an autoStoreLocales flag on a
 * metadata service in the manifest. It is deliberately not used here. That path
 * reads the stored value on a background serial executor and then calls
 * recreate() on every live delegate when it lands, so on API 21-32 every cold
 * start would render one activity in the device language and then recreate it --
 * arriving, in this app, right inside HomeActivity's 800ms splash hold. Storing
 * it in SettingsStore and applying it from MedBookApp, before any delegate
 * exists, costs one preferences read and recreates nothing.
 *
 * Not declaring the AppLocalesMetadataHolderService is what opts out: appcompat
 * looks it up and treats NameNotFoundException as "not opted in". One debug log
 * line per process is the whole cost.
 */
public final class LocaleController {

	private LocaleController() {
	}

	/**
	 * Called from Application.onCreate, where there are no activities to recreate.
	 *
	 * From API 33 the framework stores the per-app locale itself, and the user can
	 * change it from system settings without MedBook running. Re-applying our own
	 * preference on top of that would silently undo their choice, so on 33+ an
	 * existing framework locale wins and is written back to the preference instead.
	 * Below 33 there is no framework store, and ours is the only answer.
	 */
	public static void applyAtStartup(SettingsStore _settings) {
		String _stored = _settings.localeTag();
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
			LocaleListCompat _current = AppCompatDelegate.getApplicationLocales();
			if (!_current.isEmpty()) {
				String _tag = _current.get(0).getLanguage();
				if (!_tag.equals(_stored)) {
					_settings.setLocaleTag(_tag);
				}
				return;
			}
		}
		if (!_stored.isEmpty()) {
			AppCompatDelegate.setApplicationLocales(LocaleListCompat.forLanguageTags(_stored));
		}
	}

	/**
	 * Changes the language from the settings screen.
	 *
	 * Stored before it is applied, because applying it recreates every live
	 * activity -- including the one calling this -- and the new instance has to
	 * read back a preference that is already durable.
	 */
	public static void set(SettingsStore _settings, String _tag) {
		String _value = _tag == null ? "" : _tag;
		_settings.setLocaleTag(_value);
		AppCompatDelegate.setApplicationLocales(_value.isEmpty()
			? LocaleListCompat.getEmptyLocaleList()
			: LocaleListCompat.forLanguageTags(_value));
	}
}
