package extensions.org.w3c.dom.NodeList;

import java.util.stream.IntStream;
import java.util.stream.Stream;

import lombok.experimental.UtilityClass;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

@UtilityClass
@Extension
public class XmlExtension {

    public static Stream<Node> stream(@This NodeList nodeList) {
        return IntStream.range(0, nodeList.getLength()).mapToObj(nodeList::item);
    }
}
