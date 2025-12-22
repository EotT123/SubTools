package org.lodder.subtools.multisubdownloader.subtitleproviders.opensubtitles.exception;

import java.io.Serial;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.exception.SubtitlesProviderException;
import org.lodder.subtools.sublibrary.model.SubtitleSource;

@NullMarked
public class OpenSubtitleException extends SubtitlesProviderException {

    @Serial
    private static final long serialVersionUID = 1L;

    @val @override String subtitleProvider = SubtitleSource.OPENSUBTITLES.name;

    public OpenSubtitleException(String message) {
        super(message);
    }

    public OpenSubtitleException(Throwable e) {
        super(e);
    }

    public OpenSubtitleException(String message, Throwable e) {
        super(message, e);
    }
}
