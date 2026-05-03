package org.lodder.subtools.sublibrary.util.webpage;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import org.htmlunit.util.Cookie;
import org.htmlunit.util.NameValuePair;
import org.jsoup.nodes.Document;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.util.webpage.exception.WebpageException;
import org.lodder.subtools.sublibrary.util.webpage.htmlunit.HtmlUnitBrowser;
import org.lodder.subtools.sublibrary.util.webpage.htmlunit.HtmlUnitSearchParams;
import org.lodder.subtools.sublibrary.util.webpage.java.JavaBrowser;
import org.lodder.subtools.sublibrary.util.webpage.webdriver.ChromeBrowser;
import org.lodder.subtools.sublibrary.util.webpage.webdriver.WebDriverSearchParams;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Class to retrieve a webpage as a {@link Document}
 */
@SuppressWarnings("unused")
@NullMarked
public class WebPage {

    private static final Logger LOG = LoggerFactory.getLogger(WebPage.class);

    private WebPage() {
        // Hide Utility Class Constructor
    }

    public static Document getWebsiteDomTree(
        String url,
        List<Cookie> cookies=(List) List.of(),
        @Nullable Proxy proxy=null,
        @Nullable Predicate<PageIntf> waitUntilCondition=null,
        BrowserMode browserMode=BrowserMode.HTMLUNIT,
        boolean manualCloudflareBypass=true) throws WebpageException {

        List<BrowserMode> browserModes = browserMode == BrowserMode.WEBDRIVER ? List.of(BrowserMode.WEBDRIVER) :
            List.of(BrowserMode.HTMLUNIT, BrowserMode.WEBDRIVER);

        for (BrowserMode mode : browserModes) {
            try {
                return switch (mode) {
                    case WEBDRIVER -> ChromeBrowser.download(
                        new WebDriverSearchParams(url, cookies, waitUntilCondition, proxy, manualCloudflareBypass));
                    case HTMLUNIT -> HtmlUnitBrowser.download(
                        new HtmlUnitSearchParams(url, cookies, waitUntilCondition, proxy));
                };
            } catch (WebpageException e) {
                LOG.error("Could not access webpage %s using browserMode %s", url, mode);
            }
        }
        throw new WebpageException("Could not access webpage " + url);
    }

    private static String getDomain(String url) {
        String urlWithoutHttp = url.replace("http://", "").replace("https://", "").replace("www.", "");
        return urlWithoutHttp.contains("/") ? urlWithoutHttp.split("/", 2)[0] : urlWithoutHttp;
    }

    /**
     * Retrieve a webpage as a {@link Document}
     *
     * @param url the url of the webpage
     * @return the webpage as a {@link String}
     * @throws WebpageException WebpageException
     */
    public static String getWebsiteHtml(String url) throws WebpageException {
        return JavaBrowser.getWebsiteHtml(url);
    }

    public static String post(String urlString, NameValuePair... params) throws WebpageException {
        return post(urlString, new ArrayList<>(), params);
    }

    public static String post(String urlString, Cookie cookie, NameValuePair... params) throws WebpageException {
        return post(urlString, List.of(cookie), params);
    }

    public static String post(String url, List<Cookie> cookies, NameValuePair... params) throws WebpageException {
        return HtmlUnitBrowser.post(url, cookies, params);
    }

    public static void openUrlInBrowser(String url) {
        if (java.awt.Desktop.isDesktopSupported()) {
            java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
            try {
                desktop.browse(new URI(url));
            } catch (IOException | URISyntaxException e) {
                try {
                    desktop.browse(new URI("http://www.google.com/search?q=" + url));
                } catch (IOException | URISyntaxException e2) {
                    e.printStackTrace();
                }
            }
        }
    }
}
