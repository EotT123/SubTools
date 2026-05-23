package org.lodder.subtools.multisubdownloader.framework;

import java.util.HashMap;
import java.util.Map;
import java.util.prefs.Preferences;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.settings.model.Settings;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.util.lazy.LazySupplier;

@NullMarked
public class Container {

    private final Map<String, LazySupplier<Object>> bindings = new HashMap<>();

    public void bind(String name, LazySupplier<Object> resolver) {
        bindings.put(name, resolver);
    }

    public Object make(String name) {
        return bindings.get(name).get();
    }

    public Manager makeManager() {
        return (Manager) make("Manager");
    }

    public Settings makeSettings() {
        return (Settings) make("Settings");
    }

    public Preferences makePreferences() {
        return (Preferences) make("Preferences");
    }
}
