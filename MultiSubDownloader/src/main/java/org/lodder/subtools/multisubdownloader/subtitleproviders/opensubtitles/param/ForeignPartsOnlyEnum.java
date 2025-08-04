package org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.param;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;

public enum ForeignPartsOnlyEnum implements ParamIntf {
    EXCLUDE("exclude"), INCLUDE("include"), ONLY("only");

    @val @override String value;

    ForeignPartsOnlyEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
