package dev.bodewig.jcoprocessor.procbridge;

/**
 * Constants used in the Protocol
 *
 * @author Gong Zhang
 * @author Lars Bodewig
 */
public enum Key {

	MESSAGE, METHOD, PAYLOAD;

	@Override
	public String toString() {
		return name().toLowerCase();
	}
}
