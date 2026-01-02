package org.lodder.subtools.sublibrary.util.throwingfunction;

import org.jspecify.annotations.NullMarked;

@NullMarked
final class SneakyThrowUtil {

    private SneakyThrowUtil() {
    }

    static <T extends Exception, R> R sneakyThrow(Exception t) throws T {
        throw (T) t;
    }
}
