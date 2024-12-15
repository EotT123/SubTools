package org.lodder.subtools.sublibrary;

import java.io.IOException;
import java.io.InputStream;

import lombok.Getter;

public final class ConfigProperties {

    @Getter(lazy = true)
    private static final ConfigProperties instance = new ConfigProperties();
    private final java.util.Properties prop = new java.util.Properties();

    private ConfigProperties() {
        try (InputStream input = getClass().getResourceAsStream("/config.properties")) {
            prop.load(input);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public String getProperty(String key) {
        return prop.getProperty(key);
    }
}
