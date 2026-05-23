package org.lodder.subtools.sublibrary.util.webpage.webdriver;

import static java.util.Objects.*;

import javax.swing.*;
import java.util.Locale;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Function;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.util.lazy.LazyObject;
import org.lodder.subtools.sublibrary.util.webpage.CloudFlare;
import org.lodder.subtools.sublibrary.util.webpage.exception.CloudflareException;
import org.lodder.subtools.sublibrary.util.webpage.exception.WebpageException;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.Proxy;
import org.openqa.selenium.chrome.ChromeOptions;
import org.slf4j.LoggerFactory;

/**
 * A utility class that facilitates web page interactions using Selenium WebDriver. Provides methods to download HTML
 * content, manage cookies, and
 * handle Cloudflare protection.
 */
@SuppressWarnings("unused")
@NullMarked
public class ChromeBrowser {

    private static final org.slf4j.Logger LOG = LoggerFactory.getLogger(ChromeBrowser.class);
    private static final Object LOCK = new Object();

    private ChromeBrowser() {
        // Hide Utility Class Constructor
    }

    /**
     * Downloads the HTML content of a webpage as a Jsoup Document based on the specified search parameters.
     *
     * @param searchParams the parameters containing the URL and other settings for the web page download.
     * @return the HTML content of the web page as a Jsoup Document.
     * @throws WebpageException if an error occurs during the download.
     */
    public static Document download(WebDriverSearchParams searchParams) throws WebpageException {
        return execute(searchParams, driver -> Jsoup.parse(requireNonNull(driver.getPageSource())));
    }

    /**
     * Retrieves all cookies associated with the web page specified by the search parameters.
     *
     * @param searchParams the parameters containing the URL and other settings for accessing the web page.
     * @return a set of cookies related to the specified web page.
     * @throws WebpageException if an error occurs while retrieving cookies.
     */
    public static Set<Cookie> getCookies(WebDriverSearchParams searchParams) throws WebpageException {
        return execute(searchParams, driver -> driver.manage().getCookies());
    }

    /**
     * Executes a specified function with a configured ChromeDriver based on search parameters.
     *
     * @param <T> the type of result returned by the function.
     * @param searchParams the parameters for the web page to be accessed.
     * @param function the function to execute, typically operating on the ChromeDriver instance.
     * @return the result of the function execution.
     * @throws WebpageException if an error occurs during execution or the page is not accessible.
     */
    private static <T> T execute(WebDriverSearchParams searchParams, Function<ChromeDriver, T> function)
        throws WebpageException {
        final ChromeOptions options = new ChromeOptions();

        options.addArguments("--disable-extensions");
        // options.setExperimentalOption("useAutomationExtension", false);
        options.addArguments("--proxy-server='direct://'");
        options.addArguments("--proxy-bypass-list=*");
        // options.addArguments("--start-maximized");
        options.addArguments("--window-size=1920,1200");
        // options.addArguments("--headless");
        options.addArguments("--ignore-certificate-errors");
        options.addArguments("--log-level=3");
        options.addArguments("--silent");
        options.addArguments("--disable-logging");
        options.addArguments("--disable-gpu");
        options.addArguments("--no-sandbox");
        options.addArguments("--window-position=10000,10000");
        // options.addArguments("--window-position=0,0");
        // options.addArguments("--headless");
        options.addArguments("--host-resolver-rules=MAP www.google-analytics.com 127.0.0.1");
        options.addArguments("--host-resolver-rules=MAP widgets.pinterest.com 127.0.0.1");
        options.addArguments("disable-infobars");
        // options.setExperimentalOption("excludeSwitches", Collections.singletonList("enable-automation"));
        options.addArguments("--disable-blink-features=AutomationControlled");
        options.addArguments("--remote-allow-origins=*");

        options.addArguments("--profile-directory=Default");
        options.addArguments("--incognito");
        options.addArguments("--disable-plugins-discovery");
        options.addArguments("--lang=" + Locale.ENGLISH);
        options.addArguments("--no-default-browser-check", "--no-first-run");
        options.addArguments("--no-sandbox", "--test-type");
        //        options.addArguments("--auto-open-devtools-for-tabs");

        options.setAcceptInsecureCerts(true);
        if (searchParams.proxy != null) {
            Proxy proxy = new Proxy();
            if (searchParams.proxy.socks) {
                proxy.setSocksProxy(searchParams.proxy.hostAndPort());
            } else {
                proxy.setHttpProxy(searchParams.proxy.hostAndPort());
            }
            options.setProxy(proxy);
        }

        if (ChromeManagerSingleton.singleInstance) {
            synchronized (LOCK) {
                return execute(searchParams, options, function);
            }
        } else {
            return execute(searchParams, options, function);
        }
    }

    /**
     * Executes a specified function on a webpage using a configured ChromeDriver, with support for Cloudflare bypass.
     *
     * @param <T> the type of result returned by the function.
     * @param searchParams the parameters for the web page, including URL, cookies, and Cloudflare settings.
     * @param options ChromeOptions used to configure the ChromeDriver.
     * @param function the function to execute on the web page.
     * @return the result of the function execution.
     * @throws WebpageException if an error occurs during execution or Cloudflare cannot be bypassed.
     */
    private static <T> T execute(WebDriverSearchParams searchParams, ChromeOptions options,
        Function<ChromeDriver, T> function)
        throws WebpageException {
        try (WebDriverIntf closeableWebDriver = createWebDriverInstance(searchParams, options)) {
            ChromeDriver driver = closeableWebDriver.driver();
            LOG.debug("Downloading page using ChromeDriver: " + searchParams.url);

            searchParams.cookies.forEach(cookie ->
                driver.manage().addCookie(new org.openqa.selenium.Cookie(cookie.name, cookie.value))
            );

            driver.get(searchParams.url);

            boolean cloudFlareProtected = !waitUntil(
                () -> !CloudFlare.isProtected(requireNonNull(driver.getPageSource())) &&
                    (searchParams.waitUntilCondition == null ||
                        searchParams.waitUntilCondition.test(new WebDriverPage(driver))), 5, 2000);
            if (cloudFlareProtected) {
                handleCloudflareProtection(searchParams, driver);
            } else if (searchParams.waitUntilCondition != null &&
                !waitUntil(() -> searchParams.waitUntilCondition.test(new WebDriverPage(driver)), 5, 2000)) {
                throw new WebpageException(searchParams.url);
            }

            LOG.debug("Done searching site: " + searchParams.url);
            return function.apply(driver);
        } catch (RuntimeException e) {
            LOG.error("Error during " + searchParams.url);
            throw new WebpageException(e);
        }
    }

    /**
     * Creates a new ChromeDriver instance based on the provided ChromeOptions and search parameters. Supports either
     * a reusable ChromeDriver or a
     * temporary, closeable instance.
     *
     * @param searchParams the parameters containing domain and ChromeDriver reusability options.
     * @param options ChromeOptions for configuring the ChromeDriver.
     * @return a new instance of WebDriverIntf for interacting with the webpage.
     */
    private static WebDriverIntf createWebDriverInstance(WebDriverSearchParams searchParams, ChromeOptions options) {
        return ChromeManagerSingleton.reuseWebDrivers
            ? new ReusableWebDriver(ChromeManagerSingleton.getInstance().getWebDriver(() -> new ChromeDriver(options),
            driver -> driver.usedForDomain(searchParams.getDomain())))
            : new CloseableWebDriver(new LazyObject<>(() -> new ChromeDriver(options)));
    }

    /**
     * Handles the Cloudflare protection encountered on a webpage. Prompts the user for manual intervention if
     * automatic bypass fails.
     *
     * @param searchParams the parameters for the web page, including URL and Cloudflare bypass options.
     * @param driver the ChromeDriver instance for interacting with the webpage.
     * @throws CloudflareException if manual intervention fails to bypass Cloudflare protection.
     */
    private static void handleCloudflareProtection(WebDriverSearchParams searchParams, ChromeDriver driver)
        throws CloudflareException {
        boolean bypassed = waitUntil(() -> !CloudFlare.isProtected(requireNonNull(driver.getPageSource())), 3, 2000);

        if (!bypassed && searchParams.manualCloudflareBypass) {
            driver.manage().window().maximize();
            int input = JOptionPane.showConfirmDialog(null, "Click OK when Cloudflare is manually bypassed",
                "Blocked by Cloudflare - " + searchParams.domain, JOptionPane.OK_CANCEL_OPTION);

            if (input == JOptionPane.OK_OPTION) {
                driver.navigate().to(searchParams.url);
                bypassed = waitUntil(() -> !CloudFlare.isProtected(requireNonNull(driver.getPageSource())), 3, 2000);
            }
            if (input == JOptionPane.CANCEL_OPTION || !bypassed) {
                LOG.warn("Could not access Cloudflare protected site: " + searchParams.url);
                throw new CloudflareException(searchParams.url);
            }
        }
    }

    /**
     * Waits until a specified condition is met or a maximum number of attempts is reached.
     *
     * @param successTest a boolean supplier that tests the condition to be met.
     * @param repeat the maximum number of attempts.
     * @param sleep the time in milliseconds between attempts.
     * @return true if the condition is met within the specified attempts, false otherwise.
     */
    private static boolean waitUntil(BooleanSupplier successTest, int repeat, int sleep) {
        for (int i = 0; i < repeat; i++) {
            if (successTest.getAsBoolean()) {
                return true;
            }
            try {
                Thread.sleep(sleep);
            } catch (InterruptedException e) {
                // ignore
            }
        }
        return successTest.getAsBoolean();
    }

    /**
     * Interface representing a WebDriver instance that can be closed, used to interact with a webpage.
     */
    public interface WebDriverIntf extends AutoCloseable {

        /**
         * @return the ChromeDriver instance.
         */
        ChromeDriver driver();

        /**
         * Opens a URL in a new browser window.
         *
         * @param url the URL to open in the browser.
         */
        default void openUrl(String url) {
            driver().openWindow(url);
        }

        /**
         * Closes the ChromeDriver instance and releases any allocated resources.
         */
        @Override
        void close();
    }

    /**
     * Implementation of WebDriverIntf that manages a one-time use ChromeDriver instance. Ensures proper shutdown of
     * ChromeDriver when no longer
     * needed.
     */
    private record CloseableWebDriver(LazyObject<ChromeDriver> lazyDriver) implements WebDriverIntf {

        @Override
        public void close() {
            lazyDriver.doIfInitialized(driver -> {
                try {
                    driver.close();
                } catch (RuntimeException ignore) {
                }
                driver.quit();
            });
        }

        @Override
        public ChromeDriver driver() {
            return lazyDriver.get();
        }
    }

    /**
     * Implementation of WebDriverIntf that manages a reusable ChromeDriver instance. Allows the ChromeDriver to be
     * returned to a pool for later
     * reuse.
     */
    private record ReusableWebDriver(LazyObject<ChromeDriver> lazyDriver) implements WebDriverIntf {

        @Override
        public void close() {
            lazyDriver.doIfInitialized(ChromeManagerSingleton.getInstance()::putWebDriverInPool);
        }

        @Override
        public ChromeDriver driver() {
            return lazyDriver.get();
        }
    }
}
