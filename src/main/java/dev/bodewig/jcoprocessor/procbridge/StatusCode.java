package dev.bodewig.jcoprocessor.procbridge;

/**
 * Constants to communicate the outcome of an operation
 *
 * @author Gong Zhang
 * @author Lars Bodewig
 */
public enum StatusCode {

	BAD_RESPONSE(2), GOOD_RESPONSE(1), REQUEST(0);

	/**
	 * Find the StatusCode from the int representation
	 *
	 * @param rawValue the int representation
	 * @return the matching StatusCode
	 */
	public static StatusCode fromRawValue(int rawValue) {
		for (StatusCode sc : StatusCode.values()) {
			if (sc.rawValue == rawValue) {
				return sc;
			}
		}
		return null;
	}

	/**
	 * The int representing the StatusCode
	 */
	public final int rawValue;

	private StatusCode(int rawValue) {
		this.rawValue = rawValue;
	}
}
