package extensions.javax.swing.text.JTextComponent;

import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.Self;
import manifold.ext.rt.api.This;
import org.jspecify.annotations.NullMarked;

@Extension
@NullMarked
public class JTextComponentExt {

    private JTextComponentExt() {
        // Hide Utility Class Constructor
    }

    public static @Self JTextComponent editable(@This JTextComponent textComponent, boolean editable) {
        textComponent.setEditable(editable);
        return textComponent;
    }

    public static @Self JTextComponent editable(@This JTextComponent textComponent) {
        return textComponent.editable(true);
    }

    public static @Self JTextComponent notEditable(@This JTextComponent textComponent) {
        return textComponent.editable(false);
    }

    public static @Self JTextComponent documentListener(@This JTextComponent textComponent, DocumentListener listener) {
        textComponent.getDocument().addDocumentListener(listener);
        return textComponent;
    }
}
