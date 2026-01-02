package org.lodder.subtools.multisubdownloader.lib.control.subtitles.sorting;

import java.io.Serial;
import java.io.Serializable;
import java.util.Comparator;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.model.Subtitle;

@NullMarked
public class SubtitleComparator implements Comparator<Subtitle>, Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public int compare(Subtitle a, Subtitle b) {
        /* inverse sorting */
        return Integer.compare(b.score, a.score);
    }
}
