package org.lodder.subtools.sublibrary.data.omdb.model;

import java.io.Serial;
import java.io.Serializable;

import lombok.AllArgsConstructor;
import manifold.ext.props.rt.api.val;
import org.lodder.subtools.sublibrary.data.ReleaseDBIntf;

@AllArgsConstructor
public class OmdbDetails implements ReleaseDBIntf, Serializable {
    @Serial
    private static final long serialVersionUID = 7701770682134890544L;
    @val String title;
    @val int year;

    @Override
    public String getName() {
        return title;
    }

    @Override
    public int year() {
        return year;
    }
}
