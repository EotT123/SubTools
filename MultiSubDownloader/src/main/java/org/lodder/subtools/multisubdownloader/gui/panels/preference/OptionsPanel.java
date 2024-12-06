package org.lodder.subtools.multisubdownloader.gui.panels.preference;

import javax.swing.*;
import java.io.Serial;

import net.miginfocom.swing.MigLayout;
import org.lodder.subtools.multisubdownloader.Messages;
import org.lodder.subtools.multisubdownloader.gui.extra.PanelCheckBox;
import org.lodder.subtools.multisubdownloader.gui.extra.TitlePanel;
import org.lodder.subtools.multisubdownloader.gui.jcomponent.jcombobox.MyComboBox;
import org.lodder.subtools.multisubdownloader.settings.SettingsControl;
import org.lodder.subtools.multisubdownloader.settings.model.SettingsProcessEpisodeSource;

public class OptionsPanel extends JPanel implements PreferencePanelIntf {

    @Serial
    private static final long serialVersionUID = -5458593307643063563L;

    private final SettingsControl settingsCtrl;
    private final JCheckBox chkAlwaysConfirm;
    private final JCheckBox chkMinScoreSelection;
    private final JSlider sldMinScoreSelection;
    private final JCheckBox chkDefaultSelection;
    private final DefaultSelectionPanel pnlDefaultSelection;
    private final JCheckBox chkSubtitleExactMethod;
    private final JCheckBox chkSubtitleKeywordMethod;
    private final JCheckBox chkExcludeHearingImpaired;
    private final JCheckBox chkOnlyFound;
    private final JCheckBox chkStopOnSearchError;
    private final MyComboBox<SettingsProcessEpisodeSource> cbxEpisodeProcessSource;
    private final JCheckBox chkConfirmProviderMapping;

    public OptionsPanel(SettingsControl settingsCtrl) {
        super(new MigLayout("insets 0, fill, nogrid"));
        this.settingsCtrl = settingsCtrl;

        TitlePanel.title(Messages.getString("PreferenceDialog.DownloadOptions"))
                .marginBottom(0).padding(0).paddingLeft(20).addTo(this, "span, grow, wrap")
                .addComponent(this.chkAlwaysConfirm =
                        new JCheckBox(Messages.getString("PreferenceDialog.CheckBeforeDownloading")), "wrap")
                .addComponent("wrap, grow", PanelCheckBox
                        .checkbox(this.chkMinScoreSelection =
                                new JCheckBox(Messages.getString("PreferenceDialog.MinAutomaticScoreSelection")))
                        .panelOnSameLine().build()
                        .addComponent(this.sldMinScoreSelection = new JSlider().withMinimum(0).withMaximum(100),
                                "wrap"))
                .addComponent("wrap, grow", PanelCheckBox
                        .checkbox(this.chkDefaultSelection =
                                new JCheckBox(Messages.getString("PreferenceDialog.DefaultSelection"), null, true))
                        .panelOnNewLine().build()
                        .addComponent(this.pnlDefaultSelection = new DefaultSelectionPanel(settingsCtrl)));

        TitlePanel.title(Messages.getString("PreferenceDialog.SearchFilter"))
                .marginBottom(0).padding(0).paddingLeft(20).addTo(this, "span, grow, wrap")
                .addComponent(this.chkSubtitleExactMethod =
                        new JCheckBox(Messages.getString("PreferenceDialog.SearchFilterExact")), "wrap")
                .addComponent(this.chkSubtitleKeywordMethod =
                        new JCheckBox(Messages.getString("PreferenceDialog.SearchFilterKeyword")), "wrap")
                .addComponent(this.chkExcludeHearingImpaired =
                        new JCheckBox(Messages.getString("PreferenceDialog.ExcludeHearingImpaired")));

        TitlePanel.title(Messages.getString("PreferenceDialog.TableOptions"))
                .marginBottom(0).padding(0).paddingLeft(20).addTo(this, "span, grow, wrap")
                .addComponent(this.chkOnlyFound = new JCheckBox(Messages.getString("PreferenceDialog.ShowOnlyFound")));

        TitlePanel.title(Messages.getString("PreferenceDialog.ErrorHandlingOption"))
                .marginBottom(0).padding(0).paddingLeft(20).addTo(this, "span, grow, wrap")
                .addComponent(this.chkStopOnSearchError =
                        new JCheckBox(Messages.getString("PreferenceDialog.StopAfterError")));

        TitlePanel.title(Messages.getString("PreferenceDialog.SerieDatabaseSource"))
                .marginBottom(0).padding(0).paddingLeft(20).addTo(this, "span, grow")
                .addComponent(this.cbxEpisodeProcessSource = MyComboBox.ofValues(SettingsProcessEpisodeSource.values()),
                        "wrap")
                .addComponent(this.chkConfirmProviderMapping =
                        new JCheckBox(Messages.getString("PreferenceDialog.ConfirmProviderMapping")));

        loadPreferenceSettings();
    }

    public void loadPreferenceSettings() {
        chkAlwaysConfirm.setSelected(settingsCtrl.settings.optionsAlwaysConfirm);
        chkMinScoreSelection.setSelected(settingsCtrl.settings.optionsMinAutomaticSelection);
        sldMinScoreSelection.setValue(settingsCtrl.settings.optionsMinAutomaticSelectionValue);
        chkDefaultSelection.setSelected(settingsCtrl.settings.optionsDefaultSelection);
        chkSubtitleExactMethod.setSelected(settingsCtrl.settings.optionSubtitleExactMatch);
        chkSubtitleKeywordMethod.setSelected(settingsCtrl.settings.optionSubtitleKeywordMatch);
        chkExcludeHearingImpaired.setSelected(settingsCtrl.settings.optionSubtitleExcludeHearingImpaired);
        chkOnlyFound.setSelected(settingsCtrl.settings.optionsShowOnlyFound);
        chkStopOnSearchError.setSelected(settingsCtrl.settings.optionsStopOnSearchError);
        cbxEpisodeProcessSource.setSelectedItem(settingsCtrl.settings.processEpisodeSource);
        chkConfirmProviderMapping.setSelected(settingsCtrl.settings.optionsConfirmProviderMapping);
    }

    public void savePreferenceSettings() {
        settingsCtrl.settings.optionsAlwaysConfirm = chkAlwaysConfirm.isSelected();
        settingsCtrl.settings.optionsMinAutomaticSelection = chkMinScoreSelection.isSelected();
        settingsCtrl.settings.optionsMinAutomaticSelectionValue = sldMinScoreSelection.getValue();
        settingsCtrl.settings.optionsDefaultSelection = chkDefaultSelection.isSelected();
        settingsCtrl.settings.optionSubtitleExactMatch = chkSubtitleExactMethod.isSelected();
        settingsCtrl.settings.optionSubtitleKeywordMatch = chkSubtitleKeywordMethod.isSelected();
        settingsCtrl.settings.optionSubtitleExcludeHearingImpaired = chkExcludeHearingImpaired.isSelected();
        settingsCtrl.settings.optionsShowOnlyFound = chkOnlyFound.isSelected();
        settingsCtrl.settings.optionsStopOnSearchError = chkStopOnSearchError.isSelected();
        settingsCtrl.settings.processEpisodeSource = cbxEpisodeProcessSource.getSelectedItem();
        settingsCtrl.settings.optionsConfirmProviderMapping = chkConfirmProviderMapping.isSelected();
        pnlDefaultSelection.savePreferenceSettings();
    }

    @Override
    public boolean hasValidSettings() {
        return pnlDefaultSelection.hasValidSettings();
    }
}
