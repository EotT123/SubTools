package org.lodder.subtools.multisubdownloader.subtitleproviders.subdl.model;

import java.nio.file.Path;

import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.exception.SubtitlesProviderException;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.SubtitleMatchType;
import org.lodder.subtools.sublibrary.model.SubtitleSource;

public class SubdlSubtitle extends Subtitle {

    private final String url;

    public SubdlSubtitle(String url,
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
        this.url = url;
    }

    public Path download() throws SubtitlesProviderException {

    }
}
