package org.lodder.subtools.sublibrary.util.filefilter;

import javax.swing.filechooser.*;
import java.io.File;

public abstract class ExtensionFileFilter extends FileFilter {

    @Override
    public boolean accept(File f) {
        return f.isDirectory() || getExtension().equals(f.toPath().getExtension());
    }

    public abstract String getExtension();
}
