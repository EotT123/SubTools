package org.lodder.subtools.sublibrary.settings.model;

import java.io.Serial;

import lombok.ToString;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.Nullable;

@ToString
public class MovieMapping extends ReleaseMapping {

    @Serial
    private static final long serialVersionUID = 1L;
    @val @Nullable Integer year;

    public MovieMapping(String name, String providerId, String providerName, @Nullable Integer year=null) {
        super(name, providerId, providerName);
        this.year = year;
    }
}
