package org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import lombok.Getter;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleAdapter;
import org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.exception.Addic7edException;
import org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.model.Addic7edSubtitle;
import org.lodder.subtools.sublibrary.Credentials;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.data.ProviderSerieId;
import org.lodder.subtools.sublibrary.exception.SubtitlesProviderInitException;
import org.lodder.subtools.sublibrary.model.SubtitleSource;
import org.lodder.subtools.sublibrary.model.TvRelease;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public final class Addic7edAdapter extends SubtitleAdapter<Addic7edSubtitle, Addic7edSubtitle, ProviderSerieId,
    Addic7edException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Addic7edAdapter.class);

    private static Addic7edApi api;
    @val @override SubtitleSource source = SubtitleSource.ADDIC7ED;
    @val @override Manager manager;
    @val @override boolean useSeasonForSerieId = true;

    public Addic7edAdapter(Manager manager, boolean speedy, Credentials credentials=null,
        UserInteractionHandler userInteractionHandler) {
        super(manager, userInteractionHandler);
        if (api == null) {
            try {
                api = new Addic7edApi(manager, speedy, credentials);
            } catch (Exception e) {
                throw new SubtitlesProviderInitException(provider, e);
            }
        }
    }

    @Override
    public List<Addic7edSubtitle> searchMovieSubtitlesWithHash(String hash, Language language) {
        // TODO implement this
        return List.of();
    }

    @Override
    public List<Addic7edSubtitle> searchMovieSubtitlesWithId(int tvdbId, Language language) {
        // TODO implement this
        return List.of();
    }

    @Override
    public Collection<Addic7edSubtitle> searchMovieSubtitlesWithName(String name, @Nullable Integer year,
        Language language) {
        // TODO implement this
        return List.of();
    }

    @Override
    public Set<Addic7edSubtitle> searchSerieSubtitles(TvRelease tvRelease, Language language)
        throws Addic7edException {
        return getProviderSerieId(tvRelease).map(
            providerSerieId -> tvRelease.episodes.stream().flatMap(episode -> {
                try {
                    return api.getSubtitles(providerSerieId, tvRelease.season, episode, language).stream();
                } catch (Addic7edException e) {
                    LOGGER.error("API $name searchSubtitles for serie [%s] (%s)".formatted(
                        TvRelease.formatName(providerSerieId.providerName, tvRelease.season, episode),
                        e.getMessage()), e);
                    return Stream.empty();
                }
            }).collect(Collectors.toSet())).orElseGet(Set::of);
    }

    @Override
    public Addic7edSubtitle convertToSubtitle(Addic7edSubtitle sub, Language language) {
        return sub;
    }

    @Override
    public List<ProviderSerieId> getSortedProviderSerieIds(@Nullable Integer tvdbId, @Nullable Integer imdbId,
        String serieName, int season) throws Addic7edException {
        return api.getProviderId(serieName)
            .stream()
            .sorted(Comparator.comparing(n -> !serieName.replaceAll("[^A-Za-z]", "")
                .equalsIgnoreCase(n.name.replaceAll("[^A-Za-z]", ""))))
            .toList();
    }

    @Override
    public String providerSerieIdToDisplayString(ProviderSerieId providerSerieId) {
        return providerSerieId.name;
    }
}
