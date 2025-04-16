package org.lodder.subtools.multisubdownloader.lib.library;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.lodder.subtools.multisubdownloader.settings.model.LibrarySettings;
import org.lodder.subtools.multisubdownloader.settings.model.structure.FolderStructureTag;
import org.lodder.subtools.multisubdownloader.settings.model.structure.MovieStructureTag;
import org.lodder.subtools.multisubdownloader.settings.model.structure.SerieStructureTag;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.data.tvdb.TheTvdbAdapter;
import org.lodder.subtools.sublibrary.model.MovieRelease;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.TvRelease;
import org.lodder.subtools.sublibrary.userinteraction.UserInteractionHandler;

public final class PathLibraryBuilder extends LibraryBuilder {

    private final String structure;
    private final boolean replaceSpace;
    private final Character replacingSpaceChar;
    private final Path libraryFolder;
    private final boolean move;

    public PathLibraryBuilder(String structure, boolean replaceSpace, char replacingSpaceChar,
        TheTvdbAdapter tvdbAdapter=null, Path libraryFolder, boolean move) {
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
            tvdbAdapter:libSettings.useTVDBNaming ? TheTvdbAdapter.getInstance(manager, userInteractionHandler) : null,
            libraryFolder:libSettings.folder,
            move:libSettings.hasAnyLibraryAction(LibraryActionType.MOVE, LibraryActionType.MOVEANDRENAME));
    }

    @Override
    public Path build(Release release) {
        if (move) {
            Path subpath = switch (release) {
                case TvRelease tvRelease -> buildEpisode(tvRelease);
                case MovieRelease movieRelease -> buildMovie(movieRelease);
            };
            return libraryFolder.resolve(subpath);
        } else {
            return release.getPath();
        }
    }

    private Path buildEpisode(TvRelease tvRelease) {
        String folder = structure;

        folder = folder.replace(SerieStructureTag.SHOW_NAME.label, getShowName(tvRelease.name))
            .removeIllegalWindowsChars();
        // order is important!
        folder = replaceFormattedEpisodeNumber(folder, SerieStructureTag.EPISODES_LONG, tvRelease.episodeNumbers, true);
        folder = replaceFormattedEpisodeNumber(folder, SerieStructureTag.EPISODES_SHORT, tvRelease.episodeNumbers,
            false);
        folder = replace(folder, SerieStructureTag.SEASON_LONG, formattedNumber(tvRelease.season, true));
        folder = replace(folder, SerieStructureTag.SEASON_SHORT, formattedNumber(tvRelease.season, false));
        folder = replace(folder, SerieStructureTag.EPISODE_LONG, formattedNumber(tvRelease.firstEpisodeNumber, true));
        folder = replace(folder, SerieStructureTag.EPISODE_SHORT, formattedNumber(tvRelease.firstEpisodeNumber, false));
        folder = replace(folder, SerieStructureTag.TITLE, tvRelease.title);
        folder = replace(folder, SerieStructureTag.QUALITY, tvRelease.quality);
        folder = replace(folder, SerieStructureTag.RELEASE_GROUP, tvRelease.releaseGroup);
        if (replaceSpace) {
            folder = folder.replace(' ', replacingSpaceChar);
        }
        folder = folder.trim();
        return Paths.get("", folder.split(FolderStructureTag.SEPARATOR.label));
    }

    private Path buildMovie(MovieRelease movieRelease) {
        String folder = structure;

        folder = replace(folder, MovieStructureTag.MOVIE_TITLE, movieRelease.name.removeIllegalWindowsChars());
        folder = replace(folder, MovieStructureTag.YEAR, Integer.toString(movieRelease.year));
        folder = replace(folder, MovieStructureTag.QUALITY, movieRelease.quality);
        if (replaceSpace) {
            folder = folder.replace(' ', replacingSpaceChar);
        }
        folder = folder.trim();
        return Paths.get("", folder.split(FolderStructureTag.SEPARATOR.label));
    }
}
