package org.lodder.subtools.sublibrary.gui;

import javax.swing.*;
import java.awt.*;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import lombok.AllArgsConstructor;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class OptionsPane<T> {

    public static final Object LOCK = new Object();

    private final T[] options;
    private final @Nullable String title;
    private final @Nullable String message;
    private final Option messageType;
    private final @Nullable Function<T, String> toStringMapper;
    private final @Nullable Component parent;

    public OptionsPane(Iterable<T> options, @Nullable Function<T, String> toStringMapper=null,
        @Nullable String title=null,
        @Nullable String message=null, Option messageType, @Nullable Component parent=null) {
        this.options = (T[]) StreamSupport.stream(options.spliterator(), false).toArray();
        this.toStringMapper = toStringMapper;
        this.title = title;
        this.message = message;
        this.messageType = messageType;
        this.parent = parent;
    }


    public Optional<T> prompt() {
        synchronized (LOCK) {
            if (toStringMapper == null) {
                return Optional.ofNullable(
                    (T) JOptionPane.showInputDialog(parent, message, title, messageType.value, null, options, "0"));
            } else {
                ElementWrapper<T>[] optionsWrapper =
                    options.stream().map(option -> new ElementWrapper<>(option, toStringMapper))
                        .toArray(ElementWrapper[]::new);
                return Optional.ofNullable(
                        (ElementWrapper<T>) JOptionPane.showInputDialog(parent, message, title, messageType.value, null,
                            optionsWrapper, "0"))
                    .map(ElementWrapper::element);
            }
        }
    }

    private record ElementWrapper<T>(T element, Function<T, String> toStringMapper) {
        @Override
        public @NonNull String toString() {
            return toStringMapper.apply(element);
        }
    }

    @AllArgsConstructor
    public enum Option {
        DEFAULT(JOptionPane.DEFAULT_OPTION),
        YES_NO(JOptionPane.YES_NO_OPTION),
        YES_NO_CANCEL(JOptionPane.YES_NO_CANCEL_OPTION),
        OK_CANCEL(JOptionPane.OK_CANCEL_OPTION);

        @val int value;
    }
}
