package org.lodder.subtools.multisubdownloader.settings.model;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;

import manifold.ext.props.rt.api.var;
import org.lodder.subtools.multisubdownloader.lib.library.LibraryActionType;
import org.lodder.subtools.multisubdownloader.lib.library.LibraryOtherFileActionType;
import org.lodder.subtools.sublibrary.Language;

public class LibrarySettings {

    @var String libraryFilenameStructure = "";
    @var String libraryFolderStructure = "";
    @var Path libraryFolder;
    @var boolean libraryFilenameReplaceSpace;
    @var boolean libraryFolderReplaceSpace;
    @var boolean libraryIncludeLanguageCode;
    @var boolean libraryRemoveEmptyFolders;
    @var boolean libraryUseTVDBNaming;
    @var LibraryActionType libraryAction = LibraryActionType.NOTHING;
    @var LibraryOtherFileActionType libraryOtherFileAction = LibraryOtherFileActionType.NOTHING;
    @var Character libraryFilenameReplacingSpaceChar;
    @var Character libraryFolderReplacingSpaceChar;
    @var boolean libraryBackupSubtitle;
    @var boolean libraryBackupUseWebsiteFileName;
    @var Path libraryBackupSubtitlePath;
    @var Map<Language, String> langCodeMap = new LinkedHashMap<>();

    public boolean hasLibraryAction(LibraryActionType libraryAction) {
        return this.libraryAction == libraryAction;
    }

    public boolean hasAnyLibraryAction(LibraryActionType... libraryActions) {
        return Arrays.stream(libraryActions).anyMatch(this::hasLibraryAction);
    }

    public boolean hasLibraryOtherFileAction(LibraryOtherFileActionType libraryOtherFileAction) {
        return this.libraryOtherFileAction == libraryOtherFileAction;
    }
}
