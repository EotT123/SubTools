package org.lodder.subtools.multisubdownloader.lib.library;

import manifold.ext.props.rt.api.val;

public enum LibraryActionType {
    NOTHING("PreferenceDialog.Action.Nothing"),
    RENAME("PreferenceDialog.Action.Rename"),
    MOVE("PreferenceDialog.Action.Move"),
    MOVE_AND_RENAME("PreferenceDialog.Action.MoveAndRename");

    @val String msgCode;

    LibraryActionType(String msgCode) {
        this.msgCode = msgCode;
    }
}
