package org.lodder.subtools.multisubdownloader.lib.control;

import static manifold.ext.props.rt.api.PropOption.*;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.settings.model.Settings;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.data.imdb.ImdbAdapter;
import org.lodder.subtools.sublibrary.data.omdb.OmdbAdapter;
import org.lodder.subtools.sublibrary.data.tvdb.TvdbAdapter;
import org.lodder.subtools.sublibrary.exception.ReleaseControlException;
import org.lodder.subtools.sublibrary.model.Release;
import org.lodder.subtools.sublibrary.userinteraction.UserInteractionHandler;

@NullMarked
public abstract sealed class ReleaseControl<T extends Release> permits MovieReleaseControl, TvReleaseControl {

    @val(Protected) Settings settings;
    @val(Protected) Manager manager;
    @val(Protected) TvdbAdapter tvdbAdapter;
    @val(Protected) ImdbAdapter imdbAdapter;
    @val(Protected) OmdbAdapter omdbAdapter;

    ReleaseControl(Settings settings, Manager manager, UserInteractionHandler userInteractionHandler) {
        this.settings = settings;
        this.manager = manager;
        this.tvdbAdapter = TvdbAdapter.getInstance(manager, userInteractionHandler);
        this.imdbAdapter = ImdbAdapter.getInstance(manager, userInteractionHandler);
        this.omdbAdapter = OmdbAdapter.getInstance(manager, userInteractionHandler);
    }

    public abstract T process(T release) throws ReleaseControlException;
}
