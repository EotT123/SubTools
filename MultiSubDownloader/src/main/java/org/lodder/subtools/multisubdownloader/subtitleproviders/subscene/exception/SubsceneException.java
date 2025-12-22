package org.lodder.subtools.multisubdownloader.subtitleproviders.subscene.exception;

import java.io.Serial;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.exception.SubtitlesProviderException;
import org.lodder.subtools.sublibrary.model.SubtitleSource;

@NullMarked
public class SubsceneException extends SubtitlesProviderException {

    @Serial
    private static final long serialVersionUID = 1L;
    
    @val @override String subtitleProvider = SubtitleSource.SUBSCENE.name;

    public SubsceneException(String message) {
        super(message);
    }

    public SubsceneException(Throwable cause) {
        super(cause);
    }

    public SubsceneException(String message, @Nullable Throwable cause) {
        super(message, cause);
    }
}
