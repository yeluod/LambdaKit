package io.github.yeluod.util.exception;

import java.io.Serial;
import java.io.Serializable;

/**
 * VerifyException
 *
 * @author W.d
 * @since 2022/10/15 13:02
 **/
public class VerifyException extends RuntimeException implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public VerifyException() {
        super();
    }

    public VerifyException(String message) {
        super(message);
    }

    public VerifyException(Throwable cause) {
        super(cause);
    }

    public VerifyException(String message, Throwable cause) {
        super(message, cause);
    }

    protected VerifyException(String message, Throwable cause,
                              boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
