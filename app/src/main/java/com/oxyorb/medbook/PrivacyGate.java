package com.oxyorb.medbook;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;

import com.oxyorb.medbook.settings.SettingsStore;

/**
 * Puts the privacy notice in front of whatever screen comes up first.
 *
 * Deliberately not hung off HomeActivity, and MedBookApp says why in its own comment:
 * after the process is killed, Android recreates the TOP activity of the task first and
 * only walks down the stack if the user backs up. Someone who was reading a doctor's
 * profile, lost the process and came back would get that profile with HomeActivity
 * never having run -- so a check in HomeActivity.onResume would be skipped exactly when
 * it was most wrong to skip it, and they would be looking at a directory whose notice
 * they had never seen. This is the same trap the theme and the locale were moved out of
 * HomeActivity to avoid, so it is solved the same way: above every activity, once.
 *
 * Registered from Application.onCreate. Nothing else in the app is centralised like
 * this -- window insets and back buttons are duplicated per screen on purpose -- but
 * those are per-screen concerns and this one is not.
 */
public final class PrivacyGate implements Application.ActivityLifecycleCallbacks {

	private final SettingsStore settings;

	/**
	 * Process-scoped, so two activities resuming in quick succession cannot stack two
	 * notices. Cleared again if the notice goes away unaccepted, which only a kill can
	 * do -- back leaves the app rather than dismissing it.
	 */
	private boolean showing;

	private PrivacyGate(SettingsStore _settings) {
		settings = _settings;
	}

	public static void install(Application _application, SettingsStore _settings) {
		_application.registerActivityLifecycleCallbacks(new PrivacyGate(_settings));
	}

	@Override
	public void onActivityResumed(@NonNull Activity _activity) {
		if (settings.privacyNoticeAccepted() || _activity instanceof WelcomeActivity || showing) {
			return;
		}
		showing = true;
		_activity.startActivity(new Intent(_activity, WelcomeActivity.class));
		// No transition override. On a cold start the splash is still being held over
		// this, so there is nothing to see either way, and on a restore the ordinary
		// open animation is the right one.
	}

	@Override
	public void onActivityDestroyed(@NonNull Activity _activity) {
		if (_activity instanceof WelcomeActivity && !settings.privacyNoticeAccepted()) {
			showing = false;
		}
	}

	@Override
	public void onActivityCreated(@NonNull Activity _activity, Bundle _savedInstanceState) {
	}

	@Override
	public void onActivityStarted(@NonNull Activity _activity) {
	}

	@Override
	public void onActivityPaused(@NonNull Activity _activity) {
	}

	@Override
	public void onActivityStopped(@NonNull Activity _activity) {
	}

	@Override
	public void onActivitySaveInstanceState(@NonNull Activity _activity, @NonNull Bundle _outState) {
	}
}
