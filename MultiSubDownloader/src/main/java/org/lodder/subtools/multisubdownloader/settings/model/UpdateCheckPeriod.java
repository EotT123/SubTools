package org.lodder.subtools.multisubdownloader.settings.model;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;

@NullMarked
public enum UpdateCheckPeriod {
    MANUAL("InputPanel.UpdateInterval.Manual"),
    DAILY("InputPanel.UpdateInterval.Daily"),
    WEEKLY("InputPanel.UpdateInterval.Weekly"),
    MONTHLY("InputPanel.UpdateInterval.Monthly");

    @val String langCode;

    UpdateCheckPeriod(String langCode) {
        this.langCode = langCode;
    }
}
