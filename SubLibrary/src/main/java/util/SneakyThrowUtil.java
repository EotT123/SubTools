package util;

public  final class SneakyThrowUtil {
    private SneakyThrowUtil() {
    }

    public static <T extends Exception, R> R sneakyThrow(Exception t) throws T {
        throw (T) t;
    }
}