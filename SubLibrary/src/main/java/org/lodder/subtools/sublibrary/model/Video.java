package org.lodder.subtools.sublibrary.model;

import manifold.ext.props.rt.api.val;

public abstract class Video {

    @val VideoType videoType;

    Video(VideoType videoType) {
        this.videoType = videoType;
    }
}
