package extensions.org.jsoup.select.Elements;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.Intercept;
import manifold.ext.rt.api.This;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@Extension
@NullMarked
public class ElementsExt {

    private ElementsExt() {
        // hide utility class constructor
    }

    @Intercept
    public static String text(@This @Nullable Elements elements) {
        return elements == null ? "" : elements.text();
    }

    @Intercept
    public static @Nullable String attr(@This @Nullable Elements elements, String attribute) {
        return elements == null ? null : elements.attr(attribute);
    }

    @Intercept
    public static @Nullable Element first(@This @Nullable Elements elements) {
        return elements == null ? null : elements.first();
    }
}
