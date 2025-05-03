package org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.proxy.gestdown;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.pivovarit.function.ThrowingSupplier;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.gestdown.invoker.ApiException;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleAdapter;
import org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.proxy.gestdown.model.Addic7edProxyGestdownSubtitle;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.data.ProviderId;
import org.lodder.subtools.sublibrary.model.SubtitleSource;
import org.lodder.subtools.sublibrary.model.TvRelease;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public final class JAddic7edViaProxyAdapter extends
    SubtitleAdapter<Addic7edProxyGestdownSubtitle, Addic7edProxyGestdownSubtitle, ProviderId, ApiException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JAddic7edViaProxyAdapter.class);

    private final JAddic7edProxyGestdownApi api;
    @val @override SubtitleSource source = SubtitleSource.ADDIC7ED;
    @val @override boolean useSeasonForSerieId = false;

    public JAddic7edViaProxyAdapter(Manager manager, UserInteractionHandler userInteractionHandler) {
        super(manager, userInteractionHandler);
        this.api = new JAddic7edProxyGestdownApi(manager);
    }

    @Override
    public Collection<Addic7edProxyGestdownSubtitle> searchMovieSubtitlesWithHash(String hash, Language language)
        throws ApiException {
        // TODO implement this
        return List.of();
    }

    @Override
    public Collection<Addic7edProxyGestdownSubtitle> searchMovieSubtitlesWithId(int tvdbId, Language language)
        throws ApiException {
        // TODO implement this
        return List.of();
    }

    @Override
    public Collection<Addic7edProxyGestdownSubtitle> searchMovieSubtitlesWithName(String name, @Nullable Integer year,
        Language language) throws ApiException {
        // TODO implement this
        return List.of();
    }

    @Override
    public Set<Addic7edProxyGestdownSubtitle> searchSerieSubtitles(TvRelease tvRelease,
        Language language) throws ApiException {
        return getProviderSerieId(tvRelease).map(
            providerSerieId -> tvRelease.episodes.stream().flatMap(episode -> {
                try {
                    return new ExecuteCall<>(
                        () -> api.getSubtitles(providerSerieId, tvRelease.season, episode, language))
                        .message("getSubtitles: [%s]".formatted(
                            TvRelease.formatName(providerSerieId.providerName, tvRelease.season, episode)))
                        .retryWhenHttpCode(ReturnCode.REFRESHING)
                        .retryWhenHttpCode(ReturnCode.RATE_LIMIT_REACHED)
                        .execute()
                        .stream();
                } catch (ApiException e) {
                    LOGGER.error("API $name searchSubtitles for serie [%s] (%s)".formatted(
                        TvRelease.formatName(providerSerieId.providerName, tvRelease.season, episode),
                        e.getMessage()), e);
                    return Stream.empty();
                }
            }).collect(Collectors.toSet())).orElseGet(Set::of);
    }

    @Override
    public List<ProviderId> getSortedProviderSerieIds(@Nullable Integer tvdbId, @Nullable Integer imdbId,
        String serieName, int season) throws ApiException {
        List<ProviderId> serieIds = tvdbId == null ? List.of() :
            new ExecuteCall<>(() -> api.getProviderId(tvdbId))
                .message("getProviderSerieName: [$tvdbId]")
                .retryWhenHttpCode(ReturnCode.RATE_LIMIT_REACHED)
                .handleHttpCode(ReturnCode.NOT_FOUND, () -> {
                    LOGGER.info("API $name - Could not find tvdbId [%s]".formatted(tvdbId));
                    return List.of();
                })
                .execute();

        if (serieIds.isEmpty()) {
            serieIds = new ExecuteCall<>(() -> api.getProviderId(serieName)).message(
                    "getProviderSerieName: [$serieName]")
                .retryWhenHttpCode(ReturnCode.RATE_LIMIT_REACHED)
                .handleHttpCode(ReturnCode.NOT_FOUND, () -> {
                    LOGGER.info("API $providerName - Could not find serie name [$serieName]");
                    return List.of();
                })
                .execute();
        }
        return serieIds.stream()
            .sorted(Comparator.comparing(n -> !serieName.replaceAll("[^A-Za-z]", "")
                .equalsIgnoreCase(n.name.replaceAll("[^A-Za-z]", ""))))
            .toList();
    }

    @Override
    public Addic7edProxyGestdownSubtitle convertToSubtitle(Addic7edProxyGestdownSubtitle sub, Language language) {
        return sub;
    }

    @Override
    public String providerSerieIdToDisplayString(ProviderId providerId) {
        return providerId.name;
    }

    @Getter
    @RequiredArgsConstructor
    private enum ReturnCode {
        NOT_FOUND(404), RATE_LIMIT_REACHED(429), REFRESHING(423);

        final int code;

        public boolean isSameCode(int code) {
            return this.code == code;
        }
    }

    private static class ExecuteCall<T> extends SubtitleAdapter.ExecuteCall<T, ApiException> {

        public ExecuteCall(ThrowingSupplier<T, ApiException> supplier) {
            super(supplier);
        }

        public ExecuteCall<T> retryWhenHttpCode(ReturnCode returnCode) {
            super.retryWhenException(e -> returnCode.isSameCode(e.getCode()));
            return this;
        }

        public ExecuteCall<T> handleHttpCode(ReturnCode returnCode, Function<ApiException, T> function) {
            super.handleException(e -> returnCode.isSameCode(e.getCode()), function);
            return this;
        }

        public ExecuteCall<T> handleHttpCode(ReturnCode returnCode, Supplier<T> supplier) {
            super.handleException(e -> returnCode.isSameCode(e.getCode()), supplier);
            return this;
        }

        @Override
        public ExecuteCall<T> handleException(Supplier<T> suppliers) {
            super.handleException(_ -> true, suppliers);
            return this;
        }
    }
}
