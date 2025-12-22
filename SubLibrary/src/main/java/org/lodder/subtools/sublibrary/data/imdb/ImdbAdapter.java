package org.lodder.subtools.sublibrary.data.imdb;

import static org.lodder.subtools.multisubdownloader.Messages.*;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.data.AdapterIntf;
import org.lodder.subtools.sublibrary.data.imdb.exception.ImdbException;
import org.lodder.subtools.sublibrary.data.imdb.exception.ImdbSearchIdException;
import org.lodder.subtools.sublibrary.data.imdb.model.ImdbDetails;
import org.lodder.subtools.sublibrary.data.imdb.model.ImdbId;
import org.lodder.subtools.sublibrary.model.VideoType;
import org.lodder.subtools.sublibrary.settings.model.MovieMapping;
import org.lodder.subtools.sublibrary.settings.model.ReleaseMapping;
import org.lodder.subtools.sublibrary.settings.model.SerieMapping;
import org.lodder.subtools.sublibrary.userinteraction.UserInteractionHandler;
import org.lodder.subtools.sublibrary.util.throwingfunction.ThrowingTriFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
public class ImdbAdapter implements AdapterIntf {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImdbAdapter.class);
    private static ImdbAdapter instance;
    @val @override Manager manager;
    @val @override String provider = "IMDB";
    private final UserInteractionHandler userInteractionHandler;
    private final ImdbApi imdbApi;
    private final ImdbSearchIdApi imdbSearchIdApi;

    private ImdbAdapter(Manager manager, UserInteractionHandler userInteractionHandler) {
        this.manager = manager;
        this.userInteractionHandler = userInteractionHandler;
        this.imdbApi = new ImdbApi(manager);
        this.imdbSearchIdApi = new ImdbSearchIdApi(manager);
    }

    public Optional<ImdbDetails> getDetails(String imdbId) {
        return getCache("details", b -> b.add("imdbId", imdbId))
            .getOptional(() -> {
                try {
                    return imdbApi.getDetails(imdbId);
                } catch (ImdbException e) {
                    LOGGER.error("$provider: error while fetching details for id [$imdbId]", e);
                    return Optional.empty();
                }
            });
    }

    public Optional<String> getImdbId(String title, VideoType videoType, @Nullable Integer year=null) {
        try {
            @SuppressWarnings("unchecked")
            Optional<ReleaseMapping> releaseMapping =
                (Optional<ReleaseMapping>) getCache(videoType.name() + "mapping",
                    b -> b.add("title", title).add("videoType", videoType).add("year", year))
                    .getOptional(
                        () -> getImdbIdOnImdb(title, year, videoType)
                            .orElseMap(() -> getImdbIdOnGoogle(title, year, videoType))
                            .orElseMap(() -> getImdbIdOnYahoo(title, year, videoType))
                            .orElseMap(
                                () -> promptUserToEnterImdbId(title).flatMap(imdbId -> getImdbIdOnImdb(title, imdbId)))
                            .map(imdbId -> switch (videoType) {
                                case EPISODE -> new SerieMapping(title, imdbId.id, imdbId.name);
                                case MOVIE -> new MovieMapping(title, imdbId.id, imdbId.name, year);
                            }),
                        storeTempNullValue:true);
            return releaseMapping.map(ReleaseMapping::getProviderId);
        } catch (Exception e) {
            LOGGER.error("API %s getImdbId for title [%s] (%s)".formatted(provider, title, e.getMessage()), e);
            return Optional.empty();
        }
    }

    private Optional<ImdbId> getImdbIdOnImdb(String title, @Nullable Integer year, VideoType videoType) {
        return getImdbIdCommon(title, year, videoType, imdbSearchIdApi::getImdbIdOnImdb);
    }

    private Optional<ImdbId> getImdbIdOnImdb(String title, String imdbId) {
        try {
            return Optional.of(imdbSearchIdApi.getImdbIdOnImdb(title, imdbId));
        } catch (ImdbSearchIdException e) {
            LOGGER.error("API %s getImdbId for title [%s] and provided imdbId [imdbId] (%s)".formatted(provider, title,
                e.getMessage()), e);
            return Optional.empty();
        }
    }


    private Optional<ImdbId> getImdbIdOnGoogle(String title, @Nullable Integer year, VideoType videoType) {
        return getImdbIdCommon(title, year, videoType, imdbSearchIdApi::getImdbIdOnGoogle);
    }

    private Optional<ImdbId> getImdbIdOnYahoo(String title, @Nullable Integer year, VideoType videoType) {
        return getImdbIdCommon(title, year, videoType, imdbSearchIdApi::getImdbIdOnYahoo);
    }

    private Optional<ImdbId> getImdbIdCommon(String title, @Nullable Integer year, VideoType videoType,
        ThrowingTriFunction<String, Integer, VideoType, Collection<ImdbId>, ImdbSearchIdException> providerSerieIdSupplier) {
        Collection<ImdbId> providerIds;
        try {
            providerIds = providerSerieIdSupplier.apply(title, year, videoType);
        } catch (ImdbSearchIdException e) {
            LOGGER.error("API %s getImdbId for title [%s] and year [%s] (%s)".formatted(provider, title, year,
                e.getMessage()), e);
            return Optional.empty();
        }
        if (!userInteractionHandler.settings.optionsConfirmProviderMapping && providerIds.size() == 1) {
            // found single exact match
            return Optional.of(providerIds.iterator().next());
        }
        Pattern yearPattern = Pattern.compile("(?<year>[1-2]\\d{3})");
        return userInteractionHandler
            .selectFromList(
                providerIds.stream().sorted(
                    Comparator.comparing((ImdbId imdbPID) -> imdbPID.videoType == videoType ? -1 : 1)
                        .thenComparing(imdbPID -> imdbPID.calculateLevenshteinDistance(title))
                        .thenComparing(imdbPID -> {
                            if (imdbPID.year != null) {
                                Matcher matcher = yearPattern.matcher(imdbPID.year);
                                Integer lastYear = null;
                                while (matcher.find()) {
                                    lastYear = Integer.parseInt(matcher.group("year"));
                                }
                                if (lastYear != null) {
                                    return Math.abs((year == null ? LocalDate.now().year : year) - lastYear);
                                }
                            }
                            return 0;
                        })).toList(),
                getText("Prompter.SelectImdbMatchForSerie", title),
                provider, providerId -> providerId.name + (StringUtils.isNotBlank(providerId.otherInfo) ?
                    " (" + providerId.otherInfo + ")" : ""));
    }

    private Optional<String> promptUserToEnterImdbId(String title) {
        return userInteractionHandler.enter(provider, getText("Prompter.EnterImdbIdForSerie", title));
    }

    public static synchronized ImdbAdapter getInstance(Manager manager, UserInteractionHandler userInteractionHandler) {
        if (instance == null) {
            instance = new ImdbAdapter(manager, userInteractionHandler);
        }
        return instance;
    }
}
