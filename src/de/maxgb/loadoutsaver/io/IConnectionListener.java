package de.maxgb.loadoutsaver.io;

public interface IConnectionListener {
	public void loggedIn(String persona,String platform);
	public void failedToLogin(String error);
}
