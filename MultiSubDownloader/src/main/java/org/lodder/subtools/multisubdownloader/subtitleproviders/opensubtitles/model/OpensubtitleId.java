package org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.model;

import java.io.Serial;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.data.ProviderId;

@NullMarked
public class OpensubtitleId extends ProviderId {

    @Serial private static final long serialVersionUID = 1L;
    @val String year;

    public OpensubtitleId(String name, int id, String year) {
        super(name, String.valueOf(id));
        this.year = year;
    }

}
