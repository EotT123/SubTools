package org.lodder.subtools.sublibrary.util.webpage.webdriver;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.function.Predicate;

import org.htmlunit.util.Cookie;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;
import org.lodder.subtools.sublibrary.util.webpage.PageIntf;
import org.lodder.subtools.sublibrary.util.webpage.Proxy;
import org.lodder.subtools.sublibrary.util.webpage.SearchParams;

@NullMarked
public record WebDriverSearchParams(
    String url,
    List<Cookie> cookies=List.of(),
    @Nullable Predicate<PageIntf> waitUntilCondition=null,
    @Nullable Proxy proxy=null,
    boolean manualCloudflareBypass=true)
    implements SearchParams {

    public String getDomain() {
        try {
            return new URI(url).getHost();
        } catch (URISyntaxException e) {
            return url.replaceAll("http(s)?://|www\\.|/.*", "");
        }
    }
}
