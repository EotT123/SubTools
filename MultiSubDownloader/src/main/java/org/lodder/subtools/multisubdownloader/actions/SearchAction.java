package org.lodder.subtools.multisubdownloader.actions;

import static java.util.Objects.*;
import static manifold.ext.props.rt.api.PropOption.*;

import java.util.List;

import manifold.ext.props.rt.api.get;
import manifold.ext.props.rt.api.set;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.Messages;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.exceptions.SearchSetupException;
import org.lodder.subtools.multisubdownloader.gui.dialog.Cancelable;
import org.lodder.subtools.multisubdownloader.listeners.IndexingProgressListener;
import org.lodder.subtools.multisubdownloader.listeners.SearchProgressListener;
import org.lodder.subtools.multisubdownloader.listeners.StatusListener;
import org.lodder.subtools.multisubdownloader.settings.SettingsControl;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleProviderStore;
import org.lodder.subtools.multisubdownloader.workers.SearchHandler;
import org.lodder.subtools.multisubdownloader.workers.SearchManager;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.util.lazy.LazySupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
public abstract class SearchAction<R extends Release> implements Runnable, Cancelable, SearchHandler<R> {

    private static final Logger LOGGER = LoggerFactory.getLogger(SearchAction.class);

    @get(Protected) @set(Private) @Nullable StatusListener statusListener;
    @get(Protected) @set(Private) @Nullable List<R> releases;
    @get(Protected) abstract Language language;
    abstract @get(Protected) IndexingProgressListener indexingProgressListener;
    abstract @get(Protected) UserInteractionHandler userInteractionHandler;
    abstract @get(Protected) SearchProgressListener searchProgressListener;
    private final LazySupplier<SearchManager> searchManagerLazy;
    @get(Protected) SearchManager searchManager; // Computed property

    protected SearchAction() {
        this.searchManagerLazy = new LazySupplier<>(() ->
            new SearchManager(language, searchProgressListener, userInteractionHandler, this));
    }

    @Override
    public void run() {
        LOGGER.trace("SearchAction is being executed");
        try {
            this.search();
        } catch (ActionException e) {
            LOGGER.trace(e.getMessage(), e);
            if (this.statusListener != null) {
                this.statusListener.onError(e);
            }
        }
    }

    private void search() throws ActionException {
        this.statusListener = this.indexingProgressListener;
        this.indexingProgressListener.reset();
        this.searchProgressListener.reset();

        validate();

        setStatusMessage(Messages.getText("SearchAction.StatusIndexing"));

        this.releases = createReleases();

        if (Thread.currentThread().isInterrupted()) {
            return;
        }

        if (this.releases.isEmpty()) {
            this.cancel(true);
            return;
        }

        this.indexingProgressListener.completed();

        this.statusListener = this.searchProgressListener;

        /* Tell the manager which providers to use */
        searchManager.reset();
        SubtitleProviderStore.providers.stream()
            .filter(subtitleProvider -> SettingsControl.settings.useSerieSource(subtitleProvider.source))
            .forEach(searchManager::addProvider);

        /* Tell the manager which releases to search. */
        this.releases.forEach(searchManager::addRelease);

        setStatusMessage(Messages.getText("SearchAction.StatusSearching"));

        /* Tell the manager to start searching */
        this.searchManager.start();
    }

    protected abstract void validate() throws SearchSetupException;

    protected abstract List<R> createReleases();

    protected void setStatusMessage(String message) {
        requireNonNull(this.statusListener).onStatus(message);
    }

    protected SearchManager getSearchManager() {
        return searchManagerLazy.get();
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        this.searchManager.cancel(mayInterruptIfRunning);
        Thread.currentThread().interrupt();
        this.indexingProgressListener.completed();
        this.searchProgressListener.completed();
        return true;
    }
}
