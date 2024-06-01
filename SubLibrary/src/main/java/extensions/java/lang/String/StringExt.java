package extensions.java.lang.String;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import lombok.experimental.UtilityClass;
import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import org.apache.commons.lang3.StringUtils;

@UtilityClass
@Extension
public class StringExt {

    public static String removeIllegalFilenameChars(@This String s) {
        return s.replace("/", "").replace("\0", "");
    }

    public static String removeIllegalWindowsChars(@This String text) {
        return StringUtils.removeEnd(text.replaceAll("[\\\\/:*?\"<>|]", ""), ".").trim();
    }

    public static String urlEncode(@This String text) {
        return URLEncoder.encode(text, StandardCharsets.UTF_8);
    }
}
