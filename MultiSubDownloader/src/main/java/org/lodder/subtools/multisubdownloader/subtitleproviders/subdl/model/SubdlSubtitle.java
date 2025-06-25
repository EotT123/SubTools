package org.lodder.subtools.multisubdownloader.subtitleproviders.subdl.model;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.control.ReleaseParser;
import org.lodder.subtools.sublibrary.exception.ReleaseParseException;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.SubtitleSource;
import org.lodder.subtools.sublibrary.model.TvRelease;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SubdlSubtitle extends Subtitle {
    private static final Logger LOGGER = LoggerFactory.getLogger(SubdlSubtitle.class);

    private final String url;
    private final Release forRelease;

    public SubdlSubtitle(String url,
        @Nullable String title=null,
        @Nullable Language language=null,
        @Nullable String releaseGroup=null,
        @Nullable String uploader=null,
        boolean hearingImpaired=false,
        @Nullable String quality=null,
        Release forRelease) {

        super(title, language, releaseGroup, uploader, SubtitleSource.SUBDL, hearingImpaired, quality);
        this.url = url;
        this.forRelease = forRelease;
    }

    @Override
    public List<Path> download(Manager manager, Path destinationFolder,
        Function<AtomicInteger, String> fileNameFunction) throws IOException {
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir")).resolve("multisubdownloader").resolve("subdl");
        Files.createDirectories(tempDir);
        String zipFileName = url.contains("/") ? StringUtils.substringAfterLast(url, "/").removeIllegalWindowsChars() :
            url.removeIllegalWindowsChars();
//        Path zipPath = tempDir.resolve(zipFileName);
        Path unzipPath = tempDir.resolve(StringUtils.substringBeforeLast(zipFileName, "."));

        if (!Files.exists(unzipPath)) {
            if (!manager.download(url, unzipPath)) {
                return List.of();
            }
        }

        // unzip the downloaded file
        // unzip(zipPath, unzipPath);

        // find all extracted subtitle files and move them to the destination folder, renaming them using the
        // provided function
        boolean multipleDirectories = Files.list(unzipPath).filter(Files::isDirectory).count() > 1;
        try (Stream<Path> stream = Files.walk(unzipPath)) {
            List<Path> subtitlesToCopy = stream
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().toLowerCase().endsWith(".srt"))
                .map(path -> {
                    if (multipleDirectories && forRelease instanceof TvRelease tvRelease) {
                        try {
                            Boolean matchingSub =
                                ReleaseParser.parse(path).filter(TvRelease.class::isInstance).map(TvRelease.class::cast)
                                    .map(release -> release.season == tvRelease.season &&
                                        release.episodes.stream().anyMatch(tvRelease.episodes::contains))
                                    .orElse(false);
                            if (!matchingSub) {
                                return null;
                            }
                        } catch (ReleaseParseException e) {
                            LOGGER.warn("Could not parse subtitle file $path: " + e.getMessage());
                        }
                    }
                    return path;
                })
                .filter(Objects::nonNull)
                .toList();

            AtomicInteger counter = subtitlesToCopy.size() > 1 ? new AtomicInteger(0) : null;
            return subtitlesToCopy.stream().map(path -> {
                    Path destination = destinationFolder.resolve(fileNameFunction.apply(counter));
                    while (Files.exists(destination)) {
                        Path destinationNew = destinationFolder.resolve(fileNameFunction.apply(counter));
                        if (destinationNew.equals(destination)) {
                            LOGGER.warn("Could not copy subtitle $path to $destinationNew, because it already exists");
                            return null;
                        }
                    }
                    try {
                        return Files.copy(path, destination, StandardCopyOption.REPLACE_EXISTING);
                    } catch (IOException e) {
                        LOGGER.error("Could not copy subtitle file $path to $destination: " + e.getMessage());
                        return null;
                    }
                }).filter(Objects::nonNull)
                .toList();
        }
    }

    private record SubToCopy(Path origin, Path destination) {
    }

    ;

    private static void unzip(Path zipPath, Path outputDir) throws IOException {
        try (ZipInputStream zis = new ZipInputStream(Files.newInputStream(zipPath))) {
            ZipEntry entry = zis.getNextEntry();
            while (entry != null) {
                Path outPath = outputDir.resolve(entry.getName()).normalize();
                // Prevent Zip Slip
                if (!outPath.startsWith(outputDir.toAbsolutePath())) {
                    throw new IOException("Blocked zip entry: " + entry.getName());
                }
                if (entry.isDirectory()) {
                    Files.createDirectories(outPath);
                } else {
                    Files.createDirectories(outPath.getParent());
                    try (OutputStream out = Files.newOutputStream(outPath)) {
                        zis.transferTo(out);
                    }
                }
                zis.closeEntry();
                entry = zis.getNextEntry();
            }
        }
    }
}
