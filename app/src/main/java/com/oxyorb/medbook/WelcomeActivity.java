package com.oxyorb.medbook;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.oxyorb.medbook.databinding.WelcomeBinding;
import com.oxyorb.medbook.settings.SettingsStore;

/**
 * The privacy notice, shown once before MedBook is used for the first time.
 *
 * One button. There is no decline, and that is a statement about the app rather than a
 * shortcut: MedBook requests no permission and collects nothing, so a button that
 * closed the app would imply it wanted something it does not. What is left is a
 * disclosure -- four claims, each of which the privacy screen states in full and the
 * manifest and backup rules actually enforce.
 *
 * Which is also why back leaves the app rather than dismissing this. A notice that can
 * be swiped past unread is not a notice, and the record that it was shown would be a
 * false one. PrivacyGate puts this screen up; SettingsStore.acceptPrivacyNotice is what
 * takes it down for good.
 *
 * On a cold start this arrives while HomeActivity is still holding its 800ms splash, so
 * the splash hands over to the notice rather than to the home screen, and the directory
 * finishes unpacking behind it.
 */
public class WelcomeActivity extends AppCompatActivity {

	private WelcomeBinding binding;

	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		binding = WelcomeBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		applyWindowInsets();

		final SettingsStore _settings = SettingsStore.get(this);

		binding.fullNoticeRow.rowTitle.setText(R.string.welcome_read_full);
		binding.fullNoticeRow.getRoot().setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				startActivity(new Intent(WelcomeActivity.this, PrivacyActivity.class));
			}
		});

		binding.continueButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				// Recorded before this screen goes away: the gate re-reads the store on
				// the next resume, and it has to already be true by then.
				_settings.acceptPrivacyNotice();
				finish();
			}
		});

		// The first back handling in the app -- every other screen has a plain button
		// that calls finish(). Through the dispatcher rather than onBackPressed, which
		// is deprecated and, at targetSdk 36, is not what predictive back drives.
		getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
			@Override
			public void handleOnBackPressed() {
				finishAffinity();
			}
		});
	}

	/**
	 * As every other screen: from API 35 the system draws edge to edge with no
	 * opt-out, so each root insets itself or the content sits under the status bar.
	 */
	private void applyWindowInsets() {
		final int baseLeft = binding.welcomeRoot.getPaddingLeft();
		final int baseTop = binding.welcomeRoot.getPaddingTop();
		final int baseRight = binding.welcomeRoot.getPaddingRight();
		final int baseBottom = binding.welcomeRoot.getPaddingBottom();
		ViewCompat.setOnApplyWindowInsetsListener(binding.welcomeRoot, new OnApplyWindowInsetsListener() {
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
