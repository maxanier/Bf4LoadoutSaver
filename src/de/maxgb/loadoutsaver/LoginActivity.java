package de.maxgb.loadoutsaver;

import java.util.ArrayList;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;

import de.maxgb.android.util.Logger;
import de.maxgb.loadoutsaver.LoadoutMainActivity.IPersonaListener;
import de.maxgb.loadoutsaver.LoginCredentialsDialog.LoginCredentialsDialogListener;
import de.maxgb.loadoutsaver.io.Client;
import de.maxgb.loadoutsaver.io.Client.Persona;
import de.maxgb.loadoutsaver.util.Constants;
import de.maxgb.loadoutsaver.util.RESULT;
import de.maxgb.loadoutsaver.util.UnexpectedStuffException;
import de.maxgb.loadoutsaver.util.UnexpectedStuffException.Location;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.widget.EditText;
import android.widget.TextView;

public class LoginActivity extends SherlockFragmentActivity implements LoginCredentialsDialogListener,Client.IConnectionListener,EnterChallengeCodeDialog.EnterChallengeDialogListener{

	private final static String TAG="LoginActivity";
	private final int ZBAR_SCANNER_REQUEST=1;
	private ProgressDialog progressDialog;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		Client.getInstance().setConnectionListener(this);
	}
	
	public void loginCredentials(View v){
		Logger.i(TAG, "Starting credentials login procedure");
		LoginCredentialsDialog d=new LoginCredentialsDialog();
		d.show(this.getSupportFragmentManager(), "login_credentials");
	}
	
	public void loginQR(View v){
		Logger.i(TAG, "Starting qr code login procedure");
		Intent intent = new Intent(this, ZBarScannerActivity.class);
		intent.putExtra("SCAN_MODES", new int[]{64});
		startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
	}

	@Override
	public void onEntered(String email, String password) {
		LoginTask task=new LoginTask();
		task.execute(email,password);
		
	}

	@Override
	public void onCanceled() {
		Logger.i(TAG, "Credentials dialog canceled");
		
	}
	
	@Override
	public void choosePersona(final ArrayList<Persona> personas,
			final IPersonaListener listener) {
		
		final Activity activity =this;
		Runnable run=new Runnable(){

			@Override
			public void run() {
				AlertDialog d=null;
				AlertDialog.Builder adb = new AlertDialog.Builder(activity);
				CharSequence items[] = new CharSequence[personas.size()];
				for(int i=0;i<personas.size();i++){
					items[i]=personas.get(i).personaName+" on "+Constants.getPlatformFromInt(personas.get(i).platform);
				}
				
				adb.setSingleChoiceItems(items, -1, new OnClickListener() {

				        @Override
				        public void onClick(DialogInterface d, int n) {
				            listener.choosenPersona(personas.get(n));
				            d.dismiss();
				        }

				});
				adb.setTitle("Choose your soldier");
				d=adb.show();
				
				
			}
			
		};
		activity.runOnUiThread(run);
		
	}

	@Override
	public void loggedIn(Persona persona) {
		Intent i=new Intent();
		i.putExtra("name", persona.personaName);
		i.putExtra("platform", persona.platform);
		this.setResult(RESULT.OK, i);
		this.finish();
		
	}
	

	/**
	 * Starts the login procedure async and shows a progress bar
	 * If there is one parameter, it used as qr token, if there are two they are used as login credentials, if there are three the first two are used for the token challenge
	 * @author Max
	 *
	 */
	private class LoginTask extends AsyncTask<String,Void,Integer>{
		
		public void onPreExecute(){
			Logger.i(TAG, "Show ProgressDialog");
			progressDialog = new ProgressDialog(LoginActivity.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setTitle("Logging in");
			progressDialog.setCancelable(false);
			progressDialog.setIndeterminate(false);
			progressDialog.show();
		}

		@Override
		protected Integer doInBackground(String... params) {
			try {
				if(params==null||params.length>3){
					throw new UnexpectedStuffException("The params count for the Logintask is wrong",Location.LOGIN);
				}
				if(params.length==1){
					Logger.i(TAG, "Starting qr login");
					return Client.getInstance().loginQR(params[0]);
				}
				if(params.length==3){
					Logger.i(TAG, "Starting token challenge");
					return Client.getInstance().loginTokenChallenge(params[0], params[1]);
				}
				Logger.i(TAG, "Starting credentials login");
				return Client.getInstance().login(params[0], params[1]);
			} catch (UnexpectedStuffException e) {
				Logger.e(TAG, e.toString());
				return RESULT.OTHERERROR;
			}
		}
		
		@Override
		public void onPostExecute(Integer result){
			progressDialog.dismiss();
			if(result!=RESULT.OK){
				showErrorDialog("Error code:"+result);
			}
		}
		
	}
	
	/**
	 * Shows a error dialog
	 * @param msg
	 */
	private void showErrorDialog(String msg) {

		// 1. Instantiate an AlertDialog.Builder with its constructor
		AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
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

		// 3. Get the AlertDialog from create()
		AlertDialog dialog = builder.create();

		dialog.show();

	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{   
		if(requestCode==ZBAR_SCANNER_REQUEST){
			if (resultCode == RESULT_OK) 
		    {
				String url=data.getStringExtra(ZBarConstants.SCAN_RESULT);
				if(!url.startsWith("http://battlelog.com/app?")){
					showErrorDialog("You have to scan the code from the battlelog ingame");
					return;
				}
				String token=url.substring(25);
				Logger.i(TAG, url+":"+token);
				LoginTask task=new LoginTask();
				task.execute(token);
		    	
		    }
		}
	    
	}

	@Override
	public void enterCode(final String code, final String token) {
		final Activity activity =this;
		Runnable run=new Runnable(){

			@Override
			public void run() {
				EnterChallengeCodeDialog d=new EnterChallengeCodeDialog();
				Bundle args=new Bundle();
				args.putString("code", code);
				args.putString("token", token);
				d.setArguments(args);
				d.show(getSupportFragmentManager(), "challenge_code_dialog");
			}
		};
		activity.runOnUiThread(run);
		
		
	}

	@Override
	public void onEnteredCode(String token, String code) {
		LoginTask task=new LoginTask();
		task.execute(token,code,"token");
		
	}

	@Override
	public void onCanceledCode() {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onStart() {
		super.onStart();
		GoogleAnalytics mGA = GoogleAnalytics.getInstance(this);

		mGA.setDryRun(Constants.GA_DRY_RUN);
		EasyTracker.getInstance(this).activityStart(this); // Starting
															// EasyTracker

	}

	@Override
	public void onStop() {
		super.onStop();
		EasyTracker.getInstance(this).activityStop(this); // Stoping EasyTracker
	}

}
