package org.lodder.subtools.sublibrary.data.imdb;

import static org.lodder.subtools.multisubdownloader.Messages.*;

import java.util.Collection;
import java.util.Comparator;
import java.util.Optional;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import name.falgout.jeffrey.throwing.ThrowingBiFunction;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.data.AdapterIntf;
import org.lodder.subtools.sublibrary.data.ProviderId;
import org.lodder.subtools.sublibrary.data.imdb.exception.ImdbException;
import org.lodder.subtools.sublibrary.data.imdb.exception.ImdbSearchIdException;
import org.lodder.subtools.sublibrary.data.imdb.model.ImdbDetails;
import org.lodder.subtools.sublibrary.userinteraction.UserInteractionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    public Optional<String> getImdbId(String title, @Nullable Integer year=null) {
        try {
            return getCache("imdbId", b -> b.add("title", title).add("year", year))
                .getOptional(
                    () -> getImdbIdOnImdb(title, year)
                        .orElseMap(() -> getImdbIdOnGoogle(title, year))
                        .orElseMap(() -> getImdbIdOnYahoo(title, year))
                        .orElseMap(() -> promptUserToEnterImdbId(title)),
                    storeTempNullValue:true);
        } catch (Exception e) {
            LOGGER.error("API %s getImdbId for title [%s] (%s)".formatted(provider, title, e.getMessage()), e);
            return Optional.empty();
        }
    }

    private Optional<String> getImdbIdOnImdb(String title, @Nullable Integer year) {
        return getImdbIdCommon(title, year, imdbSearchIdApi::getImdbIdOnImdb);
    }

    private Optional<String> getImdbIdOnGoogle(String title, @Nullable Integer year) {
        return getImdbIdCommon(title, year, imdbSearchIdApi::getImdbIdOnGoogle);
    }

    private Optional<String> getImdbIdOnYahoo(String title, @Nullable Integer year) {
        return getImdbIdCommon(title, year, imdbSearchIdApi::getImdbIdOnYahoo);
    }

    private Optional<String> getImdbIdCommon(String title, @Nullable Integer year,
        ThrowingBiFunction<String, Integer, Collection<ProviderId>, ImdbSearchIdException> providerSerieIdSupplier) {
        Collection<ProviderId> providerIds;
        try {
            providerIds = providerSerieIdSupplier.apply(title, year);
        } catch (ImdbSearchIdException e) {
            LOGGER.error("API %s getImdbId for title [%s] and year [%s] (%s)".formatted(provider, title, year,
                e.getMessage()), e);
            return Optional.empty();
        }
        if (!userInteractionHandler.settings.optionsConfirmProviderMapping && providerIds.size() == 1) {
            // found single exact match
            return Optional.of(providerIds.iterator().next().id);
        }
        String formattedTitle = title.replaceAll("[^A-Za-z]", "");
        return userInteractionHandler
            .selectFromList(
                providerIds.stream().sorted(Comparator
                        .comparing((ProviderId providerId) -> providerId.name.replaceAll(
                                "[^A-Za-z]", "")
                            .equalsIgnoreCase(formattedTitle), Comparator.reverseOrder())
                        .thenComparing(ProviderId::getName))
                    .toList(),
                getText("Prompter.SelectImdbMatchForSerie", title),
                provider,
                ProviderId::getName)
            .map(providerSerieId -> providerSerieId.id);
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
