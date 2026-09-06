package com.oxyorb.medbook.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.util.Base64;
import android.util.Log;
import com.oxyorb.medbook.BuildConfig;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.GZIPInputStream;
import javax.crypto.Cipher;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.SecretKeySpec;

/**
 * Turns the packed asset into a usable database file in the app's private storage.
 *
 * The directory covers 7,438 named doctors along with their chamber addresses, phone
 * numbers and BMDC registration numbers. Shipping that as a readable file inside the
 * APK would put the whole thing one unzip away from anyone who downloads the app, so
 * the asset is gzipped and AES-256-GCM sealed, and only ever opened into internal
 * storage where the OS keeps other apps out.
 *
 * That is the honest extent of it. The key is compiled into this APK and can be
 * recovered by anyone determined enough to look, and the unpacked database is plain
 * SQLite on disk. This stops casual extraction, not a reverse engineer. Preventing bulk
 * extraction outright would need the data to live on a server, which MedBook does not
 * have and does not want - the directory works with no connection at all.
 *
 * Format, produced by pipeline/pack.py in the dataset archive:
 *
 * <pre>
 *   "MBDB"   4 bytes   magic
 *   0x01     1 byte    format version
 *   nonce   12 bytes   GCM nonce
 *   ...                ciphertext + 16-byte GCM tag, over the gzipped database
 * </pre>
 */
public final class DatasetUnpacker {

	private static final String TAG = "DatasetUnpacker";

	static final String ASSET_NAME = "medbook.dat";
	static final String DATABASE_NAME = "medbook.db";

	private static final byte[] MAGIC = {'M', 'B', 'D', 'B'};
	private static final int FORMAT_VERSION = 1;
	private static final int NONCE_LENGTH = 12;
	private static final int HEADER_LENGTH = MAGIC.length + 1 + NONCE_LENGTH;
	/** GCM tag length in bits; must match what AESGCM in the pipeline produces. */
	private static final int TAG_BITS = 128;

	private static final String PREFS = "dataset";
	private static final String KEY_STAMP = "unpacked_stamp";

	private static final int COPY_BUFFER = 1 << 16;

	private DatasetUnpacker() {
	}

	/**
	 * Unpacks the database if it is missing or out of date, and returns its file.
	 *
	 * Returns null when this build has no directory data at all, which is what a
	 * checkout without dataset.properties produces. Callers show an empty state
	 * rather than treating it as a failure - the build is deliberately allowed to
	 * succeed without the private archive.
	 *
	 * Blocking and slow the first time. Never call it on the main thread.
	 */
	public static File ensureUnpacked(Context _context) {
		if (BuildConfig.DATASET_KEY.isEmpty()) {
			return null;
		}

		long _assetLength = assetLength(_context);
		if (_assetLength < 0) {
			return null;
		}

		File _database = _context.getDatabasePath(DATABASE_NAME);

		// The asset's length changes whenever the dataset is rebuilt, and the version
		// code changes whenever the app is updated. Either is reason to unpack again;
		// together they are cheap to check and need no decryption to compare.
		String _stamp = BuildConfig.VERSION_CODE + ":" + _assetLength;
		SharedPreferences _prefs = _context.getSharedPreferences(PREFS, Context.MODE_PRIVATE);
		if (_database.exists() && _stamp.equals(_prefs.getString(KEY_STAMP, null))) {
			return _database;
		}

		try {
			unpack(_context, _database);
		} catch (Exception _e) {
			// A half-written database is worse than none: it would open, and then
			// return wrong answers. Clear it and report nothing rather than that.
			Log.e(TAG, "could not unpack the doctor directory", _e);
			if (_database.exists() && !_database.delete()) {
				Log.w(TAG, "could not delete the failed database at " + _database);
			}
			_prefs.edit().remove(KEY_STAMP).apply();
			return null;
		}

		_prefs.edit().putString(KEY_STAMP, _stamp).apply();
		return _database;
	}

	private static long assetLength(Context _context) {
		AssetFileDescriptor _descriptor = null;
		try {
			_descriptor = _context.getAssets().openFd(ASSET_NAME);
			return _descriptor.getLength();
		} catch (IOException _e) {
			// Expected when the build had no dataset directory to package.
			return -1L;
		} finally {
			closeQuietly(_descriptor);
		}
	}

	private static void unpack(Context _context, File _database) throws Exception {
		File _parent = _database.getParentFile();
		if (_parent != null && !_parent.exists() && !_parent.mkdirs()) {
			throw new IOException("could not create " + _parent);
		}

		byte[] _packed = readAsset(_context);
		if (_packed.length <= HEADER_LENGTH) {
			throw new IOException("packed dataset is too short: " + _packed.length + " bytes");
		}
		for (int _i = 0; _i < MAGIC.length; _i++) {
			if (_packed[_i] != MAGIC[_i]) {
				throw new IOException("packed dataset has the wrong magic header");
			}
		}
		if (_packed[MAGIC.length] != FORMAT_VERSION) {
			throw new IOException("unsupported dataset format: " + _packed[MAGIC.length]);
		}

		// Decrypted whole rather than streamed: CipherInputStream has a long history of
		// swallowing GCM tag failures and simply reporting end-of-stream, which would
		// turn a corrupt asset into a silently truncated database. doFinal throws.
		byte[] _key = Base64.decode(BuildConfig.DATASET_KEY, Base64.DEFAULT);
		Cipher _cipher = Cipher.getInstance("AES/GCM/NoPadding");
		_cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(_key, "AES"),
			new GCMParameterSpec(TAG_BITS, _packed, MAGIC.length + 1, NONCE_LENGTH));
		byte[] _compressed = _cipher.doFinal(_packed, HEADER_LENGTH, _packed.length - HEADER_LENGTH);

		// Written beside the target and renamed, so a kill mid-write leaves the old
		// database in place instead of a truncated one that would still open.
		File _temp = new File(_database.getPath() + ".tmp");
		InputStream _in = null;
		OutputStream _out = null;
		try {
			_in = new GZIPInputStream(new ByteArrayInputStream(_compressed));
			_out = new FileOutputStream(_temp);
			byte[] _buffer = new byte[COPY_BUFFER];
			int _read;
			while ((_read = _in.read(_buffer)) != -1) {
				_out.write(_buffer, 0, _read);
			}
			_out.flush();
		} finally {
			closeQuietly(_in);
			closeQuietly(_out);
		}

		if (_database.exists() && !_database.delete()) {
			throw new IOException("could not replace " + _database);
		}
		if (!_temp.renameTo(_database)) {
			throw new IOException("could not move the unpacked database into place");
		}
	}

	private static byte[] readAsset(Context _context) throws IOException {
		InputStream _in = null;
		try {
			_in = _context.getAssets().open(ASSET_NAME, android.content.res.AssetManager.ACCESS_STREAMING);
			java.io.ByteArrayOutputStream _buffer = new java.io.ByteArrayOutputStream();
			byte[] _chunk = new byte[COPY_BUFFER];
			int _read;
			while ((_read = _in.read(_chunk)) != -1) {
				_buffer.write(_chunk, 0, _read);
			}
			return _buffer.toByteArray();
		} finally {
			closeQuietly(_in);
		}
	}

	private static void closeQuietly(java.io.Closeable _closeable) {
		if (_closeable == null) {
			return;
		}
		try {
			_closeable.close();
		} catch (IOException _ignored) {
			// Nothing useful to do; the caller already has the real outcome.
		}
	}

	private static void closeQuietly(AssetFileDescriptor _descriptor) {
		if (_descriptor == null) {
			return;
		}
		try {
			_descriptor.close();
		} catch (IOException _ignored) {
			// As above.
		}
	}
}
