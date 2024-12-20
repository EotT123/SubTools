package org.lodder.subtools.multisubdownloader.gui.dialog;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.Serial;

import net.miginfocom.swing.MigLayout;
import org.lodder.subtools.multisubdownloader.Messages;
import org.lodder.subtools.multisubdownloader.gui.extra.progress.Messenger;
import org.lodder.subtools.multisubdownloader.gui.extra.progress.StatusMessenger;

public class ProgressDialog extends MultiSubDialog implements Messenger {

    @Serial
    private static final long serialVersionUID = -2320149791421648965L;

    private JProgressBar progressBar;
    private JLabel label;

    public ProgressDialog(JFrame frame, Cancelable sft) {
        super(frame, Messages.getText("ProgressDialog.Title"), false);
        StatusMessenger.instance.addListener(this);
        initializeUi(sft);
        setDialogLocation(frame);
        repaint();
    }

    public ProgressDialog(Cancelable sft) {
        super(Messages.getText("ProgressDialog.Title"), false);
        StatusMessenger.instance.addListener(this);
        initializeUi(sft);
        repaint();
    }

    private void initializeUi(Cancelable worker) {
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                worker.cancel(true);
            }
        });
        setBounds(100, 100, 501, 151);

        getContentPane()
            .layout(new MigLayout("", "[][475px,center][]", "[][40px:n][][]"))
            .addComponent("cell 1 0 2 1,alignx left", label = new JLabel(""))
            .addComponent("cell 1 1,grow", progressBar = new JProgressBar(0, 100).indeterminate((true)))
            .addComponent("cell 1 2 1 2,alignx left", new JButton("Stop!")
                .actionListener(_ -> worker.cancel(true))
            );
    }

    public void setMessage(String message) {
        label.text = message;
        repaint();
    }

    public String getMessage() {
        return label.text;
    }

    @Override
    public void message(String message) {
        this.message = message;
    }

    public void updateProgress(int progress) {
        if (progress == 0) {
            progressBar.setIndeterminate(true);
        } else {
            progressBar.setIndeterminate(false);
            progressBar.setValue(progress);
            progressBar.setString(Integer.toString(progress));
        }
    }
}
