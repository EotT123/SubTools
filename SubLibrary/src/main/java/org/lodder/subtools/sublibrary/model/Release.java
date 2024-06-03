package org.lodder.subtools.sublibrary.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import manifold.ext.props.rt.api.val;
import org.apache.commons.lang3.StringUtils;

public abstract class Release extends Video {

    private final Set<Subtitle> matchingSubsSet = new HashSet<>();
    @val Path filePath;
    @val String quality;
    @val String description;
    @val String releaseGroup;

    public void addMatchingSub(Subtitle sub) {
        matchingSubsSet.add(sub);
    }

    public List<Subtitle> getMatchingSubs() {
        return new ArrayList<>(matchingSubsSet);
    }

    public int getMatchingSubCount() {
        return matchingSubsSet.size();
    }

    protected Release(VideoType videoFileType, Path filePath, String description, String releaseGroup, String quality) {
        super(videoFileType);
        this.filePath = filePath;
        this.description = description;
        this.releaseGroup = releaseGroup;
        this.quality = quality;
    }

    public String getFileName() {
        return filePath != null ? filePath.getFileName().toString() : null;
    }

    public Path getPath() {
        return filePath != null ? filePath.getParent() : null;
    }

    public String getExtension() {
        return StringUtils.substringAfterLast(fileName, ".");
    }

    public boolean hasExtension(String extension) {
        return StringUtils.endsWith(fileName, "." + extension);
    }

    @Override
    public String toString() {
        return "${getClass().getSimpleName()}: $fileName $quality";
    }

    public String getReleaseDescription() {
        return fileName;
    }
}
