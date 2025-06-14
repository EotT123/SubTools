package org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.exception;

import manifold.ext.props.rt.api.val;
import org.lodder.subtools.sublibrary.util.http.HttpStatus;

public class Addic7edResponseException extends Addic7edException {

    @val HttpStatus errorCode;

    public Addic7edResponseException(HttpStatus errorCode) {
        super("Addic7ed: " + errorCode.description);
        this.errorCode = errorCode;
    }
}
