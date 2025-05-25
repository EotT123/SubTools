package org.lodder.subtools.sublibrary.model;

import manifold.ext.props.rt.api.val;

public enum VideoSearchType {
    EPISODE("App.Episode"),
    MOVIE("App.Movie"),
    RELEASE("App.Release");

    @val String msgCode;

    VideoSearchType(String msgCode) {
        this.msgCode = msgCode;
    }
}
