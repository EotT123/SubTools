package org.lodder.subtools.multisubdownloader.subtitleproviders.subscene.model;

import java.io.Serial;

import org.lodder.subtools.sublibrary.data.ProviderId;

public abstract class SubSceneId extends ProviderId {

    @Serial
    private static final long serialVersionUID = 5858875211782260667L;

    public SubSceneId(String name, String id) {
        super(name, id);
    }

}
