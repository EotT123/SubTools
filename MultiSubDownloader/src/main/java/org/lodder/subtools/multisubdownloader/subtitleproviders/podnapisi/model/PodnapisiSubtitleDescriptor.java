package org.lodder.subtools.multisubdownloader.subtitleproviders.podnapisi.model;

import org.lodder.subtools.sublibrary.Language;

import lombok.Getter;
import lombok.Setter;

/**
 * Created by IntelliJ IDEA.
 * User: lodder
 * Date: 20/08/11
 * Time: 13:44
 * To change this template use File | Settings | File Templates.
 */
@Getter
@Setter
public class PodnapisiSubtitleDescriptor {

    private String subtitleId;
    private Language language;
    private String uploaderName;
    private String uploaderUid;
    private String matchRanking;
    private String releaseString;
    private String flagsString;
    private String subtitleRating;
    private boolean isInexact;

}
