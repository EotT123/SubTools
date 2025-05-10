package org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.exception;

import manifold.ext.props.rt.api.val;
import org.lodder.subtools.sublibrary.util.http.HttpStatus;

public class Addic7edResponseException extends Addic7edException {

    @val HttpStatus errorCode;
    @val boolean skipProvider;

    public Addic7edResponseException(HttpStatus errorCode, String message=errorCode.description,
        boolean skipProvider=errorCode.stopContactingServer) {
        super("Addic7ed: " + message);
        this.errorCode = errorCode;
        this.skipProvider = skipProvider;
    }
}
