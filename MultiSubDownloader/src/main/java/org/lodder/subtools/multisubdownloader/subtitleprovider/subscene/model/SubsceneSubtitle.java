package org.lodder.subtools.multisubdownloader.subtitleprovider.subscene.model;

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
import org.lodder.subtools.multisubdownloader.subtitleprovider.subscene.exception.SubsceneException;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.SubtitleSource;

@NullMarked
public class SubsceneSubtitle extends Subtitle {

    private final ThrowingSupplier<String, ? extends SubsceneException> urlSupplier;
    @val @override SubtitleSource source = SubtitleSource.SUBSCENE;

    public SubsceneSubtitle(ThrowingSupplier<String, ? extends SubsceneException> urlSupplier,
        String fileName,
        Language language,
        String releaseGroup,
        String uploader,
        boolean hearingImpaired,
        String quality) {

        super(fileName, language, releaseGroup, uploader, hearingImpaired, quality);
        this.urlSupplier = urlSupplier;
    }

    @Override
    public List<Path> download(Path destinationFolder, Function<@Nullable AtomicInteger, String> fileNameFunction)
        throws IOException {
        try {
            String url = urlSupplier.get();
            Path subPath = destinationFolder.resolve(fileNameFunction.apply(null));
            Manager.downloadAndExtractFile(url, subPath);
            return List.of(subPath);
        } catch (SubsceneException e) {
            throw new IOException(e);
        }
    }
}
