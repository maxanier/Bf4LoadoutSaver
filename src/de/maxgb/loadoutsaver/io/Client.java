package de.maxgb.loadoutsaver.io;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
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

import de.maxgb.android.util.Logger;
import de.maxgb.loadoutsaver.LoadoutMainActivity.IPersonaListener;
import de.maxgb.loadoutsaver.util.Constants;
import de.maxgb.loadoutsaver.util.Loadout;
import de.maxgb.loadoutsaver.util.RESULT;
import de.maxgb.loadoutsaver.util.UnexpectedStuffException;
import de.maxgb.loadoutsaver.util.UnexpectedStuffException.Location;

public class Client implements IPersonaListener {
	public interface IConnectionListener {

		public void choosePersona(ArrayList<Persona> personas,
				IPersonaListener listener);

		public void enterCode(String code, String token,boolean again);
		

		public void loggedIn(Persona persona, String mobileToken, String userId);
	}
	public class Persona {
		public String personaName;
		public String personaId;
		public int platform;

		@Override
		public String toString() {
			return "N: " + personaName + " Id: " + personaId + " Pl: "
					+ platform;
		}

	}

	public static synchronized Client getInstance() {
		if (instance == null) {
			instance = new Client();
		}
		return instance;
	}

	private final String TAG = "Client";
	private static Client instance;
	HttpClient httpclient;
	private long loggedInSince = 0;

	private String sessionKey = "";

	private Persona persona = null;

	private JSONObject lastFullLoadout;

	private IConnectionListener conListener;

	private Client() {

		final HttpParams httpParams = new BasicHttpParams();
		HttpConnectionParams.setConnectionTimeout(httpParams,
				Constants.CONNECTION_TIMEOUT);
		HttpConnectionParams.setSoTimeout(httpParams,
				Constants.CONNECTION_TIMEOUT);
		httpclient = new DefaultHttpClient(httpParams);
		lastFullLoadout = null;
	}

	@Override
	public void choosenPersona(Persona persona) {
		synchronized (persona) {
			this.persona = persona;
		}

	}

	/**
	 * Executes a standard HttpPostRequest, always includes devicetype and
	 * timestamp
	 * 
	 * @param url
	 *            Url request is send to.
	 * @param sessionkey
	 *            SessionKey, null if not available yet
	 * @param params
	 *            Parameter pairs, list of key, value
	 * @return HttpResponse
	 * @throws ClientProtocolException
	 * @throws IOException
	 */
	private HttpResponse executePostRequest(String url, String sessionkey,
			String... params) throws ClientProtocolException, IOException {
		if (params.length % 2 != 0) {
			Logger.e(TAG, "Post request params have to be a even count");
			return null;
		}

		Long tsLong = System.currentTimeMillis() / 1000;

		// Create Http-Post request
		HttpPost httppost = new HttpPost(url);

		if (sessionkey != null) {
			httppost.addHeader("X-Session-Id", sessionkey);
		}

		List<NameValuePair> paare = new ArrayList<NameValuePair>(
				params.length / 2 + 2);
		for (int i = 0; i < params.length; i += 2) {
			paare.add(new BasicNameValuePair(params[i], params[i + 1]));
		}
		paare.add(new BasicNameValuePair("deviceType", "1"));
		paare.add(new BasicNameValuePair("timestamp", tsLong.toString()));
		httppost.setEntity(new UrlEncodedFormEntity(paare));
		// -----------------------------------------------
		return httpclient.execute(httppost);
	}

	public JSONObject getLastFullLoadout() {

		return lastFullLoadout;
	}

	public long getLoggedInSince() {
		return loggedInSince;
	}

	public String getPersonaId() {
		if (persona == null) {
			return "";
		}
		return persona.personaId;
	}

	public String getPersonaName() {
		if (persona == null || persona.personaName.equals("")) {
			return "Unknown";
		}
		return persona.personaName;
	}

	public boolean isLoggedIn() {
		if (System.currentTimeMillis() - getLoggedInSince() > Constants.LOGIN_TIMEOUT
				|| persona == null || sessionKey.equals("")) {
			return false;
		}
		return true;
		// TODO make a real check
	}

	public synchronized int login(String email, String password)
			throws UnexpectedStuffException {

		String responseString = null;
		try {
			Logger.i(TAG, "Loginvorgang gestartet");
			HttpResponse response = executePostRequest(Constants.LOGIN_URL,
					null, "email", email, "password", password);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				responseString = out.toString();
				// Test String for multiple personas: responseString =
				// "{\"data\":{\"pushToken\":\"1404492198;5eab7a96ed028e4d61b7b1b2f5c1b1d018cac385\",\"rollouts\":[\"LIVE_SCOREBOARD\",\"SERVERBANNER_UPLOAD\",\"ESPORT_MATCHES\",\"ESPORT_MATCHES_PC\",\"SERVERBANNER_UPLOAD_PS3\",\"ESPORT_MATCHES_PS3\",\"SERVERBANNER_UPLOAD_XBOX\",\"ESPORT_MATCHES_XBOX\",\"BF3LOADOUT\",\"CLUB_EMBLEMS\",\"WARSAW_RESET_STATS\",\"USERNPS\",\"APP_PROMOTION\",\"BFH_COMMUNITY_MISSIONS\",\"BFH_MOBILE\"],\"userGameExpansions\":[],\"personas\":[{\"picture\":\"\",\"userId\":\"2955057794699152819\",\"user\":null,\"updatedAt\":1403815068,\"firstPartyId\":\"\",\"personaId\":\"376180755\",\"personaName\":\"BlueTig3r131\",\"gamesLegacy\":\"0\",\"namespace\":\"ps3\",\"gamesJson\":\"{\\\"32\\\":\\\"10240\\\",\\\"4\\\":\\\"0\\\"}\",\"games\":{\"32\":\"10240\",\"4\":\"0\"},\"clanTag\":\"\"},{\"picture\":\"\",\"userId\":\"2955057794699152819\",\"user\":null,\"updatedAt\":1403815068,\"firstPartyId\":\"\",\"personaId\":\"1075332762\",\"personaName\":\"Into_The_World13\",\"gamesLegacy\":\"0\",\"namespace\":\"ps3\",\"gamesJson\":\"{\\\"32\\\":\\\"10240\\\",\\\"4\\\":\\\"0\\\"}\",\"games\":{\"32\":\"10240\",\"4\":\"0\"},\"clanTag\":\"\"},{\"picture\":\"\",\"userId\":\"2955057794699152819\",\"user\":null,\"updatedAt\":1403208816,\"firstPartyId\":\"\",\"personaId\":\"1075338761\",\"personaName\":\"blackoutidk\",\"gamesLegacy\":\"0\",\"namespace\":\"cem_ea_id\",\"gamesJson\":\"{\\\"1\\\":\\\"0\\\"}\",\"games\":{\"1\":\"0\"},\"clanTag\":\"\"}],\"clientId\":null,\"activePersonas\":{\"8192\":{\"platform\":32,\"game\":8192,\"persona\":{\"picture\":\"\",\"userId\":\"2955057794699152819\",\"user\":null,\"updatedAt\":1403815068,\"firstPartyId\":\"\",\"personaId\":\"1075332762\",\"personaName\":\"Into_The_World13\",\"gamesLegacy\":\"0\",\"namespace\":\"ps3\",\"gamesJson\":\"{\\\"32\\\":\\\"10240\\\",\\\"4\\\":\\\"0\\\"}\",\"games\":{\"32\":10240,\"4\":0},\"clanTag\":\"\"},\"userId\":\"2955057794699152819\",\"personaId\":\"1075332762\"},\"2\":null},\"isOmahaUser\":true,\"sessionKey\":\"palst53rr6upouysy9wrhavfr54wq6wk\",\"mobileToken\":\"PApQio3PetqeUPe-H19MDhLxkcm06_505MJMHxfl2yqSMxuTI278YMsghEadgSKFMRTqdk4hRc6vVt6nXzsVksIbHNNe8JnioZ3ucuxZ-YiLJe7Yf0I5Bmkpy37MnU4kf1ywy4n2FSMrxQc2H2Dh_yG1WKAoEfzG_WZUDw0GPYU.\",\"isWarsawPremiumUser\":false,\"isWarsawUser\":true,\"user\":{\"username\":\"blackoutidk\",\"gravatarMd5\":\"7371dc42d2d9731feca519e305f89678\",\"userId\":\"2955057794699152819\",\"createdAt\":1403208815,\"presence\":{\"onlineGame\":{\"platform\":32,\"game\":2048,\"personaId\":\"1075332762\"},\"userId\":\"2955057794699152819\",\"playingMp\":{\"serverGuid\":\"3a8bfb53-b838-484a-8aec-99d32d4a836e\",\"platform\":32,\"personaId\":\"1075332762\",\"gameId\":\"720575940390419858\",\"role\":1,\"gameExpansions\":[0],\"serverName\":\"-[DICE]- BF4 TDM - Normal 140286\",\"gameMode\":\"32\",\"game\":2048,\"levelName\":\"MP_Naval\"},\"updatedAt\":1404448961,\"isPlaying\":true,\"presenceStates\":\"266\",\"isOnline\":true}}},\"success\":1}";
				// Logger.i(TAG, "Response String: " +
				// replacePersonalInfo(responseString));

				JSONObject responseJson = new JSONObject(responseString);

				if (responseJson.getInt("success") == 0) {
					String error = null;

					if (responseJson.has("error")) {
						error = responseJson.getString("error");
					} else {
						error = responseJson.toString();
					}

					Logger.e(TAG, "Login request was not succesfull: " + error);
					if (error.contains("USER_CREDENTIALS_ERROR")) {
						return RESULT.LOGINCREDENTIALSERROR;
					}
					return RESULT.REQUESTFAILED;
				}
				JSONObject data = responseJson.getJSONObject("data");

				return processLogin(data);

			} else {
				Logger.w(TAG, "Logging http request not ok: "
						+ response.getStatusLine().getStatusCode() + ":"
						+ response.getStatusLine().getReasonPhrase());
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
					return RESULT.INTERNALSERVERERROR;
				}
				return RESULT.CONNECTING_PROBLEM;
			}

		} catch (SocketTimeoutException e) {
			Logger.w(TAG, "Timeout during login");
			return RESULT.TIMEOUT;
		} catch (IOException e) {
			Logger.e(TAG, "IOException during login", e);
			return RESULT.CONNECTING_PROBLEM;
		} catch (JSONException e) {
			Logger.e(TAG, "Failed to parse response to JSON", e);
			if (responseString != null) {
				Logger.i(TAG, "Response: " + responseString);
			}
			throw new UnexpectedStuffException(
					"Response json structure unlike expected", Location.LOGIN);
		}

	}

	/**
	 * Initiates the qr login procedure. Retrieves the challenge code from
	 * battlelog
	 * 
	 * @param qrtoken
	 *            The token (string at the end of the QR url)
	 * @return Result id
	 * @throws UnexpectedStuffException
	 */
	public synchronized int loginQR(String qrtoken)
			throws UnexpectedStuffException {

		String responseString = null;
		try {

			Logger.i(TAG, "QR Loginvorgang gestartet");
			HttpResponse response = executePostRequest(
					Constants.TOKEN_CHALLENGE_URL, null, "token", qrtoken);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				responseString = out.toString();
				Logger.i(TAG, "TOKEN challenge answer: " + responseString); // TODO
																			// remove
				JSONObject responseJson = new JSONObject(responseString);

				if (responseJson.getInt("success") == 0) {
					String error = null;

					if (responseJson.has("error")) {
						error = responseJson.getString("error");
					} else {
						error = responseJson.toString();
					}

					Logger.e(TAG, "Login request was not succesfull: " + error);
					return RESULT.REQUESTFAILED;
				}

				String code = responseJson.getJSONObject("data").getString(
						"challenge");
				conListener.enterCode(code, qrtoken,false);
				return RESULT.OK;

			} else {
				Logger.w(TAG, "Logging http request not ok: "
						+ response.getStatusLine().getStatusCode() + ":"
						+ response.getStatusLine().getReasonPhrase());
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
					return RESULT.INTERNALSERVERERROR;
				}
				return RESULT.CONNECTING_PROBLEM;
			}
		} catch (IOException e) {
			Logger.e(TAG, "IOException during login", e);
			return RESULT.CONNECTING_PROBLEM;
		} catch (JSONException e) {
			Logger.e(TAG, "Failed to parse response to JSON", e);
			if (responseString != null) {
				Logger.i(TAG, "Response: " + responseString);
			}
			throw new UnexpectedStuffException(
					"Response json structure unlike expected", Location.LOGIN);
		}
	}

	public synchronized int loginTokenChallenge(String token, String code)
			throws UnexpectedStuffException {

		String responseString = null;
		String authCode = null;
		String responseString2 = null;

		try {
			HttpResponse response = executePostRequest(
					Constants.GET_AUTHCODE_URL, null, "token", token,
					"challenge", code);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				responseString = out.toString();
				Logger.i(TAG, "AuthCode answer: " + responseString); // TODO
																		// remove
				JSONObject responseJson = new JSONObject(responseString);
				if (responseJson.getInt("success") == 1) {
					authCode = responseJson.getJSONObject("data").getString(
							"authorizationCode");
					Logger.i(TAG, "Retrieved auth code: " + authCode); // TODO
																		// remove
				} else {
					String error = null;
					try {
						error = responseJson.getString("error");
					} catch (JSONException e) {
						error = responseJson.toString();
					}
					Logger.e(TAG, "Get auth code request was not successfull: "
							+ error);
					
					if(error.equals("NOT_CONFIRMED")){
						conListener.enterCode(authCode, token,true);
						return RESULT.OK;
					}
					return RESULT.REQUESTFAILED;
				}
			} else {
				Logger.w(TAG, "Get authcode http request not ok: "
						+ response.getStatusLine().getStatusCode() + ":"
						+ response.getStatusLine().getReasonPhrase());
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
					return RESULT.INTERNALSERVERERROR;
				}
				return RESULT.CONNECTING_PROBLEM;
			}
		} catch (IOException e) {
			Logger.e(TAG, "IOException during login", e);
			return RESULT.CONNECTING_PROBLEM;
		} catch (JSONException e) {
			Logger.e(TAG, "Failed to parse response to JSON", e);
			if (responseString != null) {
				Logger.i(TAG, "Response: " + responseString);
			}
			throw new UnexpectedStuffException(
					"Response json structure unlike expected", Location.LOGIN);
		}

		try {
			HttpResponse response = executePostRequest(
					Constants.FINISH_TOKEN_CHALLENGE_URL, null,
					"authorizationCode", authCode);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				responseString2 = out.toString();
				Logger.i(TAG, "Response String: " + responseString2); // TODO
																		// remove

				JSONObject responseJson = new JSONObject(responseString2);

				if (responseJson.getInt("success") == 0) {
					String error = null;

					if (responseJson.has("error")) {
						error = responseJson.getString("error");
					} else {
						error = responseJson.toString();
					}

					Logger.e(TAG,
							"Finsh Token challenge request was not succesfull: "
									+ error);
					return RESULT.REQUESTFAILED;
				}

				JSONObject data = responseJson.getJSONObject("data");

				return processLogin(data);
			} else {
				Logger.w(TAG, "FinishTokenChallenge http request not ok: "
						+ response.getStatusLine().getStatusCode() + ":"
						+ response.getStatusLine().getReasonPhrase());
				return RESULT.CONNECTING_PROBLEM;
			}
		} catch (IOException e) {
			Logger.e(TAG, "IOException during login", e);
			return RESULT.CONNECTING_PROBLEM;
		} catch (JSONException e) {
			Logger.e(TAG, "Failed to parse response to JSON", e);
			if (responseString2 != null) {
				Logger.i(TAG, "Response: " + responseString2);
			}
			throw new UnexpectedStuffException(
					"Response json structure unlike expected", Location.LOGIN);
		}
	}

	private int processLogin(JSONObject data) throws UnexpectedStuffException {
		// Read out
		// values------------------------------------------------------------------
		sessionKey = null;
		persona = null;
		String mobileToken = null;
		String userId = null;
		ArrayList<Persona> personas = new ArrayList<Persona>();

		try {
			if (data != null) {

				try {
					sessionKey = data.getString(Constants.BJSON_SESSIONKEY);
				} catch (JSONException e) {

				}

				JSONArray ps = data.getJSONArray("personas");
				for (int i = 0; i < ps.length(); i++) {
					JSONObject p = ps.getJSONObject(i);

					JSONObject games = p.getJSONObject(Constants.BJSON_GAMES);
					for (int j = 0; j < games.names().length(); j++) {
						Logger.i(
								TAG,
								"Owns game "
										+ games.getString(games.names()
												.getString(j)) + " on "
										+ games.names().getString(j));
						if (!games.getString(games.names().getString(j))
								.equals("0")) {
							Persona pers = new Persona();
							pers.personaName = p
									.getString(Constants.BJSON_PERSONANAME);
							pers.personaId = p
									.getString(Constants.BJSON_PERSONAID);
							pers.platform = Integer.parseInt(games.names()
									.getString(j));
							personas.add(pers);
						}
					}

				}

				try {
					mobileToken = data.getString("mobileToken");
					userId = data.getJSONObject("user").getString("userId");
				} catch (JSONException e) {
					Logger.e(TAG, "Failed to read out mobile token or userId",
							e);
				}

			} else {
				throw new JSONException("Data json null");
			}

		} catch (JSONException e) {
			Logger.e(TAG, "Failed to parse login to JSON", e);
			throw new UnexpectedStuffException("Cant parse data",
					Location.LOGIN);
		}

		if (sessionKey == null) {
			throw new UnexpectedStuffException("Cant find session key",
					Location.LOGIN);
		}

		Logger.i(TAG, "Login analysis complete: SessionKey: cencored,length:"
				+ sessionKey.length() + ". Found " + personas.size()
				+ " personas: " + personas.toString());

		if (personas.size() == 0) {
			return RESULT.NOPERSONA;
		}
		loggedInSince = System.currentTimeMillis();

		if (personas.size() == 1) {
			persona = personas.get(0);
			Logger.i(TAG, "Only one persona, so skipping choosing part");

		} else {
			Logger.i(TAG, "Multiple personas, asking user to choose");
			conListener.choosePersona(personas, this);
			while (persona == null) {
				Thread.yield();
			}
		}
		if (conListener != null)
			conListener.loggedIn(persona, mobileToken, userId);
		return RESULT.OK;
	}

	public synchronized int relogin(String mobileToken, String userId)
			throws UnexpectedStuffException {
		String responseString = null;
		try {

			Logger.i(TAG, "Re Loginvorgang gestartet");
			HttpResponse response = executePostRequest(Constants.LOGIN_URL,
					null, "mobileToken", mobileToken, "userId", userId);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {

				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				responseString = out.toString();
				Logger.i(TAG, "RElogin answer: " + responseString); // TODO
																	// remove
				JSONObject responseJson = new JSONObject(responseString);

				if (responseJson.getInt("success") == 0) {
					Logger.w(TAG, "Relogin failed: " + responseString);
					return RESULT.REQUESTFAILED;
				} else {
					return processLogin(responseJson.getJSONObject("data"));
				}
			} else {
				Logger.w(TAG, "Logging http request not ok: "
						+ response.getStatusLine().getStatusCode() + ":"
						+ response.getStatusLine().getReasonPhrase());
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
					return RESULT.INTERNALSERVERERROR;
				}
				return RESULT.CONNECTING_PROBLEM;
			}
		} catch (IOException e) {
			Logger.e(TAG, "IOException during relogin", e);
			return RESULT.CONNECTING_PROBLEM;
		} catch (JSONException e) {
			Logger.e(TAG, "Failed to parse response to JSON", e);
			if (responseString != null) {
				Logger.i(TAG, "Response: " + responseString);
			}
			throw new UnexpectedStuffException(
					"Response json structure unlike expected", Location.LOGIN);
		}
	}

	public void resetLogin() {
		loggedInSince = 0;
		persona = null;
		sessionKey = null;
	}

	/**
	 * Downloads the currently equipped Loadout and querys it in LoadoutManager.
	 * You need to call LoadoutManager.addQuery() in UI-Thread afterwards Also
	 * logs into Battlelog if neccessary.
	 * 
	 * @param loadout
	 *            Empty Loadout of the type which should be saved
	 * @return Errorcode
	 * @throws UnexpectedStuffException
	 */
	public synchronized int saveCurrentLoadout(Loadout loadout)
			throws UnexpectedStuffException {

		if (sessionKey == null || sessionKey.equals("")) {
			throw new UnexpectedStuffException("Session key is null or empty",
					Location.SAVING);
		}

		try {
			HttpResponse response = executePostRequest(
					Constants.GETLOADOUT_URL, sessionKey, "personaId",
					persona.personaId, "personaName", persona.personaName,
					"platformInt", "" + persona.platform);
			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				response.getEntity().writeTo(out);
				out.close();
				String responseString = out.toString();
				String debugShortResponse;
				try {
					debugShortResponse = responseString.substring(0, 300);
				} catch (Exception e1) {
					debugShortResponse = responseString;
				}

				Logger.i(TAG, "GetLoadout short responseString: "
						+ debugShortResponse); // TODO Remove if fully working

				JSONObject currentLoadout = null;
				JSONArray vehicles = null;
				JSONObject weapons = null;
				JSONArray kits = null;

				try {
					JSONObject responseJson = new JSONObject(responseString);

					if (responseJson.getInt("success") == 0) {
						Logger.e(TAG, "Get Loadout request not successfull\n"
								+ responseJson.toString());
						if (responseJson.has("error")) {
							if (responseJson.getString("error").equals(
									"SESSION_NOT_FOUND")) {
								// Session probably expired
								Logger.w(TAG, "Session is probably expired");
								loggedInSince = 0;
								return RESULT.SESSION_EXPIRED;
							} else if (responseJson.getString("error").equals(
									"nostats")) {
								// Soldier does not exist or has not played yet
								return RESULT.NOSTATS;
							}
						}

						return RESULT.REQUESTFAILED;
					}

					JSONObject data = responseJson
							.getJSONObject(Constants.BJSON_DATA);

					try {
						Logger.i(
								TAG,
								"Player information: ID:"
										+ data.getString(Constants.BJSON_PERSONAID)
										+ " Name: "
										+ data.getString(Constants.BJSON_PERSONANAME)
										+ " Platform: "
										+ data.getString(Constants.BJSON_PLATFORMINT));

						if (data.getBoolean("mySoldier")) {
							Logger.w(TAG, "Player doesnt belong to user");
						}
					} catch (JSONException e) {
						Logger.e(TAG, "Failed to get player informations", e);
					}

					if (data.has("error")
							&& data.getString("error").equals("nostats")) {
						// Soldier does not exist or has not played yet
						return RESULT.NOSTATS;
					}

					currentLoadout = data
							.getJSONObject(Constants.BJSON_CURRENT_LOADOUT);
					vehicles = currentLoadout
							.getJSONArray(Constants.BJSON_VEHICLES);
					weapons = currentLoadout
							.getJSONObject(Constants.BJSON_WEAPONS);
					kits = currentLoadout.getJSONArray(Constants.BJSON_KITS);

				} catch (JSONException e1) {
					Logger.e(TAG, "Failed to parse loadout answer to JSON", e1);
					Logger.e(TAG, "JSON:\n" + responseString);
					return RESULT.INTERNALSERVERERROR;
				}
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

					loadout.setPersonaId(persona.personaId);
					loadout.setLoadout(finishedLoadout);
				} catch (JSONException e) {
					Logger.e(TAG, "Failed to add parts to finished Loadout", e);
					return RESULT.OTHERERROR;
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
				Logger.w(TAG, "Get Loadout http request not ok: "
						+ response.getStatusLine().getStatusCode() + ":"
						+ response.getStatusLine().getReasonPhrase());
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
					return RESULT.INTERNALSERVERERROR;
				}
				return RESULT.CONNECTING_PROBLEM;
			}

		} catch (IOException e) {
			Logger.e(TAG, "IOException while getting Loadout", e);
			return RESULT.CONNECTING_PROBLEM;
		}

	}

	public synchronized int sendLoadout(String loadout, String id)
			throws UnexpectedStuffException {

		if (sessionKey == null || sessionKey.equals("")) {
			throw new UnexpectedStuffException("Session key is null or empty",
					Location.SENDING);
		}

		if (!id.equals("")) {
			if (!id.equals(persona.personaId)) {
				Logger.w(TAG, "Trying to mix Loadouts");
				return RESULT.MIXING_LOADOUTS;
			}
		}
		try {
			HttpResponse response = executePostRequest(
					Constants.SAVELOADOUT_URL, sessionKey, "personaId",
					persona.personaId, "personaName", persona.personaName,
					"platformInt", "" + persona.platform, "loadout", loadout);

			if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
				ByteArrayOutputStream out = new ByteArrayOutputStream();
				String responseString;
				response.getEntity().writeTo(out);
				out.close();
				responseString = out.toString();

				JSONObject responseJson;
				try {
					responseJson = new JSONObject(responseString);
					if (responseJson.getInt("success") == 0) {
						Logger.w(TAG, "Repsonse without success: "
								+ responseString);
						if (responseJson.has("error")) {
							if (responseJson.getString("error").equals(
									"SESSION_NOT_FOUND")) {
								// Session probably expired
								Logger.w(TAG, "Session is probably expired");
								loggedInSince = 0;
								return RESULT.SESSION_EXPIRED;
							} else if (responseJson.getString("error").equals(
									"nostats")) {
								// Soldier does not exist or has not played yet
								return RESULT.NOSTATS;
							}
						}

						return RESULT.REQUESTFAILED;
					}
					return RESULT.OK;
				} catch (JSONException e) {
					Logger.e(TAG, "Failed to parse response to JSON", e);
					if (responseString != null) {
						Logger.i(TAG, "Response: " + responseString);
					}
					throw new UnexpectedStuffException(
							"Response json structure unlike expected",
							Location.LOGIN);
				}

			} else {
				Logger.w(TAG, "Send Loadout http request not ok: "
						+ response.getStatusLine().getStatusCode() + ":"
						+ response.getStatusLine().getReasonPhrase());
				if (response.getStatusLine().getStatusCode() == HttpStatus.SC_INTERNAL_SERVER_ERROR) {
					return RESULT.INTERNALSERVERERROR;
				}
				return RESULT.CONNECTING_PROBLEM;
			}
		} catch (IOException e) {

			Logger.e(TAG, "IOException while sending loadout", e);
			return RESULT.CONNECTING_PROBLEM;
		}

	}

	public void setConnectionListener(IConnectionListener l) {
		conListener = l;
	}

}
