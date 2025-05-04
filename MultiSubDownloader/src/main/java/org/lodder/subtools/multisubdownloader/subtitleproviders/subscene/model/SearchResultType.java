package org.lodder.subtools.multisubdownloader.subtitleproviders.subscene.model;

import manifold.ext.props.rt.api.val;

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
