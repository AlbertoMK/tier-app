package server.Utils;

import java.io.IOException;
import java.util.Properties;

public class PropertiesLoader {
    private static Properties properties = new Properties();

    static {
        try {
            properties.load(PropertiesLoader.class.getClassLoader().getResourceAsStream("application.properties"));
        } catch (IOException e) {
            throw new RuntimeException("Error finding application.properties file.");
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}
