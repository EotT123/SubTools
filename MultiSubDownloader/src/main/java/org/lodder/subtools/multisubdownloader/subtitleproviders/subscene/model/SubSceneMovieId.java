package org.lodder.subtools.multisubdownloader.subtitleproviders.subscene.model;

import java.io.Serial;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.Nullable;

public class SubSceneMovieId extends SubSceneId {

    @Serial
    private static final long serialVersionUID = 5858875211782260667L;

    @val @Nullable String title;
    @val @Nullable Integer year;

    public SubSceneMovieId(String text, String id, @Nullable String title=null, @Nullable Integer year=null) {
        super(text, id);
        this.title = title;
        this.year = year;
    }

}
