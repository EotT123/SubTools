package org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.exception;

import java.io.Serial;

import lombok.experimental.StandardException;
import manifold.ext.props.rt.api.val;
import org.lodder.subtools.sublibrary.exception.SubtitlesProviderException;
import org.lodder.subtools.sublibrary.model.SubtitleSource;

@StandardException
public class OpenSubtitleException extends SubtitlesProviderException {

    @Serial
    private static final long serialVersionUID = -9050358290926245586L;

    //    @val int errorCode;
//    @val boolean skipProvider;
    @val String subtitleProvider = SubtitleSource.OPENSUBTITLES.name;

//    public OpenSubtitleException(int errorCode, String message=null, boolean skipProvider=true) {
//        super(message);
//        this.errorCode = errorCode;
//        this.skipProvider = skipProvider;
//    }

}
