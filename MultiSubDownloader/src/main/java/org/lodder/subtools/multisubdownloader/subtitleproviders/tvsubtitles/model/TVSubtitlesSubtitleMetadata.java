package org.lodder.subtools.multisubdownloader.subtitleproviders.tvsubtitles.model;

import java.io.Serializable;

import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.control.VideoPatterns.Source;

public record TVSubtitlesSubtitleMetadata(String title, String filename, String url, Source source, String releaseGroup,
    Language language) implements Serializable {
}
