package org.lodder.subtools.multisubdownloader.subtitleproviders;

import java.util.ArrayList;
import java.util.List;

import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.model.MovieRelease;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.Subtitle.SubtitleSource;
import org.lodder.subtools.sublibrary.model.TvRelease;
import org.slf4j.LoggerFactory;

public interface SubtitleProvider {

    List<Subtitle> searchSubtitles(TvRelease tvRelease, Language language);

    List<Subtitle> searchSubtitles(MovieRelease movieRelease, Language language);

    SubtitleSource getSubtitleSource();

    /**
     * @return The name of the SubtitleProvider
     */
    default String getName() {
        return getSubtitleSource().getName();
    }

    /**
     * Starts a search for subtitles
     *
     * @param release The release being searched for
     * @param language The language of the desired subtitles
     * @return The found subtitles
     */
    default List<Subtitle> search(Release release, Language language) {
        try {
            if (release instanceof MovieRelease movieRelease) {
                return this.searchSubtitles(movieRelease, language);
            } else if (release instanceof TvRelease tvRelease) {
                return this.searchSubtitles(tvRelease, language);
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(SubtitleProvider.class).error("Error in %s API: %s".formatted(getName(), e.getMessage()), e);
        }
        return new ArrayList<>();
    }
}
