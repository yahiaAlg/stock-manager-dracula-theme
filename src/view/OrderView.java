package view;

import controller.OrderController;
import controller.CustomerController;
import model.Order;
import model.Customer;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.List;

public class OrderView extends JPanel {
    
    private JTable orderTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> statusFilter;
    private JComboBox<Customer> customerFilter;
    private JButton searchButton;
    private JButton newOrderButton;
    private JButton viewButton;
    private JButton editButton;
    private JButton deleteButton;
    
    private OrderController orderController;
    private CustomerController customerController;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public OrderView(OrderController orderController, CustomerController customerController) {
        this.orderController = orderController;
        this.customerController = customerController;
        
        setLayout(new BorderLayout());
        
        // Create top panel with search controls
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        searchField = new JTextField(15);
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        
        // Status filter
        searchPanel.add(new JLabel("Status:"));
        statusFilter = new JComboBox<>(new String[] {"All", "New", "Processing", "Shipped", "Delivered", "Cancelled"});
        searchPanel.add(statusFilter);
        
        // Customer filter
        searchPanel.add(new JLabel("Customer:"));
        customerFilter = new JComboBox<>();
        customerFilter.addItem(new Customer(0, "All Customers", "", "", ""));
        loadCustomers();
        searchPanel.add(customerFilter);
        
        searchButton = new JButton("Search");
        searchButton.addActionListener(this::searchOrders);
        searchPanel.add(searchButton);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        newOrderButton = new JButton("New Order");
        newOrderButton.addActionListener(e -> createNewOrder());
        buttonPanel.add(newOrderButton);
        
        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Create table
        String[] columns = {"ID", "Date", "Customer", "Total Amount", "Status", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only actions column is editable
            }
        };
        
        orderTable = new JTable(tableModel);
        orderTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonsRenderer());
        orderTable.getColumnModel().getColumn(5).setCellEditor(new ButtonsEditor(orderTable));
        
        JScrollPane scrollPane = new JScrollPane(orderTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Create bottom panel with buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        viewButton = new JButton("View Selected");
        viewButton.addActionListener(e -> viewSelectedOrder());
        bottomPanel.add(viewButton);
        
        editButton = new JButton("Edit Selected");
        editButton.addActionListener(e -> editSelectedOrder());
        bottomPanel.add(editButton);
        
        deleteButton = new JButton("Delete Selected");
        deleteButton.addActionListener(e -> deleteSelectedOrder());
        bottomPanel.add(deleteButton);
        
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Load initial data
        loadOrders();
    }
    
    private void loadCustomers() {
        List<Customer> customers = customerController.getAllCustomers();
        for (Customer customer : customers) {
            customerFilter.addItem(customer);
        }
    }
    
    private void loadOrders() {
        tableModel.setRowCount(0);
        List<Order> orders = orderController.getAllOrders();
        
        for (Order order : orders) {
            addOrderToTable(order);
        }
    }
    
    private void addOrderToTable(Order order) {
        Object[] row = new Object[6];
        row[0] = order.getId();
        row[1] = dateFormat.format(order.getOrderDate());
        row[2] = order.getCustomerName();
        row[3] = String.format("$%.2f", order.getTotalAmount());
        row[4] = order.getStatus();
        row[5] = "Actions"; // Placeholder for buttons
        
        tableModel.addRow(row);
    }
    
    private void searchOrders(ActionEvent e) {
        tableModel.setRowCount(0);
        
        // Get filter values
        String status = statusFilter.getSelectedItem().toString();
        if (status.equals("All")) {
            status = null;
        }
        
        Customer selectedCustomer = (Customer) customerFilter.getSelectedItem();
        Integer customerId = (selectedCustomer.getId() > 0) ? selectedCustomer.getId() : null;
        
        // Get search term
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            searchTerm = null;
        }
        
        // Search orders with the new search term parameter
        List<Order> orders = orderController.searchOrders(customerId, status, null, null, searchTerm);
        
        for (Order order : orders) {
            addOrderToTable(order);
        }
    }
    private void createNewOrder() {
        OrderForm orderForm = new OrderForm(null, orderController, customerController);
        orderForm.setVisible(true);
        
        // Reload orders after form is closed
        orderForm.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosed(java.awt.event.WindowEvent e) {
                loadOrders();
            }
        });
    }
    
    private void viewSelectedOrder() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow >= 0) {
            int orderId = (int) tableModel.getValueAt(selectedRow, 0);
            Order order = orderController.getOrderById(orderId);
            
            if (order != null) {
                // Create a view-only order form
                OrderForm orderForm = new OrderForm(order, orderController, customerController);
                orderForm.setViewOnly(true);
                orderForm.setVisible(true);
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an order to view", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void editSelectedOrder() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow >= 0) {
            int orderId = (int) tableModel.getValueAt(selectedRow, 0);
            Order order = orderController.getOrderById(orderId);
            
            if (order != null) {
                // Check if order can be edited
                if (order.getStatus().equals("New") || order.getStatus().equals("Processing")) {
                    OrderForm orderForm = new OrderForm(order, orderController, customerController);
                    orderForm.setVisible(true);
                    
                    // Reload orders after form is closed
                    orderForm.addWindowListener(new java.awt.event.WindowAdapter() {
                        @Override
                        public void windowClosed(java.awt.event.WindowEvent e) {
                            loadOrders();
                        }
                    });
                } else {
                    JOptionPane.showMessageDialog(this, 
                        "This order cannot be edited because its status is " + order.getStatus(), 
                        "Cannot Edit", JOptionPane.WARNING_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an order to edit", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void deleteSelectedOrder() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow >= 0) {
            int orderId = (int) tableModel.getValueAt(selectedRow, 0);
            
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this order? This action cannot be undone.",
                "Confirm Delete", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                
            if (confirm == JOptionPane.YES_OPTION) {
                if (orderController.deleteOrder(orderId)) {
                    tableModel.removeRow(selectedRow);
                    JOptionPane.showMessageDialog(this, "Order deleted successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, "Failed to delete order", "Error", JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an order to delete", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    // Custom renderer for the buttons column
    private class ButtonsRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private JButton viewButton = new JButton("View");
        private JButton editButton = new JButton("Edit");
        
        public ButtonsRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 2, 0));
            add(viewButton);
            add(editButton);
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setBackground(table.getSelectionBackground());
            } else {
                setBackground(table.getBackground());
            }
            return this;
        }
    }
    
    // Custom editor for the buttons column
    private class ButtonsEditor extends DefaultCellEditor {
        private JButton viewButton = new JButton("View");
        private JButton editButton = new JButton("Edit");
        private JPanel panel;
        private int clickedRow;
        
        public ButtonsEditor(JTable table) {
            super(new JTextField());
            panel = new JPanel();
            panel.setLayout(new FlowLayout(FlowLayout.CENTER, 2, 0));
            
            viewButton.addActionListener(e -> {
                fireEditingStopped();
                viewOrder(clickedRow);
            });
            
            editButton.addActionListener(e -> {
                fireEditingStopped();
                editOrder(clickedRow);
            });
            
            panel.add(viewButton);
            panel.add(editButton);
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            clickedRow = row;
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }
        
        @Override
        public Object getCellEditorValue() {
            return "Actions";
        }
        
        private void viewOrder(int row) {
            int orderId = (int) tableModel.getValueAt(row, 0);
            Order order = orderController.getOrderById(orderId);
            
            if (order != null) {
                OrderForm orderForm = new OrderForm(order, orderController, customerController);
                orderForm.setViewOnly(true);
                orderForm.setVisible(true);
            }
        }
        
        private void editOrder(int row) {
            int orderId = (int) tableModel.getValueAt(row, 0);
            Order order = orderController.getOrderById(orderId);
            
            if (order != null) {
                // Check if order can be edited
                if (order.getStatus().equals("New") || order.getStatus().equals("Processing")) {
                    OrderForm orderForm = new OrderForm(order, orderController, customerController);
                    orderForm.setVisible(true);
                    
                    // Reload orders after form is closed
                    orderForm.addWindowListener(new java.awt.event.WindowAdapter() {
                        @Override
                        public void windowClosed(java.awt.event.WindowEvent e) {
                            loadOrders();
                        }
                    });
                } else {
                    JOptionPane.showMessageDialog(panel, 
                        "This order cannot be edited because its status is " + order.getStatus(), 
                        "Cannot Edit", JOptionPane.WARNING_MESSAGE);
                }
            }
        }
    }
}