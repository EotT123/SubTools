package org.lodder.subtools.sublibrary.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

public class ProviderIds {
    private final Map<ProviderIdType, Object> providerIdMap = new LinkedHashMap<>();

    public ProviderIds add(ProviderIdType providerIdType, Object value){
        providerIdMap.put(providerIdType, value);
        return this;
    }

    public Object get(ProviderIdType providerIdType){
        return providerIdMap.get(providerIdType);
    }
    public OptionalInt getTvdbId(){
        Object tvdbId = get(ProviderIdType.TVDB);
        return tvdbId == null ? OptionalInt.empty() : OptionalInt.of((int) tvdbId);
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
