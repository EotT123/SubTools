package org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.exception;

import manifold.ext.props.rt.api.val;
import org.lodder.subtools.sublibrary.util.http.HttpStatus;

public class OpenSubtitleResponseException extends OpenSubtitleException {

    @val HttpStatus errorCode;
    @val boolean stopContactingClient;

    public OpenSubtitleResponseException(HttpStatus errorCode, String message=errorCode.description,
        boolean stopContactingClient=errorCode.stopContactingClient) {
        super("OpenSubtitle: " + message);
        this.errorCode = errorCode;
        this.stopContactingClient = stopContactingClient;
    }
}
