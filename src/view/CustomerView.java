package view;

import controller.CustomerController;
import model.Customer;
import model.Order;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.List;

public class CustomerView extends JPanel {
    
    private CustomerController controller;
    
    private JTable customerTable;
    private DefaultTableModel tableModel;
    
    private JTextField searchField;
    private JButton searchButton;
    private JButton clearButton;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton viewOrdersButton;
    
    private List<Customer> currentCustomers;
    
    public CustomerView(CustomerController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        initComponents();
        loadAllCustomers();
    }
    
    private void initComponents() {
        // Search panel (top)
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Customers"));
        
        JPanel searchFieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        searchField = new JTextField(30);
        searchFieldPanel.add(new JLabel("Search:"));
        searchFieldPanel.add(searchField);
        
        JPanel searchButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchButton = new JButton("Search");
        clearButton = new JButton("Clear");
        
        searchButton.addActionListener(this::onSearchButtonClicked);
        clearButton.addActionListener(e -> {
            searchField.setText("");
            loadAllCustomers();
        });
        
        searchButtonsPanel.add(searchButton);
        searchButtonsPanel.add(clearButton);
        
        searchPanel.add(searchFieldPanel, BorderLayout.CENTER);
        searchPanel.add(searchButtonsPanel, BorderLayout.EAST);
        
        // Table panel (center)
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Customers"));
        
        String[] columnNames = {"ID", "Name", "Contact", "Email", "Address"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        customerTable = new JTable(tableModel);
        customerTable.setFillsViewportHeight(true);
        customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Add double-click listener for editing
        customerTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onEditButtonClicked(null);
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(customerTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons panel (bottom)
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        addButton = new JButton("Add Customer");
        editButton = new JButton("Edit");
        deleteButton = new JButton("Delete");
        viewOrdersButton = new JButton("View Orders");
        
        addButton.addActionListener(e -> showCustomerDialog(null));
        editButton.addActionListener(this::onEditButtonClicked);
        deleteButton.addActionListener(this::onDeleteButtonClicked);
        viewOrdersButton.addActionListener(this::onViewOrdersButtonClicked);
        
        buttonsPanel.add(addButton);
        buttonsPanel.add(editButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(viewOrdersButton);
        
        // Add all panels to the main view
        add(searchPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
    }
    
    private void loadAllCustomers() {
        currentCustomers = controller.getAllCustomers();
        refreshCustomerTable();
    }
    
    private void refreshCustomerTable() {
        // Clear existing data
        tableModel.setRowCount(0);
        
        // Populate table with customers
        for (Customer customer : currentCustomers) {
            Object[] rowData = {
                customer.getId(),
                customer.getName(),
                customer.getContact(),
                customer.getEmail(),
                customer.getAddress()
            };
            tableModel.addRow(rowData);
        }
    }
    
    private void onSearchButtonClicked(ActionEvent e) {
        String searchTerm = searchField.getText().trim();
        
        if (searchTerm.isEmpty()) {
            loadAllCustomers();
        } else {
            currentCustomers = controller.searchCustomers(searchTerm);
            refreshCustomerTable();
        }
    }
    
    private void onEditButtonClicked(ActionEvent e) {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < currentCustomers.size()) {
            Customer selectedCustomer = currentCustomers.get(selectedRow);
            showCustomerDialog(selectedCustomer);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Please select a customer to edit.", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void onDeleteButtonClicked(ActionEvent e) {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < currentCustomers.size()) {
            Customer selectedCustomer = currentCustomers.get(selectedRow);
            
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the customer: " + selectedCustomer.getName() + "?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = controller.deleteCustomer(selectedCustomer.getId());
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "Customer deleted successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    loadAllCustomers();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Error deleting customer. They may have associated orders.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "Please select a customer to delete.", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void onViewOrdersButtonClicked(ActionEvent e) {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < currentCustomers.size()) {
            Customer selectedCustomer = currentCustomers.get(selectedRow);
            showOrdersDialog(selectedCustomer);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Please select a customer to view orders.", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void showCustomerDialog(Customer customer) {
        // Create a dialog for adding/editing customers
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Customer Details");
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField contactField = new JTextField();
        JTextField emailField = new JTextField();
        JTextArea addressField = new JTextArea();
        addressField.setLineWrap(true);
        addressField.setWrapStyleWord(true);
        
        // Populate fields if editing
        if (customer != null) {
            idField.setText(String.valueOf(customer.getId()));
            nameField.setText(customer.getName());
            contactField.setText(customer.getContact());
            emailField.setText(customer.getEmail());
            addressField.setText(customer.getAddress());
        } else {
            // Default values for new customer
            idField.setText("0");
        }
        
        // ID is not editable
        idField.setEditable(false);
        
        formPanel.add(new JLabel("ID:"));
        formPanel.add(idField);
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Contact:"));
        formPanel.add(contactField);
        formPanel.add(new JLabel("Email:"));
        formPanel.add(emailField);
        formPanel.add(new JLabel("Address:"));
        formPanel.add(new JScrollPane(addressField)); // Use scroll pane for text area
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            try {
                // Validate input
                if (nameField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Name is a required field.", 
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                Customer c = new Customer();
                c.setId(Integer.parseInt(idField.getText()));
                c.setName(nameField.getText().trim());
                c.setContact(contactField.getText().trim());
                c.setEmail(emailField.getText().trim());
                c.setAddress(addressField.getText().trim());
                
                boolean success = controller.saveCustomer(c);
                
                if (success) {
                    JOptionPane.showMessageDialog(dialog,
                        "Customer saved successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadAllCustomers();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                        "Error saving customer. Please check your inputs.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Please enter valid values.", 
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    private void showOrdersDialog(Customer customer) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), 
            "Orders for " + customer.getName());
        dialog.setLayout(new BorderLayout());
        dialog.setSize(800, 500);
        dialog.setLocationRelativeTo(this);
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("Orders for customer: " + customer.getName());
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 14));
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel infoPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        infoPanel.add(new JLabel("Contact: " + customer.getContact()));
        infoPanel.add(new JLabel("Email: " + customer.getEmail()));
        infoPanel.add(new JLabel("Address: " + customer.getAddress()));
        headerPanel.add(infoPanel, BorderLayout.SOUTH);
        
        // Table for orders
        String[] columnNames = {"Order ID", "Date", "Total Amount", "Status"};
        DefaultTableModel orderTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable orderTable = new JTable(orderTableModel);
        orderTable.setFillsViewportHeight(true);
        
        // Get orders for this customer
        List<Order> orders = controller.getOrdersByCustomer(customer.getId());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        for (Order order : orders) {
            Object[] rowData = {
                order.getId(),
                dateFormat.format(order.getOrderDate()),
                String.format("$%.2f", order.getTotalAmount()),
                order.getStatus()
            };
            orderTableModel.addRow(rowData);
        }
        
        JScrollPane scrollPane = new JScrollPane(orderTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton newOrderButton = new JButton("New Order");
        JButton viewDetailsButton = new JButton("View Details");
        JButton closeButton = new JButton("Close");
        
        newOrderButton.addActionListener(e -> {
            // Create new order functionality would go here
            // Typically would launch an OrderForm with this customer pre-selected
            JOptionPane.showMessageDialog(dialog, 
                "New order functionality not implemented in this view.\nUse the Orders tab to create a new order.",
                "Information",
                JOptionPane.INFORMATION_MESSAGE);
        });
        
        viewDetailsButton.addActionListener(e -> {
            int selectedRow = orderTable.getSelectedRow();
            if (selectedRow >= 0 && selectedRow < orders.size()) {
                int orderId = orders.get(selectedRow).getId();
                // View order details functionality would go here
                JOptionPane.showMessageDialog(dialog, 
                    "View order details functionality not implemented in this view.\nUse the Orders tab to view order #" + orderId,
                    "Information",
                    JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(dialog, 
                    "Please select an order to view details.", 
                    "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        });
        
        closeButton.addActionListener(e -> dialog.dispose());
        
        footerPanel.add(newOrderButton);
        footerPanel.add(viewDetailsButton);
        footerPanel.add(closeButton);
        
        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(footerPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }

    public void refreshData() {
        loadAllCustomers();
    }
}