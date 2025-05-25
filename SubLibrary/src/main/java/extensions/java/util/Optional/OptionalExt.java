package extensions.java.util.Optional;

import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Supplier;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.Self;
import manifold.ext.rt.api.This;
import name.falgout.jeffrey.throwing.ThrowingConsumer;
import name.falgout.jeffrey.throwing.ThrowingFunction;
import name.falgout.jeffrey.throwing.ThrowingRunnable;
import name.falgout.jeffrey.throwing.ThrowingSupplier;
import name.falgout.jeffrey.throwing.ThrowingToIntFunction;

@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Extension
public class OptionalExt {

    private OptionalExt() {
        // hide utility class constructor
    }

//    /**
//     * Applies the given function to the value if present.
//     *
//     * @param optional the input {@code Optional} for the extension method
//     * @param function the function to apply
//     * @param <T> the type of the value
//     * @return an {@code Optional} describing the result of the function, or empty if not present
//     */
//    public static <T, R> Optional<R> map(@This Optional<T> optional, Function<? super T, ? extends R> function) {
//        return optional.map(function);
//    }

    /**
     * Applies the given {@link ThrowingFunction} to the value if present.
     *
     * @param optional the input {@code Optional} for the extension method
     * @param function the function to apply
     * @param <T> the type of the input value
     * @param <X> the type of exception the function may throw
     * @return an {@code Optional} describing the result of the function, or empty if not present
     * @throws X if the function throws an exception
     */
    public static <T, R, X extends Exception> Optional<R> mapEx(@This Optional<T> optional,
        ThrowingFunction<? super T, ? extends R, X> function) throws X {
        return optional.isPresent() ? Optional.ofNullable(function.apply(optional.get())) : Optional.empty();
    }

    /**
     * Returns the current optional if a value is present, otherwise returns the result of the supplier.
     *
     * @param optional the input {@code Optional}
     * @param supplier a supplier that provides an {@code Optional} if the current one is empty
     * @param <T> the type of the value
     * @return the current value or the result of the supplier
     */
    public static <T> Optional<T> orElseMap(@This Optional<T> optional, Supplier<Optional<T>> supplier) {
        return optional.isPresent() ? optional : supplier.get();
    }

    /**
     * Returns the current optional if a value is present, otherwise returns the result of the supplier.
     *
     * @param optional the input {@code Optional}
     * @param supplier a supplier that provides an {@code Optional} if the current one is empty
     * @param <T> the type of the value
     * @param <X> the type of exception the supplier may throw
     * @return the current value or the result of the supplier
     * @throws X if the supplier throws an exception
     */
    public static <T, X extends Exception> Optional<T> orElseMapEx(@This Optional<T> optional,
        ThrowingSupplier<Optional<T>, X> supplier) throws X {
        return optional.isPresent() ? optional : supplier.get();
    }

    /**
     * Executes the given runnable if the {@code Optional} is empty.
     *
     * @param optional the input {@code Optional}
     * @param runnable the action to perform if the value is absent
     * @param <T> the type of the value
     */
    public static <T> void ifNotPresent(@This Optional<T> optional, Runnable runnable) {
        if (optional.isEmpty()) {
            runnable.run();
        }
    }

    /**
     * Executes the given {@link ThrowingRunnable} if the {@code Optional} is empty.
     *
     * @param optional the input {@code Optional}
     * @param runnable the action to perform if the value is absent
     * @param <T> the type of the value
     * @param <X> the type of exception the runnable may throw
     * @throws X if the runnable throws an exception
     */
    public static <T, X extends Throwable> void ifNotPresentEx(@This Optional<T> optional,
        ThrowingRunnable<X> runnable) throws X {
        if (optional.isEmpty()) {
            runnable.run();
        }
    }

    public static <T, X extends Exception> OptionalInt mapToIntEx(@This Optional<T> optional,
        ThrowingToIntFunction<T, X> mapper) throws X {
        return optional.isPresent() ? OptionalInt.of(mapper.applyAsInt(optional.get())) : OptionalInt.empty();
    }


    public static <T, X extends Exception> @Self Optional<T> useIfPresentEx(@This Optional<T> optional,
        ThrowingConsumer<T, X> consumer) throws X {
        if (optional.isPresent()) {
            consumer.accept(optional.get());
        }
        return optional;
    }

    public static <T, X extends Throwable> T orElseGetEx(@This Optional<T> optional,
        ThrowingSupplier<T, X> supplier) throws X {
        return optional.isPresent() ? optional.get() : supplier.get();
    }

    public static <T, X extends Exception> void ifPresentEx(@This Optional<T> optional,
        ThrowingConsumer<T, X> supplier) throws X {
        if (optional.isPresent()) {
            supplier.accept(optional.get());
        }
    }
}
