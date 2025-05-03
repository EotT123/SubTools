package org.lodder.subtools.multisubdownloader.subtitleproviders.subscene.model;

import java.nio.file.Path;

import com.pivovarit.function.ThrowingSupplier;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subscene.exception.SubsceneException;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.exception.SubtitlesProviderException;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.SubtitleMatchType;
import org.lodder.subtools.sublibrary.model.SubtitleSource;

public class SubsceneSubtitle extends Subtitle {

    private final ThrowingSupplier<String, SubsceneException> urlSupplier;

    public SubsceneSubtitle(ThrowingSupplier<String, SubsceneException> urlSupplier,
        @Nullable String fileName=null,
        @Nullable Language language=null,
        @Nullable String releaseGroup=null,
        @Nullable String uploader=null,
        SubtitleMatchType subtitleMatchType,
        SubtitleSource subtitleSource,
        boolean hearingImpaired=false,
        String quality) {

        super(fileName, language, releaseGroup, uploader, subtitleMatchType, subtitleSource,
            hearingImpaired, quality);
        this.urlSupplier = urlSupplier;
    }

    public Path download() throws SubtitlesProviderException {

    }
}
