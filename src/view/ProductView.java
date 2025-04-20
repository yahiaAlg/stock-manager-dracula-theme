package view;

import controller.ProductController;
import model.Category;
import model.Product;
import model.Supplier;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;

public class ProductView extends JPanel {
    
    private ProductController controller;
    
    private JTable productTable;
    private DefaultTableModel tableModel;
    
    private JTextField searchField;
    private JComboBox<Category> categoryComboBox;
    private JComboBox<Supplier> supplierComboBox;
    private JCheckBox lowStockCheckBox;
    
    private JButton searchButton;
    private JButton clearButton;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton adjustStockButton;
    
    private List<Product> currentProducts;
    
    public ProductView(ProductController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        initComponents();
        loadAllProducts();
    }
    
    private void initComponents() {
        // Search panel (top)
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Products"));
        
        JPanel searchFieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        searchField = new JTextField(20);
        searchFieldPanel.add(new JLabel("Search:"));
        searchFieldPanel.add(searchField);
        
        categoryComboBox = new JComboBox<>();
        categoryComboBox.addItem(null); // Add empty selection
        loadCategories();
        searchFieldPanel.add(new JLabel("Category:"));
        searchFieldPanel.add(categoryComboBox);
        
        supplierComboBox = new JComboBox<>();
        supplierComboBox.addItem(null); // Add empty selection
        loadSuppliers();
        searchFieldPanel.add(new JLabel("Supplier:"));
        searchFieldPanel.add(supplierComboBox);
        
        lowStockCheckBox = new JCheckBox("Low Stock Only");
        searchFieldPanel.add(lowStockCheckBox);
        
        JPanel searchButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchButton = new JButton("Search");
        clearButton = new JButton("Clear");
        
        searchButton.addActionListener(this::onSearchButtonClicked);
        clearButton.addActionListener(e -> {
            searchField.setText("");
            categoryComboBox.setSelectedItem(null);
            supplierComboBox.setSelectedItem(null);
            lowStockCheckBox.setSelected(false);
            loadAllProducts();
        });
        
        searchButtonsPanel.add(searchButton);
        searchButtonsPanel.add(clearButton);
        
        searchPanel.add(searchFieldPanel, BorderLayout.CENTER);
        searchPanel.add(searchButtonsPanel, BorderLayout.EAST);
        
        // Table panel (center)
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Products"));
        
        String[] columnNames = {"ID", "SKU", "Name", "Category", "Supplier", "Unit Price", "Stock Qty", "Reorder Level"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        productTable = new JTable(tableModel);
        productTable.setFillsViewportHeight(true);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Add double-click listener for editing
        productTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onEditButtonClicked(null);
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(productTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons panel (bottom)
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        addButton = new JButton("Add Product");
        editButton = new JButton("Edit");
        deleteButton = new JButton("Delete");
        adjustStockButton = new JButton("Adjust Stock");
        
        addButton.addActionListener(e -> showProductDialog(null));
        editButton.addActionListener(this::onEditButtonClicked);
        deleteButton.addActionListener(this::onDeleteButtonClicked);
        adjustStockButton.addActionListener(this::onAdjustStockButtonClicked);
        
        buttonsPanel.add(addButton);
        buttonsPanel.add(editButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(adjustStockButton);
        
        // Add all panels to the main view
        add(searchPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
    }
    
    void loadAllProducts() {
        currentProducts = controller.getAllProducts();
        refreshProductTable();
    }
    
    private void refreshProductTable() {
        // Clear existing data
        tableModel.setRowCount(0);
        
        // Populate table with products
        for (Product product : currentProducts) {
            Object[] rowData = {
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getCategoryName(),
                product.getSupplierName(),
                String.format("$%.2f", product.getUnitPrice()),
                product.getStockQty(),
                product.getReorderLevel()
            };
            tableModel.addRow(rowData);
        }
    }
    
    private void loadCategories() {
        List<Category> categories = controller.getAllCategories();
        for (Category category : categories) {
            categoryComboBox.addItem(category);
        }
    }
    
    private void loadSuppliers() {
        // We assume there's a method to get all suppliers
        // This would typically come from a SupplierController
        List<Supplier> suppliers = controller.getAllSuppliers();
        for (Supplier supplier : suppliers) {
            supplierComboBox.addItem(supplier);
        }
    }

    public void refreshData() {
        loadAllProducts();
    }
    private void onSearchButtonClicked(ActionEvent e) {
        String searchTerm = searchField.getText().trim();
        Category selectedCategory = (Category) categoryComboBox.getSelectedItem();
        Supplier selectedSupplier = (Supplier) supplierComboBox.getSelectedItem();
        boolean lowStockOnly = lowStockCheckBox.isSelected();
        
        Integer categoryId = selectedCategory != null ? selectedCategory.getId() : null;
        Integer supplierId = selectedSupplier != null ? selectedSupplier.getId() : null;
        
        if (searchTerm.isEmpty() && categoryId == null && supplierId == null && !lowStockOnly) {
            loadAllProducts();
        } else {
            currentProducts = controller.searchProducts(
                searchTerm.isEmpty() ? null : searchTerm,
                categoryId,
                supplierId,
                lowStockOnly ? Boolean.TRUE : null
            );
            refreshProductTable();
        }
    }
    
    private void onEditButtonClicked(ActionEvent e) {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < currentProducts.size()) {
            Product selectedProduct = currentProducts.get(selectedRow);
            showProductDialog(selectedProduct);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Please select a product to edit.", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void onDeleteButtonClicked(ActionEvent e) {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < currentProducts.size()) {
            Product selectedProduct = currentProducts.get(selectedRow);
            
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the product: " + selectedProduct.getName() + "?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = controller.deleteProduct(selectedProduct.getId());
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "Product deleted successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    loadAllProducts();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Error deleting product. It may be used in orders or inventory adjustments.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "Please select a product to delete.", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void onAdjustStockButtonClicked(ActionEvent e) {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < currentProducts.size()) {
            Product selectedProduct = currentProducts.get(selectedRow);
            showStockAdjustmentDialog(selectedProduct);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Please select a product to adjust stock.", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void showProductDialog(Product product) {
        // Create a dialog for adding/editing products
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Product Details");
        dialog.setLayout(new BorderLayout());
        dialog.setSize(450, 450);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridLayout(8, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextField idField = new JTextField();
        JTextField skuField = new JTextField();
        JTextField nameField = new JTextField();
        JComboBox<Category> categoryField = new JComboBox<>();
        JComboBox<Supplier> supplierField = new JComboBox<>();
        JTextField priceField = new JTextField();
        JTextField stockField = new JTextField();
        JTextField reorderField = new JTextField();
        
        // Load categories and suppliers
        categoryField.addItem(null);
        for (int i = 1; i < categoryComboBox.getItemCount(); i++) {
            categoryField.addItem(categoryComboBox.getItemAt(i));
        }
        
        supplierField.addItem(null);
        for (int i = 1; i < supplierComboBox.getItemCount(); i++) {
            supplierField.addItem(supplierComboBox.getItemAt(i));
        }
        
        // Populate fields if editing
        if (product != null) {
            idField.setText(String.valueOf(product.getId()));
            skuField.setText(product.getSku());
            nameField.setText(product.getName());
            
            // Set selected category
            for (int i = 0; i < categoryField.getItemCount(); i++) {
                Category cat = categoryField.getItemAt(i);
                if (cat != null && cat.getId() == product.getCategoryId()) {
                    categoryField.setSelectedIndex(i);
                    break;
                }
            }
            
            // Set selected supplier
            for (int i = 0; i < supplierField.getItemCount(); i++) {
                Supplier sup = supplierField.getItemAt(i);
                if (sup != null && sup.getId() == product.getSupplierId()) {
                    supplierField.setSelectedIndex(i);
                    break;
                }
            }
            
            priceField.setText(String.format("%.2f", product.getUnitPrice()));
            stockField.setText(String.valueOf(product.getStockQty()));
            reorderField.setText(String.valueOf(product.getReorderLevel()));
        } else {
            // Default values for new product
            idField.setText("0");
            reorderField.setText("5");
            stockField.setText("0");
            priceField.setText("0.00");
        }
        
        // ID is not editable
        idField.setEditable(false);
        
        formPanel.add(new JLabel("ID:"));
        formPanel.add(idField);
        formPanel.add(new JLabel("SKU:"));
        formPanel.add(skuField);
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Category:"));
        formPanel.add(categoryField);
        formPanel.add(new JLabel("Supplier:"));
        formPanel.add(supplierField);
        formPanel.add(new JLabel("Unit Price:"));
        formPanel.add(priceField);
        formPanel.add(new JLabel("Stock Quantity:"));
        formPanel.add(stockField);
        formPanel.add(new JLabel("Reorder Level:"));
        formPanel.add(reorderField);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            try {
                // Validate input
                if (skuField.getText().trim().isEmpty() || nameField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, 
                        "SKU and Name are required fields.", 
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                Product p = new Product();
                p.setId(Integer.parseInt(idField.getText()));
                p.setSku(skuField.getText().trim());
                p.setName(nameField.getText().trim());
                
                Category selectedCategory = (Category) categoryField.getSelectedItem();
                p.setCategoryId(selectedCategory != null ? selectedCategory.getId() : 0);
                
                Supplier selectedSupplier = (Supplier) supplierField.getSelectedItem();
                p.setSupplierId(selectedSupplier != null ? selectedSupplier.getId() : 0);
                
                p.setUnitPrice(Double.parseDouble(priceField.getText().replace(",", "")));
                p.setStockQty(Integer.parseInt(stockField.getText()));
                p.setReorderLevel(Integer.parseInt(reorderField.getText()));
                
                boolean success = controller.saveProduct(p);
                
                if (success) {
                    JOptionPane.showMessageDialog(dialog,
                        "Product saved successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadAllProducts();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                        "Error saving product. Please check your inputs.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Please enter valid numeric values for price, stock quantity, and reorder level.", 
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
    
    private void showStockAdjustmentDialog(Product product) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Adjust Stock");
        dialog.setLayout(new BorderLayout());
        dialog.setSize(350, 250);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextField productField = new JTextField(product.getName());
        productField.setEditable(false);
        
        JTextField currentStockField = new JTextField(String.valueOf(product.getStockQty()));
        currentStockField.setEditable(false);
        
        JTextField adjustmentField = new JTextField("0");
        
        JRadioButton addRadio = new JRadioButton("Add to Stock");
        JRadioButton removeRadio = new JRadioButton("Remove from Stock");
        ButtonGroup group = new ButtonGroup();
        group.add(addRadio);
        group.add(removeRadio);
        addRadio.setSelected(true);
        
        JPanel radioPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        radioPanel.add(addRadio);
        radioPanel.add(removeRadio);
        
        JTextField reasonField = new JTextField();
        
        formPanel.add(new JLabel("Product:"));
        formPanel.add(productField);
        formPanel.add(new JLabel("Current Stock:"));
        formPanel.add(currentStockField);
        formPanel.add(new JLabel("Adjustment Type:"));
        formPanel.add(radioPanel);
        formPanel.add(new JLabel("Quantity:"));
        formPanel.add(adjustmentField);
        formPanel.add(new JLabel("Reason:"));
        formPanel.add(reasonField);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            try {
                int quantity = Integer.parseInt(adjustmentField.getText());
                if (quantity <= 0) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Please enter a positive quantity.", 
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Convert to positive or negative based on selection
                int adjustmentValue = addRadio.isSelected() ? quantity : -quantity;
                String reason = reasonField.getText().trim();
                
                if (reason.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Please enter a reason for the adjustment.", 
                        "Input Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                boolean success = controller.adjustInventory(product.getId(), adjustmentValue, reason);
                
                if (success) {
                    JOptionPane.showMessageDialog(dialog,
                        "Stock adjusted successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadAllProducts();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                        "Error adjusting stock. Please try again.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Please enter a valid number for quantity.", 
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
}