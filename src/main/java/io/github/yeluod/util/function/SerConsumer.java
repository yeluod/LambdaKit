package io.github.yeluod.util.function;

import io.github.yeluod.util.exception.KitException;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Consumer;

/**
 * SerConsumer
 *
 * @author W.d
 * @since 2022/10/14 16:18
 **/
@FunctionalInterface
public interface SerConsumer<T> extends Consumer<T>, Serializable {

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     */
    @Override
    default void accept(T t) {
        try {
            accepting(t);
        } catch (Exception e) {
            throw new KitException(e);
        }
    }

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     * @throws Exception Exception
     */
    void accepting(T t) throws Exception;

    /**
     * Returns a composed {@code Consumer} that performs, in sequence, this
     * operation followed by the {@code after} operation. If performing either
     * operation throws an exception, it is relayed to the caller of the
     * composed operation.  If performing this operation throws an exception,
     * the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code Consumer} that performs in sequence this
     * operation followed by the {@code after} operation
     * @throws NullPointerException if {@code after} is null
     */
    default SerConsumer<T> andThen(SerConsumer<? super T> after) {
        Objects.requireNonNull(after);
        return (T t) -> {
            accept(t);
            after.accept(t);
        };
    }
}
