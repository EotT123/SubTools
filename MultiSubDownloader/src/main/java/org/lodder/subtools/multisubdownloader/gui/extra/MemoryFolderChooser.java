package org.lodder.subtools.multisubdownloader.gui.extra;

import static util.Utils.*;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.nio.file.Path;
import java.util.Optional;

import manifold.ext.props.rt.api.var;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.util.lazy.LazySupplier;

@NullMarked
public class MemoryFolderChooser {

    private static final LazySupplier<MemoryFolderChooser> instance = new LazySupplier<>(MemoryFolderChooser::new);
    private final JFileChooser chooser;
    @var @Nullable Path memory;

    private MemoryFolderChooser() {
        chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        chooser.setAcceptAllFileFilterUsed(false);
    }

    public static MemoryFolderChooser getInstance() {
        return instance.get();
    }

    public Optional<Path> selectDirectory(Component c, String title, @Nullable Path path) {
        return selectDirectory(c, title, ifNotNull(path, Path::toFile));
    }

    public Optional<Path> selectDirectory(Component c, String title, @Nullable File file) {
        chooser.setDialogTitle(title);
        if (file != null && !StringUtils.isBlank(file.getAbsolutePath())) {
            chooser.setCurrentDirectory(ifNotNullOrElseGet(memory, Path::toFile, () -> new File(".")));
        } else {
            chooser.setCurrentDirectory(file);
        }

        int result = chooser.showOpenDialog(c);
        if (result == JFileChooser.APPROVE_OPTION) {
            memory = chooser.getSelectedFile().toPath();
            return Optional.of(chooser.getSelectedFile().toPath());
        }
        return Optional.empty();
    }

    public Optional<Path> selectDirectory(Component c, String title) {
        return selectDirectory(c, title, memory);
    }
}
