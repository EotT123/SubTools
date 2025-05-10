package org.lodder.subtools.multisubdownloader.subtitleproviders.tvsubtitles;

import java.util.Optional;

import manifold.ext.props.rt.api.val;
import org.lodder.subtools.sublibrary.Language;

public enum TVSubtitlesLanguage {
    ENGLISH(Language.ENGLISH, "en"), SPANISH(Language.SPANISH_CASTILIAN, "es"),
    FRENCH(Language.FRENCH, "fr"),
    GERMAN(Language.GERMAN, "de"),
    RUSSIAN(Language.RUSSIAN, "ru"),
    UKRAINIAN(Language.UKRAINIAN, "ua"),
    ITALIAN(Language.ITALIAN, "it"), GREEK(Language.GREEK_MODERN, "gr"),
    ARABIC(Language.ARABIC, "ar"),
    HUNGARIAN(Language.HUNGARIAN, "hu"),
    POLISH(Language.POLISH, "pl"),
    TURKISH(Language.TURKISH, "tr"), DUTCH(Language.DUTCH_FLEMISH, "nl"),
    PORTUGUESE(Language.PORTUGUESE, "pt"),
    SWEDISH(Language.SWEDISH, "sv"),
    DANISH(Language.DANISH, "da"),
    FINNISH(Language.FINNISH, "fi"),
    KOREAN(Language.KOREAN, "ko"), CHINESE_SIMPLIFIED(Language.CHINESE, "cn"),
    JAPANESE(Language.JAPANESE, "jp"),
    BULGARIAN(Language.BULGARIAN, "bg"),
    CZECH(Language.CZECH, "cz"), ROMANIAN(Language.ROMANIAN_MOLDAVIAN_MOLDOVAN, "ro");

    @val Language language;
    @val String langCode;

    TVSubtitlesLanguage(Language language, String langCode) {
        this.language = language;
        this.langCode = langCode;
    }

    public static Optional<TVSubtitlesLanguage> of(String langCode) {
        return TVSubtitlesLanguage.values().stream().filter(lang -> lang.langCode.equals(langCode)).findAny();
    }

    public static Optional<TVSubtitlesLanguage> of(Language language) {
        return TVSubtitlesLanguage.values().stream().filter(lang -> lang.language == language).findAny();
    }
}