package extensions.java.lang.String;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.function.Function;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import org.apache.commons.lang3.Strings;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@Extension
@NullMarked
public class StringExt {

    private StringExt() {
        // hide utility class constructor
    }

    public static String removeIllegalFilenameChars(@This String s) {
        return s.replace("/", "").replace("\0", "");
    }

    public static String removeIllegalWindowsChars(@This String text) {
        return Strings.CS.removeEnd(text.replaceAll("[\\\\/:*?\"<>|]", ""), ".").trim();
    }

    public static String urlEncode(@This String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }

    public static InputStream toInputStream(@This String text, Charset charset) {
        return new ByteArrayInputStream(text.getBytes(charset));
    }

    public static <T extends Number> Optional<T> parseAsNumber(@This @Nullable String text,
        Function<String, T> mapper) {
        if (text == null) {
            return Optional.empty();
        }
        try {
            return Optional.of(mapper.apply(text));
        } catch (NumberFormatException e) {
            return Optional.empty();
        }
    }

    public static @Nullable String keepLettersOnly(@This @Nullable String text) {
        return text == null ? null : text.replaceAll("[^A-Za-z]", "");
    }

    public static @Nullable String keepNumbersOnly(@This @Nullable String text) {
        return text == null ? null : text.replaceAll("[^0-9]", "");
    }
}
