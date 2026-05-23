package org.lodder.subtools.multisubdownloader.gui.actions.search;

import static util.Utils.*;

import java.util.List;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.GUI;
import org.lodder.subtools.multisubdownloader.Messages;
import org.lodder.subtools.multisubdownloader.exceptions.SearchSetupException;
import org.lodder.subtools.multisubdownloader.gui.extra.table.VideoTableModel;
import org.lodder.subtools.multisubdownloader.gui.panels.SearchPanel;
import org.lodder.subtools.multisubdownloader.gui.panels.SearchTextInputPanel;
import org.lodder.subtools.multisubdownloader.lib.ReleaseFactory;
import org.lodder.subtools.multisubdownloader.settings.model.Settings;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleProviderStore;
import org.lodder.subtools.sublibrary.model.MovieReleaseWithoutPath;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.Subtitle;
import org.lodder.subtools.sublibrary.model.TvReleaseWithoutPath;
import org.lodder.subtools.sublibrary.model.VideoSearchType;

@NullMarked
public final class TextGuiSearchAction extends GuiSearchAction<SearchTextInputPanel, Release> {

    public TextGuiSearchAction(Settings settings, SubtitleProviderStore subtitleProviderStore, GUI mainWindow,
        SearchPanel<SearchTextInputPanel> searchPanel, ReleaseFactory releaseFactory) {
        super(settings, subtitleProviderStore, mainWindow, searchPanel, releaseFactory);
    }

    @Override
    protected void validate() throws SearchSetupException {
        if (getInputPanel().getReleaseName().isEmpty()) {
            throw new SearchSetupException(Messages.getText("App.NoReleaseEntered"));
        }
    }

    @Override
    protected List<Release> createReleases() {
        String name = getInputPanel().getReleaseName();
        VideoSearchType type = getInputPanel().getType();

        VideoTableModel model = (VideoTableModel) this.searchPanel.resultPanel.getTable().getModel();
        model.clearTable();

        // TODO: Redefine what a "release" is.
        return switch (type) {
            case EPISODE -> List.of(
                new TvReleaseWithoutPath(name:name, season:inputPanel.season, episode:inputPanel.episode, quality:
                    inputPanel.quality, completeName:name));
            case MOVIE ->
                List.of(new MovieReleaseWithoutPath(name:name, quality:inputPanel.quality, completeName:name));
            default ->
                ifNotNullOrElseGet(releaseFactory.createRelease(name, userInteractionHandler), List::of, List::of);
        };
    }

    @Override
    public void onFound(Release release, List<Subtitle> subtitles) {
        VideoTableModel model = (VideoTableModel) this.searchPanel.resultPanel.getTable().getModel();

        List<Subtitle> subtitlesFiltered =
            subtitles.stream().filter(subtitle -> filtering.useSubtitle(subtitle, release)).toList();
        subtitlesFiltered.forEach(release::addMatchingSub);

        // use automatic selection to reduce the selection for the user
        List<Subtitle> subtitlesFilteredAutomatic = userInteractionHandler.getAutomaticSelection(subtitlesFiltered);
        subtitlesFilteredAutomatic.forEach(model::addRow);

        /* Let GuiSearchAction also make some decisions */
        super.onFound(release, subtitlesFilteredAutomatic);
    }
}
