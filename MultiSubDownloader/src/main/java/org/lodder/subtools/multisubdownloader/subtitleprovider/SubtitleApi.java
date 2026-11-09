package org.lodder.subtools.multisubdownloader.subtitleprovider;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.data.ApiIntf;
import org.lodder.subtools.sublibrary.model.SubtitleProviderFrontEnd;
import org.lodder.subtools.sublibrary.model.SubtitleSource;

@NullMarked
public interface SubtitleApi extends ApiIntf {

    @val SubtitleProviderFrontEnd subtitleProviderFrontEnd;
    @val SubtitleSource source = subtitleProviderFrontEnd.subtitleSource;
    @val @override String provider = subtitleProviderFrontEnd.name;
}
