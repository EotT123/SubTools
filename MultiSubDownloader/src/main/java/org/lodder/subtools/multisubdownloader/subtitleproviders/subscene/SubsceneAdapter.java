package org.lodder.subtools.multisubdownloader.subtitleproviders.subscene;

import static org.lodder.subtools.multisubdownloader.subtitleproviders.subscene.model.SearchResultType.*;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.function.ToIntFunction;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleAdapter;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subscene.exception.SubsceneException;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subscene.model.SearchResultType;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subscene.model.SubSceneSerieId;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subscene.model.SubsceneSubtitle;
import org.lodder.subtools.multisubdownloader.subtitleproviders.subscene.model.SubsceneSubtitleMetadata;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.control.ReleaseParser;
import org.lodder.subtools.sublibrary.model.ProviderIds;
import org.lodder.subtools.sublibrary.model.SubtitleSource;
import org.lodder.subtools.sublibrary.settings.model.SerieMapping;

public final class SubsceneAdapter
    extends SubtitleAdapter<SubsceneSubtitleMetadata, SubsceneSubtitle, SubSceneSerieId, SubsceneException> {

    private static SubsceneApi api;
    @val @override SubtitleSource source = SubtitleSource.SUBSCENE;
    @val @override boolean useSeasonForSerieId = true;

    public SubsceneAdapter(Manager manager, UserInteractionHandler userInteractionHandler) {
        super(manager, userInteractionHandler);
        if (api == null) {
            api = new SubsceneApi(manager);
        }
    }

    // ===== \\
    // MOVIE \\
    // ===== \\

    @Override
    public List<SubsceneSubtitleMetadata> searchMovieSubtitlesWithHash(String hash, Language language) {
        // TODO implement this
        return List.of();
    }

    @Override
    public List<SubsceneSubtitleMetadata> searchMovieSubtitlesWithId(ProviderIds providerIds, Language language) {
        // TODO implement this
        return List.of();
    }

    @Override
    public Collection<SubsceneSubtitleMetadata> searchMovieSubtitlesWithName(String name, @Nullable Integer year,
        Language language, ProviderIds providerIds) {
        // TODO implement this
        return List.of();
    }

    // ===== \\
    // SERIE \\
    // ===== \\

    @Override
    public List<SubSceneSerieId> getSerieProviderIdById(ProviderIds providerIds, Integer season)
        throws SubsceneException {
        return providerIds.getImdbId().mapEx(imdbId -> getSortedSerieProviderIds(imdbId,
            Objects.requireNonNull(season))).orElseGet(List::of);
    }

    /**
     * @param searchQuery the name of the serie or the imdb id
     * @param season the season number of the serie
     * @return list of matching SubSceneSerieId
     * @throws SubsceneException SubsceneException
     */
    @Override
    public List<SubSceneSerieId> getSortedSerieProviderIds(String searchQuery, Integer season)
        throws SubsceneException {
        ToIntFunction<SearchResultType> providerTypeFunction = value -> switch (value) {
            case EXACT -> 2;
            case TV_SERIE -> 1;
            case CLOSE -> 3;
            case null -> 4;
        };
        Map<SearchResultType, List<SubSceneSerieId>> serieProviderIds = api.getSerieProviderIds(searchQuery);
        List<SubSceneSerieId> filteredResults =
            serieProviderIds.get(TV_SERIE).stream().filter(subSceneSerieId -> Objects.equals(subSceneSerieId.season,
                season)).toList();
        if (filteredResults.size() == 1) {
            return filteredResults;
        }
        return api.getSerieProviderIds(searchQuery)
            .entrySet()
            .stream()
            .sorted(Comparator.comparingInt(entry -> providerTypeFunction.applyAsInt(entry.getKey())))
            .map(Entry::getValue)
            .flatMap(values -> values.stream().sorted(Comparator.comparing(s -> s.getScore(searchQuery, season))))
            .distinct()
            .toList();
    }

    @Override
    public Collection<SubsceneSubtitleMetadata> searchSubtitles(ProviderIds providerIds, int season, int episode,
        Language language) throws SubsceneException {
        return List.of();
    }

    @Override
    public Collection<SubsceneSubtitleMetadata> searchSubtitles(SerieMapping serieMapping, int season, int episode,
        Language language) throws SubsceneException {
        return api.getSubtitles(serieMapping.providerId, season, episode, language);
    }

    @Override
    public String providerSerieIdToDisplayString(SubSceneSerieId providerSerieId) {
//        if (providerSerieId.id.endsWith("-season")) {
//            OptionalInt season = IntStream.rangeClosed(1, 100)
//                .filter(i -> providerSerieId.id.endsWith("-${SubsceneApi.getOrdinalName(i).toLowerCase()}-season"))
//                .findAny();
//            if (season.isPresent()) {
//                return "%s %s %s".formatted(providerSerieId.name, Messages.getText("App.Season"), season.getAsInt());
//            }
//        }
        return providerSerieId.name;
    }

    // ====== \\
    // COMMON \\
    // ====== \\

    @Override
    public SubsceneSubtitle convertToSubtitle(SubsceneSubtitleMetadata sub) {
        return new SubsceneSubtitle(
            urlSupplier:sub.urlSupplier,
            fileName:sub.name.removeIllegalFilenameChars(),
            language:sub.language,
            quality:ReleaseParser.getQualityKeyword(sub.name),
            releaseGroup:ReleaseParser.extractReleaseGroup(sub.name, false),
            uploader:sub.uploader,
            hearingImpaired:sub.hearingImpaired);
    }
}
