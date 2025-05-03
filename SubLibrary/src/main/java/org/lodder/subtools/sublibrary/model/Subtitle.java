package org.lodder.subtools.sublibrary.model;

import java.io.Serializable;
import java.nio.file.Path;

import manifold.ext.props.rt.api.val;
import manifold.ext.props.rt.api.var;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.exception.SubtitlesProviderException;

public abstract class Subtitle implements Serializable {

    @val @Nullable String fileName;
    @val @Nullable Language language;
    @val @Nullable String releaseGroup;
    @val @Nullable String uploader;
    @val @Nullable SubtitleMatchType subtitleMatchType;
    @val @Nullable SubtitleSource subtitleSource;
    @val boolean hearingImpaired;
    @val @Nullable String quality;
    @var int score;

    public Subtitle(@Nullable String fileName=null,
        @Nullable Language language=null,
        @Nullable String releaseGroup=null,
        @Nullable String uploader=null,
        @Nullable SubtitleMatchType subtitleMatchType=null,
        @Nullable SubtitleSource subtitleSource=null,
        boolean hearingImpaired=false,
        @Nullable String quality=null) {
        this.fileName = fileName;
        this.language = language;
        this.releaseGroup = releaseGroup;
        this.uploader = uploader;
        this.subtitleMatchType = subtitleMatchType;
        this.subtitleSource = subtitleSource;
        this.hearingImpaired = hearingImpaired;
        this.quality = quality;
    }

    public abstract Path download() throws SubtitlesProviderException;

//    @EqualsAndHashCode
//    public static class DownloadSource {
//        @val SourceLocation sourceLocation;
//        @EqualsExclude @val @Nullable ThrowingSupplier<String, ? extends SubtitlesProviderException> urlSupplier;
//        @val @Nullable String url;
//        @val @Nullable Path file;
//
//        private DownloadSource(
//            SourceLocation sourceLocation,
//            @Nullable ThrowingSupplier<String, ? extends SubtitlesProviderException> urlSupplier=null,
//            @Nullable String url=null,
//            @Nullable Path file=null) {
//
//            this.urlSupplier = urlSupplier;
//            this.url = url;
//            this.file = file;
//            this.sourceLocation = sourceLocation;
//        }
//
//        public static DownloadSource of(
//            ThrowingSupplier<String, ? extends SubtitlesProviderException> urlSupplier) {
//            return new DownloadSource(SourceLocation.URL_SUPPLIER, urlSupplier:urlSupplier);
//        }
//
//        public static DownloadSource of(String url) {
//            return new DownloadSource(SourceLocation.URL, url:url);
//        }
//
//        public static DownloadSource of(Path file) {
//            return new DownloadSource(SourceLocation.FILE, file:file);
//        }
//
//        @SuppressWarnings("ConstantConditions")
//        public String getValue() throws SubtitlesProviderException {
//            return switch (sourceLocation) {
//                case FILE -> file.toString();
//                case URL -> url;
//                case URL_SUPPLIER -> urlSupplier.get();
//            };
//        }
//
//        @Override public String toString() {
//            return "DownloadSource: " + sourceLocation + " " + switch (sourceLocation) {
//                case FILE -> file;
//                case URL -> url;
//                case URL_SUPPLIER -> "";
//            };
//        }
//    }

//    public enum SourceLocation {
//        URL, URL_SUPPLIER, FILE
//    }

    @Override
    public String toString() {
        return "${getClass().getSimpleName()}: $fileName $quality";
    }
}
