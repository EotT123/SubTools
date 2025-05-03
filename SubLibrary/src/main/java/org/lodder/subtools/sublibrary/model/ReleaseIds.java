package org.lodder.subtools.sublibrary.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Objects;
import java.util.Optional;
import java.util.OptionalInt;

public class ReleaseIds {
    private final Map<ReleaseIdType, Object> releaseIdMap = new LinkedHashMap<>();

    public ReleaseIds add(ReleaseIdType releaseIdType, Object value){
        releaseIdMap.put(releaseIdType, value);
        return this;
    }

    public Object get(ReleaseIdType releaseIdType){
        return releaseIdMap.get(releaseIdType);
    }
    public OptionalInt getTvdbId(){
        Object imdbId = get(ReleaseIdType.TVDB);
        return imdbId == null ? OptionalInt.empty() : OptionalInt.of((int)imdbId);
    }
    public Optional<String> getImdbId(){
        return Optional.ofNullable((String) get(ReleaseIdType.IMDB));
    }

    public List<Entry<ReleaseIdType, Object>> getNonNullIds(){
        return releaseIdMap.entrySet().stream().filter(entry -> entry.value != null).toList();
    }

    public boolean isEqual(ReleaseIds other, ReleaseIdType releaseIdType){
        if(!releaseIdMap.containsKey(releaseIdType) || !other.releaseIdMap.containsKey(releaseIdType)){
            return false;
        }
        return Objects.equals(releaseIdMap.get(releaseIdType), other.releaseIdMap.get(releaseIdType));
    }
}
