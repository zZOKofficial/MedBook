package com.oxyorb.medbook;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.oxyorb.medbook.databinding.AboutBinding;

/**
 * What MedBook is, which build this is, and where everything in it came from.
 *
 * Deliberately carries no doctor or chamber count. A checkout without the private
 * dataset builds an app whose directory is empty and which is supposed to run anyway,
 * so a number compiled in here would be a plain falsehood in exactly that build -- and
 * a true one would go stale the next time the dataset is rebuilt. The README keeps the
 * statistics, where they describe one snapshot and can be corrected with it.
 *
 * This is the first screen to read BuildConfig.VERSION_NAME. Until now the version
 * lived in build.gradle, two README badges and SECURITY.md, all kept in step by hand.
 */
public class AboutActivity extends AppCompatActivity {

	private AboutBinding binding;

	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		binding = AboutBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		applyWindowInsets();

		binding.backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				finish();
			}
		});

		binding.versionText.setText(getString(R.string.about_version,
			BuildConfig.VERSION_NAME, BuildConfig.VERSION_CODE));

		// An included row carries no text of its own: an <include> cannot pass one down.
		binding.privacyRow.rowTitle.setText(R.string.settings_privacy);
		binding.privacyRow.getRoot().setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				startActivity(new Intent(AboutActivity.this, PrivacyActivity.class));
			}
		});

		binding.licencesRow.rowTitle.setText(R.string.licences_title);
		binding.licencesRow.getRoot().setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				startActivity(new Intent(AboutActivity.this, LicencesActivity.class));
			}
		});

		binding.sourceRow.rowTitle.setText(R.string.about_source);
		binding.sourceRow.rowSummary.setText(R.string.about_source_summary);
		binding.sourceRow.rowSummary.setVisibility(View.VISIBLE);
		binding.sourceRow.getRoot().setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				Links.open(AboutActivity.this, Links.SOURCE);
			}
		});

		binding.securityRow.rowTitle.setText(R.string.about_security);
		binding.securityRow.rowSummary.setText(R.string.about_security_summary);
		binding.securityRow.rowSummary.setVisibility(View.VISIBLE);
		binding.securityRow.getRoot().setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				Links.open(AboutActivity.this, Links.SECURITY);
			}
		});
	}

	/**
	 * As every other screen: from API 35 the system draws edge to edge with no
	 * opt-out, so each root insets itself or the content sits under the status bar.
	 */
	private void applyWindowInsets() {
		final int baseLeft = binding.aboutRoot.getPaddingLeft();
		final int baseTop = binding.aboutRoot.getPaddingTop();
		final int baseRight = binding.aboutRoot.getPaddingRight();
		final int baseBottom = binding.aboutRoot.getPaddingBottom();
		ViewCompat.setOnApplyWindowInsetsListener(binding.aboutRoot, new OnApplyWindowInsetsListener() {
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
