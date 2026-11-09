package org.lodder.subtools.multisubdownloader.listener;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface IndexingProgressListener extends StatusListener {

    void progress(int progress);

    void progress(String directory);

    void completed();

    void reset();

}
