package org.lodder.subtools.sublibrary.util.webpage;

import org.jspecify.annotations.NullMarked;
import org.lodder.subtools.sublibrary.util.webpage.PageCondition.PageConditionIntf;

@NullMarked
public interface PageIntf {

    boolean contains(PageConditionIntf pageCondition);

    default boolean doesNotContain(PageConditionIntf pageCondition) {
        return !contains(pageCondition);
    }

    default boolean contains(String text) {
        return getPageSource().contains(text);
    }

    default boolean doesNotContain(String text) {
        return !contains(text);
    }

    String getPageSource();
}
