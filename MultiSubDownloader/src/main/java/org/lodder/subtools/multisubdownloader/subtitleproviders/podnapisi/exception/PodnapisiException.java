package org.lodder.subtools.multisubdownloader.subtitleproviders.podnapisi.exception;

import java.io.Serial;

import lombok.experimental.StandardException;
import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.lodder.subtools.sublibrary.exception.SubtitlesProviderException;
import org.lodder.subtools.sublibrary.model.SubtitleSource;

@StandardException
public class PodnapisiException extends SubtitlesProviderException {

    @Serial
    private static final long serialVersionUID = -2390367212064062005L;

    @val @override String subtitleProvider = SubtitleSource.PODNAPISI.name;
}
