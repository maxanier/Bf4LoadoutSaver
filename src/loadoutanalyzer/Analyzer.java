package loadoutanalyzer;

public class Analyzer {

	// Temporaly disabled

	// BufferedWriter writer;
	// boolean debug = false;
	//
	//
	//
	// public static void analyzeLoadout(Loadout l) {
	//
	// final String name = l.getName();
	// int type=l.getType();
	// switch (type) {
	// case Constants.ALL_TYPE:
	// try {
	// writeLoadout(name,
	// analyzeInfantryLoadout(Loadout.getInfantryFromFull(l.getLoadout())),
	// analyzeVehicleLoadout(Loadout.getVehicleFromFull(l.getLoadout())));
	// } catch (IOException e) {e.printStackTrace();}
	// break;
	// case Constants.INFANTRY_TYPE:
	// try {
	// writeLoadout(name,
	// analyzeInfantryLoadout(Loadout.getInfantryFromFull(l.getLoadout())),
	// null);
	// } catch (IOException e) {e.printStackTrace();}
	// break;
	// case Constants.VEHICLE_TYPE:
	// try {
	// writeLoadout(name, null,
	// analyzeVehicleLoadout(Loadout.getVehicleFromFull(l.getLoadout())));
	// } catch (IOException e) {e.printStackTrace();}
	// break;
	// }
	// }
	//
	// private static void writeLoadout(String name, InfantryPack ifp,
	// String[][] vep) throws IOException {
	// BufferedWriter writer = null;
	// File dest = new File(Constants.DIRECTORY + name + "_ANALYZED.txt");
	// dest.createNewFile();
	// writer = new BufferedWriter(new FileWriter(dest));
	// writer.write("Loadout Name: " + name);
	// if (ifp != null) {
	// writer.write("Version: " + ifp.getVersion() + "\nSelectedKit: " +
	// ifp.getSelectedKit() + "\n\nWeapons:\n");
	// for (int i = 0; i < ifp.getWeapons().length; i++) {
	// String tmp = "Weapon ID: " + ifp.getWeapons()[i][0] + "\n";
	// for (int j = 1; j < ifp.getWeapons()[i].length; j++) {
	// tmp = tmp + ifp.getWeapons()[i][j] + "  ";
	// }
	// writer.write(tmp + "\n");
	// }
	// writer.write("\n\nKits:\n");
	// for (int i = 0; i < ifp.getKits().length; i++) {
	// String tmp = "Kit " + i + "\n";
	// for (int j = 0; j < ifp.getKits()[i].length; j++) {
	// tmp = tmp + ifp.getKits()[i][j] + "  ";
	// }
	// writer.write(tmp + "\n");
	// }
	// }
	// if(vep!=null) {
	// writer.write("\n\nVehicles:\n");
	// for(int i=0;i<vep.length;i++) {
	// String tmp = "Vehicle " + i + ": ";
	// for(int j=0;j<vep[i].length;j++) {
	// tmp = tmp + vep[i][j] + "  ";
	// }
	// writer.write(tmp + "\n");
	// }
	// }
	// writer.close();
	// }
	//
	// private static InfantryPack analyzeInfantryLoadout(String l) {
	// // Split at },"selectedKit":"
	// String[] weaponsSKKITS = l.split(",\"selectedKit\":\"");
	// weaponsSKKITS[0] = weaponsSKKITS[0].substring(0,
	// weaponsSKKITS[0].length()-2);
	//
	// // Analyze the weapons part of the loadout:
	// // Put the weapon part in its own String Array, remove
	// // "weapons":{" and all "and all [ and split at ],
	// String[] weapons1 = weaponsSKKITS[0].replace("\"weapons\":{\"", "")
	// .replace("\"", "").replace("[", "").split("],");
	// String[][] weaponsFinal = new String[weapons1.length][];
	// // For each weapon: replace : with , (to make the following splitting
	// // possible), then split it at all ,
	// // Result: 2-dimensional array. First index: weapon, second index: IDs
	// // (0 = weapon ID, other = setup IDs)
	// for (int i = 0; i < weapons1.length; i++) {
	// weaponsFinal[i] = weapons1[i].replace(":", ",").split(",");
	// }
	//
	// // Analyze the kits part of the loadout (selectedKit and kits):
	// final char selectedKit = weaponsSKKITS[1].charAt(0);
	// final char version = weaponsSKKITS[1]
	// .charAt(weaponsSKKITS[1].length() - 2);
	// // Cut off the version part and the kits:[[ part and remove all "
	// String kits1 = weaponsSKKITS[1].substring(11,
	// weaponsSKKITS[1].length() - 16).replace("\"", "");
	// // Split the different kits
	// String[] kits2 = kits1.replace("[", "").split("],");
	// // For each kit: split the different numbers, result: 2-dimensional
	// // array. First index: kit, second index: index of the number inside the
	// // kit
	// String[][] kitsFinal = new String[kits2.length][];
	// for (int i = 0; i < kits2.length; i++) {
	// kitsFinal[i] = kits2[i].split(",");
	// }
	//
	// // Printing everything that was analyzed, for debug purposes
	// /*
	// * System.out.println("Version: " + version + "\nSelectedKit: " +
	// * selectedKit + "\n\nWeapons:"); for (int i = 0; i <
	// * weaponsFinal.length; i++) { String tmp = ("Weapon ID: " +
	// * weaponsFinal[i][0] + "\n"); for (int j = 1; j <
	// * weaponsFinal[i].length; j++) tmp = tmp + weaponsFinal[i][j] + "  ";
	// * System.out.println(tmp); } System.out.println("\n\nKits:"); for (int
	// * i = 0; i < kitsFinal.length; i++) { String tmp = "Kit " + i + "\n";
	// * for (int j = 0; j < kitsFinal[i].length; j++) tmp = tmp +
	// * kitsFinal[i][j] + "  "; System.out.println(tmp); }
	// */
	//
	// return new InfantryPack(weaponsFinal, kitsFinal, selectedKit, version);
	// }
	//
	// private static String[][] analyzeVehicleLoadout(String l) {
	// // Cut off the "vehicles":[[ part, remove all " and [ and split the
	// // separate vehicles at ],
	// String vehicles1[] = l.substring(13, l.length() - 2).replace("\"", "")
	// .replace("[", "").split("],");
	// // Create the final 2-dimensional array. Result will be: first index:
	// // vehicle, second index: ID of the setup
	// String[][] vehiclesFinal = new String[vehicles1.length][];
	// for (int i = 0; i < vehiclesFinal.length; i++)
	// vehiclesFinal[i] = vehicles1[i].split(",");
	//
	// /*
	// * System.out.println("Vehicles:"); for(int
	// * i=0;i<vehiclesFinal.length;i++) { String tmp = "\nVehicle " + i +
	// * ": "; for (int j=0;j<vehiclesFinal[i].length;j++) tmp = tmp +
	// * vehiclesFinal[i][j] + "  "; System.out.println(tmp); }
	// */
	//
	// return vehiclesFinal;
	// }
	//
	//
	//
}
