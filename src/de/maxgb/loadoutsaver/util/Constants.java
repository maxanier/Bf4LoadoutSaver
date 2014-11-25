package de.maxgb.loadoutsaver.util;

import android.os.Environment;
import de.maxgb.android.util.InfoBox.Instruction;

public class Constants {
	public static String getPlatformFromInt(int platform) {
		switch (platform) {
		case 1:
			return "PC";
		case 2:
			return "XBox-360";
		case 4:
			return "PS3";
		case 32:
			return "PS4";
		case 64:
			return "XBox-One";
		default:
			return "unknown";

		}
	}
	// Connection Parameter
	public final static int CONNECTION_TIMEOUT = 10000;

	public final static int LOGIN_TIMEOUT = 1000000;
	// Setting keys
	public final static String PREF_NAME = "settings";
	public final static String INFO_BOX_PREF_NAME = "infobox";
	public final static String EMAIL_KEY = "email";
	public final static String PASSWORD_KEY = "password";
	public final static String ANALYSE_LOADOUT_KEY = "analyse_loadout";
	public final static String KEEP_SCREEN_ON_KEY = "keep_screen_on";
	public final static String MIX_LOADOUTS_KEY = "mix_loadouts";
	public final static String MOBILE_TOKEN_KEY = "mobile_token";

	public final static String USER_ID = "user_id";
	// URLs
	public final static String LOGIN_URL = "https://battlelog.battlefield.com/mobile/gettoken";
	public final static String TOKEN_CHALLENGE_URL = "https://battlelog.battlefield.com/mobile/tokenchallenge";
	public final static String FINISH_TOKEN_CHALLENGE_URL = "https://battlelog.battlefield.com/mobile/gettoken";
	public final static String GET_AUTHCODE_URL = "https://battlelog.battlefield.com/mobile/getauthcode";
	public final static String GETLOADOUT_URL = "https://battlelog.battlefield.com/bf4/mobile/getloadout";

	public final static String SAVELOADOUT_URL = "https://battlelog.battlefield.com/bf4/mobile/saveloadout";
	// Directorys and files
	public static final String DIRECTORY = Environment
			.getExternalStorageDirectory().getPath() + "/bf4Loadout/";
	public static final String LOADOUT_FILE_NAME = "loadout.json";
	public static final String LOADOUT_OLD_FILE_NAME = "loadout";

	public static final String LOADOUT_SEPERATOR = "!";
	// Loadout types
	public static final int ALL_TYPE = 0;
	public static final int INFANTRY_TYPE = 1;

	public static final int VEHICLE_TYPE = 2;
	// etc.
	public static final int TOAST_DURATION = 3;
	public static final String LOG_REPORT_EMAIL = "app@maxgb.de";

	public static final String LOG_REPORT_SUBJECT = "Error Report: Loadoutsaver version: ";
	// Battlelog JSON Names
	public static final String BJSON_WEAPONS = "weapons";
	public static final String BJSON_VEHICLES = "vehicles";
	public static final String BJSON_KITS = "kits";
	public static final String BJSON_CURRENT_LOADOUT = "currentLoadout";
	public static final String BJSON_DATA = "data";
	public static final String BJSON_PERSONAID = "personaId";
	public static final String BJSON_PERSONANAME = "personaName";
	public static final String BJSON_PLATFORMINT = "platformInt";
	public static final String BJSON_GAMES = "games";

	public static final String BJSON_SESSIONKEY = "sessionKey";
	// Instructions
	public static final Instruction INSTRUCTION_MAIN = new Instruction(
			"Loadout Saver",
			28,
			"<html><body>1. Use the +-Button to save your currently equipped Loadout and choose a name  for it and select which parts of the Loadout should be saved<br>2. Click on a Loadout to load it and watch it happen on Battlelog or in game<br>4. Long click on a Loadout to remove it.<br><b>Important: You probably need to be playing BF4, when trying to send or save a loadout!</b><p>If you have any questions please use the feedback dialog in the settings menu</body></html");
	public static final Instruction INSTRUCTION_BETA = new Instruction(
			"Loadout Saver",
			28,
			"<html><body><b>Important notice</b><br>I can't confirm that this app works for console players.<br><b>Important: You probably need to be playing BF4, when trying to send or save a loadout!</b><br>If it does thats great, if not, please don´t rate this app bad, but report the problem, so I can make it work<br>Thanks");
	public static final Instruction INSTRUCTION_LOGIN = new Instruction(
			"Login",
			28,
			"<html><body><b>Login</b><br>You need to login to battlelog to perform that action.<br>You can either enter your <i>battlelog login credentials</i> (email and password)<br>or use the <i>QR code</i> from ingame Battlelog. For the latter press the \"<-\"(backspace) while being on the spawn screen, in the lower right corner you can continue to the qr code</body></html>");

	public static boolean GA_DRY_RUN = false;

}
