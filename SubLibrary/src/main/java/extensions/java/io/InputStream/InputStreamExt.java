package extensions.java.io.InputStream;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import manifold.ext.rt.api.Extension;
import manifold.ext.rt.api.This;
import org.jspecify.annotations.NullMarked;

@Extension
@NullMarked
public class InputStreamExt {

    private InputStreamExt() {
        // hide utility class constructor
    }

    public static String asString(@This InputStream inputStream, Charset charset) throws IOException {
        return new String(inputStream.readAllBytes(), charset);
    }
}
