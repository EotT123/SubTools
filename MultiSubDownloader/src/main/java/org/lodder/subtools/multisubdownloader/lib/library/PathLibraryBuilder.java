package org.lodder.subtools.multisubdownloader.lib.library;

import java.nio.file.Path;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.settings.model.LibrarySettings;
import org.lodder.subtools.multisubdownloader.settings.model.structure.FolderStructureTag;
import org.lodder.subtools.multisubdownloader.settings.model.structure.MovieStructureTag;
import org.lodder.subtools.multisubdownloader.settings.model.structure.SerieStructureTag;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.data.tvdb.TvdbAdapter;
import org.lodder.subtools.sublibrary.model.MovieRelease;
import org.lodder.subtools.sublibrary.model.MovieReleaseWithPath;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.ReleaseWithPath;
import org.lodder.subtools.sublibrary.model.TvRelease;
import org.lodder.subtools.sublibrary.model.TvReleaseWithPath;
import org.lodder.subtools.sublibrary.userinteraction.UserInteractionHandler;

@NullMarked
public final class PathLibraryBuilder extends LibraryBuilder {

    private final String structure;
    private final boolean replaceSpace;
    private final Character replacingSpaceChar;
    private final Path libraryFolder;
    private final boolean move;

    public PathLibraryBuilder(String structure, boolean replaceSpace, char replacingSpaceChar,
        @Nullable TvdbAdapter tvdbAdapter=null, Path libraryFolder, boolean move) {
        super(tvdbAdapter);
        this.structure = structure;
        this.replaceSpace = replaceSpace;
        this.replacingSpaceChar = replacingSpaceChar;
        this.libraryFolder = libraryFolder;
        this.move = move;
    }

    public static PathLibraryBuilder fromSettings(LibrarySettings libSettings, Manager manager,
        UserInteractionHandler userInteractionHandler) {
        return new PathLibraryBuilder(
            structure:libSettings.folderStructure,
            replaceSpace:libSettings.folderReplaceSpace,
            replacingSpaceChar:libSettings.folderReplacingSpaceChar,
            tvdbAdapter:libSettings.useTvdbNaming ? TvdbAdapter.getInstance(manager, userInteractionHandler) : null,
            libraryFolder:libSettings.folder,
            move:libSettings.hasAnyLibraryAction(LibraryActionType.MOVE, LibraryActionType.MOVE_AND_RENAME));
    }

    /**
     * Builds an absolute Path object based on a release object.
     *
     * @param release The ReleaseWithPath object.
     * @return The created absolute path
     */
    @Override
    public Path buildPath(ReleaseWithPath release) {
        if (move) {
            String pathStructure = switch (release) {
                case TvReleaseWithPath tvRelease -> buildEpisodeFolderStructure(tvRelease);
                case MovieReleaseWithPath movieRelease -> buildMovieFolderStructure(movieRelease);
            };
            return libraryFolder.resolve(pathStructure.split(FolderStructureTag.SEPARATOR.label));
        } else {
            return release.path;
        }
    }

    @Override
    public String buildPathStructure(Release release) {
        if (move) {
            return switch (release) {
                case TvRelease tvRelease -> buildEpisodeFolderStructure(tvRelease);
                case MovieRelease movieRelease -> buildMovieFolderStructure(movieRelease);
            };
        } else {
            return release.fileNameOrName;
        }
    }

    private String buildEpisodeFolderStructure(TvRelease tvRelease) {
        String structure = this.structure;

        structure = structure.replace(SerieStructureTag.SHOW_NAME.label, getShowName(tvRelease.name))
            .removeIllegalWindowsChars();
        // order is important!
        structure = replaceFormattedEpisodeNumber(structure, SerieStructureTag.EPISODES_LONG, tvRelease.episodes, true);
        structure =
            replaceFormattedEpisodeNumber(structure, SerieStructureTag.EPISODES_SHORT, tvRelease.episodes, false);
        structure = replace(structure, SerieStructureTag.SEASON_LONG, formatNumber(tvRelease.season, true));
        structure = replace(structure, SerieStructureTag.SEASON_SHORT, formatNumber(tvRelease.season, false));
        structure = replace(structure, SerieStructureTag.EPISODE_LONG, formatNumber(tvRelease.firstEpisode, true));
        structure = replace(structure, SerieStructureTag.EPISODE_SHORT, formatNumber(tvRelease.firstEpisode, false));
        structure = replace(structure, SerieStructureTag.TITLE, tvRelease.title);
        structure = replace(structure, SerieStructureTag.QUALITY, tvRelease.quality);
        structure = replace(structure, SerieStructureTag.RELEASE_GROUP, tvRelease.releaseGroup);
        if (replaceSpace) {
            structure = structure.replace(' ', replacingSpaceChar);
        }
        return structure.trim();
    }

    private String buildMovieFolderStructure(MovieRelease movieRelease) {
        String structure = this.structure;

        structure = replace(structure, MovieStructureTag.MOVIE_TITLE, movieRelease.name.removeIllegalWindowsChars());
        structure = replace(structure, MovieStructureTag.YEAR, Integer.toString(movieRelease.year));
        structure = replace(structure, MovieStructureTag.QUALITY, movieRelease.quality);
        if (replaceSpace) {
            structure = structure.replace(' ', replacingSpaceChar);
        }
        return structure.trim();
    }
}
