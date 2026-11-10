package org.lodder.subtools.multisubdownloader.subtitleprovider.addic7ed.proxy.subvault.model;

import java.io.Serial;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.data.ProviderId;

@NullMarked
public class Addic7edProxySubvaultSerieId extends ProviderId {

    @Serial
    private static final long serialVersionUID = 1L;

    @val @Nullable Integer tvdbId;
    @val @Nullable Integer tmdbId;

    public Addic7edProxySubvaultSerieId(String text, String id, @Nullable Integer tvdbId=null,
        @Nullable Integer tmdbId=null) {
        super(text, id);
        this.tvdbId = tvdbId;
        this.tmdbId = tmdbId;
    }
}
