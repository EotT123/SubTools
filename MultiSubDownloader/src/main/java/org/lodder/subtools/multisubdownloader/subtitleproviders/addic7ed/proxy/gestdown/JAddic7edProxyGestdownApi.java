package org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.proxy.gestdown;

import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import extensions.java.lang.String.StringExt;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.gestdown.api.SubtitlesApi;
import org.gestdown.api.TvShowsApi;
import org.gestdown.invoker.ApiException;
import org.gestdown.model.EpisodeDto;
import org.gestdown.model.SubtitleDto;
import org.gestdown.model.SubtitleSearchResponse;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleApi;
import org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.proxy.gestdown.model.Addic7edProxyGestdownSubtitle;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.control.ReleaseParser;
import org.lodder.subtools.sublibrary.data.ProviderId;
import org.lodder.subtools.sublibrary.model.SubtitleMatchType;
import org.lodder.subtools.sublibrary.model.SubtitleSource;
import org.lodder.subtools.sublibrary.settings.model.SerieMapping;

// see https://www.gestdown.info/Api
public class JAddic7edProxyGestdownApi implements SubtitleApi {

    private static final String DOMAIN = "https://api.gestdown.info";

    @val @override Manager manager;
    @val @override SubtitleSource source = SubtitleSource.ADDIC7ED;
    private final TvShowsApi tvShowsApi = new TvShowsApi();
    private final SubtitlesApi subtitlesApi = new SubtitlesApi();

    public JAddic7edProxyGestdownApi(Manager manager) {
        this.manager = manager;
    }

    public List<ProviderId> getProviderId(String name) throws ApiException {
        return getCache("providerId", b -> b.add("name", name))
            .getCollection(() -> tvShowsApi.showsSearchSearchGet(name).getShows().stream()
                .map(showDto -> new ProviderId(showDto.getName(), showDto.getId().toString())).toList());
    }

    public List<ProviderId> getProviderId(int tvdbId) throws ApiException {
        return getCache("providerId", b -> b.add("tvdbId", tvdbId))
            .getCollection(() -> tvShowsApi.showsExternalTvdbTvdbIdGet(tvdbId).getShows().stream()
                .map(showDto -> new ProviderId(showDto.getName(), showDto.getId().toString())).toList());
    }

    public Set<Addic7edProxyGestdownSubtitle> getSubtitles(SerieMapping providerId, int season, int episode,
        Language language) throws ApiException {
        return getCache("subtitles", b -> b.add("providerId", providerId.providerId)
            .add("season", season).add("episode", episode).add("language", language.getName()))
            .getCollection(() -> {
                SubtitleSearchResponse response = subtitlesApi.subtitlesGetShowUniqueIdSeasonEpisodeLanguageGet(
                    language.getName(), UUID.fromString(providerId.providerId), season, episode);
                return response.getMatchingSubtitles()
                    .stream()
                    .filter(SubtitleDto::isCompleted)
                    .map(sub -> mapToSubtitle(sub, response.episode, language))
                    .collect(Collectors.toSet());
            });
    }

    private Addic7edProxyGestdownSubtitle mapToSubtitle(SubtitleDto sub, EpisodeDto episodedto, Language language) {
        return new Addic7edProxyGestdownSubtitle(
            url:DOMAIN + sub.getDownloadUri(),
            subtitleSource:subtitleSource,
            fileName:StringExt.removeIllegalFilenameChars("${episodedto.show} - ${episodedto.title} - ${sub.version}"),
            language:language,
            quality:ReleaseParser.getQualityKeyword(episodedto.getTitle() + " " + sub.getVersion()),
            subtitleMatchType:SubtitleMatchType.EVERYTHING,
            releaseGroup:sub.getVersion(),
            uploader:"",
            hearingImpaired:false);
    }
}
