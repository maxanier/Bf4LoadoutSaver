package de.maxgb.loadoutsaver.util;

/**
 * InfantryLoadout
 * Saves the part of a Loadout beginning with "weapons:", including the '"' but excluding the preceding '{', and ending with the after the "version"="x", excluding the following ',' 
 */
public class InfantryLoadout extends Loadout {

	public InfantryLoadout(String name, String loadout) {
		super(name, loadout);

	}

}
