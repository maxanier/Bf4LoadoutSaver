package de.maxgb.loadoutsaver.util;

import de.maxgb.loadoutsaver.R;

public class Loadout {
	private String name;
	private String loadout;
	
	public Loadout(String name,String loadout){
		this.name=name;
		this.loadout=loadout;
	}

	public String getName() {
		return name;
	}

	public String getLoadout() {
		return loadout;
	}
	public void setLoadout(String loadout){
		this.loadout=loadout;
	}
	
	public int getDrawableId(){
		int type=getType();
		switch(type){
		case Constants.VEHICLE_TYPE:
			return R.drawable.vehicle;
		case Constants.INFANTRY_TYPE:
			return R.drawable.infantry;
		default:
			return R.drawable.all_type;
		}
	}
	
	public static String getCombinedLoadout(String infantry,String vehicle){
		return "{"+infantry+","+vehicle+"}";
	}
	
	public int getType(){
		if(this.getClass().equals(InfantryLoadout.class)){
			return Constants.INFANTRY_TYPE;
		}
		if(this.getClass().equals(VehicleLoadout.class)){
			return Constants.VEHICLE_TYPE;
		}
		return Constants.ALL_TYPE;
	}
	public static String getInfantryFromFull(String loadout){
		return loadout.split(",\"vehicles\"")[0].substring(1);
	}
	public static String getVehicleFromFull(String loadout){
		int index = loadout.indexOf("\"vehicles\"");
		return loadout.substring(index,loadout.length()-1);
	}
}
