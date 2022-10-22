package org.lodder.subtools.sublibrary.data.tvdb;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

import javax.swing.JOptionPane;

import org.lodder.subtools.multisubdownloader.Messages;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.Manager.ValueBuilderIsPresentIntf;
import org.lodder.subtools.sublibrary.cache.CacheType;
import org.lodder.subtools.sublibrary.data.tvdb.exception.TheTvdbException;
import org.lodder.subtools.sublibrary.data.tvdb.model.TheTvdbEpisode;
import org.lodder.subtools.sublibrary.data.tvdb.model.TheTvdbSerie;
import org.lodder.subtools.sublibrary.model.TvRelease;
import org.lodder.subtools.sublibrary.settings.model.SerieMapping;
import org.lodder.subtools.sublibrary.userinteraction.UserInteractionHandler;
import org.lodder.subtools.sublibrary.util.OptionalExtension;
import org.lodder.subtools.sublibrary.util.lazy.LazySupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.experimental.ExtensionMethod;

@Getter(value = AccessLevel.PROTECTED)
@ExtensionMethod({ OptionalExtension.class })
public class TheTvdbAdapter {

    private static final Logger LOGGER = LoggerFactory.getLogger(TheTvdbAdapter.class);
    private static TheTvdbAdapter instance;
    private final Manager manager;
    private final UserInteractionHandler userInteractionHandler;
    private final LazySupplier<TheTvdbApi> jtvapi;

    private TheTvdbAdapter(Manager manager, UserInteractionHandler userInteractionHandler) {
        this.manager = manager;
        this.userInteractionHandler = userInteractionHandler;
        this.jtvapi = new LazySupplier<>(() -> {
            try {
                return new TheTvdbApi("A1720D2DDFDCE82D");
            } catch (Exception e) {
                LOGGER.error("API TVDB INIT (%s)".formatted(e.getMessage()), e);
            }
            return null;
        });
    }

    private TheTvdbApi getApi() {
        return jtvapi.get();
    }

    // public Optional<TheTvdbSerie> getSerie(String serieName) {
    // String encodedSerieName = URLEncoder.encode(serieName.toLowerCase().replace(" ", "-"), StandardCharsets.UTF_8);
    // return manager.valueBuilder()
    // .cacheType(CacheType.DISK)
    // .key("TVDB-serieId-%s-%s".formatted(encodedSerieName, null))
    // .optionalSupplier(() -> {
    // try {
    // List<TheTvdbSerie> serieIds = getApi().getSeries(encodedSerieName, null);
    // if (serieIds.isEmpty()) {
    // return Optional.empty();
    // } else if (!userInteractionHandler.getSettings().isOptionsConfirmProviderMapping() && serieIds.size() == 1) {
    // return Optional.of(serieIds.get(0));
    // } else {
    // String formattedSerieName = serieName.replaceAll("[^A-Za-z]", "");
    // Comparator<TheTvdbSerie> comparator = Comparator
    // .comparing((TheTvdbSerie s) -> formattedSerieName.equalsIgnoreCase(s.getSerieName().replaceAll("[^A-Za-z]", "")),
    // Comparator.reverseOrder())
    // .thenComparing(TheTvdbSerie::getFirstAired, Comparator.reverseOrder());
    // return userInteractionHandler
    // .selectFromList(serieIds.stream().sorted(comparator).toList(),
    // Messages.getString("Prompter.SelectTvdbMatchForSerie").formatted(serieName),
    // "tvdb", s -> "%s (%s)".formatted(s.getSerieName(), s.getFirstAired()))
    // .orElseMap(() -> askUserToEnterTvdbId(serieName).mapToOptionalObj(id -> getApi().getSerie(id, null)));
    // }
    // } catch (Exception e) {
    // LOGGER.error("API TVDB getSerieId for serie [%s] (%s)".formatted(serieName, e.getMessage()), e);
    // return Optional.empty();
    // }
    // })
    // .storeTempValue()
    // .getOptional();
    // }

    public Optional<TheTvdbSerie> getSerie(String serieName) {
        String encodedSerieName = URLEncoder.encode(serieName.toLowerCase().replace(" ", "-"), StandardCharsets.UTF_8);
        ValueBuilderIsPresentIntf valueBuilder = manager.valueBuilder()
                .cacheType(CacheType.DISK)
                .key("TVDB-tvdbSerie-%s-%s".formatted(encodedSerieName, null));
        if (valueBuilder.isPresent()) {
            return valueBuilder.returnType(TheTvdbSerie.class).getOptional();
        }

        Optional<TheTvdbSerie> tvdbSerie;
        List<TheTvdbSerie> serieIds;
        try {
            serieIds = getApi().getSeries(encodedSerieName, null);
        } catch (TheTvdbException e) {
            serieIds = List.of();
        }
        if (serieIds.isEmpty()) {
            tvdbSerie = Optional.empty();
        } else if (!userInteractionHandler.getSettings().isOptionsConfirmProviderMapping() && serieIds.size() == 1) {
            tvdbSerie = Optional.of(serieIds.get(0));
        } else {
            String formattedSerieName = serieName.replaceAll("[^A-Za-z]", "");
            Comparator<TheTvdbSerie> comparator = Comparator
                    .comparing((TheTvdbSerie s) -> formattedSerieName.equalsIgnoreCase(s.getSerieName().replaceAll("[^A-Za-z]", "")),
                            Comparator.reverseOrder())
                    .thenComparing(TheTvdbSerie::getFirstAired, Comparator.reverseOrder());
            try {
                tvdbSerie = userInteractionHandler
                        .selectFromList(serieIds.stream().sorted(comparator).toList(),
                                Messages.getString("Prompter.SelectTvdbMatchForSerie").formatted(serieName),
                                "tvdb", s -> "%s (%s)".formatted(s.getSerieName(), s.getFirstAired()))
                        .orElseMap(() -> askUserToEnterTvdbId(serieName).mapToOptionalObj(id -> getApi().getSerie(id, null)));
            } catch (TheTvdbException e) {
                tvdbSerie = Optional.empty();
            }
        }
        valueBuilder.optionalValue(tvdbSerie).storeTempValue().store();
        manager.valueBuilder()
                .cacheType(CacheType.DISK)
                .key("TVDB-serieId-%s-%s".formatted(encodedSerieName, null))
                .optionalValue(tvdbSerie.mapToObj(tvdbS -> new SerieMapping(serieName, tvdbS.getId(), tvdbS.getSerieName())))
                .storeTempValue()
                .store();
        return tvdbSerie;
    }

    // @Getter
    // public static class TvdbSerieId extends ProviderSerieId implements Serializable {
    //
    // private static final long serialVersionUID = 1L;
    // private final String firstAired;
    //
    // public TvdbSerieId(String name, int id, String firstAired) {
    // super(name, String.valueOf(id));
    // this.firstAired = firstAired;
    // }
    // }

    public Optional<TheTvdbEpisode> getEpisode(int tvdbId, int season, int episode) {
        return manager.valueBuilder()
                .cacheType(CacheType.DISK)
                .key("TVDB-episode-%s-%s-%s".formatted(tvdbId, season, episode))
                .optionalSupplier(() -> {
                    try {
                        return getApi().getEpisode(tvdbId, season, episode, Language.ENGLISH);
                    } catch (TheTvdbException e) {
                        LOGGER.error("API TVDB getEpisode for serie id [%s] %s (%s)".formatted(tvdbId,
                                TvRelease.formatSeasonEpisode(season, episode), e.getMessage()), e);
                        return Optional.empty();
                    }
                }).storeTempValue().getOptional();

    }

    public synchronized static TheTvdbAdapter getInstance(Manager manager, UserInteractionHandler userInteractionHandler) {
        if (instance == null) {
            instance = new TheTvdbAdapter(manager, userInteractionHandler);
        }
        return instance;
    }

    private OptionalInt askUserToEnterTvdbId(String showName) {
        LOGGER.error("Unknown serie name in tvdb: " + showName);
        String tvdbidString = JOptionPane.showInputDialog(null, "Enter tvdb id for serie " + showName);
        if (tvdbidString == null) {
            return OptionalInt.empty();
        }
        try {
            return OptionalInt.of(Integer.parseInt(tvdbidString));
        } catch (NumberFormatException e) {
            LOGGER.error("Invalid tvdb id: " + tvdbidString);
            return askUserToEnterTvdbId(showName);
        }
    }
}
