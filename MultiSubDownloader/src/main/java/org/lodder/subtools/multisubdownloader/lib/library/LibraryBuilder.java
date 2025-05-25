package org.lodder.subtools.multisubdownloader.lib.library;

import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import com.tvdb.model.SearchResult;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.settings.model.structure.StructureTag;
import org.lodder.subtools.sublibrary.data.tvdb.TvdbAdapter;
import org.lodder.subtools.sublibrary.model.Release;

public abstract sealed class LibraryBuilder permits FilenameLibraryBuilder, PathLibraryBuilder {

    private final @Nullable TvdbAdapter tvdbAdapter;

    public LibraryBuilder(@Nullable TvdbAdapter tvdbAdapter) {
        this.tvdbAdapter = tvdbAdapter;
    }

    public abstract Path build(Release release);

    protected String getShowName(String name) {
        return tvdbAdapter != null ? tvdbAdapter.searchSerie(name).map(SearchResult::getName).orElse(name) : name;
    }

    protected String replace(String structure, StructureTag tag, String value) {
        return structure.replace(tag.label, value);
    }

    protected String replaceFormattedEpisodeNumber(String structure, StructureTag tag, Set<Integer> episodeNumbers,
        boolean leadingZero) {
        if (structure.contains(tag.label)) {
            String afterLabel = StringUtils.substringAfter(structure, tag.label);
            String separator = StringUtils.isNotEmpty(afterLabel) ? afterLabel.substring(0, 1) : "";
            if ("%".equals(separator)) {
                separator = "";
            }
            String formattedEpisodeNumber = episodeNumbers.stream()
                .map(episode -> formatNumber(episode, leadingZero))
                .collect(Collectors.joining(separator));
            return structure.replace(tag.label, formattedEpisodeNumber);
        }
        return structure;

    }

    protected String formatNumber(int number, boolean leadingZero) {
        return number < 10 && leadingZero ? "0" + number : Integer.toString(number);
    }
}
