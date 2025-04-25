package util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Locale;
import java.util.Properties;

/**
 * Utility class to manage application locale settings
 */
public class LocaleManager {
    private static final String CONFIG_FILE = "config.properties";
    private static final String LOCALE_KEY = "app.locale";
    private static Locale currentLocale;
    
    static {
        // Load locale from properties file or default to system locale
        loadLocale();
    }
    
    /**
     * Get the current application locale
     * @return The current locale
     */
    public static Locale getCurrentLocale() {
        return currentLocale;
    }
    
    /**
     * Set the application locale and save it to the configuration file
     * @param locale The new locale to set
     */
    public static void setCurrentLocale(Locale locale) {
        if (locale != null && !locale.equals(currentLocale)) {
            currentLocale = locale;
            saveLocale();
        }
    }
    
    /**
     * Load the locale from the configuration file
     */
    private static void loadLocale() {
        Properties props = new Properties();
        File configFile = new File(CONFIG_FILE);
        
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);
                String localeStr = props.getProperty(LOCALE_KEY);
                
                if (localeStr != null && !localeStr.isEmpty()) {
                    if (localeStr.equals("ar")) {
                        currentLocale = new Locale("ar");
                    } else {
                        currentLocale = new Locale("en");
                    }
                } else {
                    currentLocale = Locale.getDefault();
                }
            } catch (IOException e) {
                e.printStackTrace();
                currentLocale = Locale.getDefault();
            }
        } else {
            currentLocale = Locale.getDefault();
            saveLocale(); // Create the file with default locale
        }
    }
    
    /**
     * Save the current locale to the configuration file
     */
    private static void saveLocale() {
        Properties props = new Properties();
        
        // Load existing properties if file exists
        File configFile = new File(CONFIG_FILE);
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        // Set the locale property
        props.setProperty(LOCALE_KEY, currentLocale.getLanguage());
        
        // Save properties
        try (FileOutputStream fos = new FileOutputStream(configFile)) {
            props.store(fos, "Application Settings");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}