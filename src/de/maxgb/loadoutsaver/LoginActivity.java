package de.maxgb.loadoutsaver;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.dm.zbar.android.scanner.ZBarConstants;
import com.dm.zbar.android.scanner.ZBarScannerActivity;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;

import de.maxgb.android.util.InfoBox;
import de.maxgb.android.util.Logger;
import de.maxgb.loadoutsaver.LoadoutMainActivity.IPersonaListener;
import de.maxgb.loadoutsaver.dialogs.EnterChallengeCodeDialog;
import de.maxgb.loadoutsaver.dialogs.LoginCredentialsDialog;
import de.maxgb.loadoutsaver.dialogs.LoginCredentialsDialog.LoginCredentialsDialogListener;
import de.maxgb.loadoutsaver.io.Client;
import de.maxgb.loadoutsaver.io.Client.Persona;
import de.maxgb.loadoutsaver.util.Constants;
import de.maxgb.loadoutsaver.util.RESULT;
import de.maxgb.loadoutsaver.util.UnexpectedStuffException;
import de.maxgb.loadoutsaver.util.UnexpectedStuffException.Location;

public class LoginActivity extends SherlockFragmentActivity implements
		LoginCredentialsDialogListener, Client.IConnectionListener,
		EnterChallengeCodeDialog.EnterChallengeDialogListener {

	/**
	 * Starts the login procedure async and shows a progress bar If there is one
	 * parameter, it used as qr token, if there are two they are used as login
	 * credentials, if there are three the first two are used for the token
	 * challenge
	 * 
	 * @author Max
	 *
	 */
	private class LoginTask extends AsyncTask<String, Void, Integer> {

		@Override
		protected Integer doInBackground(String... params) {
			try {
				if (params == null || params.length > 3) {
					throw new UnexpectedStuffException(
							"The params count for the Logintask is wrong",
							Location.LOGIN);
				}
				if (params.length == 1) {
					Logger.i(TAG, "Starting qr login");
					return Client.getInstance().loginQR(params[0]);
				}
				if (params.length == 3) {
					Logger.i(TAG, "Starting token challenge");
					return Client.getInstance().loginTokenChallenge(params[0],
							params[1]);
				}
				Logger.i(TAG, "Starting credentials login");
				return Client.getInstance().login(params[0], params[1]);
			} catch (UnexpectedStuffException e) {
				Logger.e(TAG, e.toString());
				return RESULT.OTHERERROR;
			}
		}

		@Override
		public void onPostExecute(Integer result) {
			progressDialog.dismiss();
			Resources res = getResources();
			Context c = LoginActivity.this;
			if (result != RESULT.OK) {
				String msg = RESULT.getDescription(result);
				if (msg != null) {
					ErrorHandler.showErrorDialog(c,
							res.getString(R.string.message_failed_to_login)
									+ " " + result + ".\n" + msg,
							RESULT.shouldBeReportable(result));
				} else {
					ErrorHandler.showErrorDialog(c,
							res.getString(R.string.message_failed_to_login)
									+ " " + result);
				}
			}
		}

		@Override
		public void onPreExecute() {
			Logger.i(TAG, "Show ProgressDialog");
			progressDialog = new ProgressDialog(LoginActivity.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setTitle("Logging in");
			progressDialog.setCancelable(false);
			progressDialog.setIndeterminate(false);
			progressDialog.show();
		}

	}
	private class ReLoginTask extends AsyncTask<String, Void, Integer> {

		@Override
		protected Integer doInBackground(String... params) {
			try {
				if (params == null || params.length != 2) {
					throw new UnexpectedStuffException(
							"The params count for the Logintask is wrong",
							Location.LOGIN);
				}
				Logger.i(TAG, "Starting relogin");
				return Client.getInstance().relogin(params[0], params[1]);
			} catch (UnexpectedStuffException e) {
				Logger.e(TAG, e.toString());
				return RESULT.OTHERERROR;
			}
		}

		@Override
		public void onPostExecute(Integer result) {
			progressDialog.dismiss();
			if (result != RESULT.OK) {

			}
		}

		@Override
		public void onPreExecute() {
			Logger.i(TAG, "Show ProgressDialog");
			progressDialog = new ProgressDialog(LoginActivity.this);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setTitle("Trying to log in");
			progressDialog.setCancelable(false);
			progressDialog.setIndeterminate(false);
			progressDialog.show();
		}

	}
	private final static String TAG = "LoginActivity";

	private final int ZBAR_SCANNER_REQUEST = 1;

	private ProgressDialog progressDialog;

	@Override
	public void choosePersona(final ArrayList<Persona> personas,
			final IPersonaListener listener) {

		final Activity activity = this;
		Runnable run = new Runnable() {

			@Override
			public void run() {
				AlertDialog d = null;
				AlertDialog.Builder adb = new AlertDialog.Builder(activity);
				CharSequence items[] = new CharSequence[personas.size()];
				for (int i = 0; i < personas.size(); i++) {
					items[i] = personas.get(i).personaName
							+ " on "
							+ Constants
									.getPlatformFromInt(personas.get(i).platform);
				}

				adb.setSingleChoiceItems(items, -1, new OnClickListener() {

					@Override
					public void onClick(DialogInterface d, int n) {
						listener.choosenPersona(personas.get(n));
						d.dismiss();
					}

				});
				adb.setTitle("Choose your soldier");
				d = adb.show();

			}

		};
		activity.runOnUiThread(run);

	}

	@Override
	public void enterCode(final String code, final String token,final boolean again) {
		final Activity activity = this;
		Runnable run = new Runnable() {

			@Override
			public void run() {
				EnterChallengeCodeDialog d = new EnterChallengeCodeDialog();
				Bundle args = new Bundle();
				args.putString("code", code);
				args.putString("token", token);
				args.putBoolean("again", again);
				d.setArguments(args);
				d.show(getSupportFragmentManager(), "challenge_code_dialog");
			}
		};
		activity.runOnUiThread(run);

	}

	@Override
	public void loggedIn(Persona persona, String mobileToken, String userId) {

		if (mobileToken != null && userId != null) {
			getSharedPreferences(Constants.PREF_NAME, 0).edit()
					.putString(Constants.MOBILE_TOKEN_KEY, mobileToken)
					.putString(Constants.USER_ID, userId).apply();
		}
		Intent i = new Intent();
		i.putExtra("name", persona.personaName);
		i.putExtra("platform", persona.platform);
		this.setResult(Activity.RESULT_OK, i);
		this.finish();

	}

	public void loginCredentials(View v) {
		Logger.i(TAG, "Starting credentials login procedure");
		LoginCredentialsDialog d = new LoginCredentialsDialog();
		d.show(this.getSupportFragmentManager(), "login_credentials");
	}

	public void loginQR(View v) {
		Logger.i(TAG, "Starting qr code login procedure");
		Intent intent = new Intent(this, ZBarScannerActivity.class);
		intent.putExtra("SCAN_MODES", new int[] { 64 });
		startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == ZBAR_SCANNER_REQUEST) {
			if (resultCode == RESULT_OK) {
				String url = data.getStringExtra(ZBarConstants.SCAN_RESULT);
				if (!url.startsWith("http://battlelog.com/app?")) {
					ErrorHandler
							.showErrorDialog(
									this,
									"You have to scan the code from the battlelog ingame",
									false);
					return;
				}
				String token = url.substring(25);
				Logger.i(TAG, url + ":" + token);
				LoginTask task = new LoginTask();
				task.execute(token);

			}
		}

	}

	@Override
	public void onCanceled() {
		Logger.i(TAG, "Credentials dialog canceled");

	}

	@Override
	public void onCanceledCode() {
		// unused

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		Client.getInstance().setConnectionListener(this);

		SharedPreferences pref = getSharedPreferences(Constants.PREF_NAME, 0);
		String mobileToken = pref.getString(Constants.MOBILE_TOKEN_KEY, null);
		String userId = pref.getString(Constants.USER_ID, null);
		if (mobileToken != null && userId != null) {
			ReLoginTask task = new ReLoginTask();
			task.execute(mobileToken, userId);
		}

		InfoBox.showInstructionBox(
				this.getSharedPreferences(Constants.PREF_NAME, 0), this,
				Constants.INSTRUCTION_LOGIN);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.login_activity_actions, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public void onEntered(String email, String password) {
		LoginTask task = new LoginTask();
		task.execute(email, password);

	}

	@Override
	public void onEnteredCode(String token, String code) {
		LoginTask task = new LoginTask();
		task.execute(token, code, "token");

	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_login_help:
			InfoBox.showInstructionBox(null, this, Constants.INSTRUCTION_LOGIN);
			break;
		default:
			break;
		}
		return true;
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
