package org.lodder.subtools.sublibrary.settings.model;

import java.io.Serial;

import lombok.ToString;
import manifold.ext.props.rt.api.val;

@ToString
public class SerieMapping extends ReleaseMapping {

    @Serial
    private static final long serialVersionUID = 1L;
    @val int season;

    public SerieMapping(String name, String providerId, String providerName, int season=0) {
        super(name, providerId, providerName);
        this.season = season;
    }
}
