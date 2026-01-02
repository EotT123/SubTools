package org.lodder.subtools.multisubdownloader.subtitleproviders.podnapisi.model;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.SubtitleSource;

@NullMarked
public class PodnapisiSubtitle extends Subtitle {

    private final String url;
    @val @override SubtitleSource source = SubtitleSource.PODNAPISI;

    public PodnapisiSubtitle(String url,
        String fileName,
        Language language,
        @Nullable String releaseGroup=null,
        String uploader,
        boolean hearingImpaired,
        @Nullable String quality=null) {

        super(fileName, language, releaseGroup, uploader, hearingImpaired, quality);
        this.url = url;
    }

    @Override
    public List<Path> download(Manager manager, Path destinationFolder,
        Function<@Nullable AtomicInteger, String> fileNameFunction) throws IOException {
        Path subPath = destinationFolder.resolve(fileNameFunction.apply(null));
        manager.downloadAndExtractFile(url, subPath);
        return List.of(subPath);
    }
}
