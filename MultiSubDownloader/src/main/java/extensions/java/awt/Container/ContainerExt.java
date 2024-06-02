package extensions.java.awt.Container;

import java.awt.*;

import lombok.experimental.UtilityClass;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.Self;
import manifold.ext.rt.api.This;

@UtilityClass
@Extension
public class ContainerExt {

    public static <S extends Component> @Self Container addComponent(@This Container component, S child) {
        component.add(child);
        return component;
    }

    public static <S extends Component> @Self Container addComponent(@This Container component, S child, Object constraints) {
        component.add(child, constraints);
        return component;
    }

    public static <S extends Component> @Self Container addComponent(@This Container component, Object constraints, S child) {
        component.add(child, constraints);
        return component;
    }

    public static @Self Container layout(@This Container container, LayoutManager mgr) {
        container.setLayout(mgr);
        return container;
    }
}
