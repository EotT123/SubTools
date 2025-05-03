package org.lodder.subtools.multisubdownloader.subtitleproviders.local.model;

import java.nio.file.Path;

import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.exception.SubtitlesProviderException;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.SubtitleMatchType;
import org.lodder.subtools.sublibrary.model.SubtitleSource;

public class LocalSubtitle extends Subtitle {

    private final Path path;

    public LocalSubtitle(Path path,
        @Nullable String fileName=null,
        @Nullable Language language=null,
        @Nullable String releaseGroup=null,
        @Nullable String uploader=null,
        @Nullable SubtitleMatchType subtitleMatchType=null,
        @Nullable SubtitleSource subtitleSource=null,
        boolean hearingImpaired=false,
        @Nullable String quality=null) {

        super(fileName, language, releaseGroup, uploader, subtitleMatchType, subtitleSource,
            hearingImpaired, quality);
        this.path = path;
    }

    public Path download() throws SubtitlesProviderException {

    }
}
