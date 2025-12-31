package org.lodder.subtools.multisubdownloader;

import javax.swing.*;
import java.util.List;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.gui.dialog.SelectDialog;
import org.lodder.subtools.sublibrary.data.UserInteractionSettingsIntf;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.model.Subtitle;

@NullMarked
public class UserInteractionHandlerGUI extends org.lodder.subtools.sublibrary.userinteraction.UserInteractionHandlerGUI
    implements UserInteractionHandler {

    public UserInteractionHandlerGUI(UserInteractionSettingsIntf settings, JFrame frame) {
        super(settings, frame);
    }

    @Override
    public List<Subtitle> selectSubtitles(Release release) {
        List<Integer> selection = new SelectDialog(frame, release.matchingSubs, release).getSelection();
        return selection.stream().map(release.matchingSubs::get).toList();

    }

    @Override
    public void dryRunOutput(Release release) {
        // TODO Auto-generated method stub
    }
}
