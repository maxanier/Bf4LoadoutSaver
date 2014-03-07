package de.maxgb.loadoutsaver.util;

import java.util.ArrayList;

import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.Base64;
import de.maxgb.android.util.GraphicUtils;
import de.maxgb.loadoutsaver.R;

public class Loadout implements Cloneable {
	private String name;
	private JSONObject loadout;
	private boolean weapons;
	private boolean kits;
	private boolean vehicles;
	
	/**
	 * 
	 * @param name Loadoutname
	 * @param loadout Loadout object
	 * @param weapons if weapons
	 * @param kits if kits
	 * @param vehicles if vehicles
	 */
	public Loadout(String name,JSONObject loadout,boolean weapons,boolean kits,boolean vehicles){
		this.name=name;
		this.loadout=loadout;
		this.weapons=weapons;
		this.kits=kits;
		this.vehicles=vehicles;
	}

	public String getName() {
		return name;
	}

	public JSONObject getLoadout() {
		return loadout;
	}
	public void setLoadout(JSONObject loadout){
		this.loadout=loadout;
	}
	

	

	
	public boolean containsVehicle(){
		return vehicles;
	}
	public boolean containsKits(){
		return kits;
	}
	public boolean containsWeapons(){
		return weapons;
	}
	
	/**
	 * returns a string representing this laodout object
	 * @param s seperator
	 * @return
	 */
	public String toString(String s){
		return name+s+(weapons ? "1" : "0")+s+(kits?"1":"0")+s+(vehicles?"1":"0")+s+loadout.toString();
	}
	
	@Override
	@Deprecated
	public String toString(){
		return super.toString();
	}
	
	@Override
	public Loadout clone(){
		return new Loadout(name,loadout,weapons,kits,vehicles);
	}
	
	public Bitmap getImage(Context context){
		if(weapons && kits && vehicles){
			return GraphicUtils.textAsBitmap("ALL", 50, Color.WHITE);
		}
		
		ArrayList<Bitmap> images=new ArrayList<Bitmap>();
		if(weapons){
			images.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.weapons));
		}
		if(vehicles){
			images.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.vehicles));
		}
		if(kits){
			images.add(BitmapFactory.decodeResource(context.getResources(), R.drawable.kits));
		}
		
		if(images.size()==2){
			return GraphicUtils.combineImages(images.get(0),images.get(1));
		}
		
		return images.get(0);
	}
	



    


}
