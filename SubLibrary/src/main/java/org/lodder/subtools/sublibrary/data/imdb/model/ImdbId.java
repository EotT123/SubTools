package org.lodder.subtools.sublibrary.data.imdb.model;

import java.io.Serial;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.data.ProviderId;
import org.lodder.subtools.sublibrary.model.VideoType;

@NullMarked
public class ImdbId extends ProviderId {

    @Serial private static final long serialVersionUID = 1L;
    @val @Nullable String year;
    @val @Nullable String otherInfo;
    @val @Nullable VideoType videoType;

    public ImdbId(String name, String id, @Nullable String year, @Nullable String otherInfo,
        @Nullable VideoType videoType=null) {
        super(name, id);
        this.year = year;
        this.otherInfo = otherInfo;
        this.videoType = videoType;
    }
}
