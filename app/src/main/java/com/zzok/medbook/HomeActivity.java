package com.zzok.medbook;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.splashscreen.SplashScreen;
import androidx.core.splashscreen.SplashScreenViewProvider;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.zzok.medbook.data.DoctorRepository;
import com.zzok.medbook.data.PortraitLoader;
import com.zzok.medbook.data.model.Department;
import com.zzok.medbook.data.model.DoctorSummary;
import com.zzok.medbook.databinding.*;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HomeActivity extends AppCompatActivity implements DirectoryAdapter.Listener {

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
	private DirectoryAdapter adapter;

	private DoctorRepository repository;
	private List<Department> departments = Collections.emptyList();

	private final Handler searchHandler = new Handler(Looper.getMainLooper());
	private final ExecutorService worker = Executors.newSingleThreadExecutor();
	private Runnable pendingSearch;
	private String query = "";

	/**
	 * Bumped on every search. A result whose token is stale is dropped, so a fast
	 * typist cannot have an earlier, slower query overwrite a later one.
	 */
	private int searchToken;

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

		adapter = new DirectoryAdapter(PortraitLoader.get(this), this);
		binding.departmentList.setLayoutManager(new LinearLayoutManager(this));
		binding.departmentList.setAdapter(adapter);

		loadDirectory();
	}

	/**
	 * Opens the directory off the main thread and shows it when it is ready.
	 *
	 * The first launch after an install or a dataset change has to decrypt and
	 * decompress a 12MB database, which measured about 1.9s on the emulator. Every
	 * launch after that is a file open. Doing it on the main thread would be an ANR
	 * on a slow device, so the list stays behind a spinner until this returns.
	 */
	private void loadDirectory() {
		worker.execute(new Runnable() {
			@Override
			public void run() {
				final DoctorRepository _repository = DoctorRepository.open(HomeActivity.this);
				final List<Department> _departments = _repository == null
					? Collections.<Department>emptyList()
					: _repository.departments();
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (isFinishing() || isDestroyed()) {
							return;
						}
						repository = _repository;
						departments = _departments;
						binding.loadingState.setVisibility(View.GONE);
						binding.departmentList.setVisibility(View.VISIBLE);
						applyFilter();
					}
				});
			}
		});
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
	 * rather than continuing. It goes on the list instead, with
	 * clipToPadding="false", so rows scroll under the translucent bar and the
	 * last one can still be brought clear of it.
	 */
	private void applyWindowInsets() {
		final int baseLeft = binding.linearBgHome.getPaddingLeft();
		final int baseTop = binding.linearBgHome.getPaddingTop();
		final int baseRight = binding.linearBgHome.getPaddingRight();
		final int baseBottom = binding.linearBgHome.getPaddingBottom();
		final int baseListBottom = binding.departmentList.getPaddingBottom();
		ViewCompat.setOnApplyWindowInsetsListener(binding.linearBgHome, new OnApplyWindowInsetsListener() {
			@Override
			public WindowInsetsCompat onApplyWindowInsets(View _view, WindowInsetsCompat _insets) {
				Insets _bars = _insets.getInsets(WindowInsetsCompat.Type.systemBars() | WindowInsetsCompat.Type.displayCutout());
				_view.setPadding(baseLeft + _bars.left, baseTop + _bars.top, baseRight + _bars.right, baseBottom);
				binding.departmentList.setPadding(
					binding.departmentList.getPaddingLeft(),
					binding.departmentList.getPaddingTop(),
					binding.departmentList.getPaddingRight(),
					baseListBottom + _bars.bottom);
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

	// -- navigation ---------------------------------------------------------------

	@Override
	public void onDepartmentSelected(Department _department) {
		startActivity(DepartmentDoctorsActivity.intentFor(
			this, _department.id, _department.name, _department.doctorCount));
	}

	@Override
	public void onDoctorSelected(DoctorSummary _doctor) {
		startActivity(DoctorDetailActivity.intentFor(this, _doctor.id));
	}

	// -- search -------------------------------------------------------------------

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
	 * match shows the empty state. The fourth is this build having no directory data
	 * at all, which a checkout without dataset.properties produces.
	 */
	private void applyFilter() {
		if (repository == null) {
			showEmptyState(getString(R.string.directory_unavailable), false);
			return;
		}
		if (query.length() < MIN_QUERY_LENGTH) {
			adapter.showDepartments(departments);
			showList();
			return;
		}
		runSearch(query);
	}

	private void runSearch(final String _query) {
		final int _token = ++searchToken;
		worker.execute(new Runnable() {
			@Override
			public void run() {
				final List<Department> _departments = repository.searchDepartments(_query);
				final List<DoctorSummary> _doctors = repository.searchDoctors(_query);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (_token != searchToken || isFinishing() || isDestroyed()) {
							return;
						}
						if (_departments.isEmpty() && _doctors.isEmpty()) {
							showEmptyState(getString(R.string.search_no_results, _query), true);
							return;
						}
						adapter.showResults(_departments, _doctors,
							getString(R.string.results_departments),
							getString(R.string.results_doctors));
						binding.departmentList.scrollToPosition(0);
						showList();
					}
				});
			}
		});
	}

	private void showList() {
		binding.emptyState.setVisibility(View.GONE);
		binding.departmentList.setVisibility(View.VISIBLE);
	}

	private void showEmptyState(String _subtitle, boolean _offerBrowseAll) {
		binding.searchResultSubtitle.setText(_subtitle);
		binding.searchResultHelp.setVisibility(_offerBrowseAll ? View.VISIBLE : View.GONE);
		binding.browseAll.setVisibility(_offerBrowseAll ? View.VISIBLE : View.GONE);
		binding.emptyState.setVisibility(View.VISIBLE);
		binding.departmentList.setVisibility(View.GONE);
		binding.loadingState.setVisibility(View.GONE);
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
		}
		worker.shutdown();
		super.onDestroy();
	}
}
