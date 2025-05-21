package org.lodder.subtools.multisubdownloader.settings;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.prefs.Preferences;
import java.util.stream.IntStream;

import com.google.common.base.CaseFormat;
import com.google.common.base.Objects;
import manifold.ext.props.rt.api.val;
import org.lodder.subtools.multisubdownloader.gui.extra.MemoryFolderChooser;
import org.lodder.subtools.multisubdownloader.lib.library.LibraryActionType;
import org.lodder.subtools.multisubdownloader.lib.library.LibraryOtherFileActionType;
import org.lodder.subtools.multisubdownloader.settings.model.LibrarySettings;
import org.lodder.subtools.multisubdownloader.settings.model.PathOrRegex;
import org.lodder.subtools.multisubdownloader.settings.model.ScreenSettings;
import org.lodder.subtools.multisubdownloader.settings.model.Settings;
import org.lodder.subtools.multisubdownloader.settings.model.SettingsProcessEpisodeSource;
import org.lodder.subtools.multisubdownloader.settings.model.UpdateCheckPeriod;
import org.lodder.subtools.multisubdownloader.settings.model.UpdateType;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.control.VideoPatterns;
import org.lodder.subtools.sublibrary.util.function.TriConsumer;

public enum SettingValue {

    // SETTINGS
    SETTINGS_VERSION(createSetting(Mappers.INT,
        SettingsControl::getSettings,
        Settings::getSettingsVersion,
        Settings::setSettingsVersion,
        0)),
    LAST_OUTPUT_DIR(createSetting(Mappers.PATH,
        SettingsControl::getSettings,
        _ -> MemoryFolderChooser.getInstance().memory,
        Settings::setLastOutputDir,
        Path.of(""))),

    GENERAL_DEFAULT_INCOMING_FOLDER(createSetting(Mappers.PATH,
        SettingsControl::getSettings,
        Settings::getDefaultIncomingFolders)),
    LOCAL_SUBTITLES_SOURCES_FOLDERS(createSetting(Mappers.PATH,
        SettingsControl::getSettings,
        Settings::getLocalSourcesFolders)),
    EXCLUDE_ITEM(createSetting(Mappers.PATH_OR_REGEX,
        SettingsControl::getSettings,
        Settings::getExcludeList)),
    DEFAULT_SELECTION_QUALITY(createSetting(enumMapper(VideoPatterns.Source.class),
        SettingsControl::getSettings,
        Settings::getOptionsDefaultSelectionQualityList)),
    DEFAULT_SELECTION_QUALITY_ENABLED(createSetting(Mappers.BOOLEAN,
        SettingsControl::getSettings,
        Settings::isOptionsDefaultSelection,
        Settings::setOptionsDefaultSelection,
        false)),

    OPTIONS_LANGUAGE(createSettingEnum(
        SettingsControl::getSettings,
        Settings::getLanguage,
        Settings::setLanguage,
        Language.ENGLISH)),
    OPTIONS_ALWAYS_CONFIRM(createSetting(Mappers.BOOLEAN,
        SettingsControl::getSettings,
        Settings::isOptionsAlwaysConfirm,
        Settings::setOptionsAlwaysConfirm,
        false)),
    OPTIONS_CONFIRM_MAPPING(createSetting(Mappers.BOOLEAN,
        SettingsControl::getSettings,
        Settings::isOptionsConfirmProviderMapping,
        Settings::setOptionsConfirmProviderMapping,
        true)),
    OPTIONS_MIN_AUTOMATIC_SELECTION(createSetting(Mappers.BOOLEAN,
        SettingsControl::getSettings,
        Settings::isOptionsMinAutomaticSelection,
        Settings::setOptionsMinAutomaticSelection,
        false)),
    OPTIONS_MIN_AUTOMATIC_SELECTION_VALUE(createSetting(Mappers.INT,
        SettingsControl::getSettings,
        Settings::getOptionsMinAutomaticSelectionValue,
        Settings::setOptionsMinAutomaticSelectionValue,
        0)),
    OPTION_SUBTITLE_EXACT_MATCH(createSetting(Mappers.BOOLEAN,
        SettingsControl::getSettings,
        Settings::isOptionSubtitleExactMatch,
        Settings::setOptionSubtitleExactMatch,
        true)),
    OPTION_SUBTITLE_KEYWORD_MATCH(createSetting(Mappers.BOOLEAN,
        SettingsControl::getSettings,
        Settings::isOptionSubtitleKeywordMatch,
        Settings::setOptionSubtitleKeywordMatch,
        true)),
    OPTION_SUBTITLE_EXCLUDE_HEARING_IMPAIRED(createSetting(Mappers.BOOLEAN,
        SettingsControl::getSettings,
        Settings::isOptionSubtitleExcludeHearingImpaired,
        Settings::setOptionSubtitleExcludeHearingImpaired,
        true)),
    OPTIONS_SHOW_ONLY_FOUND(createSetting(Mappers.BOOLEAN,
        SettingsControl::getSettings,
        Settings::isOptionsShowOnlyFound,
        Settings::setOptionsShowOnlyFound,
        true)),
    OPTIONS_STOP_ON_SEARCH_ERROR(createSetting(Mappers.BOOLEAN,
        SettingsControl::getSettings,
        Settings::isOptionsStopOnSearchError,
        Settings::setOptionsStopOnSearchError,
        false)),
    OPTION_RECURSIVE(createSetting(Mappers.BOOLEAN,
        SettingsControl::getSettings,
        Settings::isOptionRecursive,
        Settings::setOptionRecursive,
        false)),
    PROCESS_EPISODE_SOURCE(createSettingEnum(
        SettingsControl::getSettings,
        Settings::getProcessEpisodeSource,
        Settings::setProcessEpisodeSource,
        SettingsProcessEpisodeSource.TVDB)),
    UPDATE_CHECK_PERIOD(createSettingEnum(
        SettingsControl::getSettings,
        Settings::getUpdateCheckPeriod,
        Settings::setUpdateCheckPeriod,
        UpdateCheckPeriod.WEEKLY)),
    USE_NIGHTLY(createSettingEnum(
        SettingsControl::getSettings,
        Settings::getUpdateType,
        Settings::setUpdateType,
        UpdateType.STABLE)),
    SUBTITLE_LANGUAGE(createSettingEnum(
        SettingsControl::getSettings,
        Settings::getSubtitleLanguage,
        Settings::setSubtitleLanguage,
        Language.DUTCH_FLEMISH)),

    // SCREEN SETTINGS
    SCREEN_HIDE_EPISODE(createSetting(Mappers.BOOLEAN,
        sCtr -> sCtr.settings.screenSettings,
        ScreenSettings::isHideEpisode,
        ScreenSettings::setHideEpisode,
        true)),
    SCREEN_HIDE_FILENAME(createSetting(Mappers.BOOLEAN,
        sCtr -> sCtr.settings.screenSettings,
        ScreenSettings::isHideFilename,
        ScreenSettings::setHideFilename,
        false)),
    SCREEN_HIDE_SEASON(createSetting(Mappers.BOOLEAN,
        sCtr -> sCtr.settings.screenSettings,
        ScreenSettings::isHideSeason,
        ScreenSettings::setHideSeason,
        true)),
    SCREEN_HIDE_TITLE(createSetting(Mappers.BOOLEAN,
        sCtr -> sCtr.settings.screenSettings,
        ScreenSettings::isHideTitle,
        ScreenSettings::setHideTitle,
        true)),
    SCREEN_HIDE_TYPE(createSetting(Mappers.BOOLEAN,
        sCtr -> sCtr.settings.screenSettings,
        ScreenSettings::isHideType,
        ScreenSettings::setHideType,
        true)),
    SCREEN_HIDE_W_I_P(createSetting(Mappers.BOOLEAN,
        sCtr -> sCtr.settings.screenSettings,
        ScreenSettings::isHideWIP,
        ScreenSettings::setHideWIP,
        true)),

    // PROXY SETTINGS
    GENERAL_PROXY_ENABLED(createSetting(Mappers.BOOLEAN,
        SettingsControl::getSettings,
        Settings::isGeneralProxyEnabled,
        Settings::setGeneralProxyEnabled,
        false)),
    GENERAL_PROXY_HOST(createSetting(Mappers.STRING,
        SettingsControl::getSettings,
        Settings::getGeneralProxyHost,
        Settings::setGeneralProxyHost,
        "")),
    GENERAL_PROXY_PORT(createSetting(Mappers.INT,
        SettingsControl::getSettings,
        Settings::getGeneralProxyPort,
        Settings::setGeneralProxyPort,
        80)),

    // LIBRARY SERIE
    EPISODE_LIBRARY_BACKUP_SUBTITLE_PATH(createSetting(Mappers.PATH,
        sCtr -> sCtr.settings.episodeLibrarySettings,
        LibrarySettings::getBackupSubtitlePath,
        LibrarySettings::setBackupSubtitlePath,
        null)),
    EPISODE_LIBRARY_BACKUP_SUBTITLE(createSetting(Mappers.BOOLEAN,
        sCtr -> sCtr.settings.episodeLibrarySettings,
        LibrarySettings::isBackupSubtitle,
        LibrarySettings::setBackupSubtitle,
        false)),
    EPISODE_LIBRARY_BACKUP_USE_WEBSITE_FILE_NAME(createSetting(Mappers.BOOLEAN,
        sCtr -> sCtr.settings.episodeLibrarySettings,
        LibrarySettings::isBackupUseWebsiteFileName,
        LibrarySettings::setBackupUseWebsiteFileName,
        false)),
    EPISODE_LIBRARY_ACTION(createSettingEnum(
        sCtr -> sCtr.settings.episodeLibrarySettings,
        LibrarySettings::getAction,
        LibrarySettings::setAction,
        LibraryActionType.NOTHING)),
    EPISODE_LIBRARY_USE_T_V_D_B_NAMING(createSetting(Mappers.BOOLEAN,
        sCtr -> sCtr.settings.episodeLibrarySettings,
        LibrarySettings::isUseTvdbNaming,
        LibrarySettings::setUseTvdbNaming,
        false)),
    EPISODE_LIBRARY_OTHER_FILE_ACTION(createSettingEnum(
        sCtr -> sCtr.settings.episodeLibrarySettings,
        LibrarySettings::getOtherFileAction,
        LibrarySettings::setOtherFileAction,
        LibraryOtherFileActionType.NOTHING)),
    EPISODE_LIBRARY_FOLDER(createSetting(Mappers.PATH,
        sCtr -> sCtr.settings.episodeLibrarySettings,
        LibrarySettings::getFolder,
        LibrarySettings::setFolder,
        null)),
    EPISODE_LIBRARY_FOLDER_STRUCTURE(createSetting(Mappers.STRING,
        sCtr -> sCtr.settings.episodeLibrarySettings,
        LibrarySettings::getFolderStructure,
        LibrarySettings::setFolderStructure,
        "")),
    EPISODE_LIBRARY_REMOVE_EMPTY_FOLDERS(createSetting(Mappers.BOOLEAN,
        sCtr -> sCtr.settings.episodeLibrarySettings,
        LibrarySettings::isRemoveEmptyFolders,
        LibrarySettings::setRemoveEmptyFolders,
        false)),
    EPISODE_LIBRARY_FILENAME_STRUCTURE(createSetting(Mappers.STRING,
        sCtr -> sCtr.settings.episodeLibrarySettings,
        LibrarySettings::getFilenameStructure,
        LibrarySettings::setFilenameStructure,
        "")),
    EPISODE_LIBRARY_REPLACE_SPACE(createSetting(Mappers.BOOLEAN,
        sCtr -> sCtr.settings.episodeLibrarySettings,
        LibrarySettings::isFilenameReplaceSpace,
        LibrarySettings::setFilenameReplaceSpace,
        false)),
    EPISODE_LIBRARY_REPLACING_SIGN(createSetting(Mappers.CHAR,
        sCtr -> sCtr.settings.episodeLibrarySettings,
        LibrarySettings::getFilenameReplacingSpaceChar,
        LibrarySettings::setFilenameReplacingSpaceChar,
        '_')),
    EPISODE_LIBRARY_FOLDER_REPLACE_SPACE(createSetting(Mappers.BOOLEAN,
        sCtr -> sCtr.settings.episodeLibrarySettings,
        LibrarySettings::isFolderReplaceSpace,
        LibrarySettings::setFolderReplaceSpace,
        false)),
    EPISODE_LIBRARY_FOLDER_REPLACING_SIGN(createSetting(Mappers.CHAR,
        sCtr -> sCtr.settings.episodeLibrarySettings,
        LibrarySettings::getFolderReplacingSpaceChar,
        LibrarySettings::setFolderReplacingSpaceChar,
        '_')),
    EPISODE_LIBRARY_INCLUDE_LANGUAGE_CODE(createSetting(Mappers.BOOLEAN,
        sCtr -> sCtr.settings.episodeLibrarySettings,
        LibrarySettings::isIncludeLanguageCode,
        LibrarySettings::setIncludeLanguageCode,
        false)),
    EPISODE_LIBRARY_LANG_CODE_MAPPING(createSetting(new Mapper<>(Language::name, Language::valueOf), Mappers.STRING,
        sCtr -> sCtr.settings.episodeLibrarySettings,
        LibrarySettings::getLangCodeMap)),

    // LIBRARY MOVIE
    MOVIE_LIBRARY_BACKUP_SUBTITLE_PATH(createSetting(Mappers.PATH,
        sCtr -> sCtr.settings.movieLibrarySettings,
        LibrarySettings::getBackupSubtitlePath,
        LibrarySettings::setBackupSubtitlePath,
        null)),

    MOVIE_LIBRARY_BACKUP_SUBTITLE(createSetting(Mappers.BOOLEAN,
        sCtr -> sCtr.settings.movieLibrarySettings,
        LibrarySettings::isBackupSubtitle,
        LibrarySettings::setBackupSubtitle,
        false)),

    MOVIE_LIBRARY_BACKUP_USE_WEBSITE_FILE_NAME(createSetting(Mappers.BOOLEAN,
        sCtr -> sCtr.settings.movieLibrarySettings,
        LibrarySettings::isBackupUseWebsiteFileName,
        LibrarySettings::setBackupUseWebsiteFileName,
        false)),

    MOVIE_LIBRARY_ACTION(createSettingEnum(
        sCtr -> sCtr.settings.movieLibrarySettings,
        LibrarySettings::getAction,
        LibrarySettings::setAction,
        LibraryActionType.NOTHING)),
    MOVIE_LIBRARY_USE_T_V_D_B_NAMING(createSetting(Mappers.BOOLEAN,
        sCtr -> sCtr.settings.movieLibrarySettings,
        LibrarySettings::isUseTvdbNaming,
        LibrarySettings::setUseTvdbNaming,
        false)),

    MOVIE_LIBRARY_OTHER_FILE_ACTION(createSettingEnum(
        sCtr -> sCtr.settings.movieLibrarySettings,
        LibrarySettings::getOtherFileAction,
        LibrarySettings::setOtherFileAction,
        LibraryOtherFileActionType.NOTHING)),

    MOVIE_LIBRARY_FOLDER(createSetting(Mappers.PATH,
        sCtr -> sCtr.settings.movieLibrarySettings,
        LibrarySettings::getFolder,
        LibrarySettings::setFolder,
        null)),

    MOVIE_LIBRARY_FOLDER_STRUCTURE(createSetting(Mappers.STRING,
        sCtr -> sCtr.settings.movieLibrarySettings,
        LibrarySettings::getFolderStructure,
        LibrarySettings::setFolderStructure,
        "")),

    MOVIE_LIBRARY_REMOVE_EMPTY_FOLDERS(createSetting(Mappers.BOOLEAN,
        sCtr -> sCtr.settings.movieLibrarySettings,
        LibrarySettings::isRemoveEmptyFolders,
        LibrarySettings::setRemoveEmptyFolders,
        false)),

    MOVIE_LIBRARY_FILENAME_STRUCTURE(createSetting(Mappers.STRING,
        sCtr -> sCtr.settings.movieLibrarySettings,
        LibrarySettings::getFilenameStructure,
        LibrarySettings::setFilenameStructure,
        "")),

    MOVIE_LIBRARY_REPLACE_SPACE(createSetting(Mappers.BOOLEAN,
        sCtr -> sCtr.settings.movieLibrarySettings,
        LibrarySettings::isFilenameReplaceSpace,
        LibrarySettings::setFilenameReplaceSpace,
        false)),

    MOVIE_LIBRARY_REPLACING_SIGN(createSetting(Mappers.CHAR,
        sCtr -> sCtr.settings.movieLibrarySettings,
        LibrarySettings::getFilenameReplacingSpaceChar,
        LibrarySettings::setFilenameReplacingSpaceChar,
        '_')),

    MOVIE_LIBRARY_FOLDER_REPLACE_SPACE(createSetting(Mappers.BOOLEAN,
        sCtr -> sCtr.settings.movieLibrarySettings,
        LibrarySettings::isFolderReplaceSpace,
        LibrarySettings::setFolderReplaceSpace,
        false)),

    MOVIE_LIBRARY_FOLDER_REPLACING_SIGN(createSetting(Mappers.CHAR,
        sCtr -> sCtr.settings.movieLibrarySettings,
        LibrarySettings::getFolderReplacingSpaceChar,
        LibrarySettings::setFolderReplacingSpaceChar,
        '_')),

    MOVIE_LIBRARY_INCLUDE_LANGUAGE_CODE(createSetting(Mappers.BOOLEAN,
        sCtr -> sCtr.settings.movieLibrarySettings,
        LibrarySettings::isIncludeLanguageCode,
        LibrarySettings::setIncludeLanguageCode,
        false)),

    MOVIE_LIBRARY_LANG_CODE_MAPPING(createSetting(new Mapper<>(Language::name, Language::valueOf), Mappers.STRING,
        sCtr -> sCtr.settings.movieLibrarySettings,
        LibrarySettings::getLangCodeMap)),

    // SERIE SOURCE SETTINGS
    LOGIN_ADDIC7ED_ENABLED(createSetting(Mappers.BOOLEAN,
        SettingsControl::getSettings,
        Settings::isLoginAddic7edEnabled,
        Settings::setLoginAddic7edEnabled,
        false)),

    LOGIN_ADDIC7ED_USERNAME(createSetting(Mappers.STRING,
        SettingsControl::getSettings,
        Settings::getLoginAddic7edUsername,
        Settings::setLoginAddic7edUsername,
        "")),

    LOGIN_ADDIC7ED_PASSWORD(createSetting(Mappers.STRING,
        SettingsControl::getSettings,
        Settings::getLoginAddic7edPassword,
        Settings::setLoginAddic7edPassword,
        "")),

    LOGIN_OPEN_SUBTITLES_ENABLED(createSetting(Mappers.BOOLEAN,
        SettingsControl::getSettings,
        Settings::isLoginOpenSubtitlesEnabled,
        Settings::setLoginOpenSubtitlesEnabled,
        false)),

    LOGIN_OPEN_SUBTITLES_USERNAME(createSetting(Mappers.STRING,
        SettingsControl::getSettings,
        Settings::getLoginOpenSubtitlesUsername,
        Settings::setLoginOpenSubtitlesUsername,
        "")),

    LOGIN_OPEN_SUBTITLES_PASSWORD(createSetting(Mappers.STRING,
        SettingsControl::getSettings,
        Settings::getLoginOpenSubtitlesPassword,
        Settings::setLoginOpenSubtitlesPassword,
        "")),

    SERIE_SOURCE_ADDIC7ED(createSetting(Mappers.BOOLEAN,
        SettingsControl::getSettings,
        Settings::isSerieSourceAddic7ed,
        Settings::setSerieSourceAddic7ed,
        true)),

    SERIE_SOURCE_ADDIC7ED_PROXY(createSetting(Mappers.BOOLEAN,
        SettingsControl::getSettings,
        Settings::isSerieSourceAddic7edProxy,
        Settings::setSerieSourceAddic7edProxy,
        true)),

    SERIE_SOURCE_LOCAL(createSetting(Mappers.BOOLEAN,
        SettingsControl::getSettings,
        Settings::isSerieSourceLocal,
        Settings::setSerieSourceLocal,
        false)),

    SERIE_SOURCE_OPENSUBTITLES(createSetting(Mappers.BOOLEAN,
        SettingsControl::getSettings,
        Settings::isSerieSourceOpensubtitles,
        Settings::setSerieSourceOpensubtitles,
        true)),

    SERIE_SOURCE_PODNAPISI(createSetting(Mappers.BOOLEAN,
        SettingsControl::getSettings,
        Settings::isSerieSourcePodnapisi,
        Settings::setSerieSourcePodnapisi,
        true)),

    SERIE_SOURCE_TV_SUBTITLES(createSetting(Mappers.BOOLEAN,
        SettingsControl::getSettings,
        Settings::isSerieSourceTvSubtitles,
        Settings::setSerieSourceTvSubtitles,
        true)),

    SERIE_SOURCE_SUBSCENE(createSetting(Mappers.BOOLEAN,
        SettingsControl::getSettings,
        Settings::isSerieSourceSubscene,
        Settings::setSerieSourceSubscene,
        true));

    private final BiConsumer<SettingsControl, Preferences> storeValueFunction;
    private final BiConsumer<SettingsControl, Preferences> loadValueFunction;

    SettingValue(SettingCommon settingsTyped) {
        this.storeValueFunction = (settings, prefs) -> settingsTyped.storeValueFunction.accept(settings, key, prefs);
        this.loadValueFunction = (settings, prefs) -> settingsTyped.loadValueFunction.accept(settings, key, prefs);
    }

    public String getKey() {
        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name());
    }

    public void store(SettingsControl settingsControl, Preferences preferences) {
        storeValueFunction.accept(settingsControl, preferences);
    }

    public void load(SettingsControl settingsControl, Preferences preferences) {
        loadValueFunction.accept(settingsControl, preferences);
    }

    public static void loadAll(SettingsControl settingsControl, Preferences preferences) {
        SettingValue.values().forEach(sv -> sv.load(settingsControl, preferences));
    }

    private static <T extends Enum<T>> Mapper<T> enumMapper(Class<T> type) {
        return new Mapper<>(Enum::name, v -> Enum.valueOf(type, v));
    }

    private record Mapper<T>(Function<T, String> toStringMapper, Function<String, T> toObjectMapper) {
    }

    private interface Mappers {
        Mapper<String> STRING = new Mapper<>(Function.identity(), Function.identity());
        Mapper<Character> CHAR = new Mapper<>(String::valueOf, s -> s.charAt(0));
        Mapper<Path> PATH = new Mapper<>(Path::toAbsolutePathAsString, Path::of);
        Mapper<Integer> INT = new Mapper<>(Object::toString, Integer::parseInt);
        Mapper<Boolean> BOOLEAN = new Mapper<>(Object::toString, Boolean::valueOf);
        Mapper<PathOrRegex> PATH_OR_REGEX = new Mapper<>(PathOrRegex::getValue, PathOrRegex::new);
    }

    private static <S, T> SettingTyped<S, T> createSetting(
        Mapper<T> mapper,
        Function<SettingsControl, S> rootElementFunction,
        Function<S, T> valueGetter,
        BiConsumer<S, T> valueSetter,
        T defaultValue) {

        return new SettingTyped<>(rootElementFunction, valueGetter, valueSetter, mapper, defaultValue);
    }

    private static <S, T> SettingTyped<S, T> createSetting(
        Mapper<T> mapper,
        Function<SettingsControl, S> rootElementFunction,
        Function<S, Collection<T>> collectionGetter) {

        return new SettingTyped<>(mapper, rootElementFunction, collectionGetter);
    }

    private static <S, T extends Enum<T>> SettingTyped<S, T> createSettingEnum(
        Function<SettingsControl, S> rootElementFunction,
        Function<S, T> valueGetter,
        BiConsumer<S, T> valueSetter,
        T defaultValue) {

        return new SettingTyped<>(rootElementFunction, valueGetter, valueSetter, new Mapper<>(Enum::name,
            v -> (T) Enum.valueOf(defaultValue.getClass(), v)), defaultValue);
    }

    private static <S, T extends Enum<T>> SettingTyped<S, T> createSettingEnum(
        Function<SettingsControl, S> rootElementFunction,
        Function<S, Collection<T>> collectionGetter,
        Class<T> type) {

        return new SettingTyped<>(new Mapper<>(Enum::name, v -> Enum.valueOf(type, v)), rootElementFunction,
            collectionGetter);
    }

    private static <S, K, V> SettingMapTyped<S, K, V> createSetting(
        Mapper<K> keyMapper,
        Mapper<V> valueMapper,
        Function<SettingsControl, S> rootElementFunction,
        Function<S, Map<K, V>> mapGetter) {

        return new SettingMapTyped<>(rootElementFunction, mapGetter, keyMapper, valueMapper);
    }

    private static class SettingTyped<S, T> extends SettingCommon {

        // SINGLE VALUE

        SettingTyped(
            Function<SettingsControl, S> rootElementFunction,
            Function<S, T> valueGetter,
            BiConsumer<S, T> valueSetter,
            Mapper<T> mapper,
            T defaultValue) {
            super(
                (settingsControl, key, preferences) -> {
                    T value = valueGetter.apply(rootElementFunction.apply(settingsControl));
                    if (!Objects.equal(value, defaultValue) &&
                        !(value instanceof String text && text.isEmpty())) {
                        preferences.put(key, mapper.toStringMapper.apply(value));
                    }
                },
                (settingsControl, key, preferences) -> valueSetter.accept(rootElementFunction.apply(settingsControl),
                    preferences.computeIfPresent(key, mapper.toObjectMapper, defaultValue)));
        }

//        SettingTyped(
//            TriConsumer<Preferences, String, T> preferencesSetter,
//            TriFunction<Preferences, String, T, T> preferencesGetter,
//            Function<SettingsControl, S> rootElementFunction,
//            Function<S, T> valueGetter,
//            BiConsumer<S, T> valueSetter,
//            T defaultValue) {
//            super(
//                (settingsControl, key, preferences) -> {
//                    T value = valueGetter.apply(rootElementFunction.apply(settingsControl));
//                    if (!Objects.equal(value, defaultValue) &&
//                        !(value instanceof String text && text.isEmpty())) {
//                        preferencesSetter.accept(preferences, key, value);
//                    }
//                },
//                (settingsControl, key, preferences) -> valueSetter.accept(rootElementFunction.apply(settingsControl),
//                    preferencesGetter.apply(preferences, key, defaultValue)));
//        }

        // COLLECTION VALUE

        SettingTyped(
            Mapper<T> mapper,
            Function<SettingsControl, S> rootElementFunction,
            Function<S, Collection<T>> collectionGetter) {
            super(
                (settingsControl, key, preferences) -> {
                    AtomicInteger i = new AtomicInteger(-1);
                    collectionGetter.apply(rootElementFunction.apply(settingsControl)).forEach(
                        value -> preferences.put(key + i.incrementAndGet(), mapper.toStringMapper.apply(value)));
                    if (i.get() > -1) {
                        preferences.putInt(key + "Size", i.get() + 1);
                    }
                },
                (settingsControl, key, preferences) -> {
                    int numberOfItems = preferences.getInt(key + "Size", 0);
                    S rootElement = rootElementFunction.apply(settingsControl);
                    collectionGetter.apply(rootElement).clear();
                    IntStream.range(0, numberOfItems)
                        .forEach(i -> collectionGetter.apply(rootElement).add(
                            mapper.toObjectMapper.apply(preferences.get(key + i, ""))));
                });
        }
    }

    private static class SettingMapTyped<S, K, V> extends SettingCommon {

        SettingMapTyped(Function<SettingsControl, S> rootElementFunction, Function<S, Map<K, V>> mapGetter,
            Mapper<K> keyMapper, Mapper<V> valueMapper) {

            super((settingsControl, key, preferences) -> {
                    AtomicInteger i = new AtomicInteger(-1);
                    mapToPreferences(rootElementFunction.apply(settingsControl), mapGetter,
                        (k, v) -> {
                            int idx = i.incrementAndGet();
                            preferences.put(getKeyString(key, idx), keyMapper.toStringMapper.apply(k));
                            preferences.put(getValueString(key, idx), valueMapper.toStringMapper.apply(v));
                        });
                    if (i.get() > -1) {
                        preferences.putInt(key + "Size", i.get() + 1);
                    }
                },
                (settingsControl, key, preferences) -> {
                    int numberOfItems = preferences.getInt(key + "Size", 0);
                    IntStream.range(0, numberOfItems).forEach(idx ->
                        preferencesToMap(rootElementFunction.apply(settingsControl), mapGetter,
                            keyMapper.toObjectMapper.apply(preferences.get(getKeyString(key, idx), "")),
                            valueMapper.toObjectMapper.apply(preferences.get(getValueString(key, idx), "")))
                    );

                });
        }

        private static <S, K, V> void mapToPreferences(S rootElement, Function<S, Map<K, V>> mapGetter,
            BiConsumer<K, V> consumer) {
            mapGetter.apply(rootElement).forEach(consumer);
        }

        private static <S, K, V> void preferencesToMap(S rootElement, Function<S, Map<K, V>> mapGetter,
            K key, V value) {
            mapGetter.apply(rootElement).put(key, value);
        }

        private static String getKeyString(String key, int idx) {
            return key + "-key" + idx;
        }

        private static String getValueString(String key, int idx) {
            return key + "-value" + idx;
        }
    }

    private abstract static class SettingCommon {
        @val TriConsumer<SettingsControl, String, Preferences> storeValueFunction;
        @val TriConsumer<SettingsControl, String, Preferences> loadValueFunction;

        SettingCommon(
            TriConsumer<SettingsControl, String, Preferences> storeValueFunction,
            TriConsumer<SettingsControl, String, Preferences> loadValueFunction) {
            this.storeValueFunction = storeValueFunction;
            this.loadValueFunction = loadValueFunction;
        }
    }
}
