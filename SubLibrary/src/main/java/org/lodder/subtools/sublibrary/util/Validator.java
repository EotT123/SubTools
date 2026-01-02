package org.lodder.subtools.sublibrary.util;

import static org.lodder.subtools.multisubdownloader.Messages.*;

import java.util.function.Predicate;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public record Validator<T extends @Nullable Object>(
    Predicate<? super T> predicate, String errorMessage=getText("Prompter.ValueIsNotValid")) {

    public boolean isValid(T value) {
        return predicate.test(value);
    }

    public boolean isInvalid(T value) {
        return !isValid(value);
    }
}