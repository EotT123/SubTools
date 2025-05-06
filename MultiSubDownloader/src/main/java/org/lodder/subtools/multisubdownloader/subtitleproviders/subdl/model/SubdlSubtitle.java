package org.lodder.subtools.multisubdownloader.subtitleproviders.subdl.model;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.function.Supplier;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import manifold.science.measures.Time;
import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.SubtitleSource;

public class SubdlSubtitle extends Subtitle {

    private final String url;

    public SubdlSubtitle(String url,
        @Nullable String title=null,
        @Nullable Language language=null,
        @Nullable String releaseGroup=null,
        @Nullable String uploader=null,
        boolean hearingImpaired=false,
        @Nullable String quality=null) {

        super(title, language, releaseGroup, uploader, SubtitleSource.SUBDL, hearingImpaired, quality);
        this.url = url;
    }

    @Override
    public List<Path> download(Manager manager, Path destinationFolder, Supplier<String> fileNameFunction)
        throws IOException {
        Path tempDir = Paths.get(System.getProperty("java.io.tmpdir")).resolve("multisubdownloader").resolve("subdl")
            .resolve(Time.now().toString());
        Files.createDirectories(tempDir);
        String zipFileName = url.contains("/") ? StringUtils.substringAfterLast(url, "/").removeIllegalWindowsChars() :
            url.removeIllegalWindowsChars();
        Path zipPath = tempDir.resolve(zipFileName);
        Path unzipPath = tempDir.resolve(StringUtils.substringBeforeLast(zipFileName, "."));
        Files.createDirectories(unzipPath);

        if (!manager.download(url, zipPath)) {
            return List.of();
        }

        // unzip the downloaded file
        unzip(zipPath, unzipPath);

        // find all extracted subtitle files and move them to the destination folder, renaming them using the
        // provided function
        try (Stream<Path> stream = Files.walk(unzipPath)) {
            return stream
                .filter(Files::isRegularFile)
                .filter(path -> path.toString().toLowerCase().endsWith(".srt"))
                .mapIgnoreEx(path -> Files.move(path, destinationFolder.resolve(fileNameFunction.get())))
                .toList();
        }
    }

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
