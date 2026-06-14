package org.lodder.subtools.multisubdownloader.settings.model;

import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import manifold.ext.props.rt.api.var;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.lib.library.LibraryActionType;
import org.lodder.subtools.multisubdownloader.lib.library.LibraryOtherFileActionType;
import org.lodder.subtools.sublibrary.Language;

@NullMarked
public class LibrarySettings {

    @var String filenameStructure = "";
    @var String folderStructure = "";
    @var @Nullable Path folder;
    @var boolean filenameReplaceSpace;
    @var boolean includeLanguageCode;
    @var boolean removeEmptyFolders;
    @var boolean useTvdbNaming;
    @var LibraryActionType action = LibraryActionType.NOTHING;
    @var LibraryOtherFileActionType otherFileAction = LibraryOtherFileActionType.NOTHING;
    @var @Nullable Character filenameReplacingSpaceChar;
    @var @Nullable Character folderReplacingSpaceChar;
    @var boolean backupUseWebsiteFileName;
    @var @Nullable Path backupSubtitlePath;
    @var Map<Language, String> langCodeMap = new LinkedHashMap<>();

    public boolean hasLibraryAction(LibraryActionType libraryAction) {
        return this.action == libraryAction;
    }

    public boolean hasAnyLibraryAction(LibraryActionType... libraryActions) {
        return libraryActions.stream().anyMatch(this::hasLibraryAction);
    }

    public boolean hasLibraryOtherFileAction(LibraryOtherFileActionType libraryOtherFileAction) {
        return this.otherFileAction == libraryOtherFileAction;
    }
}
