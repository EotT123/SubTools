package org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.model;

import org.lodder.subtools.sublibrary.Language;

public record Addic7edSubtitleDescriptor(String version, Language language, String url, String title, String uploader,
                                         boolean hearingImpaired) {
}
