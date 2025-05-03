package org.lodder.subtools.sublibrary.model;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import manifold.ext.props.rt.api.val;
import manifold.ext.props.rt.api.var;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;

public abstract sealed class Release permits MovieRelease, TvRelease {

    private final Set<Subtitle> matchingSubsSet = new HashSet<>();
    @var String name;
    @val VideoType videoType;
    @val @Nullable Path filePath;
    @val @Nullable String quality;
    @val @Nullable String releaseGroup;
    @val @Nullable String extension;
    @val ReleaseIds releaseIds = new ReleaseIds();

    protected Release(String name, VideoType videoType, @Nullable Path filePath, @Nullable String releaseGroup,
        @Nullable String quality, @Nullable String extension) {
        this.name = name;
        this.videoType = videoType;
        this.filePath = filePath;
        this.releaseGroup = releaseGroup;
        this.quality = quality;
        this.extension = extension;
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

    public String getFileName() {
        return filePath != null ? filePath.getFileName().toString() : null;
    }

    public Path getPath() {
        return filePath != null ? filePath.getParent() : null;
    }

    public boolean hasExtension(String extension) {
        return StringUtils.isNotBlank(extension) && extension.equals(this.extension);
    }

//    public String getImdbIdAsString() {
//        return "tt%07d".formatted(imdbId);
//    }

    @Override
    public String toString() {
        return "${getClass().getSimpleName()}: $fileName $quality";
    }

    public String getReleaseDescription() {
        return fileName;
    }

    public boolean hasSameId(Release other, ReleaseIdType releaseIdType) {
        return releaseIds.isEqual(other.releaseIds, releaseIdType);
    }
}
