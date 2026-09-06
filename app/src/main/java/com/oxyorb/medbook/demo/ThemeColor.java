package com.oxyorb.medbook.demo;

import android.content.Context;
import android.util.TypedValue;

/**
 * A Material colour role, resolved from the running theme.
 *
 * The demo screens set backgrounds from code, which a layout would do with ?colorX and
 * code cannot. Reading the role off the theme keeps them honest: colors_m3.xml is
 * generated from the mark and regenerated rather than hand-edited, so a literal hex
 * copied into a class here would quietly stop matching the app the first time the
 * palette is reseeded, and would ignore values-night entirely.
 */
public final class ThemeColor {

	private ThemeColor() {
	}

	public static int of(Context _context, int _attribute) {
		TypedValue _value = new TypedValue();
		_context.getTheme().resolveAttribute(_attribute, _value, true);
		return _value.data;
	}
}
