package de.maxgb.loadoutsaver.util;

public class UnexpectedStuffException extends Exception {

	public enum Location {
		LOGIN, SENDING, SAVING, OTHER;
	}

	/**
	 * 
	 */
	private static final long serialVersionUID = 54545404L;

	Location loc;

	public UnexpectedStuffException(String message, Location l) {
		super(message);
		loc = l;
	}

	@Override
	public String toString() {
		return super.getMessage() + " at Location: " + loc;
	}

}
