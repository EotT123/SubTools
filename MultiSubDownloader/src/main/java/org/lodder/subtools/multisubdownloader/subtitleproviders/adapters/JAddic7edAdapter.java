package org.lodder.subtools.multisubdownloader.subtitleproviders.adapters;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.OptionalInt;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import extensions.java.lang.String.StringExt;
import lombok.Getter;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.JAddic7edApi;
import org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.exception.Addic7edException;
import org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.model.Addic7edSubtitleDescriptor;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.control.ReleaseParser;
import org.lodder.subtools.sublibrary.data.ProviderSerieId;
import org.lodder.subtools.sublibrary.exception.SubtitlesProviderInitException;
import org.lodder.subtools.sublibrary.model.MovieRelease;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.SubtitleMatchType;
import org.lodder.subtools.sublibrary.model.SubtitleSource;
import org.lodder.subtools.sublibrary.model.TvRelease;
import org.lodder.subtools.sublibrary.util.lazy.LazySupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Getter
public final class JAddic7edAdapter extends AbstractAdapter<Addic7edSubtitleDescriptor, ProviderSerieId,
    Addic7edException> {

    private static final Logger LOGGER = LoggerFactory.getLogger(JAddic7edAdapter.class);

    private static LazySupplier<JAddic7edApi> jaapi;
    @val @override SubtitleSource subtitleSource = SubtitleSource.ADDIC7ED;
    @val @override String providerName = subtitleSource.name();

    public JAddic7edAdapter(boolean isLoginEnabled, String username, String password, boolean speedy, Manager manager,
        UserInteractionHandler userInteractionHandler) {
        super(manager, userInteractionHandler);
        if (jaapi == null) {
            jaapi = new LazySupplier<>(() -> {
                try {
                    return isLoginEnabled ? new JAddic7edApi(username, password, speedy, manager) :
                        new JAddic7edApi(speedy, manager);
                } catch (Exception e) {
                    throw new SubtitlesProviderInitException(providerName, e);
                }
            });
        }
    }

    private JAddic7edApi getApi() {
        return jaapi.get();
    }


    @Override
    public List<Addic7edSubtitleDescriptor> searchMovieSubtitlesWithHash(String hash, Language language) {
        // TODO implement this
        return List.of();
    }

    @Override
    public List<Addic7edSubtitleDescriptor> searchMovieSubtitlesWithId(int tvdbId, Language language) {
        // TODO implement this
        return List.of();
    }

    @Override
    public List<Addic7edSubtitleDescriptor> searchMovieSubtitlesWithName(String name, int year, Language language) {
        // TODO implement this
        return List.of();
    }

    @Override
    public Set<Subtitle> convertToSubtitles(MovieRelease movieRelease, Set<Addic7edSubtitleDescriptor> subtitles,
        Language language) {
        // TODO implement this
        return Set.of();
    }

    @Override
    public Set<Addic7edSubtitleDescriptor> searchSerieSubtitles(TvRelease tvRelease, Language language)
        throws Addic7edException {
        return getProviderSerieId(tvRelease).map(
            providerSerieId -> tvRelease.episodeNumbers.stream().flatMap(episode -> {
                try {
                    return getApi().getSubtitles(providerSerieId, tvRelease.season, episode, language).stream();
                } catch (Addic7edException e) {
                    LOGGER.error("API %s searchSubtitles for serie [%s] (%s)".formatted(subtitleSource.name,
                        TvRelease.formatName(providerSerieId.providerName, tvRelease.season, episode),
                        e.getMessage()), e);
                    return Stream.empty();
                }
            }).collect(Collectors.toSet())).orElseGet(Set::of);
    }

    @Override
    public Set<Subtitle> convertToSubtitles(TvRelease tvRelease, Collection<Addic7edSubtitleDescriptor> subtitles,
        Language language) {
        return subtitles.stream()
            .filter(sub -> language == sub.getLanguage())
            .map(sub -> Subtitle.downloadSource(sub.getUrl())
                .subtitleSource(subtitleSource)
                .fileName(StringExt.removeIllegalFilenameChars(sub.getTitle() + " " + sub.getVersion()))
                .language(sub.getLanguage())
                .quality(ReleaseParser.getQualityKeyword(sub.getTitle() + " " + sub.getVersion()))
                .subtitleMatchType(SubtitleMatchType.EVERYTHING)
                .releaseGroup(ReleaseParser.extractReleaseGroup(sub.getTitle(), sub.getTitle().endsWith(".srt")))
                .uploader(sub.getUploader())
                .hearingImpaired(false))
            .collect(Collectors.toSet());
    }

    @Override
    public List<ProviderSerieId> getSortedProviderSerieIds(OptionalInt tvdbIdOptional, String serieName, int season)
        throws Addic7edException {
        return getApi().getProviderId(serieName)
            .stream()
            .sorted(Comparator.comparing(n -> !serieName.replaceAll("[^A-Za-z]", "")
                .equalsIgnoreCase(n.name.replaceAll("[^A-Za-z]", ""))))
            .toList();
    }

    @Override
    public boolean useSeasonForSerieId() {
        return true;
    }

    @Override
    public String providerSerieIdToDisplayString(ProviderSerieId providerSerieId) {
        return providerSerieId.name;
    }
}
