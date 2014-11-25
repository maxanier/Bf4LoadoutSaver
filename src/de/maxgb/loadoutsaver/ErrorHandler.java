package de.maxgb.loadoutsaver;

import java.util.ArrayList;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;

import de.maxgb.android.util.Logger;
import de.maxgb.loadoutsaver.io.LoadoutManager;
import de.maxgb.loadoutsaver.util.Constants;

public class ErrorHandler {

	public static void reportError(Context c, Exception e) {
		// refer to:
		// https://developers.google.com/analytics/devguides/collection/android/v3/exceptions
		EasyTracker easyTracker = EasyTracker.getInstance(c);

		// StandardExceptionParser is provided to help get meaningful Exception
		// descriptions.
		easyTracker.send(MapBuilder.createException(
				new StandardExceptionParser(c, null) // Context and optional
														// collection of package
														// names
														// to be used in
														// reporting the
														// exception.
						.getDescription(Thread.currentThread().getName(), // The
																			// name
																			// of
																			// the
																			// thread
																			// on
																			// which
																			// the
																			// exception
																			// occurred.
								e), // The exception.
				false) // False indicates a fatal exception
				.build());

	}

	public static void reportToAnalytics(Context c, String category,
			String label, String msg) {
		EasyTracker tracker = EasyTracker.getInstance(c);

		tracker.send(MapBuilder.createEvent(category, label, msg, null).build());
	}

	public static void reportToAnalytics(Context c, String category,
			String label, String msg, long value) {
		EasyTracker tracker = EasyTracker.getInstance(c);

		tracker.send(MapBuilder.createEvent(category, label, msg, value)
				.build());
	}

	/**
	 * Shows a Error dialog with the given message and with a report button
	 * 
	 * @param msg
	 */
	public static void showErrorDialog(Context c, String msg) {
		showErrorDialog(c, msg, true);
	}

	/**
	 * Shows a Error dialog with the given message and offers if report is true
	 * a report button
	 * 
	 * @param msg
	 * @param report
	 *            whether to show a report button or not
	 */
	public static void showErrorDialog(final Context c, String msg,
			boolean report) {

		// 1. Instantiate an AlertDialog.Builder with its constructor
		AlertDialog.Builder builder = new AlertDialog.Builder(c);
		final String message = msg;
		// 2. Chain together various setter methods to set the dialog
		// characteristics
		builder.setMessage(message)
				.setTitle(R.string.error)
				.setNegativeButton(R.string.ok,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {

							}
						});

		if (report) {
			builder.setPositiveButton(R.string.report,
					new DialogInterface.OnClickListener() {
						@Override
						public void onClick(DialogInterface dialog, int id) {
							showErrorReportingDialog(c, message);
						}
					});
		}
		// 3. Get the AlertDialog from create()
		AlertDialog dialog = builder.create();

		dialog.show();

	}

	public static void showErrorReportingDialog(final Context c, String msg) {

		// 1. Instantiate an AlertDialog.Builder with its constructor
		AlertDialog.Builder builder = new AlertDialog.Builder(c);
		final String message = msg;
		// 2. Chain together various setter methods to set the dialog
		// characteristics
		builder.setMessage(
				"Do you want to report this error to the developer?  A logfile will be appended, which also contains your username, but no further personal information. It really helps fixing the problem! Thanks.")
				.setTitle(R.string.report)
				.setPositiveButton(R.string.report,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								PackageInfo pInfo;
								String version = "X";
								try {
									pInfo = c.getPackageManager()
											.getPackageInfo(c.getPackageName(),
													0);
									version = pInfo.versionName;
								} catch (NameNotFoundException e) {

									reportError(c, e);
								}

								// Send the email
								Intent mailIntent = new Intent(
										Intent.ACTION_SEND_MULTIPLE);
								mailIntent.setType("text/plain");
								mailIntent
										.putExtra(
												Intent.EXTRA_EMAIL,
												new String[] { Constants.LOG_REPORT_EMAIL });
								mailIntent.putExtra(Intent.EXTRA_SUBJECT,
										Constants.LOG_REPORT_SUBJECT + version);
								mailIntent.putExtra(Intent.EXTRA_TEXT,
										"Error: " + message);

								ArrayList<Uri> uris = new ArrayList<Uri>();
								if (Logger.getLogFile() != null)
									uris.add(Uri.fromFile(Logger.getLogFile()));
								if (Logger.getOldLogFile() != null)
									uris.add(Uri.fromFile(Logger
											.getOldLogFile()));
								if (LoadoutManager.getInstance()
										.getLoadoutFileUri() != null)
									uris.add(LoadoutManager.getInstance()
											.getLoadoutFileUri());

								mailIntent.putParcelableArrayListExtra(
										Intent.EXTRA_STREAM, uris);

								// Send, if possible
								try {
									c.startActivity(Intent.createChooser(
											mailIntent, "Send mail..."));
								} catch (android.content.ActivityNotFoundException ex) {
									Toast.makeText(
											c.getApplicationContext(),
											"There are no email clients installed.",
											Toast.LENGTH_SHORT).show();
								}
							}
						})
				.setNegativeButton(R.string.abort,
						new DialogInterface.OnClickListener() {

							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								reportToAnalytics(c, "action", "report",
										"abort");

							}
						});

		// 3. Get the AlertDialog from create()
		AlertDialog dialog = builder.create();

		dialog.show();
	}
}
