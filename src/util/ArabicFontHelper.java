package util;

import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.awt.GraphicsEnvironment;
import java.io.InputStream;
import javax.swing.JComponent;
import javax.swing.UIManager;

/**
 * Helper class for applying proper fonts for Arabic text
 */
public class ArabicFontHelper {
    
    private static Font arabicFont = null;
    private static boolean fontInitialized = false;
    
    /**
     * Get the Arabic font, loading it if necessary
     * @return Font suitable for Arabic text
     */
    public static Font getArabicFont() {
        if (!fontInitialized) {
            initializeFont();
        }
        
        // If we couldn't load the custom font, use a system font suitable for Arabic
        if (arabicFont == null) {
            // Try some common fonts that support Arabic
            String[] arabicFontNames = {"Noto Sans Arabic", "Arabic Typesetting", "Simplified Arabic", "Tahoma"};
            
            for (String fontName : arabicFontNames) {
                Font testFont = new Font(fontName, Font.PLAIN, 14);
                if (testFont.canDisplay('\u0627')) {  // Test with Arabic letter Alef
                    arabicFont = testFont;
                    break;
                }
            }
            
            // If still no suitable font found, use the default
            if (arabicFont == null) {
                arabicFont = new Font(Font.DIALOG, Font.PLAIN, 14);
            }
        }
        
        return arabicFont;
    }
    
    /**
     * Initialize the Arabic font from the bundled font file
     */
    private static void initializeFont() {
        try {
            // Try to load the Amiri font from resources
            InputStream is = ArabicFontHelper.class.getResourceAsStream("/fonts/NotoSansArabic-Regular.ttf");
            if (is != null) {
                System.err.println("font not found");
                arabicFont = Font.createFont(Font.TRUETYPE_FONT, is);
                arabicFont = arabicFont.deriveFont(Font.PLAIN, 14);
                
                // Register font with the Graphics Environment
                GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
                ge.registerFont(arabicFont);
                
                is.close();
            }
        } catch (Exception e) {
            System.err.println("Error loading Arabic font: " + e.getMessage());
            e.printStackTrace();
        }
        
        fontInitialized = true;
    }
    
    /**
     * Apply Arabic font to all components in the container
     * @param container The container to apply fonts to
     */
    public static void applyArabicFont(Container container) {
        Font font = getArabicFont();
        
        // Apply font to the container itself if it's a JComponent
        if (container instanceof JComponent) {
            ((JComponent) container).setFont(font);
        }
        
        // Apply to all contained components recursively
        for (Component comp : container.getComponents()) {
            if (comp instanceof JComponent) {
                ((JComponent) comp).setFont(font);
            }
            
            // Recursively process nested containers
            if (comp instanceof Container) {
                applyArabicFont((Container) comp);
            }
        }
    }
    
    /**
     * Set Arabic font as default for common UI components
     */
    public static void setDefaultArabicFont() {
        Font font = getArabicFont();
        
        // Apply to common UI component types
        UIManager.put("Button.font", font);
        UIManager.put("Label.font", font);
        UIManager.put("TextField.font", font);
        UIManager.put("TextArea.font", font);
        UIManager.put("ComboBox.font", font);
        UIManager.put("Table.font", font);
        UIManager.put("TableHeader.font", font);
        UIManager.put("TabbedPane.font", font);
        UIManager.put("Menu.font", font);
        UIManager.put("MenuItem.font", font);
        UIManager.put("OptionPane.font", font);
        UIManager.put("Panel.font", font);
        UIManager.put("ToolTip.font", font);
    }
}