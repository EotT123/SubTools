package extensions.java.util.prefs.Preferences;

import java.util.function.Function;
import java.util.prefs.Preferences;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@Extension
@NullMarked
public class PreferencesExt {

    private PreferencesExt() {
        // Hide Utility Class Constructor
    }

    public static <R extends @Nullable Object> R computeIfPresent(@This Preferences preferences, String key,
        Function<String, R> mapper, R defaultValue) {
        String value = preferences.get(key, null);
        return value == null ? defaultValue : mapper.apply(value);
    }
}

