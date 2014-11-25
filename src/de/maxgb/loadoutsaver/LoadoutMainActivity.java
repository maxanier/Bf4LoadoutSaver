package de.maxgb.loadoutsaver;

import java.util.HashMap;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperCardToast;
import com.github.johnpersano.supertoasts.SuperToast;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.GoogleAnalytics;

import de.maxgb.android.util.InfoBox;
import de.maxgb.android.util.Logger;
import de.maxgb.loadoutsaver.dialogs.LoadoutNameDialog;
import de.maxgb.loadoutsaver.io.Client;
import de.maxgb.loadoutsaver.io.Client.Persona;
import de.maxgb.loadoutsaver.io.LoadoutManager;
import de.maxgb.loadoutsaver.util.Constants;
import de.maxgb.loadoutsaver.util.Loadout;
import de.maxgb.loadoutsaver.util.RESULT;
import de.maxgb.loadoutsaver.util.UnexpectedStuffException;

public class LoadoutMainActivity extends SherlockFragmentActivity implements
		LoadoutNameDialog.LoadoutNameDialogListener {

	public interface IPersonaListener {
		public void choosenPersona(Persona persona);
	}

	// AsyncTask---------------------------------------------------------------------
	/**
	 * @author Max Becker
	 * 
	 * 
	 *         SaveLoadoutTask Saves Current Online Loadout to LoadoutManager
	 *         and shows Feedback
	 * 
	 */
	private class SaveLoadoutTask extends AsyncTask<Loadout, Void, Integer> {

		@Override
		protected Integer doInBackground(Loadout... params) {
			if (params == null || params.length != 1) {
				Logger.e(TAG, "Wrong param count at SaveLoadoutTask");
				return RESULT.OTHERERROR;
			}
			Client client = Client.getInstance();

			try {
				return client.saveCurrentLoadout(params[0]);
			} catch (UnexpectedStuffException e) {
				Logger.e(TAG, e.toString());
				return RESULT.OTHERERROR;
			}
		}

		@Override
		protected void onPostExecute(Integer result) {

			try {
				if (progressDialog != null) {
					progressDialog.dismiss();
				}
			} catch (IllegalArgumentException e) {
				ErrorHandler.reportError(LoadoutMainActivity.this, e);
			}

			Logger.i(TAG, "SaveLoadoutTask finished with: " + result);
			Resources res = getResources();
			Context c = LoadoutMainActivity.this;

			if (result == RESULT.OK) {
				Logger.i(TAG, "Succesfully saved Loadout");
				LoadoutManager.getInstance().addQuery();
				updateList();
				Toast.makeText(
						getApplicationContext(),
						res.getString(R.string.message_successfully_saved_loadout),
						Constants.TOAST_DURATION).show();
			} else {
				String msg = RESULT.getDescription(result);
				if (msg != null) {
					ErrorHandler
							.showErrorDialog(
									c,
									res.getString(R.string.message_failed_to_save_loadout)
											+ " " + result + ".\n\n" + msg,
									RESULT.shouldBeReportable(result));
				} else if (result == RESULT.NOSTATS) {
					ErrorHandler
							.showErrorDialog(
									c,
									res.getString(R.string.message_failed_to_save_loadout)
											+ " "
											+ result
											+ ".\n\nYour soldier ("
											+ Client.getInstance()
													.getPersonaName()
											+ ") was not found. Maybe you do not own BF4 or have not played it yet. If that is not the case please report the problem, so I can investigate it.");
				} else if (result == RESULT.SESSION_EXPIRED) {
					startLoginActivity(false);
				} else {
					ErrorHandler
							.showErrorDialog(
									c,
									res.getString(R.string.message_failed_to_save_loadout)
											+ " " + result);
				}
			}

		}

		/**
		 * Saves Current Online Loadout with the given name to the
		 * LoadoutManager
		 * 
		 * @param params
		 *            1. Loadoutname 2. LoadoutType as String
		 */
		@Override
		protected void onPreExecute() {
			Logger.i(TAG, "Show ProgressDialog");
			progressDialog = new ProgressDialog(getActivity());
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setTitle("Saving");
			progressDialog.setCancelable(false);
			progressDialog.setIndeterminate(false);
			progressDialog.show();
		}

	}

	/**
	 * 
	 * @author Max Becker
	 * 
	 *         Sends the given Loadout to Battlelog
	 */
	private class SendLoadoutTask extends AsyncTask<Loadout, Void, Integer> {

		/**
		 * Sends the given Loadout to Battlelog
		 * 
		 * @param params
		 *            The Loadout to send
		 * @return Error Code see ERROR
		 */
		@Override
		protected Integer doInBackground(Loadout... params) {
			if (params == null || params.length == 0) {
				Logger.e(TAG, "Wrong param count at SaveLoadoutTask");
				return RESULT.OTHERERROR;
			}
			Logger.i(TAG, "Sending loadout: " + params[0].toString());
			Client client = Client.getInstance();

			int saveOldLoadoutResult = 0;
			try {
				saveOldLoadoutResult = client.saveCurrentLoadout(new Loadout(
						"Old Loadout", new JSONObject(), true, true, true,
						Color.BLACK, ""));
			} catch (UnexpectedStuffException e1) {
				Logger.e(TAG, e1.toString());
				return RESULT.OTHERERROR;
			}
			if (saveOldLoadoutResult != RESULT.OK) {
				Logger.w(TAG, "Failed to save old Loadout");
				return saveOldLoadoutResult;
			}

			// Add queried Loadouts in UI-Thread
			runOnUiThread(new Runnable() {

				@Override
				public void run() {
					LoadoutManager.getInstance().addQuery();
					updateList();
				}

			});

			try {
				Loadout loadout = params[0];

				LoadoutManager loadoutManager = LoadoutManager.getInstance();
				JSONObject full = client.getLastFullLoadout();
				if (full == null) {
					Logger.e(TAG,
							"Can't find last full loadout -> cant complete and send Loadout");
					return RESULT.OTHERERROR;
				}

				if (loadout.containsKits()) {
					full.put(Constants.BJSON_KITS, loadout.getLoadout()
							.getJSONArray(Constants.BJSON_KITS));
				}
				if (loadout.containsVehicle()) {
					full.put(Constants.BJSON_VEHICLES, loadout.getLoadout()
							.getJSONArray(Constants.BJSON_VEHICLES));
				}
				if (loadout.containsWeapons()) {
					full.put(Constants.BJSON_WEAPONS, loadout.getLoadout()
							.getJSONObject(Constants.BJSON_WEAPONS));
				}
				String id = loadout.getPersonaId();
				if (getSharedPreferences().getBoolean(
						Constants.MIX_LOADOUTS_KEY, false)) {
					id = "";
				}

				return client.sendLoadout(full.toString(), id);
			} catch (JSONException e) {
				Logger.e(TAG,
						"Error while putting Loadout together for sending", e);
				return RESULT.OTHERERROR;
			} catch (UnexpectedStuffException e) {
				Logger.e(TAG, e.toString());
				return RESULT.OTHERERROR;
			}

		}

		@Override
		protected void onPostExecute(Integer result) {
			Logger.i(TAG, "Sent Loadout with Result: " + result);
			if (sendingToast != null)
				sendingToast.dismiss();
			Resources res = getResources();
			Context c = LoadoutMainActivity.this;
			if (result == RESULT.OK) {
				SuperToast
						.create(getApplication(),
								res.getString(R.string.message_successfully_sent_loadout),
								SuperToast.Duration.MEDIUM).show();
			} else {
				String msg = RESULT.getDescription(result);
				if (msg != null) {
					ErrorHandler
							.showErrorDialog(
									c,
									res.getString(R.string.message_failed_to_send_loadout)
											+ " " + result + ".\n\n" + msg,
									RESULT.shouldBeReportable(result));
				} else if (result == RESULT.SESSION_EXPIRED) {
					startLoginActivity(false);
				} else if (result == RESULT.NOSTATS) {
					ErrorHandler
							.showErrorDialog(
									c,
									res.getString(R.string.message_failed_to_send_loadout)
											+ " "
											+ result
											+ ".\n\nYour soldier ("
											+ Client.getInstance()
													.getPersonaName()
											+ ") was not found. Maybe you do not own BF4 or have not played it yet. If that is not the case please report the problem, so I can investigate it.");
				} else {
					ErrorHandler
							.showErrorDialog(
									c,
									res.getString(R.string.message_failed_to_send_loadout)
											+ " " + result);
				}
			}

		}

		@Override
		public void onPreExecute() {

			if (sendingToast != null)
				sendingToast.dismiss();

			// new SuperActivityToast(this, SuperToast.Type.PROGRESS);
			sendingToast = SuperActivityToast.create(getActivity(),
					"Sending Loadout", SuperToast.Duration.EXTRA_LONG);

			sendingToast.setIndeterminate(true);

			sendingToast.show();
		}

	}
	private ListView list;
	private static final String TAG = "MainActivity";
	private static final int LOGIN_ACTIVITY_RESULT = 1;
	private ProgressDialog progressDialog;

	private HashMap<AsyncTask<Loadout, ?, ?>, Loadout> quequedTasks;

	private SuperActivityToast sendingToast;

	@Override
	public void addCurrentLoadout(String name, boolean w, boolean k, boolean v,
			int color) {

		if (!isOnline()) {
			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.message_no_connection),
					Constants.TOAST_DURATION).show();
			return;
		}
		Loadout loadout = new Loadout(name, new JSONObject(), w, k, v, color,
				"");

		Logger.i(TAG, "User wants new Loadout: " + loadout.toString());

		SaveLoadoutTask task = new SaveLoadoutTask();

		if (!handleLogin()) {
			this.quequeTask(task, loadout);
			Logger.i(TAG, "Not logged in, quequeing SaveLoadoutTask");
			return;
		}
		task.execute(loadout);
	}

	private void executeQuequedTasks() {
		for (AsyncTask<Loadout, ?, ?> task : quequedTasks.keySet()) {
			task.execute(quequedTasks.get(task));
		}
		quequedTasks.clear();
	}

	protected Activity getActivity() {
		return this;
	}

	private SharedPreferences getSharedPreferences() {
		return getSharedPreferences(Constants.PREF_NAME, 0);
	}

	/**
	 * Checks if the user is logged in, if not starts the login procedure
	 * 
	 * @return Logged in
	 */
	private boolean handleLogin() {
		if (Client.getInstance().isLoggedIn()) {
			return true;
		} else {
			startLoginActivity(true);
			return false;
		}
	}

	private boolean isOnline() {
		ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting())
			return true;
		return false;

	}

	private void loggedIn(final String name, final int platform) {
		try {
			this.runOnUiThread(new Runnable() {

				@Override
				public void run() {

					SuperCardToast toast = SuperCardToast.create(getActivity(),
							"Logged in with soldier: " + name + " on "
									+ Constants.getPlatformFromInt(platform),
							SuperToast.Duration.LONG);
					toast.setBackground(SuperToast.Background.GREEN);
					toast.show();

				}

			});
		} catch (Exception e) {
			Logger.e(TAG, "Error while showing logged in Toast", e);
			ErrorHandler.reportError(this, e);
		}

	}

	public void newLoadout() {
		LoadoutNameDialog dialog = new LoadoutNameDialog();
		dialog.show(getSupportFragmentManager(), "LoadoutNameDialog");
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent i) {
		if (requestCode == LOGIN_ACTIVITY_RESULT) {
			if (resultCode == Activity.RESULT_OK) {
				this.loggedIn(i.getStringExtra("name"),
						i.getIntExtra("platform", -1));
				this.executeQuequedTasks();
			} else if (resultCode == Activity.RESULT_CANCELED) {
				try {
					this.runOnUiThread(new Runnable() {
						@Override
						public void run() {

							SuperCardToast toast = SuperCardToast.create(
									getActivity(),
									"You have to login to use this app",
									SuperToast.Duration.LONG);
							toast.setBackground(SuperToast.Background.ORANGE);
							toast.show();
						}

					});
				} catch (Exception e) {
					Logger.e(TAG, "Error while showing logged in Toast", e);
					ErrorHandler.reportError(this, e);
				}
			}
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		// Init Logger
		Logger.init(Constants.DIRECTORY);
		Logger.setDebugMode(true);
		boolean inEmulator = Logger.inEmulator();
		if (inEmulator) {
			Logger.setDebugMode(false);// Disable debug mode if in emulator
			Constants.GA_DRY_RUN = true;// Stop GA logging
		}

		Logger.addRegex("\"mobileToken\":\"\\S{152}\"",
				"\"mobileToken\":\"removed\"");
		Logger.addRegex("\"sessionKey\":\"\\w{32}\"",
				"\"sessionKey\":\"removed\"");

		// Initialize LoadoutManager and Client
		LoadoutManager loadoutManager = LoadoutManager.getInstance();

		// Setup loadout list
		list = (ListView) findViewById(R.id.list);
		final CustomArrayAdapter adapter = new CustomArrayAdapter(this,
				R.layout.list_view_item, loadoutManager.getLoadout());
		list.setAdapter(adapter);

		list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			@SuppressLint("NewApi")
			@Override
			public boolean onItemLongClick(AdapterView<?> parent,
					final View view, int position, long id) {
				final Loadout item = (Loadout) parent
						.getItemAtPosition(position);

				if (android.os.Build.VERSION.SDK_INT >= 16) {
					view.animate().setDuration(2000).alpha(0)
							.withEndAction(new Runnable() {
								@Override
								public void run() {
									LoadoutManager.getInstance().removeLoadout(
											item);
									adapter.notifyDataSetChanged();
									view.setAlpha(1);
								}
							});

				} else {
					LoadoutManager.getInstance().removeLoadout(item);
					adapter.notifyDataSetChanged();
				}
				return true;
			}
		});

		list.setOnItemClickListener(new AdapterView.OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long arg3) {
				Logger.i(TAG, "Activating Loadout at position: " + position);
				sendLoadout(LoadoutManager.getInstance().getLoadout()
						.get(position));
				view.startAnimation(AnimationUtils.loadAnimation(getActivity(),
						R.anim.view_click));
			}
		});

		// Initialize other stuff
		quequedTasks = new HashMap<AsyncTask<Loadout, ?, ?>, Loadout>();

		// Show beta informations shortly after activity creation
		final SharedPreferences pref = getSharedPreferences();
		final Context con = this;
		list.postDelayed(new Runnable() {

			@Override
			public void run() {
				InfoBox.showInstructionBox(pref, con,
						Constants.INSTRUCTION_BETA);

			}

		}, 2000);

		InfoBox.showInstructionBox(
				this.getSharedPreferences(Constants.PREF_NAME, 0), this,
				Constants.INSTRUCTION_MAIN);

		// -----------------------------------------------------------

	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getSupportMenuInflater();
		inflater.inflate(R.menu.main_activity_actions, menu);

		return super.onCreateOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Logger.i(TAG, "Starting Settings");
			Intent i = new Intent(this, OptionsActivity.class);
			startActivity(i);
			break;
		case R.id.action_newLoadout:
			newLoadout();
			break;
		case R.id.action_main_help:
			InfoBox.showInstructionBox(null, this, Constants.INSTRUCTION_MAIN);
			break;
		default:
			break;
		}
		return true;
	}

	@Override
	public void onPause() {
		this.getWindow().clearFlags(
				WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		super.onPause();

	}

	@Override
	public void onResume() {
		super.onResume();
		if (getSharedPreferences().getBoolean(Constants.KEEP_SCREEN_ON_KEY,
				false)) {
			this.getWindow().addFlags(
					WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		}

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

	private void quequeTask(AsyncTask<Loadout, ?, ?> task, Loadout l) {
		if (task == null) {
			return;
		}
		quequedTasks.put(task, l);
	}

	public void sendLoadout(Loadout loadout) {
		if (!isOnline()) {
			Logger.i(TAG, "No internet connection");
			Toast.makeText(getApplicationContext(),
					getResources().getString(R.string.message_no_connection),
					Constants.TOAST_DURATION).show();
			return;
		}

		SendLoadoutTask task = new SendLoadoutTask();

		if (!handleLogin()) {
			Logger.i(TAG, "Not logged in, quequeing SendLoadoutTask");
			this.quequeTask(task, loadout);
			return;
		}

		task.execute(loadout);
	}

	private void startLoginActivity(boolean allowRelogin) {
		Intent i = new Intent(this, LoginActivity.class);
		i.putExtra("relogin", allowRelogin);
		this.startActivityForResult(i, LOGIN_ACTIVITY_RESULT);
	}

	/**
	 * Updates the LoadoutListView/Adapter, must be called after Loadoutlist
	 * changes
	 */
	public void updateList() {
		Logger.i(TAG, "UpdatingList");
		((CustomArrayAdapter) list.getAdapter()).notifyDataSetChanged();
	}

}
