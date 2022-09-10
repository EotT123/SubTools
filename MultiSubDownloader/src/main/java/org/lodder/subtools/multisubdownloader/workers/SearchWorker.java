package org.lodder.subtools.multisubdownloader.workers;

import java.util.ArrayList;
import java.util.List;

import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleProvider;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SearchWorker extends Thread {

    protected final SubtitleProvider provider;
    private final SearchManager scheduler;
    private boolean busy = false;
    private boolean isInterrupted = false;
    private Release release;
    private List<Subtitle> subtitles;
    private static final Logger LOGGER = LoggerFactory.getLogger(SearchWorker.class);

    public SearchWorker(SubtitleProvider provider, SearchManager scheduler) {
        this.provider = provider;
        this.scheduler = scheduler;
    }

    @Override
    public void run() {
        Language language = this.scheduler.getLanguage();
        this.busy = false;
        while (!this.isInterrupted()) {
            this.busy = true;
            Release release = this.scheduler.getNextRelease(provider);
            if (release == null) {
                this.busy = false;
                break;
            }
            this.release = release;
            LOGGER.debug("[Search] {} searching {} ", this.provider.getName(), release.toString());

            List<Subtitle> subtitles = this.provider.search(release, language);
            if (subtitles == null) {
                subtitles = new ArrayList<>();
            }

            /* clone to prevent other threads from ever messing with it */
            this.subtitles = new ArrayList<>(subtitles);

            this.busy = false;
            LOGGER.debug("[Search] {} found {} subtitles for {} ", this.provider.getName(), subtitles.size(), release.toString());

            if (!this.isInterrupted()) {
                this.scheduler.onCompleted(this);
            }
        }
    }

    @Override
    public boolean isInterrupted() {
        /* bugfix? interupt-state isn't being held */
        return super.isInterrupted() || this.isInterrupted;
    }

    @Override
    public void interrupt() {
        this.isInterrupted = true;
        super.interrupt();
    }

    public boolean isBusy() {
        return busy;
    }

    public Release getRelease() {
        return release;
    }

    public List<Subtitle> getSubtitles() {
        return subtitles;
    }

    public SubtitleProvider getProvider() {
        return provider;
    }
}
