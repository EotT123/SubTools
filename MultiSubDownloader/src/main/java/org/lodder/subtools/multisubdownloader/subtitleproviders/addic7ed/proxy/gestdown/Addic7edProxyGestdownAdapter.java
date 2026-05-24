package org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.proxy.gestdown;

import static org.lodder.subtools.sublibrary.model.ProviderIdType.*;
import static util.Utils.*;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.BiPredicate;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.apache.commons.lang3.Strings;
import org.gestdown.model.ShowDto;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleAdapter;
import org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.exception.Addic7edException;
import org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.proxy.gestdown.model.Addic7edProxyGestdownSerieId;
import org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.proxy.gestdown.model.Addic7edProxyGestdownSubtitle;
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
public final class Addic7edProxyGestdownAdapter extends
    SubtitleAdapter<Addic7edProxyGestdownSubtitle, Addic7edProxyGestdownSubtitle, Addic7edProxyGestdownSerieId,
        Addic7edException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Addic7edProxyGestdownAdapter.class);

    private final Addic7edProxyGestdownApi api;
    @val @override SubtitleProviderFrontEnd subtitleProviderFrontEnd = SubtitleProviderFrontEnd.ADDIC7ED_GESTDOWN;
    @val @override boolean useSeasonForSerieId = false;

    public Addic7edProxyGestdownAdapter(UserInteractionHandler userInteractionHandler) {
        super(userInteractionHandler);
        this.api = new Addic7edProxyGestdownApi();
    }

    // ===== \\
    // MOVIE \\
    // ===== \\

    @Override
    public Collection<Addic7edProxyGestdownSubtitle> searchMovieSubtitlesWithHash(String hash, Language language)
        throws Addic7edException {
        return List.of();
    }

    @Override
    public Collection<Addic7edProxyGestdownSubtitle> searchMovieSubtitlesWithId(ProviderIds providerIds,
        Language language) throws Addic7edException {
        return List.of();
    }

    @Override
    public Collection<Addic7edProxyGestdownSubtitle> searchMovieSubtitlesWithName(String name, @Nullable Integer year,
        Language language, ProviderIds providerIds) throws Addic7edException {
        return List.of();
    }

    // ===== \\
    // SERIE \\
    // ===== \\

    @Override
    public List<Addic7edProxyGestdownSerieId> getSerieProviderIdById(ProviderIds providerIds, @Nullable Integer season)
        throws Addic7edException {
        return providerIds.userOrElse(TVDB,
            tvdbId -> ifNotNull(api.getProviderSerieIds(tvdbId), ids -> List.of(toSerieId(ids))), List::of);
    }

    @Override
    public List<Addic7edProxyGestdownSerieId> getSortedSerieProviderIds(String serieName,
        @Nullable Integer season) throws Addic7edException {
        return api.getProviderSerieIds(serieName).stream()
            .sorted(Comparator.comparing(n -> !Strings.CI.equalsAny(serieName.keepLettersOnly(),
                n.name.keepLettersOnly())))
            .map(this::toSerieId)
            .toList();
    }

    private Addic7edProxyGestdownSerieId toSerieId(ShowDto showDto) {
        return new Addic7edProxyGestdownSerieId(showDto.name, showDto.id, showDto.tvDbId, showDto.tmdbId);
    }

    @Override
    public String providerSerieIdToDisplayString(Addic7edProxyGestdownSerieId providerId) {
        return providerId.name;
    }

    @Override
    public Collection<Addic7edProxyGestdownSubtitle> searchSubtitles(ProviderIds providerIds, int season,
        int episode, Language language) throws Addic7edException {
        return List.of();
    }

    @Override
    public Collection<Addic7edProxyGestdownSubtitle> searchSubtitles(SerieMapping serieMapping, int season,
        int episode, Language language) throws Addic7edException {
        LOGGER.debug("$provider - getSubtitles: {}",
            TvRelease.formatName(serieMapping.providerName, season, episode));
        return api.getSubtitles(serieMapping.providerId, season, episode, language);
    }

    // ====== \\
    // COMMON \\
    // ====== \\

    @Override
    public Addic7edProxyGestdownSubtitle convertToSubtitle(Release release, Addic7edProxyGestdownSubtitle sub) {
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
