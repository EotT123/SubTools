package org.lodder.subtools.multisubdownloader.worker;

import java.util.List;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.Subtitle;

@NullMarked
public interface SearchHandler<R extends Release> {
    void onFound(R release, List<Subtitle> subtitles);
}
