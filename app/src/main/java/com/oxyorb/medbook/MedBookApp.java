package com.oxyorb.medbook;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.oxyorb.medbook.settings.LocaleController;
import com.oxyorb.medbook.settings.SettingsStore;

/**
 * Applies the stored theme before anything is drawn.
 *
 * This has to run before the first activity exists, and that rules out the two
 * other places it could have gone.
 *
 * Not HomeActivity.onCreate: after the process is killed, Android recreates the
 * TOP activity of the task first and only walks down the stack if the user backs
 * up. Someone who was reading a doctor's profile, lost the process, and came back
 * would get that profile in the wrong theme, with HomeActivity never having run.
 *
 * Not an androidx.startup Initializer: it would work, but it needs an explicit
 * startup-runtime dependency to get the compile-time API (we only have it
 * transitively, through emoji2), a keep rule once R8 sees a class instantiated
 * only by reflection, and it hides startup behaviour in a manifest meta-data
 * string. Initializers earn that for library authors who cannot ask for an
 * Application class. We can just have one.
 *
 * Neither this nor an Initializer runs before the splash WINDOW - the system
 * builds that from the activity's theme before the process is ready - so there
 * is no first-frame flash to trade off either way.
 */
public final class MedBookApp extends Application {

	@Override
	public void onCreate() {
		super.onCreate();
		// A synchronous preferences read on the main thread, which is the honest cost
		// of this approach: a few hundred microseconds for a two-key file, in an app
		// that already holds its own splash for 800ms on purpose. Making it async
		// would reintroduce exactly the race this class exists to avoid.
		SettingsStore _settings = SettingsStore.get(this);
		AppCompatDelegate.setDefaultNightMode(_settings.nightMode());
		LocaleController.applyAtStartup(_settings);
	}
}
