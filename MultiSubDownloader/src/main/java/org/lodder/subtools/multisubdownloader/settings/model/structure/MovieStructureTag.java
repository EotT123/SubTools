package org.lodder.subtools.multisubdownloader.settings.model.structure;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.lodder.subtools.multisubdownloader.Messages;

@AllArgsConstructor(access = AccessLevel.PRIVATE)
public enum MovieStructureTag implements StructureTag {

    MOVIE_TITLE("%MOVIE TITLE%", Messages.getText("StructureBuilderDialog.MovieName")),
    QUALITY("%QUALITY%", Messages.getText("StructureBuilderDialog.QualityOfMovie")),
    DESCRIPTION("%DESCRIPTION%", Messages.getText("StructureBuilderDialog.MovieDescription")),
    YEAR("%YEAR%", Messages.getText("StructureBuilderDialog.MovieYear"));

    @val @override String label;
    @val @override String description;

}
