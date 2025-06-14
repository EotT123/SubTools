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
import org.lodder.subtools.multisubdownloader.settings.model.Settings;
import org.lodder.subtools.multisubdownloader.settings.model.State;
import org.lodder.subtools.sublibrary.Manager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//@ExtensionMethod({Files.class})
public class SettingsControl {

    private static final Logger LOGGER = LoggerFactory.getLogger(SettingsControl.class);
    private static final String BACKING_STORE_AVAIL = "BackingStoreAvail";
    public static final String DATABASE_VERSION_KEY = "DATABASE_VERSION";

    private final Manager manager;
    private final Preferences preferences;
    @val Settings settings;
    @val State state;

    public SettingsControl(Manager manager) {
        if (!backingStoreAvailable()) {
            LOGGER.error("Unable to store preferences, used debug for reason");
        }
        this.manager = manager;
        this.preferences = Preferences.userRoot().node("MultiSubDownloader");
        this.settings = new Settings();
        this.state = new State();
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

    public void store() {
        try {
            // clean up
            preferences.clear();
            SettingValue.values().forEach(sv -> sv.store(settings, preferences));
            updateProxySettings();
        } catch (BackingStoreException e) {
            LOGGER.error(e.getMessage(), e);
        }
    }

    public void load() {
        migrateSettings();
        migrateDatabase();
        SettingValue.loadAll(settings, preferences);
        updateProxySettings();
    }

    public void exportPreferences(Path file) throws IOException, BackingStoreException {
        store();
        try (OutputStream os = file.newOutputStream()) {
            preferences.exportSubtree(os);
        }
    }

    public void importPreferences(Path file)
        throws IOException, BackingStoreException, InvalidPreferencesFormatException {
        try (InputStream is = new BufferedInputStream(file.newInputStream())) {
            preferences.clear();
            Preferences.importPreferences(is);
            load();
        }
    }

    public void updateProxySettings() {
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
    private void migrateSettings() {
        SettingValue.loadAll(settings, preferences);
//        int version = settings.settingsVersion;
    }

    private void migrateDatabase() {
//        int version = manager.getCache(DISK, DATABASE_VERSION_KEY).get(() -> 0);
    }
}
