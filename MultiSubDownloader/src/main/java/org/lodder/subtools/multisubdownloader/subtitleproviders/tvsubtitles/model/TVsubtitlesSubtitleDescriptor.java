package org.lodder.subtools.multisubdownloader.subtitleproviders.tvsubtitles.model;

import java.io.Serial;
import java.io.Serializable;

import lombok.Builder;
import manifold.ext.props.rt.api.val;

@Builder
public class TVsubtitlesSubtitleDescriptor implements Serializable {

    @Serial
    private static final long serialVersionUID = 6423513286301479905L;
    @val String filename;
    @val String url;
    @val String rip;
    @val String author;

}
