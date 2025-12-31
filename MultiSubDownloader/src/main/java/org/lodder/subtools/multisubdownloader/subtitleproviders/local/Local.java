package org.lodder.subtools.multisubdownloader.subtitleproviders.local;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.apache.commons.lang3.NotImplementedException;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.lib.control.MovieReleaseControl;
import org.lodder.subtools.multisubdownloader.lib.control.TvReleaseControl;
import org.lodder.subtools.multisubdownloader.settings.model.Settings;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleProvider;
import org.lodder.subtools.multisubdownloader.subtitleproviders.local.model.LocalSubtitle;
import org.lodder.subtools.sublibrary.DetectLanguage;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.control.ReleaseParser;
import org.lodder.subtools.sublibrary.exception.ReleaseControlException;
import org.lodder.subtools.sublibrary.model.MovieRelease;
import org.lodder.subtools.sublibrary.model.MovieReleaseWithPath;
import org.lodder.subtools.sublibrary.model.ProviderIdType;
import org.lodder.subtools.sublibrary.model.SubtitleProviderFrontEnd;
import org.lodder.subtools.sublibrary.model.TvRelease;
import org.lodder.subtools.sublibrary.model.TvReleaseWithoutPath;
import org.lodder.subtools.sublibrary.settings.model.SerieMapping;
import org.lodder.subtools.sublibrary.userinteraction.UserInteractionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
public class Local implements SubtitleProvider<LocalSubtitle> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Local.class);

    private final Settings settings;
    private final UserInteractionHandler userInteractionHandler;
    @val @override Manager manager;
    @val @override SubtitleProviderFrontEnd subtitleProviderFrontEnd = SubtitleProviderFrontEnd.LOCAL;

    public Local(Settings settings, Manager manager, UserInteractionHandler userInteractionHandler) {
        this.settings = settings;
        this.manager = manager;
        this.userInteractionHandler = userInteractionHandler;
    }

    private List<Path> getPossibleSubtitles(String filter) {
        return settings.localSourcesFolders.stream()
            .flatMap(local -> getAllSubtitlesFiles(local, filter).stream())
            .toList();
    }

    @Override
    public Set<LocalSubtitle> searchSubtitles(TvRelease tvRelease, Language language) {
        Set<LocalSubtitle> listFoundSubtitles = new HashSet<>();

        String name = !tvRelease.originalName.isEmpty() ? tvRelease.originalName : tvRelease.name;
        String filter = name.replaceAll("[^A-Za-z]", "").trim();

        TvReleaseControl tvReleaseControl = new TvReleaseControl(settings, manager, userInteractionHandler);
        for (Path fileSub : getPossibleSubtitles(filter)) {
            try {
                ReleaseParser.parse(fileSub).stream()
                    .filterCast(TvReleaseWithoutPath.class)
                    .filter(release -> release.season == tvRelease.season)
                    .filter(release -> release.episodes.containsAll(tvRelease.episodes))
                    .mapEx(tvReleaseControl::process)
                    .filter(release -> release.hasSameId(tvRelease, ProviderIdType.TVDB))
                    .filter(_ -> DetectLanguage.execute(fileSub) == language)
                    .peek(_ -> LOGGER.debug("Local Sub found, adding [{}]", fileSub))
                    .forEachEx(_ -> listFoundSubtitles.add(
                        ReleaseParser.parse(fileSub)
                            .map(r -> new LocalSubtitle(fileSub, language, r.quality, r.releaseGroup))
                            .orElseGet(() -> new LocalSubtitle(fileSub, language,
                                ReleaseParser.getQualityKeyword(fileSub.fileNameAsString),
                                ReleaseParser.extractReleaseGroup(fileSub.fileNameAsString, true)))));
            } catch (ReleaseControlException e) {
                LOGGER.error(e.getMessage(), LOGGER.isDebugEnabled() ? e : null);
            }
        }

        return listFoundSubtitles;
    }

    @Override
    public Set<LocalSubtitle> searchSubtitles(MovieRelease movieRelease, Language language) {
        Set<LocalSubtitle> listFoundSubtitles = new HashSet<>();

        String filter = movieRelease.name;
        MovieReleaseControl movieCtrl = new MovieReleaseControl(settings, manager, userInteractionHandler);

        for (Path fileSub : getPossibleSubtitles(filter)) {
            try {
                ReleaseParser.parse(fileSub).stream()
                    .filterCast(MovieReleaseWithPath.class)
                    .mapEx(movieCtrl::process)
                    .filter(release -> release.hasSameId(movieRelease, ProviderIdType.IMDB))
                    .filter(_ -> DetectLanguage.execute(fileSub) == language)
                    .peek(_ -> LOGGER.debug("Local Sub found, adding [{}]", fileSub))
                    .forEachEx(_ -> listFoundSubtitles.add(
                        ReleaseParser.parse(fileSub)
                            .map(r -> new LocalSubtitle(fileSub, language, r.quality, r.releaseGroup))
                            .orElseGet(() -> new LocalSubtitle(fileSub, language,
                                ReleaseParser.getQualityKeyword(fileSub.fileNameAsString),
                                ReleaseParser.extractReleaseGroup(fileSub.fileNameAsString, true)))));
            } catch (ReleaseControlException e) {
                LOGGER.error(e.getMessage(), LOGGER.isDebugEnabled() ? e : null);
            }
        }
        return listFoundSubtitles;
    }

    private List<Path> getAllSubtitlesFiles(Path dir, String filter) {
        try {
            return dir.list().filter(Files::isRegularFile)
                .filter(file -> file.hasExtension("srt"))
                .filter(file -> file.getFileNameAsString()
                    .replaceAll("[^A-Za-z]", "")
                    .toLowerCase()
                    .contains(filter.toLowerCase()))
                .toList();
        } catch (IOException e) {
            LOGGER.error(e.getMessage(), e);
            return List.of();
        }
    }

    @Override
    public <X extends Exception> Optional<SerieMapping> getProviderSerieMapping(TvRelease tvRelease) throws X {
        throw new NotImplementedException();
    }
}
