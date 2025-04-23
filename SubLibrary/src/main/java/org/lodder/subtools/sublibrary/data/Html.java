package org.lodder.subtools.sublibrary.data;

import java.util.Map;
import java.util.concurrent.TimeUnit;

import lombok.AllArgsConstructor;
import manifold.ext.props.rt.api.val;
import org.lodder.subtools.sublibrary.Manager;

@AllArgsConstructor
public class Html {
    @val Manager manager;
    private final String userAgent;

    public Html(Manager manager) {
        this.manager = manager;
        this.userAgent = "";
    }

    public void storeCookies(String domain, Map<String, String> cookieMap) {
        manager.storeCookies(domain, cookieMap);
    }

    public void sleepSeconds(long seconds) {
        try {
            TimeUnit.SECONDS.sleep(seconds);
        } catch (InterruptedException e1) {
            // restore interrupted status
            Thread.currentThread().interrupt();
        }
    }
}
