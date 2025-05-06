package org.lodder.subtools.multisubdownloader.subtitleproviders.tvsubtitles.model;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.SubtitleSource;

public class TvSubtiltesSubtitle extends Subtitle {

    private final String url;

    public TvSubtiltesSubtitle(String url,
        @Nullable String fileName=null,
        @Nullable Language language=null,
        @Nullable String releaseGroup=null,
        @Nullable String uploader=null,
        boolean hearingImpaired=false,
        @Nullable String quality=null) {

        super(fileName, language, releaseGroup, uploader, SubtitleSource.TVSUBTITLES, hearingImpaired, quality);
        this.url = url;
    }

    @Override
    public List<Path> download(Manager manager, Path destinationFolder, Supplier<String> fileNameFunction)
        throws IOException {
        Path subPath = destinationFolder.resolve(fileNameFunction.get());
        manager.download(url, subPath);
        return List.of(subPath);
    }
}
