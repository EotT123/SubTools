package org.lodder.subtools.multisubdownloader;

import static org.lodder.subtools.multisubdownloader.cli.CliOptions.CliOptionEnable.*;
import static org.lodder.subtools.multisubdownloader.cli.CliOptions.CliOptionLanguage.*;
import static org.lodder.subtools.multisubdownloader.cli.CliOptions.CliOptionPath.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.actions.DownloadAction;
import org.lodder.subtools.multisubdownloader.actions.FileListAction;
import org.lodder.subtools.multisubdownloader.actions.UserInteractionHandlerAction;
import org.lodder.subtools.multisubdownloader.cli.actions.CliSearchAction;
import org.lodder.subtools.multisubdownloader.cli.progress.CLIFileIndexerProgress;
import org.lodder.subtools.multisubdownloader.cli.progress.CLISearchProgress;
import org.lodder.subtools.multisubdownloader.exception.CliException;
import org.lodder.subtools.multisubdownloader.exception.SearchSetupException;
import org.lodder.subtools.multisubdownloader.lib.Info;
import org.lodder.subtools.multisubdownloader.lib.ReleaseFactory;
import org.lodder.subtools.multisubdownloader.lib.control.subtitles.SubtitleFiltering;
import org.lodder.subtools.multisubdownloader.settings.SettingsControl;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.model.ReleaseWithPath;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
public class CLI {

    private static final Logger LOGGER = LoggerFactory.getLogger(CLI.class);

    private final boolean recursive;
    private final Language language;
    private final boolean force;
    private final List<Path> folders;
    private final boolean downloadAll;
    private final boolean subtitleSelection;
    private final boolean verboseProgress;
    private final DownloadAction downloadAction;
    private final UserInteractionHandlerAction userInteractionHandlerAction;
    private final boolean dryRun;

    public CLI(Commandline commandline) throws CliException {
        checkUpdate();
        UserInteractionHandlerCLI userInteractionHandler = new UserInteractionHandlerCLI(SettingsControl.settings);
        userInteractionHandlerAction = new UserInteractionHandlerAction(userInteractionHandler);
        downloadAction = new DownloadAction(userInteractionHandler);
        this.folders = commandline.get(FOLDER, List::of, () -> List.copyOf(SettingsControl.settings.defaultFolders));
        this.language = commandline.get(LANGUAGE, Language.ENGLISH);
        this.force = commandline.isEnabled(FORCE);
        this.downloadAll = commandline.isEnabled(DOWNLOAD_ALL);
        this.recursive = commandline.isEnabled(RECURSIVE);
        this.subtitleSelection = commandline.isEnabled(SELECTION);
        this.verboseProgress = commandline.isEnabled(VERBOSE_PROGRESS);
        this.dryRun = commandline.isEnabled(DRY_RUN);
        Messages.language = language;
    }

    private void checkUpdate() {
        UpdateAvailableGithub u = new UpdateAvailableGithub();
        if (u.shouldCheckForNewUpdate(SettingsControl.settings.updateCheckPeriod) && u.isNewVersionAvailable()) {
            System.out.println(Messages.getText("UpdateAppAvailable") + ": " + u.getLatestDownloadUrl());
        }
    }

    public void run() {
        Info.subtitleSources(true);
        Info.subtitleFiltering(true);
        this.search();
    }

    public void download(List<ReleaseWithPath> releases) {
        Info.downloadOptions(true);
        for (ReleaseWithPath release : releases) {
            try {
                this.download(release);
            } catch (Exception e) {
                LOGGER.error("Error while downloading subtitle for ${release.releaseDescription} (%${e.getMessage()})",
                    e);
            }
        }
    }

    public void search() {
        try {
            new CliSearchAction(
                new CLIFileIndexerProgress().verbose(verboseProgress),
                new CLISearchProgress().verbose(verboseProgress),
                this,
                new FileListAction(),
                language,
                new ReleaseFactory(),
                new SubtitleFiltering(),
                folders,
                recursive,
                force)
                /* CLI has no benefit of running this in a separate Thread */
                .run();
        } catch (SearchSetupException e) {
            LOGGER.error("executeArgs: search (%s)".formatted(e.getMessage()), e);
        }
    }

    private void download(ReleaseWithPath release) {
        List<Subtitle> selection;
        if (downloadAll) {
            selection = release.matchingSubs;
            if (!selection.isEmpty()) {
                System.out.println("Downloading ALL found subtitles for release: ${release.fileNameOrName}");
            }
        } else {
            selection = userInteractionHandlerAction.subtitleSelection(release, subtitleSelection, dryRun);
        }
        if (selection.isEmpty()) {
            System.out.println("No subtitles found for: ${release.fileNameOrName}");
        } else {
            AtomicInteger counter = new AtomicInteger(1);
            IntStream.range(0, selection.size()).forEach(j -> {
                System.out.println("Downloading subtitle: " + release.matchingSubs.get(j).fileName);
                try {
                    downloadAction.download(release, release.matchingSubs.get(j), selection.size() == 1 ? null :
                        counter);
                } catch (IOException e) {
                    LOGGER.error(
                        "Error while downloading subtitle for ${release.releaseDescription} (${e.getMessage()})", e);
                }
            });
        }
    }
}
