package extensions.org.w3c.dom.NodeList;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@Extension
@NullMarked
public class NodeListExt {

    private NodeListExt() {
        // Hide Utility Class Constructor
    }

    public static Stream<Node> stream(@This @Nullable NodeList nodeList) {
        return nodeList == null ? Stream.of() : IntStream.range(0, nodeList.getLength()).mapToObj(nodeList::item);
    }
}
