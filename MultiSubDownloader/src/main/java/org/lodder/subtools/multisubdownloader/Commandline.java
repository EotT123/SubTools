package org.lodder.subtools.multisubdownloader;

import static util.Utils.*;

import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.apache.commons.cli.CommandLine;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
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

    @Contract("_,null->null; _,!null->!null")
    public <T> @Nullable T get(CliOptionWithArgParam<T> cliOption, @Nullable T defaultValue=null) throws CliException {
        return map(cliOption, v -> v, () -> defaultValue);
    }

    public <T, R extends @Nullable Object> R map(CliOptionWithArgParam<T> cliOption, Function<T, R> mapper,
        Supplier<R> defaultValueSupplier=() -> null) throws CliException {
        return ifNotNullOrElseGet(cliOption.getValue(commandLine), mapper::apply, defaultValueSupplier);
    }

    public <T> void execute(CliOptionWithArgParam<T> cliOption, Consumer<T> consumer) throws CliException {
        ifNotNullDo(cliOption.getValue(commandLine), consumer::accept);
    }
}
