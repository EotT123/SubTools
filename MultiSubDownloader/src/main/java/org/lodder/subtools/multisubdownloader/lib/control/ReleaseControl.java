package org.lodder.subtools.multisubdownloader.lib.control;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.lodder.subtools.multisubdownloader.settings.model.Settings;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.exception.ReleaseControlException;
import org.lodder.subtools.sublibrary.model.Release;

@Getter(value = AccessLevel.PROTECTED)
@AllArgsConstructor
public abstract class ReleaseControl {

    private final Settings settings;
    private final Manager manager;

    public abstract void process() throws ReleaseControlException;

    public abstract Release getVideoFile();
}
