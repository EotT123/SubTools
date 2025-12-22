package org.lodder.subtools.multisubdownloader.workers;

import java.util.List;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.Subtitle;

@NullMarked
public interface SearchHandler {
    void onFound(Release release, List<Subtitle> subtitles);
}
