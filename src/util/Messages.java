package util;

import java.util.ResourceBundle;

public class Messages {
    private static final String BUNDLE_NAME = "resources.Messages";
    
    public static ResourceBundle getBundle() {
        return ResourceBundle.getBundle(BUNDLE_NAME, LocaleManager.getCurrentLocale());
    }
    
    public static String getString(String key) {
        try {
            return getBundle().getString(key);
        } catch (Exception e) {
            return "!" + key + "!";
        }
    }
}