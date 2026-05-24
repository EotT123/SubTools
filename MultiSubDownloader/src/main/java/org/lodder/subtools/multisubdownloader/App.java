package org.lodder.subtools.multisubdownloader;

import static java.util.Objects.*;
import static org.lodder.subtools.multisubdownloader.cli.CliOptions.CliOptionEnable.*;
import static org.lodder.subtools.multisubdownloader.cli.CliOptions.CliOptionPath.*;
import static util.Utils.*;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import ch.qos.logback.classic.Level;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import org.apache.commons.cli.help.HelpFormatter;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.multisubdownloader.cli.CliOptions;
import org.lodder.subtools.multisubdownloader.cli.CliOptions.CliOption;
import org.lodder.subtools.multisubdownloader.exceptions.CliException;
import org.lodder.subtools.multisubdownloader.framework.Bootstrapper;
import org.lodder.subtools.multisubdownloader.gui.Splash;
import org.lodder.subtools.multisubdownloader.settings.SettingsControl;
import org.lodder.subtools.multisubdownloader.subtitleproviders.SubtitleProviderStore;
import org.lodder.subtools.sublibrary.ConfigProperties;
import org.lodder.subtools.sublibrary.ConfigProperties.Property;
import org.lodder.subtools.sublibrary.Language;
import org.lodder.subtools.sublibrary.Manager;
import org.lodder.subtools.sublibrary.cache.CacheType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@NullMarked
public class App {

    private static final Logger LOGGER = LoggerFactory.getLogger(App.class);

    private static @Nullable Splash splash;

    static void main(String[] args) throws ReflectiveOperationException, UnsupportedLookAndFeelException {
        UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());

        CommandLineParser parser = new DefaultParser();
        HelpFormatter formatter = HelpFormatter.builder().get();

        Commandline commandline;
        try {
            commandline = new Commandline(parser.parse(getCLIOptions(), args));
        } catch (ParseException e) {
            LOGGER.error("Unable to parse cli options", e);
            return;
        }


        Messages.language = ifNullThen(SettingsControl.settings.language, Language.ENGLISH);

        if (commandline.isEnabled(TRACE)) {
            setLogLevel(Level.ALL);
        } else if (commandline.isEnabled(DEBUG)) {
            setLogLevel(Level.DEBUG);
        }

        if (commandline.isEnabled(NO_GUI)) {
            new Bootstrapper(new UserInteractionHandlerCLI(SettingsControl.settings));

            /* Defined here so there is output on console */
            importPreferences(commandline);

            try {
                CLI cmd = new CLI(commandline);
                if (commandline.isEnabled(HELP)) {
                    formatter.printHelp(ConfigProperties.getProperty(Property.NAME), "help", getCLIOptions(), "",
                        false);
                    return;
                }
                cmd.run();
            } catch (IOException | CliException e) {
                System.out.println("Error: " + e.getMessage());
                return;
            }
        } else {
            splash = new Splash(Messages.getText("App.Starting")).showSplash();
            new Bootstrapper(new UserInteractionHandlerGUI(SettingsControl.settings, null));

            /* Defined here so there is output in the splash */
            importPreferences(commandline);

            EventQueue.invokeLater(() -> {
                try {
                    JFrame window = new GUI();
                    window.setVisible(true);
                    splash.setVisible(false);
                    splash.dispose();
                } catch (Exception e) {
                    LOGGER.error("", e);
                }
            });
        }
        new Thread(() -> {
            List<String> providerNames =
                SubtitleProviderStore.allProviders.stream().map(provider -> provider.provider)
                    .map(providerName -> providerName.contains("-") ? providerName.split("-")[0] : providerName)
                    .map(providerName -> providerName + "-").toList();
            Manager.getInstance()
                .getCache(CacheType.DISK, key -> providerNames.stream().noneMatch(key.provider::equals))
                .clearExpiredCache();
        }).start();

    }

    private static void setLogLevel(Level level) {
        ch.qos.logback.classic.Logger root =
            (ch.qos.logback.classic.Logger) org.slf4j.LoggerFactory.getLogger(Logger.ROOT_LOGGER_NAME);
        root.setLevel(level);
    }

    private static void importPreferences(Commandline commandline) {
        if (!commandline.contains(IMPORT_PREFERENCES)) {
            return;
        }
        try {
            Path file = requireNonNull(commandline.get(IMPORT_PREFERENCES));
            if (file.isRegularFile()) {
                SettingsControl.importPreferences(file);
            }
        } catch (Exception e) {
            LOGGER.error("executeArgs: importPreferences", e);
        }
    }

    public static Options getCLIOptions() {
        Options options = new Options();
        CliOptions.values().stream().map(CliOption::getOption).forEach(options::addOption);
        return options;
    }
}
