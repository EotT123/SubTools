package org.lodder.subtools.multisubdownloader.gui.panels.preference;

import static java.util.function.Predicate.*;
import static org.lodder.subtools.multisubdownloader.Messages.*;

import javax.swing.*;
import java.io.Serial;
import java.nio.file.Path;

import net.miginfocom.swing.MigLayout;
import org.lodder.subtools.multisubdownloader.gui.extra.JListWithImages;
import org.lodder.subtools.multisubdownloader.gui.extra.JListWithImages.LabelPanel;
import org.lodder.subtools.multisubdownloader.gui.extra.MemoryFolderChooser;
import org.lodder.subtools.multisubdownloader.gui.extra.PanelCheckBox;
import org.lodder.subtools.multisubdownloader.gui.extra.TitlePanel;
import org.lodder.subtools.multisubdownloader.gui.jcomponent.jtextfield.MyPasswordField;
import org.lodder.subtools.multisubdownloader.gui.jcomponent.jtextfield.MyTextFieldString;
import org.lodder.subtools.multisubdownloader.settings.SettingsControl;
import org.lodder.subtools.multisubdownloader.settings.model.PathMatchType;
import org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.OpenSubtitlesApi;

public class SerieProvidersPanel extends JPanel implements PreferencePanelIntf {

    @Serial
    private static final long serialVersionUID = -5458593307643063563L;

    private final SettingsControl settingsCtrl;
    private final JCheckBox chkSourceAddic7ed;
    private final JCheckBox chkUserAddic7edLogin;
    private final JCheckBox chkSourceAddic7edProxy;
    private final MyTextFieldString txtAddic7edUsername;
    private final MyPasswordField txtAddic7edPassword;
    private final JCheckBox chkSourceTvSubtitles;
    private final JCheckBox chkSourcePodnapisi;
    private final JCheckBox chkSourceOpenSubtitles;
    private final JCheckBox chkUserOpenSubtitlesLogin;
    private final MyTextFieldString txtOpenSubtitlesUsername;
    private final MyPasswordField txtOpenSubtitlesPassword;
    private final JCheckBox chkSourceSubscene;
    private final JCheckBox chkSourceLocal;
    private final JListWithImages<Path> localSourcesFoldersList;

    public SerieProvidersPanel(SettingsControl settingsCtrl) {
        super(new MigLayout("insets 0, fill, nogrid"));
        this.settingsCtrl = settingsCtrl;

        JPanel titlePanel = TitlePanel.title(getText("PreferenceDialog.SelectPreferredSources"))
                .addTo(this, "span, grow");

        {
            // ADDIC7ED
            this.chkSourceAddic7ed = new JCheckBox("Addic7ed");
            this.chkUserAddic7edLogin = new JCheckBox(getText("PreferenceDialog.UseAddic7edLogin"));
            this.chkSourceAddic7edProxy = new JCheckBox(getText("PreferenceDialog.Proxy"));

            PanelCheckBox.checkbox(chkSourceAddic7ed).panelOnNewLine().addTo(titlePanel, "wrap")
                    .addComponent("wrap", chkSourceAddic7edProxy)
                    .addComponent(PanelCheckBox.checkbox(chkUserAddic7edLogin).panelOnNewLine()
                            .panelLayout(new MigLayout("insets 0, novisualpadding")).build()
                            .addComponent(new JLabel(getText("PreferenceDialog.Username")))
                            .addComponent("wrap", this.txtAddic7edUsername =
                                    MyTextFieldString.builder().requireValue().build().columns(20))
                            .addComponent(new JLabel(getText("PreferenceDialog.Password")))
                            .addComponent(this.txtAddic7edPassword =
                                    MyPasswordField.builder().requireValue().build().columns(20)));

            // TV SUBTITLES
            this.chkSourceTvSubtitles = new JCheckBox("Tv Subtitles").addTo(titlePanel, "wrap");

            // PODNAPISI
            this.chkSourcePodnapisi = new JCheckBox("Podnapisi").addTo(titlePanel, "wrap");

            // OPENSUBTITLES
            this.chkSourceOpenSubtitles = new JCheckBox("OpenSubtitles");
            this.chkUserOpenSubtitlesLogin = new JCheckBox(getText("PreferenceDialog.UseOpenSubtitlesLogin"));
            PanelCheckBox.checkbox(chkSourceOpenSubtitles).panelOnNewLine().addTo(titlePanel, "wrap")
                    .addComponent(PanelCheckBox.checkbox(chkUserOpenSubtitlesLogin).panelOnNewLine()
                            .panelLayout(new MigLayout("insets 0, novisualpadding")).build()
                            .addComponent(new JLabel(getText("PreferenceDialog.Username")))
                            .addComponent("wrap", txtOpenSubtitlesUsername =
                                    MyTextFieldString.builder().requireValue().build().columns(20))
                            .addComponent(new JLabel(getText("PreferenceDialog.Password")))
                            .addComponent(txtOpenSubtitlesPassword =
                                    MyPasswordField.builder().requireValue().build().columns(20)));

            // SUBSCENE
            this.chkSourceSubscene = new JCheckBox("Subscene").addTo(titlePanel, "wrap");

            // LOCAL
            this.chkSourceLocal = new JCheckBox(getText("PreferenceDialog.Local"));
            JScrollPane scrLocalSources =
                new JScrollPane().viewportView(this.localSourcesFoldersList = new JListWithImages<>());
            JButton btnBrowseLocalSources = new JButton(getText("PreferenceDialog.AddFolder"))
                    .actionListener(() -> MemoryFolderChooser.getInstance()
                            .selectDirectory(this, getText("PreferenceDialog.SelectFolder"))
                            .map(Path::toAbsolutePath).filter(not(localSourcesFoldersList::contains))
                            .ifPresent(path -> localSourcesFoldersList.addItem(PathMatchType.FOLDER.image, path)));
            JButton btnRemoveLocalSources = new JButton(getText("PreferenceDialog.DeleteFolder"))
                    .actionListener(localSourcesFoldersList::removeSelectedItem);

            PanelCheckBox.checkbox(chkSourceLocal).panelOnNewLine().addTo(titlePanel)
                    .addComponent("aligny top, gapy 5px",
                            new JLabel(getText("PreferenceDialog.LocalFolderWithSubtitles")))
                    .addComponent("wrap",
                            new JPanel(new MigLayout("insets 0", "[grow, nogrid]"))
                                    .addComponent("split", btnBrowseLocalSources)
                                    .addComponent("wrap", btnRemoveLocalSources)
                                    .addComponent("wrap", scrLocalSources));
        }

        loadPreferenceSettings();
    }

    public void loadPreferenceSettings() {
        chkSourceAddic7ed.setSelected(settingsCtrl.settings.serieSourceAddic7ed);
        chkUserAddic7edLogin.setSelected(settingsCtrl.settings.loginAddic7edEnabled);
        chkSourceAddic7edProxy.setSelected(settingsCtrl.settings.serieSourceAddic7edProxy);
        // chkSourceAddic7edProxy.setEnabled(settingsCtrl.settings.serieSourceAddic7ed);
        txtAddic7edUsername.setText(settingsCtrl.settings.loginAddic7edUsername);
        txtAddic7edPassword.setText(settingsCtrl.settings.loginAddic7edPassword);

        chkSourceTvSubtitles.setSelected(settingsCtrl.settings.serieSourceTvSubtitles);
        chkSourcePodnapisi.setSelected(settingsCtrl.settings.serieSourcePodnapisi);
        chkSourceOpenSubtitles.setSelected(settingsCtrl.settings.serieSourceOpensubtitles);
        chkUserOpenSubtitlesLogin.setSelected(settingsCtrl.settings.loginOpenSubtitlesEnabled);
        txtOpenSubtitlesUsername.setText(settingsCtrl.settings.loginOpenSubtitlesUsername);
        txtOpenSubtitlesPassword.setText(settingsCtrl.settings.loginOpenSubtitlesPassword);
        chkSourceSubscene.setSelected(settingsCtrl.settings.serieSourceSubscene);
        chkSourceLocal.setSelected(settingsCtrl.settings.serieSourceLocal);
        settingsCtrl.settings.localSourcesFolders.forEach(
                path -> localSourcesFoldersList.addItem(PathMatchType.FOLDER.image, path));
    }

    public void savePreferenceSettings() {
        settingsCtrl.settings.serieSourceAddic7ed = chkSourceAddic7ed.isSelected();
        settingsCtrl.settings.loginAddic7edEnabled = chkUserAddic7edLogin.isSelected();
        settingsCtrl.settings.serieSourceAddic7edProxy = chkSourceAddic7edProxy.isSelected();
        settingsCtrl.settings.loginAddic7edUsername = txtAddic7edUsername.getText();
        settingsCtrl.settings.loginAddic7edPassword = new String(txtAddic7edPassword.getPassword());
        settingsCtrl.settings.serieSourceTvSubtitles = chkSourceTvSubtitles.isSelected();
        settingsCtrl.settings.serieSourcePodnapisi = chkSourcePodnapisi.isSelected();
        settingsCtrl.settings.serieSourceOpensubtitles = chkSourceOpenSubtitles.isSelected();
        settingsCtrl.settings.loginOpenSubtitlesEnabled = chkUserOpenSubtitlesLogin.isSelected();
        settingsCtrl.settings.loginOpenSubtitlesUsername = txtOpenSubtitlesUsername.getText();
        settingsCtrl.settings.loginOpenSubtitlesPassword = new String(txtOpenSubtitlesPassword.getPassword());
        settingsCtrl.settings.serieSourceSubscene = chkSourceSubscene.isSelected();
        settingsCtrl.settings.serieSourceLocal = chkSourceLocal.isSelected();
        settingsCtrl.settings.localSourcesFolders =
                localSourcesFoldersList.stream().map(LabelPanel::getObject).toList();
    }

    private boolean hasValidSettingsAddic7ed() {
        return txtAddic7edUsername.hasValidValue() && txtAddic7edPassword.hasValidValue();
    }

    private boolean hasValidSettingsOpenSubtitles() {
        if (!txtOpenSubtitlesUsername.hasValidValue() || !txtOpenSubtitlesPassword.hasValidValue()) {
            return false;
        }
        if (chkUserOpenSubtitlesLogin.isSelected() &&
                !OpenSubtitlesApi.isValidCredentials(txtOpenSubtitlesUsername.getText(),
                        new String(txtOpenSubtitlesPassword.getPassword()))) {
            txtOpenSubtitlesUsername.setErrorBorder();
            txtOpenSubtitlesPassword.setErrorBorder();
            return false;
        }
        return true;
    }

    @Override
    public boolean hasValidSettings() {
        return hasValidSettingsAddic7ed() && hasValidSettingsOpenSubtitles();
    }
}
