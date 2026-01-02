package org.lodder.subtools.sublibrary.model;

import static util.Utils.*;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class ProviderIds {
    private final Map<ProviderIdType, @Nullable Object> providerIdMap = new LinkedHashMap<>();

    public ProviderIds add(ProviderIdType providerIdType, Object value){
        providerIdMap.put(providerIdType, value);
        return this;
    }

    public @Nullable Object get(ProviderIdType providerIdType) {
        return providerIdMap.get(providerIdType);
    }
    public OptionalInt getTvdbId(){
        return ifNotNullOrElseGet(get(ProviderIdType.TVDB), v -> OptionalInt.of((int) v), OptionalInt::empty);
    }
    public Optional<String> getImdbId(){
        return Optional.ofNullable((String) get(ProviderIdType.IMDB));
    }

    public List<Entry<ProviderIdType, Object>> getNonNullIds(){
        return providerIdMap.entrySet().stream().filter(entry -> entry.value != null).toList();
    }

    public boolean isEqual(ProviderIds other, ProviderIdType providerIdType){
        if(!providerIdMap.containsKey(providerIdType) || !other.providerIdMap.containsKey(providerIdType)){
            return false;
        }
        return Objects.equals(providerIdMap.get(providerIdType), other.providerIdMap.get(providerIdType));
    }
}
