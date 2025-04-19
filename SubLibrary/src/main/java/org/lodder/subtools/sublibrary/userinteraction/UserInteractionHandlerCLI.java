package org.lodder.subtools.sublibrary.userinteraction;

import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;

import manifold.ext.props.rt.api.override;
import manifold.ext.props.rt.api.val;
import org.codehaus.plexus.components.interactivity.DefaultInputHandler;
import org.codehaus.plexus.components.interactivity.DefaultOutputHandler;
import org.codehaus.plexus.components.interactivity.DefaultPrompter;
import org.codehaus.plexus.components.interactivity.Prompter;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.data.UserInteractionSettingsIntf;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class UserInteractionHandlerCLI implements UserInteractionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(UserInteractionHandlerCLI.class);
    @val Prompter prompter = new DefaultPrompter(new DefaultOutputHandler(), new DefaultInputHandler());
    @val @override UserInteractionSettingsIntf settings;

    public UserInteractionHandlerCLI(UserInteractionSettingsIntf settings) {
        this.settings = settings;
    }

    @Override
    public <T> Optional<T> selectFromList(Iterable<T> options, @Nullable String message, @Nullable String title,
        @Nullable Function<T, String> toStringMapper) {
        return prompter.promptValue(
            elements:options,
            toStringMapper:toStringMapper,
            message:message,
            includeNull:true);
//        return new PrompterValueFromList<>(
//            elements:options,
//            toStringMapper:toStringMapper,
//            message:message,
//            includeNull:true)
//            .prompt(prompter);
    }

    @Override
    public <T> Optional<T> choice(Iterable<T> options, @Nullable String message, @Nullable String title,
        @Nullable Function<T, String> toStringMapper) {
        return selectFromList(options, message, title, toStringMapper);
    }

    @Override
    public boolean confirm(String message, String title) {
        return prompter.promptBoolean(message:message + " (Y/N)");
//        return new PrompterBoolean(message:message + " (Y/N)").prompt(prompter);
    }

    @Override
    public Optional<String> enter(String title, String message, @Nullable String errorMessage,
        @Nullable Predicate<String> validator) {
        return prompter.promptString(
            message:message,
            errorMessage:errorMessage,
            validator:validator);

//        return new PrompterString(
//            message:message,
//            errorMessage:errorMessage,
//            validator:validator)
//                .prompt(prompter);
    }

    @Override
    public void showMessage(String message, String title, MessageSeverity messageSeverity) {
        switch (messageSeverity) {
            case INFO -> LOGGER.info(message);
            case WARNING -> LOGGER.warn(message);
            case ERROR -> LOGGER.error(message);
            default -> LOGGER.info(message);
        }
    }
}
