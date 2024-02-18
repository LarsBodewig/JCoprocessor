package dev.bodewig.jcoprocessor.procbridge;

public class ServerException extends RuntimeException {

	private static final long serialVersionUID = 9194211579590756799L;

	protected static final String UNKNOWN_SERVER_ERROR = "unknown server error";

	public ServerException(String message) {
		super(message != null ? message : UNKNOWN_SERVER_ERROR);
	}

	public ServerException(Throwable cause) {
		super(cause.getMessage(), cause);
	}

}
