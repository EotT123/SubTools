package org.lodder.subtools.multisubdownloader.subtitleproviders.tvsubtitles;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleAdapter;
import org.lodder.subtools.multisubdownloader.subtitleproviders.tvsubtitles.exception.TvSubtitleException;
import org.lodder.subtools.multisubdownloader.subtitleproviders.tvsubtitles.model.TVSubtitlesSubtitleMetadata;
import org.lodder.subtools.multisubdownloader.subtitleproviders.tvsubtitles.model.TvSubtiltesSubtitle;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.control.ReleaseParser;
import org.lodder.subtools.sublibrary.data.ProviderId;
import org.lodder.subtools.sublibrary.exception.SubtitlesProviderInitException;
import org.lodder.subtools.sublibrary.model.ProviderIds;
import org.lodder.subtools.sublibrary.model.SubtitleMatchType;
import org.lodder.subtools.sublibrary.model.SubtitleSource;
import org.lodder.subtools.sublibrary.settings.model.SerieMapping;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class TvSubtitlesAdapter
    extends SubtitleAdapter<TVSubtitlesSubtitleMetadata, TvSubtiltesSubtitle, ProviderId, TvSubtitleException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(TvSubtitlesAdapter.class);

    private static TvSubtitlesApi api;
    @val @override SubtitleSource source = SubtitleSource.TVSUBTITLES;
    @val @override boolean useSeasonForSerieId = false;

    public TvSubtitlesAdapter(Manager manager, UserInteractionHandler userInteractionHandler) {
        super(manager, userInteractionHandler);
        if (api == null) {
            try {
                api = new TvSubtitlesApi(manager);
            } catch (Exception e) {
                throw new SubtitlesProviderInitException(name, e);
            }
        }
    }

    @Override
    public List<TVSubtitlesSubtitleMetadata> searchMovieSubtitlesWithHash(String hash, Language language) {
        // TODO implement this
        return List.of();
    }

    @Override
    public List<TVSubtitlesSubtitleMetadata> searchMovieSubtitlesWithId(int tvdbId, Language language) {
        // TODO implement this
        return List.of();
    }

    @Override
    public Collection<TVSubtitlesSubtitleMetadata> searchMovieSubtitlesWithName(String name, @Nullable Integer year,
        Language language) {
        // TODO implement this
        return List.of();
    }

    @Override
    public Collection<TVSubtitlesSubtitleMetadata> searchSubtitles(SerieMapping serieMapping, int season, int episode,
        Language language) throws TvSubtitleException {
        return api.getSubtitles(serieMapping.providerId, season, episode, language);
    }

    @Override
    public TvSubtiltesSubtitle convertToSubtitle(TVSubtitlesSubtitleMetadata sub) {
        return new TvSubtiltesSubtitle(
            url:sub.url,
            subtitleSource:source,
            fileName:sub.filename,
            language:sub.language,
            quality:ReleaseParser.getQualityKeyword(sub.filename + " " + sub.source),
            subtitleMatchType:SubtitleMatchType.EVERYTHING,
            releaseGroup:sub.releaseGroup);
    }

    @Override
    public List<ProviderId> getSortedSerieProviderIds(ProviderIds providerIds, String serieName,
        @Nullable Integer season) throws TvSubtitleException {
        Pattern yearPatter = Pattern.compile("\\((\\d\\d\\d\\d)-(\\d\\d\\d\\d)\\)");
        return api.getProviderIds(serieName)
            .stream()
            .sorted(Comparator.comparing((ProviderId n) -> !serieName.replaceAll("[^A-Za-z]", "")
                    .equalsIgnoreCase(n.name.replaceAll("[^A-Za-z]", "")))
                .thenComparing((ProviderId providerId) -> {
                    Matcher matcher = yearPatter.matcher(providerId.name);
                    if (matcher.find()) {
                        return Integer.parseInt(matcher.group(2));
                    }
                    return 0;
                }, Comparator.reverseOrder()))
            .toList();
    }

    @Override
    public String providerSerieIdToDisplayString(ProviderId providerId) {
        return providerId.name;
    }
}
