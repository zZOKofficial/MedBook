package com.oxyorb.medbook.data;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import java.util.Locale;

/**
 * A circle with a doctor's initials, for the 1,304 profiles that have no portrait.
 *
 * The source deliberately records those as having no image rather than substituting one
 * of the site's two generic silhouettes, so the app needs something of its own. Initials
 * beat a grey outline: they are legible in a list, and they differ between rows.
 *
 * The colour is derived from the name, so a given doctor keeps the same one every time
 * without anything being stored.
 */
public final class InitialsDrawable extends Drawable {

	/** Muted enough to sit under white text and not fight the rest of the screen. */
	private static final int[] PALETTE = {
		0xFF1976D2, 0xFF00796B, 0xFF5E35B1, 0xFFC2185B,
		0xFF00838F, 0xFF6D4C41, 0xFF455A64, 0xFF827717,
	};

	private final Paint circlePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final Paint textPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
	private final String initials;
	private final Rect textBounds = new Rect();

	public InitialsDrawable(String _displayName) {
		initials = initialsOf(_displayName);
		circlePaint.setColor(PALETTE[Math.abs(hash(_displayName)) % PALETTE.length]);
		textPaint.setColor(Color.WHITE);
		textPaint.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
		textPaint.setTextAlign(Paint.Align.CENTER);
	}

	/**
	 * Skips professional prefixes, which almost every name here carries. Taking the
	 * first two words of "Prof. Dr. M. R. Khan" gives "PD", which is the same for
	 * hundreds of doctors and identifies none of them.
	 */
	private static String initialsOf(String _displayName) {
		if (_displayName == null) {
			return "?";
		}
		StringBuilder _builder = new StringBuilder(2);
		for (String _word : _displayName.split("\\s+")) {
			if (_builder.length() >= 2) {
				break;
			}
			String _clean = _word.replaceAll("[^\\p{L}]", "");
			if (_clean.isEmpty() || isTitle(_clean)) {
				continue;
			}
			_builder.append(Character.toUpperCase(_clean.charAt(0)));
		}
		return _builder.length() == 0 ? "?" : _builder.toString();
	}

	private static boolean isTitle(String _word) {
		String _lower = _word.toLowerCase(Locale.ROOT);
		return _lower.equals("dr") || _lower.equals("prof") || _lower.equals("professor")
			|| _lower.equals("brig") || _lower.equals("gen") || _lower.equals("col")
			|| _lower.equals("lt") || _lower.equals("maj") || _lower.equals("capt")
			|| _lower.equals("mr") || _lower.equals("mrs") || _lower.equals("ms")
			|| _lower.equals("md");
	}

	private static int hash(String _text) {
		return _text == null ? 0 : _text.hashCode();
	}

	@Override
	public void draw(Canvas _canvas) {
		Rect _bounds = getBounds();
		float _radius = Math.min(_bounds.width(), _bounds.height()) / 2f;
		_canvas.drawCircle(_bounds.exactCenterX(), _bounds.exactCenterY(), _radius, circlePaint);

		textPaint.setTextSize(_radius);
		textPaint.getTextBounds(initials, 0, initials.length(), textBounds);
		// Centred on the glyphs' own bounds, not the font's line metrics: the font's
		// ascent leaves room for accents these initials never have, which reads as the
		// text sitting low in the circle.
		_canvas.drawText(initials, _bounds.exactCenterX(),
			_bounds.exactCenterY() + textBounds.height() / 2f, textPaint);
	}

	@Override
	public void setAlpha(int _alpha) {
		circlePaint.setAlpha(_alpha);
		textPaint.setAlpha(_alpha);
	}

	@Override
	public void setColorFilter(ColorFilter _filter) {
		circlePaint.setColorFilter(_filter);
		textPaint.setColorFilter(_filter);
	}

	@Override
	public int getOpacity() {
		return PixelFormat.TRANSLUCENT;
	}
}
