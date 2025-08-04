package extensions.org.w3c.dom.Node;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

@Extension
@NullMarked
public class NodeExt {

    private NodeExt() {
        // hide utility class constructor
    }

    public static @Nullable String getAttribute(@This @Nullable Node node, String attribute) {
        if (node == null) {
            return null;
        }
        NamedNodeMap attributes = node.getAttributes();
        if (attributes == null) {
            return null;
        }
        Node attributeNode = attributes.getNamedItem(attribute);
        if (attributeNode == null) {
            return null;
        }
        return attributeNode.getNodeValue();
    }
}
