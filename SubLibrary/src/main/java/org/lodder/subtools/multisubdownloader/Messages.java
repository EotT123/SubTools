package org.lodder.subtools.multisubdownloader;

import java.util.List;
import java.util.Locale;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import manifold.ext.props.rt.api.var;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.Language;

@NullMarked
public class Messages {
    private static final String BUNDLE_NAME = "resourcebundle.Message";
    private static ResourceBundle resourceBundle = ResourceBundle.getBundle(BUNDLE_NAME, Locale.ROOT);
    static @var Language language = Language.ENGLISH;

    private Messages() {
    }

    public static String getText(String key, @Nullable Object... replacements) {
        try {
            String text = resourceBundle.getString(key);
            return replacements.isEmpty() ? text : text.formatted(replacements);
        } catch (MissingResourceException e) {
            return "!$key!";
        }
    }

    public static String getText(String key, Language language, @Nullable Object... replacements) {
        try {
            String text = getMessageBundle(language).getString(key);
            return replacements.isEmpty() ? text : text.formatted(replacements);
        } catch (MissingResourceException e) {
            return "!$key!";
        }
    }

    public static void setLanguage(Language language) {
        Messages.language = language;
        resourceBundle = getMessageBundle(language);
    }

    private static ResourceBundle getMessageBundle(Language language) {
        Locale locale = language == Language.ENGLISH ? Locale.ROOT : Locale.forLanguageTag(language.iso639_3);
        return ResourceBundle.getBundle(BUNDLE_NAME, locale);
    }

    public static List<Language> getAvailableLanguages() {
        return List.of(Language.DUTCH_FLEMISH, Language.ENGLISH);
    }
}
