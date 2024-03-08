package dev.bodewig.jcoprocessor.procbridge;

/**
 * 
 * @author Gong Zhang
 * @author Lars Bodewig
 */
public enum StatusCode {

	BAD_RESPONSE(2), GOOD_RESPONSE(1), REQUEST(0);

	public static StatusCode fromRawValue(int rawValue) {
		for (StatusCode sc : StatusCode.values()) {
			if (sc.rawValue == rawValue) {
				return sc;
			}
		}
		return null;
	}

	public int rawValue;

	private StatusCode(int rawValue) {
		this.rawValue = rawValue;
	}

}
