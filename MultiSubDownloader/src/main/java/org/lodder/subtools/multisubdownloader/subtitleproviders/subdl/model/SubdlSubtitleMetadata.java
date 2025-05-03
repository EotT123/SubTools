package org.lodder.subtools.multisubdownloader.subtitleproviders.subdl.model;

import java.io.Serializable;
import java.util.List;

import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.model.SubtitleSource;

/**
 * Represents metadata for a subtitle retrieved from the SubDL API.
 * </p>
 *
 * @param title The release name or title of the subtitle.
 * @param fileName The filename of the subtitle archive (e.g., ZIP file).
 * @param url The full URL where the subtitle can be downloaded.
 * @param season The season number this subtitle is associated with.
 * @param episodes A list of episode numbers this subtitle applies to.
 * @param uploader The name of the person or group that uploaded the subtitle.
 * @param hearingImpaired Indicates whether the subtitle includes hearing-impaired annotations.
 */
public record SubdlSubtitleMetadata(String title, String fileName, String url, int season, List<Integer> episodes,
                                    String uploader, boolean hearingImpaired, Language language)
    implements Serializable {

    public SubtitleSource getSource() {
        return SubtitleSource.SUBDL;
    }
}
