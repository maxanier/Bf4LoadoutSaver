package de.maxgb.loadoutsaver.io;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;

import loadoutanalyzer.Analyzer;

import android.util.Log;

import de.maxgb.android.util.Logger;
import de.maxgb.loadoutsaver.util.Constants;
import de.maxgb.loadoutsaver.util.InfantryLoadout;
import de.maxgb.loadoutsaver.util.Loadout;
import de.maxgb.loadoutsaver.util.VehicleLoadout;

public class LoadoutManager {
	/*
	 * Save Format: <name>!<type>!<loadoutString> linebreak
	 * Types refer to Constants
	 */
	private static LoadoutManager instance;
	private final static String TAG = "LoadoutManager";
	private ArrayList<Loadout> loadout;
	
	
	private LoadoutManager(){
		loadout = readLoadout();
	}
	
	public static synchronized LoadoutManager getInstance(){
		if (instance == null) {
			instance = new LoadoutManager();
		}
		return instance;
	}
	
	private ArrayList<Loadout> readLoadout(){
		Logger.i(TAG,"Start reading Loadout");
		ArrayList<Loadout> temp = new ArrayList<Loadout>();
		File loadoutFile = new File(Constants.DIRECTORY+Constants.LOADOUT_FILE_NAME);
		if(!loadoutFile.exists()){
			Logger.w(TAG,"LoadoutFile doesn´t exist");
			return temp;
		}
		if(!loadoutFile.canRead()){
			Logger.w(TAG,"Can´t read Loadout File");
			return temp;
		}
		
		try {
			BufferedReader br = new BufferedReader(new FileReader(loadoutFile));
			
			String line = br.readLine();
			
			while(line !=null){
				String[] lineSplitted = line.trim().split(Constants.LOADOUT_SEPERATOR);
				if(lineSplitted.length==3){
					if(lineSplitted[1].equals(""+Constants.ALL_TYPE)){
						temp.add(new Loadout(lineSplitted[0],lineSplitted[2]));
					}
					else if(lineSplitted[1].equals(""+Constants.INFANTRY_TYPE)){
						temp.add(new InfantryLoadout(lineSplitted[0],lineSplitted[2]));
					}
					else{
						temp.add(new VehicleLoadout(lineSplitted[0],lineSplitted[2]));
					}
					
				}
				line= br.readLine();
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		Logger.i(TAG,"Found "+temp.size()+" Loadouts");
		return temp;
	}
	
	/**
	 * Adds a Loadout to the ArrayList and writes it to the file
	 * If there already is a Loadout with that name, the old one is deleted and the new one is appended
	 * @param temp Loadout which should be add
	 * @return true if succesfully
	 */
	public boolean addLoadout(Loadout temp){
		File directory=new File(Constants.DIRECTORY);
		directory.mkdir();
		File loadoutFile=new File(Constants.DIRECTORY+Constants.LOADOUT_FILE_NAME);
		for(int i=0;i<loadout.size();i++){
			if(loadout.get(i).getName().equals(temp.getName())){
				removeLoadout(i);
			}
		}
		
		try {
			loadoutFile.createNewFile();
			BufferedWriter output=new BufferedWriter(new FileWriter(loadoutFile,true));
			output.newLine();
			output.append(temp.getName()+Constants.LOADOUT_SEPERATOR+temp.getType()+Constants.LOADOUT_SEPERATOR+temp.getLoadout());
			output.close();
			loadout.add(temp);
			
			return true;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Logger.e(TAG, "Adding Loadout failed",e);
		}
		return false;
	}
	
	private boolean writeLoadout(ArrayList<Loadout> list){
		
		File directory=new File(Constants.DIRECTORY);
		directory.mkdir();
		File loadoutFile=new File(Constants.DIRECTORY+Constants.LOADOUT_FILE_NAME);
		
		try {
			loadoutFile.createNewFile();
			BufferedWriter output=new BufferedWriter(new FileWriter(loadoutFile,false));
			for(int i=0;i<list.size();i++){
				output.newLine();
				output.append(list.get(i).getName()+Constants.LOADOUT_SEPERATOR+list.get(i).getType()+Constants.LOADOUT_SEPERATOR+list.get(i).getLoadout());
			}

			output.close();
			return true;
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			Logger.e(TAG, "Writing Loadout failed",e);
		}
		return false;
	}
	
	public ArrayList<Loadout> getLoadout() {
		return loadout;
	}

	public void removeLoadout(Loadout temp){
		loadout.remove(temp);
		writeLoadout(loadout);
	}
	public void removeLoadout(int position){
		removeLoadout(loadout.get(position));
	}
	
	
}
