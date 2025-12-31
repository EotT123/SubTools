package org.lodder.subtools.sublibrary.model;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import manifold.ext.props.rt.api.var;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public abstract sealed class ReleaseWithoutPath implements Release permits MovieReleaseWithoutPath,
    TvReleaseWithoutPath {

    private final Set<Subtitle> matchingSubsSet = new HashSet<>();
    @var @override String name;
    @val @override VideoType videoType;
    @val @override @Nullable String quality;
    @val @override @Nullable String releaseGroup;
    @val @override ProviderIds providerIds = new ProviderIds();
    @val @override String completeName;

    protected ReleaseWithoutPath(String name, VideoType videoType, @Nullable String releaseGroup,
        @Nullable String quality, String completeName) {
        this.name = name;
        this.videoType = videoType;
        this.releaseGroup = releaseGroup;
        this.quality = quality;
        this.completeName = completeName;
    }

    @Override
    public void addMatchingSub(Subtitle sub) {
        matchingSubsSet.add(sub);
    }

    @Override
    public List<Subtitle> getMatchingSubs() {
        return List.copyOf(matchingSubsSet);
    }

    @Override
    public String toString() {
        return "${getClass().getSimpleName()}: $name $quality";
    }

    public boolean hasSameId(Release other, ProviderIdType providerIdType) {
        return providerIds.isEqual(other.providerIds, providerIdType);
    }
}
