package org.lodder.subtools.multisubdownloader.subtitleprovider.addic7ed.proxy.gestdown.model;

import java.io.Serial;
import java.util.UUID;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.data.ProviderId;

@NullMarked
public class Addic7edProxyGestdownSerieId extends ProviderId {

    @Serial
    private static final long serialVersionUID = 1L;

    @val @Nullable Integer tvdbId;
    @val @Nullable Integer tmdbId;

    public Addic7edProxyGestdownSerieId(String text, UUID id, @Nullable Integer tvdbId=null,
        @Nullable Integer tmdbId=null) {
        super(text, id.toString());
        this.tvdbId = tvdbId;
        this.tmdbId = tmdbId;
    }
}
