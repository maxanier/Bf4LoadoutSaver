package de.maxgb.loadoutsaver.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.net.Uri;
import de.maxgb.android.util.Logger;
import de.maxgb.loadoutsaver.util.Constants;
import de.maxgb.loadoutsaver.util.Loadout;

public class LoadoutManager {
	/*
	 * Save Format: <name>!<weapons>!<kits>!<weapons>!<loadoutString> linebreak
	 * Types are either 0 for false or 1 for true
	 */
	private static LoadoutManager instance;
	private final static String TAG = "LoadoutManager";

	public static synchronized LoadoutManager getInstance() {
		if (instance == null) {
			instance = new LoadoutManager();
		}
		return instance;
	}

	private ArrayList<Loadout> loadout;
	private ArrayList<Loadout> query;

	private final String CURRENTVERSION = "2";

	private LoadoutManager() {
		loadout = readLoadout();
		query = new ArrayList<Loadout>();
	}

	/**
	 * Adds a Loadout to the ArrayList and writes it to the file If there
	 * already is a Loadout with that name, the old one is deleted and the new
	 * one is appended
	 * 
	 * @param temp
	 *            Loadout which should be add
	 * @return true if succesfully
	 */
	public boolean addLoadout(Loadout temp) {
		File directory = new File(Constants.DIRECTORY);
		directory.mkdir();

		for (int i = 0; i < loadout.size(); i++) {
			if (loadout.get(i).getName().equals(temp.getName())) {
				removeLoadout(i);
			}
		}

		File loadoutFile = getLoadoutFile();

		try {
			loadoutFile.createNewFile();
			BufferedWriter output = new BufferedWriter(new FileWriter(
					loadoutFile, true));
			output.newLine();
			output.append(temp.toString(Constants.LOADOUT_SEPERATOR));
			output.close();
			loadout.add(temp);

			return true;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			Logger.e(TAG, "Adding Loadout failed", e);
		}
		return false;
	}

	/**
	 * Adds the queried Loadouts
	 */
	public void addQuery() {
		for (int i = 0; i < query.size(); i++) {
			addLoadout(query.get(i));
		}
		query = new ArrayList<Loadout>();
		Logger.i(TAG, "Added queried Loadouts");
	}

	public boolean checkIfOldFileExists() {
		File loadoutFile = new File(Constants.DIRECTORY
				+ Constants.LOADOUT_OLD_FILE_NAME);
		if (loadoutFile.exists()) {
			loadoutFile.delete();
			return true;
		}
		return false;
	}

	public ArrayList<Loadout> getLoadout() {
		return loadout;
	}

	private File getLoadoutFile() {
		File directory = new File(Constants.DIRECTORY);
		directory.mkdir();
		File loadoutFile = new File(Constants.DIRECTORY
				+ Constants.LOADOUT_FILE_NAME);

		try {
			if (loadoutFile.createNewFile()) {
				BufferedWriter output = new BufferedWriter(new FileWriter(
						loadoutFile, false));
				output.append("VERSION:" + CURRENTVERSION);
				output.newLine();
				output.flush();
				output.close();
			}
		} catch (IOException e) {
			Logger.e(TAG,
					"Failed to create a new file and to fill it with version info");
		}

		return loadoutFile;
	}

	public Uri getLoadoutFileUri() {
		File f = getLoadoutFile();
		if (f != null) {
			return Uri.fromFile(f);
		} else
			return null;
	}

	/**
	 * Queries the given Loadout object. Call addQuery() in UI Thread to add the
	 * file to the List and to save it to file.
	 * 
	 * @param temp
	 *            Loadout
	 */
	public void queryLoadout(Loadout temp) {

		query.add(temp);
		Logger.i(TAG, "Queried Loadout: " + temp.getName());
	}

	private ArrayList<Loadout> readLoadout() {
		Logger.i(TAG, "Start reading Loadout");
		ArrayList<Loadout> temp = new ArrayList<Loadout>();
		String version = "0";
		File loadoutFile = new File(Constants.DIRECTORY
				+ Constants.LOADOUT_FILE_NAME);

		if (!loadoutFile.exists()) {
			Logger.w(TAG, "LoadoutFile doesn´t exist");
			return temp;
		}
		if (!loadoutFile.canRead()) {
			Logger.w(TAG, "Can´t read Loadout File");
			return temp;
		}

		try {
			BufferedReader br = new BufferedReader(new FileReader(loadoutFile));

			String line = br.readLine();

			if (line != null && line.startsWith("Version:")) {
				version = line.replace("Version:", "");
				line = br.readLine();
			}

			while (line != null) {
				Loadout l = readLoadout(line);
				if (l != null) {
					temp.add(l);
				}
				line = br.readLine();
			}

		} catch (FileNotFoundException e) {
			Logger.e(
					TAG,
					"FileNotFoundException even though file existence was checked",
					e);
		} catch (IOException e) {
			Logger.e(TAG, "IOException while reading Loadout", e);
		}

		Logger.i(TAG, "Found " + temp.size() + " Loadouts");
		return temp;
	}

	/**
	 * Converts a single loadout line into a new loadout object.
	 * 
	 * @param s
	 * @return
	 */
	private Loadout readLoadout(String s) {
		if (s == null)
			return null;

		try {
			String[] splitted = s.split(Constants.LOADOUT_SEPERATOR);
			if (splitted.length == 5) {
				String name = splitted[0];
				boolean weapons = splitted[1].equals("1");
				boolean kits = splitted[2].equals("1");
				boolean vehicles = splitted[3].equals("1");

				JSONObject loadout = new JSONObject(splitted[4]);
				return new Loadout(name, loadout, weapons, kits, vehicles);
			} else {
				Logger.e(TAG, "Loadoutline did not contain 5 parts");
			}
		} catch (JSONException e) {
			Logger.e(TAG, "Failed to parse saved json loadout string to json");
		}
		return null;
	}

	public void removeLoadout(int position) {
		removeLoadout(loadout.get(position));
	}

	public void removeLoadout(Loadout temp) {
		loadout.remove(temp);
		writeLoadout(loadout);
	}

	private boolean writeLoadout(ArrayList<Loadout> list) {

		File loadoutFile = getLoadoutFile();

		try {
			BufferedWriter output = new BufferedWriter(new FileWriter(
					loadoutFile, false));
			for (int i = 0; i < list.size(); i++) {
				output.newLine();
				output.append(list.get(i).toString(Constants.LOADOUT_SEPERATOR));
			}

			output.close();
			return true;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			Logger.e(TAG, "Writing Loadout failed", e);
		}
		return false;
	}
}
