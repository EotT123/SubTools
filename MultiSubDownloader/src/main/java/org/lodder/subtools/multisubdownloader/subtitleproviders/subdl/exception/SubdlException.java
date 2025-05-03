package org.lodder.subtools.multisubdownloader.subtitleproviders.subdl.exception;

import java.io.Serial;

import lombok.experimental.StandardException;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.lodder.subtools.sublibrary.exception.SubtitlesProviderException;
import org.lodder.subtools.sublibrary.model.SubtitleSource;

@StandardException
public class SubdlException extends SubtitlesProviderException {

    @Serial
    private static final long serialVersionUID = -9050358290926245586L;

    @val @override String subtitleProvider = SubtitleSource.SUBDL.name;
}
