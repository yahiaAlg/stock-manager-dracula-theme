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
import java.util.Locale;
import java.util.ResourceBundle;
import util.ArabicFontHelper;

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
    private ResourceBundle messages;
    private boolean isRightToLeft;
    
    public ProductView(ProductController controller) {
        this.controller = controller;
        
        // Load localization resources
        loadLocalization();
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        initComponents();
        loadAllProducts();
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
        searchPanel.setBorder(BorderFactory.createTitledBorder(messages.getString("products.searchTitle")));
        
        JPanel searchFieldPanel = new JPanel(new FlowLayout(isRightToLeft ? FlowLayout.RIGHT : FlowLayout.LEFT));
        
        searchField = new JTextField(20);
        if (isRightToLeft) {
            searchField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        
        searchFieldPanel.add(new JLabel(messages.getString("common.search") + ":"));
        searchFieldPanel.add(searchField);
        
        categoryComboBox = new JComboBox<>();
        categoryComboBox.addItem(null); // Add empty selection
        loadCategories();
        searchFieldPanel.add(new JLabel(messages.getString("products.column.category") + ":"));
        searchFieldPanel.add(categoryComboBox);
        
        supplierComboBox = new JComboBox<>();
        supplierComboBox.addItem(null); // Add empty selection
        loadSuppliers();
        searchFieldPanel.add(new JLabel(messages.getString("products.column.supplier") + ":"));
        searchFieldPanel.add(supplierComboBox);
        
        lowStockCheckBox = new JCheckBox(messages.getString("products.filter.lowStock"));
        searchFieldPanel.add(lowStockCheckBox);
        
        JPanel searchButtonsPanel = new JPanel(new FlowLayout(isRightToLeft ? FlowLayout.LEFT : FlowLayout.RIGHT));
        searchButton = new JButton(messages.getString("button.search"));
        clearButton = new JButton(messages.getString("button.clear"));
        
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
        tablePanel.setBorder(BorderFactory.createTitledBorder(messages.getString("products.title")));
        
        String[] columnNames = {
            messages.getString("products.column.id"),
            messages.getString("products.column.sku"),
            messages.getString("products.column.name"),
            messages.getString("products.column.category"),
            messages.getString("products.column.supplier"),
            messages.getString("products.column.price"),
            messages.getString("products.column.stock"),
            messages.getString("products.column.reorder")
        };
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        productTable = new JTable(tableModel);
        productTable.setFillsViewportHeight(true);
        productTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Apply RTL to table if needed
        if (isRightToLeft) {
            productTable.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            productTable.getTableHeader().setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        
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
        JPanel buttonsPanel = new JPanel(new FlowLayout(isRightToLeft ? FlowLayout.LEFT : FlowLayout.RIGHT));
        
        addButton = new JButton(messages.getString("products.button.add"));
        editButton = new JButton(messages.getString("button.edit"));
        deleteButton = new JButton(messages.getString("button.delete"));
        adjustStockButton = new JButton(messages.getString("products.button.adjustStock"));
        
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
                String.format("DZD %.2f", product.getUnitPrice()),
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
                messages.getString("products.error.selectToEdit"), 
                messages.getString("dialog.noSelection"), 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void onDeleteButtonClicked(ActionEvent e) {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < currentProducts.size()) {
            Product selectedProduct = currentProducts.get(selectedRow);
            
            int confirm = JOptionPane.showConfirmDialog(this,
                messages.getString("products.confirm.delete").replace("{0}", selectedProduct.getName()),
                messages.getString("dialog.confirmDeletion"),
                JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = controller.deleteProduct(selectedProduct.getId());
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        messages.getString("products.success.deleted"),
                        messages.getString("dialog.success"),
                        JOptionPane.INFORMATION_MESSAGE);
                    loadAllProducts();
                } else {
                    JOptionPane.showMessageDialog(this,
                        messages.getString("products.error.delete"),
                        messages.getString("dialog.error"),
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                messages.getString("products.error.selectToDelete"), 
                messages.getString("dialog.noSelection"), 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void onAdjustStockButtonClicked(ActionEvent e) {
        int selectedRow = productTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < currentProducts.size()) {
            Product selectedProduct = currentProducts.get(selectedRow);
            showStockAdjustmentDialog(selectedProduct);
        } else {
            JOptionPane.showMessageDialog(this, 
                messages.getString("products.error.selectToAdjust"), 
                messages.getString("dialog.noSelection"), 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void showProductDialog(Product product) {
        // Create a dialog for adding/editing products
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), 
            messages.getString("products.dialog.details"));
        dialog.setLayout(new BorderLayout());
        dialog.setSize(450, 450);
        dialog.setLocationRelativeTo(this);
        
        // Apply RTL orientation if needed
        if (isRightToLeft) {
            dialog.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            ArabicFontHelper.applyArabicFont(dialog);
        }
        
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
        
        // Apply RTL to text components if needed
        if (isRightToLeft) {
            idField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            skuField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            nameField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            priceField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            stockField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            reorderField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        
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
        
        formPanel.add(new JLabel(messages.getString("products.column.id") + ":"));
        formPanel.add(idField);
        formPanel.add(new JLabel(messages.getString("products.column.sku") + ":"));
        formPanel.add(skuField);
        formPanel.add(new JLabel(messages.getString("products.column.name") + ":"));
        formPanel.add(nameField);
        formPanel.add(new JLabel(messages.getString("products.column.category") + ":"));
        formPanel.add(categoryField);
        formPanel.add(new JLabel(messages.getString("products.column.supplier") + ":"));
        formPanel.add(supplierField);
        formPanel.add(new JLabel(messages.getString("products.column.price") + ":"));
        formPanel.add(priceField);
        formPanel.add(new JLabel(messages.getString("products.column.stock") + ":"));
        formPanel.add(stockField);
        formPanel.add(new JLabel(messages.getString("products.column.reorder") + ":"));
        formPanel.add(reorderField);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(isRightToLeft ? FlowLayout.LEFT : FlowLayout.RIGHT));
        JButton saveButton = new JButton(messages.getString("button.save"));
        JButton cancelButton = new JButton(messages.getString("button.cancel"));
        
        saveButton.addActionListener(e -> {
            try {
                // Validate input
                if (skuField.getText().trim().isEmpty() || nameField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, 
                        messages.getString("products.error.requiredFields"), 
                        messages.getString("dialog.validationError"), 
                        JOptionPane.ERROR_MESSAGE);
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
                        messages.getString("products.success.saved"),
                        messages.getString("dialog.success"),
                        JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadAllProducts();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                        messages.getString("products.error.save"),
                        messages.getString("dialog.error"),
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, 
                    messages.getString("products.error.numericValues"), 
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
    
    private void showStockAdjustmentDialog(Product product) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), 
            messages.getString("products.dialog.adjustStock"));
        dialog.setLayout(new BorderLayout());
        dialog.setSize(550, 250);
        dialog.setLocationRelativeTo(this);
        
        // Apply RTL orientation if needed
        if (isRightToLeft) {
            dialog.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            ArabicFontHelper.applyArabicFont(dialog);
        }
        
        JPanel formPanel = new JPanel(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextField productField = new JTextField(product.getName());
        productField.setEditable(false);
        
        JTextField currentStockField = new JTextField(String.valueOf(product.getStockQty()));
        currentStockField.setEditable(false);
        
        JTextField adjustmentField = new JTextField("0");
        
        // Apply RTL to text components if needed
        if (isRightToLeft) {
            productField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            currentStockField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            adjustmentField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        
        JRadioButton addRadio = new JRadioButton(messages.getString("products.radio.add"));
        JRadioButton removeRadio = new JRadioButton(messages.getString("products.radio.remove"));
        ButtonGroup group = new ButtonGroup();
        group.add(addRadio);
        group.add(removeRadio);
        addRadio.setSelected(true);
        
        JPanel radioPanel = new JPanel(new FlowLayout(isRightToLeft ? FlowLayout.RIGHT : FlowLayout.LEFT));
        radioPanel.add(addRadio);
        radioPanel.add(removeRadio);
        
        JTextField reasonField = new JTextField();
        if (isRightToLeft) {
            reasonField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        
        formPanel.add(new JLabel(messages.getString("products.label.product") + ":"));
        formPanel.add(productField);
        formPanel.add(new JLabel(messages.getString("products.label.currentStock") + ":"));
        formPanel.add(currentStockField);
        formPanel.add(new JLabel(messages.getString("products.label.adjustmentType") + ":"));
        formPanel.add(radioPanel);
        formPanel.add(new JLabel(messages.getString("products.label.quantity") + ":"));
        formPanel.add(adjustmentField);
        formPanel.add(new JLabel(messages.getString("products.label.reason") + ":"));
        formPanel.add(reasonField);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(isRightToLeft ? FlowLayout.LEFT : FlowLayout.RIGHT));
        JButton saveButton = new JButton(messages.getString("button.save"));
        JButton cancelButton = new JButton(messages.getString("button.cancel"));
        
        saveButton.addActionListener(e -> {
            try {
                int quantity = Integer.parseInt(adjustmentField.getText());
                if (quantity <= 0) {
                    JOptionPane.showMessageDialog(dialog, 
                        messages.getString("products.error.positiveQuantity"), 
                        messages.getString("dialog.inputError"), 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Convert to positive or negative based on selection
                int adjustmentValue = addRadio.isSelected() ? quantity : -quantity;
                String reason = reasonField.getText().trim();
                
                if (reason.isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, 
                        messages.getString("products.error.reasonRequired"), 
                        messages.getString("dialog.inputError"), 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                boolean success = controller.adjustInventory(product.getId(), adjustmentValue, reason);
                
                if (success) {
                    JOptionPane.showMessageDialog(dialog,
                        messages.getString("products.success.adjusted"),
                        messages.getString("dialog.success"),
                        JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadAllProducts();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                        messages.getString("products.error.adjust"),
                        messages.getString("dialog.error"),
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, 
                    messages.getString("products.error.numericValues"), 
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
}