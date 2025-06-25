package org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed;

import static org.lodder.subtools.multisubdownloader.Messages.*;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleAdapter;
import org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.exception.Addic7edException;
import org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.model.Addic7edMovieSubtitleId;
import org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.model.Addic7edSubtitle;
import org.lodder.subtools.sublibrary.Credentials;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.data.ProviderId;
import org.lodder.subtools.sublibrary.exception.SubtitlesProviderInitException;
import org.lodder.subtools.sublibrary.model.ProviderIds;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.SubtitleProviderFrontEnd;
import org.lodder.subtools.sublibrary.settings.model.SerieMapping;

public final class Addic7edAdapter extends SubtitleAdapter<Addic7edSubtitle, Addic7edSubtitle, ProviderId,
    Addic7edException> {

    private static Addic7edApi api;
    @val @override SubtitleProviderFrontEnd subtitleProviderFrontEnd = SubtitleProviderFrontEnd.ADDIC7ED;
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


    // ===== \\
    // MOVIE \\
    // ===== \\

    @Override
    public List<Addic7edSubtitle> searchMovieSubtitlesWithHash(String hash, Language language) {
        return List.of();
    }

    @Override
    public List<Addic7edSubtitle> searchMovieSubtitlesWithId(ProviderIds providerIds, Language language) {
        return List.of();
    }

    @Override
    public Collection<Addic7edSubtitle> searchMovieSubtitlesWithName(String title, @Nullable Integer year,
        Language language, ProviderIds providerIds) throws Addic7edException {
        return getMovieProviderId(title, year).mapEx(providerId -> api.searchMovieSubtitles(providerId.id, language))
            .orElse(List.of());
    }

    private Optional<Addic7edMovieSubtitleId> getMovieProviderId(String title, @Nullable Integer year)
        throws Addic7edException {
        List<Addic7edMovieSubtitleId> sortedMovieProviderIds = api.getMovieProviderIds(title, year)
            .stream()
            .sorted(Comparator.comparing((Addic7edMovieSubtitleId sId) -> sId.getScore(title, year)).reversed())
            .toList();
        if (sortedMovieProviderIds.isEmpty()) {
            return Optional.empty();
        }
        if (!userInteractionHandler.settings.optionsConfirmProviderMapping && sortedMovieProviderIds.size() == 1) {
            // If only one releases mapping is found and the user has disabled confirmation for single results,
            // automatically select this mapping as the desired one.
            return Optional.of(sortedMovieProviderIds.first());
        } else {
            String selectFromListMessage =
                year == null ? getText("SelectDialog.SelectMovieNameForName", title) :
                    getText("SelectDialog.SelectMovieNameForNameWithSeason", title, year);
            // Prompt the user to select the correct provider release id.
            return userInteractionHandler.selectFromList(
                sortedMovieProviderIds,
                selectFromListMessage,
                provider,
                Addic7edMovieSubtitleId::getName);
        }
    }

    // ===== \\
    // SERIE \\
    // ===== \\

    @Override
    public List<ProviderId> getSerieProviderIdById(ProviderIds providerIds, @Nullable Integer season)
        throws Addic7edException {
        return List.of();
    }

    @Override
    public List<ProviderId> getSortedSerieProviderIds(String serieName, @Nullable Integer season)
        throws Addic7edException {
        return api.getSerieProviderId(serieName)
            .stream()
            .sorted(Comparator.comparing(n -> !serieName.replaceAll("[^A-Za-z]", "")
                .equalsIgnoreCase(n.name.replaceAll("[^A-Za-z]", ""))))
            .toList();
    }

    @Override
    public String providerSerieIdToDisplayString(ProviderId providerId) {
        return providerId.name;
    }

    @Override
    public Optional<Collection<Addic7edSubtitle>> searchSubtitles(ProviderIds providerIds, int season,
        int episode, Language language) throws Addic7edException {
        return Optional.empty();
    }

    @Override
    public Optional<Collection<Addic7edSubtitle>> searchSubtitles(SerieMapping serieMapping, int season,
        int episode, Language language) throws Addic7edException {
        return Optional.of(api.searchSerieSubtitles(serieMapping.providerId, serieMapping.providerName, season, episode,
            language));
    }

    // ====== \\
    // COMMON \\
    // ====== \\

    @Override
    public Addic7edSubtitle convertToSubtitle(Release release, Addic7edSubtitle sub) {
        return sub;
    }
}
