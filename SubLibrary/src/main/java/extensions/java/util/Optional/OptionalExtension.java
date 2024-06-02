package extensions.java.util.Optional;

import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Function;
import java.util.function.Supplier;

import lombok.experimental.UtilityClass;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import name.falgout.jeffrey.throwing.ThrowingConsumer;
import name.falgout.jeffrey.throwing.ThrowingFunction;
import name.falgout.jeffrey.throwing.ThrowingRunnable;
import name.falgout.jeffrey.throwing.ThrowingSupplier;
import name.falgout.jeffrey.throwing.ThrowingToIntFunction;

@Extension
@UtilityClass
public class OptionalExtension {
    /**
     * If the value is present, apply the {@link ThrowingFunction} and return the value. Otherwise return {@code null}
     *
     * @param optional input object of the extension method
     * @param function the {@link ThrowingFunction} to apply to the value if present
     * @param <T> The type of value of the input {@code Optional}
     * @param <S> type of the return value
     * @param <X> type of the exception that can be thrown
     * @return the result of the {@link ThrowingFunction} if the value is present, otherwise {@code null}
     * @throws X exception type of the throwing Function
     */
    public static <O extends Optional<T>, T, S, X extends Throwable> Optional<S> map(@This O optional, ThrowingFunction<T, S, X> function) throws X {
        return optional.isPresent() ? Optional.ofNullable(function.apply(optional.get())) : Optional.empty();
    }

    /**
     * If a value is present, returns the result of applying the given {@code Optional}-bearing mapping function to the value, otherwise returns an
     * empty {@code Optional}.
     *
     * <p>
     * This method is similar to {@link #map(Function)}, but the mapping function is one whose result is already an {@code Optional}, and if invoked,
     * {@code flatMap} does not wrap it within an additional {@code Optional}.
     *
     * @param optional input object of the extension method
     * @param <T> The type of value of the input {@code Optional}
     * @param <O> The type of value of the {@code Optional} returned by the mapping function
     * @param <X> The type of exception that can be thrown by the {@code ThrowingFunction}
     * @param mapper the mapping function to apply to a value, if present
     * @return the result of applying an {@code Optional}-bearing mapping function to the value of this {@code Optional}, if a value is present,
     *         otherwise an empty {@code Optional}
     * @throws X exception type of the throwing Function
     * @throws NullPointerException if the mapping function is {@code null} or returns a {@code null} result
     */
    public static <O1 extends Optional<T>, T, O2 extends Optional<S>, S, X extends Throwable> O2 flatMap(
            @This O1 optional, ThrowingFunction<T, O2, X> mapper) throws X {
        if (optional.isEmpty()) {
            return (O2) Optional.empty();
        } else {
            return Objects.requireNonNull(mapper.apply(optional.get()));
        }
    }

    //    /**
    //     * If a value is present, returns an {@code Optional} describing the value,
    //     * otherwise returns an {@code Optional} produced by the supplying function.
    //     *
    //     * @param <T>
    //     *         The type of value of the input {@code Optional}
    //     * @param <X>
    //     *         The type of exception that can be thrown by the {@code ThrowingSupplier}
    //     * @param optional
    //     *         input object of the extension method
    //     * @param supplier
    //     *         the supplying function that produces an {@code Optional}
    //     *         to be returned
    //     * @return returns an {@code Optional} describing the value of this
    //     * {@code Optional}, if a value is present, otherwise an
    //     * {@code Optional} produced by the supplying function.
    //     * @throws X
    //     *         exception type of the throwing Supplier
    //     * @throws NullPointerException
    //     *         if the supplying function is {@code null} or
    //     *         produces a {@code null} result
    //     */
    //    public static <T, X extends Throwable> Optional<T> orElseGet(@This Optional<T> optional, ThrowingSupplier<? extends Optional<T>, X>
    //    supplier) throws X {
    //        if (optional.isPresent()) {
    //            return optional;
    //        } else {
    //            return Objects.requireNonNull(supplier.get());
    //        }
    //    }

    /**
     * If a value is present, returns the value, otherwise returns the result produced by the supplying function.
     *
     * @param <T> The type of value of the input {@code Optional}
     * @param <X> The type of exception that can be thrown by the {@code ThrowingSupplier}
     * @param optional input object of the extension method
     * @param supplier the supplying function that produces a value to be returned
     * @return the value, if present, otherwise the result produced by the supplying function
     * @throws X exception type of the throwing Supplier
     * @throws NullPointerException if no value is present and the supplying function is {@code null}
     */

    public static <O extends Optional<T>, T, X extends Throwable> T orElseGet(@This O optional,
            ThrowingSupplier<T, X> supplier) throws X {
        return optional.isPresent() ? optional.get() : supplier.get();
    }

    /**
     * If a value is present, performs the given action with the value, otherwise does nothing.
     *
     * @param <T> The type of value of the input {@code Optional}
     * @param <X> The type of exception that can be thrown by the {@code ThrowingConsumer}
     * @param optional input object of the extension method
     * @param consumer the action to be performed, if a value is present
     * @throws X exception type of the throwing Consumer
     * @throws NullPointerException if value is present and the given action is {@code null}
     */
    public static <O extends Optional<T>, T, X extends Throwable> void ifPresent(@This O optional, ThrowingConsumer<T, X> consumer) throws X {
        if (optional.isPresent()) {
            consumer.accept(optional.get());
        }
    }


    /**
     * If a value is present, performs the given action with the value, otherwise performs the given empty-based action.
     *
     * @param <T> The type of value of the input {@code Optional}
     * @param <X> The type of exception that can be thrown by the {@code ThrowingConsumer}
     * @param optional input object of the extension method
     * @param ifPresentConsumer the action to be performed, if a value is present
     * @param ifAbsentRunnable the empty-based action to be performed, if no value is present
     * @throws X exception type of the throwing Consumer
     * @throws NullPointerException if a value is present and the given action is {@code null}, or no value is present and the given
     *         empty-based action is {@code null}.
     */
    public static <O extends Optional<T>, T, X extends Throwable> void ifPresentOrElse(@This O optional,
            ThrowingConsumer<T, X> ifPresentConsumer, ThrowingRunnable<X> ifAbsentRunnable) throws X {
        if (optional.isPresent()) {
            ifPresentConsumer.accept(optional.get());
        } else {
            ifAbsentRunnable.run();
        }
    }

    //    public static <T, X extends Exception> Optional<T> ifPresentDo(@This Optional<T> optional, ThrowingConsumer<T, X> consumer) throws X {
    //        if (optional.isPresent()) {
    //            consumer.accept(optional.get());
    //        }
    //        return optional;
    //    }
    //
    //    ////
    //
    //    public static <T, X extends Exception> void orElseDo(@This Optional<T> optional, ThrowingRunnable<X> consumer) throws X {
    //        if (optional.isEmpty()) {
    //            consumer.run();
    //        }
    //    }
    //
    //    //
    //
    //    public static <T, X extends Exception> Optional<T> ifEmptyDo(@This Optional<T> optional, ThrowingRunnable<X> runnable) throws X {
    //        if (optional.isEmpty()) {
    //            runnable.run();
    //        }
    //        return optional;
    //    }
    //
    //    //
    //
    public static <T, X extends Exception> Optional<T> orElseMap(@This Optional<T> optional, ThrowingSupplier<Optional<T>, X> supplier)
            throws X {
        return optional.isPresent() ? optional : supplier.get();
    }

    //
    //    //
    //
    //    public static <T, S, X extends Exception> T mapOrElseGet(@This Optional<S> optional, ThrowingFunction<S, T, X> ifPresentFunction,
    //            ThrowingSupplier<T, X> absentSupplier) throws X {
    //        return optional.isPresent() ? ifPresentFunction.apply(optional.get()) : absentSupplier.get();
    //    }
    //
    //    //
    //
    public static <T, X extends Exception> OptionalInt mapToInt(@This Optional<T> optional, ThrowingToIntFunction<T, X> mapper) throws X {
        return optional.isPresent() ? OptionalInt.of(mapper.applyAsInt(optional.get())) : OptionalInt.empty();
    }

    //
    public static OptionalInt mapToOptionalInt(@This Optional<Integer> optional) {
        return optional.map(OptionalInt::of).orElseGet(OptionalInt::empty);
    }

    //
    //    //
    //
    public static <T, S, X extends Exception> Optional<S> mapToObj(@This Optional<T> optional, ThrowingFunction<T, S, X> mapper) throws X {
        return optional.isPresent() ? Optional.ofNullable(mapper.apply(optional.get())) : Optional.empty();
    }

    //
    //    //
    //
    //    public static <T, S, X extends Exception> Optional<S> mapToOptionalObj(@This Optional<T> optional, ThrowingFunction<T, Optional<S>, X>
    //    mapper)
    //            throws X {
    //        return optional.isPresent() ? mapper.apply(optional.get()) : Optional.empty();
    //    }
    //
    //    //
    //
    public static <T, X extends Exception> void ifPresentOrThrow(@This Optional<T> optional, ThrowingConsumer<T, X> consumer,
            Supplier<X> exceptionSupplier) throws X {
        if (optional.isPresent()) {
            consumer.accept(optional.get());
        } else {
            throw exceptionSupplier.get();
        }
    }
}
