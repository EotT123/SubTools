package org.lodder.subtools.sublibrary.util;

import java.io.Serial;

public final class Nothing extends RuntimeException {
    @Serial
    private static final long serialVersionUID = 1L;

    private Nothing() {
        throw new Error("No instances!");
    }
}
