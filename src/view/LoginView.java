package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import controller.UserController;
import model.User;

public class LoginView extends JFrame {
    
    private UserController userController;
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton exitButton;
    private JLabel statusLabel;
    
    public LoginView() {
        setTitle("Stock Manager - Login");
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
        
        loginButton = new JButton("Login");
        exitButton = new JButton("Exit");
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
        JLabel titleLabel = new JLabel("Stock Manager");
        titleLabel.setFont(new Font("Arial", Font.BOLD, 24));
        titlePanel.add(titleLabel);
        
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
        
        // Password row
        gc.gridx = 0;
        gc.gridy = 1;
        formPanel.add(new JLabel("Password:"), gc);
        
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
    }
    
    private void login() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        
        // Validate inputs
        if (username.isEmpty() || password.isEmpty()) {
            statusLabel.setText("Username and password are required");
            return;
        }
        
        // Authenticate user
        User user = userController.authenticate(username, password);
        
        if (user != null) {
            if (!user.isActive()) {
                statusLabel.setText("This account is inactive. Please contact administrator.");
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
            statusLabel.setText("Invalid username or password");
            passwordField.setText("");
        }
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