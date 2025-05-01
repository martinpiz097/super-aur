package cl.estencialabs.tools.superaur.config;

import java.io.IOException;
import java.util.Properties;

public class PropertiesManager {
    private final Properties properties;

    private static final String PROPS_FILE_NAME = "/application.properties";

    public PropertiesManager() {
        this.properties = new Properties();
        loadProperties(properties);
    }

    private void loadProperties(Properties properties) {
        try {
            properties.load(getClass().getResourceAsStream(PROPS_FILE_NAME));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public String getValue(String name) {
        if (name == null) {
            return null;
        }
        return properties.getProperty(name);
    }

//    public void setValue(String name, Object value) {
//        if (name == null || value == null) {
//            if (name == null && value == null) {
//                throw new NullPointerException("name and value are null");
//            } else if (name == null) {
//                throw new NullPointerException("name is null");
//            } else {
//                throw new NullPointerException("value is null");
//            }
//        }
//
//        properties.setProperty(name, value.toString());
//    }
}
