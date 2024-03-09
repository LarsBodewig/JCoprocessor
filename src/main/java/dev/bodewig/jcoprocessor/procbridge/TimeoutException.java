package dev.bodewig.jcoprocessor.procbridge;

/**
 * Exception to indicate that a timeout occured
 *
 * @author Gong Zhang
 * @author Lars Bodewig
 */
public class TimeoutException extends RuntimeException {

	private static final long serialVersionUID = 5925042415173238694L;

	/**
	 * Creates a new TimeoutException
	 */
	public TimeoutException() {
		super();
	}
}
