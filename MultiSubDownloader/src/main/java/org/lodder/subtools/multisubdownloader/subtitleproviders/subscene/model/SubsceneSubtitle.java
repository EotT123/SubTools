package org.lodder.subtools.multisubdownloader.subtitleproviders.subscene.model;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import name.falgout.jeffrey.throwing.ThrowingSupplier;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subscene.exception.SubsceneException;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.SubtitleSource;

@NullMarked
public class SubsceneSubtitle extends Subtitle {

    private final ThrowingSupplier<String, ? extends SubsceneException> urlSupplier;

    public SubsceneSubtitle(ThrowingSupplier<String, ? extends SubsceneException> urlSupplier,
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
    public List<Path> download(Manager manager, Path destinationFolder,
        Function<AtomicInteger, String> fileNameFunction) throws IOException {
        try {
            String url = urlSupplier.get();
            Path subPath = destinationFolder.resolve(fileNameFunction.apply(null));
            manager.downloadAndExtractFile(url, subPath);
            return List.of(subPath);
        } catch (SubsceneException e) {
            throw new IOException(e);
        }
    }
}
