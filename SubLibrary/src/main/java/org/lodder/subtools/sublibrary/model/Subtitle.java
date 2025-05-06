package org.lodder.subtools.sublibrary.model;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Supplier;

import manifold.ext.props.rt.api.val;
import manifold.ext.props.rt.api.var;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.control.ReleaseParser;

public abstract class Subtitle implements Serializable {

    @val @Nullable String fileName;
    @val @Nullable Language language;
    @val @Nullable String releaseGroup;
    @val @Nullable String uploader;
    @val SubtitleSource source;
    @val boolean hearingImpaired;
    @val String quality;

    @var @Nullable SubtitleMatchType subtitleMatchType;
    @var int score;

    public Subtitle(@Nullable String fileName=null,
        @Nullable Language language=null,
        @Nullable String releaseGroup=null,
        @Nullable String uploader=null,
        SubtitleSource source,
        boolean hearingImpaired=false,
        @Nullable String quality=null) {
        this.fileName = fileName;
        this.language = language;
        this.releaseGroup = releaseGroup;
        this.uploader = uploader;
        this.source = source;
        this.hearingImpaired = hearingImpaired;
        this.quality = quality == null ? ReleaseParser.getQualityKeyword(fileName) : quality;
    }

    public abstract List<Path> download(Manager manager, Path destinationFolder,
        Supplier<String> fileNameFunction) throws IOException;

    @Override
    public String toString() {
        return "${getClass().getSimpleName()}: $fileName $quality";
    }
}
