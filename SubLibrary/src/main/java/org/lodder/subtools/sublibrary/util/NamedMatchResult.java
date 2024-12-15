package org.lodder.subtools.sublibrary.util;

import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.stream.IntStream;

public interface NamedMatchResult extends MatchResult {

    default List<String> orderedGroups() {
        return IntStream.rangeClosed(1, groupCount()).sequential().mapToObj(this::group).toList();
    }

    Map<String, Integer> namedGroups();

    String group(String groupName);

    int start(String groupName);

    int end(String groupName);

}
