package org.lodder.subtools.multisubdownloader.lib.library;

import java.nio.file.Path;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.settings.model.LibrarySettings;
import org.lodder.subtools.multisubdownloader.settings.model.structure.MovieStructureTag;
import org.lodder.subtools.multisubdownloader.settings.model.structure.SerieStructureTag;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.data.tvdb.TvdbAdapter;
import org.lodder.subtools.sublibrary.model.MovieRelease;
import org.lodder.subtools.sublibrary.model.MovieReleaseWithPath;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.ReleaseWithPath;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.TvRelease;
import org.lodder.subtools.sublibrary.model.TvReleaseWithPath;
import org.lodder.subtools.sublibrary.userinteraction.UserInteractionHandler;

@NullMarked
public final class FilenameLibraryBuilder extends LibraryBuilder {

    private final String structure;
    private final boolean replaceSpace;
    private final Character replacingSpaceChar;
    private final boolean includeLanguageCode;
    private final Map<Language, String> languageTags;
    private final boolean rename;

    public FilenameLibraryBuilder(String structure, boolean replaceSpace, char replacingSpaceChar,
        boolean includeLanguageCode, Map<Language, String> languageTags, @Nullable TvdbAdapter tvdbAdapter=null,
        boolean rename) {
        super(tvdbAdapter);
        this.structure = structure;
        this.replaceSpace = replaceSpace;
        this.replacingSpaceChar = replacingSpaceChar;
        this.includeLanguageCode = includeLanguageCode;
        this.languageTags = languageTags;
        this.rename = rename;
    }

    public static FilenameLibraryBuilder fromSettings(LibrarySettings libSettings,
        UserInteractionHandler userInteractionHandler) {
        return new FilenameLibraryBuilder(
            structure:libSettings.folderStructure,
            replaceSpace:libSettings.folderReplaceSpace,
            replacingSpaceChar:libSettings.folderReplacingSpaceChar,
            includeLanguageCode:libSettings.includeLanguageCode,
            languageTags:libSettings.langCodeMap,
            tvdbAdapter:libSettings.useTvdbNaming ? TvdbAdapter.getInstance(userInteractionHandler) : null,
            rename:libSettings.hasAnyLibraryAction(LibraryActionType.RENAME, LibraryActionType.MOVE_AND_RENAME));
    }

    /**
     * Builds a relative Path (i.e. the new file name) based on a release object.
     *
     * @param release The ReleaseWithPath object.
     * @return The new file name as a path
     */
    @Override
    public Path buildPath(ReleaseWithPath release) {
        if (rename && StringUtils.isNotBlank(structure)) {
            return Path.of(switch (release) {
                case TvReleaseWithPath tvRelease -> buildEpisodeFolderStructure(tvRelease);
                case MovieReleaseWithPath movieRelease -> buildMovieFolderStructure(movieRelease);
            });
        }
        return release.path.fileName;
    }

    @Override
    public String buildPathStructure(Release release) {
        if (rename && StringUtils.isNotBlank(structure)) {
            return switch (release) {
                case TvRelease tvRelease -> buildEpisodeFolderStructure(tvRelease);
                case MovieRelease movieRelease -> buildMovieFolderStructure(movieRelease);
            };
        }
        return release.folderNameOrName;
    }

    private String buildEpisodeFolderStructure(TvRelease tvRelease) {
        String filename = structure;
        // order is important!
        filename = replace(filename, SerieStructureTag.SHOW_NAME, getShowName(tvRelease.name));
        filename =
            replaceFormattedEpisodeNumber(filename, SerieStructureTag.EPISODES_LONG, tvRelease.episodes, true);
        filename = replaceFormattedEpisodeNumber(filename, SerieStructureTag.EPISODES_SHORT, tvRelease.episodes,
            false);
        filename = replace(filename, SerieStructureTag.SEASON_LONG, formatNumber(tvRelease.season, true));
        filename = replace(filename, SerieStructureTag.SEASON_SHORT, formatNumber(tvRelease.season, false));
        filename = replace(filename, SerieStructureTag.EPISODE_LONG, formatNumber(tvRelease.firstEpisode, true));
        filename =
            replace(filename, SerieStructureTag.EPISODE_SHORT, formatNumber(tvRelease.firstEpisode, false));

        filename = replace(filename, SerieStructureTag.TITLE, tvRelease.title);
        filename = replace(filename, SerieStructureTag.QUALITY, tvRelease.quality);
        filename = replace(filename, SerieStructureTag.RELEASE_GROUP, tvRelease.releaseGroup);

        filename += "." + StringUtils.substringAfterLast(tvRelease.fileNameOrName, ".");
        filename = filename.removeIllegalWindowsChars();
        if (replaceSpace) {
            filename = filename.replace(' ', replacingSpaceChar);
        }
        return filename;
    }

    private String buildMovieFolderStructure(MovieRelease movieRelease) {
        String filename = structure;
        // order is important!
        filename = replace(filename, MovieStructureTag.MOVIE_TITLE, getShowName(movieRelease.name));
        filename = replace(filename, MovieStructureTag.YEAR, formatNumber(movieRelease.year, false));
        filename = replace(filename, MovieStructureTag.QUALITY, movieRelease.quality);
        filename = replace(filename, MovieStructureTag.RELEASE_GROUP, movieRelease.releaseGroup);

        filename += "." + StringUtils.substringAfterLast(movieRelease.fileNameOrName, ".");

        filename = filename.removeIllegalWindowsChars();
        if (replaceSpace) {
            filename = filename.replace(' ', replacingSpaceChar);
        }
        return filename;
    }

    public String buildSubtitle(ReleaseWithPath release, Subtitle sub, String filename, @Nullable Integer version) {
        return buildSubtitle(release.fileName, filename, sub.language, version);
    }

    public String buildSubtitle(String name, String filename, @Nullable Language language,
        @Nullable Integer version) {
        String extension = "." + StringUtils.substringAfterLast(name, ".");
        String subFileName = filename;
        if (version != null) {
            subFileName =
                subFileName.substring(0, subFileName.indexOf(extension)) + "-v$version." +
                    StringUtils.substringAfterLast(name, ".");
        }
        if (includeLanguageCode) {
            String langCode = language == null ? "" : languageTags.getOrDefault(language, language.iso639_3);
            subFileName = changeExtension(subFileName, !langCode.isEmpty() ? ".$langCode.srt" : ".srt");
        } else {
            subFileName = changeExtension(subFileName, ".srt");
        }
        subFileName = subFileName.removeIllegalWindowsChars();
        if (replaceSpace) {
            subFileName = subFileName.replace(' ', replacingSpaceChar);
        }
        return subFileName;
    }

    /**
     * Changes the extension of a file to a new extension.
     * <p>
     * Example: changeExtension("data.txt", ".java") will result in "data.java".
     *
     * @param fileName the name of the file
     * @param newExtension the new extension to be applied to the filename
     * @return the filename with the updated extension
     */
    private static String changeExtension(String fileName, String newExtension) {
        int lastDot = fileName.lastIndexOf(".");
        if (lastDot != -1) {
            return fileName.substring(0, lastDot) + newExtension;
        } else {
            return fileName + newExtension;
        }
    }
}
