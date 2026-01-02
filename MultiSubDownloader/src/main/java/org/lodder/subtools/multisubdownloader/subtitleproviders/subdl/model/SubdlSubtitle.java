package org.lodder.subtools.multisubdownloader.subtitleproviders.subdl.model;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.control.ReleaseParser;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.SubtitleSource;
import org.lodder.subtools.sublibrary.model.TvReleaseWithPath;
import org.lodder.subtools.sublibrary.model.TvReleaseWithoutPath;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
public class SubdlSubtitle extends Subtitle {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubdlSubtitle.class);

    private final String url;
    private final Release forRelease;
    @val @override SubtitleSource source = SubtitleSource.SUBDL;

    public SubdlSubtitle(String url, String title, Language language, @Nullable String releaseGroup, String uploader,
        boolean hearingImpaired, @Nullable String quality, Release forRelease) {

        super(title, language, releaseGroup, uploader, hearingImpaired, quality);
        this.url = url;
        this.forRelease = forRelease;
    }

    @Override
    public List<Path> download(Manager manager, Path destinationFolder,
        Function<@Nullable AtomicInteger, String> fileNameFunction) throws IOException {
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir")).resolve("multisubdownloader").resolve("subdl");
        Files.createDirectories(tempDir);
        String zipFileName = url.contains("/") ? StringUtils.substringAfterLast(url, "/").removeIllegalWindowsChars() :
            url.removeIllegalWindowsChars();
        Path unzipPath = tempDir.resolve(StringUtils.substringBeforeLast(zipFileName, "."));

        if (!Files.exists(unzipPath)) {
            manager.downloadAndExtractFile(url, unzipPath);
        }

        // find all extracted subtitle files and move them to the destination folder, renaming them using the
        // provided function
        try (Stream<Path> files = Files.list(unzipPath)) {
            boolean multipleDirectories = files.filter(Files::isDirectory).count() > 1;
            try (Stream<Path> stream = Files.walk(unzipPath)) {
                List<Path> subtitlesToCopy = stream
                    .filter(Files::isRegularFile)
                    .filter(path -> path.toString().toLowerCase().endsWith(".srt"))
                    .mapFilterNonNull(path -> {
                        if (multipleDirectories && forRelease instanceof TvReleaseWithoutPath tvRelease) {
                            Boolean matchingSub = ReleaseParser.parse(path).filter(TvReleaseWithPath.class::isInstance)
                                .map(TvReleaseWithPath.class::cast)
                                .map(release -> release.season == tvRelease.season &&
                                    release.episodes.stream().anyMatch(tvRelease.episodes::contains))
                                .orElse(false);
                            if (!matchingSub) {
                                return null;
                            }
                        }
                        return path;
                    })
                    .toList();

                AtomicInteger counter = subtitlesToCopy.size() > 1 ? new AtomicInteger(0) : null;
                return subtitlesToCopy.stream().mapFilterNonNull(path -> {
                        Path destination = destinationFolder.resolve(fileNameFunction.apply(counter));
                        while (Files.exists(destination)) {
                            Path destinationNew = destinationFolder.resolve(fileNameFunction.apply(counter));
                            if (destinationNew.equals(destination)) {
                                LOGGER.warn("Could not copy subtitle $path to $destinationNew, because it already " +
                                    "exists");
                                return null;
                            }
                        }
                        try {
                            return Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
                        } catch (IOException e) {
                            LOGGER.error("Could not copy subtitle file $path to $destination: " + e.getMessage());
                            return null;
                        }
                    })
                    .toList();
            }
        }
    }
}
