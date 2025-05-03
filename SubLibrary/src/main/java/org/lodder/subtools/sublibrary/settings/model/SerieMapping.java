package org.lodder.subtools.sublibrary.settings.model;

import java.io.Serial;

import lombok.ToString;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.Nullable;

@ToString
public class SerieMapping extends ReleaseMapping {

    @Serial
    private static final long serialVersionUID = 1L;
    @val Integer season;

    public SerieMapping(String name, String providerId, String providerName, @Nullable Integer season=null) {
        super(name, providerId, providerName);
        this.season = season;
    }
}
