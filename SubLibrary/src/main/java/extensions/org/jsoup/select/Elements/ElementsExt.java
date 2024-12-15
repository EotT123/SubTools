package extensions.org.jsoup.select.Elements;

import lombok.experimental.UtilityClass;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import name.falgout.jeffrey.throwing.stream.ThrowingStream;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

@Extension
@UtilityClass
public class ElementsExt {

    public static <E extends Exception> ThrowingStream<Element, E> throwingStream(@This Elements elements,
            Class<E> exceptionType) {
        return ThrowingStream.of(elements.stream(), exceptionType);
    }
}
