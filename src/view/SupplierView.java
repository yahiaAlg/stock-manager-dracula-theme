package view;

import controller.SupplierController;
import model.Supplier;
import model.Product;

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

public class SupplierView extends JPanel {
    
    private SupplierController controller;
    
    private JTable supplierTable;
    private DefaultTableModel tableModel;
    
    private JTextField searchField;
    private JButton searchButton;
    private JButton clearButton;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    private JButton viewProductsButton;
    
    private List<Supplier> currentSuppliers;
    private ResourceBundle messages;
    private boolean isRightToLeft;
    
    public SupplierView(SupplierController controller) {
        this.controller = controller;
        
        // Load localization resources
        loadLocalization();
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        initComponents();
        loadAllSuppliers();
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
        searchPanel.setBorder(BorderFactory.createTitledBorder(messages.getString("suppliers.searchTitle")));
        
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
            loadAllSuppliers();
        });
        
        searchButtonsPanel.add(searchButton);
        searchButtonsPanel.add(clearButton);
        
        searchPanel.add(searchFieldPanel, BorderLayout.CENTER);
        searchPanel.add(searchButtonsPanel, BorderLayout.EAST);
        
        // Table panel (center)
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder(messages.getString("suppliers.title")));
        
        String[] columnNames = {
            messages.getString("column.id"), 
            messages.getString("suppliers.column.name"), 
            messages.getString("suppliers.column.contact"),
            messages.getString("suppliers.column.address")
        };
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        supplierTable = new JTable(tableModel);
        supplierTable.setFillsViewportHeight(true);
        supplierTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Apply RTL to table if needed
        if (isRightToLeft) {
            supplierTable.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            supplierTable.getTableHeader().setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        
        // Add double-click listener for editing
        supplierTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onEditButtonClicked(null);
                }
            }
        });
        
        JScrollPane scrollPane = new JScrollPane(supplierTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons panel (bottom)
        JPanel buttonsPanel = new JPanel(new FlowLayout(isRightToLeft ? FlowLayout.LEFT : FlowLayout.RIGHT));
        
        addButton = new JButton(messages.getString("suppliers.button.add"));
        editButton = new JButton(messages.getString("button.edit"));
        deleteButton = new JButton(messages.getString("button.delete"));
        viewProductsButton = new JButton(messages.getString("suppliers.button.viewProducts"));
        
        addButton.addActionListener(e -> showSupplierDialog(null));
        editButton.addActionListener(this::onEditButtonClicked);
        deleteButton.addActionListener(this::onDeleteButtonClicked);
        viewProductsButton.addActionListener(this::onViewProductsButtonClicked);
        
        buttonsPanel.add(addButton);
        buttonsPanel.add(editButton);
        buttonsPanel.add(deleteButton);
        buttonsPanel.add(viewProductsButton);
        
        // Add all panels to the main view
        add(searchPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
    }
    
    private void loadAllSuppliers() {
        currentSuppliers = controller.getAllSuppliers();
        refreshSupplierTable();
    }
    
    private void refreshSupplierTable() {
        // Clear existing data
        tableModel.setRowCount(0);
        
        // Populate table with suppliers
        for (Supplier supplier : currentSuppliers) {
            Object[] rowData = {
                supplier.getId(),
                supplier.getName(),
                supplier.getContact(),
                supplier.getAddress()
            };
            tableModel.addRow(rowData);
        }
    }

    public void refreshData() {
        loadAllSuppliers();
    }
    
    private void onSearchButtonClicked(ActionEvent e) {
        String searchTerm = searchField.getText().trim();
        
        if (searchTerm.isEmpty()) {
            loadAllSuppliers();
        } else {
            currentSuppliers = controller.searchSuppliers(searchTerm);
            refreshSupplierTable();
        }
    }
    
    private void onEditButtonClicked(ActionEvent e) {
        int selectedRow = supplierTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < currentSuppliers.size()) {
            Supplier selectedSupplier = currentSuppliers.get(selectedRow);
            showSupplierDialog(selectedSupplier);
        } else {
            JOptionPane.showMessageDialog(this, 
                messages.getString("error.noSelection"),
                messages.getString("dialog.noSelection"), 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void onDeleteButtonClicked(ActionEvent e) {
        int selectedRow = supplierTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < currentSuppliers.size()) {
            Supplier selectedSupplier = currentSuppliers.get(selectedRow);
            
            int confirm = JOptionPane.showConfirmDialog(this,
                messages.getString("suppliers.confirm.delete").replace("{0}", selectedSupplier.getName()),
                messages.getString("dialog.confirmDeletion"),
                JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = controller.deleteSupplier(selectedSupplier.getId());
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        messages.getString("suppliers.success.deleted"),
                        messages.getString("dialog.success"),
                        JOptionPane.INFORMATION_MESSAGE);
                    loadAllSuppliers();
                } else {
                    JOptionPane.showMessageDialog(this,
                        messages.getString("suppliers.error.delete"),
                        messages.getString("dialog.error"),
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                messages.getString("suppliers.error.selectToDelete"), 
                messages.getString("dialog.noSelection"), 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void onViewProductsButtonClicked(ActionEvent e) {
        int selectedRow = supplierTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < currentSuppliers.size()) {
            Supplier selectedSupplier = currentSuppliers.get(selectedRow);
            showProductsDialog(selectedSupplier);
        } else {
            JOptionPane.showMessageDialog(this, 
                messages.getString("suppliers.error.selectToViewProducts"), 
                messages.getString("dialog.noSelection"), 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void showSupplierDialog(Supplier supplier) {
        // Create a dialog for adding/editing suppliers
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), 
            messages.getString("dialog.supplierDetails"));
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        // Apply RTL orientation if needed
        if (isRightToLeft) {
            dialog.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            ArabicFontHelper.applyArabicFont(dialog);
        }
        
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField contactField = new JTextField();
        JTextArea addressField = new JTextArea();
        addressField.setLineWrap(true);
        addressField.setWrapStyleWord(true);
        
        // Apply RTL to text components if needed
        if (isRightToLeft) {
            idField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            nameField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            contactField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            addressField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        
        // Populate fields if editing
        if (supplier != null) {
            idField.setText(String.valueOf(supplier.getId()));
            nameField.setText(supplier.getName());
            contactField.setText(supplier.getContact());
            addressField.setText(supplier.getAddress());
        } else {
            // Default values for new supplier
            idField.setText("0");
        }
        
        // ID is not editable
        idField.setEditable(false);
        
        formPanel.add(new JLabel(messages.getString("column.id") + ":"));
        formPanel.add(idField);
        formPanel.add(new JLabel(messages.getString("suppliers.column.name") + ":"));
        formPanel.add(nameField);
        formPanel.add(new JLabel(messages.getString("suppliers.column.contact") + ":"));
        formPanel.add(contactField);
        formPanel.add(new JLabel(messages.getString("suppliers.column.address") + ":"));
        formPanel.add(new JScrollPane(addressField)); // Use scroll pane for text area
        
        JPanel buttonPanel = new JPanel(new FlowLayout(isRightToLeft ? FlowLayout.LEFT : FlowLayout.RIGHT));
        JButton saveButton = new JButton(messages.getString("button.save"));
        JButton cancelButton = new JButton(messages.getString("button.cancel"));
        
        saveButton.addActionListener(e -> {
            try {
                // Validate input
                if (nameField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, 
                        messages.getString("suppliers.error.nameRequired"), 
                        messages.getString("dialog.validationError"), 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                Supplier s = new Supplier();
                s.setId(Integer.parseInt(idField.getText()));
                s.setName(nameField.getText().trim());
                s.setContact(contactField.getText().trim());
                s.setAddress(addressField.getText().trim());
                
                boolean success = controller.saveSupplier(s);
                
                if (success) {
                    JOptionPane.showMessageDialog(dialog,
                        messages.getString("suppliers.success.saved"),
                        messages.getString("dialog.success"),
                        JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadAllSuppliers();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                        messages.getString("suppliers.error.save"),
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
    
    private void showProductsDialog(Supplier supplier) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), 
            messages.getString("suppliers.dialog.productsFrom") + " " + supplier.getName());
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
        
        JLabel titleLabel = new JLabel(messages.getString("suppliers.label.productsSuppliedBy") + " " + supplier.getName());
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 14));
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel infoPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        infoPanel.add(new JLabel(messages.getString("suppliers.column.contact") + ": " + supplier.getContact()));
        infoPanel.add(new JLabel(messages.getString("suppliers.column.address") + ": " + supplier.getAddress()));
        headerPanel.add(infoPanel, BorderLayout.SOUTH);
        
        // Table for products
        String[] columnNames = {
            messages.getString("column.id"), 
            messages.getString("products.column.sku"), 
            messages.getString("products.column.name"),
            messages.getString("products.column.category"),
            messages.getString("products.column.price"),
            messages.getString("products.column.stockQty"),
            messages.getString("products.column.reorderLevel")
        };
        
        DefaultTableModel productTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable productTable = new JTable(productTableModel);
        productTable.setFillsViewportHeight(true);
        
        // Apply RTL to table if needed
        if (isRightToLeft) {
            productTable.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            productTable.getTableHeader().setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        
        // Get products for this supplier
        List<Product> products = controller.getProductsBySupplier(supplier.getId());
        
        for (Product product : products) {
            Object[] rowData = {
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getCategoryName(),
                String.format("DZD %.2f", product.getUnitPrice()),
                product.getStockQty(),
                product.getReorderLevel()
            };
            productTableModel.addRow(rowData);
        }
        
        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel footerPanel = new JPanel(new FlowLayout(isRightToLeft ? FlowLayout.LEFT : FlowLayout.RIGHT));
        JButton closeButton = new JButton(messages.getString("button.close"));
        closeButton.addActionListener(e -> dialog.dispose());
        footerPanel.add(closeButton);
        
        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(footerPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
}