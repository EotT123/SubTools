package org.lodder.subtools.sublibrary.util.webpage;

import org.jspecify.annotations.NullMarked;

@NullMarked
public record Proxy(String host, int port, boolean socks) {

    public String hostAndPort() {
        return host + ":" + port;
    }
}
