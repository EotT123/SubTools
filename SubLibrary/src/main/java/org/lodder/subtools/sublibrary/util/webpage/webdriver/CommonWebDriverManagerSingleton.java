package org.lodder.subtools.sublibrary.util.webpage.webdriver;

import static manifold.ext.props.rt.api.PropOption.*;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.function.Supplier;

import manifold.ext.props.rt.api.get;
import manifold.ext.props.rt.api.var;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.util.lazy.LazyObject;
import org.openqa.selenium.WebDriver;

@NullMarked
class CommonWebDriverManagerSingleton<W extends WebDriver> {
    private static final List<WebDriver> WEB_DRIVER_POOL = new ArrayList<>();
    private static final List<WebDriver> CREATED_WEB_DRIVERS = new ArrayList<>();
    private static boolean patched = false;
    private static CommonWebDriverManagerSingleton<? extends WebDriver> instance;

    @get @var(Private) static boolean reuseWebDrivers = false;
    @get @var(Private) static boolean singleInstance = false;
    private final BooleanSupplier webDriverPatcher;

    protected CommonWebDriverManagerSingleton(Runnable initialization, BooleanSupplier webDriverPatcher) {
        this.webDriverPatcher = webDriverPatcher;
        initialization.run();
    }

    protected static synchronized <W extends WebDriver> CommonWebDriverManagerSingleton<W> getInstance(
        Runnable initialization) {
        return getInstance(initialization, () -> true);
    }

    @SuppressWarnings("unchecked")
    protected static synchronized <W extends WebDriver> CommonWebDriverManagerSingleton<W> getInstance(
        Runnable initialization, BooleanSupplier webDriverPatcher) {
        if (instance == null) {
            instance = new CommonWebDriverManagerSingleton<>(initialization, webDriverPatcher);
        }
        return (CommonWebDriverManagerSingleton<W>) instance;
    }

    public static void reuseWebDrivers() {
        reuseWebDrivers = true;
    }

    public static void singleInstance() {
        singleInstance = true;
    }

    public LazyObject<W> getWebDriver(Supplier<W> webDriverSupplier) {
        return getWebDriver(webDriverSupplier, webDriver -> true, true);
    }

    public LazyObject<W> getWebDriver(Supplier<W> webDriverSupplier, Predicate<W> filter) {
        return getWebDriver(webDriverSupplier, filter, true);
    }

    public LazyObject<W> getWebDriver(Supplier<W> webDriverSupplier, Predicate<W> filter,
        boolean suppressFilterWhenNoMatch) {
        return new LazyObject<>(() -> {
            List<W> webDriverPool = getWebDriverPool();
            synchronized (webDriverPool) {
                if (!patched) {
                    patched = webDriverPatcher.getAsBoolean();
                }
            }
            if (!reuseWebDrivers) {
                return webDriverSupplier.get();
            }
            synchronized (webDriverPool) {
                if (!webDriverPool.isEmpty()) {
                    W webDriver = webDriverPool.stream().filter(filter).findAny().orElse(null);
                    if (webDriver != null) {
                        return webDriverPool.remove(webDriverPool.indexOf(webDriver));
                    } else if (suppressFilterWhenNoMatch) {
                        return webDriverPool.removeFirst();
                    }
                }
            }
            W webDriver = webDriverSupplier.get();
            synchronized (CREATED_WEB_DRIVERS) {
                CREATED_WEB_DRIVERS.add(webDriver);
            }
            return webDriver;
        });
    }

    @SuppressWarnings("unchecked")
    private List<W> getWebDriverPool() {
        return (List<W>) WEB_DRIVER_POOL;
    }

    public void putWebDriverInPool(W driver) {
        synchronized (WEB_DRIVER_POOL) {
            WEB_DRIVER_POOL.add(driver);
        }
    }

    public static void closeAllWebDrivers() {
        synchronized (CREATED_WEB_DRIVERS) {
            CREATED_WEB_DRIVERS.forEach(webDriver -> {
                try {
                    webDriver.close();
                    webDriver.quit();
                } catch (Exception e) {
                    // continue
                }
            });
            CREATED_WEB_DRIVERS.clear();
        }
        reuseWebDrivers = false;
        singleInstance = false;
    }
}
