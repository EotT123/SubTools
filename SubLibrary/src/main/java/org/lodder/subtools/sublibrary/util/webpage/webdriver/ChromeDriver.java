package org.lodder.subtools.sublibrary.util.webpage.webdriver;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.HashSet;
import java.util.Set;

import org.jspecify.annotations.NullMarked;
import org.openqa.selenium.Capabilities;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.remote.http.ClientConfig;

@NullMarked
public class ChromeDriver extends org.openqa.selenium.chrome.ChromeDriver implements WebDriver {

    private final Set<String> usedForDomains = new HashSet<>();

    /**
     * Creates a new ChromeDriver using the {@link ChromeDriverService#createDefaultService default} server
     * configuration.
     *
     * @see #ChromeDriver(ChromeDriverService, ChromeOptions)
     */
    public ChromeDriver() {
        super();
    }

    /**
     * Creates a new ChromeDriver instance. The {@code service} will be started along with the driver, and shutdown
     * upon calling {@link #quit()}.
     *
     * @param service The service to use.
     * @see RemoteWebDriver#RemoteWebDriver(org.openqa.selenium.remote.CommandExecutor, Capabilities)
     */
    public ChromeDriver(ChromeDriverService service) {
        super(service);
    }

    /**
     * Creates a new ChromeDriver instance with the specified options.
     *
     * @param options The options to use.
     * @see #ChromeDriver(ChromeDriverService, ChromeOptions)
     */
    public ChromeDriver(ChromeOptions options) {
        super(options);
    }

    /**
     * Creates a new ChromeDriver instance with the specified options. The {@code service} will be started along with
     * the driver, and shutdown upon
     * calling {@link #quit()}.
     *
     * @param service The service to use.
     * @param options The options required from ChromeDriver.
     */
    public ChromeDriver(ChromeDriverService service, ChromeOptions options) {
        super(service, options);
    }

    public ChromeDriver(ChromeDriverService service, ChromeOptions options, ClientConfig clientConfig) {
        super(service, options, clientConfig);
    }

    @Override
    public boolean usedForDomain(String domain) {
        System.out.printf("usedForDomain [%s]: %s%n", domain, usedForDomains);
        return usedForDomains.contains(domain);
    }

    @Override
    public void get(String url) {
        System.out.println("get: " + url);
        usedForDomains.add(getDomain(url));
        super.get(url);
    }

    public void openWindow(String url) {
        System.out.println("open window: " + url);
        usedForDomains.add(getDomain(url));
        executeScript("window.open('{%s}', '_blank')".formatted(url));
    }

    public static String getDomain(String url) {
        try {
            return new URI(url).getHost();
        } catch (URISyntaxException e) {
            return url.replaceAll("http(s)?://|www\\.|/.*", "");
        }
    }
}
