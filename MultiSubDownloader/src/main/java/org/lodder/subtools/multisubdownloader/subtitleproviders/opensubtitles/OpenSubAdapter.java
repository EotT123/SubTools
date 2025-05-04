package org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;

import lombok.Getter;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleAdapter;
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.exception.OpenSubtitleException;
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.model.OpenSubtilteSubtitle;
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.model.OpensubtitleId;
import org.lodder.subtools.sublibrary.Credentials;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.exception.SubtitlesProviderInitException;
import org.lodder.subtools.sublibrary.model.ProviderIds;
import org.lodder.subtools.sublibrary.model.SubtitleSource;
import org.lodder.subtools.sublibrary.settings.model.SerieMapping;

@Getter
public final class OpenSubAdapter
    extends SubtitleAdapter<OpenSubtilteSubtitle, OpenSubtilteSubtitle, OpensubtitleId,
    OpenSubtitleException> {

    private static OpenSubtitlesApi api;
    @val @override SubtitleSource source = SubtitleSource.OPENSUBTITLES;
    @val @override boolean useSeasonForSerieId = false;

    public OpenSubAdapter(Manager manager, Credentials credentials, UserInteractionHandler userInteractionHandler) {
        super(manager, userInteractionHandler);
        if (api == null) {
            try {
                api = new OpenSubtitlesApi(manager, credentials);
            } catch (OpenSubtitleException e) {
                throw new SubtitlesProviderInitException(name, e);
            }
        }
    }

    @Override
    public List<OpenSubtilteSubtitle> searchMovieSubtitlesWithHash(String hash, Language language)
        throws OpenSubtitleException {
        return api.searchSubtitles(movieHash:hash, language:language);
    }

    @Override
    public List<OpenSubtilteSubtitle> searchMovieSubtitlesWithId(ProviderIds providerIds, Language language)
        throws OpenSubtitleException {
        return providerIds.getImdbId().mapThrowing(imdbId -> api.searchSubtitles(imdbId:imdbId, language:language))
            .orElse(List.of());
    }

    @Override
    public Collection<OpenSubtilteSubtitle> searchMovieSubtitlesWithName(String name,
        @Nullable Integer year, Language language) throws OpenSubtitleException {
        return api.searchSubtitles(query:name, language:language);
    }

    // ===== \\
    // SERIE \\
    // ===== \\

    @Override
    public List<OpensubtitleId> getSortedSerieProviderIds(ProviderIds providerIds, String serieName,
        @Nullable Integer season) throws OpenSubtitleException {
        return api.getProviderSerieIds(serieName)
            .stream()
            .sorted(
                Comparator.comparing((OpensubtitleId n) -> !serieName.replaceAll("[^A-Za-z]", "")
                        .equalsIgnoreCase(n.name.replaceAll("[^A-Za-z]", "")))
                    .thenComparing(OpensubtitleId::getYear, Comparator.nullsLast(Comparator.reverseOrder())))
            .toList();
    }

    @Override
    public String providerSerieIdToDisplayString(OpensubtitleId providerSerieId) {
        return "${providerSerieId.name} (${providerSerieId.year})";
    }

    @Override
    public Collection<OpenSubtilteSubtitle> searchSubtitles(SerieMapping serieMapping, int season,
        int episode, Language language) throws OpenSubtitleException {
        return api.searchSubtitles(
            query:serieMapping.name,
            season:season,
            episode:episode,
            language:language);
    }


    // ====== \\
    // COMMON \\
    // ====== \\

    @Override
    public OpenSubtilteSubtitle convertToSubtitle(OpenSubtilteSubtitle sub) {
        return sub;
    }
}
