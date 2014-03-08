package de.maxgb.loadoutsaver;

import java.util.ArrayList;

import com.google.analytics.tracking.android.EasyTracker;

import de.maxgb.android.util.Logger;
import de.maxgb.loadoutsaver.io.LoadoutManager;
import de.maxgb.loadoutsaver.util.Constants;
import android.net.Uri;
import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.util.Log;
import android.view.Menu;
import android.view.View;
import android.widget.CheckBox;
import android.widget.TextView;
import android.widget.Toast;

public class OptionsActivity extends Activity {
	private static final String TAG="Options";
	private TextView email;
	private TextView password;
	private CheckBox analyse;
	private CheckBox screenLock;
	
	  @Override
	  public void onStart() {
	    super.onStart();
	    EasyTracker.getInstance(this).activityStart(this);  // Add this method.
	  }

	  @Override
	  public void onStop() {
	    super.onStop();
	    EasyTracker.getInstance(this).activityStop(this);  // Add this method.
	  }

	  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_options);
		
		email = (TextView) findViewById(R.id.edit_email);
		password = (TextView) findViewById(R.id.edit_password);
		analyse =(CheckBox) findViewById(R.id.checkBox_analyse);
		screenLock =(CheckBox) findViewById(R.id.checkBox_screenlock);
		
		
		SharedPreferences prefs=getSharedPreferences(Constants.PREF_NAME,0);
		
		// Load Settings
		String oldEmail = prefs.getString(Constants.EMAIL_KEY, "");
		String oldPassword= prefs.getString(Constants.PASSWORD_KEY, "");
		
		email.setText(oldEmail);
		password.setText(oldPassword);
		analyse.setChecked(prefs.getBoolean(Constants.ANALYSE_LOADOUT_KEY,false));
		screenLock.setChecked(prefs.getBoolean(Constants.KEEP_SCREEN_ON_KEY,false));
		
		
	}


	
	public void save(View v){
		Logger.i(TAG,"Saving");
		SharedPreferences prefs=getSharedPreferences(Constants.PREF_NAME,0);
		SharedPreferences.Editor editor=prefs.edit();
		
		editor.putString(Constants.EMAIL_KEY,email.getText().toString().trim());
		editor.putString(Constants.PASSWORD_KEY,password.getText().toString().trim());
		editor.putBoolean(Constants.ANALYSE_LOADOUT_KEY,analyse.isChecked());
		editor.putBoolean(Constants.KEEP_SCREEN_ON_KEY,screenLock.isChecked());
		editor.commit();
		finish();
		
	}
	public void abort(View v){
		Logger.i(TAG, "Abort");
		finish();
	}
	public void feedback(View v){
		Logger.i(TAG,"Feedback");
		
		PackageInfo pInfo;
		String version = "X";
		try {
			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
			version = pInfo.versionName;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType("message/rfc822");
		i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"app@maxgb.de"});
		i.putExtra(Intent.EXTRA_SUBJECT, "Feedback to Battlefield 4 Loadout Saver App "+version);
		i.putExtra(Intent.EXTRA_TEXT   , "");
		
   		ArrayList<Uri> uris=new ArrayList<Uri>();
   		if(Logger.getLogFile()!=null) uris.add(Uri.fromFile(Logger.getLogFile()));
   		if(Logger.getOldLogFile()!=null) uris.add(Uri.fromFile(Logger.getOldLogFile()));
   		if(LoadoutManager.getInstance().getLoadoutFileUri()!=null) uris.add(LoadoutManager.getInstance().getLoadoutFileUri());

		i.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
		
		
		try {
		    startActivity(Intent.createChooser(i, "Send mail..."));
		} catch (android.content.ActivityNotFoundException ex) {
		    Toast.makeText(this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
		}
	}
}
