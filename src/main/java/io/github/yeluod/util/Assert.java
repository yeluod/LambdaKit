package io.github.yeluod.util;

import io.github.yeluod.util.function.SerSupplier;

import java.util.Collection;
import java.util.Map;
import java.util.Objects;

/**
 * 断言
 *
 * @author W.d
 * @since 2022/10/15 13:00
 **/
public class Assert {

    /**
     * 断言是否为 null
     * 如果不为 null 抛出给定的异常
     *
     * @param value {@link Object} 值
     * @throws IllegalArgumentException if value is non null
     * @author W.d
     * @since 2022/10/15 13:09
     **/
    public static <E extends Throwable> void isNull(Object value) throws E {
        nonNull(value, "The value must be null");
    }

    /**
     * 断言是否为 null
     * 如果不为 null 抛出给定的异常
     *
     * @param value    {@link Object} 值
     * @param errorMsg {@link SerSupplier<E>} 指定断言不通过时抛出的异常信息
     * @throws IllegalArgumentException if value is non null
     * @author W.d
     * @since 2022/10/15 13:09
     **/
    public <E extends Throwable> void isNull(Object value, String errorMsg) throws E {
        isNull(value, () -> new IllegalArgumentException(errorMsg));
    }

    /**
     * 断言是否为 null
     * 如果不为 null 抛出给定的异常
     *
     * @param value    {@link Object} 值
     * @param supplier {@link SerSupplier<E>} 指定断言不通过时抛出的异常
     * @throws E if value is non null
     * @author W.d
     * @since 2022/10/15 13:09
     **/
    @SuppressWarnings("rawtypes")
    public <E extends Throwable> void isNull(Object value, SerSupplier<? extends E> supplier) throws E {
        if (value instanceof CharSequence sequence && !sequence.toString().trim().isEmpty()) {
            throw supplier.get();
        }
        if (value instanceof Map map && !map.isEmpty()) {
            throw supplier.get();
        }
        if (value instanceof Collection coll && !coll.isEmpty()) {
            throw supplier.get();
        }
        if (Objects.nonNull(value)) {
            throw supplier.get();
        }
    }

    /**
     * 断言是否不为 null
     * 如果为 null 抛出给定的异常
     *
     * @param value {@link Object} 值
     * @throws IllegalArgumentException if value is null
     * @author W.d
     * @since 2022/10/15 13:09
     **/
    public <E extends Throwable> void nonNull(Object value) throws E {
        nonNull(value, "The value must be non null");
    }

    /**
     * 断言是否不为 null
     * 如果为 null 抛出给定的异常
     *
     * @param value    {@link Object} 值
     * @param errorMsg {@link SerSupplier<E>} 指定断言不通过时抛出的异常信息
     * @throws IllegalArgumentException if value is null
     * @author W.d
     * @since 2022/10/15 13:09
     **/
    public static <E extends Throwable> void nonNull(Object value, String errorMsg) throws E {
        nonNull(value, () -> new IllegalArgumentException(errorMsg));
    }

    /**
     * 断言是否不为 null
     * 如果为 null 抛出给定的异常
     *
     * @param value    {@link Object} 值
     * @param supplier {@link SerSupplier<E>} 指定断言不通过时抛出的异常
     * @throws E if value is null
     * @author W.d
     * @since 2022/10/15 13:09
     **/
    @SuppressWarnings("rawtypes")
    public static <E extends Throwable> void nonNull(Object value, SerSupplier<? extends E> supplier) throws E {
        if (Objects.isNull(value)) {
            throw supplier.get();
        }
        if (value instanceof CharSequence sequence && sequence.toString().trim().isEmpty()) {
            throw supplier.get();
        }
        if (value instanceof Map map && map.isEmpty()) {
            throw supplier.get();
        }
        if (value instanceof Collection coll && coll.isEmpty()) {
            throw supplier.get();
        }
    }

    /**
     * 断言是否为 true
     * 如果为 false 抛出异常
     *
     * @param expression {@link Boolean} 布尔值
     * @throws IllegalArgumentException if expression is false
     * @author W.d
     * @since 2022/10/15 13:09
     **/
    public static void isTrue(boolean expression) throws IllegalArgumentException {
        isTrue(expression, "The value must be true");
    }

    /**
     * 断言是否为 true
     * 如果为 false 抛出给定的异常信息
     *
     * @param expression {@link Boolean} 布尔值
     * @param errorMsg   {@link String} 指定断言不通过时抛出的异常信息
     * @throws IllegalArgumentException if expression is false
     * @author W.d
     * @since 2022/10/15 13:09
     **/
    public static void isTrue(boolean expression, String errorMsg) throws IllegalArgumentException {
        isTrue(expression, () -> new IllegalArgumentException(errorMsg));
    }

    /**
     * 断言是否为 true
     * 如果为 false 抛出给定的异常
     *
     * @param expression {@link Boolean} 布尔值
     * @param supplier   {@link SerSupplier<E>} 指定断言不通过时抛出的异常
     * @throws E if expression is false
     * @author W.d
     * @since 2022/10/15 13:09
     **/
    public static <E extends Throwable> void isTrue(boolean expression, SerSupplier<? extends E> supplier) throws E {
        if (!expression) {
            throw supplier.get();
        }
    }

    /**
     * 断言是否为 false
     * 如果为 true 抛出异常
     *
     * @param expression {@link Boolean} 布尔值
     * @throws IllegalArgumentException if expression is true
     * @author W.d
     * @since 2022/10/15 13:09
     **/
    public static void isFalse(boolean expression) throws IllegalArgumentException {
        isFalse(expression, "The value must be false");
    }

    /**
     * 断言是否为 false
     * 如果为 true 抛出给定的异常信息
     *
     * @param expression {@link Boolean} 布尔值
     * @param errorMsg   {@link String} 指定断言不通过时抛出的异常信息
     * @throws IllegalArgumentException if expression is true
     * @author W.d
     * @since 2022/10/15 13:09
     **/
    public static void isFalse(boolean expression, String errorMsg) throws IllegalArgumentException {
        isFalse(expression, () -> new IllegalArgumentException(errorMsg));
    }

    /**
     * 断言是否为 false
     * 如果为 true 抛出给定的异常
     *
     * @param expression {@link Boolean} 布尔值
     * @param supplier   {@link SerSupplier<E>} 指定断言不通过时抛出的异常
     * @throws E if expression is true
     * @author W.d
     * @since 2022/10/15 13:09
     **/
    public static <E extends Throwable> void isFalse(boolean expression, SerSupplier<? extends E> supplier) throws E {
        if (expression) {
            throw supplier.get();
        }
    }
}
