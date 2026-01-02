package org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.proxy.gestdown.model;

import static util.Utils.*;

import java.io.Serial;
import java.util.OptionalInt;
import java.util.UUID;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.data.ProviderId;

@NullMarked
public class Addic7edProxyGestdownSerieId extends ProviderId {

    @Serial
    private static final long serialVersionUID = 1L;

    @val OptionalInt tvdbId;
    @val OptionalInt tmdbId;

    public Addic7edProxyGestdownSerieId(String text, UUID id, @Nullable Integer tvdbId=null,
        @Nullable Integer tmdbId=null) {
        super(text, id.toString());
        this.tvdbId = ifNotNullOrElseGet(tvdbId, OptionalInt::of, OptionalInt::empty);
        this.tmdbId = ifNotNullOrElseGet(tmdbId, OptionalInt::of, OptionalInt::empty);
    }
}
