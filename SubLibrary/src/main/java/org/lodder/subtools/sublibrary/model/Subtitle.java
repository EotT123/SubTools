package org.lodder.subtools.sublibrary.model;

import java.nio.file.Path;

import com.pivovarit.function.ThrowingSupplier;
import lombok.AccessLevel;
import lombok.EqualsAndHashCode;
import lombok.RequiredArgsConstructor;
import manifold.ext.props.rt.api.val;
import manifold.ext.props.rt.api.var;
import org.apache.commons.lang3.builder.EqualsExclude;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.exception.SubtitlesProviderException;

@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
@EqualsAndHashCode
public class Subtitle {
    @EqualsExclude
    @val ThrowingSupplier<String, ? extends SubtitlesProviderException> urlSupplier;
    @val String url;
    @val Path file;
    @val SourceLocation sourceLocation;

    @var String fileName;
    @var Language language;
    @var String releaseGroup;
    @var String uploader;
    @var SubtitleMatchType subtitleMatchType;
    @var SubtitleSource subtitleSource;
    @var boolean hearingImpaired;
    @var String quality;
    @var int score;

    public enum SourceLocation {
        URL, URL_SUPPLIER, FILE
    }

    private Subtitle(ThrowingSupplier<String, ? extends SubtitlesProviderException> urlSupplier) {
        this.urlSupplier = urlSupplier;
        this.url = null;
        this.file = null;
        this.sourceLocation = SourceLocation.URL_SUPPLIER;
    }

    private Subtitle(String url) {
        this.urlSupplier = null;
        this.url = url;
        this.file = null;
        this.sourceLocation = SourceLocation.URL;
    }

    private Subtitle(Path file) {
        this.urlSupplier = null;
        this.url = null;
        this.file = file;
        this.sourceLocation = SourceLocation.FILE;
    }

    public static Subtitle downloadSource(ThrowingSupplier<String, ? extends SubtitlesProviderException> urlSupplier) {
        return new Subtitle(urlSupplier);
    }

    public static Subtitle downloadSource(String url) {
        return new Subtitle(url);
    }

    public static Subtitle downloadSource(Path file) {
        return new Subtitle(file);
    }

    @Override
    public String toString() {
        return "${getClass().getSimpleName()}: $fileName $quality";
    }

    public Subtitle fileName(String fileName) {
        this.fileName = fileName;
        return this;
    }

    public Subtitle language(Language language) {
        this.language = language;
        return this;
    }

    public Subtitle releaseGroup(String releaseGroup) {
        this.releaseGroup = releaseGroup;
        return this;
    }

    public Subtitle uploader(String uploader) {
        this.uploader = uploader;
        return this;
    }

    public Subtitle subtitleMatchType(SubtitleMatchType subtitleMatchType) {
        this.subtitleMatchType = subtitleMatchType;
        return this;
    }

    public Subtitle subtitleSource(SubtitleSource subtitleSource) {
        this.subtitleSource = subtitleSource;
        return this;
    }

    public Subtitle hearingImpaired(boolean hearingImpaired) {
        this.hearingImpaired = hearingImpaired;
        return this;
    }

    public Subtitle quality(String quality) {
        this.quality = quality;
        return this;
    }

    public Subtitle score(int score) {
        this.score = score;
        return this;
    }

}
