package dev.bodewig.jcoprocessor.procbridge;

/**
 * Exception to indicate an unexpected behaviour in the Client
 *
 * @author Gong Zhang
 * @author Lars Bodewig
 */
public class ClientException extends RuntimeException {

	private static final long serialVersionUID = -36101886841377711L;

	/**
	 * Creates a ClientException with a cause
	 *
	 * @param cause the cause
	 */
	public ClientException(Throwable cause) {
		super(cause);
	}
}
