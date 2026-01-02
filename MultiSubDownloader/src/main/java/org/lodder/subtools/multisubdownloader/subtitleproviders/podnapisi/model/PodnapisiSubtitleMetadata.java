package org.lodder.subtools.multisubdownloader.subtitleproviders.podnapisi.model;

import java.io.Serializable;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.Language;

@NullMarked
public record PodnapisiSubtitleMetadata(String subtitleId, String name, String imdb, Language language,
                                        String uploaderName, String releaseString, String url, boolean hearingImpaired,
                                        String year, double rating, boolean isInexact=false) implements Serializable {
}
