package de.maxgb.loadoutsaver.util;

import java.util.ArrayList;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import de.maxgb.android.util.GraphicUtils;
import de.maxgb.android.util.Logger;
import de.maxgb.loadoutsaver.R;

public class Loadout implements Cloneable {
	private String name;
	private JSONObject loadout;
	private boolean weapons;
	private boolean kits;
	private boolean vehicles;
	private int color;
	private String personalId;

	/**
	 * 
	 * @param name
	 *            Loadoutname
	 * @param loadout
	 *            Loadout object
	 * @param weapons
	 *            if weapons
	 * @param kits
	 *            if kits
	 * @param vehicles
	 *            if vehicles
	 * @param color Color for displaying purpose
	 */
	public Loadout(String name, JSONObject loadout, boolean weapons,
			boolean kits, boolean vehicles,int color,String personalId) {
		this.name = name;
		this.loadout = loadout;
		this.weapons = weapons;
		this.kits = kits;
		this.vehicles = vehicles;
		this.color=color;
	}
	
	private Loadout(){
		personalId="";
	}

	@Override
	public Loadout clone() {
		return new Loadout(name, loadout, weapons, kits, vehicles,color,personalId);
	}

	public boolean containsKits() {
		return kits;
	}

	public boolean containsVehicle() {
		return vehicles;
	}

	public boolean containsWeapons() {
		return weapons;
	}

	public Bitmap getImage(Context context) {
		if (weapons && kits && vehicles) {
			return GraphicUtils.textAsBitmap("ALL", 50, Color.WHITE);
		}

		ArrayList<Bitmap> images = new ArrayList<Bitmap>();
		if (weapons) {
			images.add(BitmapFactory.decodeResource(context.getResources(),
					R.drawable.weapons));
		}
		if (vehicles) {
			images.add(BitmapFactory.decodeResource(context.getResources(),
					R.drawable.vehicles));
		}
		if (kits) {
			images.add(BitmapFactory.decodeResource(context.getResources(),
					R.drawable.kits));
		}

		if (images.size() == 2) {
			return GraphicUtils.combineImages(images.get(0), images.get(1));
		}

		return images.get(0);
	}

	public JSONObject getLoadout() {
		return loadout;
	}

	public String getName() {
		return name;
	}
	
	public int getColor(){
		return color;
	}

	public void setLoadout(JSONObject loadout) {
		this.loadout = loadout;
	}

	@Override
	@Deprecated
	public String toString() {
		String s = " ";
		return name + s + (weapons ? "1" : "0") + s + (kits ? "1" : "0") + s
				+ (vehicles ? "1" : "0");
	}
	
	public JSONObject toJson(){
		JSONObject json=new JSONObject();
		try {
			return json.put("name", name).put("loadout", loadout).put("weapons", weapons).put("vehicles",vehicles).put("kits", kits).put("color", color).put("personalId",personalId);
		} catch (JSONException e) {
			Logger.e("Loadout", "Failed to create Json from Loadout",e);
			return json;
		}
		
		
	}

	
	public static Loadout fromJSON(JSONObject json){
		
		
		try {
			Loadout l=new Loadout();
			l.name=json.getString("name");
			l.loadout=json.getJSONObject("loadout");
			l.weapons=json.getBoolean("weapons");
			l.vehicles=json.getBoolean("vehicles");
			l.kits=json.getBoolean("kits");
			l.color=json.getInt("color");
			if(json.has("personalId")){
				l.personalId=json.getString("personalId");
			}
			
			return l;
		} catch (JSONException e) {
			Logger.e("Loadout", "Failed to create Loadout frome json",e);
			return null;
		}
	}
	
	public void setPersonaId(String p){
		this.personalId=p;
	}
	
	public String getPersonaId(){
		return personalId;
	}

}
