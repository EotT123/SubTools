package extensions.java.util.Collection;

import java.util.Collection;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.Self;
import manifold.ext.rt.api.This;
import org.jspecify.annotations.NullMarked;

@Extension
@NullMarked
public class CollectionExt {

    public static <E> @Self Collection<E> replaceContents(@This Collection<E> collection, Collection<E> values) {
        collection.clear();
        collection.addAll(values);
        return collection;
    }
}
