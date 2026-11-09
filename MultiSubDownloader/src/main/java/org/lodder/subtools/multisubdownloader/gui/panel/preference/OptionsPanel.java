package org.lodder.subtools.multisubdownloader.gui.panel.preference;

import static org.lodder.subtools.multisubdownloader.Messages.*;

import javax.swing.*;
import java.io.Serial;

import net.miginfocom.swing.MigLayout;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.gui.extra.BoxModelProperties;
import org.lodder.subtools.multisubdownloader.gui.extra.PanelCheckBox;
import org.lodder.subtools.multisubdownloader.gui.extra.TitlePanel;
import org.lodder.subtools.multisubdownloader.settings.SettingsControl;
import org.lodder.subtools.multisubdownloader.settings.model.Settings;
import org.lodder.subtools.multisubdownloader.settings.model.SettingsProcessEpisodeSource;

@NullMarked
public class OptionsPanel extends JPanel implements PreferencePanelIntf {

    @Serial private static final long serialVersionUID = 1L;

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
    private final JComboBox<SettingsProcessEpisodeSource> cbxEpisodeProcessSource;
    private final JCheckBox chkConfirmProviderMapping;

    public OptionsPanel() {
        super(new MigLayout("insets 0, fill, nogrid"));

        new TitlePanel(
            title:getText("PreferenceDialog.DownloadOptions"),
            margin:new BoxModelProperties(left:20),
            padding:new BoxModelProperties(left:20))
            .addToPanel(this, "span, grow, wrap")
            .addComponent(this.chkAlwaysConfirm =
                new JCheckBox(getText("PreferenceDialog.CheckBeforeDownloading")), "wrap")
            .addComponent("wrap, grow",
                new PanelCheckBox(
                    checkbox:this.chkMinScoreSelection =
                        new JCheckBox(getText("PreferenceDialog.MinAutomaticScoreSelection")),
                    panelOnNewLine:false
                    )
                    .addComponent(this.sldMinScoreSelection = new JSlider().minimum(0).maximum(100), "wrap"))
            .addComponent("wrap, grow",
                new PanelCheckBox(
                    checkbox:this.chkDefaultSelection =
                        new JCheckBox(getText("PreferenceDialog.DefaultSelection"), null, true),
                    panelOnNewLine:true
                    )
                    .addComponent(this.pnlDefaultSelection = new DefaultSelectionPanel()));

        new TitlePanel(
            title:getText("PreferenceDialog.SearchFilter"),
            margin:new BoxModelProperties(left:20),
            padding:new BoxModelProperties(left:20))
            .addToPanel(this, "span, grow, wrap")
            .addComponent(
                this.chkSubtitleExactMethod = new JCheckBox(getText("PreferenceDialog.SearchFilterExact")),
                "wrap")
            .addComponent(this.chkSubtitleKeywordMethod =
                new JCheckBox(getText("PreferenceDialog.SearchFilterKeyword")), "wrap")
            .addComponent(this.chkExcludeHearingImpaired =
                new JCheckBox(getText("PreferenceDialog.ExcludeHearingImpaired")));

        new TitlePanel(
            title:getText("PreferenceDialog.TableOptions"),
            margin:new BoxModelProperties(bottom:0),
            padding:new BoxModelProperties(left:20))
            .addToPanel(this, "span, grow, wrap")
            .addComponent(this.chkOnlyFound = new JCheckBox(getText("PreferenceDialog.ShowOnlyFound")));

        new TitlePanel(
            title:getText("PreferenceDialog.ErrorHandlingOption"),
            margin:new BoxModelProperties(left:20),
            padding:new BoxModelProperties(left:20))
            .addToPanel(this, "span, grow, wrap")
            .addComponent(this.chkStopOnSearchError = new JCheckBox(getText("PreferenceDialog.StopAfterError")));

        new TitlePanel(
            title:getText("PreferenceDialog.SerieDatabaseSource"),
            margin:new BoxModelProperties(left:20),
            padding:new BoxModelProperties(left:20))
            .addToPanel(this, "span, grow")
            .addComponent(this.cbxEpisodeProcessSource = new JComboBox<>(SettingsProcessEpisodeSource.values()),
                "wrap")
            .addComponent(this.chkConfirmProviderMapping =
                new JCheckBox(getText("PreferenceDialog.ConfirmProviderMapping")));

        loadPreferenceSettings();
    }

    public void loadPreferenceSettings() {
        Settings settings = SettingsControl.settings;
        chkAlwaysConfirm.setSelected(settings.optionsAlwaysConfirm);
        chkMinScoreSelection.setSelected(settings.optionsMinAutomaticSelection);
        sldMinScoreSelection.setValue(settings.optionsMinAutomaticSelectionValue);
        chkDefaultSelection.setSelected(settings.optionsDefaultSelection);
        chkSubtitleExactMethod.setSelected(settings.optionSubtitleExactMatch);
        chkSubtitleKeywordMethod.setSelected(settings.optionSubtitleKeywordMatch);
        chkExcludeHearingImpaired.setSelected(settings.optionSubtitleExcludeHearingImpaired);
        chkOnlyFound.setSelected(settings.optionsShowOnlyFound);
        chkStopOnSearchError.setSelected(settings.optionsStopOnSearchError);
        cbxEpisodeProcessSource.setSelectedItem(settings.processEpisodeSource);
        chkConfirmProviderMapping.setSelected(settings.optionsConfirmProviderMapping);
    }

    public void savePreferenceSettings() {
        Settings settings = SettingsControl.settings;
        settings.optionsAlwaysConfirm = chkAlwaysConfirm.isSelected();
        settings.optionsMinAutomaticSelection = chkMinScoreSelection.isSelected();
        settings.optionsMinAutomaticSelectionValue = sldMinScoreSelection.getValue();
        settings.optionsDefaultSelection = chkDefaultSelection.isSelected();
        settings.optionSubtitleExactMatch = chkSubtitleExactMethod.isSelected();
        settings.optionSubtitleKeywordMatch = chkSubtitleKeywordMethod.isSelected();
        settings.optionSubtitleExcludeHearingImpaired = chkExcludeHearingImpaired.isSelected();
        settings.optionsShowOnlyFound = chkOnlyFound.isSelected();
        settings.optionsStopOnSearchError = chkStopOnSearchError.isSelected();
        settings.processEpisodeSource = cbxEpisodeProcessSource.getSelectedValue();
        settings.optionsConfirmProviderMapping = chkConfirmProviderMapping.isSelected();
        pnlDefaultSelection.savePreferenceSettings();
    }

    @Override
    public boolean hasValidSettings() {
        return pnlDefaultSelection.hasValidSettings();
    }
}
