package org.lodder.subtools.multisubdownloader.framework.service.providers;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.multisubdownloader.UserInteractionHandler;
import org.lodder.subtools.multisubdownloader.framework.Container;

@NullMarked
public interface ServiceProvider {
    @val int priority;

    void register(Container app, UserInteractionHandler userInteractionHandler);
}
