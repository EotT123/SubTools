package org.lodder.subtools.multisubdownloader.gui;

import static org.lodder.subtools.multisubdownloader.Messages.*;

import javax.swing.*;
import java.awt.*;
import java.util.function.Function;

import org.jspecify.annotations.NullMarked;

@NullMarked
public final class ToStringListCellRenderer<T> implements ListCellRenderer<T> {

    private final ListCellRenderer originalRenderer;
    private final Function<T, String> toStringMapper;

    private ToStringListCellRenderer(ListCellRenderer originalRenderer, Function<T, String> toStringMapper) {
        this.originalRenderer = originalRenderer;
        this.toStringMapper = toStringMapper;
    }

    public static <T> ToStringListCellRenderer<T> of(ListCellRenderer originalRenderer,
            Function<T, String> toStringMapper) {
        return new ToStringListCellRenderer<>(originalRenderer, toStringMapper);
    }

    public static <T> ToStringListCellRenderer<T> ofMessage(ListCellRenderer originalRenderer,
            Function<T, String> toStringMapper) {
        return of(originalRenderer, item -> getText(toStringMapper.apply(item)));
    }

    @Override
    public Component getListCellRendererComponent(JList<? extends T> list, T value, int index, boolean isSelected,
            boolean cellHasFocus) {
        return originalRenderer.getListCellRendererComponent(list, toStringMapper.apply(value), index, isSelected,
                cellHasFocus);
    }

}
