package com.oxyorb.medbook;

import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.oxyorb.medbook.databinding.PrivacyBinding;

/**
 * The privacy notice in full, and the only copy of it in the app.
 *
 * Everything it claims is enforced somewhere a reader can go and check: the empty
 * permission list at the top of AndroidManifest.xml, and the exclusions in
 * backup_rules.xml and data_extraction_rules.xml. It is kept in step with PRIVACY.md
 * at the root of the repository, and its wording is what SettingsStore's
 * PRIVACY_NOTICE_VERSION counts -- change the substance here and that goes up, so
 * everyone is shown the new text once.
 *
 * All prose, no state. The one thing a reader might need to act on is the corrections
 * row at the bottom.
 */
public class PrivacyActivity extends AppCompatActivity {

	private PrivacyBinding binding;

	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		binding = PrivacyBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		applyWindowInsets();

		binding.backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				finish();
			}
		});

		binding.correctionsRow.rowTitle.setText(R.string.privacy_corrections_action);
		binding.correctionsRow.rowSummary.setText(R.string.privacy_corrections_summary);
		binding.correctionsRow.rowSummary.setVisibility(View.VISIBLE);
		binding.correctionsRow.getRoot().setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				Links.open(PrivacyActivity.this, Links.ISSUES);
			}
		});
	}

	/**
	 * As every other screen: from API 35 the system draws edge to edge with no
	 * opt-out, so each root insets itself or the content sits under the status bar.
	 */
	private void applyWindowInsets() {
		final int baseLeft = binding.privacyRoot.getPaddingLeft();
		final int baseTop = binding.privacyRoot.getPaddingTop();
		final int baseRight = binding.privacyRoot.getPaddingRight();
		final int baseBottom = binding.privacyRoot.getPaddingBottom();
		ViewCompat.setOnApplyWindowInsetsListener(binding.privacyRoot, new OnApplyWindowInsetsListener() {
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
