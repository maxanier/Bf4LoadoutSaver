package de.maxgb.loadoutsaver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.actionbarsherlock.app.ActionBar;
import com.actionbarsherlock.app.SherlockActivity;
import com.actionbarsherlock.app.SherlockFragmentActivity;
import com.actionbarsherlock.view.Menu;
import com.actionbarsherlock.view.MenuInflater;
import com.actionbarsherlock.view.MenuItem;
import com.github.johnpersano.supertoasts.SuperActivityToast;
import com.github.johnpersano.supertoasts.SuperCardToast;
import com.github.johnpersano.supertoasts.SuperToast;
import com.google.analytics.tracking.android.EasyTracker;
import com.google.analytics.tracking.android.MapBuilder;
import com.google.analytics.tracking.android.StandardExceptionParser;


import de.maxgb.android.util.InfoBox;
import de.maxgb.android.util.Logger;
import de.maxgb.loadoutsaver.io.Client;
import de.maxgb.loadoutsaver.io.IConnectionListener;
import de.maxgb.loadoutsaver.io.LoadoutManager;
import de.maxgb.loadoutsaver.util.Constants;
import de.maxgb.loadoutsaver.util.RESULT;

import de.maxgb.loadoutsaver.util.Loadout;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.os.PowerManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.view.animation.AnimationUtils;

public class LoadoutMainActivity extends SherlockFragmentActivity implements LoadoutNameDialog.LoadoutNameDialogListener, IConnectionListener{
	
	private ListView list;
	private static final String TAG="MainActivity";
	private ProgressDialog progressDialog;
	private Context context;
	private SuperActivityToast sendingToast;

	
	
	  @Override
	  public void onStart() {
	    super.onStart();
	    EasyTracker.getInstance(this).activityStart(this);  // Starting EasyTracker
	    
	  }

	  @Override
	  public void onStop() {
	    super.onStop();
	    EasyTracker.getInstance(this).activityStop(this);  // Stoping EasyTracker
	  }
	  
	@Override
	public void onPause(){
		this.getWindow().
        clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		super.onPause();

	}
	
	@Override
	public void onResume(){
		super.onResume();
		if(getSharedPreferences().getBoolean(Constants.KEEP_SCREEN_ON_KEY,false)){
			this.getWindow().
	        addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		}
		
	}
	  
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		//Save context for ProgressDialog
		context=this;
		Logger.init(Constants.DIRECTORY);
		Logger.setDebugMode(true);//TODO Replace by user settings
		
		LoadoutManager loadoutManager =LoadoutManager.getInstance();
		Client.getInstance(getSharedPreferences()).setConnectionListener(this);
		
		
		
		list= (ListView) findViewById(R.id.list);
		final CustomArrayAdapter adapter = new CustomArrayAdapter(this,
				R.layout.list_view_item, loadoutManager.getLoadout());
		list.setAdapter(adapter);
		
		list.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {

			
			@SuppressLint("NewApi")  
			@Override
		      public boolean onItemLongClick(AdapterView<?> parent, final View view,
		          int position, long id) {
		        final Loadout item = (Loadout) parent.getItemAtPosition(position);
		        
		        if(android.os.Build.VERSION.SDK_INT >= 16){
			        view.animate().setDuration(2000).alpha(0)
			            .withEndAction(new Runnable() {
			              @Override
			              public void run() {
			            	  LoadoutManager.getInstance().removeLoadout(item);
				              adapter.notifyDataSetChanged();
				              view.setAlpha(1);
			              }
			            });
			        
			      }
		        else{
		        	LoadoutManager.getInstance().removeLoadout(item);
	                adapter.notifyDataSetChanged();
		        }
		        return true;
		      }
		    });
		
		list.setOnItemClickListener(new AdapterView.OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view, int position,
					long arg3) {
				Logger.i(TAG,"Activating Loadout at position: "+position);
				sendLoadout(LoadoutManager.getInstance().getLoadout().get(position));
				view.startAnimation(AnimationUtils.loadAnimation(context, R.anim.view_click));
			}
		});
		//Show beta informations shortly after activity creation
		final SharedPreferences pref = getSharedPreferences();
		final Context con=this;
		list.postDelayed(new Runnable(){

			@Override
			public void run() {
				InfoBox.showInstructionBox(pref,con,Constants.INSTRUCTION_BETA);
				
			}
			
		}, 2000);
		
		InfoBox.showInstructionBox(this.getSharedPreferences(Constants.PREF_NAME,0),this,Constants.INSTRUCTION_OPTIONS);


		if(loadoutManager.checkIfOldFileExists()){
			InfoBox.showInfoBox(this, "Old Loadouts deleted", "Because of a bigger update behind the scenes all previous loadouts had to be deleted. Sorry.");
		}
			
		
	}
	

	
	@Override
	public boolean onCreateOptionsMenu(Menu menu){
		MenuInflater inflater = getSupportMenuInflater();
	    inflater.inflate(R.menu.main_activity_actions, menu);

		return super.onCreateOptionsMenu(menu);
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.action_settings:
			Logger.i(TAG,"Starting Settings");
			Intent i=new Intent(this,OptionsActivity.class);
			startActivity(i);
			break;
		case R.id.action_newLoadout:
			newLoadout();
		default:
			break;
		}
		return true;
	}
	
	
	
	public void newLoadout(){
		LoadoutNameDialog dialog = new LoadoutNameDialog();
		dialog.show(getSupportFragmentManager(),"LoadoutNameDialog");
	}
	
	@Override
	public void addCurrentLoadout(String name,boolean w,boolean k,boolean v){
		
		if(!isOnline()){
			Toast.makeText(getApplicationContext(),getResources().getString(R.string.message_no_connection),Constants.TOAST_DURATION).show();
			return;
		}
		Loadout loadout=new Loadout(name,new JSONObject(),w,k,v);
		
		Logger.i(TAG, "User wants new Loadout: "+loadout.toString(" "));
		SaveLoadoutTask task=new SaveLoadoutTask();
		task.execute(loadout);
	}
	
	private void showErrorDialog(String msg){
		
		// 1. Instantiate an AlertDialog.Builder with its constructor
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		final String message=msg;
		// 2. Chain together various setter methods to set the dialog characteristics
		builder.setMessage(message)
		       .setTitle(R.string.error).setPositiveButton(R.string.report, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                	showErrorReportingDialog(message);
                   }
               }).setNegativeButton(R.string.ok, new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					
					
				}
			});


		// 3. Get the AlertDialog from create()
		AlertDialog dialog = builder.create();
		
		dialog.show();
		
		
		
		
		
	}
	
	private void showErrorReportingDialog(String msg){
		
		// 1. Instantiate an AlertDialog.Builder with its constructor
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				final String message=msg;
				// 2. Chain together various setter methods to set the dialog characteristics
				builder.setMessage("Do you want to report this error to the developer?  A logfile will be appended, which also contains your username, but no further personal information. It really helps fixing the problem! Thanks." )
				       .setTitle(R.string.report).setPositiveButton(R.string.report, new DialogInterface.OnClickListener() {
		                   public void onClick(DialogInterface dialog, int id) {
		                	PackageInfo pInfo;
		               		String version="X";
		               		try {
		               			pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
		               			version = pInfo.versionName;
		               		} catch (NameNotFoundException e) {
		               			
		               			reportError(e);
		               		}
		               		
		               		//Send the email
		               		Intent mailIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
		               		mailIntent.setType("text/plain");
		               		mailIntent.putExtra(Intent.EXTRA_EMAIL  , new String[]{Constants.LOG_REPORT_EMAIL});
		               		mailIntent.putExtra(Intent.EXTRA_SUBJECT, Constants.LOG_REPORT_SUBJECT+version);
		               		mailIntent.putExtra(Intent.EXTRA_TEXT   , "Error: "+message);
		               		
		               		ArrayList<Uri> uris=new ArrayList<Uri>();
		               		if(Logger.getLogFile()!=null) uris.add(Uri.fromFile(Logger.getLogFile()));
		               		if(Logger.getOldLogFile()!=null) uris.add(Uri.fromFile(Logger.getOldLogFile()));
		               		if(LoadoutManager.getInstance().getLoadoutFileUri()!=null) uris.add(LoadoutManager.getInstance().getLoadoutFileUri());

		            		mailIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uris);
		               	    
		               	  //Send, if possible
		               	    try {
		               	       startActivity(Intent.createChooser(mailIntent, "Send mail..."));
		               	    } catch (android.content.ActivityNotFoundException ex) {
		               	        Toast.makeText(getApplicationContext(), 
		               	                   "There are no email clients installed.", 
		               	                   Toast.LENGTH_SHORT).show();
		               	    }
		                   }
		               }).setNegativeButton(R.string.abort, new DialogInterface.OnClickListener() {
						
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// TODO Auto-generated method stub
							
						}
					});


				// 3. Get the AlertDialog from create()
				AlertDialog dialog = builder.create();
				
				dialog.show();
	}
	
	private void reportError(Exception e){
		//refer to: https://developers.google.com/analytics/devguides/collection/android/v3/exceptions
		  EasyTracker easyTracker = EasyTracker.getInstance(this);

		  // StandardExceptionParser is provided to help get meaningful Exception descriptions.
		        easyTracker.send(MapBuilder
		      .createException(new StandardExceptionParser(this, null)              // Context and optional collection of package names
		                                                                            // to be used in reporting the exception.
		                       .getDescription(Thread.currentThread().getName(),    // The name of the thread on which the exception occurred.
		                                       e),                                  // The exception.
		                       false)                                               // False indicates a fatal exception
		      .build()
		  );

	}
	
	private void reportToAnalytics(String category,String label ,String msg,long value){
		EasyTracker tracker=EasyTracker.getInstance(this);
		
		tracker.send(MapBuilder.createEvent(category, label, msg,value ).build()); //TODO test if it works
	}
	
	public void sendLoadout(Loadout loadout){
		if(!isOnline()){
			Logger.i(TAG, "No internet connection");
			Toast.makeText(getApplicationContext(),getResources().getString(R.string.message_no_connection),Constants.TOAST_DURATION).show();
			return;
		}
		
		Logger.i(TAG, "Sending loadout: "+loadout.toString());
		if(sendingToast!=null) sendingToast.dismiss();
		
		sendingToast=new SuperActivityToast(this, SuperToast.Type.PROGRESS).create(this, "Sending Loadout", SuperToast.Duration.EXTRA_LONG);
		
		sendingToast.setIndeterminate(true);

		sendingToast.show();
		
		SendLoadoutTask task=new SendLoadoutTask();
		task.execute(loadout);
	}
	
	/**
	 * Updates the LoadoutListView/Adapter, must be called after Loadoutlist changes
	 */
	public void updateList(){
		Logger.i(TAG, "UpdatingList");
		((CustomArrayAdapter)list.getAdapter()).notifyDataSetChanged();
	}
	

	private SharedPreferences getSharedPreferences(){
		return getSharedPreferences(Constants.PREF_NAME,0);
	}
	
	//AsyncTask---------------------------------------------------------------------
	/**
	 * @author Max Becker
	 * 
	 * 
	 * SaveLoadoutTask
	 * Saves Current Online Loadout to LoadoutManager and shows Feedback
	 * 
	 */
	private class SaveLoadoutTask extends AsyncTask<Loadout,Void,Integer>{
		
		/**
		 * Saves Current Online Loadout with the given name to the LoadoutManager
		 * @param params 1. Loadoutname 2. LoadoutType as String
		 */
		@Override
		protected void onPreExecute(){
			Logger.i(TAG,"Show ProgressDialog");
			progressDialog = new ProgressDialog(context);
			progressDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
			progressDialog.setTitle("Saving");
			progressDialog.setCancelable(false);
			progressDialog.setIndeterminate(false);
			progressDialog.show();
		}
		@Override
		protected Integer doInBackground(Loadout... params) {
			if(params==null||params.length!=1){
				return RESULT.MISSINGPARAMETER;
			}
			Client client=Client.getInstance(getSharedPreferences());
			
			
			
			
			return client.saveCurrentLoadout(params[0]);
		}
		
		@Override
		protected void onPostExecute(Integer result){
			
			try {
				if(progressDialog!=null){
					progressDialog.dismiss();
				}
			} catch (IllegalArgumentException e) {
				reportError(e);
			}
			Resources res = getResources();
			
			if(result==RESULT.OK){
				Logger.i(TAG, "Succesfully saved Loadout");
				LoadoutManager.getInstance().addQuery();
				updateList();
				Toast.makeText(getApplicationContext(),res.getString(R.string.message_successfully_saved_loadout),Constants.TOAST_DURATION).show();
			}
			else if(result==RESULT.LOGINCREDENTIALSMISSING){
				showErrorDialog(res.getString(R.string.message_failed_to_save_loadout)+" "+result+".\nPlease enter your battlelog login credentials in the options menu");
			}
			else if (result==RESULT.NOSESSIONKEY){
				showErrorDialog(res.getString(R.string.message_failed_to_save_loadout)+" "+result+".\nProbably failed to login, please check your Login information");
			}
			else if(result==RESULT.TIMEOUT){
				showErrorDialog(res.getString(R.string.message_failed_to_save_loadout)+" "+result+".\nServer Timeout. Either the server or your internet is too slow.\nTry again later");
			}
			else if(result==RESULT.INTERNALSERVERERROR){
				showErrorDialog(res.getString(R.string.message_failed_to_save_loadout)+" "+result+".\nBattlelog probably changed something on their servers, please report this problem to get it fixed");
			}
			else{
				showErrorDialog(res.getString(R.string.message_failed_to_save_loadout)+" "+result);
			}
			
			reportToAnalytics("ui_action","action","save",result.longValue());
		}
		
	}
	
	/**
	 * 
	 * @author Max Becker
	 *
	 *	Sends the given Loadout to Battlelog 
	 */
	private class SendLoadoutTask extends AsyncTask<Loadout,Void,Integer>{
		/**
		 * Sends the given Loadout to Battlelog
		 * @param params The Loadout to send
		 * @return Error Code see ERROR
		 */
		@Override
		protected Integer doInBackground(Loadout... params) {
			if(params==null||params.length==0){
				return RESULT.MISSINGPARAMETER;
			}
			Client client=Client.getInstance(getSharedPreferences());
			
			
			int saveOldLoadoutResult=client.saveCurrentLoadout(new Loadout("Old Loadout",new JSONObject(),true,true,true));
			if(saveOldLoadoutResult!=RESULT.OK){
				Logger.w(TAG,"Failed to save old Loadout");
				return saveOldLoadoutResult;
			}
			
			//Add queried Loadouts in UI-Thread
			runOnUiThread(new Runnable(){

				@Override
				public void run() {
					LoadoutManager.getInstance().addQuery();
					updateList();
				}
				
			});
			
			
			
			
			try{
				Loadout loadout=params[0];
				
				LoadoutManager loadoutManager=LoadoutManager.getInstance();
				JSONObject full=client.getLastFullLoadout();
				if(full==null){
					return RESULT.NOFULLLOADOUT;
				}
				
				if(loadout.containsKits()){
					full.put(Constants.BJSON_KITS,loadout.getLoadout().getJSONArray(Constants.BJSON_KITS));
				}
				if(loadout.containsVehicle()){
					full.put(Constants.BJSON_VEHICLES, loadout.getLoadout().getJSONArray(Constants.BJSON_VEHICLES));
				}
				if(loadout.containsWeapons()){
					full.put(Constants.BJSON_WEAPONS, loadout.getLoadout().getJSONObject(Constants.BJSON_WEAPONS));
				}
				
				Logger.i(TAG, full.toString());
				return client.sendLoadout(full.toString());
			}
			catch(JSONException e){
				Logger.e(TAG, "Error while putting Loadout together for sending",e);
				return RESULT.PARSINGERROR;
			}

		}
		
		@Override
		protected void onPostExecute(Integer result){
			Logger.i(TAG,"Sent Loadout with Result: "+result);
			if(sendingToast!=null) sendingToast.dismiss();
			Resources res = getResources();
			
			if(result==RESULT.OK){
				SuperToast.create(getApplication(), res.getString(R.string.message_successfully_sent_loadout), SuperToast.Duration.MEDIUM).show();
			}
			else if(result==RESULT.LOGINCREDENTIALSMISSING){
				showErrorDialog(res.getString(R.string.message_failed_to_send_loadout)+" "+result+".\nPlease enter your battlelog login credentials in the options menu");
			}
			else if (result==RESULT.NOSESSIONKEY){
				showErrorDialog(res.getString(R.string.message_failed_to_send_loadout)+" "+result+".\nProbably failed to login, please check your Login information");
			}
			else if(result==RESULT.TIMEOUT){
				showErrorDialog(res.getString(R.string.message_failed_to_send_loadout)+" "+result+".\nServer Timeout. Either the server or your internet is too slow\nTry again later");
			}
			else if(result==RESULT.INTERNALSERVERERROR){
				showErrorDialog(res.getString(R.string.message_failed_to_send_loadout)+" "+result+".\nBattlelog probably changed something on their servers, please report this problem to get it fixed");
			}
			else{
				showErrorDialog(res.getString(R.string.message_failed_to_send_loadout)+" "+result);
			}
			reportToAnalytics("ui_action","action","send",result.longValue());
		}
		
	}
	
	private boolean isOnline(){
		ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo netInfo = cm.getActiveNetworkInfo();
		if (netInfo != null && netInfo.isConnectedOrConnecting())
			return true;
		return false;
		
	}

	@Override
	public void loggedIn(final String persona, final String platform) {
		final Activity context=this;
		this.runOnUiThread(new Runnable(){
			

			@Override
			public void run() {
				SuperCardToast toast=SuperCardToast.create(context,"Logged in with soldier: "+persona+" on "+platform,SuperToast.Duration.LONG);
				toast.setBackground(SuperToast.Background.GREEN);
				toast.show();
				
			}
			
		});


	}

	@Override
	public void failedToLogin(final String error) {
		final Activity context=this;
		this.runOnUiThread(new Runnable(){
			

			@Override
			public void run() {
				SuperCardToast toast=SuperCardToast.create(context,"Failed to login: "+error,SuperToast.Duration.LONG);
				toast.setBackground(SuperToast.Background.RED);
				toast.show();
				
			}
			
		});


		
	}

}
