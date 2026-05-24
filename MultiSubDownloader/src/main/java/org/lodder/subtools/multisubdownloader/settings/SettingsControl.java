package org.lodder.subtools.multisubdownloader.settings;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Path;
import java.util.prefs.BackingStoreException;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.settings.model.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
public class SettingsControl {

    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsControl.class);
    private static final String BACKING_STORE_AVAIL = "BackingStoreAvail";
    public static final String DATABASE_VERSION_KEY = "DATABASE_VERSION";

    private static final Preferences PREFERENCES;
    @val static Settings settings = new Settings();

    static {
        if (!backingStoreAvailable()) {
            LOGGER.error("Unable to store preferences, used debug for reason");
        }
        PREFERENCES = Preferences.userRoot().node("MultiSubDownloader");
        load();
    }

    private static boolean backingStoreAvailable() {
        Preferences prefs = Preferences.userRoot().node("MultiSubDownloader");
        try {
            boolean oldValue = prefs.getBoolean(BACKING_STORE_AVAIL, false);
            prefs.putBoolean(BACKING_STORE_AVAIL, !oldValue);
            prefs.flush();
        } catch (BackingStoreException e) {
            LOGGER.error("BackingStore is not available, settings could not be loaded using defaults", e);
            return false;
        }
        return true;
    }

    public static void store() {
        try {
            // clean up
            PREFERENCES.clear();
            SettingValue.values().forEach(sv -> sv.store(PREFERENCES));
            updateProxySettings();
        } catch (BackingStoreException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public static void load() {
        migrateSettings();
        migrateDatabase();
        SettingValue.loadAll(PREFERENCES);
        updateProxySettings();
    }

    public static void exportPreferences(Path file) throws IOException, BackingStoreException {
        store();
        try (OutputStream os = file.newOutputStream()) {
            PREFERENCES.exportSubtree(os);
        }
    }

    public static void importPreferences(Path file)
        throws IOException, BackingStoreException, InvalidPreferencesFormatException {
        try (InputStream is = new BufferedInputStream(file.newInputStream())) {
            PREFERENCES.clear();
            Preferences.importPreferences(is);
            load();
        }
    }

    private static void updateProxySettings() {
        if (settings.generalProxyEnabled) {
            System.getProperties().put("proxySet", "true");
            System.getProperties().put("proxyHost", settings.generalProxyHost);
            System.getProperties().put("proxyPort", settings.generalProxyPort);
        } else {
            System.getProperties().put("proxySet", "false");
        }
    }

    /**
     * Migrate settings layout for backward incompatibility changes.
     */
    private static void migrateSettings() {
        SettingValue.loadAll(PREFERENCES);
//        int version = settings.settingsVersion;
    }

    private static void migrateDatabase() {
//        int version = Manager.getInstance().getCache(DISK, DATABASE_VERSION_KEY).get(() -> 0);
    }
}
