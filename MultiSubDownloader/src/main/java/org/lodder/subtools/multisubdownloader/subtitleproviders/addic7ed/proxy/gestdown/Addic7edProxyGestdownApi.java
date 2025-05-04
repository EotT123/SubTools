package org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.proxy.gestdown;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import extensions.java.lang.String.StringExt;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.gestdown.api.SubtitlesApi;
import org.gestdown.api.TvShowsApi;
import org.gestdown.invoker.ApiException;
import org.gestdown.model.EpisodeDto;
import org.gestdown.model.ShowDto;
import org.gestdown.model.SubtitleDto;
import org.gestdown.model.SubtitleSearchResponse;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleApi;
import org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.proxy.gestdown.model.Addic7edProxyGestdownSerieId;
import org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.proxy.gestdown.model.Addic7edProxyGestdownSubtitle;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.control.ReleaseParser;
import org.lodder.subtools.sublibrary.control.ReleaseParser.ReleaseParserExtraInfo;
import org.lodder.subtools.sublibrary.model.SubtitleSource;

/**
 * Provides access to Addic7ed subtitle data via the Gestdown proxy.
 * <p>
 * Note: Only supports TV series search; movie support is not available.
 */
// see https://www.gestdown.info/Api
public class Addic7edProxyGestdownApi implements SubtitleApi {

    private static final String DOMAIN = "https://api.gestdown.info";

    @val @override Manager manager;
    @val @override SubtitleSource source = SubtitleSource.ADDIC7ED;
    private final TvShowsApi tvShowsApi = new TvShowsApi();
    private final SubtitlesApi subtitlesApi = new SubtitlesApi();

    public Addic7edProxyGestdownApi(Manager manager) {
        this.manager = manager;
    }

    // ===== \\
    // MOVIE \\
    // ===== \\

    // No movie support

    // ===== \\
    // SERIE \\
    // ===== \\

    public List<Addic7edProxyGestdownSerieId> getProviderSerieIds(String name) throws ApiException {
        return getCache("providerId", b -> b.add("name", name))
            .getCollection(() -> {
                List<ShowDto> shows = tvShowsApi.showsSearchSearchGet(name).getShows();
                if (shows == null || shows.isEmpty()) {
                    return List.of();
                }
                return shows.stream().map(showDto -> new Addic7edProxyGestdownSerieId(showDto.name, showDto.id,
                    showDto.tvDbId, showDto.tmdbId)).toList();
            });
    }

    public List<Addic7edProxyGestdownSerieId> getProviderSerieIds(int tvdbId) throws ApiException {
        return getCache("providerId", b -> b.add("tvdbId", tvdbId))
            .getCollection(() -> {
                List<ShowDto> shows = tvShowsApi.showsExternalTvdbTvdbIdGet(tvdbId).getShows();
                if (shows == null || shows.isEmpty()) {
                    return List.of();
                }
                return shows.stream().map(showDto -> new Addic7edProxyGestdownSerieId(showDto.name, showDto.id,
                    showDto.tvDbId, showDto.tmdbId)).toList();
            });
    }

    public Set<Addic7edProxyGestdownSubtitle> getSubtitles(String providerId, int season, int episode,
        Language language) throws ApiException {
        return getCache("subtitles", b -> b.add("providerId", providerId)
            .add("season", season).add("episode", episode).add("language", language.getName()))
            .getCollection(() -> {
                SubtitleSearchResponse response = subtitlesApi.subtitlesGetShowUniqueIdSeasonEpisodeLanguageGet(
                    language.getName(), UUID.fromString(providerId), season, episode);
                List<SubtitleDto> subtitles = response.getMatchingSubtitles();
                if (subtitles == null || subtitles.isEmpty()) {
                    return Set.of();
                }
                return subtitles
                    .stream()
                    .filter(SubtitleDto::isCompleted)
                    .map(sub -> mapToSubtitle(sub, response.episode, language))
                    .toSet();
            });
    }

    // ====== \\
    // COMMON \\
    // ====== \\


    private Addic7edProxyGestdownSubtitle mapToSubtitle(SubtitleDto sub, EpisodeDto episodedto, Language language) {
        ReleaseParserExtraInfo extraInfoParser = ReleaseParser.parseExtraInfo(sub.getVersion());
        return new Addic7edProxyGestdownSubtitle(
            url:DOMAIN + sub.getDownloadUri(),
            fileName:StringExt.removeIllegalFilenameChars("${episodedto.show} - ${episodedto.title} - ${sub.version}"),
            language:language,
            quality:extraInfoParser.getQualityKeyword(),
            releaseGroup:extraInfoParser.getReleaseGroupBestEffort(),
            uploader:"",
            hearingImpaired:false);
    }
}
