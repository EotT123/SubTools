package extensions.java.util.Collection;

import java.util.Collection;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.Intercept;
import manifold.ext.rt.api.This;
import org.jspecify.annotations.Nullable;

@Extension
public class CollectionExt {

    @Intercept
    public static int size(@This @Nullable Collection<?> collection) {
        return collection == null ? 0 : collection.size();
    }
}
