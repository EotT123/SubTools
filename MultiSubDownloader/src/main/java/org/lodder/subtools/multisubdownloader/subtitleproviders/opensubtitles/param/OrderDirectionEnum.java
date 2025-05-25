package org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.param;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;

public enum OrderDirectionEnum implements ParamIntf {
    ASCENDING("asc"), DESCENDING("desc");

    @val @override String value;

    OrderDirectionEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
