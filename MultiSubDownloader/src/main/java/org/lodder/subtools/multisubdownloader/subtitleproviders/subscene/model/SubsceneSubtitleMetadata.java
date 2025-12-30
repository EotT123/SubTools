package org.lodder.subtools.multisubdownloader.subtitleproviders.subscene.model;

import java.io.Serializable;

import manifold.ext.props.rt.api.val;
import name.falgout.jeffrey.throwing.ThrowingSupplier;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subscene.exception.SubsceneException;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.model.SeasonEpisode;

@NullMarked
public class SubsceneSubtitleMetadata implements Serializable {

    @val Language language;
    @val String name;
    @val boolean hearingImpaired;
    @val String uploader;
    @val String comment;
    @val @Nullable SeasonEpisode seasonEpisode;
    @val ThrowingSupplier<String, ? extends SubsceneException> urlSupplier;

    public SubsceneSubtitleMetadata(Language language, String name, boolean hearingImpaired, String uploader,
        String comment, ThrowingSupplier<String, ? extends SubsceneException> urlSupplier) {
        this.language = language;
        this.name = name;
        this.hearingImpaired = hearingImpaired;
        this.uploader = uploader;
        this.comment = comment;
        this.seasonEpisode = SeasonEpisode.fromText(name).orElse(null);
        this.urlSupplier = urlSupplier;
    }
}
