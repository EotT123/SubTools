package org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.param;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;

@NullMarked
public enum TrustedSourcesEnum implements ParamIntf {
    INCLUDE("include"),
    ONLY("only");

    @val @override String value;

    TrustedSourcesEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
