package io.github.yeluod.util.exception;

import java.io.Serial;
import java.io.Serializable;

/**
 * KitException
 *
 * @author W.d
 * @since 2022/10/15 13:02
 **/
public class KitException extends RuntimeException implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    public KitException() {
        super();
    }

    public KitException(String message) {
        super(message);
    }

    public KitException(Throwable cause) {
        super(cause);
    }

    public KitException(String message, Throwable cause) {
        super(message, cause);
    }

    protected KitException(String message, Throwable cause,
                           boolean enableSuppression, boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
