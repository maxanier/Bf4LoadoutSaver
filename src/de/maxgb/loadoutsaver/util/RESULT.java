package de.maxgb.loadoutsaver.util;

/**
 * Class with all Error constants
 * 
 * @author Max Becker
 * 
 */
public class RESULT {
	public final static String getDescription(int result) {
		switch (result) {
		case REQUESTFAILED:
			return "There was a problem executing you request, please try again or report this error";
		case CONNECTING_PROBLEM:
			return "There is a problem with the connecting to the battlelog servers. Try again later or report this problem.";
		case INTERNALSERVERERROR:
			return "Battlelog probably changed something on their servers, please report this problem to get it fixed.";
		case OTHERERROR:
			return "An unexpected error occured please report this to the developer so this can be fixed.";
		case TIMEOUT:
			return "Server Timeout. Either the server or your internet is too slow.\nTry again later.";
		case LOGINCREDENTIALSERROR:
			return "You credentials seem to be wrong please try again.";
		case NOPERSONA:
			return "Do you own Battlefield 4?";
		case MIXING_LOADOUTS:
			return "You tried to mix Loadouts, this could create problems, if you want to do it anyway activate it in the settings menu.";
		default:
			return null;

		}
	}
	public final static boolean shouldBeReportable(int result) {
		if (result == MIXING_LOADOUTS || result == NOPERSONA
				|| result == TIMEOUT || result == LOGINCREDENTIALSERROR) {
			return false;
		}
		return true;
	}
	// Result constants
	public final static int OK = 1;
	public final static int REQUESTFAILED = 2;
	public final static int CONNECTING_PROBLEM = 3;
	public final static int INTERNALSERVERERROR = 4;
	public final static int OTHERERROR = 5;
	public final static int TIMEOUT = 6;
	public final static int LOGINCREDENTIALSERROR = 7;
	public final static int NOSTATS = 8;
	public final static int NOPERSONA = 9;

	public final static int MIXING_LOADOUTS = 10;

	public final static int SESSION_EXPIRED = 11;
}
