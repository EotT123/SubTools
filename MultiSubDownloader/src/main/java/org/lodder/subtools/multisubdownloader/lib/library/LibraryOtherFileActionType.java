package org.lodder.subtools.multisubdownloader.lib.library;

import org.lodder.subtools.multisubdownloader.Messages;

public enum LibraryOtherFileActionType {

    NOTHING("PreferenceDialog.Action.Nothing"),
    REMOVE("PreferenceDialog.Action.Remove"),
    RENAME("PreferenceDialog.Action.Rename"),
    MOVE("PreferenceDialog.Action.Move"),
    MOVE_AND_RENAME("PreferenceDialog.Action.MoveAndRename");

    private final String msgCode;

    LibraryOtherFileActionType(String msgCode) {
        this.msgCode = msgCode;
    }

    @Override
    public String toString() {
        return Messages.getText(msgCode);
    }
}
