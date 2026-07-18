package dev.bodewig.jcoprocessor.procbridge;

import java.io.Serial;

/**
 * Exception to indicate that a timeout occurred
 *
 * @author Gong Zhang
 * @author Lars Bodewig
 */
public class TimeoutException extends RuntimeException {

    @Serial
    private static final long serialVersionUID = 5925042415173238694L;

    /**
     * Creates a new TimeoutException
     */
    public TimeoutException() {
        super();
    }
}
