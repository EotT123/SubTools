package org.lodder.subtools.sublibrary.data.imdb;

import static org.lodder.subtools.multisubdownloader.Messages.*;
import static util.Utils.*;

import java.time.LocalDate;
import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.data.AdapterIntf;
import org.lodder.subtools.sublibrary.data.imdb.exception.ImdbException;
import org.lodder.subtools.sublibrary.data.imdb.exception.ImdbSearchIdException;
import org.lodder.subtools.sublibrary.data.imdb.model.ImdbDetails;
import org.lodder.subtools.sublibrary.data.imdb.model.ImdbId;
import org.lodder.subtools.sublibrary.model.VideoType;
import org.lodder.subtools.sublibrary.settings.model.SerieMapping;
import org.lodder.subtools.sublibrary.userinteraction.UserInteractionHandler;
import org.lodder.subtools.sublibrary.util.lazy.LazyFunction;
import org.lodder.subtools.sublibrary.util.throwingfunction.ThrowingTriFunction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
public class ImdbAdapter implements AdapterIntf {

    private static final Logger LOGGER = LoggerFactory.getLogger(ImdbAdapter.class);
    private static final LazyFunction<UserInteractionHandler, ImdbAdapter> INSTANCE =
        new LazyFunction<>(ImdbAdapter::new);
    @val @override String provider = "IMDB";
    private final UserInteractionHandler userInteractionHandler;
    private final ImdbApi imdbApi;
    private final ImdbSearchIdApi imdbSearchIdApi;

    private ImdbAdapter(UserInteractionHandler userInteractionHandler) {
        this.userInteractionHandler = userInteractionHandler;
        this.imdbApi = new ImdbApi();
        this.imdbSearchIdApi = new ImdbSearchIdApi();
    }

    public @Nullable ImdbDetails getDetails(String imdbId) {
        return getCache("details", b -> b.add("imdbId", imdbId))
            .get(() -> {
                try {
                    return imdbApi.getDetails(imdbId);
                } catch (ImdbException e) {
                    LOGGER.error("$provider: error while fetching details for id [$imdbId]", e);
                    return null;
                }
            });
    }

    public @Nullable String getImdbId(String title, VideoType videoType, @Nullable Integer year=null) {
        try {
            SerieMapping imdbSerieMapping = getCache(videoType.name() + "mapping",
                b -> b.add("title", title).add("videoType", videoType).add("year", year))
                .get(() -> {
                    ImdbId imdbId = ifNullThenGet(getImdbIdOnImdb(title, year, videoType),
                        () -> ifNullThenGet(getImdbIdOnGoogle(title, year, videoType),
                            () -> ifNullThenGet(getImdbIdOnYahoo(title, year, videoType),
                                () -> promptUserToEnterImdbId(title).map(id -> getImdbIdOnImdb(title, id))
                                    .orElse(null))));
                    return ifNotNull(imdbId, id -> new SerieMapping(title, id.id, imdbId.name));
                }, storeTempNullValue:true);
            return ifNotNull(imdbSerieMapping, SerieMapping::getProviderId);
        } catch (Exception e) {
            LOGGER.error("API %s getImdbId for title [%s] (%s)".formatted(provider, title, e.getMessage()), e);
            return null;
        }
    }

    private @Nullable ImdbId getImdbIdOnImdb(String title, @Nullable Integer year, VideoType videoType) {
        return getImdbIdCommon(title, year, videoType, imdbSearchIdApi::getImdbIdOnImdb);
    }

    private @Nullable ImdbId getImdbIdOnImdb(String title, String imdbId) {
        try {
            return imdbSearchIdApi.getImdbIdOnImdb(title, imdbId);
        } catch (ImdbSearchIdException e) {
            LOGGER.error("API %s getImdbId for title [%s] and provided imdbId [imdbId] (%s)".formatted(provider, title,
                e.getMessage()), e);
            return null;
        }
    }


    private @Nullable ImdbId getImdbIdOnGoogle(String title, @Nullable Integer year, VideoType videoType) {
        return getImdbIdCommon(title, year, videoType, imdbSearchIdApi::getImdbIdOnGoogle);
    }

    private @Nullable ImdbId getImdbIdOnYahoo(String title, @Nullable Integer year, VideoType videoType) {
        return getImdbIdCommon(title, year, videoType, imdbSearchIdApi::getImdbIdOnYahoo);
    }

    private @Nullable ImdbId getImdbIdCommon(String title, @Nullable Integer year, VideoType videoType,
        ThrowingTriFunction<String, @Nullable Integer, VideoType, Collection<ImdbId>, ImdbSearchIdException> providerSerieIdSupplier) {
        Collection<ImdbId> providerIds;
        try {
            providerIds = providerSerieIdSupplier.apply(title, year, videoType);
        } catch (ImdbSearchIdException e) {
            LOGGER.error(
                "API %s getImdbId for title [%s] and year [%s] (%s)".formatted(provider, title, year, e.getMessage()),
                e);
            return null;
        }
        if (!userInteractionHandler.settings.optionsConfirmProviderMapping && providerIds.size() == 1) {
            // found single exact match
            return providerIds.iterator().next();
        }
        Pattern yearPattern = Pattern.compile("(?<year>[1-2]\\d{3})");
        return userInteractionHandler.selectFromList(providerIds.stream().sorted(
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
                                return Math.abs((year == null ? LocalDate.now().getYear() : year) - lastYear);
                            }
                        }
                        return 0;
                    })).toList(), getText("Prompter.SelectImdbMatchForSerie", title), provider,
            providerId -> providerId.name + ifNotNullOrElse(StringUtils.trimToNull(
                Stream.of(providerId.year, providerId.otherInfo).mapFilterNonNull(StringUtils::trimToNull).collect(
                    Collectors.joining(" - "))), v -> "(" + v + ")", "")).orElse(null);
    }

    private Optional<String> promptUserToEnterImdbId(String title) {
        return userInteractionHandler.enter(provider, getText("Prompter.EnterImdbIdForSerie", title));
    }

    public static synchronized ImdbAdapter getInstance(UserInteractionHandler userInteractionHandler) {
        return INSTANCE.apply(userInteractionHandler);
    }
}
