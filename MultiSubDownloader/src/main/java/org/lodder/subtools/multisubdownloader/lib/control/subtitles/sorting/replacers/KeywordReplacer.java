package org.lodder.subtools.multisubdownloader.lib.control.subtitles.sorting.replacers;

import java.util.Map;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.ReleaseWithoutPath;

@NullMarked
public interface KeywordReplacer {
    void replace(Release release, Map<String, Integer> weights);
}
