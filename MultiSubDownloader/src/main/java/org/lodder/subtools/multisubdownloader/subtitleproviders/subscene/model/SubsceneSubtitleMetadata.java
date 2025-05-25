package org.lodder.subtools.multisubdownloader.subtitleproviders.subscene.model;

import java.io.Serializable;

import manifold.ext.props.rt.api.val;
import name.falgout.jeffrey.throwing.ThrowingSupplier;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subscene.exception.SubsceneException;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.model.SeasonEpisode;

public class SubsceneSubtitleMetadata implements Serializable {

    @val @Nullable Language language;
    @val @Nullable String name;
    @val boolean hearingImpaired;
    @val @Nullable String uploader;
    @val @Nullable String comment;
    @val @Nullable SeasonEpisode seasonEpisode;
    @val ThrowingSupplier<String, SubsceneException> urlSupplier;

    public SubsceneSubtitleMetadata(@Nullable Language language,
        @Nullable String name, boolean hearingImpaired,@Nullable String uploader, @Nullable String comment,
        ThrowingSupplier<String, SubsceneException> urlSupplier) {
        this.language = language;
        this.name = name;
        this.hearingImpaired = hearingImpaired;
        this.uploader = uploader;
        this.comment = comment;
        this.seasonEpisode = SeasonEpisode.fromText(name).orElse(null);
        this.urlSupplier = urlSupplier;
    }
}
