package org.lodder.subtools.multisubdownloader.actions;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.lib.library.FilenameLibraryBuilder;
import org.lodder.subtools.multisubdownloader.lib.library.LibraryActionType;
import org.lodder.subtools.multisubdownloader.lib.library.LibraryOtherFileActionType;
import org.lodder.subtools.multisubdownloader.lib.library.PathLibraryBuilder;
import org.lodder.subtools.multisubdownloader.settings.model.LibrarySettings;
import org.lodder.subtools.multisubdownloader.settings.model.Settings;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.userinteraction.UserInteractionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(DownloadAction.class);

    private final Settings settings;
    private final Manager manager;
    private final UserInteractionHandler userInteractionHandler;

    public DownloadAction(Settings settings, Manager manager, UserInteractionHandler userInteractionHandler) {
        this.settings = settings;
        this.manager = manager;
        this.userInteractionHandler = userInteractionHandler;
    }

    public void download(Release release, Subtitle subtitle, @Nullable AtomicInteger counter=null) throws IOException {
        LOGGER.info("Downloading subtitle: [{}] for release: [{}]", subtitle.fileName, release.fileName);
        switch (release.videoType) {
            case EPISODE -> download(release, subtitle, settings.episodeLibrarySettings, counter);
            case MOVIE -> download(release, subtitle, settings.movieLibrarySettings, counter);
            default -> throw new IllegalArgumentException("Unexpected value: " + release.videoType);
        }
    }

    private void download(Release release, Subtitle subtitle, LibrarySettings librarySettings,
        @Nullable AtomicInteger counter) throws IOException {
        LOGGER.trace("cleanUpFiles: LibraryAction {}", librarySettings.action);
        Path path = PathLibraryBuilder.fromSettings(librarySettings, manager, userInteractionHandler).build(release);
        if (!path.exists()) {
            LOGGER.debug("Download creating folder [{}] ", path.toAbsolutePath());
            try {
                Files.createDirectories(path);
            } catch (IOException e) {
                throw new IOException("Download unable to create folder: " + path.toAbsolutePath(), e);
            }
        }

        FilenameLibraryBuilder filenameLibraryBuilder =
            FilenameLibraryBuilder.fromSettings(librarySettings, manager, userInteractionHandler);
        String videoFileName = filenameLibraryBuilder.build(release).toString();

        Function<AtomicInteger, String> fileNameFunction = counterOverride ->
            filenameLibraryBuilder.buildSubtitle(release, subtitle, videoFileName,
                counter == null ?
                    (counterOverride == null ? null : counterOverride.incrementAndGet()) :
                    Integer.valueOf(counter.incrementAndGet()));

        List<Path> downloadedSubtitles;
        try {
            downloadedSubtitles = subtitle.download(manager, path, fileNameFunction);
            LOGGER.debug("downloaded {} subtitles", downloadedSubtitles.size());
        } catch (IOException e) {
            throw new IOException(
                "Error while downloading subtitle for [${release.releaseDescription}] (" + e.getMessage() + ")", e);
        }
        if (downloadedSubtitles.isEmpty()) {
            return;
        }

        if (!librarySettings.hasLibraryAction(LibraryActionType.NOTHING)) {
            Path oldLocationFile = release.getPath().resolve(release.fileName);
            if (oldLocationFile.exists()) {
                LOGGER.info("Moving/Renaming [{}] to folder [{}] this might take a while... ", videoFileName, path);
                oldLocationFile.moveToDir(path);
                if (!librarySettings.hasLibraryOtherFileAction(LibraryOtherFileActionType.NOTHING)) {
                    CleanAction cleanAction = new CleanAction(librarySettings);
                    cleanAction.cleanUpFiles(release, path, videoFileName);
                }
                if (librarySettings.removeEmptyFolders && release.path.isEmptyDir()) {
                    release.path.deletePath();
                }
            }
        }
        if (librarySettings.backupSubtitle) {
            String langFolder = subtitle.language == null ? Language.ENGLISH.iso639_3 : subtitle.language.iso639_3;
            Path backupPath = librarySettings.backupSubtitlePath.resolve(langFolder);

            if (!backupPath.exists()) {
                try {
                    Files.createDirectories(backupPath);
                } catch (IOException e) {
                    throw new IOException("Download unable to create folder: " + backupPath.toAbsolutePath(), e);
                }
            }

            AtomicInteger subCounter = counter == null && downloadedSubtitles.size() < 2 ? null : new AtomicInteger(0);
            downloadedSubtitles.forEachEx(subFile -> {
                if (librarySettings.backupUseWebsiteFileName) {
                    if (subCounter != null) {
                        String filename = StringUtils.substringBeforeLast(subtitle.fileName, ".");
                        String extension = StringUtils.substringAfterLast(subtitle.fileName, ".");
                        subFile.copyToDirAndRename(backupPath,
                            filename + "-v" + subCounter.incrementAndGet() + "." + extension);
                    } else {
                        subFile.copyToDirAndRename(backupPath, subtitle.fileName);
                    }
                } else {
                    subFile.copyToDirAndRename(backupPath, subFile.fileNameAsString);
                }
            });
        }
    }
}
