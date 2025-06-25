package org.lodder.subtools.multisubdownloader.subtitleproviders.local.model;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.SubtitleSource;

public class LocalSubtitle extends Subtitle {

    private final Path path;

    public LocalSubtitle(Path path,
        @Nullable Language language=null,
        @Nullable String releaseGroup=null,
        @Nullable String quality=null) {

        super(path.fileNameAsString, language, releaseGroup, null, SubtitleSource.LOCAL, false, quality);
        this.path = path;
    }

    @Override
    public List<Path> download(Manager manager, Path destinationFolder,
        Function<AtomicInteger, String> fileNameFunction) throws IOException {
        Path subPath = destinationFolder.resolve(path.fileName);
        path.copyToDir(subPath);
        return List.of(subPath);
    }
}
