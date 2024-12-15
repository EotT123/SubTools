package org.lodder.subtools.sublibrary.data.imdb.model;

import java.io.Serializable;

import lombok.AllArgsConstructor;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.lodder.subtools.sublibrary.data.ReleaseDBIntf;

@AllArgsConstructor
public class ImdbDetails implements ReleaseDBIntf, Serializable {

    @val String title;
    @val @override int year;

    @Override
    public String getName() {
        return title;
    }
}

