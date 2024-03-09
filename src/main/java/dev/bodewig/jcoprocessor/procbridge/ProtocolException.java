package dev.bodewig.jcoprocessor.procbridge;

/**
 * Exception to indicate an unexpected behaviour in the Protocol
 *
 * @author Gong Zhang
 * @author Lars Bodewig
 */
public class ProtocolException extends RuntimeException {

	private static final long serialVersionUID = -2222582272583575540L;

	/**
	 * The message to indicate an incompatible version
	 */
	protected static final String INCOMPATIBLE_VERSION = "Incompatible protocol version";

	/**
	 * The message to indicate incomplete data
	 */
	protected static final String INCOMPLETE_DATA = "Incomplete data";

	/**
	 * The message to indicate an invalid body
	 */
	protected static final String INVALID_BODY = "Invalid body";

	/**
	 * The message to indicate an invalid status code
	 */
	protected static final String INVALID_STATUS_CODE = "Invalid status code";

	/**
	 * The message to indicate an unrecognized protocol
	 */
	protected static final String UNRECOGNIZED_PROTOCOL = "Unrecognized protocol";

	/**
	 * Creates a new ProtocolException with a message
	 *
	 * @param message the message
	 */
	public ProtocolException(String message) {
		super(message);
	}
}
