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
import java.util.Locale;
import java.util.ResourceBundle;
import util.ArabicFontHelper;

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
    private ResourceBundle messages;
    private boolean isRightToLeft;
    
    public OrderForm(Order order, OrderController orderController, CustomerController customerController) {
        this.order = (order != null) ? order : new Order();
        this.orderController = orderController;
        this.customerController = customerController;
        this.productController = new ProductController();
        
        // Load localization resources
        loadLocalization();
        
        setTitle((order != null && order.getId() > 0) ? 
            messages.getString("orders.edit") + " #" + order.getId() : 
            messages.getString("orders.new"));
        setSize(800, 600);
        setModal(true);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());
        
        // Configure component orientation based on locale
        if (isRightToLeft) {
            applyRightToLeftOrientation();
        }
        
        initComponents();
        loadData();
    }
    
    private void loadLocalization() {
        // Get current locale from LocaleManager
        Locale currentLocale = util.LocaleManager.getCurrentLocale();
        messages = ResourceBundle.getBundle("resources.Messages", currentLocale);
        
        // Configure component orientation based on locale
        isRightToLeft = currentLocale.getLanguage().equals("ar");
    }
    
    private void applyRightToLeftOrientation() {
        // Set right-to-left orientation for this dialog
        setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        
        // Apply Arabic font to this dialog if needed
        if (isRightToLeft) {
            ArabicFontHelper.applyArabicFont(this);
        }
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
        detailsPanel.setBorder(BorderFactory.createTitledBorder(messages.getString("orders.details")));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.WEST;
        gbc.insets = new Insets(5, 5, 5, 5);
        
        // Customer
        detailsPanel.add(new JLabel(messages.getString("orders.customer") + ":"), gbc);
        gbc.gridx = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        
        customerCombo = new JComboBox<>();
        if (isRightToLeft) {
            customerCombo.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        detailsPanel.add(customerCombo, gbc);
        
        // Date
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0;
        detailsPanel.add(new JLabel(messages.getString("orders.date") + ":"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        dateField = new JTextField(20);
        dateField.setEditable(false);
        if (isRightToLeft) {
            dateField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        detailsPanel.add(dateField, gbc);
        
        // Status
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0;
        detailsPanel.add(new JLabel(messages.getString("orders.status") + ":"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        String[] statusOptions = {
            messages.getString("orders.status.new"),
            messages.getString("orders.status.processing"),
            messages.getString("orders.status.shipped"),
            messages.getString("orders.status.delivered"),
            messages.getString("orders.status.cancelled")
        };
        statusCombo = new JComboBox<>(statusOptions);
        if (isRightToLeft) {
            statusCombo.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        detailsPanel.add(statusCombo, gbc);
        
        // Total
        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.weightx = 0;
        detailsPanel.add(new JLabel(messages.getString("orders.total") + ":"), gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        totalField = new JTextField(10);
        totalField.setEditable(false);
        if (isRightToLeft) {
            totalField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        detailsPanel.add(totalField, gbc);
        
        add(detailsPanel, BorderLayout.NORTH);
        
        // Order items table
        String[] columns = {
            messages.getString("orders.column.productId"),
            messages.getString("orders.column.productName"),
            messages.getString("orders.column.quantity"),
            messages.getString("orders.column.unitPrice"),
            messages.getString("orders.column.subtotal")
        };
        
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                // Only make the quantity column editable
                return column == 2;
            }
        };
        
        itemsTable = new JTable(tableModel);
        
        // Apply RTL to table if needed
        if (isRightToLeft) {
            itemsTable.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            itemsTable.getTableHeader().setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        
        // Add cell editor listener to detect quantity changes
        itemsTable.getModel().addTableModelListener(e -> {
            if (e.getColumn() == 2) { // Quantity column
                int row = e.getFirstRow();
                updateItemQuantity(row);
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(itemsTable);
        scrollPane.setBorder(BorderFactory.createTitledBorder(messages.getString("orders.items")));
        
        add(scrollPane, BorderLayout.CENTER);
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(isRightToLeft ? FlowLayout.LEFT : FlowLayout.RIGHT));
        
        addItemButton = new JButton(messages.getString("orders.button.addItem"));
        addItemButton.addActionListener(this::addItem);
        buttonsPanel.add(addItemButton);
        
        removeItemButton = new JButton(messages.getString("orders.button.removeItem"));
        removeItemButton.addActionListener(this::removeItem);
        buttonsPanel.add(removeItemButton);
        
        saveButton = new JButton(messages.getString("orders.button.save"));
        saveButton.addActionListener(this::saveOrder);
        buttonsPanel.add(saveButton);
        
        cancelButton = new JButton(messages.getString("button.cancel"));
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
                        tableModel.setValueAt(String.format("DZD %.2f", subtotal), row, 4);
                        
                        // Recalculate order total
                        order.calculateTotal();
                        updateTotal();
                        break;
                    }
                }
            } catch (NumberFormatException ex) {
                // Handle invalid quantity input
                JOptionPane.showMessageDialog(this, 
                    messages.getString("orders.error.invalidQuantity"), 
                    messages.getString("dialog.invalidInput"), 
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
            for (int i = 0; i < statusCombo.getItemCount(); i++) {
                String statusValue = translateStatusToStoredValue(statusCombo.getItemAt(i));
                if (statusValue.equals(order.getStatus())) {
                    statusCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
        
        // Load order items
        loadOrderItems();
        
        // Set total
        updateTotal();
    }
    
    // This method translates display status to stored status value
    private String translateStatusToStoredValue(String displayStatus) {
        // Logic to map localized status back to database status values
        if (displayStatus.equals(messages.getString("orders.status.new"))) return "New";
        if (displayStatus.equals(messages.getString("orders.status.processing"))) return "Processing";
        if (displayStatus.equals(messages.getString("orders.status.shipped"))) return "Shipped";
        if (displayStatus.equals(messages.getString("orders.status.delivered"))) return "Delivered";
        if (displayStatus.equals(messages.getString("orders.status.cancelled"))) return "Cancelled";
        return displayStatus; // Default fallback
    }
    
    private void loadOrderItems() {
        tableModel.setRowCount(0);
        
        for (OrderItem item : order.getOrderItems()) {
            Object[] row = new Object[5];
            row[0] = item.getProductId();
            row[1] = item.getProductName();
            row[2] = item.getQuantity();
            row[3] = String.format("DZD %.2f", item.getUnitPrice());
            row[4] = String.format("DZD %.2f", item.getSubtotal());
            
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
                            tableModel.setValueAt(String.format("DZD %.2f", item.getSubtotal()), row, 4);
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
                row[3] = String.format("DZD %.2f", item.getUnitPrice());
                row[4] = String.format("DZD %.2f", item.getSubtotal());
                
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
            JOptionPane.showMessageDialog(this, 
                messages.getString("orders.error.selectToRemove"), 
                messages.getString("dialog.noSelection"), 
                JOptionPane.WARNING_MESSAGE);
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
            String unitPriceStr = tableModel.getValueAt(i, 3).toString().replace("DZD", "");
            item.setUnitPrice(Double.parseDouble(unitPriceStr));
            
            order.addOrderItem(item);
        }
    }
    
    private void updateTotal() {
        totalField.setText(String.format("DZD %.2f", order.getTotalAmount()));
    }
    
    private void saveOrder(ActionEvent e) {
        // Validate form
        if (customerCombo.getSelectedItem() == null) {
            JOptionPane.showMessageDialog(this, 
                messages.getString("orders.error.selectCustomer"), 
                messages.getString("dialog.validationError"), 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        if (order.getOrderItems().isEmpty()) {
            JOptionPane.showMessageDialog(this, 
                messages.getString("orders.error.noItems"), 
                messages.getString("dialog.validationError"), 
                JOptionPane.ERROR_MESSAGE);
            return;
        }
        
        // Update order object
        Customer selectedCustomer = (Customer) customerCombo.getSelectedItem();
        order.setCustomerId(selectedCustomer.getId());
        order.setCustomerName(selectedCustomer.getName());
        
        // Set status - translate displayed status to stored value
        order.setStatus(translateStatusToStoredValue(statusCombo.getSelectedItem().toString()));
        
        // Make sure the order items are synced with the table
        updateOrderFromTable();
        
        // Save order
        if (orderController.saveOrder(order)) {
            JOptionPane.showMessageDialog(this, 
                messages.getString("orders.success.saved"), 
                messages.getString("dialog.success"), 
                JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, 
                messages.getString("orders.error.save"), 
                messages.getString("dialog.error"), 
                JOptionPane.ERROR_MESSAGE);
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
        private ResourceBundle dialogMessages;
        
        public ProductSelectionDialog(JDialog parent, ProductController productController) {
            super(parent, messages.getString("orders.dialog.selectProduct"), true);
            this.productController = productController;
            this.dialogMessages = messages; // Use same bundle
            
            setSize(700, 500);
            setLocationRelativeTo(parent);
            setLayout(new BorderLayout());
            
            // Apply RTL orientation if needed
            if (isRightToLeft) {
                applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
                ArabicFontHelper.applyArabicFont(this);
            }
            
            // Search panel
            JPanel searchPanel = new JPanel(new FlowLayout(isRightToLeft ? FlowLayout.RIGHT : FlowLayout.LEFT));
            searchField = new JTextField(20);
            searchButton = new JButton(dialogMessages.getString("button.search"));
            
            if (isRightToLeft) {
                searchField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            }
            
            searchButton.addActionListener(e -> searchProducts());
            
            searchPanel.add(new JLabel(dialogMessages.getString("common.search") + ":"));
            searchPanel.add(searchField);
            searchPanel.add(searchButton);
            
            add(searchPanel, BorderLayout.NORTH);
            
            // Products table
            String[] columns = {
                dialogMessages.getString("column.id"),
                dialogMessages.getString("products.column.sku"),
                dialogMessages.getString("products.column.name"),
                dialogMessages.getString("products.column.unitPrice"),
                dialogMessages.getString("products.column.stock")
            };
            
            tableModel = new DefaultTableModel(columns, 0);
            
            productTable = new JTable(tableModel);
            
            // Apply RTL to table if needed
            if (isRightToLeft) {
                productTable.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
                productTable.getTableHeader().setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            }
            
            JScrollPane scrollPane = new JScrollPane(productTable);
            add(scrollPane, BorderLayout.CENTER);
            
            // Buttons panel
            JPanel buttonsPanel = new JPanel(new FlowLayout(isRightToLeft ? FlowLayout.LEFT : FlowLayout.RIGHT));
            
            selectButton = new JButton(dialogMessages.getString("button.select"));
            selectButton.addActionListener(e -> selectProduct());
            buttonsPanel.add(selectButton);
            
            cancelButton = new JButton(dialogMessages.getString("button.cancel"));
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
                row[3] = String.format("DZD %.2f", product.getUnitPrice());
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
                row[3] = String.format("DZD %.2f", product.getUnitPrice());
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
                        dialogMessages.getString("orders.error.outOfStock"), 
                        dialogMessages.getString("dialog.outOfStock"), 
                        JOptionPane.WARNING_MESSAGE);
                    return;
                }
                
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, 
                    dialogMessages.getString("products.error.selectToAdd"), 
                    dialogMessages.getString("dialog.noSelection"), 
                    JOptionPane.WARNING_MESSAGE);
            }
        }
        
        public Product getSelectedProduct() {
            return selectedProduct;
        }
    }
}