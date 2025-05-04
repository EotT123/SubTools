package org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.model;

import java.io.IOException;
import java.nio.file.Path;

import name.falgout.jeffrey.throwing.ThrowingSupplier;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.exception.OpenSubtitleException;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.ManagerException;
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

    @Override
    public boolean download(Manager manager,
        Path destination) throws OpenSubtitleException, IOException, ManagerException {
        return manager.download(urlSupplier.get(), destination);
    }
}
