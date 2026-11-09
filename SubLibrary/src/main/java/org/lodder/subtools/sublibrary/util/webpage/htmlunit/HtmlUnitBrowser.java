package org.lodder.subtools.sublibrary.util.webpage.htmlunit;

import java.io.IOException;
import java.io.Serial;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.logging.Level;
import java.util.stream.Collectors;

import org.htmlunit.AbstractPage;
import org.htmlunit.AjaxController;
import org.htmlunit.BrowserVersion;
import org.htmlunit.FailingHttpStatusCodeException;
import org.htmlunit.HttpMethod;
import org.htmlunit.Page;
import org.htmlunit.SgmlPage;
import org.htmlunit.UnexpectedPage;
import org.htmlunit.WebClient;
import org.htmlunit.WebRequest;
import org.htmlunit.html.HtmlPage;
import org.htmlunit.util.Cookie;
import org.htmlunit.util.NameValuePair;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.util.webpage.CloudFlare;
import org.lodder.subtools.sublibrary.util.webpage.PageIntf;
import org.lodder.subtools.sublibrary.util.webpage.exception.CloudflareException;
import org.lodder.subtools.sublibrary.util.webpage.exception.WebpageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
public class HtmlUnitBrowser {
    private static final Logger LOG = LoggerFactory.getLogger(HtmlUnitBrowser.class);

    private HtmlUnitBrowser() {
        // Hide Utility Class Constructor
    }

    public static Document download(HtmlUnitSearchParams searchParams) throws WebpageException {
        try {
            java.util.logging.Logger.getLogger("com.gargoylesoftware").setLevel(Level.OFF);
            LOG.debug("Downloading page using HtmlUnit: " + searchParams.url);
            WebRequest wr = new WebRequest(new URI(searchParams.url).toURL(), HttpMethod.GET);
            if (searchParams.proxy != null) {
                wr.setProxyHost(searchParams.proxy.host);
                wr.setProxyPort(searchParams.proxy.port);
                wr.setSocksProxy(searchParams.proxy.socks);
            }
            String pageContents = getPageContents(wr, searchParams.cookies, searchParams.waitUntilCondition);
            if (pageContents == null) {
                throw new WebpageException("Could not access url: " + searchParams.url);
            }
            if (CloudFlare.isProtected(pageContents)) {
                // DDOS protection
                try {
                    Thread.sleep(9000);
                } catch (InterruptedException e) {
                    // continue
                }
                pageContents = getPageContents(wr, searchParams.cookies, searchParams.waitUntilCondition);
                if (pageContents == null) {
                    throw new WebpageException("Could not access url: " + searchParams.url);
                }
                if (CloudFlare.isProtected(pageContents)) {
                    LOG.warn("Could not access Cloudflare protected site: " + searchParams.url);
                    throw new CloudflareException(searchParams.url);
                }
            }
            return Jsoup.parse(pageContents);
        } catch (IOException | URISyntaxException e) {
            throw new WebpageException(e);
        }
    }

    private static @Nullable String getPageContents(WebRequest wr, List<Cookie> cookies,
        @Nullable Predicate<PageIntf> waitUntilCondition) throws WebpageException {
        try (WebClient webClient = new WebClient(BrowserVersion.CHROME)) {
            cookies.forEach(webClient.cookieManager::addCookie);
            setWebClientProps(webClient);
            Page page = webClient.getPage(wr);
            if (waitUntilCondition != null) {
                LOG.debug("Wait until page is loaded");
                HtmlUnitPage htmlUnitPage = new HtmlUnitPage((HtmlPage) page);
                int i = 5;
                while (i > 0 && !waitUntilCondition.test(htmlUnitPage)) {
                    LOG.debug("Waiting... " + i + " (" + wr.url + ")");
                    webClient.waitForBackgroundJavaScript(2000);
                    i--;
                }
                if (!waitUntilCondition.test(htmlUnitPage)) {
                    return null;
                }
            }
            if (page instanceof UnexpectedPage) {
                LOG.debug("UNEXPECTED PAGE: %s", page.webResponse.statusMessage);
                throw new WebpageException("Could not access url: " + page.url);
            }
            return switch (page) {
                case SgmlPage sgmlPage -> sgmlPage.asXml();
                case AbstractPage abstractPage -> abstractPage.webResponse.contentAsString;
                default -> throw new IllegalStateException("Unexpected value: " + page);
            };
        } catch (FailingHttpStatusCodeException | IOException e) {
            throw new WebpageException(e);
        }
    }

    public static void setWebClientProps(WebClient webClient) {
        // JavaScript to pass Cloudflare's security challenge
        webClient.options.setJavaScriptEnabled(true);
        webClient.setJavaScriptTimeout(30000);

        webClient.options.setTimeout(30000);
        webClient.options.setCssEnabled(false);
        webClient.options.setRedirectEnabled(true);
        webClient.options.setThrowExceptionOnFailingStatusCode(false);
        webClient.options.setThrowExceptionOnScriptError(false);
        webClient.options.setPrintContentOnFailingStatusCode(false);
        webClient.options.setPopupBlockerEnabled(false);
        // webClient.options.setJavaScriptEnabled(false);
        webClient.options.setUseInsecureSSL(true);
        webClient.waitForBackgroundJavaScript(30000);
        webClient.waitForBackgroundJavaScriptStartingBefore(30000);
        webClient.setAjaxController(new AjaxController() {
            @Serial private static final long serialVersionUID = 1L;

            @Override
            public boolean processSynchron(HtmlPage page, WebRequest request, boolean async) {
                return true;
            }
        });
    }

    // public static String post(String urlString, Pair<String, String>... params) throws
    // FailingHttpStatusCodeException, IOException {
    // return post(urlString, new ArrayList<>(), params);
    // }
    //
    // @SafeVarargs
    // public static String post(String urlString, Cookie cookie, Pair<String, String>... params) throws
    // FailingHttpStatusCodeException, IOException
    // {
    // return post(urlString, List.of(cookie), params);
    // }

    public static String post(String urlString, List<Cookie> cookies, NameValuePair... params) throws WebpageException {
        try {
            WebRequest wr = new WebRequest(new URI(urlString).toURL(), HttpMethod.POST);
            // wr.additionalHeaders.put("Accept", "*/*");
            wr.additionalHeaders.put("Accept", "application/json, text/javascript, */*; q=0.01");
            wr.additionalHeaders.put("Accept-Encoding", "gzip, deflate, br");
            wr.additionalHeaders.put("Accept-Language", "en-US,en;q=0.9");
            wr.additionalHeaders.put("Connection", "keep-alive");
            wr.additionalHeaders.put("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
            String cookieString =
                cookies.stream().map(cookie -> cookie.name + "=" + cookie.value).collect(Collectors.joining("; "));
            wr.additionalHeaders.put("Cookie", cookieString);
            // wr.additionalHeaders.put("User-Agent",
            // "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:44.0) Gecko/20100101 Firefox/44.0");
            wr.additionalHeaders.put("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:84.0) Gecko/20100101 Firefox/84.0");
            wr.additionalHeaders.put("X-Requested-With", "XMLHttpRequest");
            wr.setRequestParameters(Arrays.asList(params));
            Page page;
            try (WebClient webClient = new WebClient(BrowserVersion.CHROME)) {
                webClient.options.setUseInsecureSSL(true);
                page = webClient.getPage(wr);
            }
            return page.webResponse.contentAsString;
        } catch (FailingHttpStatusCodeException | IOException | URISyntaxException e) {
            throw new WebpageException(e);
        }
    }
}
