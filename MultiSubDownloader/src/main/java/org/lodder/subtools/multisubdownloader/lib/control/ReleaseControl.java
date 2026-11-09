package org.lodder.subtools.multisubdownloader.lib.control;

import static manifold.ext.props.rt.api.PropOption.*;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.data.imdb.ImdbAdapter;
import org.lodder.subtools.sublibrary.data.omdb.OmdbAdapter;
import org.lodder.subtools.sublibrary.data.tvdb.TvdbAdapter;
import org.lodder.subtools.sublibrary.exception.ReleaseControlException;
import org.lodder.subtools.sublibrary.model.ReleaseWithoutPath;
import org.lodder.subtools.sublibrary.userinteraction.UserInteractionHandler;

@NullMarked
public abstract sealed class ReleaseControl<T extends ReleaseWithoutPath>
    permits MovieReleaseControl, TvReleaseControl {

    @val(Protected) TvdbAdapter tvdbAdapter;
    @val(Protected) ImdbAdapter imdbAdapter;
    @val(Protected) OmdbAdapter omdbAdapter;

    ReleaseControl(UserInteractionHandler userInteractionHandler) {
        this.tvdbAdapter = TvdbAdapter.getInstance(userInteractionHandler);
        this.imdbAdapter = ImdbAdapter.getInstance(userInteractionHandler);
        this.omdbAdapter = OmdbAdapter.getInstance(userInteractionHandler);
    }

    public abstract <R extends T> R process(R release) throws ReleaseControlException;
}
