package org.lodder.subtools.sublibrary.model;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;

@NullMarked
public enum SubtitleSource {
    OPENSUBTITLES("OpenSubtitles"),
    PODNAPISI("Podnapisi"),
    ADDIC7ED("Addic7ed"),
    TVSUBTITLES("TvSubtitles"),
    LOCAL("Local"),
    SUBSCENE("Subscene"),
    SUBDL("SubDL");

    @val String name;

    SubtitleSource(String name) {
        this.name = name;
    }
}
