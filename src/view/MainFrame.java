package view;

import javax.swing.*;

import controller.*;
import model.User;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import util.DBConnection;

public class MainFrame extends JFrame {
    
    private JTabbedPane tabbedPane;
    private JMenuBar menuBar;
    private User currentUser;
    
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
        setTitle("Stock Manager");
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
        
        // Create placeholder panels for lazy loading
        JPanel dashboardPlaceholder = createPlaceholderPanel("Loading Dashboard...");
        JPanel categoriesPlaceholder = createPlaceholderPanel("Loading Categories...");
        JPanel productsPlaceholder = createPlaceholderPanel("Loading Products...");
        JPanel suppliersPlaceholder = createPlaceholderPanel("Loading Suppliers...");
        JPanel customersPlaceholder = createPlaceholderPanel("Loading Customers...");
        JPanel ordersPlaceholder = createPlaceholderPanel("Loading Orders...");
        JPanel reportsPlaceholder = createPlaceholderPanel("Loading Reports...");
        JPanel adjustmentsPlaceholder = createPlaceholderPanel("Loading Inventory Adjustments...");
        
        // Add tabs with placeholders
        tabbedPane.addTab("Dashboard", dashboardPlaceholder);
        tabbedPane.addTab("Category", categoriesPlaceholder);
        tabbedPane.addTab("Products", productsPlaceholder);
        tabbedPane.addTab("Suppliers", suppliersPlaceholder);
        tabbedPane.addTab("Customers", customersPlaceholder);
        tabbedPane.addTab("Orders", ordersPlaceholder);
        tabbedPane.addTab("Reports", reportsPlaceholder);
        tabbedPane.addTab("Inventory Adjustments", adjustmentsPlaceholder);
        
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
        
        // Initialize the first tab (Dashboard) immediately
        tabbedPane.setSelectedIndex(0);
    }
    
    private void createMenuBar() {
        menuBar = new JMenuBar();
        
        // Create File menu
        JMenu fileMenu = new JMenu("File");
        JMenuItem exitMenuItem = new JMenuItem("Exit");
        exitMenuItem.addActionListener(e -> {
            DBConnection.closeConnection();
            System.exit(0);
        });
        fileMenu.add(exitMenuItem);
        
        // Create User menu
        JMenu userMenu = new JMenu("User");
        JMenuItem profileMenuItem = new JMenuItem("Profile");
        JMenuItem logoutMenuItem = new JMenuItem("Logout");
        
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
                "Are you sure you want to logout?",
                "Confirm Logout",
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
            setTitle("Stock Manager - Logged in as: " + currentUser.getUsername() + " (" + currentUser.getRole() + ")");
            
            // Add User Management tab for Admin users
            if (currentUser.getRole().equalsIgnoreCase("Admin")) {
                // Check if the tab already exists
                boolean tabExists = false;
                for (int i = 0; i < tabbedPane.getTabCount(); i++) {
                    if (tabbedPane.getTitleAt(i).equals("User Management")) {
                        tabExists = true;
                        break;
                    }
                }
                
                if (!tabExists) {
                    JPanel userManagementPlaceholder = createPlaceholderPanel("Loading User Management...");
                    tabbedPane.addTab("User Management", userManagementPlaceholder);
                }
            }
        }
    }

    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.intellijthemes.FlatGradiantoMidnightBlueIJTheme");
        } catch (Exception e) {
            e.printStackTrace();
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
            super(parent, "User Profile", true);
            
            this.user = user;
            
            initComponents();
            setupLayout();
            populateFields();
            
            setSize(400, 350);
            setLocationRelativeTo(parent);
            setResizable(false);
        }
        
        private void initComponents() {
            usernameField = new JTextField(20);
            usernameField.setEditable(false);
            
            currentPasswordField = new JPasswordField(20);
            newPasswordField = new JPasswordField(20);
            confirmPasswordField = new JPasswordField(20);
            emailField = new JTextField(20);
            fullNameField = new JTextField(20);
            
            saveButton = new JButton("Save Changes");
            cancelButton = new JButton("Cancel");
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
            formPanel.add(new JLabel("Username:"), gc);
            
            gc.gridx = 1;
            gc.gridy = 0;
            formPanel.add(usernameField, gc);
            
            // Current Password row
            gc.gridx = 0;
            gc.gridy = 1;
            formPanel.add(new JLabel("Current Password:"), gc);
            
            gc.gridx = 1;
            gc.gridy = 1;
            formPanel.add(currentPasswordField, gc);
            
            // New Password row
            gc.gridx = 0;
            gc.gridy = 2;
            formPanel.add(new JLabel("New Password:"), gc);
            
            gc.gridx = 1;
            gc.gridy = 2;
            formPanel.add(newPasswordField, gc);
            
            // Confirm Password row
            gc.gridx = 0;
            gc.gridy = 3;
            formPanel.add(new JLabel("Confirm Password:"), gc);
            
            gc.gridx = 1;
            gc.gridy = 3;
            formPanel.add(confirmPasswordField, gc);
            
            // Email row
            gc.gridx = 0;
            gc.gridy = 4;
            formPanel.add(new JLabel("Email:"), gc);
            
            gc.gridx = 1;
            gc.gridy = 4;
            formPanel.add(emailField, gc);
            
            // Full Name row
            gc.gridx = 0;
            gc.gridy = 5;
            formPanel.add(new JLabel("Full Name:"), gc);
            
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
                statusLabel.setText("Email and Full Name are required");
                return;
            }
            
            // Validate current password if trying to change password
            if (!newPassword.isEmpty() || !confirmPassword.isEmpty()) {
                if (currentPassword.isEmpty()) {
                    statusLabel.setText("Current password is required to change password");
                    return;
                }
                
                // Verify current password
                User tempUser = new User();
                tempUser.setUsername(user.getUsername());
                tempUser.setPassword(currentPassword);
                
                User authenticatedUser = userController.authenticate(user.getUsername(), currentPassword);
                if (authenticatedUser == null) {
                    statusLabel.setText("Current password is incorrect");
                    return;
                }
                
                if (!newPassword.equals(confirmPassword)) {
                    statusLabel.setText("New passwords do not match");
                    return;
                }
                if (newPassword.length() < 6) {
                    statusLabel.setText("New password must be at least 6 characters");
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
                        "Profile updated successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                statusLabel.setText("Failed to update profile");
            }
        }
        
        public boolean isProfileUpdated() {
            return profileUpdated;
        }
    }
}