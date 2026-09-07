package com.oxyorb.medbook;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.RawRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.oxyorb.medbook.databinding.LicencesBinding;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * The licences MedBook is distributed under, and the two it redistributes fonts under.
 *
 * The font licences are here in full rather than as links, and that is the point of the
 * screen. The SIL Open Font License requires the licence to accompany the fonts it
 * covers; MedBook ships subsets of Gabarito and Hind Siliguri inside the APK, and a URL
 * is not accompaniment -- least of all in an app whose whole claim is that it works
 * with no connection at all. The pair costs 8.8KB in res/raw.
 *
 * They stay English. They are the licences as granted, not interface copy, and
 * translating a licence would change what it says.
 */
public class LicencesActivity extends AppCompatActivity {

	private static final String TAG = "LicencesActivity";

	private LicencesBinding binding;

	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		binding = LicencesBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		applyWindowInsets();

		binding.backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				finish();
			}
		});

		binding.agplRow.rowTitle.setText(R.string.licences_app_action);
		binding.agplRow.getRoot().setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				Links.open(LicencesActivity.this, Links.LICENCE);
			}
		});

		// Small enough to read on the main thread: 4.4KB each, out of the APK, on a
		// screen that has nothing else to do while it opens.
		binding.gabaritoText.setText(readRaw(R.raw.gabarito_ofl));
		binding.hindSiliguriText.setText(readRaw(R.raw.hind_siliguri_ofl));
	}

	/**
	 * The licence text, or empty if it could not be read.
	 *
	 * Empty rather than a crash: a licence that fails to load is a compliance problem
	 * to fix in the build, not a reason to take the app down in a user's hand. It
	 * cannot happen from a well-formed APK -- res/raw is packed with the rest -- so if
	 * it ever does, the log line is the thing worth having.
	 */
	private String readRaw(@RawRes int _resource) {
		InputStream _in = null;
		try {
			_in = getResources().openRawResource(_resource);
			ByteArrayOutputStream _out = new ByteArrayOutputStream(_in.available());
			byte[] _buffer = new byte[4096];
			int _read;
			while ((_read = _in.read(_buffer)) != -1) {
				_out.write(_buffer, 0, _read);
			}
			return _out.toString("UTF-8");
		} catch (IOException _e) {
			Log.w(TAG, "Could not read licence text", _e);
			return "";
		} finally {
			if (_in != null) {
				try {
					_in.close();
				} catch (IOException _ignored) {
					// Closing a resource stream that already failed changes nothing.
				}
			}
		}
	}

	/**
	 * As every other screen: from API 35 the system draws edge to edge with no
	 * opt-out, so each root insets itself or the content sits under the status bar.
	 */
	private void applyWindowInsets() {
		final int baseLeft = binding.licencesRoot.getPaddingLeft();
		final int baseTop = binding.licencesRoot.getPaddingTop();
		final int baseRight = binding.licencesRoot.getPaddingRight();
		final int baseBottom = binding.licencesRoot.getPaddingBottom();
		ViewCompat.setOnApplyWindowInsetsListener(binding.licencesRoot, new OnApplyWindowInsetsListener() {
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
