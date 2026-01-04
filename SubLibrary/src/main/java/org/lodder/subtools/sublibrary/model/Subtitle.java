package org.lodder.subtools.sublibrary.model;

import static manifold.ext.props.rt.api.PropOption.*;
import static util.Utils.*;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import manifold.ext.props.rt.api.val;
import manifold.ext.props.rt.api.var;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.control.ReleaseParser;

@NullMarked
public abstract class Subtitle implements Serializable {

    @val @Nullable String fileName;
    @val Language language;
    @val @Nullable String releaseGroup;
    @val @Nullable String uploader;
    @val boolean hearingImpaired;
    @val String quality;
    @val(Abstract) SubtitleSource source;

    @var @Nullable SubtitleMatchType subtitleMatchType;
    @var int score;

    public Subtitle(@Nullable String fileName=null,
        Language language,
        @Nullable String releaseGroup=null,
        @Nullable String uploader=null,
        boolean hearingImpaired=false,
        @Nullable String quality=null) {

        this.fileName = fileName;
        this.language = language;
        this.releaseGroup = releaseGroup;
        this.uploader = uploader;
        this.hearingImpaired = hearingImpaired;
        this.quality = ifNullThenGet(quality, () -> ifNotNullOrElse(fileName, ReleaseParser::getQualityKeyword, ""));
    }

    public abstract List<Path> download(Manager manager, Path destinationFolder,
        Function<@Nullable AtomicInteger, String> fileNameFunction) throws IOException;

    @Override
    public String toString() {
        return "${getClass().getSimpleName()}: $fileName $quality";
    }
}
