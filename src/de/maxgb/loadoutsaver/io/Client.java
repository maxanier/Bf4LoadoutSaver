package de.maxgb.loadoutsaver.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

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
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.SharedPreferences;
import de.maxgb.android.util.Logger;
import de.maxgb.loadoutsaver.util.Constants;
import de.maxgb.loadoutsaver.util.Loadout;
import de.maxgb.loadoutsaver.util.RESULT;

public class Client {
	private final String TAG = "Client";
	private static Client instance;

	public static synchronized Client getInstance(SharedPreferences pref) {
		if (instance == null) {
			instance = new Client();
		}
		instance.setPreferences(pref);
		return instance;
	}

	private SharedPreferences pref;

	HttpClient httpclient;
	// Login, SessionKey & Userinfo
	private boolean loggedIn = false;
	private long loggedInSince = 0;
	private String sessionKey = "";
	private String personaName = "";
	private String personaId = "";

	private int platform = 1;
	private JSONObject lastFullLoadout;

	private IConnectionListener conListener;

	// Konstruktor and getInstance method
	private Client() {

		final HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams,
				Constants.CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParams,
				Constants.CONNECTION_TIMEOUT);
		httpclient = new DefaultHttpClient(httpParams);
		lastFullLoadout = null;
	}

	private int checkLogin() {
		if (!isLoggedIn() == false
				|| System.currentTimeMillis() - getLoggedInSince() > Constants.LOGIN_TIMEOUT) {
			return login();
		}
		return RESULT.OK;
	}

	// AsyncTasks
	/*
	 * private class LoginTask extends AsyncTask<Void,Void,Integer>{
	 * 
	 * 
	 * @Override protected Integer doInBackground(Void... params) {
	 * 
	 * return login(); }
	 * 
	 * @Override protected void onPostExecute(Integer result){
	 * Logger.i(TAG,"Login process finished with result: "+result);
	 * if(result==OK){ loggedIn=true; loggedInSince=System.currentTimeMillis();
	 * } else{ loggedIn=false; } } }
	 */

	// Server communication methods

	public JSONObject getLastFullLoadout() {

		return lastFullLoadout;
	}

	public long getLoggedInSince() {
		return loggedInSince;
	}

	public boolean isLoggedIn() {
		return loggedIn;
	}

	public synchronized int login() {

		String email = pref.getString(Constants.EMAIL_KEY, "");
		String password = pref.getString(Constants.PASSWORD_KEY, "");

		if (email.equals("") || password.equals("")) {
			if (conListener != null)
				conListener.failedToLogin("Login credentials missing");
			return RESULT.LOGINCREDENTIALSMISSING;
		}
		Long tsLong = System.currentTimeMillis() / 1000;

		Logger.i(TAG, "Loginvorgang gestartet");

		try {
			// Create Http-Post request
			HttpPost httppost = new HttpPost(Constants.LOGIN_URL);

			List<NameValuePair> paare = new ArrayList<NameValuePair>(4); // Post-Parameter
			paare.add(new BasicNameValuePair("email", email));
			paare.add(new BasicNameValuePair("password", password));
			paare.add(new BasicNameValuePair("deviceType", "1"));
			paare.add(new BasicNameValuePair("timestamp", tsLong.toString()));
			httppost.setEntity(new UrlEncodedFormEntity(paare));
			// -----------------------------------------------

			HttpResponse response = httpclient.execute(httppost);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				String responseString = out.toString();
				Logger.i(TAG, "Response String: " + responseString);

				// Read out
				// values------------------------------------------------------------------
				sessionKey = null;
				platform = 0;
				personaName = null;
				personaId = null;

				try {
					JSONObject responseJson = new JSONObject(responseString);
					JSONObject data = responseJson.getJSONObject("data");

					if (data != null) {

						try {
							sessionKey = data.getString("sessionKey");
						} catch (JSONException e) {

						}

						JSONObject activePersona = null;
						try {
							activePersona = data
									.getJSONObject("activePersonas")
									.getJSONObject("2");
						} catch (JSONException e1) {

						}

						if (activePersona != null) {

							try {
								platform = activePersona.getInt("platform");
							} catch (JSONException e) {

							}

							JSONObject persona = null;
							try {
								persona = activePersona
										.getJSONObject("persona");
							} catch (JSONException e) {

							}

							if (persona != null) {
								try {
									personaName = persona
											.getString("personaName");
									personaId = persona.getString("personaId");
								} catch (JSONException e) {

								}
							} else {
								Logger.w(TAG, "persona not found");
							}
						} else {
							Logger.w(TAG, "activePersona not found");
						}

					} else {
						Logger.w(TAG, "DataObject in response json is null");
					}

				} catch (JSONException e) {
					Logger.w(TAG, "Failed to parse response to JSON");
				}

				Logger.i(TAG,
						"After JSON analysis the following information was found: ID: "
								+ personaId + ", Name: " + (personaName)
								+ ", Key: " + sessionKey + ", Platform: "
								+ platform);

				// In case something was not found -> String analysis
				int index;
				if (sessionKey == null) {
					index = responseString.indexOf("sessionKey");
					if (index != -1) {
						sessionKey = responseString.substring(index + 13,
								index + 13 + 32);
						Logger.i(TAG, "SessionKey: " + sessionKey);
					} else {
						if (conListener != null)
							conListener.failedToLogin("Login request failed");
						return RESULT.NOSESSIONKEY;
					}
				}

				if (personaId == null) {
					index = responseString.indexOf("personaId");
					if (index != -1) {
						String sub = responseString.substring(index + 12);
						index = sub.indexOf('"');

						if (index == -1) {
							return RESULT.NOPERSONAID;
						}

						personaId = sub.substring(0, index);

					} else {
						if (conListener != null)
							conListener.failedToLogin("Login request failed");
						return RESULT.NOPERSONAID;
					}
				}

				if (personaName == null) {
					index = responseString.indexOf("username");
					if (index != -1) {
						String subString = responseString.substring(index + 11);
						personaName = subString.substring(0,
								subString.indexOf('"'));

					} else {
						if (conListener != null)
							conListener.failedToLogin("Login request failed");
						return RESULT.NOUSERNAME;
					}
				}
				if (platform == 0) {
					index = responseString.indexOf("platform");
					
					if (index != -1) {
						int index2=responseString.indexOf(',',index);
						platform = Integer.parseInt(responseString.substring(
								index + 10, index2));
						Logger.i(TAG, "PlatformId: " + platform);
					}
					else{
						return RESULT.NOPLATFORMID;
					}
				}
				Logger.i(TAG, "Login analysis complete: SessionKey: "
						+ sessionKey + ", PersonaName: " + personaName
						+ ", PersonaId: " + personaId + ", Platform: "
						+ platform);

				loggedInSince = System.currentTimeMillis();

				if (conListener != null)
					conListener.loggedIn(personaName,
							Constants.getPlatformFromInt(platform));
				return RESULT.OK;

			} else {
				if (conListener != null)
					conListener.failedToLogin("Login request failed");
				return RESULT.REQUESTFAILED;
			}

		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return RESULT.REQUESTFAILED;
		} catch (SocketTimeoutException e) {
			Logger.w(TAG, "Timeout during login");
			return RESULT.TIMEOUT;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return RESULT.REQUESTFAILED;
		}

	}

	/**
	 * Downloads the currently equipped Loadout and querys it in LoadoutManager.
	 * You need to call LoadoutManager.addQuery() in UI-Thread afterwards Also
	 * logs into Battlelog if neccessary.
	 * 
	 * @param loadout
	 *            Empty Loadout of the type which should be saved
	 * @return Errorcode
	 */
	public synchronized int saveCurrentLoadout(Loadout loadout) {

		// Check login
		int loginResult = checkLogin();
		if (loginResult != RESULT.OK) {
			Logger.w(TAG, "Login failed with result: " + loginResult);
			return loginResult;
		}

		if (sessionKey == null || sessionKey.equals("")) {
			return RESULT.NOSESSIONKEY;
		}
		if (personaName == null || personaName.equals("")) {
			return RESULT.NOUSERNAME;
		}
		if (personaId == null || sessionKey.equals("")) {
			return RESULT.NOPERSONAID;
		}

		Long tsLong = System.currentTimeMillis() / 1000;

		try {
			// Create Http-Post request
			HttpPost httppost = new HttpPost(Constants.GETLOADOUT_URL);

			List<NameValuePair> paare = new ArrayList<NameValuePair>(4); // Post-Parameter
			paare.add(new BasicNameValuePair("personaId", personaId));
			paare.add(new BasicNameValuePair("personaName", personaName));
			paare.add(new BasicNameValuePair("platformInt", "" + platform));
			paare.add(new BasicNameValuePair("timestamp", tsLong.toString()));

			httppost.setEntity(new UrlEncodedFormEntity(paare));
			httppost.addHeader("X-Session-Id", sessionKey);
			HttpResponse response = httpclient.execute(httppost);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				String responseString = out.toString();
				String debugShortResponse;
				try {
					debugShortResponse = responseString.substring(0, 200);
				} catch (Exception e1) {
					debugShortResponse = responseString;
				}

				Logger.i(TAG, "GetLoadout short responseString: "
						+ debugShortResponse); // TODO Remove if fully working

				if (responseString.contains("success\":0")) {
					// Answer with no success;
					return RESULT.REQUESTFAILED;
				}
				
				if(responseString.contains("\"error\":\"nostats\"")){
					//Soldier has no stats or does not exist
					return RESULT.NOSTATS;
				}

				JSONObject currentLoadout = null;
				JSONArray vehicles = null;
				JSONObject weapons = null;
				JSONArray kits = null;

				try {
					JSONObject responseJson = new JSONObject(responseString);
					JSONObject data = responseJson
							.getJSONObject(Constants.BJSON_DATA);
					currentLoadout = data
							.getJSONObject(Constants.BJSON_CURRENT_LOADOUT);
					vehicles = currentLoadout
							.getJSONArray(Constants.BJSON_VEHICLES);
					weapons = currentLoadout
							.getJSONObject(Constants.BJSON_WEAPONS);
					kits = currentLoadout.getJSONArray(Constants.BJSON_KITS);

				} catch (JSONException e1) {
					Logger.e(TAG, "Failed to parse loadout answer to JSON", e1);
					return RESULT.INTERNALSERVERERROR;
				}

				/*
				 * try{
				 * Logger.i(TAG,"Current Loadout: "+currentLoadout.substring
				 * (0,500)+"!Ende");
				 * Logger.i(TAG,"Current Loadout Part 2: "+currentLoadout
				 * .substring(500,1000)+"!Ende");
				 * Logger.i(TAG,"Current Loadout Part 3:"
				 * +currentLoadout.substring(1000)+"!Ende");//TODO Remove if
				 * fully working } catch(StringIndexOutOfBoundsException e){
				 * 
				 * }
				 */
				try {
					JSONObject finishedLoadout = new JSONObject();
					if (loadout.containsKits()) {
						finishedLoadout.put(Constants.BJSON_KITS, kits);
					}
					if (loadout.containsVehicle()) {
						finishedLoadout.put(Constants.BJSON_VEHICLES, vehicles);
					}
					if (loadout.containsWeapons()) {
						finishedLoadout.put(Constants.BJSON_WEAPONS, weapons);
					}

					loadout.setLoadout(finishedLoadout);
				} catch (JSONException e) {
					Logger.e(TAG, "Failed to add parts to finished Loadout", e);
					return RESULT.FAILEDTOSAVE;
				}

				LoadoutManager.getInstance().queryLoadout(loadout);

				/*
				 * Temporally disabled
				 * if(pref.getBoolean(Constants.ANALYSE_LOADOUT_KEY,false)){
				 * Analyzer.analyzeLoadout(loadout); }
				 */
				lastFullLoadout = currentLoadout;
				return RESULT.OK;

			} else {
				Logger.w(TAG, "Loadout Request Failed with ReasonPhrase: "
						+ response.getStatusLine().getReasonPhrase());
				return RESULT.REQUESTFAILED;
			}

		} catch (UnsupportedEncodingException e) {
			Logger.e(TAG, "Failed to save Current Loadout", e);
		}

		catch (ClientProtocolException e) {
			Logger.e(TAG, "Failed to save Current Loadout", e);
		} catch (IOException e) {
			Logger.e(TAG, "Failed to save Current Loadout", e);
		}

		return RESULT.REQUESTFAILED;

	}

	public synchronized int sendLoadout(String loadout) {

		// Check login
		int loginResult = checkLogin();
		if (loginResult != RESULT.OK) {
			Logger.w(TAG, "Login failed with result: " + loginResult);
			return loginResult;
		}

		if (sessionKey == null || sessionKey.equals("")) {
			return RESULT.NOSESSIONKEY;
		}
		if (personaName == null || personaName.equals("")) {
			return RESULT.NOUSERNAME;
		}
		if (personaId == null || sessionKey.equals("")) {
			return RESULT.NOPERSONAID;
		}

		Long tsLong = System.currentTimeMillis() / 1000;

		// Create Http-Post request
		HttpPost httppost = new HttpPost(Constants.SAVELOADOUT_URL);
		HttpResponse response;

		try {
			List<NameValuePair> paare = new ArrayList<NameValuePair>(5); // Post-Parameter
			paare.add(new BasicNameValuePair("personaId", personaId));

			paare.add(new BasicNameValuePair("platformInt", "" + platform));
			paare.add(new BasicNameValuePair("loadout", loadout));
			paare.add(new BasicNameValuePair("timestamp", tsLong.toString()));
			httppost.setEntity(new UrlEncodedFormEntity(paare));
			httppost.addHeader("X-Session-Id", sessionKey);

			// -----------------------------------------------

			response = httpclient.execute(httppost);

		} catch (ClientProtocolException e) {

			Logger.e(TAG, "Error while sending Loadout", e);
			return RESULT.REQUESTFAILED;
		} catch (IOException e) {

			Logger.e(TAG, "Error while sending Loadout", e);
			return RESULT.REQUESTFAILED;
		}
		if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
			ByteArrayOutputStream out = new ByteArrayOutputStream();
			String responseString;
			try {
				response.getEntity().writeTo(out);
				out.close();
				responseString = out.toString();
			} catch (IOException e) {
				Logger.e(TAG, "Error while reading response", e);
				responseString = "";

			}

			Logger.i(TAG, "SendLoadout responseString: " + responseString);

			if (!responseString.contains("success\":1")) {
				// Anwser with no success;
				return RESULT.REQUESTFAILED;
			}
		} else {
			Logger.w(TAG, "Loadout Sending failed with ReasonPhrase: "
					+ response.getStatusLine().getReasonPhrase());
			return RESULT.REQUESTFAILED;
		}
		return RESULT.OK;

	}

	public void setConnectionListener(IConnectionListener l) {
		conListener = l;
	}

	
	public void setPreferences(SharedPreferences pref) {
		this.pref = pref;
	}
	
	public String getPersonaName(){
		if(personaName==null||personaName.equals("")){
			return "Unknown";
		}
		return personaName;
	}

}
