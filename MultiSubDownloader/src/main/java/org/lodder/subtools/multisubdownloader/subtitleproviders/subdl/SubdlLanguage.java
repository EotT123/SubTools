package org.lodder.subtools.multisubdownloader.subtitleproviders.subdl;

import manifold.ext.props.rt.api.val;
import manifold.ext.props.rt.api.var;
import org.lodder.subtools.sublibrary.Language;

public enum SubdlLanguage {

    ARABIC(Language.ARABIC, "AR"),
    //    BRAZILLIAN_PORTUGUESE(Language.PORTUGUESE,"BR_PT"),
    DANISH(Language.DANISH, "DA"),
    DUTCH(Language.DUTCH, "NL"),
    ENGLISH(Language.ENGLISH, "EN"),
    FARSI_PERSIAN(Language.PERSIAN, "FA"),
    FINNISH(Language.FINNISH, "FI"),
    FRENCH(Language.FRENCH, "FR"),
    INDONESIAN(Language.INDONESIAN, "ID"),
    ITALIAN(Language.ITALIAN, "IT"),
    NORWEGIAN(Language.NORWEGIAN, "NO"),
    ROMANIAN(Language.ROMANIAN, "RO"),
    SPANISH(Language.SPANISH, "ES"),
    SWEDISH(Language.SWEDISH, "SV"),
    VIETNAMESE(Language.VIETNAMESE, "VI"),
    ALBANIAN(Language.ALBANIAN, "SQ"),
    AZERBAIJANI(Language.AZERBAIJANI, "AZ"),
    BELARUSIAN(Language.BELARUSIAN, "BE"),
    BENGALI(Language.BENGALI, "BN"),
    //    BIG 5 CODE(Language.BIG 5 CODE,"ZH_BG"),
    BOSNIAN(Language.BOSNIAN, "BS"),
    BULGARIAN(Language.BULGARIAN, "BG"),
    //    BULGARIAN_ENGLISH(Language.BULGARIAN_ENGLISH,"BG_EN"),
//    BURMESE(Language.BURMESE,"MY"),
    CATALAN(Language.CATALAN, "CA"),
    CHINESE(Language.CHINESE_SIMPLIFIED, "ZH"),
    CROATIAN(Language.CROATIAN, "HR"),
    CZECH(Language.CZECH, "CS"),
    //    DUTCH_ENGLISH(Language.DUTCH_ENGLISH,"NL_EN"),
//    ENGLISH_GERMAN(Language.ENGLISH_GERMAN,"EN_DE"),
//    ESPERANTO(Language.ESPERANTO,"EO"),
    ESTONIAN(Language.ESTONIAN, "ET"),
    //    GEORGIAN(Language.GEORGIAN,"KA"),
    GERMAN(Language.GERMAN, "DE"),
    GREEK(Language.GREEK, "EL"),
    //    GREENLANDIC(Language.GREENLANDIC,"KL"),
    HEBREW(Language.HEBREW, "HE"),
    HINDI(Language.HINDI, "HI"),
    HUNGARIAN(Language.HUNGARIAN, "HU"),
    //    HUNGARIAN_ENGLISH(Language.HUNGARIAN_ENGLISH,"HU_EN"),
    ICELANDIC(Language.ICELANDIC, "IS"),
    JAPANESE(Language.JAPANESE, "JA"),
    KOREAN(Language.KOREAN, "KO"),
    //    KURDISH(Language.KURDISH,"KU"),
    LATVIAN(Language.LATVIAN, "LV"),
    LITHUANIAN(Language.LITHUANIAN, "LT"),
    MACEDONIAN(Language.MACEDONIAN, "MK"),
    MALAY(Language.MALAY, "MS"),
    MALAYALAM(Language.MALAYALAM, "ML"),
    //    MANIPURI(Language.MANIPURI,"MNI"),
    POLISH(Language.POLISH, "PL"),
    PORTUGUESE(Language.PORTUGUESE, "PT"),
    RUSSIAN(Language.RUSSIAN, "RU"),
    SERBIAN(Language.SERBIAN, "SR"),
    SINHALA(Language.SINHALA, "SI"),
    SLOVAK(Language.SLOVAK, "SK"),
    SLOVENIAN(Language.SLOVENIAN, "SL"),
    TAGALOG(Language.TAGALOG, "TL"),
    TAMIL(Language.TAMIL, "TA"),
    TELUGU(Language.TELUGU, "TE"),
    THAI(Language.THAI, "TH"),
    TURKISH(Language.TURKISH, "TR")
//    UKRANIAN(Language.UKRANIAN,"UK"),
//    URDU(Language.URDU,"UR");

    @var Language language;
    @val String langCode;

    SubdlLanguage(Language language, String langCode) {
        this.language = language;
        this.langCode = langCode;
    }

    public static SubdlLanguage fromLanguage(Language language) {
        return SubdlLanguage.values().stream().filter(l -> l.language == language).findFirst().orElse(null);
    }
    }
