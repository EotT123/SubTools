package org.lodder.subtools.sublibrary.model;

import org.jspecify.annotations.NullMarked;

@NullMarked
public class ProviderIdType<T> {

    public static ProviderIdType<String> IMDB = new ProviderIdType<>();
    public static ProviderIdType<Integer> TVDB = new ProviderIdType<>();
    public static ProviderIdType<String> OMDB = new ProviderIdType<>();

    private ProviderIdType() {}

}
