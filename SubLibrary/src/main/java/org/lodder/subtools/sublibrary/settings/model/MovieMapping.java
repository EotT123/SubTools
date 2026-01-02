package org.lodder.subtools.sublibrary.settings.model;

import java.io.Serial;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class MovieMapping extends ReleaseMapping {

    @Serial
    private static final long serialVersionUID = 1L;
    @val @Nullable Integer year;

    public MovieMapping(String name, String providerId, String providerName, @Nullable Integer year=null) {
        super(name, providerId, providerName);
        this.year = year;
    }
}
