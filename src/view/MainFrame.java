package view;
import com.formdev.flatlaf.intellijthemes.FlatDarkFlatIJTheme;
import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Properties;
import controller.*;
import model.User;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Locale;
import java.util.ResourceBundle;
import javax.swing.border.EmptyBorder;

import util.DBConnection;
import util.LocaleManager;

public class MainFrame extends JFrame {
    
    private JTabbedPane tabbedPane;
    private JMenuBar menuBar;
    private User currentUser;
    private ResourceBundle messages;
    private boolean isRightToLeft;
    
    // Controllers
    private DashboardController dashboardController;
    private ProductController productController;
    private SupplierController supplierController;
    private CustomerController customerController;
    private OrderController orderController;
    private ReportController reportController;
    private CategoryController categoryController;
    private InventoryAdjustmentController inventoryAdjustmentController;
    private UserController userController;

    // Views
    private DashboardView dashboardView;
    private ProductView productView;
    private SupplierView supplierView;
    private CustomerView customerView;
    private OrderView orderView;
    private ReportView reportView;
    private CategoryView categoryView;
    private InventoryAdjustmentView inventoryAdjustmentView;
    private UserManagementView userManagementView;

    public MainFrame(MainController mainController) {
        // Load localization resources
        loadLocalization();
        
        setTitle(messages.getString("app.title"));
        setSize(1024, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Initialize controllers
        initControllers();
        
        // Initialize UI components
        initComponents();
        
        // Create and set menu bar
        createMenuBar();
        
        // Close database connection when application exits
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DBConnection.closeConnection();
            }
        });
    }
    
    private void loadLocalization() {
        // Get current locale from LocaleManager
        Locale currentLocale = LocaleManager.getCurrentLocale();
        messages = ResourceBundle.getBundle("resources.Messages", currentLocale);
        
        // Configure component orientation based on locale
        isRightToLeft = currentLocale.getLanguage().equals("ar");
        if (isRightToLeft) {
            applyRightToLeftOrientation();
        }
    }
    
    private void applyRightToLeftOrientation() {
        // Set right-to-left orientation for the whole application
        setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        JComponent.setDefaultLocale(new Locale("ar"));
        
        // Set default RTL for JOptionPane
        UIManager.put("OptionPane.messageDialogTitle", messages.getString("dialog.title"));
        UIManager.put("OptionPane.buttonOrientation", SwingConstants.RIGHT);
    }
    
    private void initControllers() {
        // Initialize all controllers
        dashboardController = new DashboardController();
        categoryController = new CategoryController();
        productController = new ProductController();
        supplierController = new SupplierController();
        customerController = new CustomerController();
        orderController = new OrderController();
        reportController = new ReportController();
        inventoryAdjustmentController = new InventoryAdjustmentController();
        userController = new UserController();
    }
    
    private void initComponents() {
        // Initialize main tabbed pane
        tabbedPane = new JTabbedPane();
        
        // Apply RTL padding if needed
        if (isRightToLeft) {
            tabbedPane.setBorder(new EmptyBorder(0, 0, 0, 5));
        }
        
        // Create placeholder panels for lazy loading
        JPanel dashboardPlaceholder = createPlaceholderPanel(messages.getString("loading.dashboard"));
        JPanel categoriesPlaceholder = createPlaceholderPanel(messages.getString("loading.categories"));
        JPanel productsPlaceholder = createPlaceholderPanel(messages.getString("loading.products"));
        JPanel suppliersPlaceholder = createPlaceholderPanel(messages.getString("loading.suppliers"));
        JPanel customersPlaceholder = createPlaceholderPanel(messages.getString("loading.customers"));
        JPanel ordersPlaceholder = createPlaceholderPanel(messages.getString("loading.orders"));
        JPanel reportsPlaceholder = createPlaceholderPanel(messages.getString("loading.reports"));
        JPanel adjustmentsPlaceholder = createPlaceholderPanel(messages.getString("loading.adjustments"));
        
        // Add tabs with placeholders
        tabbedPane.addTab(messages.getString("tab.dashboard"), dashboardPlaceholder);
        tabbedPane.addTab(messages.getString("tab.categories"), categoriesPlaceholder);
        tabbedPane.addTab(messages.getString("tab.products"), productsPlaceholder);
        tabbedPane.addTab(messages.getString("tab.suppliers"), suppliersPlaceholder);
        tabbedPane.addTab(messages.getString("tab.customers"), customersPlaceholder);
        tabbedPane.addTab(messages.getString("tab.orders"), ordersPlaceholder);
        tabbedPane.addTab(messages.getString("tab.reports"), reportsPlaceholder);
        tabbedPane.addTab(messages.getString("tab.adjustments"), adjustmentsPlaceholder);
        
        // User Management tab will be added only for admin users
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Add change listener to load views when tabs are selected
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            switch (selectedIndex) {
                case 0: // Dashboard
                    if (dashboardView == null) {
                        dashboardView = new DashboardView(dashboardController);
                        tabbedPane.setComponentAt(0, dashboardView);
                    }
                    break;
                case 1: // Categories
                    if (categoryView == null) {
                        categoryView = new CategoryView(categoryController);
                        tabbedPane.setComponentAt(1, categoryView);
                    }
                    break;
                case 2: // Products
                    if (productView == null) {
                        productView = new ProductView(productController);
                        tabbedPane.setComponentAt(2, productView);
                    }
                    break;
                case 3: // Suppliers
                    if (supplierView == null) {
                        supplierView = new SupplierView(supplierController);
                        tabbedPane.setComponentAt(3, supplierView);
                    }
                    break;
                case 4: // Customers
                    if (customerView == null) {
                        customerView = new CustomerView(customerController);
                        tabbedPane.setComponentAt(4, customerView);
                    }
                    break;
                case 5: // Orders
                    if (orderView == null) {
                        orderView = new OrderView(orderController, customerController);
                        tabbedPane.setComponentAt(5, orderView);
                    }
                    break;
                case 6: // Reports
                    if (reportView == null) {
                        reportView = new ReportView(reportController);
                        tabbedPane.setComponentAt(6, reportView);
                    }
                    break;
                case 7: // Inventory Adjustments
                    if (inventoryAdjustmentView == null) {
                        inventoryAdjustmentView = new InventoryAdjustmentView(inventoryAdjustmentController);
                        tabbedPane.setComponentAt(7, inventoryAdjustmentView);
                    }
                    break;
                case 8: // User Management (admin only)
                    if (userManagementView == null) {
                        userManagementView = new UserManagementView(userController);
                        userManagementView.setCurrentUser(currentUser);
                        tabbedPane.setComponentAt(8, userManagementView);
                    }
                    break;
            }
        });
        
        // Add Import Sample Data button at the bottom
        JButton importSamplesButton = new JButton(messages.getString("button.importSamples"));
        importSamplesButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                importSampleData();
            }
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(isRightToLeft ? FlowLayout.LEFT : FlowLayout.RIGHT));
        buttonPanel.add(importSamplesButton);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Initialize the first tab (Dashboard) immediately
        tabbedPane.setSelectedIndex(0);
    }
    
    private void importSampleData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle(messages.getString("dialog.selectSqlFile"));
        fileChooser.setFileFilter(new javax.swing.filechooser.FileFilter() {
            public boolean accept(File f) {
                return f.isDirectory() || f.getName().toLowerCase().endsWith(".sql");
            }
            public String getDescription() {
                return messages.getString("filter.sqlFiles");
            }
        });
        
        // Try to set initial directory to resources folder
        try {
            String rootPath = new File(".").getCanonicalPath();
            File resourcesDir = new File(rootPath + "/resources");
            if (resourcesDir.exists()) {
                fileChooser.setCurrentDirectory(resourcesDir);
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        
        int result = fileChooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            executeSqlFile(selectedFile);
        }
    }

    private void clearDatabase() {
        int choice = JOptionPane.showConfirmDialog(
                this,
                messages.getString("confirm.clearDatabase"),
                messages.getString("title.clearDatabase"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        
        if (choice == JOptionPane.YES_OPTION) {
            try {
                // Close the database connection first
                DBConnection.closeConnection();
                
                // Delete the database file
                File dbFile = new File("stock-manager.db");
                if (dbFile.exists()) {
                    if (dbFile.delete()) {
                        // Get a new connection to regenerate the database
                        DBConnection.getConnection();
                        
                        // Reset the views
                        dashboardView = null;
                        categoryView = null;
                        productView = null;
                        supplierView = null;
                        customerView = null;
                        orderView = null;
                        reportView = null;
                        inventoryAdjustmentView = null;
                        userManagementView = null;
                        
                        // Replace with placeholders
                        for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                            String tabTitle = tabbedPane.getTitleAt(i);
                            tabbedPane.setComponentAt(i, createPlaceholderPanel(
                                messages.getString("loading.generic").replace("{0}", tabTitle)));
                        }
                        
                        // Reload the current tab
                        int selectedIndex = tabbedPane.getSelectedIndex();
                        tabbedPane.setSelectedIndex(-1); // Force reload
                        tabbedPane.setSelectedIndex(selectedIndex);
                        
                        JOptionPane.showMessageDialog(this, 
                            messages.getString("success.databaseCleared"),
                            messages.getString("title.databaseCleared"), 
                            JOptionPane.INFORMATION_MESSAGE);
                    } else {
                        JOptionPane.showMessageDialog(this,
                            messages.getString("error.deleteDatabase"),
                            messages.getString("title.operationFailed"), 
                            JOptionPane.ERROR_MESSAGE);
                        
                        // Reconnect to the database
                        DBConnection.getConnection();
                    }
                } else {
                    // If file doesn't exist, just create a new database
                    DBConnection.getConnection();
                    JOptionPane.showMessageDialog(this, 
                        messages.getString("info.newDatabaseCreated"),
                        messages.getString("title.databaseReset"), 
                        JOptionPane.INFORMATION_MESSAGE);
                }
            } catch (Exception e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                    messages.getString("error.clearingDatabase") + e.getMessage(),
                    messages.getString("title.operationFailed"), 
                    JOptionPane.ERROR_MESSAGE);
                    
                // Ensure connection is available
                DBConnection.getConnection();
            }
        }
    }   
    
    private void executeSqlFile(File file) {
        try {
            // Show progress dialog
            JDialog progressDialog = new JDialog(this, messages.getString("title.importingData"), true);
            JProgressBar progressBar = new JProgressBar();
            progressBar.setIndeterminate(true);
            JLabel statusLabel = new JLabel(messages.getString("status.readingSqlFile"), JLabel.CENTER);
            
            progressDialog.setLayout(new BorderLayout());
            progressDialog.add(statusLabel, BorderLayout.CENTER);
            progressDialog.add(progressBar, BorderLayout.SOUTH);
            progressDialog.setSize(300, 100);
            progressDialog.setLocationRelativeTo(this);
            
            // Run the SQL execution in a background thread
            new Thread(() -> {
                Connection conn = null;
                try {
                    conn = DBConnection.getConnection();
                    conn.setAutoCommit(false);
                    
                    try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
                        StringBuilder currentStatement = new StringBuilder();
                        String line;
                        int statementCount = 0;
                        
                        SwingUtilities.invokeLater(() -> 
                            statusLabel.setText(messages.getString("status.executingSql")));
                        
                        while ((line = reader.readLine()) != null) {
                            // Skip comments
                            if (line.trim().startsWith("--")) {
                                continue;
                            }
                            
                            // Add the line to the current statement
                            currentStatement.append(line).append(" ");
                            
                            // If the line has a semicolon, execute the statement
                            if (line.trim().endsWith(";")) {
                                String sql = currentStatement.toString().trim();
                                if (!sql.isEmpty()) {
                                    try (Statement stmt = conn.createStatement()) {
                                        stmt.execute(sql);
                                        statementCount++;
                                        final int count = statementCount;
                                        SwingUtilities.invokeLater(() -> 
                                            statusLabel.setText(messages.getString("status.executed")
                                                .replace("{0}", String.valueOf(count))));
                                    } catch (SQLException ex) {
                                        System.err.println("Error executing: " + sql);
                                        System.err.println("Error message: " + ex.getMessage());
                                    }
                                }
                                currentStatement = new StringBuilder();
                            }
                        }
                        
                        conn.commit();
                        
                        final int totalCount = statementCount;
                        SwingUtilities.invokeLater(() -> {
                            progressDialog.dispose();
                            JOptionPane.showMessageDialog(MainFrame.this, 
                                messages.getString("success.importedSql")
                                    .replace("{0}", String.valueOf(totalCount)),
                                messages.getString("title.importComplete"), 
                                JOptionPane.INFORMATION_MESSAGE);
                            
                            // Refresh the currently active view
                            refreshCurrentTab();
                        });
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    SwingUtilities.invokeLater(() -> {
                        progressDialog.dispose();
                        JOptionPane.showMessageDialog(MainFrame.this,
                            messages.getString("error.importingData") + ex.getMessage(),
                            messages.getString("title.importFailed"), 
                            JOptionPane.ERROR_MESSAGE);
                    });
                    
                    if (conn != null) {
                        try {
                            conn.rollback();
                        } catch (SQLException rollbackEx) {
                            rollbackEx.printStackTrace();
                        }
                    }
                } finally {
                    if (conn != null) {
                        try {
                            conn.setAutoCommit(true);
                        } catch (SQLException autoCommitEx) {
                            autoCommitEx.printStackTrace();
                        }
                    }
                }
            }).start();
            
            // Show the progress dialog after starting the thread
            progressDialog.setVisible(true);
            
        } catch (Exception e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this,
                messages.getString("error.importingData") + e.getMessage(),
                messages.getString("title.importFailed"), 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void createMenuBar() {
        menuBar = new JMenuBar();
        
        // Create File menu
        JMenu fileMenu = new JMenu(messages.getString("menu.file"));
        JMenuItem importSamplesMenuItem = new JMenuItem(messages.getString("menu.importSamples"));
        JMenuItem clearDatabaseMenuItem = new JMenuItem(messages.getString("menu.clearDatabase"));
        JMenuItem languageMenuItem = new JMenuItem(messages.getString("menu.language"));
        JMenuItem themeMenuItem = new JMenuItem(messages.getString("menu.theme"));
        JMenuItem exitMenuItem = new JMenuItem(messages.getString("menu.exit"));
        
        importSamplesMenuItem.addActionListener(e -> importSampleData());
        clearDatabaseMenuItem.addActionListener(e -> clearDatabase());
        languageMenuItem.addActionListener(e -> showLanguageDialog());
        themeMenuItem.addActionListener(e -> showThemeDialog());
        exitMenuItem.addActionListener(e -> {
            DBConnection.closeConnection();
            System.exit(0);
        });
        
        fileMenu.add(importSamplesMenuItem);
        fileMenu.add(clearDatabaseMenuItem);
        fileMenu.add(languageMenuItem);
        fileMenu.add(themeMenuItem);
        fileMenu.addSeparator();
        fileMenu.add(exitMenuItem);
        
        // Create User menu
        JMenu userMenu = new JMenu(messages.getString("menu.user"));
        JMenuItem profileMenuItem = new JMenuItem(messages.getString("menu.profile"));
        JMenuItem logoutMenuItem = new JMenuItem(messages.getString("menu.logout"));
        
        profileMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showUserProfileDialog();
            }
        });
        
        logoutMenuItem.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                logout();
            }
        });
        
        userMenu.add(profileMenuItem);
        userMenu.add(logoutMenuItem);
        
        // Add menus to menu bar
        menuBar.add(fileMenu);
        menuBar.add(userMenu);
        
        // Set the menu bar
        setJMenuBar(menuBar);
    }
    // Helper method to determine the index of the current language
    private int getLanguageIndex(Locale locale) {
        String language = locale.getLanguage();
        switch (language) {
            case "ar": return 1;
            case "fr": return 2;
            default: return 0; // English is default
        }
    }
    
    private void showLanguageDialog() {
        String[] languages = {
            messages.getString("language.english"),
            messages.getString("language.arabic"),
            messages.getString("language.french")
        };
        
        String selectedLanguage = (String) JOptionPane.showInputDialog(
            this,
            messages.getString("dialog.selectLanguage"),
            messages.getString("title.language"),
            JOptionPane.QUESTION_MESSAGE,
            null,
            languages,
            languages[getLanguageIndex(LocaleManager.getCurrentLocale())]
        );
        
        if (selectedLanguage != null) {
            Locale newLocale;
            if (selectedLanguage.equals(messages.getString("language.arabic"))) {
                newLocale = new Locale("ar");
            } else if (selectedLanguage.equals(messages.getString("language.french"))) {
                newLocale = new Locale("fr");
            } else {
                newLocale = new Locale("en");
            }
            
            // Only reload if the locale changed
            if (!newLocale.equals(LocaleManager.getCurrentLocale())) {
                LocaleManager.setCurrentLocale(newLocale);
                
                // Inform user to restart application
                JOptionPane.showMessageDialog(
                    this,
                    messages.getString("info.restartRequired"),
                    messages.getString("title.languageChanged"),
                    JOptionPane.INFORMATION_MESSAGE
                );
            }
        }
    }
    
    private void showThemeDialog() {
        String[] themes = {
            "FlatIntelliJLaf",
            "FlatDarkFlatIJTheme",
            "FlatLightFlatIJTheme",
            "FlatDraculaIJTheme",
            "FlatArcIJTheme",
            "FlatArcOrangeIJTheme",
            "FlatArcDarkOrangeIJTheme",
            "FlatCarbonIJTheme",
            "FlatCyanLightIJTheme",
            "FlatDarkPurpleIJTheme",
            "FlatSolarizedDarkIJTheme"
        };
        
        String currentTheme = loadThemePreference();
        
        String selectedTheme = (String) JOptionPane.showInputDialog(
            this,
            messages.getString("dialog.selectTheme"),
            messages.getString("title.theme"),
            JOptionPane.QUESTION_MESSAGE,
            null,
            themes,
            getThemeDisplayName(currentTheme)
        );
        
        if (selectedTheme != null) {
            String themeClass = getThemeClassName(selectedTheme);
            
            try {
                // Save the selected theme to properties
                saveThemePreference(themeClass);
                
                // Inform user to restart application
                JOptionPane.showMessageDialog(
                    this,
                    messages.getString("info.restartRequired"),
                    messages.getString("title.themeChanged"),
                    JOptionPane.INFORMATION_MESSAGE
                );
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                    this,
                    messages.getString("error.themeChange") + ex.getMessage(),
                    messages.getString("title.themeFailed"),
                    JOptionPane.ERROR_MESSAGE
                );
            }
        }
    }


    private String getThemeClassName(String displayName) {
        return "com.formdev.flatlaf.intellijthemes." + displayName;
    }

    private String getThemeDisplayName(String className) {
        // Extract just the class name without package
        if (className.contains(".")) {
            String[] parts = className.split("\\.");
            return parts[parts.length - 1];
        }
        return className;
    }
    // Add this method to save the theme preference
    private void saveThemePreference(String themeClass) {
        try {
            Properties props = new Properties();
            File configFile = new File("config.properties");
            
            // Load existing properties if file exists
            if (configFile.exists()) {
                try (FileInputStream fis = new FileInputStream(configFile)) {
                    props.load(fis);
                }
            }
            
            // Set the theme property
            props.setProperty("app.theme", themeClass);
            
            // Save properties
            try (FileOutputStream fos = new FileOutputStream(configFile)) {
                props.store(fos, "Application Settings");
            }
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Failed to save theme preference: " + e.getMessage());
        }
    }

    // Add this static method to load the theme
    public static String loadThemePreference() {
        Properties props = new Properties();
        File configFile = new File("config.properties");
        
        if (configFile.exists()) {
            try (FileInputStream fis = new FileInputStream(configFile)) {
                props.load(fis);
                String theme = props.getProperty("app.theme");
                return theme != null ? theme : "com.formdev.flatlaf.FlatIntelliJLaf"; // Default theme
            } catch (IOException e) {
                e.printStackTrace();
                return "com.formdev.flatlaf.FlatIntelliJLaf"; // Default theme
            }
        }
        return "com.formdev.flatlaf.FlatIntelliJLaf"; // Default theme
    }
    
    private void showUserProfileDialog() {
        if (currentUser != null) {
            UserProfileDialog dialog = new UserProfileDialog(this, currentUser);
            dialog.setVisible(true);
            
            // If profile was updated, refresh relevant views
            if (dialog.isProfileUpdated()) {
                refreshCurrentTab();
            }
        }
    }
    
    private void logout() {
        // Show confirmation dialog
        int choice = JOptionPane.showConfirmDialog(
                this,
                messages.getString("confirm.logout"),
                messages.getString("title.confirmLogout"),
                JOptionPane.YES_NO_OPTION);
        
        if (choice == JOptionPane.YES_OPTION) {
            // Close the main frame
            dispose();
            
            // Show login screen
            SwingUtilities.invokeLater(() -> {
                LoginView loginView = new LoginView();
                loginView.setVisible(true);
            });
        }
    }
    
    // Add this method to refresh the active tab when needed
    public void refreshCurrentTab() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        switch (selectedIndex) {
            case 0: // Dashboard
                if (dashboardView != null) {
                    dashboardView.refreshData();
                }
                break;
            case 1: // Categories
                if (categoryView != null) {
                    categoryView.refreshData();
                }
                break;
            case 2: // Products
                if (productView != null) {
                    productView.refreshData();
                }
                break;
            case 3: // Suppliers
                if (supplierView != null) {
                    supplierView.refreshData();
                }
                break;
            case 4: // Customers
                if (customerView != null) {
                    customerView.refreshData();
                }
                break;
            case 7: // Inventory Adjustments
                if (inventoryAdjustmentView != null) {
                    inventoryAdjustmentView.refreshData();
                }
                break;
            case 8: // User Management
                if (userManagementView != null) {
                    userManagementView.refreshData();
                }
                break;
        }
    }
    
    private JPanel createPlaceholderPanel(String message) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(message, JLabel.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(label, BorderLayout.CENTER);
        
        // Add a loading spinner or progress indicator
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        panel.add(progressBar, BorderLayout.SOUTH);
        
        return panel;
    }
    
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        
        // Update title to include username
        if (currentUser != null) {
            setTitle(messages.getString("app.titleWithUser")
                .replace("{0}", currentUser.getUsername())
                .replace("{1}", currentUser.getRole()));
            
            // Add User Management tab for Admin users
            if (currentUser.getRole().equalsIgnoreCase("Admin")) {
                // Check if the tab already exists
                boolean tabExists = false;
                for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                    if (tabbedPane.getTitleAt(i).equals(messages.getString("tab.userManagement"))) {
                        tabExists = true;
                        break;
                    }
                }
                
                if (!tabExists) {
                    JPanel userManagementPlaceholder = createPlaceholderPanel(
                        messages.getString("loading.userManagement"));
                    tabbedPane.addTab(messages.getString("tab.userManagement"), userManagementPlaceholder);
                }
            }
        }
    }

    // Modify the main method to load and apply the theme
    public static void main(String[] args) {
        // Set system look and feel using the saved theme preference
        try {
            String themeClass = loadThemePreference();
            UIManager.setLookAndFeel(themeClass);
        } catch (Exception e) {
            e.printStackTrace();
            // Fallback to default theme
            try {
                UIManager.setLookAndFeel("com.formdev.flatlaf.FlatIntelliJLaf");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
        
        // Load database
        DBConnection.getConnection();
        
        // Start application with login screen
        SwingUtilities.invokeLater(() -> {
            LoginView loginView = new LoginView();
            loginView.setVisible(true);
        });
    }

    // Inner class for user profile dialog
    class UserProfileDialog extends JDialog {
        
        private JTextField usernameField;
        private JPasswordField currentPasswordField;
        private JPasswordField newPasswordField;
        private JPasswordField confirmPasswordField;
        private JTextField emailField;
        private JTextField fullNameField;
        private JButton saveButton;
        private JButton cancelButton;
        private JLabel statusLabel;
        
        private User user;
        private boolean profileUpdated = false;
        
        public UserProfileDialog(JFrame parent, User user) {
            super(parent, messages.getString("dialog.userProfile"), true);
            
            this.user = user;
            
            initComponents();
            setupLayout();
            populateFields();
            
            setSize(400, 350);
            setLocationRelativeTo(parent);
            setResizable(false);
            
            // Set component orientation for RTL if needed
            if (isRightToLeft) {
                applyDialogRtlSupport();
            }
        }
        
        private void applyDialogRtlSupport() {
            this.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        
        private void initComponents() {
            usernameField = new JTextField(20);
            usernameField.setEditable(false);
            
            currentPasswordField = new JPasswordField(20);
            newPasswordField = new JPasswordField(20);
            confirmPasswordField = new JPasswordField(20);
            emailField = new JTextField(20);
            fullNameField = new JTextField(20);
            
            saveButton = new JButton(messages.getString("button.saveChanges"));
            cancelButton = new JButton(messages.getString("button.cancel"));
            statusLabel = new JLabel(" ");
            statusLabel.setForeground(Color.RED);
            
            saveButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    saveProfile();
                }
            });
            
            cancelButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dispose();
                }
            });
        }
        
        private void setupLayout() {
            JPanel mainPanel = new JPanel();
            mainPanel.setLayout(new BorderLayout());
            
            // Create form panel
            JPanel formPanel = new JPanel();
            formPanel.setLayout(new GridBagLayout());
            GridBagConstraints gc = new GridBagConstraints();
            gc.fill = GridBagConstraints.HORIZONTAL;
            gc.insets = new Insets(5, 5, 5, 5);
            
            // Username row
            gc.gridx = 0;
            gc.gridy = 0;
            formPanel.add(new JLabel(messages.getString("label.username")), gc);
            
            gc.gridx = 1;
            gc.gridy = 0;
            formPanel.add(usernameField, gc);
            
            // Current Password row
            gc.gridx = 0;
            gc.gridy = 1;
            formPanel.add(new JLabel(messages.getString("label.currentPassword")), gc);
            
            gc.gridx = 1;
            gc.gridy = 1;
            formPanel.add(currentPasswordField, gc);
            
            // New Password row
            gc.gridx = 0;
            gc.gridy = 2;
            formPanel.add(new JLabel(messages.getString("label.newPassword")), gc);
            
            gc.gridx = 1;
            gc.gridy = 2;
            formPanel.add(newPasswordField, gc);
            
            // Confirm Password row
            gc.gridx = 0;
            gc.gridy = 3;
            formPanel.add(new JLabel(messages.getString("label.confirmPassword")), gc);
            
            gc.gridx = 1;
            gc.gridy = 3;
            formPanel.add(confirmPasswordField, gc);
            
            // Email row
            gc.gridx = 0;
            gc.gridy = 4;
            formPanel.add(new JLabel(messages.getString("label.email")), gc);
            
            gc.gridx = 1;
            gc.gridy = 4;
            formPanel.add(emailField, gc);
            
            // Full Name row
            gc.gridx = 0;
            gc.gridy = 5;
            formPanel.add(new JLabel(messages.getString("label.fullName")), gc);
            
            gc.gridx = 1;
            gc.gridy = 5;
            formPanel.add(fullNameField, gc);
            
            // Status label
            gc.gridx = 0;
            gc.gridy = 6;
            gc.gridwidth = 2;
            formPanel.add(statusLabel, gc);
            
            // Create button panel
            JPanel buttonPanel = new JPanel();
            buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
            buttonPanel.add(saveButton);
            buttonPanel.add(cancelButton);
            
            // Add panels to main panel
            mainPanel.add(formPanel, BorderLayout.CENTER);
            mainPanel.add(buttonPanel, BorderLayout.SOUTH);
            
            // Add main panel to dialog
            add(mainPanel);
        }
        
        private void populateFields() {
            usernameField.setText(user.getUsername());
            emailField.setText(user.getEmail());
            fullNameField.setText(user.getFullName());
        }
        
        private void saveProfile() {
            String currentPassword = new String(currentPasswordField.getPassword());
            String newPassword = new String(newPasswordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            String email = emailField.getText().trim();
            String fullName = fullNameField.getText().trim();
            
            // Validate inputs
            if (email.isEmpty() || fullName.isEmpty()) {
                statusLabel.setText(messages.getString("error.emailFullNameRequired"));
                return;
            }
            
            // Validate current password if trying to change password
            if (!newPassword.isEmpty() || !confirmPassword.isEmpty()) {
                if (currentPassword.isEmpty()) {
                    statusLabel.setText(messages.getString("error.currentPasswordRequired"));
                    return;
                }
                
                // Verify current password
                User tempUser = new User();
                tempUser.setPassword(currentPassword);
                
                User authenticatedUser = userController.authenticate(user.getUsername(), currentPassword);
                if (authenticatedUser == null) {
                    statusLabel.setText(messages.getString("error.incorrectPassword"));
                    return;
                }
                
                if (!newPassword.equals(confirmPassword)) {
                    statusLabel.setText(messages.getString("error.passwordsDoNotMatch"));
                    return;
                }
                if (newPassword.length() < 6) {
                    statusLabel.setText(messages.getString("error.passwordTooShort"));
                    return;
                }
            }
            
            // Update user object
            user.setEmail(email);
            user.setFullName(fullName);
            
            // Set new password if provided
            if (!newPassword.isEmpty()) {
                user.setPassword(newPassword);
            }
            
            // Save user
            boolean success = userController.updateUser(user);
            
            if (success) {
                profileUpdated = true;
                JOptionPane.showMessageDialog(this,
                        messages.getString("success.profileUpdated"),
                        messages.getString("title.success"),
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                statusLabel.setText(messages.getString("error.updateProfile"));
            }
        }
        
        public boolean isProfileUpdated() {
            return profileUpdated;
        }
    }
}