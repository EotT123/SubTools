package org.lodder.subtools.sublibrary.model;

import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import manifold.ext.props.rt.api.val;
import org.apache.commons.lang3.StringUtils;

public abstract sealed class Release permits MovieRelease, TvRelease {

    private final Set<Subtitle> matchingSubsSet = new HashSet<>();
    @val VideoType videoType;
    @val Path filePath;
    @val String quality;
    @val String releaseGroup;
    @val String extension;

    public void addMatchingSub(Subtitle sub) {
        matchingSubsSet.add(sub);
    }

    public List<Subtitle> getMatchingSubs() {
        return List.copyOf(matchingSubsSet);
    }

    public int getMatchingSubCount() {
        return matchingSubsSet.size();
    }

    protected Release(VideoType videoType, Path filePath, String releaseGroup, String quality, String extension) {
        this.videoType = videoType;
        this.filePath = filePath;
        this.releaseGroup = releaseGroup;
        this.quality = quality;
        this.extension = extension;
    }

    public String getFileName() {
        return filePath != null ? filePath.getFileName().toString() : null;
    }

    public Path getPath() {
        return filePath != null ? filePath.getParent() : null;
    }

    public String getExtension() {
        return extension;
    }

    public boolean hasExtension(String extension) {
        return StringUtils.isNotBlank(extension);
    }

    @Override
    public String toString() {
        return "${getClass().getSimpleName()}: $fileName $quality";
    }

    public String getReleaseDescription() {
        return fileName;
    }
}
