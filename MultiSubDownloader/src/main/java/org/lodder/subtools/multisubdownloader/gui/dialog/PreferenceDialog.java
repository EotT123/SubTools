package org.lodder.subtools.multisubdownloader.gui.dialog;

import static org.lodder.subtools.multisubdownloader.Messages.*;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.io.Serial;
import java.util.concurrent.atomic.AtomicInteger;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.GUI;
import org.lodder.subtools.multisubdownloader.gui.panels.preference.EpisodeLibraryPanel;
import org.lodder.subtools.multisubdownloader.gui.panels.preference.GeneralPanel;
import org.lodder.subtools.multisubdownloader.gui.panels.preference.MovieLibraryPanel;
import org.lodder.subtools.multisubdownloader.gui.panels.preference.OptionsPanel;
import org.lodder.subtools.multisubdownloader.gui.panels.preference.PreferencePanelIntf;
import org.lodder.subtools.multisubdownloader.gui.panels.preference.SerieProvidersPanel;
import org.lodder.subtools.multisubdownloader.settings.SettingsControl;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleProviderStore;
import org.lodder.subtools.sublibrary.userinteraction.UserInteractionHandler;

@NullMarked
public class PreferenceDialog extends MultiSubDialog {

    @Serial private static final long serialVersionUID = 1L;

    private final GeneralPanel pnlGeneral;
    private final EpisodeLibraryPanel pnlEpisodeLibrary;
    private final MovieLibraryPanel pnlMovieLibrary;
    private final OptionsPanel pnlOptions;
    private final SerieProvidersPanel pnlSerieSources;

    public PreferenceDialog(GUI gui, UserInteractionHandler userInteractionHandler) {
        super(gui, getText("PreferenceDialog.Title"), true);

        setResizable(false);
        setModalityType(ModalityType.APPLICATION_MODAL);
        setBounds(100, 100, 650, 700);

        AtomicInteger selectedIdx = new AtomicInteger();
        contentPane
            .layout(new BorderLayout())
            .addComponent(BorderLayout.CENTER, new JLabel()
                .border(new EmptyBorder(5, 5, 5, 5))
                .layout(new BorderLayout(0, 0))
                .addComponent(new JTabbedPane(SwingConstants.TOP)
                    .tabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT)
                    .changeListener(tabbedPane -> {
                        if (tabbedPane.selectedIndex != selectedIdx.get()) {
                            var sourcePanel = (PreferencePanelIntf) tabbedPane.getComponentAt(selectedIdx.get());
                            if (!sourcePanel.hasValidSettings()) {
                                tabbedPane.selectedIndex = selectedIdx.get();
                                JOptionPane.showMessageDialog(this, getText("PreferenceDialog.invalidInput"),
                                    "Error", JOptionPane.ERROR_MESSAGE);
                            } else {
                                selectedIdx.set(tabbedPane.selectedIndex);
                            }
                        }
                    })
                    .withTab(getText("PreferenceDialog.TabGeneral"),
                        pnlGeneral = new GeneralPanel(gui))
                    .withTab(getText("PreferenceDialog.SerieLibrary"),
                        pnlEpisodeLibrary = new EpisodeLibraryPanel(SettingsControl.settings.episodeLibrarySettings,
                            false, userInteractionHandler))
                    .withTab(getText("PreferenceDialog.MovieLibrary"),
                        pnlMovieLibrary = new MovieLibraryPanel(SettingsControl.settings.movieLibrarySettings, false,
                            userInteractionHandler))
                    .withTab(getText("PreferenceDialog.Options"),
                        pnlOptions = new OptionsPanel())
                    .withTab(getText("PreferenceDialog.SerieSources"),
                        pnlSerieSources = new SerieProvidersPanel()))
            )
            .addComponent(BorderLayout.SOUTH, new JPanel()
                .layout(new FlowLayout(FlowLayout.RIGHT))
                .addComponent(new JButton(getText("App.OK"))
                    .defaultButtonFor(getRootPane())
                    .actionListener(this::testAndSaveValues)
                    .actionCommand(getText("App.OK")))
                .addComponent(new JButton(getText("App.Cancel"))
                    .actionListener(() -> setVisible(false))
                    .actionCommand("Cancel")));
    }

    private void testAndSaveValues() {
        if (pnlGeneral.hasValidSettings() && pnlEpisodeLibrary.hasValidSettings() &&
            pnlMovieLibrary.hasValidSettings() && pnlOptions.hasValidSettings() &&
            pnlSerieSources.hasValidSettings()) {
            pnlGeneral.savePreferenceSettings();
            pnlEpisodeLibrary.savePreferenceSettings();
            pnlMovieLibrary.savePreferenceSettings();
            pnlOptions.savePreferenceSettings();
            pnlSerieSources.savePreferenceSettings();
            setVisible(false);
            SettingsControl.store();
            SubtitleProviderStore.resetProviders();
        } else {
            JOptionPane.showMessageDialog(this, getText("PreferenceDialog.invalidInput"), "Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
}
