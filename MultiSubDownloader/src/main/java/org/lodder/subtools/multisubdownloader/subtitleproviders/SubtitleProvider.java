package org.lodder.subtools.multisubdownloader.subtitleproviders;

import java.util.Optional;
import java.util.Set;

import manifold.ext.props.rt.api.val;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.cache.CacheType;
import org.lodder.subtools.sublibrary.model.MovieRelease;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.SubtitleSource;
import org.lodder.subtools.sublibrary.model.TvRelease;
import org.lodder.subtools.sublibrary.settings.model.SerieMapping;
import org.slf4j.LoggerFactory;

public interface SubtitleProvider {

    @val String providerName;
    @val Manager manager;

    Set<Subtitle> searchSubtitles(TvRelease tvRelease, Language language);

    Set<Subtitle> searchSubtitles(MovieRelease movieRelease, Language language);

    SubtitleSource getSubtitleSource();

    /**
     * @return The name of the SubtitleProvider
     */
    default String getName() {
        return getSubtitleSource().name;
    }

    /**
     * Starts a search for subtitles
     *
     * @param release The release being searched for
     * @param language The language of the desired subtitles
     * @return The found subtitles
     */
    default Set<Subtitle> search(Release release, Language language) {
        try {
            if (release instanceof MovieRelease movieRelease) {
                return this.searchSubtitles(movieRelease, language);
            } else if (release instanceof TvRelease tvRelease) {
                return this.searchSubtitles(tvRelease, language);
            }
        } catch (Exception e) {
            LoggerFactory.getLogger(SubtitleProvider.class)
                    .error("Error in %s API: %s".formatted(getName(), e.getMessage()), e);
        }
        return Set.of();
    }

    default void clearCache() {
        manager.clearExpiredCacheBuilder()
                .cacheType(CacheType.DISK)
                .keyFilter((String k) -> k.startsWith(providerName + "-"))
                .clear();
    }


    <X extends Exception> Optional<SerieMapping> getProviderSerieId(TvRelease tvRelease) throws X;

}
