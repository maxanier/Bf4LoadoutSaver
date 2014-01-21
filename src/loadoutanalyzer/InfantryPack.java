package loadoutanalyzer;

public class InfantryPack {
	private String[][] weapons, kits;
	private char selectedKit, version;
	
	public InfantryPack(String[][] w, String[][] k, char sk, char v) {
		weapons = w;
		kits = k;
		selectedKit = sk;
		version = v;
	}
	
	public String[][] getWeapons() {
		return weapons;
	}
	public String[][] getKits() {
		return kits;
	}
	public char getSelectedKit() {
		return selectedKit;
	}
	public char getVersion() {
		return version;
	}
}
