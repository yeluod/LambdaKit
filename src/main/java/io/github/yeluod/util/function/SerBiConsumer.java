package io.github.yeluod.util.function;

import io.github.yeluod.util.exception.KitException;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.BiConsumer;

/**
 * SerBiConsumer
 *
 * @author W.d
 * @since 2022/10/14 16:18
 **/
@FunctionalInterface
public interface SerBiConsumer<T, U> extends BiConsumer<T, U>, Serializable {

    /**
     * Performs this operation on the given arguments.
     *
     * @param t the first input argument
     * @param u the second input argument
     */
    @Override
    default void accept(T t, U u) {
        try {
            accepting(t, u);
        } catch (Exception e) {
            throw new KitException(e);
        }
    }

    /**
     * Performs this operation on the given arguments.
     *
     * @param t the first input argument
     * @param u the second input argument
     * @throws Exception Exception
     */
    void accepting(T t, U u) throws Exception;

    /**
     * Returns a composed {@code BiConsumer} that performs, in sequence, this
     * operation followed by the {@code after} operation. If performing either
     * operation throws an exception, it is relayed to the caller of the
     * composed operation.  If performing this operation throws an exception,
     * the {@code after} operation will not be performed.
     *
     * @param after the operation to perform after this operation
     * @return a composed {@code BiConsumer} that performs in sequence this
     * operation followed by the {@code after} operation
     * @throws NullPointerException if {@code after} is null
     */
    default SerBiConsumer<T, U> andThen(SerBiConsumer<? super T, ? super U> after) {
        Objects.requireNonNull(after);
        return (l, r) -> {
            accept(l, r);
            after.accept(l, r);
        };
    }

}
