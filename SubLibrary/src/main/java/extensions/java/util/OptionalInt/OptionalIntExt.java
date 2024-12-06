package extensions.java.util.OptionalInt;


import java.util.Optional;
import java.util.OptionalInt;

import com.pivovarit.function.ThrowingIntFunction;
import com.pivovarit.function.ThrowingSupplier;
import lombok.experimental.UtilityClass;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import name.falgout.jeffrey.throwing.ThrowingIntUnaryOperator;


@Extension
@UtilityClass
public class OptionalIntExt {

    /**
     * If the value is present, apply the {@link ThrowingIntUnaryOperator} and return the value wrapped in an
     *
     * @param optional input object of the extension method
     * @param function the function to apply to the value if present
     * @param <X> type of the exception that can be thrown
     * @return the result of the function wrapped in an @{link OptionalInt} if the value is present, otherwise an empty
     *         {@code OptionalInt}
     * @throws X exception type of the throwing Function
     * @{link OptionalInt}. Otherwise, return an empty {@code OptionalInt}
     */
    public static <X extends Exception> OptionalInt map(@This OptionalInt optional,
            ThrowingIntUnaryOperator<X> function) throws X {
        return optional.isPresent() ? OptionalInt.of(function.applyAsInt(optional.getAsInt())) : OptionalInt
                .empty();
    }
    //
    //    /**
    //     * If the value is present, apply the {@link ThrowingUnaryOperator} and return the value wrapped in an @{link
    //     * Optional}. Otherwise, return an empty {@code Optional}
    //     *
    //     * @param optional input object of the extension method
    //     * @param function the function to apply to the value if present
    //     * @param <T> type of the result value wrapped in the @{link Optional}
    //     * @param <X> type of the exception that can be thrown
    //     * @return the result of the function wrapped in an @{link Optional} if the value is present, otherwise an
    //     empty
    //     *         {@code Optional}
    //     * @throws X exception type of the throwing Function
    //     */
    //    public static <T, X extends Exception> Optional<T> mapToObj(@This OptionalInt optional,
    //            ThrowingFunction<Integer, T, X> function) throws X {
    //        return optional.isPresent() ? Optional.ofNullable(function.apply(optional.getAsInt())) : Optional.empty();
    //    }


    //    public static <X extends Exception> OptionalInt ifPresentDo(@This OptionalInt optional,
    //    ThrowingIntConsumer<X> consumer) throws X {
    //        if (optional.isPresent()) {
    //            consumer.accept(optional.getAsInt());
    //        }
    //        return optional;
    //    }
    //
    //    ////
    //
    //    public static <X extends Exception> void orElseDo(@This OptionalInt optional, ThrowingRunnable<X> consumer)
    //    throws X {
    //        if (optional.isEmpty()) {
    //            consumer.run();
    //        }
    //    }
    //
    //    //
    //

    public static <X extends Exception> OptionalInt orElseMap(@This OptionalInt optionalInt,
            ThrowingSupplier<OptionalInt, X> intSupplier) throws X {
        return optionalInt.isPresent() ? optionalInt : intSupplier.get();
    }

    //

    //    public static <T, X extends Exception> T mapOrElseGet(@This OptionalInt optionalInt,
    //            ThrowingIntFunction<T, X> ifPresentFunction, ThrowingSupplier<T, X> absentSupplier) throws X {
    //        return optionalInt.isPresent() ? ifPresentFunction.apply(optionalInt.getAsInt()) : absentSupplier.get();
    //    }

    //

    public static <T, X extends Exception> Optional<T> mapToObj(@This OptionalInt optionalInt,
            ThrowingIntFunction<T, X> mapper) throws X {
        return optionalInt.isPresent() ? Optional.ofNullable(mapper.apply(optionalInt.getAsInt())) : Optional
                .empty();
    }

    public static Integer orElseNull(@This OptionalInt optionalInt) {
        return optionalInt.isPresent() ? optionalInt.getAsInt() : null;
    }
    //
    //    public static <T, X extends Exception> Optional<T> mapToOptionalObj(@This OptionalInt optionalInt,
    //    ThrowingIntFunction<Optional<T>, X> mapper)
    //            throws X {
    //        return optionalInt.isPresent() ? mapper.apply(optionalInt.getAsInt()) : Optional.empty();
    //    }
    //
    //    //
    //
    //    public static <X extends Exception> OptionalInt filter(@This OptionalInt optionalInt,
    //    ThrowingIntPredicate<X> predicate)
    //            throws X {
    //        return optionalInt.isPresent() && predicate.test(optionalInt.getAsInt()) ? optionalInt : OptionalInt
    //        .empty();
    //    }
    //
    //    //
    //
    //    public static <X extends Exception> void ifPresentOrThrow(@This OptionalInt optionalInt,
    //    ThrowingIntConsumer<X> consumer,
    //            Supplier<X> exceptionSupplier) throws X {
    //        if (optionalInt.isPresent()) {
    //            consumer.accept(optionalInt.getAsInt());
    //        } else {
    //            throw exceptionSupplier.get();
    //        }
    //    }
}
