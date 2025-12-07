package org.lodder.subtools.sublibrary.model;

import static util.Utils.*;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import manifold.ext.props.rt.api.val;
import manifold.ext.props.rt.api.var;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public abstract sealed class Release permits MovieRelease, TvRelease {

    private final Set<Subtitle> matchingSubsSet = new HashSet<>();
    @var String name;
    @val VideoType videoType;
    @val @Nullable Path filePath;
    @val @Nullable String quality;
    @val @Nullable String releaseGroup;
    @val ProviderIds providerIds = new ProviderIds();

    protected Release(String name, VideoType videoType, @Nullable Path filePath, @Nullable String releaseGroup,
        @Nullable String quality) {
        this.name = name;
        this.videoType = videoType;
        this.filePath = filePath;
        this.releaseGroup = releaseGroup;
        this.quality = quality;
    }

    public void addMatchingSub(Subtitle sub) {
        matchingSubsSet.add(sub);
    }

    public List<Subtitle> getMatchingSubs() {
        return List.copyOf(matchingSubsSet);
    }

    public int getMatchingSubCount() {
        return matchingSubsSet.size();
    }

    public @Nullable String getFileName() {
        return filePath != null ? filePath.getFileName().toString() : null;
    }

    public @Nullable Path getPath() {
        return ifNotNull(filePath, Path::getParent);
    }

    public boolean hasExtension(String extension) {
        return filePath != null && filePath.fileNameAsString.endsWith(extension);
    }

    @Override
    public String toString() {
        return "${getClass().getSimpleName()}: $fileName $quality";
    }

    public @Nullable String getReleaseDescription() {
        return fileName;
    }

    public boolean hasSameId(Release other, ProviderIdType providerIdType) {
        return providerIds.isEqual(other.providerIds, providerIdType);
    }
}
