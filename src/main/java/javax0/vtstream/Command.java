package javax0.vtstream;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Performs a stream command, like Map, Filter and so on.
 *
 * @param <T>
 * @param <R>
 */
abstract class Command<T, R> {
    static class Result<R> {
        final boolean isDeleted;
        final R result;

        Result(boolean isDeleted, R result) {
            this.isDeleted = isDeleted;
            this.result = result;
        }
    }

    public static final Result RESULT_DELETED = new Result(true, null);

    public abstract Result<R> execute(T t);

    public static class Filter<T> extends Command<T, T> {
        private final Predicate<T> predicate;

        public Filter(Predicate<T> predicate) {
            this.predicate = predicate;
        }

        @Override
        public Result<T> execute(T t) {
            return new Result<T>(predicate.test(t), t);
        }
    }

    public static class AnyMatch<T> extends Command<T, T> {
        private final Predicate<T> predicate;
        private
        public Filter(Predicate<T> predicate) {
            this.predicate = predicate;
        }

        @Override
        public Result<T> execute(T t) {
            return new Result<T>(predicate.test(t), t);
        }
    }

    public static class Distinct<T> extends Command<T, T> {
        private final Set<T> accumulator = new HashSet<>();

        @Override
        public Result<T> execute(T t) {
            synchronized (this) {
                return new Result<T>(accumulator.contains(t), t);
            }
        }
    }

    public static class Limit<T> extends Command<T, T> {
        private final long maxSize;
        private AtomicLong counter = new AtomicLong(1);

        public Limit(long maxSize) {
            this.maxSize = maxSize;
        }

        @Override
        public Result<T> execute(T t) {
            return new Result<T>(counter.getAndIncrement() > maxSize, t);
        }
    }

    public static class Skip<T> extends Command<T, T> {
        private AtomicLong n;

        public Skip(long n) {
            this.n = new AtomicLong(n);
        }

        @Override
        public Result<T> execute(T t) {
            return new Result<T>(n.getAndDecrement() > 0, t);
        }
    }

    public static class Peek<T> extends Command<T, T> {
        private final Consumer<? super T> action;

        public Peek(Consumer<? super T> action) {
            this.action = action;
        }

        @Override
        public Result<T> execute(T t) {
            action.accept(t);
            return new Result<>(false, t);
        }
    }

    public static class Map<T, R> extends Command<T, R> {
        private final Function<T, R> transform;

        public Map(Function<T, R> transform) {
            this.transform = transform;
        }

        @Override
        public Result<R> execute(T t) {
            return new Result<R>(false, transform.apply(t));
        }
    }

}