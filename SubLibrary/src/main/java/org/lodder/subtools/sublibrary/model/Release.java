package org.lodder.subtools.sublibrary.model;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import manifold.ext.props.rt.api.val;
import org.apache.commons.lang3.StringUtils;

public abstract class Release extends Video {

    @val Set<Subtitle> matchingSubs = new HashSet<>();
    @val Path path;
    @val String quality;
    @val String description;
    @val String releaseGroup;

    public void addMatchingSub(Subtitle sub) {
        matchingSubs.add(sub);
    }

    public List<Subtitle> getMatchingSubs() {
        return new ArrayList<>(matchingSubs);
    }

    public int getMatchingSubCount() {
        return matchingSubs.size();
    }

    protected Release(VideoType videoFileType, Path path, String description, String releaseGroup, String quality) {
        super(videoFileType);
        this.path = path;
        this.description = description;
        this.releaseGroup = releaseGroup;
        this.quality = quality;
    }

    public String getFileName() {
        return path != null ? path.getFileName().toString() : null;
    }

    public Path getPath() {
        return path != null ? path.getParent() : null;
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
