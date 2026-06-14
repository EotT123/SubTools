package org.lodder.subtools.multisubdownloader.settings;

import java.nio.file.Path;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.prefs.Preferences;
import java.util.stream.IntStream;

import com.google.common.base.CaseFormat;
import com.google.common.base.Objects;
import extensions.java.nio.file.Path.PathExt;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
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

@NullMarked
public enum SettingValue {

    // SETTINGS
    SETTINGS_VERSION(createSetting(Mappers.INT,
        Settings::getSettingsVersion,
        Settings::setSettingsVersion,
        0)),
    LAST_OUTPUT_DIR(createSetting(Mappers.PATH,
        _ -> MemoryFolderChooser.getInstance().memory,
        Settings::setLastOutputDir,
        Path.of(""))),

    GENERAL_DEFAULT_INCOMING_FOLDER(createSetting(Mappers.PATH,
        Settings::getDefaultIncomingFolders)),
    LOCAL_SUBTITLES_SOURCES_FOLDERS(createSetting(Mappers.PATH,
        Settings::getLocalSourcesFolders)),
    EXCLUDE_ITEM(createSetting(Mappers.PATH_OR_REGEX,
        Settings::getExcludeList)),
    DEFAULT_SELECTION_QUALITY(createSetting(enumMapper(VideoPatterns.Source.class),
        Settings::getOptionsDefaultSelectionQualityList)),
    DEFAULT_SELECTION_QUALITY_ENABLED(createSetting(Mappers.BOOLEAN,
        Settings::isOptionsDefaultSelection,
        Settings::setOptionsDefaultSelection,
        false)),

    OPTIONS_LANGUAGE(createSettingEnum(
        Settings::getLanguage,
        Settings::setLanguage,
        Language.ENGLISH)),
    OPTIONS_ALWAYS_CONFIRM(createSetting(Mappers.BOOLEAN,
        Settings::isOptionsAlwaysConfirm,
        Settings::setOptionsAlwaysConfirm,
        false)),
    OPTIONS_CONFIRM_MAPPING(createSetting(Mappers.BOOLEAN,
        Settings::isOptionsConfirmProviderMapping,
        Settings::setOptionsConfirmProviderMapping,
        true)),
    OPTIONS_MIN_AUTOMATIC_SELECTION(createSetting(Mappers.BOOLEAN,
        Settings::isOptionsMinAutomaticSelection,
        Settings::setOptionsMinAutomaticSelection,
        false)),
    OPTIONS_MIN_AUTOMATIC_SELECTION_VALUE(createSetting(Mappers.INT,
        Settings::getOptionsMinAutomaticSelectionValue,
        Settings::setOptionsMinAutomaticSelectionValue,
        0)),
    OPTION_SUBTITLE_EXACT_MATCH(createSetting(Mappers.BOOLEAN,
        Settings::isOptionSubtitleExactMatch,
        Settings::setOptionSubtitleExactMatch,
        true)),
    OPTION_SUBTITLE_KEYWORD_MATCH(createSetting(Mappers.BOOLEAN,
        Settings::isOptionSubtitleKeywordMatch,
        Settings::setOptionSubtitleKeywordMatch,
        true)),
    OPTION_SUBTITLE_EXCLUDE_HEARING_IMPAIRED(createSetting(Mappers.BOOLEAN,
        Settings::isOptionSubtitleExcludeHearingImpaired,
        Settings::setOptionSubtitleExcludeHearingImpaired,
        true)),
    OPTIONS_SHOW_ONLY_FOUND(createSetting(Mappers.BOOLEAN,
        Settings::isOptionsShowOnlyFound,
        Settings::setOptionsShowOnlyFound,
        true)),
    OPTIONS_STOP_ON_SEARCH_ERROR(createSetting(Mappers.BOOLEAN,
        Settings::isOptionsStopOnSearchError,
        Settings::setOptionsStopOnSearchError,
        false)),
    OPTION_RECURSIVE(createSetting(Mappers.BOOLEAN,
        Settings::isOptionRecursive,
        Settings::setOptionRecursive,
        false)),
    PROCESS_EPISODE_SOURCE(createSettingEnum(
        Settings::getProcessEpisodeSource,
        Settings::setProcessEpisodeSource,
        SettingsProcessEpisodeSource.TVDB)),
    UPDATE_CHECK_PERIOD(createSettingEnum(
        Settings::getUpdateCheckPeriod,
        Settings::setUpdateCheckPeriod,
        UpdateCheckPeriod.WEEKLY)),
    USE_NIGHTLY(createSettingEnum(
        Settings::getUpdateType,
        Settings::setUpdateType,
        UpdateType.STABLE)),
    SUBTITLE_LANGUAGE(createSettingEnum(
        Settings::getSubtitleLanguage,
        Settings::setSubtitleLanguage,
        Language.DUTCH_FLEMISH)),

    // SCREEN SETTINGS
    SCREEN_HIDE_EPISODE(createSetting(Mappers.BOOLEAN,
        Settings::getScreenSettings,
        ScreenSettings::isHideEpisode,
        ScreenSettings::setHideEpisode,
        true)),
    SCREEN_HIDE_FILENAME(createSetting(Mappers.BOOLEAN,
        Settings::getScreenSettings,
        ScreenSettings::isHideFilename,
        ScreenSettings::setHideFilename,
        false)),
    SCREEN_HIDE_SEASON(createSetting(Mappers.BOOLEAN,
        Settings::getScreenSettings,
        ScreenSettings::isHideSeason,
        ScreenSettings::setHideSeason,
        true)),
    SCREEN_HIDE_TITLE(createSetting(Mappers.BOOLEAN,
        Settings::getScreenSettings,
        ScreenSettings::isHideTitle,
        ScreenSettings::setHideTitle,
        true)),
    SCREEN_HIDE_TYPE(createSetting(Mappers.BOOLEAN,
        Settings::getScreenSettings,
        ScreenSettings::isHideType,
        ScreenSettings::setHideType,
        true)),
    SCREEN_HIDE_W_I_P(createSetting(Mappers.BOOLEAN,
        Settings::getScreenSettings,
        ScreenSettings::isHideWIP,
        ScreenSettings::setHideWIP,
        true)),

    // PROXY SETTINGS
    GENERAL_PROXY_ENABLED(createSetting(Mappers.BOOLEAN,
        Settings::isGeneralProxyEnabled,
        Settings::setGeneralProxyEnabled,
        false)),
    GENERAL_PROXY_HOST(createSetting(Mappers.STRING,
        Settings::getGeneralProxyHost,
        Settings::setGeneralProxyHost,
        "")),
    GENERAL_PROXY_PORT(createSetting(Mappers.INT,
        Settings::getGeneralProxyPort,
        Settings::setGeneralProxyPort,
        80)),

    // LIBRARY SERIE
    EPISODE_LIBRARY_BACKUP_SUBTITLE_PATH(createSetting(Mappers.PATH,
        Settings::getEpisodeLibrarySettings,
        LibrarySettings::getBackupSubtitlePath,
        LibrarySettings::setBackupSubtitlePath,
        null)),
    EPISODE_LIBRARY_BACKUP_USE_WEBSITE_FILE_NAME(createSetting(Mappers.BOOLEAN,
        Settings::getEpisodeLibrarySettings,
        LibrarySettings::isBackupUseWebsiteFileName,
        LibrarySettings::setBackupUseWebsiteFileName,
        false)),
    EPISODE_LIBRARY_ACTION(createSettingEnum(
        Settings::getEpisodeLibrarySettings,
        LibrarySettings::getAction,
        LibrarySettings::setAction,
        LibraryActionType.NOTHING)),
    EPISODE_LIBRARY_USE_T_V_D_B_NAMING(createSetting(Mappers.BOOLEAN,
        Settings::getEpisodeLibrarySettings,
        LibrarySettings::isUseTvdbNaming,
        LibrarySettings::setUseTvdbNaming,
        false)),
    EPISODE_LIBRARY_OTHER_FILE_ACTION(createSettingEnum(
        Settings::getEpisodeLibrarySettings,
        LibrarySettings::getOtherFileAction,
        LibrarySettings::setOtherFileAction,
        LibraryOtherFileActionType.NOTHING)),
    EPISODE_LIBRARY_FOLDER(createSetting(Mappers.PATH,
        Settings::getEpisodeLibrarySettings,
        LibrarySettings::getFolder,
        LibrarySettings::setFolder,
        null)),
    EPISODE_LIBRARY_FOLDER_STRUCTURE(createSetting(Mappers.STRING,
        Settings::getEpisodeLibrarySettings,
        LibrarySettings::getFolderStructure,
        LibrarySettings::setFolderStructure,
        "")),
    EPISODE_LIBRARY_REMOVE_EMPTY_FOLDERS(createSetting(Mappers.BOOLEAN,
        Settings::getEpisodeLibrarySettings,
        LibrarySettings::isRemoveEmptyFolders,
        LibrarySettings::setRemoveEmptyFolders,
        false)),
    EPISODE_LIBRARY_FILENAME_STRUCTURE(createSetting(Mappers.STRING,
        Settings::getEpisodeLibrarySettings,
        LibrarySettings::getFilenameStructure,
        LibrarySettings::setFilenameStructure,
        "")),
    EPISODE_LIBRARY_REPLACE_SPACE(createSetting(Mappers.BOOLEAN,
        Settings::getEpisodeLibrarySettings,
        LibrarySettings::isFilenameReplaceSpace,
        LibrarySettings::setFilenameReplaceSpace,
        false)),
    EPISODE_LIBRARY_REPLACING_SIGN(createSetting(Mappers.CHAR,
        Settings::getEpisodeLibrarySettings,
        LibrarySettings::getFilenameReplacingSpaceChar,
        LibrarySettings::setFilenameReplacingSpaceChar,
        '_')),
    EPISODE_LIBRARY_FOLDER_REPLACING_SIGN(createSetting(Mappers.CHAR,
        Settings::getEpisodeLibrarySettings,
        LibrarySettings::getFolderReplacingSpaceChar,
        LibrarySettings::setFolderReplacingSpaceChar,
        '_')),
    EPISODE_LIBRARY_INCLUDE_LANGUAGE_CODE(createSetting(Mappers.BOOLEAN,
        Settings::getEpisodeLibrarySettings,
        LibrarySettings::isIncludeLanguageCode,
        LibrarySettings::setIncludeLanguageCode,
        false)),
    EPISODE_LIBRARY_LANG_CODE_MAPPING(createSetting(new Mapper<>(Language::name, Language::valueOf), Mappers.STRING,
        Settings::getEpisodeLibrarySettings,
        LibrarySettings::getLangCodeMap)),

    // LIBRARY MOVIE
    MOVIE_LIBRARY_BACKUP_SUBTITLE_PATH(createSetting(Mappers.PATH,
        Settings::getMovieLibrarySettings,
        LibrarySettings::getBackupSubtitlePath,
        LibrarySettings::setBackupSubtitlePath,
        null)),

    MOVIE_LIBRARY_BACKUP_USE_WEBSITE_FILE_NAME(createSetting(Mappers.BOOLEAN,
        Settings::getMovieLibrarySettings,
        LibrarySettings::isBackupUseWebsiteFileName,
        LibrarySettings::setBackupUseWebsiteFileName,
        false)),

    MOVIE_LIBRARY_ACTION(createSettingEnum(
        Settings::getMovieLibrarySettings,
        LibrarySettings::getAction,
        LibrarySettings::setAction,
        LibraryActionType.NOTHING)),
    MOVIE_LIBRARY_USE_T_V_D_B_NAMING(createSetting(Mappers.BOOLEAN,
        Settings::getMovieLibrarySettings,
        LibrarySettings::isUseTvdbNaming,
        LibrarySettings::setUseTvdbNaming,
        false)),

    MOVIE_LIBRARY_OTHER_FILE_ACTION(createSettingEnum(
        Settings::getMovieLibrarySettings,
        LibrarySettings::getOtherFileAction,
        LibrarySettings::setOtherFileAction,
        LibraryOtherFileActionType.NOTHING)),

    MOVIE_LIBRARY_FOLDER(createSetting(Mappers.PATH,
        Settings::getMovieLibrarySettings,
        LibrarySettings::getFolder,
        LibrarySettings::setFolder,
        null)),

    MOVIE_LIBRARY_FOLDER_STRUCTURE(createSetting(Mappers.STRING,
        Settings::getMovieLibrarySettings,
        LibrarySettings::getFolderStructure,
        LibrarySettings::setFolderStructure,
        "")),

    MOVIE_LIBRARY_REMOVE_EMPTY_FOLDERS(createSetting(Mappers.BOOLEAN,
        Settings::getMovieLibrarySettings,
        LibrarySettings::isRemoveEmptyFolders,
        LibrarySettings::setRemoveEmptyFolders,
        false)),

    MOVIE_LIBRARY_FILENAME_STRUCTURE(createSetting(Mappers.STRING,
        Settings::getMovieLibrarySettings,
        LibrarySettings::getFilenameStructure,
        LibrarySettings::setFilenameStructure,
        "")),

    MOVIE_LIBRARY_REPLACE_SPACE(createSetting(Mappers.BOOLEAN,
        Settings::getMovieLibrarySettings,
        LibrarySettings::isFilenameReplaceSpace,
        LibrarySettings::setFilenameReplaceSpace,
        false)),

    MOVIE_LIBRARY_REPLACING_SIGN(createSetting(Mappers.CHAR,
        Settings::getMovieLibrarySettings,
        LibrarySettings::getFilenameReplacingSpaceChar,
        LibrarySettings::setFilenameReplacingSpaceChar,
        '_')),

    MOVIE_LIBRARY_FOLDER_REPLACING_SIGN(createSetting(Mappers.CHAR,
        Settings::getMovieLibrarySettings,
        LibrarySettings::getFolderReplacingSpaceChar,
        LibrarySettings::setFolderReplacingSpaceChar,
        '_')),

    MOVIE_LIBRARY_INCLUDE_LANGUAGE_CODE(createSetting(Mappers.BOOLEAN,
        Settings::getMovieLibrarySettings,
        LibrarySettings::isIncludeLanguageCode,
        LibrarySettings::setIncludeLanguageCode,
        false)),

    MOVIE_LIBRARY_LANG_CODE_MAPPING(createSetting(new Mapper<>(Language::name, Language::valueOf), Mappers.STRING,
        Settings::getMovieLibrarySettings,
        LibrarySettings::getLangCodeMap)),

    // SERIE SOURCE SETTINGS
    LOGIN_ADDIC7ED_ENABLED(createSetting(Mappers.BOOLEAN,
        Settings::isLoginAddic7edEnabled,
        Settings::setLoginAddic7edEnabled,
        false)),

    LOGIN_ADDIC7ED_USERNAME(createSetting(Mappers.STRING,
        Settings::getLoginAddic7edUsername,
        Settings::setLoginAddic7edUsername,
        "")),

    LOGIN_ADDIC7ED_PASSWORD(createSetting(Mappers.STRING,
        Settings::getLoginAddic7edPassword,
        Settings::setLoginAddic7edPassword,
        "")),

    LOGIN_OPEN_SUBTITLES_ENABLED(createSetting(Mappers.BOOLEAN,
        Settings::isLoginOpenSubtitlesEnabled,
        Settings::setLoginOpenSubtitlesEnabled,
        false)),

    LOGIN_OPEN_SUBTITLES_USERNAME(createSetting(Mappers.STRING,
        Settings::getLoginOpenSubtitlesUsername,
        Settings::setLoginOpenSubtitlesUsername,
        "")),

    LOGIN_OPEN_SUBTITLES_PASSWORD(createSetting(Mappers.STRING,
        Settings::getLoginOpenSubtitlesPassword,
        Settings::setLoginOpenSubtitlesPassword,
        "")),

    SERIE_SOURCE_ADDIC7ED(createSetting(Mappers.BOOLEAN,
        Settings::isSerieSourceAddic7ed,
        Settings::setSerieSourceAddic7ed,
        true)),

    SERIE_SOURCE_ADDIC7ED_PROXY(createSetting(Mappers.BOOLEAN,
        Settings::isSerieSourceAddic7edProxy,
        Settings::setSerieSourceAddic7edProxy,
        true)),

    SERIE_SOURCE_LOCAL(createSetting(Mappers.BOOLEAN,
        Settings::isSerieSourceLocal,
        Settings::setSerieSourceLocal,
        false)),

    SERIE_SOURCE_OPENSUBTITLES(createSetting(Mappers.BOOLEAN,
        Settings::isSerieSourceOpensubtitles,
        Settings::setSerieSourceOpensubtitles,
        true)),

    SERIE_SOURCE_PODNAPISI(createSetting(Mappers.BOOLEAN,
        Settings::isSerieSourcePodnapisi,
        Settings::setSerieSourcePodnapisi,
        true)),

    SERIE_SOURCE_TV_SUBTITLES(createSetting(Mappers.BOOLEAN,
        Settings::isSerieSourceTvSubtitles,
        Settings::setSerieSourceTvSubtitles,
        true)),

    SERIE_SOURCE_SUBDL(createSetting(Mappers.BOOLEAN,
        Settings::isSerieSourceSubdl,
        Settings::setSerieSourceSubdl,
        true)),

    SERIE_SOURCE_SUBSCENE(createSetting(Mappers.BOOLEAN,
        Settings::isSerieSourceSubscene,
        Settings::setSerieSourceSubscene,
        true));

    private final Consumer<Preferences> storeValueFunction;
    private final Consumer<Preferences> loadValueFunction;

    SettingValue(SettingCommon settingsTyped) {
        this.storeValueFunction = prefs -> settingsTyped.storeValueFunction.accept(key, prefs);
        this.loadValueFunction = prefs -> settingsTyped.loadValueFunction.accept(key, prefs);
    }

    public String getKey() {
        return CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, name());
    }

    public void store(Preferences preferences) {
        storeValueFunction.accept(preferences);
    }

    public void load(Preferences preferences) {
        loadValueFunction.accept(preferences);
    }

    public static void loadAll(Preferences preferences) {
        SettingValue.values().forEach(sv -> sv.load(preferences));
    }

    private static <T extends Enum<T>> Mapper<T> enumMapper(Class<T> type) {
        return new Mapper<>(Enum::name, v -> Enum.valueOf(type, v));
    }

    @NullMarked
    private record Mapper<T extends @Nullable Object>(Function<T, String> toStringMapper,
        Function<String, T> toObjectMapper) {
    }

    @NullMarked
    private interface Mappers {
        Mapper<String> STRING = new Mapper<>(Function.identity(), Function.identity());
        Mapper<Character> CHAR = new Mapper<>(String::valueOf, s -> s.charAt(0));
        Mapper<Path> PATH = new Mapper<>(PathExt::toAbsolutePathAsString, Path::of);
        Mapper<Integer> INT = new Mapper<>(Object::toString, Integer::parseInt);
        Mapper<Boolean> BOOLEAN = new Mapper<>(Object::toString, Boolean::valueOf);
        Mapper<PathOrRegex> PATH_OR_REGEX = new Mapper<>(PathOrRegex::getValue, PathOrRegex::new);
    }

    private static <T extends @Nullable Object> SettingTyped<Settings, T> createSetting(
        Mapper<T> mapper,
        Function<Settings, T> valueGetter,
        BiConsumer<Settings, T> valueSetter,
        T defaultValue) {

        return createSetting(mapper, Function.identity(), valueGetter, valueSetter, defaultValue);
    }

    private static <S, T extends @Nullable Object> SettingTyped<S, T> createSetting(
        Mapper<T> mapper,
        Function<Settings, S> rootElementFunction,
        Function<S, T> valueGetter,
        BiConsumer<S, T> valueSetter,
        T defaultValue) {

        return new SettingTyped<>(rootElementFunction, valueGetter, valueSetter, mapper, defaultValue);
    }

    private static <T> SettingTyped<Settings, T> createSetting(
        Mapper<T> mapper,
        Function<Settings, Collection<T>> collectionGetter) {

        return createSetting(mapper, Function.identity(), collectionGetter);
    }

    private static <S, T> SettingTyped<S, T> createSetting(
        Mapper<T> mapper,
        Function<Settings, S> rootElementFunction,
        Function<S, Collection<T>> collectionGetter) {

        return new SettingTyped<>(mapper, rootElementFunction, collectionGetter);
    }

    private static <T extends @Nullable Enum<T>> SettingTyped<Settings, T> createSettingEnum(
        Function<Settings, T> valueGetter,
        BiConsumer<Settings, T> valueSetter,
        T defaultValue) {

        return createSettingEnum(Function.identity(), valueGetter, valueSetter, defaultValue);
    }

    private static <S, T extends @Nullable Enum<T>> SettingTyped<S, T> createSettingEnum(
        Function<Settings, S> rootElementFunction,
        Function<S, T> valueGetter,
        BiConsumer<S, T> valueSetter,
        T defaultValue) {

        return new SettingTyped<>(rootElementFunction, valueGetter, valueSetter, new Mapper<>(Enum::name,
            v -> (T) Enum.valueOf(defaultValue.getClass(), v)), defaultValue);
    }

    private static <T extends Enum<T>> SettingTyped<Settings, T> createSettingEnum(
        Function<Settings, Collection<T>> collectionGetter,
        Class<T> type) {

        return createSettingEnum(Function.identity(), collectionGetter, type);
    }

    private static <S, T extends Enum<T>> SettingTyped<S, T> createSettingEnum(
        Function<Settings, S> rootElementFunction,
        Function<S, Collection<T>> collectionGetter,
        Class<T> type) {

        return new SettingTyped<>(new Mapper<>(Enum::name, v -> Enum.valueOf(type, v)), rootElementFunction,
            collectionGetter);
    }

    private static <K, V> SettingMapTyped<Settings, K, V> createSetting(
        Mapper<K> keyMapper,
        Mapper<V> valueMapper,
        Function<Settings, Map<K, V>> mapGetter) {

        return createSetting(keyMapper, valueMapper, Function.identity(), mapGetter);
    }

    private static <S, K, V> SettingMapTyped<S, K, V> createSetting(
        Mapper<K> keyMapper,
        Mapper<V> valueMapper,
        Function<Settings, S> rootElementFunction,
        Function<S, Map<K, V>> mapGetter) {

        return new SettingMapTyped<>(rootElementFunction, mapGetter, keyMapper, valueMapper);
    }

    @NullMarked
    private static class SettingTyped<S, T extends @Nullable Object> extends SettingCommon {

        // SINGLE VALUE

        SettingTyped(
            Function<Settings, S> rootElementFunction,
            Function<S, T> valueGetter,
            BiConsumer<S, T> valueSetter,
            Mapper<T> mapper,
            T defaultValue) {
            super(
                (key, preferences) -> {
                    T value = valueGetter.apply(rootElementFunction.apply(SettingsControl.settings));
                    if (!Objects.equal(value, defaultValue) &&
                        !(value instanceof String text && text.isEmpty())) {
                        preferences.put(key, mapper.toStringMapper.apply(value));
                    }
                },
                (key, preferences) -> valueSetter.accept(rootElementFunction.apply(SettingsControl.settings),
                    preferences.computeIfPresent(key, mapper.toObjectMapper, defaultValue)));
        }

//        SettingTyped(
//            TriConsumer<Preferences, String, T> preferencesSetter,
//            TriFunction<Preferences, String, T, T> preferencesGetter,
//            Function<Settings, S> rootElementFunction,
//            Function<S, T> valueGetter,
//            BiConsumer<S, T> valueSetter,
//            T defaultValue) {
//            super(
//                (Settings, key, preferences) -> {
//                    T value = valueGetter.apply(rootElementFunction.apply(Settings));
//                    if (!Objects.equal(value, defaultValue) &&
//                        !(value instanceof String text && text.isEmpty())) {
//                        preferencesSetter.accept(preferences, key, value);
//                    }
//                },
//                (Settings, key, preferences) -> valueSetter.accept(rootElementFunction.apply(Settings),
//                    preferencesGetter.apply(preferences, key, defaultValue)));
//        }

        // COLLECTION VALUE

        SettingTyped(
            Mapper<T> mapper,
            Function<Settings, S> rootElementFunction,
            Function<S, Collection<T>> collectionGetter) {
            super(
                (key, preferences) -> {
                    AtomicInteger i = new AtomicInteger(-1);
                    collectionGetter.apply(rootElementFunction.apply(SettingsControl.settings)).forEach(
                        value -> preferences.put(key + i.incrementAndGet(), mapper.toStringMapper.apply(value)));
                    if (i.get() > -1) {
                        preferences.putInt(key + "Size", i.get() + 1);
                    }
                },
                (key, preferences) -> {
                    int numberOfItems = preferences.getInt(key + "Size", 0);
                    S rootElement = rootElementFunction.apply(SettingsControl.settings);
                    collectionGetter.apply(rootElement).clear();
                    IntStream.range(0, numberOfItems)
                        .forEach(i -> collectionGetter.apply(rootElement).add(
                            mapper.toObjectMapper.apply(preferences.get(key + i, ""))));
                });
        }
    }

    @NullMarked
    private static class SettingMapTyped<S, K, V> extends SettingCommon {

        SettingMapTyped(Function<Settings, S> rootElementFunction, Function<S, Map<K, V>> mapGetter,
            Mapper<K> keyMapper, Mapper<V> valueMapper) {

            super((key, preferences) -> {
                    AtomicInteger i = new AtomicInteger(-1);
                    mapToPreferences(rootElementFunction.apply(SettingsControl.settings), mapGetter,
                        (k, v) -> {
                            int idx = i.incrementAndGet();
                            preferences.put(getKeyString(key, idx), keyMapper.toStringMapper.apply(k));
                            preferences.put(getValueString(key, idx), valueMapper.toStringMapper.apply(v));
                        });
                    if (i.get() > -1) {
                        preferences.putInt(key + "Size", i.get() + 1);
                    }
                },
                (key, preferences) -> {
                    int numberOfItems = preferences.getInt(key + "Size", 0);
                    IntStream.range(0, numberOfItems).forEach(idx ->
                        preferencesToMap(rootElementFunction.apply(SettingsControl.settings), mapGetter,
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
        @val BiConsumer<String, Preferences> storeValueFunction;
        @val BiConsumer<String, Preferences> loadValueFunction;

        SettingCommon(
            BiConsumer<String, Preferences> storeValueFunction,
            BiConsumer<String, Preferences> loadValueFunction) {
            this.storeValueFunction = storeValueFunction;
            this.loadValueFunction = loadValueFunction;
        }
    }
}
