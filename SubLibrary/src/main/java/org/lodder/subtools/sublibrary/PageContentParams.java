package org.lodder.subtools.sublibrary;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.cache.CacheType;
import org.lodder.subtools.sublibrary.util.http.CookieManager;

@NullMarked
public record PageContentParams(String url,
    CacheType cacheType=CacheType.NONE,
    String userAgent="Mozilla/5.25 Netscape/5.0 (Windows; I; Win95)",
    Manager.Retry retry=Manager.Retry.NONE,
    @Nullable CookieManager cookieManager=null) {
}
