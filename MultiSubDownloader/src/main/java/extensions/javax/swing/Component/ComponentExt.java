package extensions.javax.swing.Component;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseListener;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Predicate;

import lombok.experimental.UtilityClass;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.Self;
import manifold.ext.rt.api.This;

@UtilityClass
@Extension
public class ComponentExt {

    public static void setRecursive(@This Component component, Consumer<Component> consumer) {
        setRecursive(component, consumer, c -> true);
    }

    public static void setRecursive(@This Component component, Consumer<Component> consumer, Predicate<Container> condition) {
        if (component != null) {
            consumer.accept(component);
            if (component instanceof Container container && condition.test(container)) {
                container.getComponents().stream().forEach(child -> setRecursive(child, consumer, condition));
            }
        }
    }

    public static <T extends Component> @Self Component withMouseListener(@This T component, MouseListener listener) {
        component.addMouseListener(listener);
        return component;
    }
}
