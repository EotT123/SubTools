package org.lodder.subtools.multisubdownloader.subtitleproviders.tvsubtitles.model;

import java.io.Serial;
import java.io.Serializable;

import lombok.Builder;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.Nullable;

@Builder
public class TVSubtitlesSubtitleDescriptor implements Serializable {

    @Serial
    private static final long serialVersionUID = 6423513286301479905L;
    @val String filename;
    @val String url;
    @val String rip;
    @val @Nullable String author;

}
