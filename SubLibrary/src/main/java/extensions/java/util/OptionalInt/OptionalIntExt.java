package extensions.java.util.OptionalInt;


import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.IntFunction;
import java.util.function.IntUnaryOperator;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import name.falgout.jeffrey.throwing.ThrowingIntConsumer;
import name.falgout.jeffrey.throwing.ThrowingIntFunction;
import name.falgout.jeffrey.throwing.ThrowingIntUnaryOperator;
import name.falgout.jeffrey.throwing.ThrowingRunnable;
import name.falgout.jeffrey.throwing.ThrowingSupplier;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;


@Extension
@SuppressWarnings({"unused", "OptionalUsedAsFieldOrParameterType"})
@NullMarked
public class OptionalIntExt {

    private OptionalIntExt() {
        // Hide Utility Class Constructor
    }

    /**
     * If a value is present, applies the {@link IntUnaryOperator} to it and returns the result wrapped in an
     * {@link OptionalInt}.
     *
     * @param optional the input {@code OptionalInt} for the extension method
     * @param function the function to apply if a value is present
     * @return an {@code OptionalInt} describing the result of applying the function, or an empty {@code OptionalInt}
     * if no value is present
     */
    public static OptionalInt map(@This OptionalInt optional, IntUnaryOperator function) {
        return optional.isPresent() ? OptionalInt.of(function.applyAsInt(optional.getAsInt())) : OptionalInt.empty();
    }

    /**
     * If a value is present, applies the {@link ThrowingIntUnaryOperator} to it and returns the result wrapped in an
     * {@link OptionalInt}.
     *
     * @param optional the input {@code OptionalInt} for the extension method
     * @param function the function to apply if a value is present
     * @param <X> the type of exception that the function may throw
     * @return an {@code OptionalInt} describing the result of applying the function, or an empty {@code OptionalInt}
     * if no value is present
     * @throws X if the function throws an exception
     */
    public static <X extends Exception> OptionalInt mapEx(@This OptionalInt optional,
        ThrowingIntUnaryOperator<X> function) throws X {
        return optional.isPresent() ? OptionalInt.of(function.applyAsInt(optional.getAsInt())) : OptionalInt
            .empty();
    }


    /**
     * If a value is present, applies the {@link IntFunction} to it and returns the result wrapped in an
     * {@link Optional}.
     *
     * @param optional the input {@code OptionalInt} for the extension method
     * @param function the function to apply if a value is present
     * @param <R> the type of the result
     * @return an {@code Optional} describing the result of applying the function, or an empty {@code Optional} if no
     * value is present
     */
    public static <R extends @Nullable Object> Optional<R> mapToObj(@This OptionalInt optional,
        IntFunction<R> function) {
        return optional.isPresent() ? Optional.ofNullable(function.apply(optional.getAsInt())) : Optional.empty();
    }

    /**
     * If a value is present, applies the {@link ThrowingIntFunction} to it and returns the result wrapped in an
     * {@link Optional}.
     *
     * @param optional the input {@code OptionalInt} for the extension method
     * @param function the function to apply if a value is present
     * @param <R> the type of the result
     * @param <X> the type of exception that the function may throw
     * @return an {@code Optional} describing the result of applying the function, or an empty {@code Optional} if no
     * value is present
     * @throws X if the function throws an exception
     */
    public static <R extends @Nullable Object, X extends Exception> Optional<R> mapToObjEx(@This OptionalInt optional,
        ThrowingIntFunction<R, X> function) throws X {
        return optional.isPresent() ? Optional.ofNullable(function.apply(optional.getAsInt())) : Optional.empty();
    }

    /**
     * If a value is present, applies the {@link IntFunction} to it and returns the result wrapped in an
     * {@link Optional}.
     *
     * @param optionalInt the input {@code OptionalInt} for the extension method
     * @param function the function to apply if a value is present
     * @param <R> the type of the result
     * @param <X> the type of exception that the function may throw
     * @return an {@code Optional} describing the result of applying the function
     */
    public static <R extends @Nullable Object, X extends Exception> Optional<R> flatMapToObj(
        @This OptionalInt optionalInt, IntFunction<Optional<R>> function) throws X {
        return optionalInt.isPresent() ? function.apply(optionalInt.getAsInt()) : Optional.empty();
    }

    /**
     * If a value is present, applies the {@link ThrowingIntFunction} to it and returns the result wrapped in an
     * {@link Optional}.
     *
     * @param optionalInt the input {@code OptionalInt} for the extension method
     * @param function the function to apply if a value is present
     * @param <R> the type of the result
     * @param <X> the type of exception that the function may throw
     * @return an {@code Optional} describing the result of applying the function
     * @throws X if the function throws an exception
     */
    public static <R extends @Nullable Object, X extends Exception> Optional<R> flatMapToObjEx(
        @This OptionalInt optionalInt, ThrowingIntFunction<Optional<R>, X> function) throws X {
        return optionalInt.isPresent() ? function.apply(optionalInt.getAsInt()) : Optional.empty();
    }

    /**
     * Returns the value if present, otherwise returns the result of the provided {@link ThrowingSupplier}.
     *
     * @param optionalInt the input {@code OptionalInt} for the extension method
     * @param intSupplier a supplier that provides an {@code OptionalInt} if the original is empty
     * @param <X> the type of exception that the supplier may throw
     * @return the original {@code OptionalInt} if present; otherwise, the result from {@code intSupplier}
     * @throws X if the supplier throws an exception
     */
    public static <X extends Exception> OptionalInt orElseMapEx(@This OptionalInt optionalInt,
        ThrowingSupplier<OptionalInt, X> intSupplier) throws X {
        return optionalInt.isPresent() ? optionalInt : intSupplier.get();
    }

    /**
     * Executes the given {@link Runnable} if the {@code OptionalInt} is empty.
     *
     * @param optional the input {@code OptionalInt} for the extension method
     * @param runnable the action to perform if no value is present
     */
    public static void ifNotPresent(@This OptionalInt optional, Runnable runnable) {
        if (optional.isEmpty()) {
            runnable.run();
        }
    }

    /**
     * Executes the given {@link ThrowingRunnable} if the {@code OptionalInt} is empty.
     *
     * @param optional the input {@code OptionalInt} for the extension method
     * @param runnable the action to perform if no value is present
     * @param <X> the type of exception that the runnable may throw
     * @throws X if the runnable throws an exception
     */
    public static <X extends Throwable> void ifNotPresentEx(@This OptionalInt optional,
        ThrowingRunnable<X> runnable) throws X {
        if (optional.isEmpty()) {
            runnable.run();
        }
    }

    /**
     * If a value is present in the given {@link OptionalInt}, performs the provided
     * {@code consumer} with the value; otherwise performs the provided
     * {@code elseRunnable}.
     *
     * <p>This method is similar to {@link OptionalInt#ifPresentOrElse}, but allows
     * both actions to throw checked exceptions.</p>
     *
     * @param <X> the type of exception that may be thrown by either action
     * @param optional the optional to inspect
     * @param consumer the action to execute if a value is present
     * @param elseRunnable the action to execute if no value is present
     * @throws X if the consumer or elseRunnable throws an exception
     */
    public static <X extends Exception> void ifPresentOrElseEx(@This OptionalInt optional,
        ThrowingIntConsumer<X> consumer, ThrowingRunnable<X> elseRunnable) throws X {
        if (optional.isPresent()) {
            consumer.accept(optional.getAsInt());
        } else {
            elseRunnable.run();
        }
    }
}
