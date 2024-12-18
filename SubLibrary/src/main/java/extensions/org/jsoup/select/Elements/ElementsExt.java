package extensions.org.jsoup.select.Elements;

import lombok.experimental.UtilityClass;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.jspecify.annotations.Nullable;

@Extension
@UtilityClass
public class ElementsExt {


    public static String getText(@This @Nullable Elements elements) {
        return elements == null ? "" : elements.text();
    }

    public static String getAttr(@This @Nullable Elements elements, String attribute) {
        return elements == null ? "" :
                elements.stream()
                        .filter(elem -> elem.hasAttr(attribute))
                        .map(elem -> elem.attr(attribute))
                        .findFirst()
                        .orElse("");
    }

    public static @Nullable Element getFirstElement(@This @Nullable Elements elements) {
        return elements == null ? null : elements.first();
    }

    public static int getSize(@This Elements elements) {
        return elements == null ? 0 : elements.size();
    }
}
