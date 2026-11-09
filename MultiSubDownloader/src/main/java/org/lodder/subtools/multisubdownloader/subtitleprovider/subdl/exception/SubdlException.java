package org.lodder.subtools.multisubdownloader.subtitleprovider.subdl.exception;

import java.io.Serial;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.exception.SubtitlesProviderException;
import org.lodder.subtools.sublibrary.model.SubtitleSource;

@NullMarked
public class SubdlException extends SubtitlesProviderException {

    @Serial
    private static final long serialVersionUID = 1L;

    @val @override String subtitleProvider = SubtitleSource.SUBDL.name;

    public SubdlException(Throwable cause) {
        super(cause);
    }

    public SubdlException(String message) {
        super(message);
    }

    public SubdlException(String message, Throwable cause) {
        super(message, cause);
    }
}
