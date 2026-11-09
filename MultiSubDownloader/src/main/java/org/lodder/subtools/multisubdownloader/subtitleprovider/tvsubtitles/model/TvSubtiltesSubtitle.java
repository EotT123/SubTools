package org.lodder.subtools.multisubdownloader.subtitleprovider.tvsubtitles.model;

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

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.SubtitleSource;

@NullMarked
public class TvSubtiltesSubtitle extends Subtitle {

    private final String url;
    @val @override SubtitleSource source = SubtitleSource.TVSUBTITLES;

    public TvSubtiltesSubtitle(String url,
        String fileName,
        Language language,
        String releaseGroup,
        @Nullable String uploader=null,
        boolean hearingImpaired=false,
        String quality) {

        super(fileName, language, releaseGroup, uploader, hearingImpaired, quality);
        this.url = url;
    }

    @Override
    public List<Path> download(Path destinationFolder, Function<@Nullable AtomicInteger, String> fileNameFunction)
        throws IOException {
        Path subPath = destinationFolder.resolve(fileNameFunction.apply(null));
        Manager.downloadAndExtractFile(getForwardUrl(url), subPath);
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
                return baseUrl + finalPath.toString().replace(" ", "%20");
            }
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }
}
