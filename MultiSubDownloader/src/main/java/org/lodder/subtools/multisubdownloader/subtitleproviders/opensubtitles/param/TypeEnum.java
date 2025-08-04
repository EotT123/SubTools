package org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.param;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;

public enum TypeEnum implements ParamIntf {
    MOVIE("movie"), EPISODE("episode"), ALL("all");

    @val @override String value;

    TypeEnum(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return value;
    }
}
