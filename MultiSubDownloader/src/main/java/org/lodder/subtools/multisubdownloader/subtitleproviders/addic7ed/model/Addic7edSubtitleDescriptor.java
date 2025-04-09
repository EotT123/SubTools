package org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.lodder.subtools.sublibrary.Language;

@EqualsAndHashCode
@Accessors(chain = true)
@Getter
@Setter
public class Addic7edSubtitleDescriptor {

    private String version;
    private Language language;
    private String url;
    private String title;
    private String uploader;
    private boolean hearingImpaired;
}
