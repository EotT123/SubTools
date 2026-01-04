package org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed;

import java.util.List;

import manifold.ext.props.rt.api.val;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.Language;

@NullMarked
public enum Addic7edLanguage {
    ALL(null, "All", 0),
    ALBANIAN(Language.ALBANIAN, "Albanian", 52),
    ARABIC(Language.ARABIC, "Arabic", 38),
    ARMENIAN(Language.ARMENIAN, "Armenian", 50),
    AZERBAIJANI(Language.AZERBAIJANI, "Azerbaijani", 48),
    BENGALI(Language.BENGALI, "Bengali", 47),
    BOSNIAN(Language.BOSNIAN, "Bosnian", 44),
    BULGARIAN(Language.BULGARIAN, "Bulgarian", 35),
    CANTONESE(Language.CHINESE, "Cantonese", 64),
    CATALAN(Language.CATALAN_VALENCIAN, "Català", 12),
    CHINESE_SIMPLIFIED(Language.CHINESE, "Chinese (Simplified)", 41),
    CHINESE_TRADITIONAL(Language.CHINESE, "Chinese (Traditional)", 24),
    CROATIAN(Language.CROATIAN, "Croatian", 31),
    CZECH(Language.CZECH, "Czech", 14),
    DANISH(Language.DANISH, "Danish", 30),
    DUTCH(Language.DUTCH_FLEMISH, "Dutch", 17),
    ENGLISH(Language.ENGLISH, "English", 1),
    ESTONIAN(Language.ESTONIAN, "Estonian", 54),
    EUSKERA(Language.BASQUE, "Euskera", 13),
    FINNISH(Language.FINNISH, "Finnish", 28),
    FRENCH(Language.FRENCH, "French", 8),
    FRENCH_CANADIAN(Language.FRENCH, "French (Canadian)", 53),
    GALICIAN(Language.GALICIAN, "Galego", 15),
    GERMAN(Language.GERMAN, "German", 11),
    GREEK(Language.GREEK_MODERN, "Greek", 27),
    HEBREW(Language.HEBREW, "Hebrew", 23),
    HINDI(Language.HINDI, "Hindi", 55),
    HUNGARIAN(Language.HUNGARIAN, "Hungarian", 20),
    ICELANDIC(Language.ICELANDIC, "Icelandic", 56),
    INDONESIAN(Language.INDONESIAN, "Indonesian", 37),
    ITALIAN(Language.ITALIAN, "Italian", 7),
    JAPANESE(Language.JAPANESE, "Japanese", 32),
    KANNADA(Language.KANNADA, "Kannada", 66),
    KLINGON(Language.KLINGON, "Klingon", 61),
    KOREAN(Language.KOREAN, "Korean", 42),
    LATVIAN(Language.LATVIAN, "Latvian", 57),
    LITHUANIAN(Language.LITHUANIAN, "Lithuanian", 58),
    MACEDONIAN(Language.MACEDONIAN, "Macedonian", 49),
    MALAY(Language.MALAY, "Malay", 40),
    MALAYALAM(Language.MALAYALAM, "Malayalam", 67),
    MARATHI(Language.MARATHI, "Marathi", 62),
    NORWEGIAN(Language.NORWEGIAN, "Norwegian", 29),
    PERSIAN(Language.PERSIAN, "Persian", 43),
    POLISH(Language.POLISH, "Polish", 21),
    PORTUGUESE(Language.PORTUGUESE, "Portuguese", 9),
    PORTUGUESE_BRASILIAN(Language.PORTUGUESE, "Portuguese (Brazilian)", 10),
    ROMANIAN(Language.ROMANIAN_MOLDAVIAN_MOLDOVAN, "Romanian", 26),
    RUSSIAN(Language.RUSSIAN, "Russian", 19),
    SERBIAN_CYRILLIC(Language.SERBIAN, "Serbian (Cyrillic)", 39),
    SERBIAN_LATIN(Language.SERBIAN, "Serbian (Latin)", 36),
    SINHALA(Language.SINHALA_SINHALESE, "Sinhala", 60),
    SLOVAK(Language.SLOVAK, "Slovak", 25),
    SLOVENIAN(Language.SLOVENIAN, "Slovenian", 22),
    SPANISH(Language.SPANISH_CASTILIAN, "Spanish", 4),
    SPANISH_ARGENTINA(Language.SPANISH_CASTILIAN, "Spanish (Argentina)", 69),
    SPANISH_LATIN_AMERICA(Language.SPANISH_CASTILIAN, "Spanish (Latin America)", 6),
    SPANISH_SPAIN(Language.SPANISH_CASTILIAN, "Spanish (Spain)", 5),
    SWEDISH(Language.SWEDISH, "Swedish", 18),
    TAGALOG(Language.TAGALOG, "Tagalog", 68),
    TAMIL(Language.TAMIL, "Tamil", 59),
    TELUGU(Language.TELUGU, "Telugu", 63),
    THAI(Language.THAI, "Thai", 46),
    TURKISH(Language.TURKISH, "Turkish", 16),
    UKRAINIAN(Language.UKRAINIAN, "Ukrainian", 51),
    VIETNAMESE(Language.VIETNAMESE, "Vietnamese", 45),
    WELSH(Language.WELSH, "Welsh", 65);

    @val Language language;
    @val String value;
    @val int id;

    Addic7edLanguage(org.lodder.subtools.sublibrary.Language language, String value, int id) {
        this.language = language;
        this.value = value;
        this.id = id;
    }

    public static Addic7edLanguage of(String value) {
        return Addic7edLanguage.values().stream().filter(l -> Strings.CS.equals(l.value, value)).findFirst()
            .orElse(null);
    }

    public static List<Addic7edLanguage> of(Language language) {
        return Addic7edLanguage.values().stream().filter(l -> l.language == language).toList();
    }
}