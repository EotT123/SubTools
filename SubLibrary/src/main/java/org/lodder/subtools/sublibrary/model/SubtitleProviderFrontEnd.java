package org.lodder.subtools.sublibrary.model;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;

@NullMarked
public enum SubtitleProviderFrontEnd {
    OPENSUBTITLES(SubtitleSource.OPENSUBTITLES),
    PODNAPISI(SubtitleSource.PODNAPISI),
    ADDIC7ED(SubtitleSource.ADDIC7ED),
    ADDIC7ED_GESTDOWN(SubtitleSource.ADDIC7ED, "Addic7ed via Gestdown"),
    TVSUBTITLES(SubtitleSource.TVSUBTITLES),
    LOCAL(SubtitleSource.LOCAL),
    SUBSCENE(SubtitleSource.SUBSCENE),
    SUBDL(SubtitleSource.SUBDL);

    @val SubtitleSource subtitleSource;
    @val String name;

    SubtitleProviderFrontEnd(SubtitleSource subtitleSource, String name=subtitleSource.name) {
        this.subtitleSource = subtitleSource;
        this.name = name;
    }
}
