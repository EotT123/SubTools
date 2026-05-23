package org.lodder.subtools.sublibrary.util.webpage.java;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.util.webpage.exception.WebpageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
public class JavaBrowser {
    private static final Logger LOG  = LoggerFactory.getLogger(JavaBrowser.class);

    private JavaBrowser() {
        // Hide Utility Class Constructor
    }

    /**
     * Retrieve a webpage as a {@link Document}
     *
     * @param url the url of the webpage
     * @return the webpage as a {@link String}
     * @throws WebpageException WebpageException
     */
    public static String getWebsiteHtml(String url) throws WebpageException {
        URL urlUrl;
        try {
            urlUrl = new URI(url.endsWith("/") ? StringUtils.chop(url) : url).toURL();
        } catch (MalformedURLException | URISyntaxException e) {
            throw new WebpageException(e);
        }
        LOG.debug("Contacting " + urlUrl);
        try (InputStream is = urlUrl.openStream();
             InputStreamReader inputStreamReader = new InputStreamReader(is, StandardCharsets.UTF_8);
             BufferedReader rd = new BufferedReader(inputStreamReader)) {
            StringBuilder sb = new StringBuilder();
            int cp;
            while ((cp = rd.read()) != -1) {
                sb.append((char) cp);
            }
            return sb.toString();
        } catch (IOException e) {
            try {
                URLConnection urlc = urlUrl.openConnection();
                urlc.addRequestProperty("User-Agent",
                    "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:52.0) Gecko/20100101 Firefox/52.0");
                try (InputStream stream = urlc.inputStream) {
                    StringBuilder sb = new StringBuilder();
                    int cp;
                    while ((cp = stream.read()) != -1) {
                        sb.append((char) cp);
                    }
                    return sb.toString();
                }
            } catch (IOException e2) {
                throw new WebpageException("Could not access url: " + urlUrl);
            }
        }
    }
}
