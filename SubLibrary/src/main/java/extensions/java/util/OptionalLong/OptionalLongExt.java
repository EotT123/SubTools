package extensions.java.util.OptionalLong;

import java.util.Optional;
import java.util.OptionalLong;
import java.util.function.LongFunction;
import java.util.function.LongUnaryOperator;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import name.falgout.jeffrey.throwing.ThrowingLongConsumer;
import name.falgout.jeffrey.throwing.ThrowingLongFunction;
import name.falgout.jeffrey.throwing.ThrowingLongUnaryOperator;
import name.falgout.jeffrey.throwing.ThrowingRunnable;
import name.falgout.jeffrey.throwing.ThrowingSupplier;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@Extension
@SuppressWarnings({"unused", "OptionalUsedAsFieldOrParameterType"})
@NullMarked
public class OptionalLongExt {

    private OptionalLongExt() {
        // Hide Utility Class Constructor
    }

    /**
     * Applies the given function to the value if present.
     *
     * @param optional the input {@code OptionalLong}
     * @param function the function to apply
     * @return an {@code OptionalLong} describing the result, or empty if not present
     */
    public static OptionalLong mapEx(@This OptionalLong optional, LongUnaryOperator function) {
        return optional.isPresent() ? OptionalLong.of(function.applyAsLong(optional.getAsLong())) :
            OptionalLong.empty();
    }

    /**
     * Applies the given {@link ThrowingLongUnaryOperator} to the value if present.
     *
     * @param optional the input {@code OptionalLong}
     * @param function the function to apply
     * @param <X> the type of exception the function may throw
     * @return an {@code OptionalLong} describing the result, or empty if not present
     * @throws X if the function throws an exception
     */
    public static <X extends Exception> OptionalLong mapEx(@This OptionalLong optional,
        ThrowingLongUnaryOperator<X> function) throws X {
        return optional.isPresent() ? OptionalLong.of(function.applyAsLong(optional.getAsLong())) :
            OptionalLong.empty();
    }

    /**
     * Applies the given function to the value and returns a mapped object.
     *
     * @param optional the input {@code OptionalLong}
     * @param function the function to apply
     * @param <R> the result type
     * @return an {@code Optional} describing the mapped result, or empty if not present
     */
    public static <R extends @Nullable Object> Optional<R> mapToObj(@This OptionalLong optional,
        LongFunction<? extends R> function) {
        return optional.isPresent() ? Optional.ofNullable(function.apply(optional.getAsLong())) : Optional.empty();
    }

    /**
     * Applies the given {@link ThrowingLongFunction} to the value and returns a mapped object.
     *
     * @param optional the input {@code OptionalLong}
     * @param function the function to apply
     * @param <R> the result type
     * @param <X> the exception type
     * @return an {@code Optional} describing the mapped result, or empty if not present
     * @throws X if the function throws an exception
     */
    public static <R extends @Nullable Object, X extends Exception> Optional<R> mapToObjEx(@This OptionalLong optional,
        ThrowingLongFunction<? extends R, X> function) throws X {
        return optional.isPresent() ? Optional.ofNullable(function.apply(optional.getAsLong())) : Optional.empty();
    }

    /**
     * If a value is present, applies the {@link LongFunction} to it and returns the result wrapped in an
     * {@link Optional}.
     *
     * @param optionalLong the input {@code OptionalLong} for the extension method
     * @param function the function to apply if a value is present
     * @param <R> the type of the result
     * @param <X> the type of exception that the function may throw
     * @return an {@code Optional} describing the result of applying the function
     */
    public static <R extends @Nullable Object, X extends Exception> Optional<R> flatMapToObj(
        @This OptionalLong optionalLong, LongFunction<Optional<R>> function) throws X {
        return optionalLong.isPresent() ? function.apply(optionalLong.getAsLong()) : Optional.empty();
    }

    /**
     * If a value is present, applies the {@link ThrowingLongFunction} to it and returns the result wrapped in an
     * {@link Optional}.
     *
     * @param optionalLong the input {@code OptionalLong} for the extension method
     * @param function the function to apply if a value is present
     * @param <R> the type of the result
     * @param <X> the type of exception that the function may throw
     * @return an {@code Optional} describing the result of applying the function
     * @throws X if the function throws an exception
     */
    public static <R extends @Nullable Object, X extends Exception> Optional<R> flatMapToObjEx(
        @This OptionalLong optionalLong, ThrowingLongFunction<Optional<R>, X> function) throws X {
        return optionalLong.isPresent() ? function.apply(optionalLong.getAsLong()) : Optional.empty();
    }


    /**
     * Returns the current {@code OptionalLong} if a value is present, otherwise returns the result of the supplier.
     *
     * @param optional the input {@code OptionalLong}
     * @param supplier a supplier that returns an {@code OptionalLong}
     * @param <X> the type of exception the supplier may throw
     * @return the current {@code OptionalLong} if present, or the supplier result otherwise
     * @throws X if the supplier throws an exception
     */
    public static <X extends Exception> OptionalLong orElseMapEx(@This OptionalLong optional,
        ThrowingSupplier<OptionalLong, X> supplier) throws X {
        return optional.isPresent() ? optional : supplier.get();
    }

    /**
     * Executes the given runnable if the {@code OptionalLong} is empty.
     *
     * @param optional the input {@code OptionalLong}
     * @param runnable the action to perform if value is not present
     */
    public static void ifNotPresent(@This OptionalLong optional, Runnable runnable) {
        if (optional.isEmpty()) {
            runnable.run();
        }
    }

    /**
     * Executes the given {@link ThrowingRunnable} if the {@code OptionalLong} is empty.
     *
     * @param optional the input {@code OptionalLong}
     * @param runnable the action to perform if value is not present
     * @param <X> the type of exception the runnable may throw
     * @throws X if the runnable throws an exception
     */
    public static <X extends Throwable> void ifNotPresentEx(@This OptionalLong optional,
        ThrowingRunnable<X> runnable) throws X {
        if (optional.isEmpty()) {
            runnable.run();
        }
    }

    /**
     * If a value is present in the given {@link OptionalLong}, performs the provided
     * {@code consumer} with the value; otherwise performs the provided
     * {@code elseRunnable}.
     *
     * <p>This method is similar to {@link OptionalLong#ifPresentOrElse}, but allows
     * both actions to throw checked exceptions.</p>
     *
     * @param <X> the type of exception that may be thrown by either action
     * @param optional the optional to inspect
     * @param consumer the action to execute if a value is present
     * @param elseRunnable the action to execute if no value is present
     * @throws X if the consumer or elseRunnable throws an exception
     */
    public static <X extends Exception> void ifPresentOrElseEx(@This OptionalLong optional,
        ThrowingLongConsumer<X> consumer, ThrowingRunnable<X> elseRunnable) throws X {
        if (optional.isPresent()) {
            consumer.accept(optional.getAsLong());
        } else {
            elseRunnable.run();
        }
    }
}
