package org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.exception;

import java.io.Serial;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.exception.SubtitlesProviderException;
import org.lodder.subtools.sublibrary.model.SubtitleSource;

@NullMarked
public class Addic7edException extends SubtitlesProviderException {

    @Serial
    private static final long serialVersionUID = 1L;

    @val @override String subtitleProvider = SubtitleSource.ADDIC7ED.name;

    public Addic7edException(String message) {
        super(message);
    }

    public Addic7edException(Throwable cause) {
        super(cause);
    }

    public Addic7edException(String message, Throwable cause) {
        super(message, cause);
    }
}
