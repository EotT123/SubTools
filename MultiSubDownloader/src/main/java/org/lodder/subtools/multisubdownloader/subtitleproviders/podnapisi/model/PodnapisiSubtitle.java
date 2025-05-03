package org.lodder.subtools.multisubdownloader.subtitleproviders.podnapisi.model;

import java.nio.file.Path;

import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.exception.SubtitlesProviderException;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.SubtitleMatchType;
import org.lodder.subtools.sublibrary.model.SubtitleSource;

public class PodnapisiSubtitle extends Subtitle {

    private final String url;

    public PodnapisiSubtitle(String url,
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
