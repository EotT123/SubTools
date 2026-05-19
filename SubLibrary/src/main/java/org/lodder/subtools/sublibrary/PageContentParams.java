package org.lodder.subtools.sublibrary;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.cache.CacheType;
import org.lodder.subtools.sublibrary.util.webpage.BrowserMode;
import org.lodder.subtools.sublibrary.util.webpage.http.CookieManager;

@NullMarked
public record PageContentParams(String url,
    CacheType cacheType=CacheType.NONE,
    String userAgent="Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/145.0.0" +
        ".0 Safari/537.36",
    Manager.Retry retry=Manager.Retry.NONE,
    @Nullable CookieManager cookieManager=null,
    BrowserMode browserMode=BrowserMode.HTMLUNIT,
    @Nullable String contentType=null) {
}
