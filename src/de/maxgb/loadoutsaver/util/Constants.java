package de.maxgb.loadoutsaver.util;

import android.os.Environment;

public class Constants {
	//Connection Parameter
	public final static int CONNECTION_TIMEOUT = 10000;
	public final static int LOGIN_TIMEOUT=100000;
	
	//Setting keys
	public final static String PREF_NAME= "settings";
	public final static String INFO_BOX_PREF_NAME="infobox";
	public final static String EMAIL_KEY= "email";
	public final static String PASSWORD_KEY= "password";
	public final static String ANALYSE_LOADOUT_KEY="analyse_loadout";
	public final static String KEEP_SCREEN_ON_KEY="keep_screen_on";
	
	//URLs
	public final static String LOGIN_URL="https://battlelog.battlefield.com/mobile/gettoken";
	public final static String GETLOADOUT_URL="https://battlelog.battlefield.com/bf4/mobile/getloadout";
	public final static String SAVELOADOUT_URL="https://battlelog.battlefield.com/bf4/mobile/saveloadout";
	
	
	//Directorys and files
	public static final String DIRECTORY = Environment.getExternalStorageDirectory().getPath()+"/bf4Loadout/";
	public static final String LOADOUT_FILE_NAME="loadout.txt";
	public static final String LOADOUT_SEPERATOR="!";
	
	//Loadout types
	public static final int ALL_TYPE=0;
	public static final int INFANTRY_TYPE=1;
	public static final int VEHICLE_TYPE=2;
	
	//etc.
	public static final int TOAST_DURATION=3;
	public static final String LOG_REPORT_EMAIL="app@maxgb.de";
	public static final String LOG_REPORT_SUBJECT="Error Report: Loadoutsaver version: ";
	

	
}
