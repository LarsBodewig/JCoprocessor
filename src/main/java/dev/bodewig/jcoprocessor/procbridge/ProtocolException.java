package dev.bodewig.jcoprocessor.procbridge;

public class ProtocolException extends RuntimeException {

	protected static final String INCOMPATIBLE_VERSION = "incompatible protocol version";

	protected static final String INCOMPLETE_DATA = "incomplete data";
	protected static final String INVALID_BODY = "invalid body";
	protected static final String INVALID_STATUS_CODE = "invalid status code";
	private static final long serialVersionUID = -2222582272583575540L;
	protected static final String UNRECOGNIZED_PROTOCOL = "unrecognized protocol";

	public ProtocolException(String message) {
		super(message);
	}

}
