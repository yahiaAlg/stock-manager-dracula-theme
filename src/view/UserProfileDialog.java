package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Locale;
import java.util.ResourceBundle;
import controller.UserController;
import model.User;
import util.ArabicFontHelper;
import util.LocaleManager;
import util.PasswordUtil;

public class UserProfileDialog extends JDialog {
    
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
    private UserController userController;
    private boolean profileUpdated = false;
    private ResourceBundle messages;
    private boolean isRightToLeft;
    
    public UserProfileDialog(JFrame parent, User user) {
        super(parent, "", true); // Title will be set after loading resources
        
        this.user = user;
        this.userController = new UserController();
        
        // Load localization resources
        loadLocalization();
        
        // Now set the title using the resource bundle
        setTitle(messages.getString("profile.title"));
        
        initComponents();
        setupLayout();
        populateFields();
        
        setSize(400, 350);
        setLocationRelativeTo(parent);
        setResizable(false);
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
        // Set right-to-left orientation for this dialog
        setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        
        // Apply Arabic font to this dialog
        ArabicFontHelper.applyArabicFont(this);
    }
    
    private void initComponents() {
        usernameField = new JTextField(20);
        usernameField.setEditable(false);
        
        currentPasswordField = new JPasswordField(20);
        newPasswordField = new JPasswordField(20);
        confirmPasswordField = new JPasswordField(20);
        emailField = new JTextField(20);
        fullNameField = new JTextField(20);
        
        // Apply RTL to text components if needed
        if (isRightToLeft) {
            usernameField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            currentPasswordField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            newPasswordField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            confirmPasswordField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            emailField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            fullNameField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        
        saveButton = new JButton(messages.getString("profile.saveChanges"));
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
        
        // Create button panel with proper flow direction based on language
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(isRightToLeft ? FlowLayout.LEFT : FlowLayout.RIGHT));
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
            if (!PasswordUtil.verifyPassword(currentPassword, user.getPassword())) {
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
                    messages.getString("dialog.success"),
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