package extensions.javax.swing.JComboBox;

import javax.annotation.Nullable;
import javax.swing.*;
import java.util.Collection;
import java.util.function.Consumer;
import java.util.function.Function;

import com.google.common.collect.Iterables;
import lombok.experimental.UtilityClass;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.Self;
import manifold.ext.rt.api.This;
import manifold.ext.rt.api.ThisClass;
import net.jodah.typetools.TypeResolver;
import org.lodder.subtools.multisubdownloader.gui.ToStringListCellRenderer;

@UtilityClass
@Extension
public class JComboBoxExt {

    public static <E> JComboBox<E> create(@ThisClass Class<JComboBox<E>> thisClass, E... values) {
        return new JComboBox<>(values);
    }

    public static <E> JComboBox<E> create(@ThisClass Class<JComboBox<E>> thisClass, Collection<E> items) {
        Class<E> elementType = (Class<E>) TypeResolver.resolveRawArguments(Collection.class, items.getClass())[0];
        return new JComboBox<>(Iterables.toArray(items, elementType));
    }

    public static <E> E getSelectedValue(@This JComboBox<E> comboBox) {
        return (E) comboBox.getSelectedItem();
    }

    public static <E> @Self JComboBox<E> renderer(@This JComboBox<E> comboBox, ListCellRenderer<? super E> renderer) {
        comboBox.setRenderer(renderer);
        return comboBox;
    }

    public static <E> @Self JComboBox<E> toStringRenderer(@This JComboBox<E> comboBox,
            Function<E, String> toStringRenderer) {
        return comboBox.renderer(ToStringListCellRenderer.of(comboBox.getRenderer(), toStringRenderer));
    }

    public static <E> @Self JComboBox<E> toMessageStringRenderer(@This JComboBox<E> comboBox,
            Function<E, String> toStringRenderer) {
        return comboBox.renderer(ToStringListCellRenderer.ofMessage(comboBox.getRenderer(), toStringRenderer));
    }

    public static <E> @Self JComboBox<E> itemListener(@This JComboBox<E> comboBox, Runnable itemListener) {
        comboBox.addItemListener(_ -> itemListener.run());
        return comboBox;
    }


    public static <E> @Self JComboBox<E> selectedValue(@This JComboBox<E> comboBox, @Nullable E item) {
        comboBox.setSelectedItem(item);
        return comboBox;
    }

    public static <E> @Self JComboBox<E> selectedItemConsumer(@This JComboBox<E> comboBox, Consumer<E>
            actionListener) {
        //noinspection unchecked
        comboBox.addActionListener(ae -> actionListener.accept(((JComboBox<E>) (ae.getSource())).getSelectedValue()));
        return comboBox;
    }
}
