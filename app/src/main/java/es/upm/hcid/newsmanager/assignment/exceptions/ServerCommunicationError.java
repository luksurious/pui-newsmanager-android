package es.upm.hcid.newsmanager.assignment.exceptions;

public class ServerCommunicationError extends Exception {
	/**
	 * 
	 */
	private static final long serialVersionUID = -2703194338913940270L;

	public ServerCommunicationError(String message){
		super(message);
	}
}
