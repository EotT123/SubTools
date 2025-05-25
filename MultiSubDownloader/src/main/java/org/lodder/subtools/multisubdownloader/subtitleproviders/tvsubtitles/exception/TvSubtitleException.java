package org.lodder.subtools.multisubdownloader.subtitleproviders.tvsubtitles.exception;

import java.io.Serial;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.lodder.subtools.sublibrary.exception.SubtitlesProviderException;
import org.lodder.subtools.sublibrary.model.SubtitleSource;

public class TvSubtitleException extends SubtitlesProviderException {

    @Serial
    private static final long serialVersionUID = 1L;

    @val @override String subtitleProvider = SubtitleSource.TVSUBTITLES.name;

    public TvSubtitleException(Throwable cause) {
        super(cause);
    }
}
