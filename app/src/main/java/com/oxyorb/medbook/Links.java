package com.oxyorb.medbook;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;

/**
 * The four places MedBook can send you, and the one guard they all need.
 *
 * MedBook holds no internet permission and does not want one. Handing a URL to
 * ACTION_VIEW needs none: the browser already has it, and does the work. So the About
 * and Privacy screens can point at the repository without the app gaining any network
 * reach of its own -- which is worth stating, because "no permissions at all" is a
 * claim those very screens make.
 *
 * There is no address to mail. SECURITY.md routes reports through GitHub's private
 * advisory form rather than an inbox, so the security link is a URL like the rest.
 */
public final class Links {

	private static final String REPOSITORY = "https://github.com/zZOKofficial/MedBook";

	public static final String SOURCE = REPOSITORY;
	public static final String SECURITY = REPOSITORY + "/security";
	public static final String ISSUES = REPOSITORY + "/issues";
	public static final String LICENCE = REPOSITORY + "/blob/main/LICENSE";

	private Links() {
	}

	/**
	 * Opens a link, or says why it could not.
	 *
	 * The same shape as DoctorDetailActivity.dial(): a device with no browser is
	 * unusual but not impossible, and a phone MedBook is meant to be usable on
	 * offline is exactly the kind that might not have one. Catching the exception is
	 * cheaper and more honest than resolveActivity, which from API 30 needs a
	 * queries entry in the manifest to answer truthfully.
	 */
	public static void open(Activity _activity, String _url) {
		try {
			_activity.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(_url)));
		} catch (ActivityNotFoundException _e) {
			Toast.makeText(_activity, R.string.no_browser, Toast.LENGTH_SHORT).show();
		}
	}
}
