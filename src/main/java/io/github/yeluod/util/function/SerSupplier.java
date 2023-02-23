package io.github.yeluod.util.function;

import io.github.yeluod.util.exception.KitException;

import java.io.Serializable;
import java.util.function.Supplier;

/**
 * SerSupplier
 *
 * @author W.d
 * @since 2022/10/15 12:28
 **/
@FunctionalInterface
public interface SerSupplier<T> extends Supplier<T>, Serializable {

    /**
     * Gets a result.
     *
     * @return a result
     */
    @Override
    default T get() {
        try {
            return getting();
        } catch (Exception e) {
            throw new KitException(e);
        }
    }

    /**
     * Gets a result.
     *
     * @return a result
     * @throws Exception Exception
     **/
    T getting() throws Exception;

}
