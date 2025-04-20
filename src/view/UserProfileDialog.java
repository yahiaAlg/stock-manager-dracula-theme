package view;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import controller.UserController;
import model.User;
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
    
    public UserProfileDialog(JFrame parent, User user) {
        super(parent, "User Profile", true);
        
        this.user = user;
        this.userController = new UserController();
        
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
            if (!PasswordUtil.verifyPassword(currentPassword, user.getPassword())) {
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
