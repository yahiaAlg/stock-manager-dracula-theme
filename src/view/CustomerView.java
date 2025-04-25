package view;

import controller.CustomerController;
import controller.OrderController;
import model.Customer;
import model.Order;
import util.ArabicFontHelper;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;

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
    private ResourceBundle messages;
    private boolean isRightToLeft;
    
    public CustomerView(CustomerController controller) {
        this.controller = controller;
        
        // Load localization resources
        loadLocalization();
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        initComponents();
        loadAllCustomers();
    }
    
    private void loadLocalization() {
        // Get current locale from LocaleManager
        Locale currentLocale = util.LocaleManager.getCurrentLocale();
        messages = ResourceBundle.getBundle("resources.Messages", currentLocale);
        
        // Configure component orientation based on locale
        isRightToLeft = currentLocale.getLanguage().equals("ar");
        if (isRightToLeft) {
            applyRightToLeftOrientation();
        }
    }
    
    private void applyRightToLeftOrientation() {
        // Set right-to-left orientation for this panel
        setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        
        // Apply Arabic font to this panel if needed
        if (isRightToLeft) {
            ArabicFontHelper.applyArabicFont(this);
        }
    }
    
    private void initComponents() {
        // Search panel (top)
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBorder(BorderFactory.createTitledBorder(messages.getString("customers.searchTitle")));
        
        JPanel searchFieldPanel = new JPanel(new FlowLayout(isRightToLeft ? FlowLayout.RIGHT : FlowLayout.LEFT));
        
        searchField = new JTextField(30);
        if (isRightToLeft) {
            searchField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        
        searchFieldPanel.add(new JLabel(messages.getString("common.search") + ":"));
        searchFieldPanel.add(searchField);
        
        JPanel searchButtonsPanel = new JPanel(new FlowLayout(isRightToLeft ? FlowLayout.LEFT : FlowLayout.RIGHT));
        searchButton = new JButton(messages.getString("button.search"));
        clearButton = new JButton(messages.getString("button.clear"));
        
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
        tablePanel.setBorder(BorderFactory.createTitledBorder(messages.getString("customers.title")));
        
        String[] columnNames = {
            messages.getString("column.id"),
            messages.getString("customers.column.name"),
            messages.getString("customers.column.contact"),
            messages.getString("customers.column.email"),
            messages.getString("customers.column.address")
        };
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        customerTable = new JTable(tableModel);
        customerTable.setFillsViewportHeight(true);
        customerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Apply RTL to table if needed
        if (isRightToLeft) {
            customerTable.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            customerTable.getTableHeader().setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        
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
        JPanel buttonsPanel = new JPanel(new FlowLayout(isRightToLeft ? FlowLayout.LEFT : FlowLayout.RIGHT));
        
        addButton = new JButton(messages.getString("customers.button.add"));
        editButton = new JButton(messages.getString("button.edit"));
        deleteButton = new JButton(messages.getString("button.delete"));
        viewOrdersButton = new JButton(messages.getString("customers.button.viewOrders"));
        
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
                messages.getString("error.noSelection"),
                messages.getString("dialog.noSelection"), 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void onDeleteButtonClicked(ActionEvent e) {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < currentCustomers.size()) {
            Customer selectedCustomer = currentCustomers.get(selectedRow);
            
            int confirm = JOptionPane.showConfirmDialog(this,
                messages.getString("customers.confirm.delete").replace("{0}", selectedCustomer.getName()),
                messages.getString("dialog.confirmDeletion"),
                JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = controller.deleteCustomer(selectedCustomer.getId());
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        messages.getString("customers.success.deleted"),
                        messages.getString("dialog.success"),
                        JOptionPane.INFORMATION_MESSAGE);
                    loadAllCustomers();
                } else {
                    JOptionPane.showMessageDialog(this,
                        messages.getString("customers.error.delete"),
                        messages.getString("dialog.error"),
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                messages.getString("customers.error.selectToDelete"), 
                messages.getString("dialog.noSelection"), 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void onViewOrdersButtonClicked(ActionEvent e) {
        int selectedRow = customerTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < currentCustomers.size()) {
            Customer selectedCustomer = currentCustomers.get(selectedRow);
            showOrdersDialog(selectedCustomer);
        } else {
            JOptionPane.showMessageDialog(this, 
                messages.getString("customers.error.selectToViewOrders"), 
                messages.getString("dialog.noSelection"), 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void showCustomerDialog(Customer customer) {
        // Create a dialog for adding/editing customers
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), 
            messages.getString("dialog.customerDetails"));
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 350);
        dialog.setLocationRelativeTo(this);
        
        // Apply RTL orientation if needed
        if (isRightToLeft) {
            dialog.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            ArabicFontHelper.applyArabicFont(dialog);
        }
        
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField contactField = new JTextField();
        JTextField emailField = new JTextField();
        JTextArea addressField = new JTextArea();
        addressField.setLineWrap(true);
        addressField.setWrapStyleWord(true);
        
        // Apply RTL to text components if needed
        if (isRightToLeft) {
            idField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            nameField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            contactField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            emailField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            addressField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        
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
        
        formPanel.add(new JLabel(messages.getString("column.id") + ":"));
        formPanel.add(idField);
        formPanel.add(new JLabel(messages.getString("customers.column.name") + ":"));
        formPanel.add(nameField);
        formPanel.add(new JLabel(messages.getString("customers.column.contact") + ":"));
        formPanel.add(contactField);
        formPanel.add(new JLabel(messages.getString("customers.column.email") + ":"));
        formPanel.add(emailField);
        formPanel.add(new JLabel(messages.getString("customers.column.address") + ":"));
        formPanel.add(new JScrollPane(addressField)); // Use scroll pane for text area
        
        JPanel buttonPanel = new JPanel(new FlowLayout(isRightToLeft ? FlowLayout.LEFT : FlowLayout.RIGHT));
        JButton saveButton = new JButton(messages.getString("button.save"));
        JButton cancelButton = new JButton(messages.getString("button.cancel"));
        
        saveButton.addActionListener(e -> {
            try {
                // Validate input
                if (nameField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, 
                        messages.getString("customers.error.nameRequired"), 
                        messages.getString("dialog.validationError"), 
                        JOptionPane.ERROR_MESSAGE);
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
                        messages.getString("customers.success.saved"),
                        messages.getString("dialog.success"),
                        JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadAllCustomers();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                        messages.getString("customers.error.save"),
                        messages.getString("dialog.error"),
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, 
                    messages.getString("error.invalidNumber"), 
                    messages.getString("dialog.inputError"), 
                    JOptionPane.ERROR_MESSAGE);
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
            messages.getString("customers.ordersFor") + " " + customer.getName());
        dialog.setLayout(new BorderLayout());
        dialog.setSize(800, 500);
        dialog.setLocationRelativeTo(this);
        
        // Apply RTL orientation if needed
        if (isRightToLeft) {
            dialog.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            ArabicFontHelper.applyArabicFont(dialog);
        }
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel(messages.getString("customers.ordersForCustomer") + " " + customer.getName());
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 14));
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel infoPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        infoPanel.add(new JLabel(messages.getString("customers.column.contact") + ": " + customer.getContact()));
        infoPanel.add(new JLabel(messages.getString("customers.column.email") + ": " + customer.getEmail()));
        infoPanel.add(new JLabel(messages.getString("customers.column.address") + ": " + customer.getAddress()));
        headerPanel.add(infoPanel, BorderLayout.SOUTH);
        
        // Table for orders
        String[] columnNames = {
            messages.getString("orders.column.id"),
            messages.getString("orders.column.date"),
            messages.getString("orders.column.totalAmount"),
            messages.getString("orders.column.status")
        };
        
        DefaultTableModel orderTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable orderTable = new JTable(orderTableModel);
        orderTable.setFillsViewportHeight(true);
        
        // Apply RTL to table if needed
        if (isRightToLeft) {
            orderTable.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            orderTable.getTableHeader().setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        
        // Get orders for this customer
        List<Order> orders = controller.getOrdersByCustomer(customer.getId());
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        
        for (Order order : orders) {
            Object[] rowData = {
                order.getId(),
                dateFormat.format(order.getOrderDate()),
                String.format("DZD %.2f", order.getTotalAmount()),
                order.getStatus()
            };
            orderTableModel.addRow(rowData);
        }
        
        JScrollPane scrollPane = new JScrollPane(orderTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel footerPanel = new JPanel(new FlowLayout(isRightToLeft ? FlowLayout.LEFT : FlowLayout.RIGHT));
        JButton newOrderButton = new JButton(messages.getString("orders.button.new"));
        JButton viewDetailsButton = new JButton(messages.getString("orders.button.viewDetails"));
        JButton closeButton = new JButton(messages.getString("button.close"));
        
        // Store customer reference for use in lambda
        Customer customerForOrder = customer;

        newOrderButton.addActionListener(e -> {
            Order newOrder = new Order();
            newOrder.setCustomerId(customerForOrder.getId());
            newOrder.setCustomerName(customerForOrder.getName());
            dialog.dispose(); // Close the current dialog
            OrderForm orderForm = new OrderForm(newOrder, new OrderController(), controller);
            orderForm.setVisible(true);
        });
        
        viewDetailsButton.addActionListener(e -> {
            int selectedRow = orderTable.getSelectedRow();
            if (selectedRow >= 0 && selectedRow < orders.size()) {
                Order selectedOrder = orders.get(selectedRow);
                
                // Get the complete order with items before displaying
                OrderController orderController = new OrderController();
                Order completeOrder = orderController.getOrderById(selectedOrder.getId());
                
                OrderForm orderForm = new OrderForm(completeOrder, orderController, controller);
                orderForm.setViewOnly(false);
                orderForm.setVisible(true);
            } else {
                JOptionPane.showMessageDialog(dialog, 
                    messages.getString("orders.error.selectToView"), 
                    messages.getString("dialog.noSelection"), 
                    JOptionPane.WARNING_MESSAGE);
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