package org.lodder.subtools.multisubdownloader;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.IntStream;

import org.apache.commons.cli.CommandLine;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.actions.DownloadAction;
import org.lodder.subtools.multisubdownloader.actions.FileListAction;
import org.lodder.subtools.multisubdownloader.actions.UserInteractionHandlerAction;
import org.lodder.subtools.multisubdownloader.cli.CliOption;
import org.lodder.subtools.multisubdownloader.cli.actions.CliSearchAction;
import org.lodder.subtools.multisubdownloader.cli.progress.CLIFileIndexerProgress;
import org.lodder.subtools.multisubdownloader.cli.progress.CLISearchProgress;
import org.lodder.subtools.multisubdownloader.exceptions.CliException;
import org.lodder.subtools.multisubdownloader.exceptions.SearchSetupException;
import org.lodder.subtools.multisubdownloader.framework.Container;
import org.lodder.subtools.multisubdownloader.lib.Info;
import org.lodder.subtools.multisubdownloader.lib.ReleaseFactory;
import org.lodder.subtools.multisubdownloader.lib.control.subtitles.SubtitleFiltering;
import org.lodder.subtools.multisubdownloader.settings.SettingsControl;
import org.lodder.subtools.multisubdownloader.settings.model.Settings;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.model.ReleaseWithPath;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
public class CLI {

    private static final Logger LOGGER = LoggerFactory.getLogger(CLI.class);

    private final Container app;
    private final Settings settings;
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

    public CLI(SettingsControl settingControl, Container app, CommandLine line) throws CliException {
        this.app = app;
        this.settings = settingControl.settings;
        Manager manager = app.makeManager();
        checkUpdate(manager);
        UserInteractionHandlerCLI userInteractionHandler = new UserInteractionHandlerCLI(settings);
        userInteractionHandlerAction = new UserInteractionHandlerAction(settings, userInteractionHandler);
        downloadAction = new DownloadAction(settings, manager, userInteractionHandler);
        this.folders = getFolders(line);
        this.language = getLanguage(line);
        this.force = line.hasCliOption(CliOption.FORCE);
        this.downloadAll = line.hasCliOption(CliOption.DOWNLOAD_ALL);
        this.recursive = line.hasCliOption(CliOption.RECURSIVE);
        this.subtitleSelection = line.hasCliOption(CliOption.SELECTION);
        this.verboseProgress = line.hasCliOption(CliOption.VERBOSE_PROGRESS);
        this.dryRun = line.hasCliOption(CliOption.DRY_RUN);
        Messages.language = language;
    }

    private void checkUpdate(Manager manager) {
        UpdateAvailableGithub u = new UpdateAvailableGithub(manager, settings);
        if (u.shouldCheckForNewUpdate(settings.updateCheckPeriod) && u.isNewVersionAvailable()) {
            System.out.println(Messages.getText("UpdateAppAvailable") + ": " + u.getLatestDownloadUrl());
        }
    }

    public void run() {
        Info.subtitleSources(this.settings, true);
        Info.subtitleFiltering(this.settings, true);
        this.search();
    }

    public void download(List<ReleaseWithPath> releases) {
        Info.downloadOptions(this.settings, true);
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
            new CliSearchAction(settings,
                new CLIFileIndexerProgress().verbose(verboseProgress),
                new CLISearchProgress().verbose(verboseProgress),
                this,
                new FileListAction(this.settings),
                language,
                new ReleaseFactory(this.settings, app.makeManager()),
                new SubtitleFiltering(this.settings),
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

    private List<Path> getFolders(CommandLine line) {
        if (line.hasCliOption(CliOption.FOLDER)) {
            return List.of(Path.of(line.getCliOptionValue(CliOption.FOLDER)));
        } else {
            return List.copyOf(this.settings.defaultFolders);
        }
    }

    private static Language getLanguage(CommandLine line) throws CliException {
        if (line.hasCliOption(CliOption.LANGUAGE)) {
            String languageString = line.getCliOptionValue(CliOption.LANGUAGE);
            return Language.values().stream()
                .filter(lang -> lang.name().equalsIgnoreCase(languageString))
                .findAny()
                .orElseThrow(() -> new CliException(Messages.getText("App.NoValidLanguage")));
        } else {
            return Language.ENGLISH;
        }
    }
}
