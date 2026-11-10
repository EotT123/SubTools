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
        requireNonPrimitive(array);
        for (Object o : (Object[]) array) {
            action.accept(o);
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

    private static void requireNonPrimitive(Object array) {
        Class<?> componentType = array.getClass().getComponentType();
        if (componentType.isPrimitive()) {
            throw new IllegalArgumentException(
                array + " has a primitive component type: " + array.getClass().getComponentType().getSimpleName());
        }
    }
}