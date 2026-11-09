package extensions.manifold.rt.api.Array;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.function.Consumer;

import com.google.common.base.Objects;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.Self;
import manifold.ext.rt.api.This;
import org.jspecify.annotations.NullMarked;

@Extension
@NullMarked
public class ArrayExt {

    private ArrayExt() {
        // Hide Utility Class Constructor
    }

    /**
     * Performs an action for each element of the array.
     *
     * @param array the Array
     * @param action an action to perform on the elements
     */
    public static void forEach(@This Object array, Consumer<? super @Self(true) Object> action) {
        primitiveCheck(array);
        Arrays.stream((Object[]) array, 0, Array.getLength(array)).forEach(action);
    }


    private static void primitiveCheck(Object array) {
        Class<?> componentType = array.getClass().getComponentType();
        if (componentType.isPrimitive()) {
            throw new IllegalArgumentException("$array has not a primitive component type: " +
                array.getClass().getComponentType().getSimpleName());
        }
    }

    public static boolean contains(@This Object array, @Self(true) Object object) {
        return ((Object[]) array).indexOf(object) > -1;
    }

    public static int indexOf(@This Object array, @Self(true) Object object) {
        Object[] a = (Object[]) array;
        for (int i = 0; i < a.length; i++) {
            if (Objects.equal(a[i], object)) {
                return i;
            }
        }
        return -1;
    }
}