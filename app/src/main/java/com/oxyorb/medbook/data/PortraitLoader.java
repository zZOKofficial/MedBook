package com.oxyorb.medbook.data;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.Looper;
import android.util.LruCache;
import android.widget.ImageView;
import java.io.IOException;
import java.io.InputStream;
import java.lang.ref.WeakReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Loads doctor portraits out of the APK's assets.
 *
 * The 6,134 portraits ship as 160px WebP and are stored uncompressed in the APK, so
 * AssetManager reads them straight out of the mapped file - there is no unpacking step
 * and they cost nothing in the app's data directory. That is also why there is no image
 * loading library here: nothing is downloaded, nothing is cached to disk, and the work
 * is a decode and a cache lookup.
 */
public final class PortraitLoader {

	private static final String ASSET_DIRECTORY = "portraits/";

	/** Enough to keep a scrolled list warm without competing with the rest of the app. */
	private static final int CACHE_FRACTION_OF_HEAP = 8;

	/** Two is plenty: decoding a 3.6 KB WebP is fast, and more threads just queue. */
	private static final int DECODE_THREADS = 2;

	private static PortraitLoader instance;

	private final AssetManager assets;
	private final LruCache<String, Bitmap> cache;
	private final ExecutorService decoders = Executors.newFixedThreadPool(DECODE_THREADS);
	private final Handler main = new Handler(Looper.getMainLooper());

	private PortraitLoader(Context _context) {
		assets = _context.getApplicationContext().getAssets();
		int _limit = (int) (Runtime.getRuntime().maxMemory() / 1024 / CACHE_FRACTION_OF_HEAP);
		cache = new LruCache<String, Bitmap>(_limit) {
			@Override
			protected int sizeOf(String _key, Bitmap _bitmap) {
				return _bitmap.getByteCount() / 1024;
			}
		};
	}

	public static synchronized PortraitLoader get(Context _context) {
		if (instance == null) {
			instance = new PortraitLoader(_context);
		}
		return instance;
	}

	/**
	 * Shows a doctor's portrait, or their initials when there is no portrait to show.
	 *
	 * The view is tagged with the portrait it was asked for, and a finished decode is
	 * only applied if that tag still matches. Without it, a recycled row in a fast
	 * scroll ends up wearing whichever image happens to finish last.
	 */
	public void load(ImageView _view, String _portrait, String _displayName) {
		_view.setTag(_portrait);

		if (_portrait == null) {
			_view.setImageDrawable(new InitialsDrawable(_displayName));
			return;
		}

		Bitmap _cached = cache.get(_portrait);
		if (_cached != null) {
			_view.setImageBitmap(_cached);
			return;
		}

		// Placeholder while the decode runs, so a row never flashes the previous
		// doctor's face during a scroll.
		_view.setImageDrawable(new InitialsDrawable(_displayName));

		final String _name = _portrait;
		final WeakReference<ImageView> _reference = new WeakReference<>(_view);
		decoders.execute(new Runnable() {
			@Override
			public void run() {
				final Bitmap _bitmap = decode(_name);
				if (_bitmap == null) {
					return;
				}
				cache.put(_name, _bitmap);
				main.post(new Runnable() {
					@Override
					public void run() {
						ImageView _target = _reference.get();
						if (_target != null && _name.equals(_target.getTag())) {
							_target.setImageBitmap(_bitmap);
						}
					}
				});
			}
		});
	}

	private Bitmap decode(String _portrait) {
		InputStream _in = null;
		try {
			_in = assets.open(ASSET_DIRECTORY + _portrait, AssetManager.ACCESS_STREAMING);
			return BitmapFactory.decodeStream(_in);
		} catch (IOException _e) {
			// A referenced portrait that is not in assets means the database and the
			// portrait set were built from different runs. The initials already
			// showing are a correct answer, so there is nothing to do here.
			return null;
		} finally {
			if (_in != null) {
				try {
					_in.close();
				} catch (IOException _ignored) {
					// Nothing useful to do.
				}
			}
		}
	}
}
