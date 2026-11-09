package org.lodder.subtools.multisubdownloader.actions;

import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import extensions.java.nio.file.Path.PathExt;
import manifold.ext.props.rt.api.set;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.listener.IndexingProgressListener;
import org.lodder.subtools.multisubdownloader.settings.SettingsControl;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.control.VideoPatterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
public class FileListAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileListAction.class);
    private static final String SUBTITLE_EXTENSION = "srt";

    @set @Nullable IndexingProgressListener indexingProgressListener;

    public List<Path> getFileListing(Path dir, boolean recursive, Language language, boolean forceSubtitleOverwrite) {
        LOGGER.trace("getFileListing: dir [{}] Recursive [{}] languageCode [{}] forceSubtitleOverwrite [{}]",
            dir, recursive, language, forceSubtitleOverwrite);
        int progressFileIndex = 0;
        int progressFilesTotal = 0;

        /* Start listing process */
        final List<Path> filelist = new ArrayList<>();
        List<Path> contents;
        try {
            contents = dir.list().toList();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return List.of();
        }

        /* Increase progressTotalFiles count */
        progressFilesTotal += contents.size();

        if (this.indexingProgressListener != null) {
            this.indexingProgressListener.progress(dir.toString());
        }

        for (Path file : contents) {
            progressFileIndex++;

            /* Update progressListener */
            if (this.indexingProgressListener != null) {
                /* Tell the progress listener the overall progress */
                int progress = (int) Math.floor((float) progressFileIndex / progressFilesTotal * 100);
                this.indexingProgressListener.progress(progress);
            }

            try {
                if (file.isRegularFile()) {
                    if (isValidVideoFile(file) && (forceSubtitleOverwrite || !fileHasSubtitles(file, language)) &&
                        !isExcludedFile(file)) {
                        filelist.add(file);
                    }
                } else if (recursive && !isExcludedDir(file)) {
                    filelist.addAll(getFileListing(file, recursive, language, forceSubtitleOverwrite));
                    if (this.indexingProgressListener != null) {
                        this.indexingProgressListener.progress(dir.toString());
                    }
                }
            } catch (IOException e) {
                LOGGER.error(e.getMessage(), e);
            }
        }
        return filelist;
    }

    private boolean isExcludedDir(Path path) {
        boolean excludedDir = SettingsControl.settings.excludeList.stream().anyMatch(item -> item.isExcludedPath(path));
        if (excludedDir) {
            LOGGER.trace("isExcludedDir, skipping [{}]", path);
        }
        return excludedDir;
    }

    private boolean isExcludedFile(Path path) {
        boolean excludedFile =
            SettingsControl.settings.excludeList.stream().anyMatch(item -> item.isExcludedPath(path));
        if (excludedFile) {
            LOGGER.trace("isExcludedFile, skipping [{}]", path);
        }
        return excludedFile;
    }

    public boolean isValidVideoFile(Path file) {
        return VideoPatterns.EXTENSIONS.contains(file.getExtension()) && !file.getFileNameAsString().contains("sample");
    }

    public boolean fileHasSubtitles(Path file, Language language) throws IOException {
        String extension = file.getExtension();
        String subtitleName = VideoPatterns.EXTENSIONS.stream()
            .filter(extension::equals)
            .map(_ -> file.changeExtension(SUBTITLE_EXTENSION))
            .findAny().orElse(null);

        if (subtitleName == null) {
            return false;
        }
        Path f = file.resolveSibling(subtitleName);
        if (f.exists()) {
            return true;
        } else {
            String subtitleExtensionWithDot = "." + SUBTITLE_EXTENSION;

            Set<String> langCodes = Set.of(language.iso639_3, language.iso639_1);
            List<String> filters = langCodes.stream().map(word -> word + "." + SUBTITLE_EXTENSION).toList();
            String subtitleNameWithoutExtension = subtitleName.replace(subtitleExtensionWithDot, "");
            return file.getParent()
                .list()
                .map(PathExt::getFileNameAsString)
                .filter(fileName -> filters.stream().anyMatch(fileName::endsWith))
                .anyMatch(fileName -> fileName.contains(subtitleNameWithoutExtension));
        }
    }
}
