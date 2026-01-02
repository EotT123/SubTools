package org.lodder.subtools.multisubdownloader.framework.event;

import org.jspecify.annotations.NullMarked;

@NullMarked
public interface Handler {
    void handle(Event event);
}
