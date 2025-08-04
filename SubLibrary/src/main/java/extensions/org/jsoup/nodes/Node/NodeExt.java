package extensions.org.jsoup.nodes.Node;

import java.util.function.Supplier;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.Intercept;
import manifold.ext.rt.api.Self;
import manifold.ext.rt.api.This;
import org.jsoup.nodes.Node;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@Extension
@NullMarked
public class NodeExt {

    // ------------ \\
    // Node methods \\
    // ------------ \\

    @Intercept
    public static @Nullable @Self Node parentNode(@This @Nullable Node node) {
        return node == null ? null : node.parentNode();
    }

    @Intercept
    public static @Nullable @Self Node parent(@This @Nullable Node node) {
        return node == null ? null : node.parent();
    }

    public static @Nullable @Self Node parent(@This @Nullable Node node, int amount) {
        if (amount < 1) {
            throw new IllegalArgumentException("The amount of parents must greater than zero");
        }
        if (node == null) {
            return null;
        }
        return amount > 1 ? node.parent(amount - 1) : node.parentNode();
    }

    @Intercept
    public static String attr(@This @Nullable Node node, String attribute) {
        return node == null || !node.hasAttr(attribute) ? "" : node.attr(attribute);
    }

    public static <X extends Exception> String attrOrElse(@This @Nullable Node node, String attribute,
        String emptyValue) throws X {
        return node == null || !node.hasAttr(attribute) ? emptyValue : node.attr(attribute);
    }

    public static <X extends Exception> String attrOrElseGet(@This @Nullable Node node, String attribute,
        Supplier<String> emptyValueSupplier) throws X {
        return node == null || !node.hasAttr(attribute) ? emptyValueSupplier.get() : node.attr(attribute);
    }

    public static <X extends Exception> String attrOrThrow(@This @Nullable Node node, String attribute,
        Supplier<X> exceptionSUpplier) throws X {
        if (node == null || !node.hasAttr(attribute)) {
            throw exceptionSUpplier.get();
        }
        return node.attr(attribute);
    }

    @Intercept
    public static @Nullable Node nextSibling(@This @Nullable Node node) {
        return node == null ? null : node.nextSibling();
    }

    // ------------- \\
    // Other methods \\
    // ------------- \\
//
//    public static <N extends Node> @Nullable N filter(@This @Nullable N node, Predicate<@Self Node> predicate) {
//        return node != null && predicate.test(node) ? node : null;
//    }

//    public static <N extends Node> boolean matches(@This @Nullable N node, Predicate<N> predicate) {
//        return node != null && predicate.test(node);
//    }

//    // TODO change to  Function<N, @Nullable T>
//    public static <N extends Node, T> @Nullable T map(@This @Nullable N node, Function<N, T> mapper) {
//        return node != null ? mapper.apply(node) : null;
//    }

//    // TODO change to  Function<N, @Nullable T>
//    public static <N extends Node, T> Optional<T> mapOptional(@This @Nullable N node, Function<N, T> mapper) {
//        return node != null ? Optional.ofNullable(mapper.apply(node)) : Optional.empty();
//    }
//
//    public static <N extends Node, T> T mapOrElse(@This @Nullable N node, Function<N, T> mapper, T obj) {
//        return node != null ? mapper.apply(node) : obj;
//    }
//
//    public static <N extends Node, T> T mapOrElseGet(@This @Nullable N node, Function<N, T> mapper,
//        Supplier<T> elseSupplier) {
//        return node != null ? mapper.apply(node) : elseSupplier.get();
//    }
//
//    public static <N extends Node, T, X extends Exception> @Nullable T mapEx(@This @Nullable N node,
//        ThrowingFunction<N, T, X> mapper) throws X {
//        return node != null ? mapper.apply(node) : null;
//    }
//
//    public static <N extends Node> N orElseGet(@This @Nullable N node, Supplier<N> elementSupplier) {
//        return node != null ? node : elementSupplier.get();
//    }
//
//    public static <N extends Node, X extends Exception> N orElseThrow(@This @Nullable N node,
//        Supplier<X> exceptionSupplier) throws X {
//        if (node == null) {
//            throw exceptionSupplier.get();
//        }
//        return node;
//    }
//
//    public static <N extends Node> void ifPresent(@This @Nullable N node, Consumer<N> consumer) {
//        if (node != null) {
//            consumer.accept(node);
//        }
//    }
//
//    public static <N extends Node> boolean isFound(@This @Nullable N node) {
//        return node != null;
//    }
//
//    public static <N extends Node> boolean isNotNull(@This @Nullable N node) {
//        return node != null;
//    }
//
//    public static <N extends Node> boolean isNotFound(@This @Nullable N node) {
//        return node == null;
//    }
//
//    public static <N extends Node> boolean isNull(@This @Nullable N node) {
//        return node == null;
//    }
}
