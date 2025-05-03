package org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.model;

import java.io.Serial;

import manifold.ext.props.rt.api.val;
import org.lodder.subtools.sublibrary.data.ProviderId;

public class OpensubtitleId extends ProviderId {

    @Serial private static final long serialVersionUID = 5858875211782260667L;
    @val String year;

    public OpensubtitleId(String name, int id, String year) {
        super(name, String.valueOf(id));
        this.year = year;
    }

}
