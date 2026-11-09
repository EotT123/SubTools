package org.lodder.subtools.multisubdownloader.subtitleprovider.tvsubtitles.model;

import java.io.Serializable;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.control.VideoPatterns.Source;

@NullMarked
public record TVSubtitlesSubtitleMetadata(String title, String filename, String url, @Nullable Source source,
    String releaseGroup, @Nullable String uploader, @Nullable Language language) implements Serializable {
}
