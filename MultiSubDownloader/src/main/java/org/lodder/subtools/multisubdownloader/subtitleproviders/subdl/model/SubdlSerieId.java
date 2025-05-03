package org.lodder.subtools.multisubdownloader.subtitleproviders.subdl.model;

import java.io.Serial;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.data.ProviderSerieId;
import subdl.Serie;
import subdl.Serie.ReleaseType;

public class SubdlSerieId extends ProviderSerieId {

    @Serial
    private static final long serialVersionUID = 5858875211782260667L;

    @val  @Nullable Integer  year;
    @val ReleaseType releaseType;

    public SubdlSerieId(String name, String id, @Nullable Integer year, Serie.ReleaseType releaseType) {
        super(name, id);
        this.year = year;
        this.releaseType = releaseType;
    }

}
