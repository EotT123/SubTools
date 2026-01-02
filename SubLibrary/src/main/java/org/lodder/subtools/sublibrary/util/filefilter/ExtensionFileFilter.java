package org.lodder.subtools.sublibrary.util.filefilter;

import javax.swing.filechooser.FileFilter;
import java.io.File;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract sealed class ExtensionFileFilter extends FileFilter permits JsonFileFilter, XmlFileFilter {

    @val abstract String extension;

    @Override
    public boolean accept(File f) {
        return f.isDirectory() || extension.equals(f.toPath().getExtension());
    }
}
