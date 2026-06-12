package org.lodder.subtools.sublibrary.model;

import static util.Utils.*;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;

import name.falgout.jeffrey.throwing.ThrowingFunction;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class ProviderIds {

    private final Map<ProviderIdType<?>, @Nullable Object> providerIdMap = new LinkedHashMap<>();

    public <T> ProviderIds add(ProviderIdType<T> providerIdType, @Nullable T value) {
        providerIdMap.put(providerIdType, value);
        return this;
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable T get(ProviderIdType<T> providerIdType) {
        return (T) providerIdMap.get(providerIdType);
    }

    public <T, S, E extends Exception> @Nullable S get(ProviderIdType<T> providerIdType,
        ThrowingFunction<T, @Nullable S, E> mapper) throws E {
        return ifNotNull(get(providerIdType), mapper);
    }

    public <T extends @Nullable S, S> S getOrPut(ProviderIdType<S> providerIdType, Supplier<T> supplier) {
        return getOrPut(providerIdType, supplier, v -> v);
    }

    public <S, T extends @Nullable S, I> S getOrPut(ProviderIdType<S> providerIdType, Supplier<@Nullable I> supplier,
        Function<I, T> mapper) {
        return getOrPut(providerIdType, supplier, mapper, v -> v);
    }

    public <S, T extends @Nullable S, I, I2> S getOrPut(ProviderIdType<S> providerIdType,
        Supplier<@Nullable I> supplier, Function<I, @Nullable I2> mapper, Function<I2, T> mapper2) {
        return (T) providerIdMap.computeIfAbsent(providerIdType,
            _ -> ifNotNull(ifNotNull(supplier.get(), mapper::apply), mapper2::apply));
    }

    public <T, S extends @Nullable Object, X extends Exception> S userOrElse(ProviderIdType<T> providerIdType,
        ThrowingFunction<T, S, X> mapper, Supplier<S> supplier) throws X {
        return ifNotNullOrElseGet(get(providerIdType), mapper, supplier);
    }

    public boolean isEqual(ProviderIds other, ProviderIdType<?> providerIdType) {
        if (!providerIdMap.containsKey(providerIdType) || !other.providerIdMap.containsKey(providerIdType)) {
            return false;
        }
        return Objects.equals(providerIdMap.get(providerIdType), other.providerIdMap.get(providerIdType));
    }
}
