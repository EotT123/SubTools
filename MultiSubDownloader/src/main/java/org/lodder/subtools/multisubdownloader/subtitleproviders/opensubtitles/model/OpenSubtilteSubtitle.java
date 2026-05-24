package org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.model;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import name.falgout.jeffrey.throwing.ThrowingSupplier;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.exception.OpenSubtitleException;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.SubtitleSource;

@NullMarked
public class OpenSubtilteSubtitle extends Subtitle {

    private final ThrowingSupplier<String, OpenSubtitleException> urlSupplier;
    @val @override SubtitleSource source = SubtitleSource.OPENSUBTITLES;

    public OpenSubtilteSubtitle(ThrowingSupplier<@Nullable String, OpenSubtitleException> urlSupplier,
        String fileName,
        Language language,
        @Nullable String releaseGroup=null,
        @Nullable String uploader=null,
        @Nullable String quality=null,
        boolean hearingImpaired=false) {

        super(fileName, language, releaseGroup, uploader, hearingImpaired, quality);
        this.urlSupplier = urlSupplier;
    }

    @Override
    public List<Path> download(Path destinationFolder, Function<@Nullable AtomicInteger, String> fileNameFunction)
        throws IOException {
        try {
            String url = urlSupplier.get();
            Path subPath = destinationFolder.resolve(fileNameFunction.apply(null));
            Manager.getInstance().downloadAndExtractFile(url, subPath);
            return List.of(subPath);
        } catch (OpenSubtitleException e) {
            throw new IOException(e);
        }
    }
}
