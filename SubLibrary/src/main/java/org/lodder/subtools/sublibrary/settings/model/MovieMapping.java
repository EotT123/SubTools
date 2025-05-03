package org.lodder.subtools.sublibrary.settings.model;

import java.io.Serial;

import lombok.ToString;

@ToString
public class MovieMapping extends ReleaseMapping {

    @Serial
    private static final long serialVersionUID = 1L;

    public MovieMapping(String name, String providerId, String providerName) {
        super(name, providerId, providerName);
    }
}
