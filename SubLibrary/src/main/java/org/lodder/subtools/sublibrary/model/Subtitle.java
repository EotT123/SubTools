package org.lodder.subtools.sublibrary.model;

import java.nio.file.Path;

import com.pivovarit.function.ThrowingSupplier;
import lombok.EqualsAndHashCode;
import manifold.ext.props.rt.api.val;
import manifold.ext.props.rt.api.var;
import org.apache.commons.lang3.builder.EqualsExclude;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.exception.SubtitlesProviderException;

@EqualsAndHashCode
public class Subtitle {

    @val DownloadSource downloadSource;
    @var String fileName;
    @var Language language;
    @var String releaseGroup;
    @var String uploader;
    @var SubtitleMatchType subtitleMatchType;
    @var SubtitleSource subtitleSource;
    @var boolean hearingImpaired;
    @var String quality;
    @var int score;

    public Subtitle(DownloadSource downloadSource,
        String fileName=null,
        Language language=null,
        String releaseGroup=null,
        String uploader=null,
        SubtitleMatchType subtitleMatchType=null,
        SubtitleSource subtitleSource=null,
        boolean hearingImpaired=false,
        String quality=null,
        int score=0) {
        this.downloadSource = downloadSource;
        this.fileName = fileName;
        this.language = language;
        this.releaseGroup = releaseGroup;
        this.uploader = uploader;
        this.subtitleMatchType = subtitleMatchType;
        this.subtitleSource = subtitleSource;
        this.hearingImpaired = hearingImpaired;
        this.quality = quality;
        this.score = score;
    }

    @EqualsAndHashCode
    public static class DownloadSource {
        @val SourceLocation sourceLocation;
        @EqualsExclude @val ThrowingSupplier<String, ? extends SubtitlesProviderException> urlSupplier;
        @val String url;
        @val Path file;

        private DownloadSource(
            SourceLocation sourceLocation,
            ThrowingSupplier<String, ? extends SubtitlesProviderException> urlSupplier=null,
            String url=null,
            Path file=null) {

            this.urlSupplier = urlSupplier;
            this.url = url;
            this.file = file;
            this.sourceLocation = sourceLocation;
        }

        public static DownloadSource of(
            ThrowingSupplier<String, ? extends SubtitlesProviderException> urlSupplier) {
            return new DownloadSource(SourceLocation.URL_SUPPLIER, urlSupplier:urlSupplier);
        }

        public static DownloadSource of(String url) {
            return new DownloadSource(SourceLocation.URL, url:url);
        }

        public static DownloadSource of(Path file) {
            return new DownloadSource(SourceLocation.FILE, file:file);
        }

        public String getValue() throws SubtitlesProviderException {
            return switch (sourceLocation) {
                case FILE -> file.toString();
                case URL -> url;
                case URL_SUPPLIER -> urlSupplier.get();
            };
        }

        @Override public String toString() {
            return "DownloadSource: " + sourceLocation + " " + switch (sourceLocation) {
                case FILE -> file;
                case URL -> url;
                case URL_SUPPLIER -> "";
            };
        }
    }

    public enum SourceLocation {
        URL, URL_SUPPLIER, FILE
    }

    @Override
    public String toString() {
        return "${getClass().getSimpleName()}: $fileName $quality";
    }
}
