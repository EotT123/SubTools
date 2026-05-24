package org.lodder.subtools.multisubdownloader;

import static java.time.temporal.ChronoUnit.*;
import static org.lodder.subtools.multisubdownloader.Messages.*;
import static util.Utils.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Element;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.settings.SettingsControl;
import org.lodder.subtools.multisubdownloader.settings.model.UpdateCheckPeriod;
import org.lodder.subtools.multisubdownloader.util.PropertiesReader;
import org.lodder.subtools.multisubdownloader.util.PropertiesReader.PomProperty;
import org.lodder.subtools.sublibrary.ConfigProperties;
import org.lodder.subtools.sublibrary.ConfigProperties.Property;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.Manager.CacheKey;
import org.lodder.subtools.sublibrary.Manager.Value;
import org.lodder.subtools.sublibrary.PageContentParams;
import org.lodder.subtools.sublibrary.cache.CacheType;
import org.lodder.subtools.sublibrary.cache.ProviderCacheKey;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
public class UpdateAvailableGithub {

    private static final Logger LOGGER = LoggerFactory.getLogger(UpdateAvailableGithub.class);

    private static final String DOMAIN = "https://github.com";
    private static final String REPO_URI = "/EotT/SubTools";
    private static final String REPO_URL = DOMAIN + REPO_URI;

    public boolean shouldCheckForNewUpdate(@Nullable UpdateCheckPeriod updateCheckPeriod) {
        LocalDate lastUpdateCheck = getLastUpdateCheck();
        try {
            return switch (updateCheckPeriod) {
                case DAILY -> DAYS.between(lastUpdateCheck, LocalDate.now()) > 0;
                case WEEKLY -> DAYS.between(lastUpdateCheck, LocalDate.now()) > 6;
                case MONTHLY -> DAYS.between(lastUpdateCheck, LocalDate.now()) > 30;
                case MANUAL -> false;
                case null -> false;
            };
        } catch (Exception e) {
            LOGGER.error("checkProgram", e);
            return false;
        }
    }

    public @Nullable String getLatestDownloadUrl() {
        return switch (SettingsControl.settings.updateType) {
            case STABLE -> getUrlLatestNewStableGithubRelease();
            case NIGHTLY -> getUrlLatestNewNightlyGithubRelease();
            case null -> null;
        };
    }

    public boolean isNewVersionAvailable() {
        return switch (SettingsControl.settings.updateType) {
            case STABLE -> getUrlLatestNewStableGithubRelease() != null;
            case NIGHTLY -> getUrlLatestNewNightlyGithubRelease() != null;
            case null -> false;
        };
    }

    private @Nullable String getUrlLatestNewStableGithubRelease() {
        return new CacheKey(CacheType.MEMORY, new ProviderCacheKey("Github", "update-url"))
            .get(() -> {
                try {
                    String currentVersion = getVersion();
                    Element element =
                        Manager.getDocument(new PageContentParams(
                                url:"$REPO_URL/releases",
                                cacheType:CacheType.NONE,
                                userAgent:null))
                            .selectFirstByCss("#repo-content-turbo-frame .box a[href='$REPO_URI/releases/latest']");
                    Pattern versionPattern = Pattern.compile("\\d*\\.\\d\\.\\d");
                    String versionText = element.parentElement().selectFirst("a").text();
                    Matcher matcher = versionPattern.matcher(versionText);
                    matcher.find();
                    String version = matcher.group();
                    if (isFinalVersion(currentVersion) && compareVersions(version, currentVersion) <= 0) {
                        return null;
                    }
                    String versionBlockUrl = REPO_URL + "/releases/expanded_assets/" + versionText;
                    Element artifactElement = Manager.getDocument(
                            new PageContentParams(url:versionBlockUrl, userAgent:null))
                        .selectFirstByCss(".Box-row a[href$='.jar']");
                    String url = DOMAIN + artifactElement.attr("href");
                    updateLastUpdateCheck();
                    return url;
                } catch (Exception e) {
                    if (LOGGER.isTraceEnabled) {
                        LOGGER.trace(getText("LoggingPanel.UpdateCheckFailed"), e);
                    } else {
                        LOGGER.error(getText("LoggingPanel.UpdateCheckFailed"));
                    }
                    return null;
                }
            });
    }

    private @Nullable String getUrlLatestNewNightlyGithubRelease() {
        return new CacheKey(CacheType.MEMORY,
            new ProviderCacheKey("Github", "update-url-nightly"))
            .get(() -> {
                try {
                    LocalDateTime buildTista = getBuildTista();

                    Element rowElement = Manager.getDocument(new PageContentParams(
                            url:"$REPO_URL/actions?query=branch%3Amaster",
                            cacheType:CacheType.MEMORY,
                            userAgent:null))
                        .selectFirstByCss("#partial-actions-workflow-runs .Box-row");
                    LocalDateTime nightlyBuildTista = zonedDateTimeStringToLocalDateTime(
                        rowElement.selectFirstByCss(".d-inline relative-time").attr("datetime"));
                    if (nightlyBuildTista.isBefore(buildTista)) {
                        return null;
                    }
                    String url = "https://nightly.link" + rowElement.selectFirstByCss(".Link--primary").attr("href");
                    String downloadUrl = Manager.getDocument(new PageContentParams(url, CacheType.MEMORY))
                        .selectFirstByCss("table td a")
                        .attr("href");
                    updateLastUpdateCheck();
                    return downloadUrl;
                } catch (Exception e) {
                    if (LOGGER.isTraceEnabled) {
                        LOGGER.trace(getText("LoggingPanel.UpdateCheckFailed"), e);
                    } else {
                        LOGGER.error(getText("LoggingPanel.UpdateCheckFailed"));
                    }
                    return null;
                }
            });
    }

    private LocalDateTime getBuildTista() {
        return ifNotNullOrElseGet(PropertiesReader.getProperty(PomProperty.BUILD_TIMESTAMP),
            this::zonedDateTimeStringToLocalDateTime, LocalDateTime::now);
    }

    private String getVersion() {
        return ConfigProperties.getProperty(Property.VERSION);
    }

    private boolean isFinalVersion(String version) {
        return !version.contains("-SNAPSHOT");
    }

    private CacheKey getUpdateLastUpdateCheckCache() {
        return new CacheKey(CacheType.MEMORY, new ProviderCacheKey("Github", "LastUpdateCheck"));
    }

    private void updateLastUpdateCheck() {
        getUpdateLastUpdateCheckCache().store(Value.of(LocalDate.now()));
    }

    private LocalDate getLastUpdateCheck() {
        return getUpdateLastUpdateCheckCache().get(() -> LocalDate.MIN);
    }

    private LocalDateTime zonedDateTimeStringToLocalDateTime(String dateString) {
        Instant instant = Instant.parse(dateString);
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.of("UTC"));
        return zonedDateTime.toLocalDateTime();
    }

    private int compareVersions(String str1, String str2) {
        String[] vals1 = str1.split("\\.");
        String[] vals2 = str2.split("\\.");
        int i = 0;
        while (i < vals1.length && i < vals2.length && vals1[i].equals(vals2[i])) {
            i++;
        }

        if (i < vals1.length && i < vals2.length) {
            int diff = Integer.valueOf(vals1[i]).compareTo(Integer.valueOf(vals2[i]));
            return Integer.signum(diff);
        }

        return Integer.signum(vals1.length - vals2.length);
    }
}
