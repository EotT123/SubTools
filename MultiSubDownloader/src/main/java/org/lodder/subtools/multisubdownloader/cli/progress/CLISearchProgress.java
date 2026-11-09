package org.lodder.subtools.multisubdownloader.cli.progress;

import dnl.utils.text.table.TextTable;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.actions.ActionException;
import org.lodder.subtools.multisubdownloader.gui.dialog.progress.search.SearchProgressTableModel;
import org.lodder.subtools.multisubdownloader.listener.SearchProgressListener;
import org.lodder.subtools.multisubdownloader.subtitleprovider.SubtitleProvider;
import org.lodder.subtools.sublibrary.model.Release;

@NullMarked
public final class CLISearchProgress extends CLIProgress implements SearchProgressListener {

    private final TextTable table;
    private final SearchProgressTableModel tableModel;

    public CLISearchProgress() {
        tableModel = new SearchProgressTableModel();
        table = new TextTable(tableModel);
    }

    @Override
    public void progress(SubtitleProvider provider, int jobsLeft, Release release) {
        this.tableModel.update(provider.provider, jobsLeft, release.fileNameOrName);
        this.printProgress();
    }

    @Override
    public void done(SubtitleProvider provider) {
        this.tableModel.update(provider.provider, 0, "Done");
        this.printProgress();
    }

    @Override
    public void progress(int progress) {
        this.progress = progress;
        this.printProgress();
    }

    @Override
    public void completed() {
        if (!this.enabled) {
            return;
        }
        this.disable();
    }

    @Override
    public void reset() {
        this.enabled = true;
    }

    @Override
    public void onError(ActionException exception) {
        if (!enabled) {
            return;
        }
        System.out.println("Error: " + exception.getMessage());
    }

    @Override
    public void onStatus(String message) {
        if (!enabled) {
            return;
        }
        System.out.println(message);
    }

    @Override
    protected void printProgress() {
        if (!enabled) {
            return;
        }

        /* print table */
        if (verbose) {
            System.out.println();
            table.printTable();
        }

        /* print progressbar */
        this.printProgBar(this.progress);
    }
}
