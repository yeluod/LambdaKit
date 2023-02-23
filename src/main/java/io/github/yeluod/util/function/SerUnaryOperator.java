package io.github.yeluod.util.function;

import java.io.Serializable;
import java.util.function.UnaryOperator;

/**
 * SerUnaryOperator
 *
 * @author W.d
 * @since 2022/10/15 12:30
 **/
@FunctionalInterface
public interface SerUnaryOperator<T> extends UnaryOperator<T>, Serializable {

    /**
     * Returns a unary operator that always returns its input argument.
     *
     * @param <T> the type of the input and output of the operator
     * @return a unary operator that always returns its input argument
     */
    static <T> UnaryOperator<T> identity() {
        return t -> t;
    }

}
