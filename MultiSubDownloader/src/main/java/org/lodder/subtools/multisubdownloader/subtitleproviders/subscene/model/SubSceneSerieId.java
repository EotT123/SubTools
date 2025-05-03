package org.lodder.subtools.multisubdownloader.subtitleproviders.subscene.model;

import java.io.Serial;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.Nullable;

public class SubSceneSerieId extends SubSceneId {

    @Serial
    private static final long serialVersionUID = 5858875211782260667L;

    @val @Nullable String serieName;
    @val @Nullable Integer season;

    public SubSceneSerieId(String text, String id, @Nullable String serieName=null,
        @Nullable Integer season=null) {
        super(text, id);
        this.serieName = serieName;
        this.season = season;
    }

}
