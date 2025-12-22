package org.lodder.subtools.sublibrary.data.omdb.model;

import java.io.Serial;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.data.ReleaseDBIntf;

@NullMarked
public record OmdbDetails(String title, int year) implements ReleaseDBIntf {

    @Serial
    private static final long serialVersionUID = 1L;

    @Override
    public String getName() {
        return title;
    }

    @Override
    public int getYear() {
        return year;
    }
}
