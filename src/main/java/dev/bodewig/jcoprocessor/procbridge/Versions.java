package dev.bodewig.jcoprocessor.procbridge;

/**
 * Contains constants to identify the used protocol version
 *
 * @author Gong Zhang
 * @author Lars Bodewig
 */
public final class Versions {

	private static final byte[] V2_0 = { 2, 0 };

	/**
	 * The current protocol version
	 */
	public static final byte[] CURRENT = V2_0;

	private Versions() {
	}
}
