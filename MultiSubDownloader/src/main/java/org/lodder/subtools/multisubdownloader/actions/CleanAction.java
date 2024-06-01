package org.lodder.subtools.multisubdownloader.actions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.lodder.subtools.multisubdownloader.settings.model.LibrarySettings;
import org.lodder.subtools.sublibrary.model.Release;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.experimental.ExtensionMethod;

@ExtensionMethod({ StringUtils.class, Files.class })
public class CleanAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanAction.class);
    private static final  String SAMPLE_DIR_NAME = "sample";

    private final LibrarySettings librarySettings;
    private final Set<String> fileFilters = Set.of("nfo", "jpg", "sfv", "srr", "srs", "nzb", "torrent", "txt");

    public CleanAction(LibrarySettings librarySettings) {
        this.librarySettings = librarySettings;
    }

    public void cleanUpFiles(Release release, Path destination, String videoFileName) throws IOException {
        LOGGER.trace("cleanUpFiles: LibraryOtherFileAction {}", librarySettings.getLibraryOtherFileAction());
        if (!destination.isDirectory()) {
            throw new IllegalArgumentException("Destination [%s] is not a folder".formatted(destination));
        }

        release.getPath().list().asThrowingStream(IOException.class)
                .filter(p -> (p.isDirectory() && p.fileNameContainsIgnoreCase(SAMPLE_DIR_NAME))
                        || (p.isRegularFile() && fileFilters.contains(p.getExtension())))
                .forEach(p -> {
                    switch (librarySettings.getLibraryOtherFileAction()) {
                        case MOVE -> move(p, destination);
                        case MOVEANDRENAME -> moveAndRename(p, destination, videoFileName);
                        case REMOVE -> delete(p);
                        case RENAME -> rename(p, destination, videoFileName);
                        case NOTHING -> {}
                        default -> {}
                    }
                });
    }

    private void rename(Path path, Path destinationFolder, String videoFileName) throws IOException {
        if (path.isRegularFile()) {
            String fileName = path.fileNameContainsIgnoreCase(SAMPLE_DIR_NAME) ? SAMPLE_DIR_NAME : StringUtils.substringBeforeLast(videoFileName, ".");
            String extension = path.getExtension();
            if (!extension.isBlank()) {
                extension = "." + extension;
            }
            path.move(path.resolveSibling(fileName + extension));
        } else {
            path.moveToDir(destinationFolder);
        }
    }

    private void delete(Path path) throws IOException {
        path.deletePath();
    }

    private void moveAndRename(Path path, Path destinationFolder, String videoFileName) throws IOException {
        if (path.isRegularFile()) {
            String fileName = path.fileNameContainsIgnoreCase(SAMPLE_DIR_NAME) ? SAMPLE_DIR_NAME : StringUtils.substringBeforeLast(videoFileName, ".");
            String extension = path.getExtension();
            if (!extension.isBlank()) {
                extension = "." + extension;
            }
            path.moveToDirAndRename(destinationFolder, fileName + extension);
        } else {
            path.moveToDir(destinationFolder);
        }
    }

    private void move(Path origin, Path destinationFolder) throws IOException {
        origin.moveToDir(destinationFolder, StandardCopyOption.REPLACE_EXISTING);
    }

}
