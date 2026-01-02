package org.lodder.subtools.sublibrary.util.filefilter;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;

@NullMarked
public final class XmlFileFilter extends ExtensionFileFilter {

    @val @override String description = "xml files";
    @val @override String extension = "xml";
}
