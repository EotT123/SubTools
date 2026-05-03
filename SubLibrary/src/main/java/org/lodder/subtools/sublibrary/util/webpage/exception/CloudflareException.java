package org.lodder.subtools.sublibrary.util.webpage.exception;

import java.io.Serial;

import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class CloudflareException extends WebpageException {
    @Serial
    private static final long serialVersionUID = 1L;

    public CloudflareException(String url, @Nullable Exception e=null) {
        super("Could not access Cloudflare protected site: " + url, e);
    }
}
