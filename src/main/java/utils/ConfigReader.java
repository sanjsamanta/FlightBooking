package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

// Reads configuration values from config.properties
public class ConfigReader {

    private static final Properties properties = new Properties();

    static {
        try {
            FileInputStream fis = new FileInputStream("src/test/resources/config.properties");
            properties.load(fis);
        } catch (IOException e) {
            System.out.println("Config file not found: "+e.getMessage());
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }
}
