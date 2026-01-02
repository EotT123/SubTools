package extensions.org.jsoup.nodes.Element;

import static org.jsoup.select.NodeFilter.FilterResult.*;

import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Evaluator;
import org.jsoup.select.NodeFilter;
import org.jsoup.select.NodeTraversor;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
class NthElementFinder implements NodeFilter {
    private final Evaluator eval;
    private Element evalRoot;
    private @Nullable Element match;
    private int index;
    private int currentIdx;

    public NthElementFinder(Evaluator eval) {
        this.eval = eval;
    }

    @Nullable Element find(Element root, Element start, int index) {
        this.index = index;
        this.evalRoot = root;
        this.match = null;
        NodeTraversor.filter(this, start);
        return match;
    }

    @Override
    public FilterResult head(Node node, int depth) {
        if (node instanceof Element el && eval.matches(evalRoot, el) && currentIdx++ == index) {
            match = el;
            return STOP;
        }
        return CONTINUE;
    }
}