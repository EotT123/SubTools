package extensions.java.util.OptionalLong;

import java.util.Optional;
import java.util.OptionalLong;

import com.pivovarit.function.ThrowingFunction;
import com.pivovarit.function.ThrowingUnaryOperator;
import lombok.experimental.UtilityClass;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;

@Extension
@UtilityClass
public class OptionalLongExtension {

    /**
     * If the value is present, apply the {@link ThrowingUnaryOperator} and return the value wrapped in an @{link OptionalLong}.
     * Otherwise, return an empty {@code OptionalLong}
     *
     * @param optional input object of the extension method
     * @param function the function to apply to the value if present
     * @param <X> type of the exception that can be thrown
     * @return the result of the function wrapped in an @{link OptionalLong} if the value is present, otherwise an empty {@code OptionalLong}
     * @throws X exception type of the throwing Function
     */
    public static <X extends Exception> OptionalLong map(@This OptionalLong optional, ThrowingUnaryOperator<Long, X> function) throws X {
        if(optional.isPresent()){
            Long value = function.apply(optional.getAsLong());
            if(value != null){
                return OptionalLong.of(value);
            }
        }
        return OptionalLong.empty();
    }

    /**
     * If the value is present, apply the {@link ThrowingUnaryOperator} and return the value wrapped in an @{link Optional}.
     * Otherwise, return an empty {@code Optional}
     *
     * @param optional input object of the extension method
     * @param function the function to apply to the value if present
     * @param <T> type of the result value wrapped in the @{link Optional}
     * @param <X> type of the exception that can be thrown
     * @return the result of the function wrapped in an @{link Optional} if the value is present, otherwise an empty {@code Optional}
     * @throws X exception type of the throwing Function
     */
    public static <T, X extends Exception> Optional<T> mapToObj(@This OptionalLong optional, ThrowingFunction<Long,T,  X> function) throws X {
        return optional.isPresent() ? Optional.ofNullable(function.apply(optional.getAsLong())) : Optional.empty();
    }

//    public static <X extends Exception> OptionalLong ifPresentDo(@This OptionalLong optional, ThrowingLongConsumer<X> consumer) throws X {
//        if (optional.isPresent()) {
//            consumer.accept(optional.getAsLong());
//        }
//        return optional;
//    }
//
//    ////
//
//    public static <X extends Exception> void orElseDo(@This OptionalLong optional, ThrowingRunnable<X> consumer) throws X {
//        if (optional.isEmpty()) {
//            consumer.run();
//        }
//    }
//
//    //
//
//    public static <T, X extends Exception> Optional<T> mapToObj(@This OptionalLong optionalLong, ThrowingLongFunction<T, X> mapper) throws X {
//        return optionalLong.isPresent() ? Optional.ofNullable(mapper.apply(optionalLong.getAsLong())) : Optional.empty();
//    }
//
//    //
//
//    public static <T, X extends Exception> OptionalLong map(@This OptionalLong optionalLong, ThrowingLongFunction<Long, X> mapper) throws X {
//        return optionalLong.isPresent() ? OptionalLong.of(mapper.apply(optionalLong.getAsLong())) : optionalLong;
//    }
}
