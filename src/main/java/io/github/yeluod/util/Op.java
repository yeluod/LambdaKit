package io.github.yeluod.util;

import io.github.yeluod.util.exception.KitException;
import io.github.yeluod.util.function.*;

import java.util.*;
import java.util.stream.Stream;

/**
 * {@link Op<T>}
 *
 * @author W.d
 * @since 1.0.0
 **/
@SuppressWarnings("unused")
public class Op<T> {

    /**
     * 一个空的实例
     **/
    private static final Op<?> EMPTY = new Op<>();

    /**
     * 元素
     */
    private final T value;

    /**
     * 异常
     */
    private Exception exception;

    /**
     * 构造函数
     **/
    private Op() {
        this.value = null;
    }

    /**
     * 构造函数
     **/
    private Op(T value) {
        this.value = value;
    }

    /**
     * 构造函数
     **/
    private Op(Exception exception) {
        this.value = null;
        this.exception = exception;
    }

    /**
     * 构造函数
     **/
    private Op(T value, Exception exception) {
        this.value = value;
        this.exception = exception;
    }

    /**
     * 返回一个空的 {@link Op<T>}
     *
     * @param <T> 元素的类型
     * @return {@link Op<T>}
     */
    @SuppressWarnings("unchecked")
    private static <T> Op<T> empty() {
        return (Op<T>) EMPTY;
    }

    /**
     * 返回一个元素不为空的 {@link Op<T>}
     *
     * @param value {@link T}
     * @param <T>   元素类型
     * @return {@link Op<T>}
     **/
    @SuppressWarnings("rawtypes")
    public static <T> Op<T> of(T value) {
        Objects.requireNonNull(value);
        if (value instanceof CharSequence sequence && sequence.toString().trim().isEmpty()) {
            throw new KitException();
        }
        if (value instanceof Map map && map.isEmpty()) {
            throw new KitException();
        }
        if (value instanceof Collection coll && coll.isEmpty()) {
            throw new KitException();
        }
        return new Op<>(value);
    }

    /**
     * 返回一个元素可能为空的 {@link Op<T>}
     *
     * @param value {@link T}
     * @param <T>   元素类型
     * @return {@link Op<T>}
     **/
    @SuppressWarnings("rawtypes")
    public static <T> Op<T> ofNullable(T value) {
        if (Objects.isNull(value) || (value instanceof Map map && map.isEmpty())
            || (value instanceof CharSequence sequence && sequence.toString().trim().isEmpty())
            || (value instanceof Collection coll && coll.isEmpty())) {
            return new Op<>();
        }
        return Op.of(value);
    }

    /**
     * 根据 {@link Optional<T>} 构造 {@link  Op<T>}
     *
     * @param optional {@link Optional<T>}
     * @param <T>      元素类型
     * @return 返回一个元素可能为空的 {@link Op<T>}
     */
    @SuppressWarnings("OptionalUsedAsFieldOrParameterType")
    public static <T> Op<T> ofOptional(Optional<T> optional) {
        return ofNullable(optional.orElse(null));
    }

    /**
     * 尝试获取一个 {@link Op<T>}
     *
     * @param supplier {@link SerSupplier}
     * @param <T>      元素类型
     * @return {@link Op<T>}
     **/
    public static <T> Op<T> ofTry(SerSupplier<T> supplier) {
        try {
            return Op.ofNullable(supplier.getting());
        } catch (Exception e) {
            return new Op<>(e);
        }
    }

    /**
     * 返回元素,取不到则为 null
     * 注意！！！此处和{@link Optional#get()}
     * 不同的一点是本方法并不会抛出{@link NoSuchElementException}
     *
     * @return {@link T}
     */
    public T get() {
        return this.value;
    }

    /**
     * 获取异常
     * 当调用 {@link #ofTry}时,异常信息不会抛出,而是保存,调用此方法获取抛出的异常
     *
     * @return {@link Exception}
     */
    public Exception getException() {
        return this.exception;
    }

    /**
     * 是否失败
     * 当调用 {@link #ofTry)}时,抛出异常则表示失败
     *
     * @return {@link Boolean}
     */
    public boolean isFail() {
        return Objects.nonNull(this.exception);
    }

    /**
     * 判断元素是否不存在
     * 不存在为 true 否则为 false
     *
     * @return {@link Boolean}
     */
    @SuppressWarnings("rawtypes")
    public boolean isEmpty() {
        if (this.value instanceof CharSequence sequence) {
            return sequence.toString().trim().isEmpty();
        }
        if (this.value instanceof Map map) {
            return map.isEmpty();
        }
        if (this.value instanceof Collection coll) {
            return coll.isEmpty();
        }
        return Objects.isNull(this.value);
    }

    /**
     * 判断元素是否存在
     * 存在为 true 否则为 false
     *
     * @return {@link Boolean}
     */
    @SuppressWarnings("rawtypes")
    public boolean isPresent() {
        if (this.value instanceof CharSequence sequence) {
            return !sequence.toString().trim().isEmpty();
        }
        if (this.value instanceof Map map) {
            return !map.isEmpty();
        }
        if (this.value instanceof Collection coll) {
            return !coll.isEmpty();
        }
        return Objects.nonNull(this.value);
    }

    /**
     * 如果元素存在,就执行传入的操作
     *
     * @param consumer {@link SerConsumer} 元素存在时执行的操作
     **/
    public void ifPresent(SerConsumer<? super T> consumer) {
        if (isPresent()) {
            consumer.accept(this.value);
        }
    }

    /**
     * 如果元素存在,就执行传入的第一个操作, 否则执行第二个操作
     *
     * @param consumer {@link SerConsumer} 元素存在时执行的操作
     * @param runnable {@link SerRunnable} 元素不存在时执行的操作
     */
    public void ifPresentOrElse(SerConsumer<T> consumer, SerRunnable runnable) {
        if (isPresent()) {
            consumer.accept(this.value);
        } else {
            runnable.run();
        }
    }

    /**
     * 判断元素存在并且与给定的条件是否满足
     * 如果满足条件则返回本身
     * 不满足条件或者元素本身为空时返回一个返回一个空的{@link Op<T>}
     *
     * @param predicate {@link SerPredicate} 给定的条件
     * @return {@link Op<T>}
     * @throws NullPointerException 如果给定的操作为null, 抛出 {@link NullPointerException}
     */
    public Op<T> filter(SerPredicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        return (isEmpty() || predicate.test(this.value)) ? this : empty();
    }

    /**
     * 如果元素存在,就执行传入的操作{@link SerFunction#apply}
     * 并返回一个包含了该操作返回元素的{@link Op<T>}
     * 如果不存在,返回一个空的{@link Op<T>}
     *
     * @param mapper {@link SerFunction} 元素存在时执行的操作
     * @param <U>    {@link  U} 返回元素类型
     * @return Op<U> 包含了该操作返回元素的{@link Op<T>}
     * @throws NullPointerException 如果给定的操作为null, 抛出 {@link NullPointerException}
     */
    public <U> Op<U> map(SerFunction<? super T, ? extends U> mapper) {
        Objects.requireNonNull(mapper);
        return isEmpty() ? empty() : Op.ofNullable(mapper.apply(this.value));
    }


    /**
     * 如果元素存在,就执行传入的操作({@link SerFunction#apply})并返回该操作返回元素
     * 如果不存在,返回一个空的{@link Op<T>}
     * 和 {@link Op#map}的区别为 传入的操作返回元素必须为 {@link Op<T>}
     *
     * @param mapper {@link SerFunction<T>} 元素存在时执行的操作
     * @param <U>    {@link  U} 返回元素类型
     * @return 包含了该操作返回元素的{@link Op<T>}
     * @throws NullPointerException 如果给定的操作为null, 抛出 {@link NullPointerException}
     */
    @SuppressWarnings("unchecked")
    public <U> Op<U> flatMap(SerFunction<? super T, ? extends Op<? extends U>> mapper) {
        Objects.requireNonNull(mapper);
        if (isEmpty()) {
            return empty();
        } else {
            return Objects.requireNonNull((Op<U>) mapper.apply(this.value));
        }
    }

    /**
     * 如果元素存在,就执行传入的操作({@link SerFunction#apply})并返回该操作返回元素
     * 如果不存在,返回一个空的{@link Op<T>}
     * 和 {@link Op#map}的区别为 传入的操作返回元素必须为 {@link Optional}
     *
     * @param mapper {@link SerFunction<T>} 元素存在时执行的操作
     * @param <U>    {@link  U} 返回元素类型
     * @return 包含了该操作返回元素的{@link Op<T>}
     * 如果不存在,返回一个空的{@link Op<T>}
     * @throws NullPointerException 如果给定的操作为null, 抛出 {@link NullPointerException}
     */
    public <U> Op<U> flattedMap(SerFunction<? super T, Optional<? extends U>> mapper) {
        Objects.requireNonNull(mapper);
        return isEmpty() ? empty() : of(mapper.apply(this.value).orElse(null));
    }

    /**
     * 如果元素存在,就执行对应的操作,并返回本身
     * 如果不存在,返回一个空的{@link Op<T>}
     *
     * @param action {@link SerConsumer <T>} 元素存在时执行的操作
     * @return {@link Op<T>}
     * @throws NullPointerException 如果给定的操作为null, 抛出 {@link NullPointerException}
     */
    public Op<T> peek(SerConsumer<T> action) {
        Objects.requireNonNull(action);
        if (isEmpty()) {
            return empty();
        }
        action.accept(this.value);
        return this;
    }

    /**
     * 如果元素存在,就执行对应的操作,并返回本身
     * 如果不存在,返回一个空的{@link Op<T>}
     *
     * @param actions {@link SerConsumer<T>...}元素存在时执行的操作
     * @return {@link Op<T>}
     * @throws NullPointerException 如果给定的操作为null, 抛出 {@link NullPointerException}
     */
    @SafeVarargs
    public final Op<T> peeks(SerConsumer<T>... actions) throws NullPointerException {
        return peek(Stream.of(Objects.requireNonNull(actions)).reduce(SerConsumer::andThen).orElseGet(() -> o -> {
        }));
    }

    /**
     * 如果元素存在,就返回本身,如果不存在,则使用传入的操作执行后获得的 {@link Op<T>}
     *
     * @param supplier {@link SerSupplier} 不存在时的操作
     * @return 如果元素存在, 就返回本身, 如果不存在, 则使用传入的函数执行后获得的 {@link Op<T>}
     * @throws NullPointerException 如果传入的操作为空,或者传入的操作执行后返回元素为空,则抛出 {@link NullPointerException}
     */
    @SuppressWarnings("unchecked")
    public Op<T> or(SerSupplier<? extends Op<? extends T>> supplier) {
        Objects.requireNonNull(supplier);
        return isPresent() ? this : (Op<T>) supplier.get();
    }

    /**
     * 如果元素存在
     * 则返回该元素,否则返回传入的元素
     *
     * @param other 元素为空时返回的元素
     * @return {@link T}
     */
    public T orElse(T other) {
        return isPresent() ? this.value : other;
    }

    /**
     * 如果元素存在,则返回该元素,否则执行传入的操作
     *
     * @param action {@link R}元素不存在时执行的操作
     * @return 如果元素存在, 则返回该元素, 否则执行传入的操作
     * @throws NullPointerException 如果元素不存在,并且传入的操作为 {@link null}
     */
    public <R extends SerRunnable> T orElseRun(R action) {
        if (isPresent()) {
            return this.value;
        } else {
            action.run();
            return null;
        }
    }

    /**
     * 异常则返回另一个可选元素
     *
     * @param other {@link T} 可选元素
     * @return 如果未发生异常, 则返回该元素, 否则返回传入的元素
     */
    public T failOrElse(T other) {
        return isFail() ? other : this.value;
    }

    /**
     * 如果元素存在,则返回该元素,否则返回传入的操作执行后的返回元素
     *
     * @param supplier {@link SerSupplier}元素不存在时需要执行的操作,返回一个类型与 元素类型 相同的元素
     * @return 如果元素存在, 则返回该元素, 否则返回传入的操作执行后的返回元素
     * @throws NullPointerException 如果之不存在,并且传入的操作为空,则抛出 {@link NullPointerException}
     */
    public T orElseGet(SerSupplier<? extends T> supplier) {
        return isPresent() ? this.value : supplier.get();
    }

    /**
     * 如果元素存在,则返回该元素,否则抛出 {@link NoSuchElementException}
     *
     * @return 返回一个不为null 的元素
     * @throws NoSuchElementException 如果元素不存在则抛出该异常
     */
    public T orElseThrow() {
        return orElseThrow(() -> new NoSuchElementException("No value present"));
    }

    /**
     * 如果元素存在,则返回该元素,否则执行传入的操作,获取异常类型的返回元素并抛出
     *
     * @param supplier {@link SerSupplier} 元素不存在时执行的操作
     * @return 不能为空的元素
     * @throws X                    如果元素不存在
     * @throws NullPointerException 如果元素不存在并且 传入的操作为 {@link null}或者操作执行后的返回元素为{@link null}
     */
    public <X extends Throwable> T orElseThrow(SerSupplier<? extends X> supplier) throws X {
        if (isPresent()) {
            return this.value;
        } else {
            throw supplier.get();
        }
    }

    /**
     * 如果元素存在,就返回一个包含该元素 {@link Stream},
     * 否则返回一个空元素的 {@link Stream}
     *
     * @return 返回一个包含该元素的 {@link Stream}或空的 {@link Stream}
     */
    public St<T> stream() {
        return isEmpty() ? St.empty() : St.of(this.value);
    }

    /**
     * 转换为 {@link Optional}对象
     *
     * @return {@link Optional}对象
     */
    public Optional<T> toOptional() {
        return Optional.ofNullable(this.value);
    }

    /**
     * 判断传入参数是否与 {@link Op<T>} 相等
     * 相等 true 不相等 false
     *
     * @param obj {@link Object}
     * @return {@link Boolean}
     **/
    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof final Op<?> other)) {
            return false;
        }
        return Objects.equals(this.value, other.value);
    }

    /**
     * hashCode
     *
     * @return int
     **/
    @Override
    public int hashCode() {
        return Objects.hashCode(this.value);
    }

    /**
     * toString
     *
     * @return java.lang.String
     **/
    @Override
    public String toString() {
        return isEmpty() ? Objects.requireNonNull(this.value).toString() : null;
    }

}
