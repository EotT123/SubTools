package org.lodder.subtools.multisubdownloader.subtitleproviders.subscene.model;

import java.io.Serial;

import manifold.ext.props.rt.api.val;
import org.lodder.subtools.sublibrary.data.ProviderId;

public class SubSceneId extends ProviderId {

    @Serial
    private static final long serialVersionUID = 5858875211782260667L;

    @val int season;

    public SubSceneId(String name, String id, int season) {
        super(name, id);
        this.season = season;
    }

}
