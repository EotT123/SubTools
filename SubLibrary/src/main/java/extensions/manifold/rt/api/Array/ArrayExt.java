package extensions.manifold.rt.api.Array;

import java.lang.reflect.Array;
import java.util.Objects;
import java.util.function.Consumer;

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
        int length = Array.getLength(array);
        if (!array.getClass().getComponentType().isPrimitive()) {
            Object[] a = (Object[]) array;
            for (int i = 0; i < length; i++) {
                action.accept(a[i]);
            }
        } else {
            for (int i = 0; i < length; i++) {
                action.accept(Array.get(array, i));
            }
        }
    }

    public static boolean contains(@This Object array, @Self(true) Object object) {
        return indexOfInternally(array, object) > -1;
    }

    public static int indexOf(@This Object array, @Self(true) Object object) {
        return indexOfInternally(array, object);
    }

    private static int indexOfInternally(Object array, Object object) {
        int length = Array.getLength(array);
        if (!array.getClass().getComponentType().isPrimitive()) {
            Object[] a = (Object[]) array;
            for (int i = 0; i < length; i++) {
                if (Objects.equals(a[i], object)) {
                    return i;
                }
            }
        } else {
            for (int i = 0; i < length; i++) {
                if (Objects.equals(Array.get(array, i), object)) {
                    return i;
                }
            }
        }
        return -1;
    }
}