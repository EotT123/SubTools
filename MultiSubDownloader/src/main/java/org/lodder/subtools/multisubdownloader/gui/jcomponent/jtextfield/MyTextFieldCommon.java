package org.lodder.subtools.multisubdownloader.gui.jcomponent.jtextfield;

import static util.Utils.*;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.Serial;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Predicate;

import manifold.ext.rt.api.Self;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.util.function.BooleanConsumer;

@NullMarked
public abstract sealed class MyTextFieldCommon<T extends @Nullable Object, R extends MyTextFieldCommon<T, R>>
    extends JTextField
    permits MyTextFieldInteger, MyTextFieldPath, MyTextFieldString {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_BORDER_PROPERTY = "DefaultBorder";
    private static final Border ERROR_BORDER = new LineBorder(Color.RED, 1);

    private final boolean requireValue;
    private final Function<T, String> toStringMapper;
    private final Function<String, T> toObjectMapper;
    private final @Nullable Consumer<T> valueChangedCallbackListener;
    private final List<BooleanConsumer> validityChangedCallbackListeners = new ArrayList<>();

    private final ObjectWrapper<T> valueWrapper = new ObjectWrapper<>();
    private final ObjectWrapper<Boolean> validWrapper = new ObjectWrapper<>();
    private final Predicate<String> completeValueVerifier;

    MyTextFieldCommon(boolean requireValue=false, Function<T, String> toStringMapper,
        Function<String, T> toObjectMapper, Predicate<String> valueVerifier,
        @Nullable Consumer<T> valueChangedCallbackListener) {
        putClientProperty(DEFAULT_BORDER_PROPERTY, getBorder());
        this.requireValue = requireValue;
        this.toStringMapper = toStringMapper;
        this.toObjectMapper = toObjectMapper;

        this.valueChangedCallbackListener = valueChangedCallbackListener;

        completeValueVerifier =
            requireValue ? text -> (StringUtils.isNotEmpty(text) && valueVerifier.test(text)) : valueVerifier;

        if (requireValue || valueChangedCallbackListener != null) {
            configureCallback();
        }
    }


    private void configureCallback() {
        checkValidity(getText());
        getDocument().addDocumentListener(new DocumentListener() {

            @Override
            public void insertUpdate(DocumentEvent e) {
                checkValidity(getText());
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                checkValidity(getText());
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                checkValidity(getText());
            }

        });
    }


    public @Self MyTextFieldCommon<T, R> addValidityChangedCallbackListeners(
        BooleanConsumer validityChangedCallbackListener) {
        if (!requireValue && valueChangedCallbackListener == null && validityChangedCallbackListeners.isEmpty()) {
            configureCallback();
        }
        return this;
    }

    @Override
    public void setBorder(Border border) {
        setSuperBorder(border);
        putClientProperty(DEFAULT_BORDER_PROPERTY, border);
    }

    public void setErrorBorder() {
        setBorder(ERROR_BORDER);
    }

    private void setSuperBorder(Border border) {
        super.setBorder(border);
    }

    @NullMarked
    private static class ObjectWrapper<S extends @Nullable Object> {
        private S value;

        public boolean setValue(S value) {
            boolean changed = this.value != value;
            this.value = value;
            return changed;
        }

        public S getValue() {
            return value;
        }
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        refreshState();
    }

    public void refreshState() {
        if (!isEnabled()) {
            setSuperBorder(getDefaultBorder(this));
        } else if (!completeValueVerifier.test(getText())) {
            setSuperBorder(ERROR_BORDER);
        }
    }

    private static Border getDefaultBorder(JComponent thisTextField) {
        return (Border) thisTextField.getClientProperty(DEFAULT_BORDER_PROPERTY);
    }

    private void checkValidity(String text) {
        boolean valid = completeValueVerifier.test(text);
        setSuperBorder(valid ? MyTextFieldCommon.getDefaultBorder(self()) : ERROR_BORDER);

        boolean changedValidity = validWrapper.setValue(valid);
        if (changedValidity) {
            validityChangedCallbackListeners.forEach(listener -> listener.accept(valid));
        }

        if (valueChangedCallbackListener != null) {
            T value = toObjectMapper.apply(text);
            boolean valueChanged = valueWrapper.setValue(toObjectMapper.apply(text));
            if (valueChanged) {
                valueChangedCallbackListener.accept(value);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private R self() {
        return (R) this;
    }

    public T getObject() {
        String text = super.getText();
        return completeValueVerifier.test(text) ? toObjectMapper.apply(text) : null;
    }

    public void setObject(T object) {
        super.setText(ifNotNull(object, toStringMapper::apply));
        valueWrapper.setValue(object);
        validWrapper.setValue(completeValueVerifier.test(toStringMapper.apply(object)));
    }

    public boolean hasValidValue() {
        return !isEnabled() || completeValueVerifier.test(getText());
    }
}
