package org.lodder.subtools.multisubdownloader.gui.panels;

import javax.swing.*;
import java.io.Serial;

import org.lodder.subtools.multisubdownloader.Messages;
import org.lodder.subtools.multisubdownloader.actions.SearchAction;
import org.lodder.subtools.sublibrary.Language;

public abstract class InputPanel extends JPanel {

    @Serial
    private static final long serialVersionUID = 7753220002440733463L;
    private JButton btnSearch;
    private JComboBox<Language> cbxLanguage;

    InputPanel() {
        createComponents();
    }

    public Language getSelectedLanguage() {
        return cbxLanguage.getSelectedValue();
    }

    public void setSelectedLanguage(Language language) {
        cbxLanguage.setSelectedItem(language);
    }

    public void addSearchAction(SearchAction searchAction) {
        if (searchAction != null) {
            btnSearch.addActionListener(event -> new Thread(searchAction).start());
        }
    }

    public void enableSearchButton() {
        btnSearch.setEnabled(true);
    }

    public void disableSearchButton() {
        this.btnSearch.setEnabled(false);
    }

    protected JButton getSearchButton() {
        return this.btnSearch;
    }

    protected JComboBox<Language> getLanguageCbx() {
        return this.cbxLanguage;
    }

    private void createComponents() {
        cbxLanguage = new JComboBox<>(Language.values()).toMessageStringRenderer(Language::getMsgCode);

        btnSearch = new JButton(Messages.getText("InputPanel.SearchForSubtitles"));
    }
}
