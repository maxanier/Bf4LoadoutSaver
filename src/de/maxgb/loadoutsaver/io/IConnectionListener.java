package de.maxgb.loadoutsaver.io;

public interface IConnectionListener {
	public void failedToLogin(String error);

	public void loggedIn(String persona, String platform);
}
