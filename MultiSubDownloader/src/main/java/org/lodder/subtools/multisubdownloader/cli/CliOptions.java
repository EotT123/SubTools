package org.lodder.subtools.multisubdownloader.cli;

import static util.Utils.*;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Option;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.Messages;
import org.lodder.subtools.multisubdownloader.exceptions.CliException;
import org.lodder.subtools.sublibrary.Language;

@NullMarked
public class CliOptions {

    @NullMarked
    public interface CliOption<T> {

        @val String msgCode;

        @val String value;

        @val @Nullable String longValue;

        boolean hasArg();

        default boolean containsValue(CommandLine commandLine) {
            return commandLine.hasOption(option);
        }

        @Nullable T getValue(CommandLine commandLine) throws CliException;

        @val String description = Messages.getText(msgCode);

        @val Option option = new Option(value, longValue, hasArg(), description);
    }


    @NullMarked
    public interface CliOptionWithoutArg<T> extends CliOption<T> {

        @Override
        default boolean hasArg() {
            return false;
        }
    }

    @NullMarked
    public interface CliOptionWithArgParam<T> extends CliOption<T> {

        @Override
        default boolean hasArg() {
            return true;
        }
    }

    public enum CliOptionEnable implements CliOptionWithoutArg<Boolean> {
        HELP("App.OptionHelpMsg", "help"),
        NO_GUI("App.OptionNoGuiMsg", "nogui"),
        RECURSIVE("R", "recursive", "App.OptionOptionRecursiveMsg"),
        DEBUG("App.OptionOptionDebugMsg", "debug"),
        TRACE("App.OptionOptionTraceMsg", "trace"),
        FORCE("App.OptionOptionForceMsg", "force"),
        DOWNLOAD_ALL("App.OptionOptionDownloadAllMsg", "downloadall"),
        SELECTION("App.OptionOptionSelectionMsg", "selection"),
        SPEEDY("App.OptionOptionSpeedyMsg", "speedy"),
        VERBOSE_PROGRESS("App.OptionVerboseProgressCLI", "verboseprogress"),
        DRY_RUN("App.OptionDryRun", "dryrun"),
        CONFIRM_PROVIDER_MAPPING("App.OptionConfirmProviderMapping", "confirmProviderMapping");

        @val @override String msgCode;

        @val @override String value;

        @val @override @Nullable String longValue;

        CliOptionEnable(String msgCode, String value, @Nullable String longValue=null) {
            this.msgCode = msgCode;
            this.value = value;
            this.longValue = longValue;
        }

        @Override
        public Boolean getValue(CommandLine commandLine) {
            return containsValue(commandLine);
        }
    }

    public enum CliOptionLanguage implements CliOptionWithArgParam<Language> {
        LANGUAGE("App.OptionOptionLanguageMsg", "language");

        @val @override String msgCode;

        @val @override String value;

        @val @override @Nullable String longValue;

        CliOptionLanguage(String msgCode, String value, @Nullable String longValue=null) {
            this.msgCode = msgCode;
            this.value = value;
            this.longValue = longValue;
        }

        @Override
        public @Nullable Language getValue(CommandLine commandLine) throws CliException {
            return ifNotNull(commandLine.getOptionValue(option),
                language -> Language.values().stream().filter(lang -> lang.name().equalsIgnoreCase(language)).findAny()
                    .orElseThrow(() -> new CliException(Messages.getText("App.NoValidLanguage"))));
        }
    }

    public enum CliOptionPath implements CliOptionWithArgParam<Path> {
        IMPORT_PREFERENCES("App.OptionOptionImportPreferencesMsg", "importpreferences"),
        FOLDER("App.OptionOptionFolderMsg", "folder");

        @val @override String msgCode;

        @val @override String value;

        @val @override @Nullable String longValue;

        CliOptionPath(String msgCode, String value, @Nullable String longValue=null) {
            this.msgCode = msgCode;
            this.value = value;
            this.longValue = longValue;
        }

        @Override
        public @Nullable Path getValue(CommandLine commandLine) {
            return ifNotNull(commandLine.getOptionValue(option), Paths::get);
        }
    }

    public static List<? extends CliOption> values() {
        return Stream.of(CliOptionEnable.values(), CliOptionLanguage.values(), CliOptionPath.values())
            .flatMap(Arrays::stream).toList();
    }
}