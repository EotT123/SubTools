package extensions.javax.swing.JProgressBar;

import javax.swing.*;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.Self;
import manifold.ext.rt.api.This;


@Extension
public class JProgressBarExt {

    private JProgressBarExt() {
        // hide utility class constructor
    }

    public static @Self JProgressBar indeterminate(@This JProgressBar progressBar, boolean indeterminate) {
        progressBar.setIndeterminate(indeterminate);
        return progressBar;
    }
}
