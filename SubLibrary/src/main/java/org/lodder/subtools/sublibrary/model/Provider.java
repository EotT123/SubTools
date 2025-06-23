package org.lodder.subtools.sublibrary.model;

import manifold.ext.props.rt.api.val;

public enum Provider {
    OPENSUBTITLES(SubtitleSource.OPENSUBTITLES),
    PODNAPISI(SubtitleSource.PODNAPISI),
    ADDIC7ED(SubtitleSource.ADDIC7ED),
    ADDIC7ED_GESTDOWN(SubtitleSource.ADDIC7ED),
    TVSUBTITLES(SubtitleSource.TVSUBTITLES),
    LOCAL(SubtitleSource.LOCAL),
    SUBSCENE(SubtitleSource.SUBSCENE),
    SUBDL(SubtitleSource.SUBDL);

    @val SubtitleSource subtitleSource;

    Provider(SubtitleSource subtitleSource) {
        this.subtitleSource = subtitleSource;
    }
}
