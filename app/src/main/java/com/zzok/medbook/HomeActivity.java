package com.zzok.medbook;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
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

	private HomeBinding binding;

	/** The 46 department buttons, collected from the layout rather than named. */
	private final List<MaterialButton> departmentButtons = new ArrayList<>();

	private final Handler searchHandler = new Handler(Looper.getMainLooper());
	private Runnable pendingSearch;
	private String query = "";

	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		SplashScreen.installSplashScreen(this);
		super.onCreate(_savedInstanceState);
		binding = HomeBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		applyWindowInsets();
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
	 * From API 35 onward the system draws content edge to edge and the opt-out is
	 * gone, so the root container has to inset itself or the wordmark sits under
	 * the status bar and the department list runs beneath the navigation bar.
	 */
	private void applyWindowInsets() {
		final int baseLeft = binding.linearBgHome.getPaddingLeft();
		final int baseTop = binding.linearBgHome.getPaddingTop();
		final int baseRight = binding.linearBgHome.getPaddingRight();
		final int baseBottom = binding.linearBgHome.getPaddingBottom();
		ViewCompat.setOnApplyWindowInsetsListener(binding.linearBgHome, new OnApplyWindowInsetsListener() {
			@Override
			public WindowInsetsCompat onApplyWindowInsets(View _view, WindowInsetsCompat _insets) {
				Insets _bars = _insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
				_view.setPadding(baseLeft + _bars.left, baseTop + _bars.top, baseRight + _bars.right, baseBottom + _bars.bottom);
				return _insets;
			}
		});
	}

	/**
	 * Walks the layout instead of referencing 46 generated binding fields, so
	 * adding or removing a department needs no change here. Stage 2 replaces the
	 * container with a RecyclerView and this method goes with it.
	 */
	private void collectDepartmentButtons() {
		for (int _i = 0; _i < binding.linearSpecialty.getChildCount(); _i++) {
			View _child = binding.linearSpecialty.getChildAt(_i);
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

		boolean _empty = _searching && _matches == 0;
		binding.emptyState.setVisibility(_empty ? View.VISIBLE : View.GONE);
		binding.vscrollSpecialty.setVisibility(_empty ? View.GONE : View.VISIBLE);
		if (_empty) {
			binding.searchResultSubtitle.setText(getString(R.string.search_no_results, query));
		}
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
