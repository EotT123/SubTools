package org.lodder.subtools.multisubdownloader.subtitleprovider.addic7ed.proxy.subvault;

import static org.lodder.subtools.sublibrary.model.ProviderIdType.*;
import static util.Utils.*;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiPredicate;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.apache.commons.lang3.Strings;
import org.subvault.model.ShowDto;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.subtitleprovider.SubtitleAdapter;
import org.lodder.subtools.multisubdownloader.subtitleprovider.addic7ed.exception.Addic7edException;
import org.lodder.subtools.multisubdownloader.subtitleprovider.addic7ed.proxy.subvault.model.Addic7edProxySubvaultSerieId;
import org.lodder.subtools.multisubdownloader.subtitleprovider.addic7ed.proxy.subvault.model.Addic7edProxySubvaultSubtitle;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.model.ProviderIds;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.SubtitleProviderFrontEnd;
import org.lodder.subtools.sublibrary.model.TvRelease;
import org.lodder.subtools.sublibrary.settings.model.SerieMapping;
import org.lodder.subtools.sublibrary.util.webpage.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
public final class Addic7edProxySubvaultAdapter extends
    SubtitleAdapter<Addic7edProxySubvaultSubtitle, Addic7edProxySubvaultSubtitle, Addic7edProxySubvaultSerieId,
        Addic7edException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Addic7edProxySubvaultAdapter.class);

    private final Addic7edProxySubvaultApi api = new Addic7edProxySubvaultApi();
    @val @override SubtitleProviderFrontEnd subtitleProviderFrontEnd = SubtitleProviderFrontEnd.ADDIC7ED_SUBVAULT;
    @val @override boolean useSeasonForSerieId = false;

    public Addic7edProxySubvaultAdapter(UserInteractionHandler userInteractionHandler) {
        super(userInteractionHandler);
    }

    // ===== \\
    // MOVIE \\
    // ===== \\

    @Override
    public Collection<Addic7edProxySubvaultSubtitle> searchMovieSubtitlesWithHash(String hash, Language language)
        throws Addic7edException {
        return List.of();
    }

    @Override
    public Collection<Addic7edProxySubvaultSubtitle> searchMovieSubtitlesWithId(ProviderIds providerIds,
        Language language) throws Addic7edException {
        return List.of();
    }

    @Override
    public Collection<Addic7edProxySubvaultSubtitle> searchMovieSubtitlesWithName(String name, @Nullable Integer year,
        Language language, ProviderIds providerIds) throws Addic7edException {
        return List.of();
    }

    // ===== \\
    // SERIE \\
    // ===== \\

    @Override
    public @Nullable Addic7edProxySubvaultSerieId getSerieProviderIdById(ProviderIds providerIds,
        @Nullable Integer season) throws Addic7edException {
        return providerIds.get(TVDB, tvdbId -> ifNotNull(api.getProviderSerieId(tvdbId), this::toSerieId));
    }

    @Override
    public List<Addic7edProxySubvaultSerieId> getSortedSerieProviderIds(String serieName,
        @Nullable Integer season) throws Addic7edException {
        return api.getProviderSerieIds(serieName).stream()
            .sorted(Comparator.comparing(n -> !Strings.CI.equals(serieName.keepLettersOnly(),
                n.name.keepLettersOnly())))
            .map(this::toSerieId)
            .toList();
    }

    private Addic7edProxySubvaultSerieId toSerieId(ShowDto showDto) {
        return new Addic7edProxySubvaultSerieId(showDto.name, showDto.id, showDto.tvDbId, showDto.tmdbId);
    }

    @Override
    public String providerSerieIdToDisplayString(Addic7edProxySubvaultSerieId providerId) {
        return providerId.name;
    }

    @Override
    public Collection<Addic7edProxySubvaultSubtitle> searchSubtitles(ProviderIds providerIds, int season,
        int episode, Language language) throws Addic7edException {
        return List.of();
    }

    @Override
    public Collection<Addic7edProxySubvaultSubtitle> searchSubtitles(SerieMapping serieMapping, int season,
        int episode, Language language) throws Addic7edException {
        LOGGER.debug("$provider - getSubtitles: {}",
            TvRelease.formatName(serieMapping.providerName, season, episode));
        return api.getSubtitles(serieMapping.providerId, season, episode, language);
    }

    // ====== \\
    // COMMON \\
    // ====== \\

    @Override
    public Addic7edProxySubvaultSubtitle convertToSubtitle(Release release, Addic7edProxySubvaultSubtitle sub) {
        return sub;
    }


    @NullMarked
    private enum ReturnCode {
        NOT_FOUND((code, _) -> code == HttpStatus.NOT_FOUND),
        RATE_LIMIT_REACHED((code, _) -> code == HttpStatus.TOO_MANY_REQUESTS),
        REFRESHING((code, _) -> code == HttpStatus.LOCKED);

        @val BiPredicate<HttpStatus, String> predicate;

        ReturnCode(BiPredicate<HttpStatus, String> predicate) {
            this.predicate = predicate;
        }
    }
}
