package org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.model;

import java.nio.file.Path;

import name.falgout.jeffrey.throwing.ThrowingSupplier;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.exception.OpenSubtitleException;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.exception.SubtitlesProviderException;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.SubtitleMatchType;
import org.lodder.subtools.sublibrary.model.SubtitleSource;

public class OpenSubtilteSubtitle extends Subtitle {

    private final ThrowingSupplier<String, OpenSubtitleException> urlSupplier;

    public OpenSubtilteSubtitle(ThrowingSupplier<String, OpenSubtitleException> urlSupplier,
        @Nullable String fileName=null,
        @Nullable Language language=null,
        @Nullable String releaseGroup=null,
        @Nullable String uploader=null,
        @Nullable SubtitleMatchType subtitleMatchType=null,
        @Nullable SubtitleSource subtitleSource=null,
        @Nullable String quality=null,
        boolean hearingImpaired=false) {

        super(fileName, language, releaseGroup, uploader, subtitleMatchType, subtitleSource,
            hearingImpaired, quality);
        this.urlSupplier = urlSupplier;
    }

    public Path download() throws SubtitlesProviderException {

    }
}
