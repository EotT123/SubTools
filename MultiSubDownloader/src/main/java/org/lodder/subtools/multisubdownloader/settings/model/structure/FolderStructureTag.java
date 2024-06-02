package org.lodder.subtools.multisubdownloader.settings.model.structure;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.lodder.subtools.multisubdownloader.Messages;

@Getter
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum FolderStructureTag implements StructureTag {

    SEPARATOR("%SEPARATOR%", Messages.getString("StructureBuilderDialog.SystemdependendSeparator"));

    private final String label;
    private final String description;
}
