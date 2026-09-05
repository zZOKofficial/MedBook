package com.zzok.medbook;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.zzok.medbook.data.DoctorRepository;
import com.zzok.medbook.data.PortraitLoader;
import com.zzok.medbook.data.model.Department;
import com.zzok.medbook.data.model.DoctorSummary;
import com.zzok.medbook.databinding.*;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Every doctor in one department, most established first. */
public class DepartmentDoctorsActivity extends AppCompatActivity implements DirectoryAdapter.Listener {

	private static final String EXTRA_ID = "department_id";
	private static final String EXTRA_NAME = "department_name";
	private static final String EXTRA_COUNT = "department_count";

	/**
	 * Rows per page. The largest department holds 1,420 doctors, so the list is paged
	 * rather than read whole - not for query speed, which is a few milliseconds, but
	 * to avoid holding 1,420 objects and their bitmaps for a list nobody scrolls to
	 * the end of.
	 */
	private static final int PAGE_SIZE = 50;

	/** Start the next page this many rows before the end, so scrolling never stalls. */
	private static final int PREFETCH_ROWS = 10;

	private DepartmentDoctorsBinding binding;
	private DirectoryAdapter adapter;
	private final ExecutorService worker = Executors.newSingleThreadExecutor();

	private DoctorRepository repository;
	private long departmentId;
	private int loaded;
	private boolean loading;
	private boolean exhausted;

	public static Intent intentFor(Context _context, long _departmentId, String _name, int _count) {
		Intent _intent = new Intent(_context, DepartmentDoctorsActivity.class);
		_intent.putExtra(EXTRA_ID, _departmentId);
		_intent.putExtra(EXTRA_NAME, _name);
		_intent.putExtra(EXTRA_COUNT, _count);
		return _intent;
	}

	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		binding = DepartmentDoctorsBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		applyWindowInsets();

		departmentId = getIntent().getLongExtra(EXTRA_ID, -1L);
		binding.departmentTitle.setText(getIntent().getStringExtra(EXTRA_NAME));
		// The department's own total, not how many rows have been paged in so far.
		int _total = getIntent().getIntExtra(EXTRA_COUNT, 0);
		binding.departmentSubtitle.setText(
			getResources().getQuantityString(R.plurals.department_doctor_count, _total, _total));
		binding.backButton.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View _view) {
				finish();
			}
		});

		adapter = new DirectoryAdapter(PortraitLoader.get(this), this);
		binding.doctorList.setLayoutManager(new LinearLayoutManager(this));
		binding.doctorList.setAdapter(adapter);
		binding.doctorList.addOnScrollListener(new RecyclerView.OnScrollListener() {
			@Override
			public void onScrolled(@NonNull RecyclerView _list, int _dx, int _dy) {
				if (_dy <= 0) {
					return;
				}
				LinearLayoutManager _manager = (LinearLayoutManager) _list.getLayoutManager();
				if (_manager != null
					&& _manager.findLastVisibleItemPosition() >= adapter.getItemCount() - PREFETCH_ROWS) {
					loadNextPage();
				}
			}
		});

		// The repository is already open by the time this screen can be reached, so
		// this call is a cheap return of the existing instance rather than a second
		// unpack. It still runs off the main thread because open() makes no promise
		// about that, and a future entry point might reach here first.
		worker.execute(new Runnable() {
			@Override
			public void run() {
				final DoctorRepository _repository = DoctorRepository.open(DepartmentDoctorsActivity.this);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						repository = _repository;
						loadNextPage();
					}
				});
			}
		});
	}

	private void loadNextPage() {
		if (repository == null || loading || exhausted) {
			return;
		}
		loading = true;
		final int _offset = loaded;
		worker.execute(new Runnable() {
			@Override
			public void run() {
				final List<DoctorSummary> _page =
					repository.doctorsInDepartment(departmentId, PAGE_SIZE, _offset);
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (isFinishing() || isDestroyed()) {
							return;
						}
						loading = false;
						if (_page.size() < PAGE_SIZE) {
							exhausted = true;
						}
						if (_offset == 0) {
							adapter.showDoctors(_page);
							binding.departmentEmpty.setVisibility(
								_page.isEmpty() ? View.VISIBLE : View.GONE);
						} else {
							adapter.appendDoctors(_page);
						}
						loaded += _page.size();
					}
				});
			}
		});
	}

	private void applyWindowInsets() {
		final int baseTop = binding.departmentRoot.getPaddingTop();
		final int baseLeft = binding.departmentRoot.getPaddingLeft();
		final int baseRight = binding.departmentRoot.getPaddingRight();
		final int baseListBottom = binding.doctorList.getPaddingBottom();
		ViewCompat.setOnApplyWindowInsetsListener(binding.departmentRoot, new OnApplyWindowInsetsListener() {
			@Override
			public WindowInsetsCompat onApplyWindowInsets(View _view, WindowInsetsCompat _insets) {
				Insets _bars = _insets.getInsets(WindowInsetsCompat.Type.systemBars()
					| WindowInsetsCompat.Type.displayCutout());
				_view.setPadding(baseLeft + _bars.left, baseTop + _bars.top,
					baseRight + _bars.right, _view.getPaddingBottom());
				binding.doctorList.setPadding(
					binding.doctorList.getPaddingLeft(),
					binding.doctorList.getPaddingTop(),
					binding.doctorList.getPaddingRight(),
					baseListBottom + _bars.bottom);
				return _insets;
			}
		});
	}

	@Override
	public void onDepartmentSelected(Department _department) {
		// This screen never shows department rows.
	}

	@Override
	public void onDoctorSelected(DoctorSummary _doctor) {
		startActivity(DoctorDetailActivity.intentFor(this, _doctor.id));
	}

	@Override
	protected void onDestroy() {
		worker.shutdown();
		super.onDestroy();
	}
}
