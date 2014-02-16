package de.maxgb.loadoutsaver.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import loadoutanalyzer.Analyzer;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;

import de.maxgb.android.util.Logger;
import de.maxgb.loadoutsaver.LoadoutMainActivity;
import de.maxgb.loadoutsaver.R;
import de.maxgb.loadoutsaver.util.Constants;
import de.maxgb.loadoutsaver.util.ERROR;
import de.maxgb.loadoutsaver.util.Loadout;



import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

public class Client extends ERROR{
	private final String TAG="Client";
	private static Client instance;
	private SharedPreferences pref;
	HttpClient httpclient;

	
	//Login, SessionKey & Userinfo
	private boolean loggedIn=false;
	private long loggedInSince=0;
	private String sessionKey="";
	private String username="";
	private String personaId="";
	private String platform="1";
	
	
	
	
	//Konstruktor and getInstance method
	private Client(){
		
		final HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams,
				Constants.CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParams,
				Constants.CONNECTION_TIMEOUT);
		httpclient = new DefaultHttpClient(
				httpParams); 
	}
	
	public static synchronized Client getInstance(SharedPreferences pref){
		if (instance == null) {
			instance = new Client();
		}
		instance.setPreferences(pref);
		return instance;
	}
	
	//Public methods
	public void setPreferences(SharedPreferences pref){
		this.pref=pref;
	}
	
	//AsyncTasks
	/*
	  private class LoginTask extends AsyncTask<Void,Void,Integer>{
	

		@Override
		protected Integer doInBackground(Void... params) {
			
			return login();
		}
		
		@Override
		protected void onPostExecute(Integer result){
			Logger.i(TAG,"Login process finished with result: "+result);
			if(result==OK){
				loggedIn=true;
				loggedInSince=System.currentTimeMillis();
			}
			else{
				loggedIn=false;
			}
		}
	}
	*/
	
	
	//Server communication methods
	
		private int checkLogin(){
			if(!isLoggedIn()==false||System.currentTimeMillis()-getLoggedInSince()>Constants.LOGIN_TIMEOUT){
				return login();
			}
			return ERROR.OK;
		}
		
		
		public synchronized int login(){
			
			String email=pref.getString(Constants.EMAIL_KEY,"");
			String password=pref.getString(Constants.PASSWORD_KEY,"");
			
			Long tsLong = System.currentTimeMillis()/1000;
			
			Logger.i(TAG, "Loginvorgang gestartet");
			
			try {
				// Create Http-Post request
				HttpPost httppost = new HttpPost(Constants.LOGIN_URL);
		
				List<NameValuePair> paare = new ArrayList<NameValuePair>(4); // Post-Parameter
				paare.add(new BasicNameValuePair("email", email));
				paare.add(new BasicNameValuePair("password",password));
				paare.add(new BasicNameValuePair("deviceType","1"));
				paare.add(new BasicNameValuePair("timestamp",tsLong.toString()));
				httppost.setEntity(new UrlEncodedFormEntity(paare));
				// -----------------------------------------------
				
				HttpResponse response = httpclient.execute(httppost);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

					ByteArrayOutputStream out = new ByteArrayOutputStream();
					response.getEntity().writeTo(out);
					out.close();
					String responseString = out.toString();
					Logger.i(TAG,responseString);
					
					//Read out values
					int index = responseString.indexOf("sessionKey");
					if(index!=-1){
						sessionKey=responseString.substring(index+13, index+13+32);
						Logger.i(TAG,"SessionKey: "+sessionKey);
					}
					else{
						return NOSESSIONKEY;
					}
					index = responseString.indexOf("personaId");
					if(index!=-1){
						String sub=responseString.substring(index+12);
						index=sub.indexOf('"');
						
						if(index==-1){
							return NOPERSONAID;
						}
						
						personaId=sub.substring(0, index);
						Logger.i(TAG,"PersonaId: "+personaId);
					}
					else{
						return NOPERSONAID;
					}
					index = responseString.indexOf("username");
					if(index!=-1){
						String subString=responseString.substring(index+11);
						username=subString.substring(0,subString.indexOf('"'));
						Logger.i(TAG,"Username: "+username);
					}
					else{
						return NOUSERNAME;
					}
					index= responseString.indexOf("platform");
					if(index !=-1){
						platform=responseString.substring(index+10,index+11);
						Logger.i(TAG, "PlatformId: "+platform);
					}
					loggedInSince=System.currentTimeMillis();
					return OK;
					
				}
				else{
					return REQUESTFAILED;
				}
				
				
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return REQUESTFAILED;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return REQUESTFAILED;
			}
			
			
			
		}
		/**
		 * Downloads the currently equipped Loadout and querys it in LoadoutManager. You need to call LoadoutManager.addQuery() in UI-Thread afterwards
		 * Also logs into Battlelog if neccessary.
		 * @param loadout Empty Loadout of the type which should be saved
		 * @return Errorcode
		 */
		public synchronized int saveCurrentLoadout(Loadout loadout){
			
			//Check login
			int loginResult = checkLogin();
			if(loginResult!=ERROR.OK){
				Logger.w(TAG, "Login failed with result: "+loginResult);
				return loginResult;
			}
			
			if(sessionKey==null||sessionKey.equals("")){
				return NOSESSIONKEY;
			}
			if(username==null||username.equals("")){
				return NOUSERNAME;
			}
			if(personaId==null||sessionKey.equals("")){
				return NOPERSONAID;
			}
			
			
			
			
			Long tsLong = System.currentTimeMillis()/1000;
			
			try {
				// Create Http-Post request
				HttpPost httppost = new HttpPost(Constants.GETLOADOUT_URL);
		
				List<NameValuePair> paare = new ArrayList<NameValuePair>(4); // Post-Parameter
				paare.add(new BasicNameValuePair("personaId", personaId));
				paare.add(new BasicNameValuePair("personaName",username));
				paare.add(new BasicNameValuePair("platformInt",platform));
				paare.add(new BasicNameValuePair("timestamp",tsLong.toString()));
				
				httppost.setEntity(new UrlEncodedFormEntity(paare));
				httppost.addHeader("X-Session-Id", sessionKey);				
				HttpResponse response = httpclient.execute(httppost);
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					ByteArrayOutputStream out = new ByteArrayOutputStream();
					response.getEntity().writeTo(out);
					out.close();
					String responseString = out.toString();
					Logger.i(TAG,"GetLoadout responseString: "+responseString); //TODO Remove if fully working
					
					if(responseString.contains("success\":0")){
						//Anwser with no success;
						return REQUESTFAILED;
					}
					int index=responseString.indexOf("currentLoadout");
					int index2=responseString.indexOf("succes");
					String currentLoadout=responseString.substring(index+16, index2-3);
					
					try{
					Logger.i(TAG,"Current Loadout: "+currentLoadout.substring(0,500)+"!Ende");
					Logger.i(TAG,"Current Loadout Part 2: "+currentLoadout.substring(500,1000)+"!Ende");
					Logger.i(TAG,"Current Loadout Part 3:"+currentLoadout.substring(1000)+"!Ende");//TODO Remove if fully working
					}
					catch(StringIndexOutOfBoundsException e){
						
					}
					
					try{
						//Differentiate between different Loadout Types
						switch(loadout.getType()){
						case Constants.ALL_TYPE:
							loadout.setLoadout(currentLoadout);
							break;
						case Constants.INFANTRY_TYPE:
							loadout.setLoadout(Loadout.getInfantryFromFull(currentLoadout));
							break;
						case Constants.VEHICLE_TYPE:
							loadout.setLoadout(Loadout.getVehicleFromFull(currentLoadout));
							break;
						}
						LoadoutManager.getInstance().queryLoadout(loadout);
						if(pref.getBoolean(Constants.ANALYSE_LOADOUT_KEY,false)){
							Analyzer.analyzeLoadout(loadout);
						}
						
					}
					catch(Exception e){
						Logger.e(TAG,"Failed to save Loadout",e);
						return FAILEDTOSAVE;
					}
					return OK;
					
				}
				else{
					Logger.w(TAG,"Loadout Request Failed with ReasonPhrase: "+response.getStatusLine().getReasonPhrase());
					return REQUESTFAILED;
				}
				
			} catch (UnsupportedEncodingException e) {
				Logger.e(TAG, "Failed to save Current Loadout",e);
			}
			
			catch (ClientProtocolException e) {
				Logger.e(TAG, "Failed to save Current Loadout",e);
			} catch (IOException e) {
				Logger.e(TAG, "Failed to save Current Loadout",e);
			}
			
			return REQUESTFAILED;
			
		}
	
		public synchronized int sendLoadout(String loadout){
			
			//Check login
			int loginResult = checkLogin();
			if(loginResult!=ERROR.OK){
				Logger.w(TAG, "Login failed with result: "+loginResult);
				return loginResult;
			}
			
			if(sessionKey==null||sessionKey.equals("")){
				return NOSESSIONKEY;
			}
			if(username==null||username.equals("")){
				return NOUSERNAME;
			}
			if(personaId==null||sessionKey.equals("")){
				return NOPERSONAID;
			}
			

			
			Long tsLong = System.currentTimeMillis()/1000;
			
			// Create Http-Post request
			HttpPost httppost = new HttpPost(Constants.SAVELOADOUT_URL);
			HttpResponse response;
			
			try {
				List<NameValuePair> paare = new ArrayList<NameValuePair>(5); // Post-Parameter
				paare.add(new BasicNameValuePair("personaId", personaId));
				paare.add(new BasicNameValuePair("game","2048"));
				paare.add(new BasicNameValuePair("platformInt",platform));
				paare.add(new BasicNameValuePair("loadout",loadout));
				paare.add(new BasicNameValuePair("timestamp",tsLong.toString()));
				httppost.setEntity(new UrlEncodedFormEntity(paare));
				httppost.addHeader("X-Session-Id", sessionKey);
				
				
				// -----------------------------------------------
				
				response = httpclient.execute(httppost);
				
			} catch (ClientProtocolException e) {
				// TODO Auto-generated catch block
				Logger.e(TAG,"Error while sending Loadout",e);
				return REQUESTFAILED;
			} catch (IOException e) {
				// TODO Auto-generated catch block
				Logger.e(TAG,"Error while sending Loadout",e);
				return REQUESTFAILED;
			}
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				String responseString;
				try {
					response.getEntity().writeTo(out);
					out.close();
					responseString = out.toString();
				} catch (IOException e) {
					Logger.e(TAG,"Error while reading response",e);
					responseString="";
					
				}
				
				Logger.i(TAG,"SendLoadout responseString: "+responseString);
				
				if(!responseString.contains("success\":1")){
					//Anwser with no success;
					return REQUESTFAILED;
				}
			}
			else{
				Logger.w(TAG,"Loadout Sending failed with ReasonPhrase: "+response.getStatusLine().getReasonPhrase());
				return REQUESTFAILED;
			}
			return OK;
			
		}

		public boolean isLoggedIn() {
			return loggedIn;
		}

		public long getLoggedInSince() {
			return loggedInSince;
		}

	
}
