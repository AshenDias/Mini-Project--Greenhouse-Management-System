package util;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Properties;

public class Config {
    private static final String CONFIG_FILE = "config.properties";
    private static Config instance;
    private Properties properties;

    private Config() {
        properties = new Properties();
        loadProperties();
    }

    public static synchronized Config getInstance() {
        if (instance == null) {
            instance = new Config();
        }
        return instance;
    }


    private void loadProperties() {
        try (FileInputStream fis = new FileInputStream(CONFIG_FILE)) {
            properties.load(fis);
        } catch (IOException e) {

            setDefaultProperties();
            saveProperties();
        }
    }


    private void setDefaultProperties() {
        properties.setProperty("db.url", "jdbc:mysql://localhost:3306/dias");
        properties.setProperty("db.user", "root");
        properties.setProperty("db.password", "asdf4444");
        properties.setProperty("app.name", "Greenhouse Management System");
        properties.setProperty("app.version", "1.0.0");
        properties.setProperty("log.level", "INFO");
        properties.setProperty("backup.enabled", "true");
        properties.setProperty("backup.location", "./backups/");
    }


    public void saveProperties() {
        try (FileOutputStream fos = new FileOutputStream(CONFIG_FILE)) {
            properties.store(fos, "Greenhouse Management System Configuration");
        } catch (IOException e) {
            Logger.getInstance().error("Failed to save configuration", e);
        }
    }


    public String getProperty(String key) {
        return properties.getProperty(key);
    }


    public String getProperty(String key, String defaultValue) {
        return properties.getProperty(key, defaultValue);
    }


    public void setProperty(String key, String value) {
        properties.setProperty(key, value);
        saveProperties();
    }


    public String getDbUrl() {
        return getProperty("db.url");
    }


    public String getDbUser() {
        return getProperty("db.user");
    }


    public String getDbPassword() {
        return getProperty("db.password");
    }


    public String getAppName() {
        return getProperty("app.name");
    }


    public boolean isBackupEnabled() {
        return Boolean.parseBoolean(getProperty("backup.enabled", "true"));
    }


    public String getBackupLocation() {
        return getProperty("backup.location", "./backups/");
    }
}
