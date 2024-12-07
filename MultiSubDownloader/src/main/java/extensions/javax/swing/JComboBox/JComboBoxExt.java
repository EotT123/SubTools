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

    //    public static <E> E getSelectedObject(@This JComboBox<E> comboBox) {
    //        return (E) comboBox.getSelectedItem();
    //    }

    public static <E> E getSelectedValue(@This JComboBox<E> comboBox) {
        return (E) comboBox.getSelectedItem();
    }
    //
    //    public static <E> Optional<E> getSelectedOptionalValue(@This JComboBox<E> comboBox) {
    //        return Optional.ofNullable((E) comboBox.getSelectedItem());
    //    }
    //
    //    public static <E> @Self JComboBox<E> model(@This JComboBox<E> comboBox, ComboBoxModel<E> model) {
    //        comboBox.setModel(model);
    //        return comboBox;
    //    }

    public static <E> @Self JComboBox<E> renderer(@This JComboBox<E> comboBox, ListCellRenderer<? super E> renderer) {
        comboBox.setRenderer(renderer);
        return comboBox;
    }

    //    public static <E> @Self JComboBox<E> renderer(@This JComboBox<E> comboBox,
    //            Function<ListCellRenderer, ListCellRenderer<? super E>> rendererFunction) {
    //        comboBox.setRenderer(rendererFunction.apply(comboBox.getRenderer()));
    //        return comboBox;
    //    }

    public static <E> @Self JComboBox<E> toStringRenderer(@This JComboBox<E> comboBox,
            Function<E, String> toStringRenderer) {
        return comboBox.renderer(ToStringListCellRenderer.of(comboBox.getRenderer(), toStringRenderer));
    }

    public static <E> @Self JComboBox<E> toMessageStringRenderer(@This JComboBox<E> comboBox,
            Function<E, String> toStringRenderer) {
        return comboBox.renderer(ToStringListCellRenderer.ofMessage(comboBox.getRenderer(), toStringRenderer));
    }


    //    public static <E> @Self JComboBox<E> itemListener(@This JComboBox<E> comboBox, ItemListener itemListener) {
    //        comboBox.addItemListener(itemListener);
    //        return comboBox;
    //    }

    public static <E> @Self JComboBox<E> itemListener(@This JComboBox<E> comboBox, Runnable itemListener) {
        comboBox.addItemListener(_ -> itemListener.run());
        return comboBox;
    }


    public static <E> @Self JComboBox<E> selectedValue(@This JComboBox<E> comboBox, @Nullable E item) {
        comboBox.setSelectedItem(item);
        return comboBox;
    }

    //
    //    public static <E> @Self JComboBox<E> selectedIndex(@This JComboBox<E> comboBox, int index) {
    //        comboBox.setSelectedIndex(index);
    //        return comboBox;
    //    }
    //
    //    public static <E> @Self JComboBox<E> actionListener(@This JComboBox<E> comboBox, ActionListener
    //    actionListener) {
    //        comboBox.addActionListener(actionListener);
    //        return comboBox;
    //    }
    //
    //    public static <E> @Self JComboBox<E> eventConsumer(@This JComboBox<E> comboBox,
    //            Consumer<JComboBox<E>> actionListener) {
    //        //noinspection unchecked
    //        comboBox.addActionListener(event -> actionListener.accept((JComboBox<E>) event.getSource()));
    //        return comboBox;
    //    }
    //
    public static <E> @Self JComboBox<E> selectedItemConsumer(@This JComboBox<E> comboBox, Consumer<E>
            actionListener) {
        //noinspection unchecked
        comboBox.addActionListener(
                event -> actionListener.accept(((JComboBox<E>) (event.getSource())).getSelectedValue()));
        return comboBox;
    }
    //
    //    public static <E> @Self JComboBox<E> border(@This JComboBox<E> comboBox, Border border) {
    //        comboBox.setBorder(border);
    //        return comboBox;
    //    }
    //
    //    public static <E> @Self JComboBox<E> enabled(@This JComboBox<E> comboBox, boolean enabled) {
    //        comboBox.setEnabled(enabled);
    //        return comboBox;
    //    }
    //
    //    public static <E> @Self JComboBox<E> items(@This JComboBox<E> comboBox, E... items) {
    //        items.stream().forEach(comboBox::addItem);
    //        return comboBox;
    //    }
    //
    //    //    public static <E> @Self JComboBox<E> items(@This JComboBox<E> comboBox, Iterable<E> items) {
    //    //        items.stream().forEach(comboBox::addItem);
    //    //        return comboBox;
    //    //    }
    //    //
    //    //    public static <E> @Self JComboBox<E> items(@This JComboBox<E> comboBox, BaseStream<E, Stream<E>>
    //    items) {
    //    //        items.iterator().stream().forEach(comboBox::addItem);
    //    //        return comboBox;
    //    //    }
    //    //
    //    //    public static <E extends Enum<E>> @Self JComboBox<E> enumItems(@This JComboBox<E> comboBox) {
    //    //        Class<E> enumType = (Class<E>) TypeResolver.resolveRawArguments(JComboBox.class, comboBox
    //    .getClass())[0];
    //    //        return comboBox.items(enumType.getEnumConstants());
    //    //    }
    //
    //    public static <E> @Self JComboBox<E> toStringMapper(@This JComboBox<E> comboBox,
    //            Function<E, String> toStringMapper) {
    //        comboBox.setRenderer(new CustomComboBoxRenderer<>(toStringMapper));
    //        return comboBox;
    //    }
    //
    //    @RequiredArgsConstructor
    //    private static class CustomComboBoxRenderer<T> extends BasicComboBoxRenderer {
    //
    //        private final Function<T, String> toStringMapper;
    //
    //        @Override
    //        public Component getListCellRendererComponent(JList<?> list, Object value, int index, boolean isSelected,
    //                boolean cellHasFocus) {
    //            Object newValue = value instanceof Icon || value == null ? value : toStringMapper.apply((T) value);
    //            super.getListCellRendererComponent(list, newValue, index, isSelected, cellHasFocus);
    //            return this;
    //        }
    //    }
}
