package org.lodder.subtools.sublibrary.util.http;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Map;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;

import extensions.java.nio.file.Path.PathExt;
import jakarta.ws.rs.core.HttpHeaders;
import name.falgout.jeffrey.throwing.ThrowingConsumer;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.helper.HttpConnection;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
public record HttpClient(CookieManager cookieManager=new CookieManager()) {

    private static final Logger LOGGER = LoggerFactory.getLogger(HttpClient.class);

    public String doGet(URL url, @Nullable String userAgent, @Nullable CookieManager cookieManager=null)
        throws IOException, HttpClientException {
        HttpURLConnection conn = null;
        try {
            conn = (HttpURLConnection) url.openConnection();
            conn.setInstanceFollowRedirects(true);
            getCookieManager(cookieManager).setCookies(conn);
            if (StringUtils.isNotBlank(userAgent)) {
                conn.setRequestProperty(HttpHeaders.USER_AGENT, userAgent);
            }
            if (conn.responseCode == 200) {
                return conn.getInputStream().asString(StandardCharsets.UTF_8);
            }
            throw new HttpClientException(conn);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public String doPost(URL url, @Nullable String userAgent, Map<String, String> data,
        @Nullable CookieManager cookieManager=null) throws HttpClientException {
        HttpURLConnection conn = null;

        try {
            String urlParameters = data.entrySet().stream()
                .map(entry -> entry.getKey() + "=" + URLEncoder.encode(entry.getValue(), StandardCharsets.UTF_8))
                .collect(Collectors.joining("&"));

            conn = (HttpURLConnection) url.openConnection();
            getCookieManager(cookieManager).setCookies(conn);
            conn.setRequestMethod("POST");
            if (StringUtils.isNotBlank(userAgent)) {
                conn.setRequestProperty(HttpHeaders.USER_AGENT, userAgent);
            }
            conn.setRequestProperty(HttpHeaders.CONTENT_TYPE, HttpConnection.FORM_URL_ENCODED);
            conn.setRequestProperty(HttpHeaders.CONTENT_LENGTH,
                String.valueOf(urlParameters.getBytes(StandardCharsets.UTF_8).length));
            conn.setUseCaches(false);
            conn.setDoInput(true);
            conn.setDoOutput(true);
            conn.setInstanceFollowRedirects(false);

            try (DataOutputStream out = new DataOutputStream(conn.getOutputStream())) {
                out.writeBytes(urlParameters);
                out.flush();
            }

            getCookieManager(cookieManager).storeCookies(conn);

            if (conn.responseCode == 302 && isUrl(conn.getHeaderField(HttpHeaders.LOCATION))) {
                return doGet(new URI(conn.getHeaderField(HttpHeaders.LOCATION)).toURL(), userAgent, cookieManager);
            }
            return conn.getInputStream().asString(StandardCharsets.UTF_8);
        } catch (IOException | URISyntaxException e) {
            throw new HttpClientException(e, conn);
        } finally {
            if (conn != null) {
                conn.disconnect();
            }
        }
    }

    public void downloadAndExtractFile(URL url, final Path file,
        @Nullable ThrowingConsumer<String, IOException> validateFunction=null,
        @Nullable CookieManager cookieManager=null)
        throws IOException {
        LOGGER.debug("doDownloadFile: URL [{}], file [{}]", url, file);

        try (InputStream rawIn = getInputStream(url, getCookieManager(cookieManager));
             InputStream in = url.getFile().endsWith(".gz") ? new GZIPInputStream(rawIn) : rawIn;
             BufferedInputStream bufferedIn = new BufferedInputStream(in)) {
            bufferedIn.mark(10); // for checking headers
            boolean isZip = PathExt.isZipFile(bufferedIn);
            bufferedIn.reset();

            if (isZip || url.getFile().endsWith(".zip")) {
                PathExt.unzip(bufferedIn, file, ".srt");
            } else {
                // Buffer the data just once
                byte[] data = bufferedIn.readAllBytes();
                if (PathExt.isGZipCompressed(data)) {
                    data = PathExt.decompressGZip(data);
                }
                String content = new String(data, StandardCharsets.UTF_8);
                if (validateFunction != null) {
                    validateFunction.accept(content);
                }
                Files.write(file, data, StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
            }
        }
    }

    private InputStream getInputStream(URL url, @Nullable CookieManager cookieManager=null) throws IOException {
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        cookieManager.setCookies(conn);
        conn.addRequestProperty(HttpHeaders.USER_AGENT, "Mozilla");
        conn.addRequestProperty("Referer", url.toString());
        conn.setInstanceFollowRedirects(false);

        int status = conn.getResponseCode();

        cookieManager.storeCookies(conn);

        if (status != HttpURLConnection.HTTP_OK) {
            if (status == HttpURLConnection.HTTP_MOVED_TEMP || status == HttpURLConnection.HTTP_MOVED_PERM
                || status == HttpURLConnection.HTTP_SEE_OTHER) {
                String locationHeader = conn.getHeaderField(HttpHeaders.LOCATION);
                try {
                    URL newUrl;
                    if (HttpClient.isUrl(locationHeader)) {
                        newUrl = new URI(locationHeader).toURL();
                    } else {
                        newUrl = new URI("%s://%s/%s".formatted(url.protocol, conn.getURL().host,
                            locationHeader.trim().replace(" ", "%20"))).toURL();
                    }
                    return getInputStream(newUrl, cookieManager);
                } catch (URISyntaxException e) {
                    throw new IOException(e);
                }
            }
            throw new IOException("error: " + status);
        } else {
            return conn.getInputStream();
        }
    }

    public static boolean isUrl(String str) {
        Pattern urlPattern = Pattern.compile(
            "((https?|ftp|gopher|telnet|file):((//)|(\\\\\\\\))+[\\\\w\\\\d:#@%/;$()~_?\\\\+-=\\\\\\\\\\\\.&]*)",
            Pattern.CASE_INSENSITIVE);
        return urlPattern.matcher(str).find();
    }

    public String downloadText(String url) throws IOException {
        try (BufferedReader in = new BufferedReader(
            new InputStreamReader(new URI(url).toURL().openStream(), StandardCharsets.UTF_8))) {
            return in.lines().collect(Collectors.joining());
        } catch (URISyntaxException e) {
            throw new IOException(e.getMessage(), e);
        }
    }

    public void storeCookies(String domain, Map<String, String> cookieMap) {
        cookieManager.storeCookies(domain, cookieMap);
    }

    private CookieManager getCookieManager(@Nullable CookieManager cookieManager) {
        return cookieManager == null ? this.cookieManager : cookieManager;
    }
}
