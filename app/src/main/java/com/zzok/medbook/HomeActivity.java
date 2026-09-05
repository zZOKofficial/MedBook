package com.zzok.medbook;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.splashscreen.SplashScreenViewProvider;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import com.google.android.material.button.MaterialButton;
import com.zzok.medbook.databinding.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class HomeActivity extends AppCompatActivity {

	/** Below this, searching is more noise than signal, so the full list stays. */
	private static final int MIN_QUERY_LENGTH = 2;
	/** Long enough to skip most intermediate keystrokes, short enough to feel live. */
	private static final long SEARCH_DEBOUNCE_MS = 250L;

	private static final String STATE_QUERY = "query";

	/** Matches windowSplashScreenAnimationDuration in Theme.MedBook.Splash. */
	private static final long SPLASH_ANIMATION_MS = 800L;
	/** The hand-off from splash to home screen. */
	private static final long SPLASH_EXIT_MS = 260L;
	private static final float SPLASH_EXIT_SCALE = 1.08f;

	private HomeBinding binding;

	/** The 46 department buttons, collected from the layout rather than named. */
	private final List<MaterialButton> departmentButtons = new ArrayList<>();

	/** Each clinical family's heading and the container holding its departments. */
	private final List<View[]> familySections = new ArrayList<>();

	private final Handler searchHandler = new Handler(Looper.getMainLooper());
	private Runnable pendingSearch;
	private String query = "";

	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		SplashScreen _splash = SplashScreen.installSplashScreen(this);
		holdSplashForAnimation(_splash);
		animateSplashExit(_splash);
		super.onCreate(_savedInstanceState);
		binding = HomeBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		applyWindowInsets();
		flattenSearchField();
		collectDepartmentButtons();
		wireSearch();
		binding.browseAll.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				binding.searchDocs.setQuery("", false);
				setQuery("");
			}
		});
		if (_savedInstanceState != null) {
			query = _savedInstanceState.getString(STATE_QUERY, "");
		}
		applyFilter();
	}

	/**
	 * The system dismisses the splash as soon as the first frame is ready, which
	 * on anything but a cold start is well before an 800ms animation has played,
	 * so the mark would be torn away mid-sweep.
	 *
	 * This is a real delay and worth being honest about. It is not the same as
	 * the 500ms Timer in the old MainActivity this replaced: that one waited for
	 * nothing at all, this one waits exactly as long as there is something to
	 * watch. Drop the call in onCreate to remove the hold entirely.
	 */
	private void holdSplashForAnimation(SplashScreen _splash) {
		final long _shownAt = SystemClock.uptimeMillis();
		_splash.setKeepOnScreenCondition(new SplashScreen.KeepOnScreenCondition() {
			@Override
			public boolean shouldKeepOnScreen() {
				return SystemClock.uptimeMillis() - _shownAt < SPLASH_ANIMATION_MS;
			}
		});
	}

	/**
	 * Hands the splash over instead of cutting to the home screen.
	 *
	 * Worth knowing why this exists: the system starts the icon's own sweep when
	 * it creates the splash window, not when that window reaches the screen. On
	 * the emulator those are about 830ms apart and the sweep runs for 740ms, so
	 * the animation had already finished by the time anything was visible - it
	 * measured as a completely static mark until the global animator scale was
	 * turned up to 10x, which stretched it enough to catch.
	 *
	 * The exit is the part whose timing we own, so it is the part that can be
	 * relied on to be seen. Making the sweep itself reliably visible would mean
	 * holding the splash roughly 1.3s, which is a real cost at every launch.
	 */
	private void animateSplashExit(SplashScreen _splash) {
		_splash.setOnExitAnimationListener(new SplashScreen.OnExitAnimationListener() {
			@Override
			public void onSplashScreenExit(final SplashScreenViewProvider _provider) {
				_provider.getView()
					.animate()
					.alpha(0f)
					.scaleX(SPLASH_EXIT_SCALE)
					.scaleY(SPLASH_EXIT_SCALE)
					.setDuration(SPLASH_EXIT_MS)
					.withEndAction(new Runnable() {
						@Override
						public void run() {
							_provider.remove();
						}
					})
					.start();
			}
		});
	}

	/**
	 * From API 35 onward the system draws content edge to edge and the opt-out is
	 * gone, so the layout has to inset itself or the wordmark sits under the
	 * status bar.
	 *
	 * The bottom inset deliberately does not go on the root. Padding the root
	 * ends the list above the navigation bar and leaves a dead band of surface
	 * with the gesture pill floating in it, which reads as the list running out
	 * rather than continuing. It goes on the scroller instead, with
	 * clipToPadding="false", so rows scroll under the translucent bar and the
	 * last one can still be brought clear of it.
	 */
	private void applyWindowInsets() {
		final int baseLeft = binding.linearBgHome.getPaddingLeft();
		final int baseTop = binding.linearBgHome.getPaddingTop();
		final int baseRight = binding.linearBgHome.getPaddingRight();
		final int baseBottom = binding.linearBgHome.getPaddingBottom();
		final int baseScrollBottom = binding.vscrollSpecialty.getPaddingBottom();
		ViewCompat.setOnApplyWindowInsetsListener(binding.linearBgHome, new OnApplyWindowInsetsListener() {
			@Override
			public WindowInsetsCompat onApplyWindowInsets(View _view, WindowInsetsCompat _insets) {
				Insets _bars = _insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
				_view.setPadding(baseLeft + _bars.left, baseTop + _bars.top, baseRight + _bars.right, baseBottom);
				binding.vscrollSpecialty.setPadding(
					binding.vscrollSpecialty.getPaddingLeft(),
					binding.vscrollSpecialty.getPaddingTop(),
					binding.vscrollSpecialty.getPaddingRight(),
					baseScrollBottom + _bars.bottom);
				return _insets;
			}
		});
	}

	/**
	 * androidx SearchView nests an EditText that keeps its own background, so the
	 * default underline is drawn straight across the capsule from bg_search. The
	 * platform ids are the only handle on those inner views.
	 */
	private void flattenSearchField() {
		View _plate = binding.searchDocs.findViewById(androidx.appcompat.R.id.search_plate);
		if (_plate != null) {
			_plate.setBackground(null);
		}
		View _text = binding.searchDocs.findViewById(androidx.appcompat.R.id.search_src_text);
		if (_text != null) {
			_text.setBackground(null);
		}
	}

	/**
	 * Walks the layout instead of referencing 46 generated binding fields, so
	 * adding or removing a department needs no change here. Stage 2 replaces the
	 * container with a RecyclerView and this method goes with it.
	 *
	 * It recurses because the departments are nested one level down, inside a
	 * container per clinical family, so that the dividers between rows stay
	 * inside a group and do not run under its heading.
	 */
	private void collectDepartmentButtons() {
		collectDepartmentButtons(binding.linearSpecialty);
		collectFamilySections();
	}

	/**
	 * Pairs each heading with the container that follows it, so a family can be
	 * hidden whole. Without this, filtering hides the departments but leaves
	 * every heading behind, and a search for "cardio" reads as twelve empty
	 * sections above three results.
	 */
	private void collectFamilySections() {
		View _heading = null;
		for (int _i = 0; _i < binding.linearSpecialty.getChildCount(); _i++) {
			View _child = binding.linearSpecialty.getChildAt(_i);
			if (_child instanceof ViewGroup) {
				if (_heading != null) {
					familySections.add(new View[] { _heading, _child });
					_heading = null;
				}
			} else {
				_heading = _child;
			}
		}
	}

	private void collectDepartmentButtons(ViewGroup _parent) {
		for (int _i = 0; _i < _parent.getChildCount(); _i++) {
			View _child = _parent.getChildAt(_i);
			if (_child instanceof ViewGroup) {
				collectDepartmentButtons((ViewGroup) _child);
				continue;
			}
			if (!(_child instanceof MaterialButton)) {
				continue;
			}
			final MaterialButton _button = (MaterialButton) _child;
			final String _name = _button.getText().toString();
			_button.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View _view) {
					onDepartmentSelected(_name);
				}
			});
			departmentButtons.add(_button);
		}
	}

	private void onDepartmentSelected(String _department) {
		// Placeholder until the doctor directory exists; see the roadmap in README.
		Toast.makeText(this, getString(R.string.department_coming_soon, _department), Toast.LENGTH_SHORT).show();
	}

	private void wireSearch() {
		binding.searchDocs.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
			@Override
			public boolean onQueryTextSubmit(String _text) {
				setQuery(_text);
				binding.searchDocs.clearFocus();
				return true;
			}

			@Override
			public boolean onQueryTextChange(String _text) {
				scheduleSearch(_text);
				return true;
			}
		});
	}

	/** Coalesces keystrokes so the list is filtered once per pause, not per letter. */
	private void scheduleSearch(String _text) {
		if (pendingSearch != null) {
			searchHandler.removeCallbacks(pendingSearch);
		}
		final String _pending = _text == null ? "" : _text.trim();
		pendingSearch = new Runnable() {
			@Override
			public void run() {
				setQuery(_pending);
			}
		};
		searchHandler.postDelayed(pendingSearch, SEARCH_DEBOUNCE_MS);
	}

	private void setQuery(String _text) {
		String _next = _text == null ? "" : _text.trim();
		if (_next.equals(query)) {
			return;
		}
		query = _next;
		applyFilter();
	}

	/**
	 * Four states, not two: an empty or too-short query shows every department and
	 * no message; a matching query shows the matches; only a real query with no
	 * match shows the empty state.
	 */
	private void applyFilter() {
		boolean _searching = query.length() >= MIN_QUERY_LENGTH;
		int _matches = 0;

		for (MaterialButton _button : departmentButtons) {
			boolean _visible = !_searching || matches(_button.getText().toString(), query);
			_button.setVisibility(_visible ? View.VISIBLE : View.GONE);
			if (_visible && _searching) {
				_matches++;
			}
		}

		for (View[] _section : familySections) {
			boolean _any = hasVisibleDepartment((ViewGroup) _section[1]);
			_section[0].setVisibility(_any ? View.VISIBLE : View.GONE);
			_section[1].setVisibility(_any ? View.VISIBLE : View.GONE);
		}

		boolean _empty = _searching && _matches == 0;
		binding.emptyState.setVisibility(_empty ? View.VISIBLE : View.GONE);
		binding.vscrollSpecialty.setVisibility(_empty ? View.GONE : View.VISIBLE);
		if (_empty) {
			binding.searchResultSubtitle.setText(getString(R.string.search_no_results, query));
		}
	}

	private static boolean hasVisibleDepartment(ViewGroup _container) {
		for (int _i = 0; _i < _container.getChildCount(); _i++) {
			View _child = _container.getChildAt(_i);
			if (_child instanceof MaterialButton && _child.getVisibility() == View.VISIBLE) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Case-insensitive substring match, with "&" and "and" treated as the same
	 * thing so typing "cardiothoracic and vascular" finds
	 * "Cardiothoracic &amp; Vascular Surgery".
	 */
	private static boolean matches(String _name, String _query) {
		return normalise(_name).contains(normalise(_query));
	}

	private static String normalise(String _text) {
		return _text.toLowerCase(Locale.ROOT).replace("&", "and").replaceAll("\\s+", " ").trim();
	}

	@Override
	protected void onSaveInstanceState(Bundle _outState) {
		super.onSaveInstanceState(_outState);
		_outState.putString(STATE_QUERY, query);
	}

	@Override
	protected void onDestroy() {
		if (pendingSearch != null) {
			searchHandler.removeCallbacks(pendingSearch);
			pendingSearch = null;
		}
		super.onDestroy();
	}

}
