package org.lodder.subtools.multisubdownloader.subtitleproviders.tvsubtitles.model;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.SubtitleSource;

public class TvSubtiltesSubtitle extends Subtitle {

    private final String url;

    public TvSubtiltesSubtitle(String url,
        @Nullable String fileName=null,
        @Nullable Language language=null,
        @Nullable String releaseGroup=null,
        @Nullable String uploader=null,
        boolean hearingImpaired=false,
        @Nullable String quality=null) {

        super(fileName, language, releaseGroup, uploader, SubtitleSource.TVSUBTITLES, hearingImpaired, quality);
        this.url = url;
    }

    @Override
    public List<Path> download(Manager manager, Path destinationFolder,
        Function<AtomicInteger, String> fileNameFunction) throws IOException {
        Path subPath = destinationFolder.resolve(fileNameFunction.apply(null));
        manager.downloadAndExtractFile(getForwardUrl(url), subPath);
        return List.of(subPath);
    }

    private String getForwardUrl(String url) throws IOException {
        try {
            URI uri = new URI(url);
            try (InputStream inputStream = uri.toURL().openStream()) {
                String pageContent = new String(inputStream.readAllBytes());

                // Regex to extract string fragments from JS
                Pattern pattern = Pattern.compile("var\\s+(s\\d)=\\s*'([^']+)'");
                Matcher matcher = pattern.matcher(pageContent);

                StringBuilder finalPath = new StringBuilder();
                while (matcher.find()) {
                    finalPath.append(matcher.group(2));
                }

                String baseUrl = uri.resolve(".").toString();
                return baseUrl + finalPath;
            }
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }
}
