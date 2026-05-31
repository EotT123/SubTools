package org.lodder.subtools.multisubdownloader.subtitleproviders.tvsubtitles;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleAdapter;
import org.lodder.subtools.multisubdownloader.subtitleproviders.tvsubtitles.exception.TvSubtitleException;
import org.lodder.subtools.multisubdownloader.subtitleproviders.tvsubtitles.model.TVSubtitlesSubtitleMetadata;
import org.lodder.subtools.multisubdownloader.subtitleproviders.tvsubtitles.model.TvSubtiltesSubtitle;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.control.ReleaseParser;
import org.lodder.subtools.sublibrary.data.ProviderId;
import org.lodder.subtools.sublibrary.model.ProviderIds;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.SubtitleProviderFrontEnd;
import org.lodder.subtools.sublibrary.settings.model.SerieMapping;

@NullMarked
public final class TvSubtitlesAdapter
    extends SubtitleAdapter<TVSubtitlesSubtitleMetadata, TvSubtiltesSubtitle, ProviderId, TvSubtitleException> {

    private final TvSubtitlesApi api = new TvSubtitlesApi();
    @val @override SubtitleProviderFrontEnd subtitleProviderFrontEnd = SubtitleProviderFrontEnd.TVSUBTITLES;
    @val @override boolean useSeasonForSerieId = false;

    public TvSubtitlesAdapter(UserInteractionHandler userInteractionHandler) {
        super(userInteractionHandler);
    }

    // ===== \\
    // MOVIE \\
    // ===== \\

    @Override
    public List<TVSubtitlesSubtitleMetadata> searchMovieSubtitlesWithHash(String hash, Language language) {
        // no movie information available for provider
        return List.of();
    }

    @Override
    public List<TVSubtitlesSubtitleMetadata> searchMovieSubtitlesWithId(ProviderIds providerIds, Language language) {
        // no movie information available for provider
        return List.of();
    }

    @Override
    public Collection<TVSubtitlesSubtitleMetadata> searchMovieSubtitlesWithName(String name, @Nullable Integer year,
        Language language, ProviderIds providerIds) {
        // no movie information available for provider
        return List.of();
    }

    // ===== \\
    // SERIE \\
    // ===== \\

    @Override
    public Collection<TVSubtitlesSubtitleMetadata> searchSubtitles(ProviderIds providerIds, int season,
        int episode, Language language) throws TvSubtitleException {
        return List.of();
    }

    @Override
    public Collection<TVSubtitlesSubtitleMetadata> searchSubtitles(SerieMapping serieMapping, int season,
        int episode, Language language) throws TvSubtitleException {
        return api.getSubtitles(serieMapping.providerId, season, episode, language);
    }

    @Override
    public @Nullable ProviderId getSerieProviderIdById(ProviderIds providerIds, @Nullable Integer season) {
        return null;
    }

    @Override
    public List<ProviderId> getSortedSerieProviderIds(String serieName, @Nullable Integer season)
        throws TvSubtitleException {
        Pattern yearPattern = Pattern.compile("\\((\\d\\d\\d\\d)-(\\d\\d\\d\\d)\\)");
        return api.getProviderIds(serieName)
            .stream()
            .sorted(Comparator.comparing((ProviderId n) -> !serieName.replaceAll("[^A-Za-z]", "")
                    .equalsIgnoreCase(n.name.replaceAll("[^A-Za-z]", "")))
                .thenComparing((ProviderId providerId) -> {
                    Matcher matcher = yearPattern.matcher(providerId.name);
                    return matcher.find() ? Integer.parseInt(matcher.group(2)) : 0;
                }, Comparator.reverseOrder()))
            .toList();
    }

    @Override
    public String providerSerieIdToDisplayString(ProviderId providerId) {
        return providerId.name;
    }

    // ====== \\
    // COMMON \\
    // ====== \\

    @Override
    public TvSubtiltesSubtitle convertToSubtitle(Release release, TVSubtitlesSubtitleMetadata sub) {
        return new TvSubtiltesSubtitle(
            url:sub.url,
            fileName:sub.filename,
            language:sub.language,
            quality:ReleaseParser.getQualityKeyword(sub.filename + " " + sub.source),
            releaseGroup:sub.releaseGroup,
            uploader:sub.uploader);
    }
}
