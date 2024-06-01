package extensions.java.util.OptionalInt;


import java.util.Optional;
import java.util.OptionalInt;
import java.util.function.Supplier;

import com.pivovarit.function.ThrowingIntFunction;
import com.pivovarit.function.ThrowingRunnable;
import com.pivovarit.function.ThrowingSupplier;
import lombok.experimental.UtilityClass;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import name.falgout.jeffrey.throwing.ThrowingIntPredicate;
import org.lodder.subtools.sublibrary.util.throwingfunction.ThrowingIntConsumer;


@Extension
@UtilityClass
public class OptionalIntExtension {

    public static <X extends Exception> OptionalInt ifPresentDo(@This OptionalInt optional, ThrowingIntConsumer<X> consumer) throws X {
        if (optional.isPresent()) {
            consumer.accept(optional.getAsInt());
        }
        return optional;
    }

    ////

    public static <X extends Exception> void orElseDo(@This OptionalInt optional, ThrowingRunnable<X> consumer) throws X {
        if (optional.isEmpty()) {
            consumer.run();
        }
    }

    //

    public static <X extends Exception> OptionalInt orElseMap(@This OptionalInt optionalInt, ThrowingSupplier<OptionalInt, X> intSupplier) throws X {
        return optionalInt.isPresent() ? optionalInt : intSupplier.get();
    }

    //

    public static <T, X extends Exception> T mapOrElseGet(@This OptionalInt optionalInt, ThrowingIntFunction<T, X> ifPresentFunction,
            ThrowingSupplier<T, X> absentSupplier) throws X {
        return optionalInt.isPresent() ? ifPresentFunction.apply(optionalInt.getAsInt()) : absentSupplier.get();
    }

    //

    public static <T, X extends Exception> Optional<T> mapToObj(@This OptionalInt optionalInt, ThrowingIntFunction<T, X> mapper) throws X {
        return optionalInt.isPresent() ? Optional.ofNullable(mapper.apply(optionalInt.getAsInt())) : Optional.empty();
    }

    public static <T, X extends Exception> Optional<T> mapToOptionalObj(@This OptionalInt optionalInt, ThrowingIntFunction<Optional<T>, X> mapper)
            throws X {
        return optionalInt.isPresent() ? mapper.apply(optionalInt.getAsInt()) : Optional.empty();
    }

    //

    public static <X extends Exception> OptionalInt filter(@This OptionalInt optionalInt, ThrowingIntPredicate<X> predicate)
            throws X {
        return optionalInt.isPresent() && predicate.test(optionalInt.getAsInt()) ? optionalInt : OptionalInt.empty();
    }

    //

    public static <X extends Exception> void ifPresentOrThrow(@This OptionalInt optionalInt, ThrowingIntConsumer<X> consumer,
            Supplier<X> exceptionSupplier) throws X {
        if (optionalInt.isPresent()) {
            consumer.accept(optionalInt.getAsInt());
        } else {
            throw exceptionSupplier.get();
        }
    }
}
