package org.lodder.subtools.multisubdownloader.cli;

import manifold.ext.props.rt.api.val;
import org.apache.commons.cli.Option;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.Messages;

@NullMarked
public enum CliOption {
    HELP("help", false, "App.OptionHelpMsg"),
    NO_GUI("nogui", false, "App.OptionNoGuiMsg"),
    RECURSIVE("R", "recursive", false, "App.OptionOptionRecursiveMsg"),
    LANGUAGE("language", true, "App.OptionOptionLanguageMsg"),
    DEBUG("debug", false, "App.OptionOptionDebugMsg"),
    TRACE("trace", false, "App.OptionOptionTraceMsg"),
    IMPORT_PREFERENCES("importpreferences", true, "App.OptionOptionImportPreferencesMsg"),
    FORCE("force", false, "App.OptionOptionForceMsg"),
    FOLDER("folder", true, "App.OptionOptionFolderMsg"),
    DOWNLOAD_ALL("downloadall", false, "App.OptionOptionDownloadAllMsg"),
    SELECTION("selection", false, "App.OptionOptionSelectionMsg"),
    SPEEDY("speedy", false, "App.OptionOptionSpeedyMsg"),
    VERBOSE_PROGRESS("verboseprogress", false, "App.OptionVerboseProgressCLI"),
    DRY_RUN("dryrun", false, "App.OptionDryRun"),
    CONFIRM_PROVIDER_MAPPING("confirmProviderMapping", false, "App.OptionConfirmProviderMapping");

    @val String value;
    @val @Nullable String longValue;
    @val boolean hasArg;
    @val String msgCode;

    CliOption(String value, @Nullable String longValue=null, boolean hasArg, String msgCode) {
        this.value = value;
        this.longValue = longValue;
        this.hasArg = hasArg;
        this.msgCode = msgCode;
    }

    public String getDescription() {
        return Messages.getText(msgCode);
    }

    public Option toOption() {
        return new Option(value, longValue, hasArg, description);
    }
}