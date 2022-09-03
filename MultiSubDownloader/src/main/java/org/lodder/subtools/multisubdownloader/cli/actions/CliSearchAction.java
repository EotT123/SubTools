package org.lodder.subtools.multisubdownloader.cli.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.lodder.subtools.multisubdownloader.CLI;
import org.lodder.subtools.multisubdownloader.Messages;
import org.lodder.subtools.multisubdownloader.actions.ActionException;
import org.lodder.subtools.multisubdownloader.actions.FileListAction;
import org.lodder.subtools.multisubdownloader.actions.SearchAction;
import org.lodder.subtools.multisubdownloader.exceptions.SearchSetupException;
import org.lodder.subtools.multisubdownloader.lib.ReleaseFactory;
import org.lodder.subtools.multisubdownloader.lib.control.subtitles.Filtering;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CliSearchAction extends SearchAction {

    private static final Logger LOGGER = LoggerFactory.getLogger(CliSearchAction.class);

    private CLI cmd;
    private FileListAction filelistAction;
    private List<File> folders;
    private boolean isRecursive;
    private String languageCode;
    private boolean overwriteSubtitles;
    private ReleaseFactory releaseFactory;
    private Filtering filtering;

    public void setCommandLine(CLI cmd) {
        this.cmd = cmd;
    }

    public void setFileListAction(FileListAction filelistAction) {
        this.filelistAction = filelistAction;
    }

    public void setFolders(List<File> folders) {
        this.folders = folders;
    }

    public void setRecursive(boolean recursive) {
        this.isRecursive = recursive;
    }

    public void setOverwriteSubtitles(boolean overwrite) {
        this.overwriteSubtitles = overwrite;
    }

    public void setReleaseFactory(ReleaseFactory releaseFactory) {
        this.releaseFactory = releaseFactory;
    }

    public void setFiltering(Filtering filtering) {
        this.filtering = filtering;
    }

    @Override
    protected List<Release> createReleases() throws ActionException {
        filelistAction.setIndexingProgressListener(this.indexingProgressListener);

        List<File> files = new ArrayList<>();
        for (File folder : this.folders) {
            files.addAll(filelistAction.getFileListing(folder, this.isRecursive, this.languageCode,
                    this.overwriteSubtitles));
        }

        /* fix: remove carriage return from progressbar */
        System.out.println("");

        LOGGER.debug("# Files found to process [{}] ", files.size());

        int total = files.size();
        int index = 0;
        int progress = 0;

        System.out.println(Messages.getString("CliSearchAction.ParsingFoundFiles"));
        this.indexingProgressListener.progress(progress);

        List<Release> releases = new ArrayList<>();
        for (File file : files) {
            index++;
            progress = (int) Math.floor((float) index / total * 100);

            /* Tell progressListener which file we are processing */
            this.indexingProgressListener.progress(file.getName());

            Release release = this.releaseFactory.createRelease(file);
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
    protected String getLanguageCode() {
        return this.languageCode;
    }

    public void setLanguageCode(String languageCode) {
        this.languageCode = languageCode;
    }

    @Override
    public void onFound(Release release, List<Subtitle> subtitles) {
        if (filtering != null) {
            subtitles = filtering.getFiltered(subtitles, release);
        }

        release.getMatchingSubs().addAll(subtitles);
        if (searchManager.getProgress() < 100) {
            return;
        }

        LOGGER.debug("found files for doDownload [{}]", releases.size());

        /* stop printing progress */
        this.searchProgressListener.completed();

        this.cmd.download(releases);
    }

    @Override
    protected void validate() throws SearchSetupException {
        if (this.cmd == null) {
            throw new SearchSetupException("Cmd must be set.");
        }
        if (this.languageCode == null) {
            throw new SearchSetupException("LanguageCode must be set.");
        }
        if (this.filelistAction == null) {
            throw new SearchSetupException("Actions must be set.");
        }
        if (this.folders == null || this.folders.size() <= 0) {
            throw new SearchSetupException("Folders must be set.");
        }
        if (this.releaseFactory == null) {
            throw new SearchSetupException("releaseFactory must be set.");
        }
        if (this.filtering == null) {
            throw new SearchSetupException("Filtering must be set.");
        }
        super.validate();
    }

    @Override
    protected String getLanguageCode(String language) {
        /* Already provided and validated as code in cli */
        return language;
    }
}
