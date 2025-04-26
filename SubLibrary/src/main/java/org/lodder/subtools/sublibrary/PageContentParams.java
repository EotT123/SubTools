package org.lodder.subtools.sublibrary;

import org.lodder.subtools.sublibrary.cache.CacheType;

public record PageContentParams(String url,
                                CacheType cacheType,
                                String userAgent,
                                Manager.Retry retry) {

    public static PageContentParams params(String url,
        CacheType cacheType=CacheType.NONE,
        String userAgent="Mozilla/5.25 Netscape/5.0 (Windows; I; Win95)",
        Manager.Retry retry=Manager.Retry.NONE) {
        return new PageContentParams(url, cacheType, userAgent, retry);
    }

    public static PageContentParams url(String url) {
        return params(url);
    }
}
