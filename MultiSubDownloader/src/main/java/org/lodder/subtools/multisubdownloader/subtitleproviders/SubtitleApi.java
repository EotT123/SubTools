package org.lodder.subtools.multisubdownloader.subtitleproviders;

import manifold.ext.props.rt.api.val;
import org.lodder.subtools.sublibrary.data.ApiIntf;
import org.lodder.subtools.sublibrary.model.SubtitleSource;

public interface SubtitleApi extends ApiIntf {

    @val SubtitleSource source;

    default String getProvider() {
        return source.name;
    }
}
