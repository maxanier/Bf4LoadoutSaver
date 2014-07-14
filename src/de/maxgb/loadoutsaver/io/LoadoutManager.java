package de.maxgb.loadoutsaver.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.graphics.Color;
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
		loadout.add(temp);
		return writeLoadout(loadout);
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
				output.append("{}");
				output.newLine();
				output.flush();
				output.close();
			}
		} catch (IOException e) {
			Logger.e(TAG,
					"Failed to create a new file");
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
	
	private void rewriteOldLoadout(File oldLoadoutFile){
		if (!oldLoadoutFile.exists()) {
			Logger.w(TAG, "Cant rewrite: oldLoadoutFile doesn´t exist");
			return;
		}
		if (!oldLoadoutFile.canRead()) {
			Logger.w(TAG, "Cant rewrite: Can´t read oldLoadout File");
			return;
		}
		ArrayList<Loadout> temp = new ArrayList<Loadout>();

		try {
			BufferedReader br = new BufferedReader(new FileReader(oldLoadoutFile));

			String line = br.readLine();

			if (line != null && line.startsWith("Version:")) {
				
				line = br.readLine();
			}

			while (line != null) {
				Loadout l = readOldLoadout(line);
				if (l != null) {
					temp.add(l);
				}
				line = br.readLine();
			}
			
			writeLoadout(temp);

		} catch (FileNotFoundException e) {
			Logger.e(
					TAG,
					"FileNotFoundException even though oldFile existence was checked",
					e);
		} catch (IOException e) {
			Logger.e(TAG, "IOException while reading oldLoadout", e);
		}
	}

	private ArrayList<Loadout> readLoadout() {
		Logger.i(TAG, "Start reading Loadout");
		
		Logger.i(TAG, "Testing of old file exists");
		File oldLoadoutFile = new File(Constants.DIRECTORY
				+ Constants.LOADOUT_OLD_FILE_NAME);
		if(oldLoadoutFile.exists()){
			Logger.i(TAG, "Found old one");
			rewriteOldLoadout(oldLoadoutFile);
			if(!oldLoadoutFile.delete()){
				oldLoadoutFile.deleteOnExit();
			}
		}
		
		Logger.i(TAG, "Reading new Loadoutfile");
		ArrayList<Loadout> temp = new ArrayList<Loadout>();
		
		File loadoutFile=new File(Constants.DIRECTORY+Constants.LOADOUT_FILE_NAME);
		
		if(!loadoutFile.exists()){
			Logger.w(TAG, "Loadout file does not exist");
			return temp;
		}
		
		if(!loadoutFile.canRead()){
			Logger.w(TAG, "Cant read loadoutfile");
			return temp;
		}
		try {
			BufferedReader reader = new BufferedReader(new FileReader(loadoutFile));
			String line=reader.readLine();
			
			JSONObject data = new JSONObject(line);
			if(data.has("loadouts")){
				JSONArray loadouts=data.getJSONArray("loadouts");
				for(int i=0;i<loadouts.length();i++){
					Loadout l=Loadout.fromJSON(loadouts.getJSONObject(i));
					if(l!=null){
						temp.add(l);
					}
				}
			}
			else{
				Logger.i(TAG, "JSON did not include any loadouts");
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
	private Loadout readOldLoadout(String s) {
		if (s == null)
			return null;

		try {
			String[] splitted = s.split(Constants.LOADOUT_SEPERATOR);
			if (splitted.length == 5||splitted.length == 6) {
				String name = splitted[0];
				boolean weapons = splitted[1].equals("1");
				boolean kits = splitted[2].equals("1");
				boolean vehicles = splitted[3].equals("1");
				

				

				JSONObject loadout = new JSONObject(splitted[4]);
				
				int color=Color.BLACK;
				if(splitted.length==6){
					try {
						color=Integer.parseInt(splitted[5]);
					} catch (NumberFormatException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				return new Loadout(name, loadout, weapons, kits, vehicles,color,"");
			} else {
				Logger.e(TAG, "Loadoutline did not contain 5/6 parts");
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
			JSONObject json=new JSONObject();
			JSONArray array=new JSONArray();
			for (int i = 0; i < list.size(); i++) {
				array.put(list.get(i).toJson());
			}
			
			json.put("loadouts", array);
			
			output.write(json.toString());

			output.close();
			return true;

		} catch (IOException e) {
			// TODO Auto-generated catch block
			Logger.e(TAG, "Writing Loadout failed", e);
		} catch (JSONException e) {
			Logger.e(TAG, "Createing Loadout Json failed",e);
		}
		return false;
	}
}
