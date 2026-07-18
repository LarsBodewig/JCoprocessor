package dev.bodewig.jcoprocessor.procbridge;

import java.io.Serial;

/**
 * Exception to indicate an unexpected behavior in the Client
 *
 * @author Gong Zhang
 * @author Lars Bodewig
 */
public class ClientException extends RuntimeException {

    @Serial
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
