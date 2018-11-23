package com.dzytsiuk.webserver.util;

import java.io.InputStream;
import java.util.Properties;

public class AppUtil {
    private static final String APP_PROP_FILENAME = "application.properties";
    private static Properties properties;

    public static void init() {
        try (InputStream is = AppUtil.class.getClassLoader().getResourceAsStream(APP_PROP_FILENAME)) {
            properties = new Properties();
            properties.load(is);
        } catch (Exception e) {
            throw new RuntimeException("Failed to read application.properties");
        }
    }

    public static String getApplicationProperty(String propertyKey) {
        if(properties == null){
            init();
        }
        return properties.getProperty(propertyKey);
    }
}
