package org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.exception;

import java.io.Serial;

import manifold.ext.props.rt.api.val;
import org.lodder.subtools.sublibrary.exception.SubtitlesProviderException;
import org.lodder.subtools.sublibrary.model.SubtitleSource;

public class OpenSubtitleException extends SubtitlesProviderException {

    @Serial
    private static final long serialVersionUID = -9050358290926245586L;

    //    @val int errorCode;
//    @val boolean skipProvider;
    @val String subtitleProvider = SubtitleSource.OPENSUBTITLES.name;

    public OpenSubtitleException(String message) {
        super(message);
    }

//    public OpenSubtitleException(int errorCode, String message=null, boolean skipProvider=true) {
//        super(message);
//        this.errorCode = errorCode;
//        this.skipProvider = skipProvider;
//    }

}
