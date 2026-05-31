package extensions.javax.swing.JProgressBar;

import javax.swing.*;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.Self;
import manifold.ext.rt.api.This;
import org.jspecify.annotations.NullMarked;


@Extension
@NullMarked
public class JProgressBarExt {

    private JProgressBarExt() {
        // Hide Utility Class Constructor
    }

    public static @Self JProgressBar indeterminate(@This JProgressBar progressBar, boolean indeterminate) {
        progressBar.setIndeterminate(indeterminate);
        return progressBar;
    }
}
