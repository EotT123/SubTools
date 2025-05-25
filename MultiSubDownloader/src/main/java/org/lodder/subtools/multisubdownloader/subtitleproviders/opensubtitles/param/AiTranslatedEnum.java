package org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.param;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;

public enum AiTranslatedEnum implements ParamIntf {
    EXCLUDE("exclude"), INCLUDE("include");

    @val @override String value;

    AiTranslatedEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
