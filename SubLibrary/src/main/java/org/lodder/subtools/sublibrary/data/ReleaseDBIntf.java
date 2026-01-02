package org.lodder.subtools.sublibrary.data;

import java.io.Serializable;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;

@NullMarked
public interface ReleaseDBIntf extends Serializable {
    @val String name;
    @val int year;
}
