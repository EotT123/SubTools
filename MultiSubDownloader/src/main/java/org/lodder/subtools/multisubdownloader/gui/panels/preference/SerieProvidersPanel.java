package org.lodder.subtools.multisubdownloader.gui.panels.preference;

import static java.util.function.Predicate.*;

import javax.swing.*;
import java.io.Serial;
import java.nio.file.Path;

import net.miginfocom.swing.MigLayout;
import org.lodder.subtools.multisubdownloader.Messages;
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

        JPanel titelPanel = TitlePanel.title(Messages.getString("PreferenceDialog.SelectPreferredSources")).addTo(this, "span, grow");

        {
            // ADDIC7ED
            this.chkSourceAddic7ed = new JCheckBox("Addic7ed");
            this.chkUserAddic7edLogin = new JCheckBox(Messages.getString("PreferenceDialog.UseAddic7edLogin"));
            this.chkSourceAddic7edProxy = new JCheckBox(Messages.getString("PreferenceDialog.Proxy"));

            PanelCheckBox.checkbox(chkSourceAddic7ed).panelOnNewLine().addTo(titelPanel, "wrap")
                    .addComponent("wrap", chkSourceAddic7edProxy)
                    .addComponent(PanelCheckBox.checkbox(chkUserAddic7edLogin).panelOnNewLine()
                            .panelLayout(new MigLayout("insets 0, novisualpadding")).build()
                            .addComponent(new JLabel(Messages.getString("PreferenceDialog.Username")))
                            .addComponent("wrap", this.txtAddic7edUsername = MyTextFieldString.builder().requireValue().build().withColumns(20))
                            .addComponent(new JLabel(Messages.getString("PreferenceDialog.Password")))
                            .addComponent(this.txtAddic7edPassword = MyPasswordField.builder().requireValue().build().withColumns(20)));

            // TV SUBTITLES
            this.chkSourceTvSubtitles = new JCheckBox("Tv Subtitles").addTo(titelPanel, "wrap");

            // PODNAPISI
            this.chkSourcePodnapisi = new JCheckBox("Podnapisi").addTo(titelPanel, "wrap");

            // OPENSUBTITLES
            this.chkSourceOpenSubtitles = new JCheckBox("OpenSubtitles");
            this.chkUserOpenSubtitlesLogin = new JCheckBox(Messages.getString("PreferenceDialog.UseOpenSubtitlesLogin"));
            PanelCheckBox.checkbox(chkSourceOpenSubtitles).panelOnNewLine().addTo(titelPanel, "wrap")
                    .addComponent(PanelCheckBox.checkbox(chkUserOpenSubtitlesLogin).panelOnNewLine()
                            .panelLayout(new MigLayout("insets 0, novisualpadding")).build()
                            .addComponent(new JLabel(Messages.getString("PreferenceDialog.Username")))
                            .addComponent("wrap", txtOpenSubtitlesUsername = MyTextFieldString.builder().requireValue().build().withColumns(20))
                            .addComponent(new JLabel(Messages.getString("PreferenceDialog.Password")))
                            .addComponent(txtOpenSubtitlesPassword = MyPasswordField.builder().requireValue().build().withColumns(20)));

            // SUBSCENE
            this.chkSourceSubscene = new JCheckBox("Subscene").addTo(titelPanel, "wrap");

            // LOCAL
            this.chkSourceLocal = new JCheckBox(Messages.getString("PreferenceDialog.Local"));
            JScrollPane scrLocalSources =
                    new JScrollPane().withViewPort(this.localSourcesFoldersList = JListWithImages.createForType(Path.class).distinctValues().build());
            JButton btnBrowseLocalSources = new JButton(Messages.getString("PreferenceDialog.AddFolder"))
                    .withActionListener(() -> MemoryFolderChooser.getInstance()
                            .selectDirectory(this, Messages.getString("PreferenceDialog.SelectFolder"))
                            .map(Path::toAbsolutePath).filter(not(localSourcesFoldersList::contains))
                            .ifPresent(path -> localSourcesFoldersList.addItem(PathMatchType.FOLDER.getImage(), path)));
            JButton btnRemoveLocalSources = new JButton(Messages.getString("PreferenceDialog.DeleteFolder"))
                    .withActionListener(localSourcesFoldersList::removeSelectedItem);

            PanelCheckBox.checkbox(chkSourceLocal).panelOnNewLine().addTo(titelPanel)
                    .addComponent("aligny top, gapy 5px", new JLabel(Messages.getString("PreferenceDialog.LocalFolderWithSubtitles")))
                    .addComponent("wrap", new JPanel(new MigLayout("insets 0", "[grow, nogrid]")).addComponent("split", btnBrowseLocalSources)
                            .addComponent("wrap", btnRemoveLocalSources).addComponent("wrap", scrLocalSources));
        }

        loadPreferenceSettings();
    }

    public void loadPreferenceSettings() {
        chkSourceAddic7ed.setSelected(settingsCtrl.getSettings().serieSourceAddic7ed);
        chkUserAddic7edLogin.setSelected(settingsCtrl.getSettings().loginAddic7edEnabled);
        chkSourceAddic7edProxy.setSelected(settingsCtrl.getSettings().serieSourceAddic7edProxy);
        // chkSourceAddic7edProxy.setEnabled(settingsCtrl.getSettings().serieSourceAddic7ed);
        txtAddic7edUsername.setText(settingsCtrl.getSettings().loginAddic7edUsername);
        txtAddic7edPassword.setText(settingsCtrl.getSettings().loginAddic7edPassword);

        chkSourceTvSubtitles.setSelected(settingsCtrl.getSettings().serieSourceTvSubtitles);
        chkSourcePodnapisi.setSelected(settingsCtrl.getSettings().serieSourcePodnapisi);
        chkSourceOpenSubtitles.setSelected(settingsCtrl.getSettings().serieSourceOpensubtitles);
        chkUserOpenSubtitlesLogin.setSelected(settingsCtrl.getSettings().loginOpenSubtitlesEnabled);
        txtOpenSubtitlesUsername.setText(settingsCtrl.getSettings().loginOpenSubtitlesUsername);
        txtOpenSubtitlesPassword.setText(settingsCtrl.getSettings().loginOpenSubtitlesPassword);
        chkSourceSubscene.setSelected(settingsCtrl.getSettings().serieSourceSubscene);
        chkSourceLocal.setSelected(settingsCtrl.getSettings().serieSourceLocal);
        settingsCtrl.getSettings().localSourcesFolders.forEach(path -> localSourcesFoldersList.addItem(PathMatchType.FOLDER.getImage(), path));
    }

    public void savePreferenceSettings() {
        settingsCtrl.getSettings().serieSourceAddic7ed = chkSourceAddic7ed.isSelected();
        settingsCtrl.getSettings().loginAddic7edEnabled = chkUserAddic7edLogin.isSelected();
        settingsCtrl.getSettings().serieSourceAddic7edProxy = chkSourceAddic7edProxy.isSelected();
        settingsCtrl.getSettings().loginAddic7edUsername = txtAddic7edUsername.getText();
        settingsCtrl.getSettings().loginAddic7edPassword = new String(txtAddic7edPassword.getPassword());
        settingsCtrl.getSettings().serieSourceTvSubtitles = chkSourceTvSubtitles.isSelected();
        settingsCtrl.getSettings().serieSourcePodnapisi = chkSourcePodnapisi.isSelected();
        settingsCtrl.getSettings().serieSourceOpensubtitles = chkSourceOpenSubtitles.isSelected();
        settingsCtrl.getSettings().loginOpenSubtitlesEnabled = chkUserOpenSubtitlesLogin.isSelected();
        settingsCtrl.getSettings().loginOpenSubtitlesUsername = txtOpenSubtitlesUsername.getText();
        settingsCtrl.getSettings().loginOpenSubtitlesPassword = new String(txtOpenSubtitlesPassword.getPassword());
        settingsCtrl.getSettings().serieSourceSubscene = chkSourceSubscene.isSelected();
        settingsCtrl.getSettings().serieSourceLocal = chkSourceLocal.isSelected();
        settingsCtrl.getSettings().localSourcesFolders = localSourcesFoldersList.stream().map(LabelPanel::getObject).toList();
    }

    private boolean hasValidSettingsAddic7ed() {
        return txtAddic7edUsername.hasValidValue() && txtAddic7edPassword.hasValidValue();
    }

    private boolean hasValidSettingsOpenSubtitles() {
        if (!txtOpenSubtitlesUsername.hasValidValue() || !txtOpenSubtitlesPassword.hasValidValue()) {
            return false;
        }
        if (chkUserOpenSubtitlesLogin.isSelected() && !OpenSubtitlesApi.isValidCredentials(txtOpenSubtitlesUsername.getText(),
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
