package org.lodder.subtools.multisubdownloader.settings.model;

import manifold.ext.props.rt.api.val;

public enum UpdateType {
    STABLE("InputPanel.UpdateType.Stable"),
    NIGHTLY("InputPanel.UpdateType.Nightly");

    @val String msgCode;

    UpdateType(String msgCode) {
        this.msgCode = msgCode;
    }
}
