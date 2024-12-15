package extensions.org.jsoup.nodes.Element;

import extensions.org.jsoup.nodes.Node.NodeExt;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.Jailbreak;
import manifold.ext.rt.api.This;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.jsoup.helper.Validate;
import org.jsoup.nodes.Element;
import org.jsoup.select.Collector;
import org.jsoup.select.Elements;
import org.jsoup.select.Evaluator;
import org.jsoup.select.QueryParser;
import org.lodder.subtools.sublibrary.exception.WebpageException;

@Extension
public class ElementExt extends NodeExt {

    // --------------- \\
    // Get all Element \\
    // --------------- \\

    public static @Nullable Elements getAllElementsByClass(@Nullable @This Element element, String className) {
        return element == null ? null : element.getElementsByClass(requireNotEmpty(className));
    }

    public static @Nullable Elements getAllElementsByTag(@Nullable @This Element element, String tagName) {
        return element == null ? null : element.getElementsByTag(requireNotEmpty(tagName));
    }

    public static @Nullable Elements getAllElementsByAttribute(@Nullable @This Element element, String attribute) {
        return element == null ? null : element.getElementsByAttribute(requireNotEmpty(attribute));
    }

    public static @Nullable Elements getAllElementsByCss(@Nullable @This Element element, String cssQuery) {
        return element == null ? null : element.getElements(requireNotEmpty(cssQuery));
    }

    public static @Nullable Elements getAllElements(@Nullable @This Element element, Evaluator evaluator) {
        return element == null ? null : Collector.collect(evaluator, element);
    }

    // ----------------- \\
    // Get First Element \\
    // ----------------- \\

    public static @Nullable Element getFirstElementByClass(@Nullable @This Element element, String className) {
        return element == null ? null : getFirstElement(element, new Evaluator.Class(requireNotEmpty(className)));
    }

    public static @Nullable Element getFirstElementByTag(@Nullable @This Element element, String tagName) {
        return element == null ? null : getFirstElement(element, new Evaluator.Tag(requireNotEmpty(tagName)));
    }

    public static @Nullable Element getFirstElementByAttribute(@Nullable @This Element element, String attribute) {
        return element == null ? null : getFirstElement(element, new Evaluator.Attribute(requireNotEmpty(attribute)));
    }

    public static @Nullable Element getFirstElementByCss(@Nullable @This Element element, String cssQuery) {
        return element == null ? null : getFirstElement(element, QueryParser.parse(requireNotEmpty(cssQuery)));
    }

    public static @Nullable Element getFirstElementById(@Nullable @This Element element, String id) {
        return element == null ? null : getFirstElement(element, new Evaluator.Id(requireNotEmpty(id)));
    }

    public static @Nullable Element getFirstElement(@Nullable @This Element element, Evaluator evaluator) {
        return element == null ? null : Collector.findFirst(evaluator, element);
    }

    // ---------------- \\
    // Get n-th Element \\
    // ---------------- \\

    public static @Nullable Element getNthElementByClass(@Nullable @This Element element, String className, int index) {
        return element == null ? null : getNthElement(element, new Evaluator.Class(requireNotEmpty(className)), index);
    }

    public static @Nullable Element getNthElementByTag(@Nullable @This Element element, String tagName, int index) {
        return element == null ? null : getNthElement(element, new Evaluator.Tag(requireNotEmpty(tagName)), index);
    }

    public static @Nullable Element getNthElementByAttribute(@Nullable @This Element element, String attribute,
            int index) {
        return element == null ? null :
                getNthElement(element, new Evaluator.Attribute(requireNotEmpty(attribute)), index);
    }

    public static @Nullable Element getNthElementByCss(@Nullable @This Element element, String cssQuery, int index) {
        return element == null ? null : getNthElement(element, QueryParser.parse(requireNotEmpty(cssQuery)), index);
    }

    public static @Nullable Element getNthElement(@Nullable @This Element element, @Jailbreak Evaluator eval, int idx) {
        eval.reset();
        return new NthElementFinder(eval).find(element, element, idx);
    }

    // ----------------------- \\
    // Get First Element or Throw \\
    // ----------------------- \\

    public static Element getFirstElementByClassOrThrow(@Nullable @This Element element, String className)
            throws WebpageException {
        Element n = getFirstElementByClass(element, className);
        if (n == null) {
            throw new WebpageException("Could not find element with class '%s'".formatted(className));
        }
        return n;
    }

    public static Element getFirstElementByTagOrThrow(@Nullable @This Element element, String tagName)
            throws WebpageException {
        Element n = getFirstElementByTag(element, tagName);
        if (n == null) {
            throw new WebpageException("Could not find element with tag '%s'".formatted(tagName));
        }
        return n;
    }

    public static Element getFirstElementByAttributeOrThrow(@Nullable @This Element element, String attribute)
            throws WebpageException {
        Element n = getFirstElementByAttribute(element, attribute);
        if (n == null) {
            throw new WebpageException("Could not find element with attribute '%s'".formatted(attribute));
        }
        return n;
    }

    public static Element getFirstElementByCssOrThrow(@Nullable @This Element element, String cssQuery)
            throws WebpageException {
        Element n = getFirstElementByCss(element, cssQuery);
        if (n == null) {
            throw new WebpageException("Could not find element with css selector '%s'".formatted(cssQuery));
        }
        return n;
    }

    public static Element getFirstElementByIdOrThrow(@Nullable @This Element element, String id)
            throws WebpageException {
        Element n = getFirstElementById(element, id);
        if (n == null) {
            throw new WebpageException("Could not find element with id '%s'".formatted(id));
        }
        return n;
    }

    public static Element getFirstElementOrThrow(@Nullable @This Element element, Evaluator evaluator)
            throws WebpageException {
        Element n = getFirstElement(element, evaluator);
        if (n == null) {
            throw new WebpageException("Could not find element using selector '%s'".formatted(evaluator));
        }
        return n;
    }

    // ------------------------- \\
    // Get n-th Element or Throw \\
    // ------------------------- \\

    public static Element getNthElementByClassOrThrow(@Nullable @This Element element, String className, int index)
            throws WebpageException {
        Element elem = getNthElementByClass(element, className, index);
        if (elem == null) {
            throw new WebpageException("Could not find %sth element with class '%s'".formatted(index, className));
        }
        return elem;
    }

    public static Element getNthElementByTagOrThrow(@Nullable @This Element element, String tagName, int index)
            throws WebpageException {
        Element elem = getNthElementByTag(element, tagName, index);
        if (elem == null) {
            throw new WebpageException("Could not find %sth element with tag '%s'".formatted(index, tagName));
        }
        return elem;
    }

    public static Element getNthElementByAttributeOrThrow(@Nullable @This Element element, String attribute, int index)
            throws WebpageException {
        Element elem = getNthElementByAttribute(element, attribute, index);
        if (elem == null) {
            throw new WebpageException("Could not find %sth element with attribute '%s'".formatted(index, attribute));
        }
        return elem;
    }

    public static Element getNthElementByCssOrThrow(@Nullable @This Element element, String cssQuery, int index)
            throws WebpageException {
        Element elem = getNthElementByCss(element, cssQuery, index);
        if (elem == null) {
            throw new WebpageException("Could not find %sth element with css selector '%s'".formatted(index, cssQuery));
        }
        return elem;
    }

    public static Element getNthElementOrThrow(@Nullable @This Element element, Evaluator eval, int index)
            throws WebpageException {
        Element elem = getNthElement(element, eval, index);
        if (elem == null) {
            throw new WebpageException(
                    "Could not find %sth element using selector '%s'".formatted(index, eval.toString()));
        }
        return elem;
    }

    // ------------ \\
    // Get Elements \\
    // ------------ \\

    public static Elements getElements(@Nullable @This Element element, String cssQuery) {
        return element == null ? new Elements() : element.select(cssQuery);
    }

    // ------------ \\
    // Element methods \\
    // ------------ \\


    public static String getText(@Nullable @This Element element) {
        return element == null ? "" : element.text();
    }


    // --------------- \\
    // Utility methods \\
    // --------------- \\

    private static String requireNotEmpty(String value) {
        Validate.notEmpty(value);
        return value;
    }
}
