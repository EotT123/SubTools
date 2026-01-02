package org.lodder.subtools.multisubdownloader.actions;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.settings.model.LibrarySettings;
import org.lodder.subtools.sublibrary.model.ReleaseWithPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
public class CleanAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(CleanAction.class);
    private static final String SAMPLE_DIR_NAME = "sample";
    private static final Set<String> FILE_FILTERS = Set.of("nfo", "jpg", "sfv", "srr", "srs", "nzb", "torrent", "txt");

    private final LibrarySettings librarySettings;

    public CleanAction(LibrarySettings librarySettings) {
        this.librarySettings = librarySettings;
    }

    public void cleanUpFiles(ReleaseWithPath release, Path destination, String videoFileName) throws IOException {
        LOGGER.trace("cleanUpFiles: LibraryOtherFileAction {}", librarySettings.otherFileAction);
        if (!destination.isDirectory()) {
            throw new IllegalArgumentException("Destination [%s] is not a folder".formatted(destination));
        }

        release.path.list()
            .filter(p -> (p.isDirectory() && p.fileNameContainsIgnoreCase(SAMPLE_DIR_NAME))
                || (p.isRegularFile() && FILE_FILTERS.contains(p.getExtension())))
            .forEachEx(p -> {
                switch (librarySettings.otherFileAction) {
                    case MOVE -> move(p, destination);
                    case MOVE_AND_RENAME -> moveAndRename(p, destination, videoFileName);
                    case REMOVE -> delete(p);
                    case RENAME -> rename(p, destination, videoFileName);
                    case NOTHING -> {
                    }
                    default -> {
                    }
                }
            });
    }

    private void rename(Path path, Path destinationFolder, String videoFileName) throws IOException {
        if (path.isRegularFile()) {
            String fileName =
                path.fileNameContainsIgnoreCase(SAMPLE_DIR_NAME) ? SAMPLE_DIR_NAME :
                    StringUtils.substringBeforeLast(videoFileName, ".");
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
            String fileName =
                path.fileNameContainsIgnoreCase(SAMPLE_DIR_NAME) ? SAMPLE_DIR_NAME :
                    StringUtils.substringBeforeLast(videoFileName, ".");
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
