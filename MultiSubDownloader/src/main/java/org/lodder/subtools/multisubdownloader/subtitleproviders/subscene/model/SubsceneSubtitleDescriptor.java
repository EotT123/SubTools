package org.lodder.subtools.multisubdownloader.subtitleproviders.subscene.model;

import com.pivovarit.function.ThrowingSupplier;
import lombok.EqualsAndHashCode;
import manifold.ext.props.rt.api.val;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subscene.exception.SubsceneException;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.model.SeasonEpisode;

@EqualsAndHashCode
public class SubsceneSubtitleDescriptor {

    @val Language language;
    @val String name;
    @val boolean hearingImpaired;
    @val String uploader;
    @val String comment;
    @val SeasonEpisode seasonEpisode;
    @EqualsAndHashCode.Exclude @val ThrowingSupplier<String, SubsceneException> urlSupplier;

    public SubsceneSubtitleDescriptor(Language language,
        String name, boolean hearingImpaired, String uploader, String comment,
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
