package org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.model;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import manifold.ext.props.rt.api.val;
import name.falgout.jeffrey.throwing.ThrowingConsumer;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.SubtitleSource;

@NullMarked
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
    public List<Path> download(Manager manager, Path destinationFolder,
        Function<AtomicInteger, String> fileNameFunction) throws IOException {
        Path subPath = destinationFolder.resolve(fileNameFunction.apply(null));
        ThrowingConsumer<String, IOException> validateFunction = content -> {
            if (content.contains("Daily Download count exceeded")) {
                throw new IOException("Addic7ed Daily Download count exceeded!");
            }
        };
        manager.downloadAndExtractFile(url, subPath, validateFunction);
        return List.of(subPath);
    }
}
