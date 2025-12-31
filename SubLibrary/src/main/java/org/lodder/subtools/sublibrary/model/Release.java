package org.lodder.subtools.sublibrary.model;

import java.util.List;

import manifold.ext.props.rt.api.val;
import manifold.ext.props.rt.api.var;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public sealed interface Release permits ReleaseWithoutPath, ReleaseWithPath, MovieRelease, TvRelease {

    @var String name;
    @val VideoType videoType;
    @val @Nullable String quality;
    @val @Nullable String releaseGroup;
    @val ProviderIds providerIds = new ProviderIds();
    @val String releaseDescription;
    @val String completeName;

    @val String fileNameOrName = switch (this) {
        case ReleaseWithPath r -> r.fileName;
        case ReleaseWithoutPath r -> r.completeName;
    };
    @val int matchingSubCount = matchingSubs.size();
    @val List<Subtitle> matchingSubs;

    void addMatchingSub(Subtitle sub);
}
