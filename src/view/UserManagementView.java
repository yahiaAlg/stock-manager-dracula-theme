package view;

import controller.UserController;
import model.User;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class UserManagementView extends JPanel {
    
    private UserController userController;
    private JTable userTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private User currentUser;
    
    public UserManagementView(UserController userController) {
        this.userController = userController;
        
        setLayout(new BorderLayout());
        
        initComponents();
        setupLayout();
        refreshData();
    }
    
    private void initComponents() {
        // Create table model with column names
        String[] columnNames = {"ID", "Username", "Email", "Full Name", "Role", "Active"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // Make all cells non-editable
            }
        };
        
        // Create table
        userTable = new JTable(tableModel);
        userTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Create buttons
        addButton = new JButton("Add User");
        editButton = new JButton("Edit User");
        deleteButton = new JButton("Delete User");
        
        // Add action listeners
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showUserDialog(null);
            }
        });
        
        editButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = userTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int userId = (int) userTable.getValueAt(selectedRow, 0);
                    User user = userController.getUserById(userId);
                    showUserDialog(user);
                } else {
                    JOptionPane.showMessageDialog(UserManagementView.this,
                            "Please select a user to edit.",
                            "No Selection",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        });
        
        deleteButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = userTable.getSelectedRow();
                if (selectedRow >= 0) {
                    int userId = (int) userTable.getValueAt(selectedRow, 0);
                    
                    // Check if user is trying to delete themselves
                    if (currentUser != null && userId == currentUser.getId()) {
                        JOptionPane.showMessageDialog(UserManagementView.this,
                                "You cannot delete your own account.",
                                "Action Denied",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    
                    int choice = JOptionPane.showConfirmDialog(
                            UserManagementView.this,
                            "Are you sure you want to deactivate this user?",
                            "Confirm Deactivation",
                            JOptionPane.YES_NO_OPTION);
                    
                    if (choice == JOptionPane.YES_OPTION) {
                        if (userController.deleteUser(userId)) {
                            refreshData();
                        } else {
                            JOptionPane.showMessageDialog(UserManagementView.this,
                                    "Failed to deactivate user.",
                                    "Operation Failed",
                                    JOptionPane.ERROR_MESSAGE);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(UserManagementView.this,
                            "Please select a user to deactivate.",
                            "No Selection",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        });
    }
    
    private void setupLayout() {
        // Create scroll pane for the table
        JScrollPane scrollPane = new JScrollPane(userTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Create button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    public void refreshData() {
        // Clear the table
        tableModel.setRowCount(0);
        
        // Fetch users from the controller
        List<User> users = userController.getAllUsers();
        
        // Add users to the table
        for (User user : users) {
            Object[] rowData = {
                    user.getId(),
                    user.getUsername(),
                    user.getEmail(),
                    user.getFullName(),
                    user.getRole(),
                    user.isActive()
            };
            tableModel.addRow(rowData);
        }
    }
    
    private void showUserDialog(User user) {
        // Create a dialog for adding or editing a user
        UserDialog dialog = new UserDialog(SwingUtilities.getWindowAncestor(this), user);
        dialog.setVisible(true);
        
        // Refresh data if dialog was closed with OK
        if (dialog.isDataChanged()) {
            refreshData();
        }
    }
    
    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
        
        // Disable user management if not admin
        if (currentUser != null && !currentUser.getRole().equalsIgnoreCase("Admin")) {
            addButton.setEnabled(false);
            editButton.setEnabled(false);
            deleteButton.setEnabled(false);
        }
    }
    
    // Inner class for user dialog
    class UserDialog extends JDialog {
        
        private JTextField usernameField;
        private JPasswordField passwordField;
        private JTextField emailField;
        private JTextField fullNameField;
        private JComboBox<String> roleComboBox;
        private JCheckBox activeCheckBox;
        private JButton saveButton;
        private JButton cancelButton;
        private JLabel statusLabel;
        
        private User user;
        private boolean isNewUser;
        private boolean dataChanged = false;
        
        public UserDialog(Window parent, User user) {
            super(parent, user == null ? "Add User" : "Edit User", ModalityType.APPLICATION_MODAL);
            
            this.user = user;
            this.isNewUser = (user == null);
            
            if (isNewUser) {
                this.user = new User();
            }
            
            initComponents();
            setupLayout();
            populateFields();
            
            setSize(400, 350);
            setLocationRelativeTo(parent);
            setResizable(false);
        }
        
        private void initComponents() {
            usernameField = new JTextField(20);
            passwordField = new JPasswordField(20);
            emailField = new JTextField(20);
            fullNameField = new JTextField(20);
            
            String[] roles = {"User", "Admin"};
            roleComboBox = new JComboBox<>(roles);
            
            activeCheckBox = new JCheckBox("Active");
            activeCheckBox.setSelected(true);
            
            saveButton = new JButton("Save");
            cancelButton = new JButton("Cancel");
            statusLabel = new JLabel(" ");
            statusLabel.setForeground(Color.RED);
            
            // If editing existing user, disable username field
            if (!isNewUser) {
                usernameField.setEditable(false);
            }
            
            saveButton.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    saveUser();
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
            
            // Password row
            gc.gridx = 0;
            gc.gridy = 1;
            formPanel.add(new JLabel("Password:"), gc);
            
            gc.gridx = 1;
            gc.gridy = 1;
            formPanel.add(passwordField, gc);
            
            // Email row
            gc.gridx = 0;
            gc.gridy = 2;
            formPanel.add(new JLabel("Email:"), gc);
            
            gc.gridx = 1;
            gc.gridy = 2;
            formPanel.add(emailField, gc);
            
            // Full Name row
            gc.gridx = 0;
            gc.gridy = 3;
            formPanel.add(new JLabel("Full Name:"), gc);
            
            gc.gridx = 1;
            gc.gridy = 3;
            formPanel.add(fullNameField, gc);
            
            // Role row
            gc.gridx = 0;
            gc.gridy = 4;
            formPanel.add(new JLabel("Role:"), gc);
            
            gc.gridx = 1;
            gc.gridy = 4;
            formPanel.add(roleComboBox, gc);
            
            // Active row
            gc.gridx = 0;
            gc.gridy = 5;
            formPanel.add(new JLabel("Status:"), gc);
            
            gc.gridx = 1;
            gc.gridy = 5;
            formPanel.add(activeCheckBox, gc);
            
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
            if (!isNewUser) {
                usernameField.setText(user.getUsername());
                emailField.setText(user.getEmail());
                fullNameField.setText(user.getFullName());
                roleComboBox.setSelectedItem(user.getRole());
                activeCheckBox.setSelected(user.isActive());
            }
        }
        
        private void saveUser() {
            // Get values from form
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String email = emailField.getText().trim();
            String fullName = fullNameField.getText().trim();
            String role = (String) roleComboBox.getSelectedItem();
            boolean active = activeCheckBox.isSelected();
            
            // Validate inputs
            if (username.isEmpty() || (!isNewUser && password.isEmpty()) || email.isEmpty() || fullName.isEmpty()) {
                statusLabel.setText("All fields are required");
                return;
            }
            
            if (isNewUser && password.length() < 6) {
                statusLabel.setText("Password must be at least 6 characters");
                return;
            }
            
            // Update user object
            user.setUsername(username);
            if (!password.isEmpty()) {
                user.setPassword(password);
            }
            user.setEmail(email);
            user.setFullName(fullName);
            user.setRole(role);
            user.setActive(active);
            
            // Save user
            boolean success;
            if (isNewUser) {
                success = userController.registerUser(user);
            } else {
                success = userController.updateUser(user);
            }
            
            if (success) {
                dataChanged = true;
                dispose();
            } else {
                statusLabel.setText("Failed to save user");
            }
        }
        
        public boolean isDataChanged() {
            return dataChanged;
        }
    }
}
