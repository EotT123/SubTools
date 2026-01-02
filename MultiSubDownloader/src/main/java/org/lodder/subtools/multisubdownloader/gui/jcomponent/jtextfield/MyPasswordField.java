package org.lodder.subtools.multisubdownloader.gui.jcomponent.jtextfield;

import javax.swing.*;
import javax.swing.border.Border;
import javax.swing.border.LineBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.io.Serial;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Predicate;

import manifold.ext.props.rt.api.var;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.util.function.BooleanConsumer;

@NullMarked
public class MyPasswordField extends JPasswordField {

    @Serial
    private static final long serialVersionUID = 1L;
    private static final String DEFAULT_BORDER_PROPERTY = "DefaultBorder";
    private static final Border ERROR_BORDER = new LineBorder(Color.RED, 1);

    public final Predicate<String> valueVerifier;

    private @Nullable Consumer<String> valueChangedCallbackListener;
    private BooleanConsumer @Nullable [] validityChangedCallbackListeners;

    private final ObjectWrapper<String> valueWrapper = new ObjectWrapper<>();
    private final ObjectWrapper<Boolean> validWrapper = new ObjectWrapper<>();
    private final Predicate<String> completeValueVerifier;

    public MyPasswordField(boolean requireValue=false, Predicate<String> verifier=StringUtils::isNotEmpty,
        @Nullable Consumer<String> valueChangedCallbackListener=null,
        BooleanConsumer @Nullable ... validityChangedCallbackListeners) {
        super();
        putClientProperty(DEFAULT_BORDER_PROPERTY, getBorder());
        this.valueVerifier = verifier;
        this.valueChangedCallbackListener = valueChangedCallbackListener;
        this.validityChangedCallbackListeners = validityChangedCallbackListeners;

        this.completeValueVerifier =
            requireValue ? text -> (StringUtils.isNotEmpty(text) && valueVerifier.test(text)) : valueVerifier;

        if (requireValue || valueChangedCallbackListener != null || validityChangedCallbackListeners != null) {
            checkValidity(getRawText());
            getDocument().addDocumentListener(new DocumentListener() {

                @Override
                public void insertUpdate(DocumentEvent e) {
                    checkValidity(getRawText());
                }

                @Override
                public void removeUpdate(DocumentEvent e) {
                    checkValidity(getRawText());
                }

                @Override
                public void changedUpdate(DocumentEvent e) {
                    checkValidity(getRawText());
                }

            });
        }
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
    private static class ObjectWrapper<S> {
        @var S value;
    }

    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
        refreshState();
    }

    public void refreshState() {
        if (!isEnabled()) {
            setSuperBorder(getDefaultBorder(this));
        } else if (!completeValueVerifier.test(getRawText())) {
            setSuperBorder(ERROR_BORDER);
        }
    }

    private static Border getDefaultBorder(JComponent thisTextField) {
        return (Border) thisTextField.getClientProperty(DEFAULT_BORDER_PROPERTY);
    }

    private void checkValidity(String text) {
        boolean valid = completeValueVerifier.test(text);
        setSuperBorder(valid ? MyPasswordField.getDefaultBorder(this) : ERROR_BORDER);

        boolean changedValidity = Objects.equals(validWrapper.value, valid);
        validWrapper.value = valid;
        if (changedValidity && validityChangedCallbackListeners != null) {
            validityChangedCallbackListeners.forEach(listener -> listener.accept(valid));
        }

        if (valueChangedCallbackListener != null) {
            boolean valueChanged = !StringUtils.equals(valueWrapper.value, text);
            valueWrapper.value = text;
            if (valueChanged) {
                valueChangedCallbackListener.accept(text);
            }
        }
    }

    private String getRawText() {
        return new String(getPassword());
    }

    @Override
    public @Nullable String getText() {
        String text = new String(getPassword());
        return completeValueVerifier.test(text) ? text : null;
    }

    @Override
    public void setText(String password) {
        super.setText(password);
        valueWrapper.value = password;
        validWrapper.value = completeValueVerifier.test(password);
    }

    public boolean hasValidValue() {
        return !isEnabled() || completeValueVerifier.test(getRawText());
    }
}
