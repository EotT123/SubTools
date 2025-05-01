package org.lodder.subtools.multisubdownloader.lib.library;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import manifold.ext.props.rt.api.val;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum LibraryActionType {
    NOTHING("PreferenceDialog.Action.Nothing"),
    RENAME("PreferenceDialog.Action.Rename"),
    MOVE("PreferenceDialog.Action.Move"),
    MOVE_AND_RENAME("PreferenceDialog.Action.MoveAndRename");

    @val String msgCode;
}
