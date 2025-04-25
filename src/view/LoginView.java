package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.Locale;

import controller.UserController;
import model.User;
import util.LocaleManager;
import util.Messages;
import util.ArabicFontHelper;

public class LoginView extends JFrame {
    
    private UserController userController;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton exitButton;
    private JLabel statusLabel;
    private JButton languageButton;
    private boolean isRightToLeft;
    
    public LoginView() {
        // Set the locale and determine text direction
        isRightToLeft = LocaleManager.getCurrentLocale().getLanguage().equals("ar");
        
        // Set component orientation based on locale
        if (isRightToLeft) {
            setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            ArabicFontHelper.setDefaultArabicFont();
        }
        
        setTitle(Messages.getString("login.title"));
        setSize(400, 250);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);
        
        // Initialize controller
        userController = new UserController();
        
        // Initialize components
        initComponents();
        setupLayout();
        
        // Create default admin user if none exists
        createDefaultAdminIfNeeded();
    }
    
    private void initComponents() {
        usernameField = new JTextField(20);
        passwordField = new JPasswordField(20);
        
        // Set RTL for text fields if needed
        if (isRightToLeft) {
            usernameField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            passwordField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        
        loginButton = new JButton(Messages.getString("button.login"));
        exitButton = new JButton(Messages.getString("button.exit"));
        
        // Create language toggle button
        String nextLanguage = isRightToLeft ? "English" : "العربية";
        languageButton = new JButton(nextLanguage);
        
        statusLabel = new JLabel(" ");
        statusLabel.setForeground(Color.RED);
        
        // Add action listeners
        loginButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                login();
            }
        });
        
        exitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        
        languageButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                toggleLanguage();
            }
        });
        
        // Add key listener to password field for Enter key
        passwordField.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    login();
                }
            }
        });
    }
    
    private void setupLayout() {
        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BorderLayout());
        
        // Create logo or app title panel
        JPanel titlePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel titleLabel = new JLabel(Messages.getString("app.title"));
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(titleLabel);
        
        // Add language button to title panel
        titlePanel.add(languageButton);
        
        // Create form panel
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.fill = GridBagConstraints.HORIZONTAL;
        gc.insets = new Insets(5, 5, 5, 5);
        
        // Username row
        gc.gridx = 0;
        gc.gridy = 0;
        formPanel.add(new JLabel(Messages.getString("login.username") + ":"), gc);
        
        gc.gridx = 1;
        gc.gridy = 0;
        formPanel.add(usernameField, gc);
        
        // Password row
        gc.gridx = 0;
        gc.gridy = 1;
        formPanel.add(new JLabel(Messages.getString("login.password") + ":"), gc);
        
        gc.gridx = 1;
        gc.gridy = 1;
        formPanel.add(passwordField, gc);
        
        // Status label
        gc.gridx = 0;
        gc.gridy = 2;
        gc.gridwidth = 2;
        formPanel.add(statusLabel, gc);
        
        // Create button panel
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(loginButton);
        buttonPanel.add(exitButton);
        
        // Add panels to main panel
        mainPanel.add(titlePanel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);
        
        // Add main panel to frame
        add(mainPanel);
        
        // Apply Arabic font if needed
        if (isRightToLeft) {
            ArabicFontHelper.applyArabicFont(this);
        }
    }
    
    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        // Validate inputs
        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText(Messages.getString("login.error.fieldsRequired"));
            return;
        }
        
        // Authenticate user
        User user = userController.authenticate(username, password);
        
        if (user != null) {
            if (!user.isActive()) {
                statusLabel.setText(Messages.getString("login.error.inactiveAccount"));
                return;
            }
            
            // Login successful, close login window
            dispose();
            
            // Open main application
            SwingUtilities.invokeLater(() -> {
                MainFrame mainFrame = new MainFrame(null);
                mainFrame.setCurrentUser(user);
                mainFrame.setVisible(true);
            });
        } else {
            statusLabel.setText(Messages.getString("login.error.invalidCredentials"));
            passwordField.setText("");
        }
    }
    
    private void toggleLanguage() {
        // Toggle between English and Arabic
        if (isRightToLeft) {
            // Switch to English
            LocaleManager.setCurrentLocale(new Locale("en"));
        } else {
            // Switch to Arabic
            LocaleManager.setCurrentLocale(new Locale("ar"));
        }
        
        // Close current window and open a new one
        dispose();
        SwingUtilities.invokeLater(() -> {
            LoginView loginView = new LoginView();
            loginView.setVisible(true);
        });
    }
    
    private void createDefaultAdminIfNeeded() {
        // Check if any users exist in the database
        if (userController.getAllUsers().isEmpty()) {
            // Create default admin user
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword("admin123");
            adminUser.setEmail("admin@stockmanager.com");
            adminUser.setFullName("System Administrator");
            adminUser.setRole("Admin");
            adminUser.setActive(true);
            
            if (userController.registerUser(adminUser)) {
                System.out.println("Default admin user created successfully.");
            } else {
                System.err.println("Failed to create default admin user.");
            }
        }
    }
}