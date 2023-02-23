package io.github.yeluod.util;

import io.github.yeluod.util.function.SerFunction;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.*;
import java.util.stream.*;

/**
 * St<T>
 *
 * @author W.d
 * @since 2022/9/19 09:51
 **/
@SuppressWarnings("unused")
public class St<T> implements Stream<T>, Iterable<T> {

    /**
     * 代表不存在的下标, 一般用于并行流的下标, 或者未找到元素时的下标
     */
    private static final int NOT_FOUND_INDEX = -1;

    protected Stream<T> stream;

    public St(Stream<T> stream) {
        this.stream = stream;
    }

    /**
     * 建造器
     *
     * @return a stream builder
     */
    public static <T> Builder<T> builder() {
        return new Builder<>() {
            private final Builder<T> builder = St.builder();

            @Override
            public void accept(T t) {
                builder.accept(t);
            }

            @Override
            public St<T> build() {
                return new St<>(builder.build());
            }
        };
    }

    /**
     * 返回空的串行流
     *
     * @return {@link St<T>}
     */
    public static <T> St<T> empty() {
        return new St<>(Stream.empty());
    }

    /**
     * 返回包含单个元素的串行流
     *
     * @param t   单个元素
     * @param <T> 元素类型
     * @return 包含单个元素的串行流
     */
    public static <T> St<T> of(T t) {
        return new St<>(Stream.of(t));
    }

    /**
     * 通过传入的{@link Stream}创建流
     *
     * @param stream {@link Stream}
     * @return {@link St}
     */
    public static <T> St<T> of(Stream<T> stream) {
        return new St<>(Objects.requireNonNull(stream));
    }

    /**
     * 返回包含指定元素的串行流
     *
     * @param values {@link T...} 元素数组
     * @return {@link St}
     */
    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> St<T> of(T... values) {
        return (Objects.isNull(values) || values.length == 0) ? empty() : new St<>(Stream.of(values));
    }

    /**
     * 通过实现了{@link Iterable}接口的对象创建串行流
     *
     * @param iterable 实现了{@link Iterable}接口的对象
     * @return {@link St}
     */
    public static <T> St<T> of(Iterable<T> iterable) {
        return of(iterable, false);
    }

    /**
     * 通过传入的{@link Iterable}创建流
     *
     * @param iterable {@link Iterable}
     * @param parallel 是否并行
     * @return {@link St}
     */
    public static <T> St<T> of(Iterable<T> iterable, boolean parallel) {
        return Op.of(iterable)
                .map(Iterable::spliterator)
                .map(spliterator -> StreamSupport.stream(spliterator, parallel))
                .map(St::new)
                .orElseGet(St::empty);
    }

    /**
     * 返回无限有序流
     * 该流由 初始值 以及执行 迭代函数 进行迭代获取到元素
     *
     * @param seed 初始值
     * @param f    用上一个元素作为参数执行并返回一个新的元素
     * @return {@link St}
     */
    public static <T> St<T> iterate(final T seed, final UnaryOperator<T> f) {
        return new St<>(Stream.iterate(seed, f));
    }

    /**
     * 返回无限有序流
     * 该流由 初始值 然后判断条件 以及执行 迭代函数 进行迭代获取到元素
     *
     * @param seed    初始值
     * @param hasNext 条件值
     * @param next    用上一个元素作为参数执行并返回一个新的元素
     * @return {@link St}
     */
    public static <T> St<T> iterate(T seed, Predicate<? super T> hasNext, UnaryOperator<T> next) {
        Objects.requireNonNull(next);
        Objects.requireNonNull(hasNext);
        Spliterator<T> spliterator = new Spliterators.AbstractSpliterator<T>(Long.MAX_VALUE,
                Spliterator.ORDERED | Spliterator.IMMUTABLE) {
            T prev;
            boolean started;
            boolean finished;

            @Override
            public boolean tryAdvance(Consumer<? super T> action) {
                Objects.requireNonNull(action);
                if (finished) {
                    return false;
                }
                T t;
                if (started) {
                    t = next.apply(prev);
                } else {
                    t = seed;
                    started = true;
                }
                if (!hasNext.test(t)) {
                    prev = null;
                    finished = true;
                    return false;
                }
                prev = t;
                action.accept(prev);
                return true;
            }

            @Override
            public void forEachRemaining(Consumer<? super T> action) {
                Objects.requireNonNull(action);
                if (finished) {
                    return;
                }
                finished = true;
                T t = started ? next.apply(prev) : seed;
                prev = null;
                while (hasNext.test(t)) {
                    action.accept(t);
                    t = next.apply(t);
                }
            }
        };
        return new St<>(StreamSupport.stream(spliterator, false));
    }

    /**
     * 返回无限串行无序流
     * 其中每一个元素都由给定的{@link Supplier}生成
     * 适用场景在一些生成常量流、随机元素等
     *
     * @param s 用来生成元素的 {@link Supplier}
     * @return {@link St}
     */
    public static <T> St<T> generate(Supplier<T> s) {
        return new St<>(Stream.generate(s));
    }

    /**
     * 创建一个惰性拼接流，其元素是第一个流的所有元素，然后是第二个流的所有元素。
     * 如果两个输入流都是有序的，则结果流是有序的，如果任一输入流是并行的，则结果流是并行的。
     * 当结果流关闭时，两个输入流的关闭处理程序都会被调用。
     *
     * @param a 第一个流
     * @param b 第二个流
     * @return {@link St}
     */
    public static <T> St<T> concat(Stream<? extends T> a, Stream<? extends T> b) {
        return new St<>(Stream.concat(a, b));
    }

    /**
     * 拆分字符串，转换为串行流
     *
     * @param str   字符串
     * @param regex 正则
     * @return {@link St<String>}
     */
    public static St<String> split(CharSequence str, String regex) {
        return Op.ofNullable(str).map(CharSequence::toString).map(s -> s.split(regex)).map(St::of).orElseGet(St::empty);
    }

    /**
     * 过滤元素，返回与指定断言匹配的元素组成的流
     *
     * @param predicate 断言
     * @return {@link St}
     */
    @Override
    public St<T> filter(Predicate<? super T> predicate) {
        return new St<>(stream.filter(predicate));
    }

    /**
     * 过滤元素，返回与 指定操作结果 匹配 指定值 的元素组成的流
     *
     * @param mapper 操作
     * @param value  用来匹配的值
     * @return {@link St}
     */
    public <R> St<T> filter(SerFunction<? super T, ? extends R> mapper, R value) {
        Objects.requireNonNull(mapper);
        return filter(item -> Objects.equals(Op.of(item).map(mapper).get(), value));
    }

    /**
     * 过滤元素，返回与指定断言匹配的元素组成的流，断言带下标，并行流时下标永远为-1
     *
     * @param predicate 断言
     * @return {@link St}
     */
    public St<T> filterIdx(BiPredicate<? super T, Integer> predicate) {
        Objects.requireNonNull(predicate);
        if (isParallel()) {
            return filter(e -> predicate.test(e, NOT_FOUND_INDEX));
        } else {
            AtomicInteger index = new AtomicInteger(NOT_FOUND_INDEX);
            return filter(e -> predicate.test(e, index.incrementAndGet()));
        }
    }

    /**
     * 过滤掉空元素
     *
     * @return {@link St}
     */
    public St<T> nonNull() {
        return new St<>(stream.filter(Objects::nonNull));
    }

    /**
     * 返回与指定函数将元素作为参数执行的结果组成的流
     *
     * @param mapper 指定的函数
     * @return {@link St}
     */
    @Override
    public <R> St<R> map(Function<? super T, ? extends R> mapper) {
        return new St<>(stream.map(mapper));
    }

    /**
     * 返回与指定函数将元素作为参数执行的结果组成的流，操作带下标，并行流时下标永远为-1
     *
     * @param mapper 指定的函数
     * @return {@link St}
     */
    public <R> St<R> mapIdx(BiFunction<? super T, Integer, ? extends R> mapper) {
        Objects.requireNonNull(mapper);
        if (isParallel()) {
            return map(e -> mapper.apply(e, NOT_FOUND_INDEX));
        } else {
            AtomicInteger index = new AtomicInteger(NOT_FOUND_INDEX);
            return map(e -> mapper.apply(e, index.incrementAndGet()));
        }
    }

    /**
     * 和{@link St#map(Function)}一样，只不过函数的返回值必须为int类型
     *
     * @param mapper 返回值为int类型的函数
     * @return {@link IntStream}
     */
    @Override
    public IntStream mapToInt(ToIntFunction<? super T> mapper) {
        return stream.mapToInt(mapper);
    }

    /**
     * 和{@link St#map(Function)}一样，只不过函数的返回值必须为long类型
     *
     * @param mapper 返回值为long类型的函数
     * @return {@link LongStream}
     */
    @Override
    public LongStream mapToLong(ToLongFunction<? super T> mapper) {
        return stream.mapToLong(mapper);
    }

    /**
     * 和{@link St#map(Function)}一样，只不过函数的返回值必须为double类型
     *
     * @param mapper 返回值为double类型的函数
     * @return {@link DoubleStream}
     */
    @Override
    public DoubleStream mapToDouble(ToDoubleFunction<? super T> mapper) {
        return stream.mapToDouble(mapper);
    }

    /**
     * 扩散流操作，可能影响流元素个数，将原有流元素执行mapper操作，返回多个流所有元素组成的流
     *
     * @param mapper 操作，返回可迭代对象
     * @return {@link St}
     */
    public <R> St<R> flat(SerFunction<? super T, ? extends Iterable<? extends R>> mapper) {
        Objects.requireNonNull(mapper);
        return flatMap(w -> Op.of(w).map(mapper).map(St::of).orElseGet(St::empty));
    }

    /**
     * 扩散流操作，可能影响流元素个数，将原有流元素执行mapper操作，返回多个流所有元素组成的流
     *
     * @param mapper 操作，返回流
     * @return {@link St}
     */
    @Override
    public <R> St<R> flatMap(Function<? super T, ? extends Stream<? extends R>> mapper) {
        return new St<>(stream.flatMap(mapper));
    }

    /**
     * 扩散流操作，可能影响流元素个数，将原有流元素执行mapper操作，返回多个流所有元素组成的流，操作带下标，并行流时下标永远为-1
     *
     * @param mapper 操作，返回流
     * @return {@link St}
     */
    public <R> St<R> flatIdx(BiFunction<? super T, Integer, ? extends Stream<? extends R>> mapper) {
        Objects.requireNonNull(mapper);
        if (isParallel()) {
            return flatMap(e -> mapper.apply(e, NOT_FOUND_INDEX));
        } else {
            AtomicInteger index = new AtomicInteger(NOT_FOUND_INDEX);
            return flatMap(e -> mapper.apply(e, index.incrementAndGet()));
        }
    }

    /**
     * 扩散流操作，可能影响流元素个数，将原有流元素执行mapper操作，返回多个流所有元素组成的流
     *
     * @param mapper 操作，返回IntStream
     * @return {@link IntStream}
     */
    @Override
    public IntStream flatMapToInt(Function<? super T, ? extends IntStream> mapper) {
        return stream.flatMapToInt(mapper);
    }

    /**
     * 扩散流操作，可能影响流元素个数，将原有流元素执行mapper操作，返回多个流所有元素组成的流
     *
     * @param mapper 操作，返回LongStream
     * @return {@link LongStream}
     */
    @Override
    public LongStream flatMapToLong(Function<? super T, ? extends LongStream> mapper) {
        return stream.flatMapToLong(mapper);
    }

    /**
     * 扩散流操作，可能影响流元素个数，将原有流元素执行mapper操作，返回多个流所有元素组成的流
     *
     * @param mapper 操作，返回DoubleStream
     * @return {@link DoubleStream}
     */
    @Override
    public DoubleStream flatMapToDouble(Function<? super T, ? extends DoubleStream> mapper) {
        return stream.flatMapToDouble(mapper);
    }

    /**
     * 扩散流操作，可能影响流元素个数，将原有流元素执行mapper操作，返回多个流所有元素组成的流，操作带一个方法，调用该方法可增加元素
     *
     * @param mapper 操作，返回流
     * @return {@link St}
     */
    @Override
    public <R> St<R> mapMulti(BiConsumer<? super T, ? super Consumer<R>> mapper) {
        Objects.requireNonNull(mapper);
        return flatMap(e -> {
            Builder<R> buffer = St.builder();
            mapper.accept(e, buffer);
            return buffer.build();
        });
    }

    /**
     * 返回一个具有去重特征的流 非并行流(顺序流)下对于重复元素，保留遇到顺序中最先出现的元素，并行流情况下不能保证具体保留哪一个
     *
     * @return {@link St}
     */
    @Override
    public St<T> distinct() {
        return new St<>(stream.distinct());
    }

    /**
     * 返回一个元素按自然顺序排序的流
     * 如果此流的元素不是{@link Comparable} ，则在执行终端操作时可能会抛出 {@link ClassCastException}
     * 对于顺序流，排序是稳定的。 对于无序流，没有稳定性保证。
     *
     * @return {@link St}
     */
    @Override
    public St<T> sorted() {
        return new St<>(stream.sorted());
    }

    /**
     * 返回一个元素按指定的{@link Comparator}排序的流
     * 如果此流的元素不是{@link Comparable} ，则在执行终端操作时可能会抛出{@link ClassCastException}
     * 对于顺序流，排序是稳定的。 对于无序流，没有稳定性保证。
     *
     * @param comparator 排序规则
     * @return {@link St}
     */
    @Override
    public St<T> sorted(Comparator<? super T> comparator) {
        return new St<>(stream.sorted(comparator));
    }

    /**
     * 返回与指定函数将元素作为参数执行后组成的流。
     *
     * @param action 指定的函数
     * @return {@link St}
     */
    @Override
    @SuppressWarnings("all")
    public St<T> peek(Consumer<? super T> action) {
        return new St<>(stream.peek(action));
    }

    /**
     * 打印
     *
     * @return {@link St}
     **/
    @SuppressWarnings("all")
    public St<T> println() {
        return peek(System.out::println);
    }

    /**
     * 返回截取后面一些元素的流
     *
     * @param maxSize 元素截取后的个数
     * @return {@link St}
     */
    @Override
    public St<T> limit(long maxSize) {
        return new St<>(stream.limit(maxSize));
    }

    /**
     * 返回丢弃前面n个元素后的剩余元素组成的流，如果当前元素个数小于n，则返回一个元素为空的流
     *
     * @param n 需要丢弃的元素个数
     * @return {@link St}
     */
    @Override
    public St<T> skip(long n) {
        return new St<>(stream.skip(n));
    }

    /**
     * 对流里面的每一个元素执行一个操作
     *
     * @param action 操作
     */
    @Override
    public void forEach(Consumer<? super T> action) {
        stream.forEach(action);
    }

    /**
     * 对流里面的每一个元素按照顺序执行一个操作
     *
     * @param action 操作
     */
    @Override
    public void forEachOrdered(Consumer<? super T> action) {
        stream.forEachOrdered(action);
    }

    /**
     * 对流里面的每一个元素执行一个操作，操作带下标，并行流时下标永远为-1
     *
     * @param action 操作
     */
    public void forEachIdx(BiConsumer<? super T, Integer> action) {
        Objects.requireNonNull(action);
        if (isParallel()) {
            stream.forEach(e -> action.accept(e, NOT_FOUND_INDEX));
        } else {
            AtomicInteger index = new AtomicInteger(NOT_FOUND_INDEX);
            stream.forEach(e -> action.accept(e, index.incrementAndGet()));
        }
    }

    /**
     * 返回一个包含此流元素的数组
     *
     * @return {@link Object[]}
     */
    @Override
    public Object[] toArray() {
        return stream.toArray();
    }

    /**
     * 返回一个包含此流元素的指定的数组
     *
     * @param generator 这里的IntFunction的参数是元素的个数，返回值为数组类型
     * @return 包含此流元素的指定的数组
     */
    @Override
    public <A> A[] toArray(IntFunction<A[]> generator) {
        return stream.toArray(generator);
    }

    /**
     * 对元素进行聚合，并返回聚合后的值
     * 求和、最小值、最大值、平均值和转换成一个String字符串均为聚合操作
     *
     * @param identity    初始值，还用于限定泛型
     * @param accumulator 你想要的聚合操作
     * @return {@link St}
     */
    @Override
    public T reduce(T identity, BinaryOperator<T> accumulator) {
        return stream.reduce(identity, accumulator);
    }

    /**
     * 对元素进行聚合，并返回聚合后用 {@link Optional}包裹的值
     *
     * @param accumulator 你想要的聚合操作
     * @return {@link Optional}
     */
    @Override
    public Optional<T> reduce(BinaryOperator<T> accumulator) {
        return stream.reduce(accumulator);
    }

    /**
     * 对元素进行聚合，并返回聚合后的值，并行流时聚合拿到的初始值不稳定
     *
     * @param identity    初始值
     * @param accumulator 累加器，具体为你要的聚合操作
     * @param combiner    用于并行流时组合多个结果
     * @return 聚合操作的结果
     */
    @Override
    public <U> U reduce(U identity, BiFunction<U, ? super T, U> accumulator, BinaryOperator<U> combiner) {
        return stream.reduce(identity, accumulator, combiner);
    }

    /**
     * 对元素进行收集，并返回收集后的容器
     *
     * @param supplier    提供初始值的函数式接口，一般可以传入构造参数
     * @param accumulator 具体收集操作
     * @param combiner    用于并行流时组合多个结果
     * @return 收集后的容器
     */
    @Override
    public <R> R collect(Supplier<R> supplier, BiConsumer<R, ? super T> accumulator, BiConsumer<R, R> combiner) {
        return stream.collect(supplier, accumulator, combiner);
    }

    /**
     * 对元素进行收集，并返回收集后的元素
     *
     * @param collector 收集器
     * @return 收集后的容器
     */
    @Override
    public <R, A> R collect(Collector<? super T, A, R> collector) {
        return stream.collect(collector);
    }

    /**
     * 获取最小值
     *
     * @param comparator 一个用来比较大小的比较器{@link Comparator}
     * @return {@link Optional}
     */
    @Override
    public Optional<T> min(Comparator<? super T> comparator) {
        return stream.min(comparator);
    }

    /**
     * 获取最大值
     *
     * @param comparator 一个用来比较大小的比较器{@link Comparator}
     * @return {@link Optional}
     */
    @Override
    public Optional<T> max(Comparator<? super T> comparator) {
        return stream.max(comparator);
    }

    /**
     * 返回流元素个数
     *
     * @return {@link Long}
     */
    @Override
    public long count() {
        return stream.count();
    }

    /**
     * 判断是否有任何一个元素满足给定断言
     *
     * @param predicate 断言
     * @return {@link Boolean}
     */
    @Override
    public boolean anyMatch(Predicate<? super T> predicate) {
        return stream.anyMatch(predicate);
    }

    /**
     * 判断是否所有元素满足给定断言
     *
     * @param predicate 断言
     * @return {@link Boolean}
     */
    @Override
    public boolean allMatch(Predicate<? super T> predicate) {
        return stream.allMatch(predicate);
    }

    /**
     * 判断是否没有元素满足给定断言
     *
     * @param predicate 断言
     * @return {@link Boolean}
     */
    @Override
    public boolean noneMatch(Predicate<? super T> predicate) {
        return stream.noneMatch(predicate);
    }

    /**
     * 获取第一个元素
     *
     * @return {@link Optional}
     */
    @Override
    public Optional<T> findFirst() {
        return stream.findFirst();
    }

    /**
     * 获取与给定断言匹配的第一个元素
     *
     * @param predicate 断言
     * @return {@link Optional}
     */
    public Optional<T> findFirst(Predicate<? super T> predicate) {
        return stream.filter(predicate).findFirst();
    }

    /**
     * 获取与给定断言匹配的第一个元素的下标，并行流下标永远为-1
     *
     * @param predicate 断言
     * @return {@link Integer}
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    public Integer findFirstIdx(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (isParallel()) {
            return NOT_FOUND_INDEX;
        } else {
            AtomicInteger index = new AtomicInteger(NOT_FOUND_INDEX);
            stream.filter(e -> {
                index.incrementAndGet();
                return predicate.test(e);
            }).findFirst();
            return index.get();
        }
    }

    /**
     * 获取最后一个元素
     *
     * @return {@link Optional}
     */
    public Optional<T> findLast() {
        if (isParallel()) {
            return Optional.of(toList()).filter(l -> !l.isEmpty()).map(l -> l.get(l.size() - 1));
        } else {
            AtomicReference<T> last = new AtomicReference<>(null);
            forEach(last::set);
            return Optional.ofNullable(last.get());
        }
    }

    /**
     * 获取与给定断言匹配的最后一个元素
     *
     * @param predicate 断言
     * @return {@link Optional}
     */
    public Optional<T> findLast(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (isParallel()) {
            return filter(predicate).findLast();
        } else {
            AtomicReference<T> last = new AtomicReference<>(null);
            forEach(e -> {
                if (predicate.test(e)) {
                    last.set(e);
                }
            });
            return Optional.ofNullable(last.get());
        }
    }

    /**
     * 获取与给定断言匹配的最后一个元素的下标，并行流下标永远为-1
     *
     * @param predicate 断言
     * @return {@link Integer}
     */
    public Integer findLastIdx(Predicate<? super T> predicate) {
        Objects.requireNonNull(predicate);
        if (isParallel()) {
            return NOT_FOUND_INDEX;
        } else {
            AtomicInteger idxRef = new AtomicInteger(NOT_FOUND_INDEX);
            forEachIdx((e, i) -> {
                if (predicate.test(e)) {
                    idxRef.set(i);
                }
            });
            return idxRef.get();
        }
    }

    /**
     * 反转顺序
     *
     * @return {@link St}
     */
    public St<T> reverse() {
        List<T> list = toList();
        Collections.reverse(list);
        return of(list, isParallel());
    }

    /**
     * 考虑性能，随便取一个，这里不是随机取一个，是随便取一个
     *
     * @return {@link Optional}
     */
    @Override
    public Optional<T> findAny() {
        return stream.findAny();
    }

    /**
     * 返回流的迭代器
     *
     * @return {@link Iterator}
     */
    @Override
    public Iterator<T> iterator() {
        return stream.iterator();
    }

    /**
     * 返回流的拆分器
     *
     * @return {@link Spliterator}
     */
    @Override
    public Spliterator<T> spliterator() {
        return stream.spliterator();
    }

    /**
     * 返回一个串行流，该方法可以将并行流转换为串行流s
     *
     * @return {@link St}
     */
    @Override
    public St<T> sequential() {
        //noinspection ResultOfMethodCallIgnored
        stream.sequential();
        return this;
    }

    /**
     * 将流转换为并行
     *
     * @return {@link St}
     */
    @Override
    public St<T> parallel() {
        //noinspection DataFlowIssue
        this.stream = stream.parallel();
        return this;
    }

    /**
     * 返回流的并行状态
     *
     * @return {@link Boolean}
     */
    @Override
    public boolean isParallel() {
        return stream.isParallel();
    }

    /**
     * 返回一个无序流(无手动排序)
     *
     * @return {@link St}
     */
    @Override
    public St<T> unordered() {
        return new St<>(stream.unordered());
    }

    /**
     * 在流关闭时执行操作
     *
     * @param closeHandler 在流关闭时执行的操作
     * @return {@link St}
     */
    @Override
    public St<T> onClose(Runnable closeHandler) {
        this.stream = stream.onClose(closeHandler);
        return this;
    }

    /**
     * 关闭流
     *
     * @see AutoCloseable#close()
     */
    @Override
    public void close() {
        stream.close();
    }

    /**
     * hashcode
     *
     * @return {@link Integer}
     */
    @Override
    public int hashCode() {
        return stream.hashCode();
    }

    /**
     * equals
     *
     * @param obj 对象
     * @return {@link Boolean}
     */
    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Stream) {
            return stream.equals(obj);
        }
        return false;
    }

    /**
     * toString
     *
     * @return string
     */
    @Override
    public String toString() {
        return stream.toString();
    }

    /**
     * 转换成集合
     *
     * @param collectionFactory 集合工厂(可以是集合构造器)
     * @return 集合
     */
    public <C extends Collection<T>> C toColl(Supplier<C> collectionFactory) {
        return collect(Collectors.toCollection(collectionFactory));
    }

    /**
     * 转换为HashSet
     *
     * @return {@link Set}
     */
    public Set<T> toSet() {
        return collect(Collectors.toSet());
    }

    /**
     * 与给定的可迭代对象转换成map，key为现有元素，value为给定可迭代对象迭代的元素<br>
     * 至少包含全部的key，如果对应位置上的value不存在，则为null
     *
     * @param other 可迭代对象
     * @return {@link Map}
     */
    public <R> Map<T, R> toZip(Iterable<R> other) {
        // value对象迭代器
        final Iterator<R> iterator = Op.of(other).map(Iterable::iterator).orElseGet(Collections::emptyIterator);
        if (isParallel()) {
            List<T> keyList = toList();
            final Map<T, R> map = new HashMap<>(keyList.size());
            for (T key : keyList) {
                map.put(key, iterator.hasNext() ? iterator.next() : null);
            }
            return map;
        } else {
            return toMap(Function.identity(), e -> iterator.hasNext() ? iterator.next() : null);
        }
    }

    /**
     * 返回拼接后的字符串
     *
     * @return {@link String}
     */
    public String join() {
        return join("");
    }

    /**
     * 返回拼接后的字符串
     *
     * @param delimiter 分隔符
     * @return {@link String}
     */
    public String join(CharSequence delimiter) {
        return join(delimiter, "", "");
    }

    /**
     * 返回拼接后的字符串
     *
     * @param delimiter 分隔符
     * @param prefix    前缀
     * @param suffix    后缀
     * @return {@link String}
     */
    public String join(CharSequence delimiter,
                       CharSequence prefix,
                       CharSequence suffix) {
        return map(String::valueOf).collect(Collectors.joining(delimiter, prefix, suffix));
    }

    /**
     * 转换为map，key为给定操作执行后的返回值,value为当前元素
     *
     * @param keyMapper 指定的key操作
     * @return {@link Map}
     */
    public <K> Map<K, T> toMap(Function<? super T, ? extends K> keyMapper) {
        return toMap(keyMapper, Function.identity());
    }

    /**
     * 转换为map，key,value为给定操作执行后的返回值
     *
     * @param keyMapper   指定的key操作
     * @param valueMapper 指定value操作
     * @return {@link Map}
     */
    public <K, U> Map<K, U> toMap(Function<? super T, ? extends K> keyMapper,
                                  Function<? super T, ? extends U> valueMapper) {
        return toMap(keyMapper, valueMapper, (l, r) -> r);
    }


    /**
     * 转换为map，key,value为给定操作执行后的返回值
     *
     * @param keyMapper     指定的key操作
     * @param valueMapper   指定value操作
     * @param mergeFunction 合并操作
     * @return {@link Map}
     */
    public <K, U> Map<K, U> toMap(Function<? super T, ? extends K> keyMapper,
                                  Function<? super T, ? extends U> valueMapper,
                                  BinaryOperator<U> mergeFunction) {
        return toMap(keyMapper, valueMapper, mergeFunction, HashMap::new);
    }

    /**
     * 转换为map，key,value为给定操作执行后的返回值
     *
     * @param keyMapper     指定的key操作
     * @param valueMapper   指定value操作
     * @param mergeFunction 合并操作
     * @param mapSupplier   map工厂
     * @return {@link Map}
     */
    public <K, U, M extends Map<K, U>> M toMap(Function<? super T, ? extends K> keyMapper,
                                               Function<? super T, ? extends U> valueMapper,
                                               BinaryOperator<U> mergeFunction,
                                               Supplier<M> mapSupplier) {
        return collect(Collectors.toMap(keyMapper, valueMapper, mergeFunction, mapSupplier));
    }


    /**
     * 通过给定分组依据进行分组
     *
     * @param classifier 分组依据
     * @return {@link Map}
     */
    public <K> Map<K, List<T>> group(Function<? super T, ? extends K> classifier) {
        return group(classifier, Collectors.toList());
    }


    /**
     * 通过给定分组依据进行分组
     *
     * @param classifier 分组依据
     * @param downstream 下游操作
     * @return {@link Map}
     */
    public <K, A, D> Map<K, D> group(Function<? super T, ? extends K> classifier,
                                     Collector<? super T, A, D> downstream) {
        return group(classifier, HashMap::new, downstream);
    }

    /**
     * 通过给定分组依据进行分组
     *
     * @param classifier 分组依据
     * @param mapFactory 提供的map
     * @param downstream 下游操作
     * @return {@link Map}
     */
    public <K, D, A, M extends Map<K, D>> M group(Function<? super T, ? extends K> classifier,
                                                  Supplier<M> mapFactory,
                                                  Collector<? super T, A, D> downstream) {
        return collect(Collectors.groupingBy(classifier, mapFactory, downstream));
    }

    public interface Builder<T> extends Consumer<T> {

        /**
         * Adds an element to the stream being built.
         *
         * @param t the element to add
         * @throws IllegalStateException if the builder has already transitioned to
         *                               the built state
         */
        @Override
        void accept(T t);

        /**
         * Adds an element to the stream being built.
         *
         * @param t the element to add
         * @return {@link this} builder
         * @throws IllegalStateException if the builder has already transitioned to
         *                               the built state
         * @implSpec The default implementation behaves as if:
         * <pre>{@link
         *     #accept
         *     return this;
         * }</pre>
         */
        default Builder<T> add(T t) {
            accept(t);
            return this;
        }

        /**
         * Builds the stream, transitioning this builder to the built state.
         * An {@link IllegalStateException} is thrown if there are further attempts
         * to operate on the builder after it has entered the built state.
         *
         * @return the built stream
         * @throws IllegalStateException if the builder has already transitioned to
         *                               the built state
         */
        St<T> build();

    }
}
