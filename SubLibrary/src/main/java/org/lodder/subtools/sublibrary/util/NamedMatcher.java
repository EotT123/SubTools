package org.lodder.subtools.sublibrary.util;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.stream.IntStream;

import manifold.ext.props.rt.api.val;
import manifold.ext.rt.api.Self;

public class NamedMatcher implements NamedMatchResult {

    private final Matcher matcher;
    @val NamedPattern parentPattern;

    NamedMatcher(NamedPattern parentPattern, CharSequence input) {
        this.parentPattern = parentPattern;
        this.matcher = parentPattern.pattern.matcher(input);
    }

    public NamedMatcher reset() {
        matcher.reset();
        return this;
    }

    public NamedMatcher reset(CharSequence input) {
        matcher.reset(input);
        return this;
    }

    public boolean matches() {
        return matcher.matches();
    }

    public boolean find() {
        return matcher.find();
    }

    public boolean find(int start) {
        return matcher.find(start);
    }

    @Override
    public String group() {
        return matcher.group();
    }

    @Override
    public String group(int group) {
        return matcher.group(group);
    }

    @Override
    public int groupCount() {
        return matcher.groupCount();
    }

    @Override
    public String group(String groupName) {
        return group(groupIndex(groupName));
    }

    @Override
    public Map<String, Integer> getNamedGroups() {
        return IntStream.rangeClosed(1, groupCount()).sequential()
            .collect(LinkedHashMap::new, (map, i) -> map.put(parentPattern.groupNames.get(i - 1), i), Map::putAll);
    }

    private int groupIndex(String groupName) {
        return parentPattern.groupNames.indexOf(groupName) + 1;
    }

    @Override
    public int start() {
        return matcher.start();
    }

    @Override
    public int start(int group) {
        return matcher.start(group);
    }

    @Override
    public int start(String groupName) {
        return start(groupIndex(groupName));
    }

    @Override
    public int end() {
        return matcher.end();
    }

    @Override
    public int end(int group) {
        return matcher.end(group);
    }

    @Override
    public int end(String groupName) {
        return end(groupIndex(groupName));
    }

    public String replaceAll(String replacement) {
        return matcher.replaceAll(replacement);
    }

    @Override
    public boolean equals(@Self Object obj) {
        return obj instanceof NamedMatcher nm && Objects.equals(matcher, nm.matcher);
    }

    @Override
    public int hashCode() {
        return matcher.hashCode();
    }

    @Override
    public String toString() {
        return matcher.toString();
    }
}
