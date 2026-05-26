package org.lodder.subtools.multisubdownloader.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import manifold.ext.props.rt.api.val;
import org.jspecify.annotations.NullMarked;
import org.jspecify.annotations.Nullable;

@NullMarked
public class PropertiesReader {

    private static final PropertiesReader INSTANCE = new PropertiesReader();
    private final Properties properties = new Properties();

    private PropertiesReader() {
        try (InputStream is = getClass().getClassLoader().getResourceAsStream("properties-from-pom.properties")) {
            this.properties.load(is);
        } catch (IOException e) {
            throw new IllegalStateException("Should not happen", e);
        }
    }

    public static @Nullable String getProperty(PomProperty property) {
        return PropertiesReader.INSTANCE.properties.getProperty(property.value);
    }

    @NullMarked
    public enum PomProperty {
        BUILD_TIMESTAMP("build.timestamp");

        @val String value;

        PomProperty(String value) {
            this.value = value;
        }
    }
}
