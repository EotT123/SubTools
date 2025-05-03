package extensions.java.util.Collection;

import java.util.Collection;
import java.util.function.Supplier;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.Self;
import manifold.ext.rt.api.This;
import name.falgout.jeffrey.throwing.ThrowingSupplier;

@Extension
public class CollectionExt {

//    @Intercept
//    public static int size(@This @Nullable Collection<?> collection) {
//        return collection == null ? 0 : collection.size();
//    }

    public static <T> @Self Collection<T> elseIfEmpty(@This Collection<T> collection,
        Supplier<@Self Collection<T>> emptySupplier) {
        return collection == null || collection.isEmpty() ? emptySupplier.get() : collection;
    }

    public static <T, X extends Exception> @Self Collection<T> elseIfEmptyThrowing(@This Collection<T> collection,
        ThrowingSupplier<@Self Collection<T>, X> emptySupplier) throws X {
        return collection == null || collection.isEmpty() ? emptySupplier.get() : collection;
    }


}
