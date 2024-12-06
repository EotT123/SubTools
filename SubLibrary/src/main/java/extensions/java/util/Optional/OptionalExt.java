package extensions.java.util.Optional;

import java.util.Optional;
import java.util.OptionalInt;

import lombok.experimental.UtilityClass;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.Self;
import manifold.ext.rt.api.This;
import name.falgout.jeffrey.throwing.ThrowingConsumer;
import name.falgout.jeffrey.throwing.ThrowingFunction;
import name.falgout.jeffrey.throwing.ThrowingToIntFunction;

@UtilityClass
@SuppressWarnings("OptionalUsedAsFieldOrParameterType")
@Extension
public class OptionalExt {

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
    public static <T, S, X extends Throwable> Optional<S> mapThrowing(@This Optional<T> optional,
            ThrowingFunction<T, S, X> function) throws X {
        return optional.isPresent() ? Optional.ofNullable(function.apply(optional.get())) : Optional.empty();
    }
    //
    //    /**
    //     * If a value is present, returns the result of applying the given {@code Optional}-bearing mapping
    //     function to the
    //     * value, otherwise returns an empty {@code Optional}.
    //     *
    //     * <p>
    //     * This method is similar to {@link Optional#map(Function)}, but the mapping function is one whose result
    //     is already
    //     * an {@code Optional}, and if invoked, {@code flatMap} does not wrap it within an additional {@code
    //     Optional}.
    //     *
    //     * @param optional input object of the extension method
    //     * @param <T> The type of value of the input {@code Optional}
    //     * @param <X> The type of exception that can be thrown by the {@code ThrowingFunction}
    //     * @param mapper the mapping function to apply to a value, if present
    //     * @return the result of applying an {@code Optional}-bearing mapping function to the value of this
    //     *         {@code Optional}, if a value is present, otherwise an empty {@code Optional}
    //     * @throws X exception type of the throwing Function
    //     * @throws NullPointerException if the mapping function is {@code null} or returns a {@code null} result
    //     */
    //    public static <T, S, X extends Throwable> Optional<S> flatMapThrowing(@This Optional<T> optional,
    //            ThrowingFunction<T, Optional<S>, X> mapper) throws X {
    //        if (optional.isEmpty()) {
    //            return Optional.empty();
    //        } else {
    //            return Objects.requireNonNull(mapper.apply(optional.get()));
    //        }
    //    }
    //
    //    /**
    //     * If a value is present, returns the value, otherwise returns the result produced by the supplying function.
    //     *
    //     * @param <T> The type of value of the input {@code Optional}
    //     * @param <X> The type of exception that can be thrown by the {@code ThrowingSupplier}
    //     * @param optional input object of the extension method
    //     * @param supplier the supplying function that produces a value to be returned
    //     * @return the value, if present, otherwise the result produced by the supplying function
    //     * @throws X exception type of the throwing Supplier
    //     * @throws NullPointerException if no value is present and the supplying function is {@code null}
    //     */
    //
    //    public static <T, X extends Throwable> T orElseGetThrowing(@This Optional<T> optional,
    //            ThrowingSupplier<T, X> supplier) throws X {
    //        return optional.isPresent() ? optional.get() : supplier.get();
    //    }
    //
    //    /**
    //     * If a value is present, applies the given function on the value. Otherwise, get  the value from the provided
    //     * supplier.
    //     *
    //     * @param <T> The type of value of the input {@code Optional}
    //     * @param <S> The type of value of the return value
    //     * @param optional input object of the extension method
    //     * @param ifPresentConsumer the function to be applied on the value, if present
    //     * @param emptyValue the value to return when no value is present
    //     * @throws NullPointerException if a value is present and the given action is {@code null}, or no value is
    //     *         present and the given empty-based action is {@code null}.
    //     */
    //    public static <T, S> S applyOrElse(@This Optional<T> optional, Function<T, S> ifPresentConsumer, S
    //    emptyValue) {
    //        if (optional.isPresent()) {
    //            return ifPresentConsumer.apply(optional.get());
    //        } else {
    //            return emptyValue;
    //        }
    //    }
    //
    //    /**
    //     * If a value is present, applies the given function on the value. Otherwise, get  the value from the provided
    //     * supplier.
    //     *
    //     * @param <T> The type of value of the input {@code Optional}
    //     * @param <S> The type of value of the return value
    //     * @param <X> The type of exception that can be thrown by the {@code ThrowingConsumer}
    //     * @param optional input object of the extension method
    //     * @param ifPresentConsumer the function to be applied on the value, if present
    //     * @param emptyValue the value to return when no value is present
    //     * @throws X exception type of the throwing Consumer
    //     * @throws NullPointerException if a value is present and the given action is {@code null}, or no value is
    //     *         present and the given empty-based action is {@code null}.
    //     */
    //    public static <T, S, X extends Throwable> S applyOrElseThrowing(@This Optional<T> optional,
    //            ThrowingFunction<T, S, X> ifPresentConsumer, S emptyValue) throws X {
    //        if (optional.isPresent()) {
    //            return ifPresentConsumer.apply(optional.get());
    //        } else {
    //            return emptyValue;
    //        }
    //    }
    //
    //    /**
    //     * If a value is present, applies the given function on the value. Otherwise, get  the value from the provided
    //     * supplier.
    //     *
    //     * @param <T> The type of value of the input {@code Optional}
    //     * @param <S> The type of value of the return value
    //     * @param optional input object of the extension method
    //     * @param ifPresentConsumer the function to be applied on the value, if present
    //     * @param ifAbsentSupplier the supplier to create a value when no value is present
    //     * @throws NullPointerException if a value is present and the given action is {@code null}, or no value is
    //     *         present and the given empty-based action is {@code null}.
    //     */
    //    public static <T, S> S applyOrElseGet(@This Optional<T> optional, Function<T, S> ifPresentConsumer,
    //            Supplier<S> ifAbsentSupplier) {
    //        if (optional.isPresent()) {
    //            return ifPresentConsumer.apply(optional.get());
    //        } else {
    //            return ifAbsentSupplier.get();
    //        }
    //    }
    //
    //    /**
    //     * If a value is present, applies the given function on the value. Otherwise, get  the value from the provided
    //     * supplier.
    //     *
    //     * @param <T> The type of value of the input {@code Optional}
    //     * @param <S> The type of value of the return value
    //     * @param <X> The type of exception that can be thrown by the {@code ThrowingConsumer}
    //     * @param optional input object of the extension method
    //     * @param ifPresentConsumer the function to be applied on the value, if present
    //     * @param ifAbsentSupplier the supplier to create a value when no value is present
    //     * @throws X exception type of the throwing Consumer
    //     * @throws NullPointerException if a value is present and the given action is {@code null}, or no value is
    //     *         present and the given empty-based action is {@code null}.
    //     */
    //    public static <T, S, X extends Throwable> S applyElseGetThrowing(@This Optional<T> optional,
    //            ThrowingFunction<T, S, X> ifPresentConsumer, ThrowingSupplier<S, X> ifAbsentSupplier) throws X {
    //        if (optional.isPresent()) {
    //            return ifPresentConsumer.apply(optional.get());
    //        } else {
    //            return ifAbsentSupplier.get();
    //        }
    //    }
    //
    //    /**
    //     * If a value is present, performs the given action with the value, otherwise does nothing.
    //     *
    //     * @param <T> The type of value of the input {@code Optional}
    //     * @param <X> The type of exception that can be thrown by the {@code ThrowingConsumer}
    //     * @param optional input object of the extension method
    //     * @param consumer the action to be performed, if a value is present
    //     * @throws X exception type of the throwing Consumer
    //     * @throws NullPointerException if value is present and the given action is {@code null}
    //     */
    //    public static <T, X extends Throwable> void ifPresentThrowing(@This Optional<T> optional,
    //            ThrowingConsumer<T, X> consumer) throws X {
    //        if (optional.isPresent()) {
    //            consumer.accept(optional.get());
    //        }
    //    }
    //
    //    /**
    //     * If a value is present, performs the given action with the value, otherwise performs the given empty-based
    //     * action.
    //     *
    //     * @param <T> The type of value of the input {@code Optional}
    //     * @param <X> The type of exception that can be thrown by the {@code ThrowingConsumer}
    //     * @param optional input object of the extension method
    //     * @param ifPresentConsumer the action to be performed, if a value is present
    //     * @param ifAbsentRunnable the empty-based action to be performed, if no value is present
    //     * @throws X exception type of the throwing Consumer
    //     * @throws NullPointerException if a value is present and the given action is {@code null}, or no value is
    //     *         present and the given empty-based action is {@code null}.
    //     */
    //    public static <T, X extends Throwable> void ifPresentOrElseThrowing(@This Optional<T> optional,
    //            ThrowingConsumer<T, X> ifPresentConsumer, ThrowingRunnable<X> ifAbsentRunnable) throws X {
    //        if (optional.isPresent()) {
    //            ifPresentConsumer.accept(optional.get());
    //        } else {
    //            ifAbsentRunnable.run();
    //        }
    //    }

    public static <T, X extends Exception> OptionalInt mapToInt(@This Optional<T> optional,
            ThrowingToIntFunction<T, X> mapper) throws X {
        return optional.isPresent() ? OptionalInt.of(mapper.applyAsInt(optional.get())) : OptionalInt.empty();
    }

    public static <T, X extends Exception> @Self Optional<T> useIfPresent(@This Optional<T> optional,
            ThrowingConsumer<T, X> consumer) throws X {
        if (optional.isPresent()) {
            consumer.accept(optional.get());
        }
        return optional;
    }
}
