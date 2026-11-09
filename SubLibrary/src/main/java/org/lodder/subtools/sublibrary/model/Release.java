package org.lodder.subtools.sublibrary.model;

import java.util.List;

import manifold.ext.props.rt.api.val;
import manifold.ext.props.rt.api.var;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public sealed interface Release permits ReleaseWithoutPath, ReleaseWithPath, MovieRelease, TvRelease {

    @var String name;
    @val @Nullable String quality;
    @val @Nullable String releaseGroup;
    @val ProviderIds providerIds = new ProviderIds();
    @val String releaseDescription;
    // the complete (file) name
    @val String completeName;

    @val String folderNameOrName = switch (this) {
        case ReleaseWithPath r -> r.path.parent.toString();
        case ReleaseWithoutPath r -> r.completeName;
    };
    @val String fileNameOrName = switch (this) {
        case ReleaseWithPath r -> r.path.fileNameAsString;
        case ReleaseWithoutPath r -> r.completeName;
    };
    @val int matchingSubCount = matchingSubs.size();
    @val List<Subtitle> matchingSubs;

    void addMatchingSub(Subtitle sub);

    boolean isOfType(VideoType videoType);
}
