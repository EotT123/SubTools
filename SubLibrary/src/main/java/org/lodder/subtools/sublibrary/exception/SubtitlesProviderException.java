package org.lodder.subtools.sublibrary.exception;

import java.io.Serial;

import lombok.experimental.StandardException;
import manifold.ext.props.rt.api.val;

@StandardException
public abstract class SubtitlesProviderException extends Exception {

    @Serial
    private static final long serialVersionUID = -2959483164333075297L;

    @val abstract String subtitleProvider;
}
