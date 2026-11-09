package org.lodder.subtools.multisubdownloader.subtitleprovider.subscene.model;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;

@NullMarked
public enum SearchResultType {
    EXACT("Exact"),
    TV_SERIE("TV-Serie"),
    CLOSE("Close");

    @val String text;

    SearchResultType(String text) {
        this.text = text;
    }

    public static SearchResultType of(String text) {
        return SearchResultType.values().stream().filter(t -> t.text.equals(text)).findFirst().orElse(null);
    }
}
