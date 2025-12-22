package org.lodder.subtools.multisubdownloader.subtitleproviders.podnapisi.exception;

import java.io.Serial;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.exception.SubtitlesProviderException;
import org.lodder.subtools.sublibrary.model.SubtitleSource;

@NullMarked
public class PodnapisiException extends SubtitlesProviderException {

    @Serial
    private static final long serialVersionUID = 1L;

    @val @override String subtitleProvider = SubtitleSource.PODNAPISI.name;

    public PodnapisiException(Throwable cause) {
        super(cause);
    }

    public PodnapisiException(String message) {
        super(message);
    }

    public PodnapisiException(String message, Throwable cause) {
        super(message);
    }
}
