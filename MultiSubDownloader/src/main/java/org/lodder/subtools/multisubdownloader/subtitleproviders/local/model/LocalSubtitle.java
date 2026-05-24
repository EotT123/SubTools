package org.lodder.subtools.multisubdownloader.subtitleproviders.local.model;

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
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.SubtitleSource;

@NullMarked
public class LocalSubtitle extends Subtitle {

    private final Path path;
    @val @override SubtitleSource source = SubtitleSource.LOCAL;

    public LocalSubtitle(Path path,
        Language language,
        @Nullable String releaseGroup=null,
        @Nullable String quality=null) {

        super(path.fileNameAsString, language, releaseGroup, null, false, quality);
        this.path = path;
    }

    @Override
    public List<Path> download(Path destinationFolder, Function<@Nullable AtomicInteger, String> fileNameFunction)
        throws IOException {
        Path subPath = destinationFolder.resolve(path.fileName);
        path.copyToDir(subPath);
        return List.of(subPath);
    }
}
