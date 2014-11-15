package de.maxgb.loadoutsaver;

import java.util.ArrayList;

import com.dm.zbar.android.scanner.ZBarScannerActivity;

import de.maxgb.android.util.Logger;
import de.maxgb.loadoutsaver.LoadoutMainActivity.IPersonaListener;
import de.maxgb.loadoutsaver.LoginCredentialsDialog.LoginCredentialsDialogListener;
import de.maxgb.loadoutsaver.io.Client;
import de.maxgb.loadoutsaver.io.Client.Persona;
import de.maxgb.loadoutsaver.util.Constants;
import de.maxgb.loadoutsaver.util.RESULT;
import android.os.Bundle;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.DialogInterface.OnClickListener;
import android.support.v4.app.FragmentActivity;
import android.view.View;

public class LoginActivity extends FragmentActivity implements LoginCredentialsDialogListener,Client.IConnectionListener{

	private final static String TAG="LoginActivity";
	private final int ZBAR_SCANNER_REQUEST=1;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
	}
	
	public void loginCredentials(View v){
		Logger.i(TAG, "Starting credentials login procedure");
		LoginCredentialsDialog d=new LoginCredentialsDialog();
		d.show(this.getSupportFragmentManager(), "login_credentials");
	}
	
	public void loginQR(View v){
		Logger.i(TAG, "Starting qr code login procedure");
		Intent intent = new Intent(this, ZBarScannerActivity.class);
		startActivityForResult(intent, ZBAR_SCANNER_REQUEST);
	}

	@Override
	public void onEntered(String email, String password) {
		// TODO Auto-generated method stub
		
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
	public void failedToLogin(String error) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void loggedIn(Persona persona) {
		Intent i=new Intent();
		i.putExtra("name", persona.personaName);
		i.putExtra("platform", persona.platform);
		this.setResult(RESULT.OK, i);
		
	}


}
