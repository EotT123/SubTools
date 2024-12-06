package org.lodder.subtools.multisubdownloader.gui.panels.preference;

import javax.swing.*;
import java.io.Serial;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;

import net.miginfocom.swing.MigLayout;
import org.apache.commons.lang3.StringUtils;
import org.lodder.subtools.multisubdownloader.GUI;
import org.lodder.subtools.multisubdownloader.Messages;
import org.lodder.subtools.multisubdownloader.gui.extra.JListWithImages;
import org.lodder.subtools.multisubdownloader.gui.extra.JListWithImages.LabelPanel;
import org.lodder.subtools.multisubdownloader.gui.extra.MemoryFolderChooser;
import org.lodder.subtools.multisubdownloader.gui.extra.PanelCheckBox;
import org.lodder.subtools.multisubdownloader.gui.extra.TitlePanel;
import org.lodder.subtools.multisubdownloader.gui.jcomponent.jcombobox.MyComboBox;
import org.lodder.subtools.multisubdownloader.gui.jcomponent.jtextfield.MyTextFieldInteger;
import org.lodder.subtools.multisubdownloader.gui.jcomponent.jtextfield.MyTextFieldString;
import org.lodder.subtools.multisubdownloader.settings.SettingsControl;
import org.lodder.subtools.multisubdownloader.settings.model.PathMatchType;
import org.lodder.subtools.multisubdownloader.settings.model.PathOrRegex;
import org.lodder.subtools.multisubdownloader.settings.model.UpdateCheckPeriod;
import org.lodder.subtools.multisubdownloader.settings.model.UpdateType;
import org.lodder.subtools.sublibrary.Language;

public class GeneralPanel extends JPanel implements PreferencePanelIntf {

    @Serial
    private static final long serialVersionUID = -5458593307643063563L;

    private final GUI gui;
    private final SettingsControl settingsCtrl;
    private final MyComboBox<Language> cbxLanguage;
    private final JListWithImages<Path> defaultIncomingFoldersList;
    private final JListWithImages<PathOrRegex> excludeList;
    private final MyComboBox<UpdateCheckPeriod> cbxUpdateCheckPeriod;
    private final MyComboBox<UpdateType> cbxUpdateType;
    private final JCheckBox chkUseProxy;
    private final MyTextFieldString txtProxyHost;
    private final MyTextFieldInteger txtProxyPort;

    public GeneralPanel(GUI gui, SettingsControl settingsCtrl) {
        super(new MigLayout("fill, nogrid"));
        this.gui = gui;
        this.settingsCtrl = settingsCtrl;

        JPanel settingsPanel = TitlePanel.title(Messages.getString("PreferenceDialog.Settings"))
                .padding(0).paddingLeft(20).useGrid().fillContents(false).addTo(this, "span, grow, wrap");
        {

            {
                new JLabel(Messages.getString("PreferenceDialog.Language")).addTo(settingsPanel);

                this.cbxLanguage = new MyComboBox<>(Messages.getAvailableLanguages(), Language.class)
                        .withToMessageStringRenderer(Language::getMsgCode)
                        .addTo(settingsPanel, "wrap");
            }

            {
                new JLabel(Messages.getString("PreferenceDialog.DefaultIncomingFolder")).addTo(settingsPanel,
                        "aligny center, span 1 2");

                new JScrollPane()
                        .viewportView(this.defaultIncomingFoldersList =
                                JListWithImages.createForType(Path.class).distinctValues().build())
                        .addTo(settingsPanel, "growx, span, wrap");

                new JButton(Messages.getString("PreferenceDialog.AddFolder"))
                        .actionListener(
                                () -> MemoryFolderChooser.getInstance()
                                        .selectDirectory(settingsPanel,
                                                Messages.getString("PreferenceDialog.SelectFolder"))
                                        .map(Path::toAbsolutePath)
                                        .filter(path -> !defaultIncomingFoldersList.contains(path))
                                        .ifPresent(
                                                path -> defaultIncomingFoldersList.addItem(PathMatchType.FOLDER.image,
                                                        path)))
                        .addTo(settingsPanel, "span, split 2");

                new JButton(Messages.getString("PreferenceDialog.DeleteFolder"))
                        .actionListener(defaultIncomingFoldersList::removeSelectedItem)
                        .addTo(settingsPanel, "wrap, gapbottom 10px");
            }
            {
                new JLabel(Messages.getString("PreferenceDialog.ExcludeList")).addTo(settingsPanel,
                        "aligny center, span 1 2");

                new JScrollPane()
                        .viewportView(this.excludeList =
                                JListWithImages.createForType(PathOrRegex.class).distinctValues().build())
                        .addTo(settingsPanel, "growx, span, wrap");

                Consumer<PathMatchType> addExcludeItemConsumer = type -> {
                    if (type == PathMatchType.FOLDER) {
                        MemoryFolderChooser.getInstance()
                                .selectDirectory(settingsPanel,
                                        Messages.getString("PreferenceDialog.SelectExcludeFolder"))
                                .map(Path::toAbsolutePath)
                                .map(PathOrRegex::new)
                                .ifPresent(pathOrRegex -> excludeList.addItem(pathOrRegex.image, pathOrRegex));
                    } else if (type == PathMatchType.REGEX) {
                        String regex = JOptionPane.showInputDialog(Messages.getString("PreferenceDialog.EnterRegex"));
                        if (StringUtils.isNotBlank(regex)) {
                            excludeList.addItem(PathMatchType.REGEX.image, new PathOrRegex(regex));
                        }
                    }
                };

                new JButton(Messages.getString("PreferenceDialog.AddFolder"))
                        .actionListener(() -> addExcludeItemConsumer.accept(PathMatchType.FOLDER))
                        .addTo(settingsPanel, "span, split 3");

                new JButton(Messages.getString("PreferenceDialog.DeleteFolder"))
                        .actionListener(excludeList::removeSelectedItem)
                        .addTo(settingsPanel);

                new JButton(Messages.getString("PreferenceDialog.RegexToevoegen"))
                        .actionListener(() -> addExcludeItemConsumer.accept(PathMatchType.REGEX))
                        .addTo(settingsPanel);
            }
        }

        {

            JPanel updatePanel = TitlePanel.title(Messages.getString("PreferenceDialog.Update"))
                    .padding(0).paddingLeft(20).useGrid().fillContents(false).addTo(this, "span, grow, wrap");
            {
                new JLabel(Messages.getString("PreferenceDialog.NewUpdateCheck")).addTo(updatePanel);
                this.cbxUpdateCheckPeriod = new MyComboBox<>(UpdateCheckPeriod.values())
                        .withToMessageStringRenderer(UpdateCheckPeriod::getLangCode)
                        .addTo(updatePanel, "wrap");
                new JLabel(Messages.getString("PreferenceDialog.UpdateType")).addTo(updatePanel);
                this.cbxUpdateType = new MyComboBox<>(UpdateType.values())
                        .withToMessageStringRenderer(UpdateType::getMsgCode).addTo(updatePanel);
            }
        }

        {

            JPanel proxyPanel = TitlePanel.title(Messages.getString("PreferenceDialog.ConfigureProxy"))
                    .padding(0).paddingLeft(20).fillContents(false).addTo(this, "span, grow");

            PanelCheckBox.checkbox(
                            this.chkUseProxy = new JCheckBox(Messages.getString("PreferenceDialog.UseProxyServer")))
                    .panelOnSameLine().panelLayout(new MigLayout("insets 0, fill")).leftGap(0).addTo(proxyPanel)
                    .addComponent(new JLabel(Messages.getString("PreferenceDialog.Hostname")))
                    .addComponent("wrap",
                            this.txtProxyHost = MyTextFieldString.builder().requireValue().build().columns(30))
                    .addComponent(new JLabel(Messages.getString("PreferenceDialog.Port")))
                    .addComponent(
                            this.txtProxyPort = MyTextFieldInteger.builder().requireValue().build().columns(5));
        }

        loadPreferenceSettings();
    }

    public void loadPreferenceSettings() {
        cbxLanguage.setSelectedItem(settingsCtrl.settings.language);
        defaultIncomingFoldersList.addItems(PathMatchType.FOLDER.image, settingsCtrl.settings.defaultIncomingFolders);
        settingsCtrl.settings.excludeList.forEach(pathOrRegex -> excludeList.addItem(pathOrRegex.image, pathOrRegex));
        cbxUpdateCheckPeriod.setSelectedItem(settingsCtrl.settings.updateCheckPeriod);
        cbxUpdateType.setSelectedItem(settingsCtrl.settings.updateType);
        chkUseProxy.setSelected(settingsCtrl.settings.generalProxyEnabled);
        txtProxyHost.setText(settingsCtrl.settings.generalProxyHost);
        txtProxyPort.setObject(settingsCtrl.settings.generalProxyPort);
    }

    public void savePreferenceSettings() {
        if (Messages.language != cbxLanguage.getSelectedItem()) {
            Messages.language = cbxLanguage.getSelectedItem();
            gui.redraw();
        }
        List<Path> defaultIncomingFolders = defaultIncomingFoldersList.stream().map(LabelPanel::getObject).toList();
        List<PathOrRegex> exclList =
                excludeList.stream().map(labelPanel -> new PathOrRegex(labelPanel.getObject().value)).toList();
        settingsCtrl.settings.language = cbxLanguage.getSelectedItem();
        settingsCtrl.settings.defaultIncomingFolders = defaultIncomingFolders;
        settingsCtrl.settings.replaceExcludeList(exclList);
        settingsCtrl.settings.updateCheckPeriod = cbxUpdateCheckPeriod.getSelectedItem();
        settingsCtrl.settings.updateType = cbxUpdateType.getSelectedItem();
        settingsCtrl.settings.generalProxyEnabled = chkUseProxy.isSelected();
        settingsCtrl.settings.generalProxyHost = txtProxyHost.getText();
        settingsCtrl.settings.generalProxyPort = txtProxyPort.getOptionalObject().orElse(80);
    }

    @Override
    public boolean hasValidSettings() {
        return txtProxyHost.hasValidValue() && txtProxyPort.hasValidValue();
    }
}
