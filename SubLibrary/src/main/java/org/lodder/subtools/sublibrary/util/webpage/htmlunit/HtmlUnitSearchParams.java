package org.lodder.subtools.sublibrary.util.webpage.htmlunit;

import java.util.List;
import java.util.function.Predicate;

import org.htmlunit.util.Cookie;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.util.webpage.PageIntf;
import org.lodder.subtools.sublibrary.util.webpage.Proxy;
import org.lodder.subtools.sublibrary.util.webpage.SearchParams;

@NullMarked
public record HtmlUnitSearchParams(
    String url,
    List<Cookie> cookies=List.of(),
    @Nullable Predicate<PageIntf> waitUntilCondition,
    @Nullable Proxy proxy=null)
    implements SearchParams {
}
