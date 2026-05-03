package org.lodder.subtools.sublibrary.util.webpage.webdriver;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.function.BooleanSupplier;
import java.util.logging.Level;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.IntStream;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.openqa.selenium.chrome.ChromeDriverInfo;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.remote.service.DriverFinder;

@NullMarked
public class ChromeManagerSingleton extends CommonWebDriverManagerSingleton<ChromeDriver> {

    private static @Nullable ChromeManagerSingleton INSTANCE;

    private ChromeManagerSingleton(Runnable initialization, BooleanSupplier webDriverPatcher) {
        super(initialization, webDriverPatcher);
    }

    protected static synchronized ChromeManagerSingleton getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ChromeManagerSingleton(() -> {
                WebDriverManager.chromedriver().setup();
                System.setProperty("webdriver.chrome.args", "--disable-logging");
                System.setProperty("webdriver.chrome.silentOutput", "true");
                System.setProperty("webdriver.chrome.verboseLogging", "false");
                java.util.logging.Logger.getLogger("org.openqa.selenium").setLevel(Level.SEVERE);
                java.util.logging.Logger.getLogger("org.apache.http").setLevel(Level.SEVERE);
            }, ChromeManagerSingleton::patchWebDriverExecutable);
        }
        return INSTANCE;
    }

    // https://github.com/ultrafunkamsterdam/undetected-chromedriver/blob/bf7dcf8b5713020de7454844fb80036b8c456503/undetected_chromedriver/patcher.py#L214
    private static boolean patchWebDriverExecutable() {
        try (ChromeDriverService chromeDriverService = ChromeDriverService.createDefaultService()) {
            File chromeDriverExecutable =
                new File(new DriverFinder(chromeDriverService,
                    new ChromeDriverInfo().getCanonicalCapabilities()).getDriverPath());
            Path patchedFile = chromeDriverExecutable.toPath().resolveSibling("already_patched");
            if (Files.exists(patchedFile)) {
                return true;
            }
            int length = (int) chromeDriverExecutable.length();
            byte[] data;
            try (FileInputStream in = new FileInputStream(chromeDriverExecutable);
                 ByteArrayOutputStream bs = new ByteArrayOutputStream(length)) {
                byte[] buffer = new byte[128_000];
                int len;
                while ((len = in.read(buffer)) > 0) {
                    bs.write(buffer, 0, len);
                }
                data = bs.toByteArray();
            }
            searchAndReplace(data);

            try (FileOutputStream out = new FileOutputStream(chromeDriverExecutable);
                 ByteArrayInputStream bs = new ByteArrayInputStream(data)) {
                byte[] buffer = new byte[128_000];
                int len;
                while ((len = bs.read(buffer)) > 0) {
                    out.write(buffer, 0, len);
                }
                bs.close();
                out.flush();
            }
            Files.createFile(patchedFile);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void searchAndReplace(byte[] data) {
        String replaceText = "{console.log(\"undetected chromedriver 1337!\")}";
        byte[] first = "{window.cdc".getBytes(StandardCharsets.US_ASCII);
        Pattern test = Pattern.compile("\\{window\\.cdc.*?;}");
        try {
            for (int i = 0; i < data.length - first.length; i++) {
                int j = i;
                if (IntStream.range(0, first.length).allMatch(idx -> data[j + idx] == first[idx])) {
                    String text = new String(data, i, 1000, StandardCharsets.US_ASCII);
                    Matcher matcher = test.matcher(text);
                    if (matcher.find()) {// found it
                        String toReplace = matcher.group();
                        byte[] replacement =
                            (replaceText + " ".repeat(toReplace.length() - replaceText.length())).getBytes(
                                StandardCharsets.US_ASCII);
                        System.arraycopy(replacement, 0, data, i, replacement.length);
                        i += replacement.length;
                    }
                }
            }
        } catch (RuntimeException e) {
            e.printStackTrace();
        }
    }
}
