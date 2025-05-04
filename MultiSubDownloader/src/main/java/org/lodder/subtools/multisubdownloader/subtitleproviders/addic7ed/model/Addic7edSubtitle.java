package org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.model;

import java.io.IOException;
import java.nio.file.Path;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.ManagerException;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.SubtitleSource;

public class Addic7edSubtitle extends Subtitle {

    @val String url;
    @val String version;

    public Addic7edSubtitle(String url,
        @Nullable String fileName=null,
        @Nullable Language language=null,
        @Nullable String releaseGroup=null,
        @Nullable String uploader=null,
        boolean hearingImpaired=false,
        @Nullable String quality=null,
        String version) {

        super(fileName, language, releaseGroup, uploader, SubtitleSource.ADDIC7ED, hearingImpaired, quality);
        this.url = url;
        this.version = version;
    }

    @Override
    public boolean download(Manager manager, Path destination) throws IOException, ManagerException {
        return manager.download(url, destination);
    }
}
