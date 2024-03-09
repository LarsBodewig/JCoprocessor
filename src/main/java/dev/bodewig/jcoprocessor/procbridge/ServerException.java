package dev.bodewig.jcoprocessor.procbridge;

/**
 * Exception to indicate an unexpected behaviour in the Server
 *
 * @author Gong Zhang
 * @author Lars Bodewig
 */
public class ServerException extends RuntimeException {

	private static final long serialVersionUID = 9194211579590756799L;

	/**
	 * The String used if no message was given
	 */
	protected static final String UNKNOWN_SERVER_ERROR = "Unknown server error";

	/**
	 * Creates a new ServerException with a message
	 *
	 * @param message the message, null defaults to "unknown server error"
	 */
	public ServerException(String message) {
		super(message != null ? message : UNKNOWN_SERVER_ERROR);
	}

	/**
	 * Creates a new ServerException with a cause
	 *
	 * @param cause the cause
	 */
	public ServerException(Throwable cause) {
		super(cause.getMessage(), cause);
	}
}
