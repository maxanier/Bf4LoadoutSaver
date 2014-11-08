package de.maxgb.loadoutsaver;

import java.util.ArrayList;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;

import de.maxgb.android.util.Logger;
import de.maxgb.loadoutsaver.io.LoadoutManager;
import de.maxgb.loadoutsaver.util.Constants;

public class OptionsActivity extends Activity {
	private static final String TAG = "Options";
	private TextView email;
	private TextView password;
	private CheckBox analyse;
	private CheckBox screenLock;
	private CheckBox mixLoadouts;
	

	public void abort(View v) {
		Logger.i(TAG, "Abort");
		finish();
	}

	public void feedback(View v) {
		Logger.i(TAG, "Feedback");

		PackageInfo pInfo;
		String version = "X";
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			version = pInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		Intent i = new Intent(Intent.ACTION_SEND_MULTIPLE);
		i.setType("text/plain");
		i.putExtra(Intent.EXTRA_EMAIL, new String[] { "app@maxgb.de" });
		i.putExtra(Intent.EXTRA_SUBJECT,
				"Feedback to Battlefield 4 Loadout Saver App " + version);
		i.putExtra(Intent.EXTRA_TEXT, "");

		ArrayList<Uri> uris = new ArrayList<Uri>();
		if (Logger.getLogFile() != null)
			uris.add(Uri.fromFile(Logger.getLogFile()));
		if (Logger.getOldLogFile() != null)
			uris.add(Uri.fromFile(Logger.getOldLogFile()));
		if (LoadoutManager.getInstance().getLoadoutFileUri() != null)
			uris.add(LoadoutManager.getInstance().getLoadoutFileUri());

		i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);

		try {
			startActivity(Intent.createChooser(i, "Send mail..."));
		} catch (android.content.ActivityNotFoundException ex) {
			Toast.makeText(this, "There are no email clients installed.",
					Toast.LENGTH_SHORT).show();
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_options);

		email = (TextView) findViewById(R.id.edit_email);
		password = (TextView) findViewById(R.id.edit_password);
		analyse = (CheckBox) findViewById(R.id.checkBox_analyse);
		screenLock = (CheckBox) findViewById(R.id.checkBox_screenlock);
		mixLoadouts = (CheckBox) findViewById(R.id.checkBox_mix_loadouts);

		SharedPreferences prefs = getSharedPreferences(Constants.PREF_NAME, 0);

		// Load Settings
		String oldEmail = prefs.getString(Constants.EMAIL_KEY, "");
		String oldPassword = prefs.getString(Constants.PASSWORD_KEY, "");

		email.setText(oldEmail);
		password.setText(oldPassword);
		analyse.setChecked(prefs.getBoolean(Constants.ANALYSE_LOADOUT_KEY,
				false));
		screenLock.setChecked(prefs.getBoolean(Constants.KEEP_SCREEN_ON_KEY,
				false));
		
		mixLoadouts.setChecked(prefs.getBoolean(Constants.MIX_LOADOUTS_KEY, false));
	}

	@Override
	public void onStart() {
		super.onStart();
		EasyTracker.getInstance(this).activityStart(this); // Add this method.
	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this); // Add this method.
	}

	public void save(View v) {
		
		Logger.i(TAG, "Saving");
		SharedPreferences prefs = getSharedPreferences(Constants.PREF_NAME, 0);
		
		if(!email.getText().toString().trim().equals("")&&!password.getText().toString().equals("")){
			if(!email.getText().toString().trim().equals(prefs.getString(Constants.EMAIL_KEY,""))||!password.getText().toString().equals(prefs.getString(Constants.PASSWORD_KEY, ""))){
				if(prefs.getString(Constants.EMAIL_KEY, "asdf").equals("asdf")&&prefs.getString(Constants.PASSWORD_KEY, "asdf").equals("asdf")){
					reportToAnalytics("status","credentials","first_entered");
				}
				else{
					reportToAnalytics("status","credentials","changed");
				}
			}
		}
		
		SharedPreferences.Editor editor = prefs.edit();

		editor.putString(Constants.EMAIL_KEY, email.getText().toString().trim());
		editor.putString(Constants.PASSWORD_KEY, password.getText().toString()
				.trim());
		editor.putBoolean(Constants.ANALYSE_LOADOUT_KEY, analyse.isChecked());
		editor.putBoolean(Constants.KEEP_SCREEN_ON_KEY, screenLock.isChecked());
		editor.putBoolean(Constants.MIX_LOADOUTS_KEY, mixLoadouts.isChecked());
		editor.commit();
		finish();

	}
	
	private void reportToAnalytics(String category, String label, String msg) {
		EasyTracker tracker = EasyTracker.getInstance(this);

		tracker.send(MapBuilder.createEvent(category, label, msg, null)
				.build()); // TODO test if it works
	}
}
