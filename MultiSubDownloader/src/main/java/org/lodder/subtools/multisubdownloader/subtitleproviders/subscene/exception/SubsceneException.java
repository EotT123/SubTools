package org.lodder.subtools.multisubdownloader.subtitleproviders.subscene.exception;

import java.io.Serial;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.lodder.subtools.sublibrary.exception.SubtitlesProviderException;
import org.lodder.subtools.sublibrary.model.SubtitleSource;

public class SubsceneException extends SubtitlesProviderException {

    @Serial
    private static final long serialVersionUID = -9050358290926245586L;
    
    @val @override String subtitleProvider = SubtitleSource.SUBSCENE.name;

    public SubsceneException(String message) {
        super(message);
    }

    public SubsceneException(Throwable cause) {
        super(cause);
    }
}
