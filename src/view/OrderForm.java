package view;

import controller.OrderController;
import controller.CustomerController;
import controller.ProductController;
import model.Order;
import model.OrderItem;
import model.Customer;
import model.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class OrderForm extends JDialog {
    
    private Order order;
    private OrderController orderController;
    private CustomerController customerController;
    private ProductController productController;
    
    private JComboBox<Customer> customerCombo;
    private JTextField dateField;
    private JTextField totalField;
    private JComboBox<String> statusCombo;
    
    private JTable itemsTable;
    private DefaultTableModel tableModel;
    
    private JButton addItemButton;
    private JButton removeItemButton;
    private JButton saveButton;
    private JButton cancelButton;
    
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public OrderForm(Order order, OrderController orderController, CustomerController customerController) {
        this.order = (order != null) ? order : new Order();
        this.orderController = orderController;
        this.customerController = customerController;
        this.productController = new ProductController();
        
        setTitle((order != null && order.getId() > 0) ? "Edit Order #" + order.getId() : "New Order");
        setSize(800, 600);
        setModal(true);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        initComponents();
        loadData();
    }
    
    public void setViewOnly(boolean viewOnly) {
        // Disable editable components
        customerCombo.setEnabled(!viewOnly);
        statusCombo.setEnabled(!viewOnly);
        addItemButton.setEnabled(!viewOnly);
        removeItemButton.setEnabled(!viewOnly);
        saveButton.setEnabled(!viewOnly);
    }
    
    private void initComponents() {
        // Order details panel
        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBorder(BorderFactory.createTitledBorder("Order Details"));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Customer
        detailsPanel.add(new JLabel("Customer:"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        customerCombo = new JComboBox<>();
        detailsPanel.add(customerCombo, gbc);
        
        // Date
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        detailsPanel.add(new JLabel("Date:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        dateField = new JTextField(20);
        dateField.setEditable(false);
        detailsPanel.add(dateField, gbc);
        
        // Status
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        detailsPanel.add(new JLabel("Status:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        statusCombo = new JComboBox<>(new String[] {"New", "Processing", "Shipped", "Delivered", "Cancelled"});
        detailsPanel.add(statusCombo, gbc);
        
        // Total
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        detailsPanel.add(new JLabel("Total:"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        totalField = new JTextField(10);
        totalField.setEditable(false);
        detailsPanel.add(totalField, gbc);
        
        add(detailsPanel, BorderLayout.NORTH);
        
        // Order items table
        String[] columns = {"Product ID", "Product Name", "Quantity", "Unit Price", "Subtotal"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only make the quantity column editable
                return column == 2;
            }
        };
        
        itemsTable = new JTable(tableModel);
        
        // Add cell editor listener to detect quantity changes
        itemsTable.getModel().addTableModelListener(e -> {
            if (e.getColumn() == 2) { // Quantity column
                int row = e.getFirstRow();
                updateItemQuantity(row);
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(itemsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder("Order Items"));
        
        add(scrollPane, BorderLayout.CENTER);
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        addItemButton = new JButton("Add Item");
        addItemButton.addActionListener(this::addItem);
        buttonsPanel.add(addItemButton);
        
        removeItemButton = new JButton("Remove Item");
        removeItemButton.addActionListener(this::removeItem);
        buttonsPanel.add(removeItemButton);
        
        saveButton = new JButton("Save Order");
        saveButton.addActionListener(this::saveOrder);
        buttonsPanel.add(saveButton);
        
        cancelButton = new JButton("Cancel");
        cancelButton.addActionListener(e -> dispose());
        buttonsPanel.add(cancelButton);
        
        add(buttonsPanel, BorderLayout.SOUTH);
    }
    
    private void updateItemQuantity(int row) {
        if (row >= 0 && row < tableModel.getRowCount()) {
            try {
                int productId = (int) tableModel.getValueAt(row, 0);
                int newQuantity = Integer.parseInt(tableModel.getValueAt(row, 2).toString());
                // Update the corresponding OrderItem in the order
                for (OrderItem item : order.getOrderItems()) {
                    if (item.getProductId() == productId) {
                        // Update the quantity
                        item.setQuantity(newQuantity);
                        
                        // Update the subtotal in the table
                        double subtotal = item.getSubtotal();
                        tableModel.setValueAt(String.format("$%.2f", subtotal), row, 4);
                        
                        // Recalculate order total
                        order.calculateTotal();
                        updateTotal();
                        break;
                    }
                }
            } catch (NumberFormatException ex) {
                // Handle invalid quantity input
                JOptionPane.showMessageDialog(this, 
                    "Please enter a valid quantity", 
                    "Invalid Input", 
                    JOptionPane.ERROR_MESSAGE);
                
                // Reload order items to reset invalid input
                loadOrderItems();
            }
        }
    }
    
    private void loadData() {
        // Load customers
        List<Customer> customers = customerController.getAllCustomers();
        for (Customer customer : customers) {
            customerCombo.addItem(customer);
            
            // Select the current customer if editing
            if (order.getId() > 0 && customer.getId() == order.getCustomerId()) {
                customerCombo.setSelectedItem(customer);
            }
        }
        
        // Set order date
        if (order.getOrderDate() != null) {
            dateField.setText(dateFormat.format(order.getOrderDate()));
        } else {
            dateField.setText(dateFormat.format(new Date()));
        }
        
        // Set status
        if (order.getStatus() != null && !order.getStatus().isEmpty()) {
            statusCombo.setSelectedItem(order.getStatus());
        }
        
        // Load order items
        loadOrderItems();
        
        // Set total
        updateTotal();
    }
    
    private void loadOrderItems() {
        tableModel.setRowCount(0);
        
        for (OrderItem item : order.getOrderItems()) {
            Object[] row = new Object[5];
            row[0] = item.getProductId();
            row[1] = item.getProductName();
            row[2] = item.getQuantity();
            row[3] = String.format("$%.2f", item.getUnitPrice());
            row[4] = String.format("$%.2f", item.getSubtotal());
            
            tableModel.addRow(row);
        }
    }
    
    private void addItem(ActionEvent e) {
        // Show product selection dialog
        ProductSelectionDialog dialog = new ProductSelectionDialog(this, productController);
        dialog.setVisible(true);
        
        Product selectedProduct = dialog.getSelectedProduct();
        if (selectedProduct != null) {
            // Check if product already exists in order
            boolean exists = false;
            for (int i = 0; i < order.getOrderItems().size(); i++) {
                OrderItem item = order.getOrderItems().get(i);
                if (item.getProductId() == selectedProduct.getId()) {
                    // Increment quantity
                    int newQty = item.getQuantity() + 1;
                    item.setQuantity(newQty);
                    
                    // Update the table
                    for (int row = 0; row < tableModel.getRowCount(); row++) {
                        if ((int)tableModel.getValueAt(row, 0) == item.getProductId()) {
                            tableModel.setValueAt(newQty, row, 2);
                            tableModel.setValueAt(String.format("$%.2f", item.getSubtotal()), row, 4);
                            break;
                        }
                    }
                    
                    exists = true;
                    break;
                }
            }
            
            if (!exists) {
                // Create new order item
                OrderItem item = new OrderItem();
                item.setProductId(selectedProduct.getId());
                item.setProductName(selectedProduct.getName());
                item.setQuantity(1);
                item.setUnitPrice(selectedProduct.getUnitPrice());
                
                // Add to order
                order.addOrderItem(item);
                
                // Add to table
                Object[] row = new Object[5];
                row[0] = item.getProductId();
                row[1] = item.getProductName();
                row[2] = item.getQuantity();
                row[3] = String.format("$%.2f", item.getUnitPrice());
                row[4] = String.format("$%.2f", item.getSubtotal());
                
                tableModel.addRow(row);
            }
            
            // Update total
            order.calculateTotal();
            updateTotal();
        }
    }
    
    private void removeItem(ActionEvent e) {
        int selectedRow = itemsTable.getSelectedRow();
        if (selectedRow >= 0) {
            int productId = (int) tableModel.getValueAt(selectedRow, 0);
            
            // Remove from order
            for (int i = 0; i < order.getOrderItems().size(); i++) {
                OrderItem item = order.getOrderItems().get(i);
                if (item.getProductId() == productId) {
                    order.removeOrderItem(item);
                    break;
                }
            }
            
            // Remove from table
            tableModel.removeRow(selectedRow);
            
            // Update total
            updateTotal();
        } else {
            JOptionPane.showMessageDialog(this, "Please select an item to remove", "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void updateOrderFromTable() {
        // Clear current items
        order.getOrderItems().clear();
        
        // Add items from table
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            OrderItem item = new OrderItem();
            item.setProductId((int) tableModel.getValueAt(i, 0));
            item.setProductName(tableModel.getValueAt(i, 1).toString());
            item.setQuantity(Integer.parseInt(tableModel.getValueAt(i, 2).toString()));
            
            // Parse unit price (remove $ sign)
            String unitPriceStr = tableModel.getValueAt(i, 3).toString().replace("$", "");
            item.setUnitPrice(Double.parseDouble(unitPriceStr));
            
            order.addOrderItem(item);
        }
    }
    
    private void updateTotal() {
        totalField.setText(String.format("$%.2f", order.getTotalAmount()));
    }
    
    private void saveOrder(ActionEvent e) {
        // Validate form
        if (customerCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, "Please select a customer", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (order.getOrderItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, "Order must have at least one item", "Validation Error", JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Update order object
        Customer selectedCustomer = (Customer) customerCombo.getSelectedItem();
        order.setCustomerId(selectedCustomer.getId());
        order.setCustomerName(selectedCustomer.getName());
        
        // Set status
        order.setStatus(statusCombo.getSelectedItem().toString());
        
        // Make sure the order items are synced with the table
        updateOrderFromTable();
        
        // Save order
        if (orderController.saveOrder(order)) {
            JOptionPane.showMessageDialog(this, "Order saved successfully", "Success", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Failed to save order", "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
 
    // Dialog for selecting products
    private class ProductSelectionDialog extends JDialog {
        
        private JTable productTable;
        private DefaultTableModel tableModel;
        private JTextField searchField;
        private JButton searchButton;
        private JButton selectButton;
        private JButton cancelButton;
        
        private Product selectedProduct;
        private ProductController productController;
        
        public ProductSelectionDialog(JDialog parent, ProductController productController) {
            super(parent, "Select Product", true);
            this.productController = productController;
            
            setSize(700, 500);
            setLocationRelativeTo(parent);
            setLayout(new BorderLayout());
            
            // Search panel
            JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
            searchField = new JTextField(20);
            searchButton = new JButton("Search");
            searchButton.addActionListener(e -> searchProducts());
            
            searchPanel.add(new JLabel("Search:"));
            searchPanel.add(searchField);
            searchPanel.add(searchButton);
            
            add(searchPanel, BorderLayout.NORTH);
            
            // Products table
            String[] columns = {"ID", "SKU", "Name", "Unit Price", "In Stock"};
            tableModel = new DefaultTableModel(columns, 0);
            
            productTable = new JTable(tableModel);
            JScrollPane scrollPane = new JScrollPane(productTable);
            add(scrollPane, BorderLayout.CENTER);
            
            // Buttons panel
            JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            
            selectButton = new JButton("Select");
            selectButton.addActionListener(e -> selectProduct());
            buttonsPanel.add(selectButton);
            
            cancelButton = new JButton("Cancel");
            cancelButton.addActionListener(e -> dispose());
            buttonsPanel.add(cancelButton);
            
            add(buttonsPanel, BorderLayout.SOUTH);
            
            // Load initial data
            loadProducts();
        }
        
        private void loadProducts() {
            tableModel.setRowCount(0);
            List<Product> products = productController.getAllProducts();
            
            for (Product product : products) {
                Object[] row = new Object[5];
                row[0] = product.getId();
                row[1] = product.getSku();
                row[2] = product.getName();
                row[3] = String.format("$%.2f", product.getUnitPrice());
                row[4] = product.getStockQty();
                
                tableModel.addRow(row);
            }
        }
        
        private void searchProducts() {
            tableModel.setRowCount(0);
            List<Product> products = productController.searchProducts(searchField.getText(), null, null, null);
            
            for (Product product : products) {
                Object[] row = new Object[5];
                row[0] = product.getId();
                row[1] = product.getSku();
                row[2] = product.getName();
                row[3] = String.format("$%.2f", product.getUnitPrice());
                row[4] = product.getStockQty();
                
                tableModel.addRow(row);
            }
        }
        
        private void selectProduct() {
            int selectedRow = productTable.getSelectedRow();
            if (selectedRow >= 0) {
                int productId = (int) tableModel.getValueAt(selectedRow, 0);
                selectedProduct = productController.getProductById(productId);
                
                // Check if there's enough stock
                if (selectedProduct.getStockQty() <= 0) {
                    JOptionPane.showMessageDialog(this, 
                        "This product is out of stock", 
                        "Out of Stock", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Please select a product", "No Selection", JOptionPane.WARNING_MESSAGE);
            }
        }
        
        public Product getSelectedProduct() {
            return selectedProduct;
        }
    }
}