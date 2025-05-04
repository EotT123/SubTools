package org.lodder.subtools.multisubdownloader.subtitleproviders.subscene.model;

import java.io.IOException;
import java.nio.file.Path;

import name.falgout.jeffrey.throwing.ThrowingSupplier;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subscene.exception.SubsceneException;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.ManagerException;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.SubtitleSource;

public class SubsceneSubtitle extends Subtitle {

    private final ThrowingSupplier<String, SubsceneException> urlSupplier;

    public SubsceneSubtitle(ThrowingSupplier<String, SubsceneException> urlSupplier,
        @Nullable String fileName=null,
        @Nullable Language language=null,
        @Nullable String releaseGroup=null,
        @Nullable String uploader=null,
        boolean hearingImpaired=false,
        String quality) {

        super(fileName, language, releaseGroup, uploader, SubtitleSource.SUBSCENE, hearingImpaired, quality);
        this.urlSupplier = urlSupplier;
    }

    @Override
    public boolean download(Manager manager, Path destination) throws SubsceneException, IOException, ManagerException {
        return manager.download(urlSupplier.get(), destination);
    }
}
