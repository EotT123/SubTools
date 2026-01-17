package org.lodder.subtools.multisubdownloader.actions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.lib.library.FilenameLibraryBuilder;
import org.lodder.subtools.multisubdownloader.lib.library.LibraryActionType;
import org.lodder.subtools.multisubdownloader.lib.library.LibraryOtherFileActionType;
import org.lodder.subtools.multisubdownloader.lib.library.PathLibraryBuilder;
import org.lodder.subtools.multisubdownloader.settings.model.LibrarySettings;
import org.lodder.subtools.sublibrary.DetectLanguage;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.ReleaseWithPath;
import org.lodder.subtools.sublibrary.userinteraction.UserInteractionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
public class MoveAndRenameAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(MoveAndRenameAction.class);

    private final LibrarySettings librarySettings;
    private final Manager manager;
    private final UserInteractionHandler userInteractionHandler;

    public MoveAndRenameAction(LibrarySettings librarySettings, Manager manager,
        UserInteractionHandler userInteractionHandler) {
        this.librarySettings = librarySettings;
        this.manager = manager;
        this.userInteractionHandler = userInteractionHandler;
    }

    public void moveAndRename(Path f, ReleaseWithPath release) {
        String filename = switch (librarySettings.action) {
            case MOVE, NOTHING -> f.fileNameAsString;
            case MOVE_AND_RENAME, RENAME -> getNewFilename(f, release);
        };
        LOGGER.trace("rename: filename [{}]", filename);


        Path newDir = switch (librarySettings.action) {
            case MOVE, MOVE_AND_RENAME ->
                PathLibraryBuilder.fromSettings(librarySettings, manager, userInteractionHandler).buildPath(release);
            case RENAME, NOTHING -> release.path.parent;
        };
        if (!newDir.exists()) {
            LOGGER.debug("Creating dir [{}]", newDir.toAbsolutePath());
            try {
                Files.createDirectories(newDir);
            } catch (IOException e) {
                LOGGER.error("Could not create dir [%s]".formatted(newDir), e);
                return;
            }
        }
        LOGGER.trace("rename: newDir [{}]", newDir);

        Path file = release.path.parent.resolve(release.fileName);

        try {
            if (librarySettings.hasLibraryAction(LibraryActionType.MOVE) ||
                librarySettings.hasLibraryAction(LibraryActionType.MOVE_AND_RENAME)) {
                LOGGER.info("Moving [{}] to the library folder [{}] , this might take a while... ", filename, newDir);
                file.moveToDirAndRename(newDir, filename);
            } else {
                LOGGER.info("Moving [{}] to the library folder [{}] , this might take a while... ", filename,
                    release.path.parent);
                file.moveToDirAndRename(release.path.parent, filename);
            }
            if (!librarySettings.hasLibraryOtherFileAction(LibraryOtherFileActionType.NOTHING)) {
                new CleanAction(librarySettings).cleanUpFiles(release, newDir, filename);
            }

            if (librarySettings.removeEmptyFolders && release.path.parent.isEmptyDir()) {
                Files.delete(release.path.parent);
            }
        } catch (IOException e) {
            LOGGER.error("Unsuccessful in moving the file to the library", e);
        }
    }

    private String getNewFilename(Path f, Release release) {
        FilenameLibraryBuilder filenameLibraryBuilder =
            FilenameLibraryBuilder.fromSettings(librarySettings, manager, userInteractionHandler);
        String filename = filenameLibraryBuilder.buildPathStructure(release);
        if (release.fileNameOrName.endsWith(".srt")) {
            Language language = null;
            if (librarySettings.includeLanguageCode) {
                language = DetectLanguage.execute(f);
                if (language == null) {
                    LOGGER.error("Unable to detect language, leaving language code blank");
                }
            }
            return filenameLibraryBuilder.buildSubtitle(release.folderNameOrName, filename, language, 0);
        }
        return filename;
    }
}
