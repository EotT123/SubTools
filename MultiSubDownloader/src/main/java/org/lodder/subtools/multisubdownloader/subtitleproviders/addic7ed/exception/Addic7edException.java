package org.lodder.subtools.multisubdownloader.subtitleproviders.addic7ed.exception;

import java.io.Serial;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.lodder.subtools.sublibrary.exception.SubtitlesProviderException;
import org.lodder.subtools.sublibrary.model.SubtitleSource;

public class Addic7edException extends SubtitlesProviderException {

    @Serial
    private static final long serialVersionUID = -9050358290926245586L;

    @val @override String subtitleProvider = SubtitleSource.ADDIC7ED.name;

    public Addic7edException(String message) {
        super(message);
    }

    public Addic7edException(Throwable cause) {
        super(cause);
    }
}
