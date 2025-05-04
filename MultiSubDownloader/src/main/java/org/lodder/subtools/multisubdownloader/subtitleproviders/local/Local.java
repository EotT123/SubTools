package org.lodder.subtools.multisubdownloader.subtitleproviders.local;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import lombok.experimental.ExtensionMethod;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.apache.commons.lang3.NotImplementedException;
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
import org.lodder.subtools.sublibrary.exception.ReleaseParseException;
import org.lodder.subtools.sublibrary.model.MovieRelease;
import org.lodder.subtools.sublibrary.model.ProviderIdType;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.SubtitleMatchType;
import org.lodder.subtools.sublibrary.model.SubtitleSource;
import org.lodder.subtools.sublibrary.model.TvRelease;
import org.lodder.subtools.sublibrary.model.VideoType;
import org.lodder.subtools.sublibrary.settings.model.SerieMapping;
import org.lodder.subtools.sublibrary.userinteraction.UserInteractionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtensionMethod({ Files.class })
public class Local implements SubtitleProvider<LocalSubtitle> {

    private static final Logger LOGGER = LoggerFactory.getLogger(Local.class);

    private final Settings settings;
    private final UserInteractionHandler userInteractionHandler;
    @val @override Manager manager;
    @val @override SubtitleSource source = SubtitleSource.LOCAL;

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
        ReleaseParser vfp = new ReleaseParser();

        String name = !tvRelease.originalName.isEmpty() ? tvRelease.originalName : tvRelease.name;
        String filter = name.replaceAll("[^A-Za-z]", "").trim();

        for (Path fileSub : getPossibleSubtitles(filter)) {
            try {
                Release release = vfp.parse(fileSub);
                if ((release.videoType == VideoType.EPISODE)
                    && (((TvRelease) release).season == tvRelease.season &&
                    new HashSet<>(((TvRelease) release).episodes).containsAll(tvRelease.episodes))) {

                    TvReleaseControl epCtrl =
                        new TvReleaseControl((TvRelease) release, settings, manager, userInteractionHandler);
                    epCtrl.process();
                    if (release.hasSameId(tvRelease, ProviderIdType.TVDB)) {
                        Language detectedLang = DetectLanguage.execute(fileSub);
                        if (detectedLang == language) {
                            LOGGER.debug("Local Sub found, adding [{}]", fileSub);
                            listFoundSubtitles.add(new LocalSubtitle(
                                path:fileSub,
                                subtitleSource:source,
                                fileName:fileSub.fileNameAsString,
                                language:language,
                                quality:ReleaseParser.getQualityKeyword(fileSub.fileNameAsString),
                                subtitleMatchType:SubtitleMatchType.EVERYTHING,
                                releaseGroup:ReleaseParser.extractReleaseGroup(fileSub.fileNameAsString, true),
                                uploader:fileSub.toAbsolutePath().toString(),
                                hearingImpaired:false));
                        }
                    }
                }
            } catch (ReleaseParseException | ReleaseControlException e) {
                if (LOGGER.isDebugEnabled() || LOGGER.isTraceEnabled()) {
                    LOGGER.error(e.getMessage(), e);
                } else {
                    LOGGER.error(e.getMessage());
                }
            }
        }

        return listFoundSubtitles;
    }

    @Override
    public Set<LocalSubtitle> searchSubtitles(MovieRelease movieRelease, Language language) {
        Set<LocalSubtitle> listFoundSubtitles = new HashSet<>();
        ReleaseParser releaseParser = new ReleaseParser();

        String filter = movieRelease.name;

        for (Path fileSub : getPossibleSubtitles(filter)) {
            try {
                switch (releaseParser.parse(fileSub)) {
                    case MovieRelease release -> {
                        MovieReleaseControl movieCtrl =
                            new MovieReleaseControl(release, settings, manager, userInteractionHandler);
                        movieCtrl.process();
                        if (release.hasSameId(movieRelease, ProviderIdType.IMDB)
                            && DetectLanguage.execute(fileSub) == language) {
                            LOGGER.debug("Local Sub found, adding {}", fileSub);
                            listFoundSubtitles.add(new LocalSubtitle(
                                path:fileSub,
                                subtitleSource:source,
                                fileName:fileSub.fileNameAsString,
                                language:language,// TODO previously: language(""). This was not correct?
                                quality:ReleaseParser.getQualityKeyword(fileSub.fileNameAsString),
                                subtitleMatchType:SubtitleMatchType.EVERYTHING,
                                releaseGroup:ReleaseParser.extractReleaseGroup(fileSub.fileNameAsString, true),
                                uploader:fileSub.toAbsolutePath().toString(),
                                hearingImpaired:false));
                        }
                    }
                    default -> {
                    }
                }
            } catch (ReleaseParseException | ReleaseControlException e) {
                if (LOGGER.isDebugEnabled() || LOGGER.isTraceEnabled()) {
                    LOGGER.error(e.getMessage(), e);
                } else {
                    LOGGER.error(e.getMessage());
                }
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
