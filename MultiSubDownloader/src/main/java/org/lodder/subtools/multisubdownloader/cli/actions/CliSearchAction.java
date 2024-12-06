package org.lodder.subtools.multisubdownloader.cli.actions;

import static manifold.ext.props.rt.api.PropOption.*;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.experimental.ExtensionMethod;
import manifold.ext.props.rt.api.get;
import manifold.ext.props.rt.api.override;
import org.lodder.subtools.multisubdownloader.CLI;
import org.lodder.subtools.multisubdownloader.Messages;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.UserInteractionHandlerCLI;
import org.lodder.subtools.multisubdownloader.actions.FileListAction;
import org.lodder.subtools.multisubdownloader.actions.SearchAction;
import org.lodder.subtools.multisubdownloader.exceptions.SearchSetupException;
import org.lodder.subtools.multisubdownloader.lib.ReleaseFactory;
import org.lodder.subtools.multisubdownloader.lib.control.subtitles.SubtitleFiltering;
import org.lodder.subtools.multisubdownloader.listeners.IndexingProgressListener;
import org.lodder.subtools.multisubdownloader.listeners.SearchProgressListener;
import org.lodder.subtools.multisubdownloader.settings.model.Settings;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleProviderStore;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtensionMethod({ Files.class })
public class CliSearchAction extends SearchAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(CliSearchAction.class);

    private final @NonNull CLI cli;
    private final @NonNull FileListAction fileListAction;
    private final @NonNull ReleaseFactory releaseFactory;
    private final @NonNull SubtitleFiltering filtering;
    private final boolean overwriteSubtitles;
    private final @NonNull List<Path> folders;
    private final boolean recursive;

    @get @override @NonNull Language language;
    @get(Protected) @override @NonNull IndexingProgressListener indexingProgressListener;
    @get(Protected) @override @NonNull SearchProgressListener searchProgressListener;

    public interface CliSearchActionBuilderSubtitleProviderStore {
        CliSearchActionBuilderIndexingProgressListener subtitleProviderStore(
                SubtitleProviderStore subtitleProviderStore);
    }

    public interface CliSearchActionBuilderIndexingProgressListener {
        CliSearchActionBuilderSearchProgressListener indexingProgressListener(
                IndexingProgressListener indexingProgressListener);
    }

    public interface CliSearchActionBuilderSearchProgressListener {
        CliSearchActionBuilderCLI searchProgressListener(SearchProgressListener searchProgressListener);
    }

    public interface CliSearchActionBuilderCLI {
        CliSearchActionBuilderFileListAction cli(CLI cli);
    }

    public interface CliSearchActionBuilderFileListAction {
        CliSearchActionBuilderLanguage fileListAction(FileListAction fileListAction);
    }

    public interface CliSearchActionBuilderLanguage {
        CliSearchActionBuilderReleaseFactory language(Language language);
    }

    public interface CliSearchActionBuilderReleaseFactory {
        CliSearchActionBuilderFiltering releaseFactory(ReleaseFactory releaseFactory);
    }

    public interface CliSearchActionBuilderFiltering {
        CliSearchActionBuilderFolders filtering(@NonNull SubtitleFiltering filtering);
    }

    public interface CliSearchActionBuilderFolders {
        CliSearchActionBuilderOther folders(List<Path> folders);
    }

    public interface CliSearchActionBuilderOther {
        CliSearchActionBuilderOther overwriteSubtitles(boolean overwriteSubtitles);

        CliSearchActionBuilderOther recursive(boolean recursive);

        CliSearchAction build() throws SearchSetupException;
    }

    public static CliSearchActionBuilderSubtitleProviderStore createWithSettings(Settings settings) {
        return new CliSearchActionBuilder(settings);
    }

    @RequiredArgsConstructor
    @Setter
    @Accessors(fluent = true, chain = true)
    public static class CliSearchActionBuilder
            implements CliSearchActionBuilderSearchProgressListener, CliSearchActionBuilderIndexingProgressListener,
            CliSearchActionBuilderSubtitleProviderStore, CliSearchActionBuilderCLI,
            CliSearchActionBuilderFileListAction, CliSearchActionBuilderLanguage, CliSearchActionBuilderReleaseFactory,
            CliSearchActionBuilderFiltering, CliSearchActionBuilderFolders, CliSearchActionBuilderOther {
        private final Settings settings;
        private SubtitleProviderStore subtitleProviderStore;
        private IndexingProgressListener indexingProgressListener;
        private SearchProgressListener searchProgressListener;
        private CLI cli;
        private FileListAction fileListAction;
        private Language language;
        private ReleaseFactory releaseFactory;
        private SubtitleFiltering filtering;
        private List<Path> folders;
        private boolean overwriteSubtitles;
        private boolean recursive;

        @Override
        public CliSearchAction build() throws SearchSetupException {
            return new CliSearchAction(settings, subtitleProviderStore, indexingProgressListener,
                    searchProgressListener, cli, fileListAction, language, releaseFactory, filtering, folders,
                    overwriteSubtitles, recursive);
        }
    }

    private CliSearchAction(Settings settings, SubtitleProviderStore subtitleProviderStore,
            IndexingProgressListener indexingProgressListener, SearchProgressListener searchProgressListener, CLI cli,
            FileListAction fileListAction, Language language, ReleaseFactory releaseFactory,
            SubtitleFiltering filtering, List<Path> folders, boolean overwriteSubtitles, boolean recursive)
            throws SearchSetupException {
        super(settings, subtitleProviderStore);
        this.indexingProgressListener = indexingProgressListener;
        this.searchProgressListener = searchProgressListener;
        this.cli = cli;
        this.fileListAction = fileListAction;
        this.language = language;
        this.releaseFactory = releaseFactory;
        this.filtering = filtering;
        this.folders = folders;
        this.overwriteSubtitles = overwriteSubtitles;
        this.recursive = recursive;
        if (this.folders.isEmpty()) {
            throw new SearchSetupException("Folders must be set.");
        }
    }

    @Override
    protected List<Release> createReleases() {
        fileListAction.indexingProgressListener = this.indexingProgressListener;

        List<Path> files = this.folders.stream()
                .flatMap(folder -> fileListAction.getFileListing(folder, recursive, language, overwriteSubtitles)
                        .stream())
                .toList();

        /* fix: remove carriage return from progressbar */
        System.out.println();

        int total = files.size();
        int index = 0;
        int progress = 0;

        LOGGER.debug("# Files found to process [{}] ", total);

        System.out.println(Messages.getString("CliSearchAction.ParsingFoundFiles"));
        this.indexingProgressListener.progress(progress);

        List<Release> releases = new ArrayList<>();
        for (Path file : files) {
            index++;
            progress = (int) Math.floor((float) index / total * 100);

            /* Tell progressListener which file we are processing */
            this.indexingProgressListener.progress(file.getFileNameAsString());

            Release release = this.releaseFactory.createRelease(file, userInteractionHandler);
            if (release == null) {
                continue;
            }

            releases.add(release);

            /* Update progressListener */
            this.indexingProgressListener.progress(progress);
        }

        return releases;
    }

    @Override
    public void onFound(Release release, List<Subtitle> subtitles) {
        subtitles.stream()
                .filter(subtitle -> filtering.useSubtitle(subtitle, release))
                .forEach(release::addMatchingSub);
        if (searchManager.getProgress() < 100) {
            return;
        }
        LOGGER.debug("found files for doDownload [{}]", releases.size());

        /* stop printing progress */
        this.searchProgressListener.completed();

        this.cli.download(releases);
    }

    @Override
    protected UserInteractionHandler getUserInteractionHandler() {
        return new UserInteractionHandlerCLI(settings);
    }

    @Override
    protected void validate() {
    }
}
