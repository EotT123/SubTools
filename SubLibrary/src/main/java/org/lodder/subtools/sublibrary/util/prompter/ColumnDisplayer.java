package org.lodder.subtools.sublibrary.util.prompter;

import java.util.function.Function;

@Deprecated
public record ColumnDisplayer<T>(String columnName, Function<T, String> toStringMapper) {}
