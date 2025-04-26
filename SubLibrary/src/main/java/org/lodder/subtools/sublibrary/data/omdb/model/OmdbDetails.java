package org.lodder.subtools.sublibrary.data.omdb.model;

import java.io.Serial;

import lombok.AllArgsConstructor;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.lodder.subtools.sublibrary.data.ReleaseDBIntf;

@AllArgsConstructor
public class OmdbDetails implements ReleaseDBIntf {
    @Serial
    private static final long serialVersionUID = 7701770682134890544L;

    @val String title;
    @val @override int year;

    @Override
    public String getName() {
        return title;
    }
}
