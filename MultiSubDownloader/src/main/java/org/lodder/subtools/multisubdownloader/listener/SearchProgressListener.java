package org.lodder.subtools.multisubdownloader.listener;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.subtitleprovider.SubtitleProvider;
import org.lodder.subtools.sublibrary.model.Release;

@NullMarked
public interface SearchProgressListener extends StatusListener {

    void progress(SubtitleProvider provider, int jobsLeft, Release release);

    void done(SubtitleProvider provider);

    void progress(int progress);

    void completed();

    void reset();
}
