package org.lodder.subtools.sublibrary.model;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;

import manifold.ext.props.rt.api.val;
import manifold.ext.props.rt.api.var;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.ManagerException;
import org.lodder.subtools.sublibrary.control.ReleaseParser;
import org.lodder.subtools.sublibrary.exception.SubtitlesProviderException;

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

    public abstract boolean download(Manager manager, Path destination) throws IOException, ManagerException,
        SubtitlesProviderException;

    @Override
    public String toString() {
        return "${getClass().getSimpleName()}: $fileName $quality";
    }
}
