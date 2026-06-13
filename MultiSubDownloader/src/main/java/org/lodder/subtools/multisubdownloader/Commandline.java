package org.lodder.subtools.multisubdownloader;

import static util.Utils.*;

import java.util.function.Function;

import name.falgout.jeffrey.throwing.ThrowingSupplier;
import org.apache.commons.cli.CommandLine;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.cli.CliOptions.CliOption;
import org.lodder.subtools.multisubdownloader.cli.CliOptions.CliOptionEnable;
import org.lodder.subtools.multisubdownloader.cli.CliOptions.CliOptionWithArgParam;
import org.lodder.subtools.multisubdownloader.exception.CliException;

@NullMarked
public class Commandline {

    private final CommandLine commandLine;

    public Commandline(CommandLine commandLine) {
        this.commandLine = commandLine;
    }

    public boolean isEnabled(CliOptionEnable cliOption) {
        return cliOption.containsValue(commandLine);
    }

    public <T> @Nullable T get(CliOptionWithArgParam<T> cliOption) throws CliException {
        return cliOption.getValue(commandLine);
    }

    public <T> T get(CliOptionWithArgParam<T> cliOption, T defaultValue) {
        return get(cliOption, Function.identity(), () -> defaultValue);
    }

    public <T, S, X extends Exception> S get(CliOptionWithArgParam<T> cliOption, Function<T, S> mapper,
        ThrowingSupplier<S, X> defaultValue) throws X {
        S value;
        try {
            value = mapper.apply(cliOption.getValue(commandLine));
        } catch (CliException e) {
            value = null;
        }
        return ifNullThenGet(value, defaultValue);
    }

    public boolean contains(CliOption cliOption) {
        return cliOption.containsValue(commandLine);
    }
}
