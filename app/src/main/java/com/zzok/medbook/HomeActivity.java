package com.zzok.medbook;

import android.animation.*;
import android.app.*;
import android.content.*;
import android.content.res.*;
import android.graphics.*;
import android.graphics.Typeface;
import android.graphics.drawable.*;
import android.media.*;
import android.net.*;
import android.os.*;
import android.text.*;
import android.text.style.*;
import android.util.*;
import android.view.*;
import android.view.View.*;
import android.view.animation.*;
import android.webkit.*;
import android.widget.*;
import androidx.annotation.*;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.OnApplyWindowInsetsListener;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.zzok.medbook.databinding.*;
import java.io.*;
import java.text.*;
import java.util.*;
import java.util.ArrayList;
import java.util.regex.*;
import org.json.*;

public class HomeActivity extends AppCompatActivity {
	
	private HomeBinding binding;
	
	
	@Override
	protected void onCreate(Bundle _savedInstanceState) {
		super.onCreate(_savedInstanceState);
		binding = HomeBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());
		applyWindowInsets();
		initialize(_savedInstanceState);
		initializeLogic();
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
	
	private void initialize(Bundle _savedInstanceState) {
	}
	
	private void initializeLogic() {
	}
	
}
