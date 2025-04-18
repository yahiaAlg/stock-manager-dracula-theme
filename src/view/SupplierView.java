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
    
    public SupplierView(SupplierController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        initComponents();
        loadAllSuppliers();
    }
    
    private void initComponents() {
        // Search panel (top)
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Suppliers"));
        
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
            loadAllSuppliers();
        });
        
        searchButtonsPanel.add(searchButton);
        searchButtonsPanel.add(clearButton);
        
        searchPanel.add(searchFieldPanel, BorderLayout.CENTER);
        searchPanel.add(searchButtonsPanel, BorderLayout.EAST);
        
        // Table panel (center)
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Suppliers"));
        
        String[] columnNames = {"ID", "Name", "Contact", "Address"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        supplierTable = new JTable(tableModel);
        supplierTable.setFillsViewportHeight(true);
        supplierTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
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
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        addButton = new JButton("Add Supplier");
        editButton = new JButton("Edit");
        deleteButton = new JButton("Delete");
        viewProductsButton = new JButton("View Products");
        
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
                "Please select a supplier to edit.", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void onDeleteButtonClicked(ActionEvent e) {
        int selectedRow = supplierTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < currentSuppliers.size()) {
            Supplier selectedSupplier = currentSuppliers.get(selectedRow);
            
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the supplier: " + selectedSupplier.getName() + "?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = controller.deleteSupplier(selectedSupplier.getId());
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "Supplier deleted successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    loadAllSuppliers();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Error deleting supplier. It may have associated products.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "Please select a supplier to delete.", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void onViewProductsButtonClicked(ActionEvent e) {
        int selectedRow = supplierTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < currentSuppliers.size()) {
            Supplier selectedSupplier = currentSuppliers.get(selectedRow);
            showProductsDialog(selectedSupplier);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Please select a supplier to view products.", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void showSupplierDialog(Supplier supplier) {
        // Create a dialog for adding/editing suppliers
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Supplier Details");
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 300);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridLayout(4, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextField contactField = new JTextField();
        JTextArea addressField = new JTextArea();
        addressField.setLineWrap(true);
        addressField.setWrapStyleWord(true);
        
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
        
        formPanel.add(new JLabel("ID:"));
        formPanel.add(idField);
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Contact:"));
        formPanel.add(contactField);
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
                
                Supplier s = new Supplier();
                s.setId(Integer.parseInt(idField.getText()));
                s.setName(nameField.getText().trim());
                s.setContact(contactField.getText().trim());
                s.setAddress(addressField.getText().trim());
                
                boolean success = controller.saveSupplier(s);
                
                if (success) {
                    JOptionPane.showMessageDialog(dialog,
                        "Supplier saved successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadAllSuppliers();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                        "Error saving supplier. Please check your inputs.",
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
    
    private void showProductsDialog(Supplier supplier) {
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), 
            "Products from " + supplier.getName());
        dialog.setLayout(new BorderLayout());
        dialog.setSize(800, 500);
        dialog.setLocationRelativeTo(this);
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JLabel titleLabel = new JLabel("Products supplied by: " + supplier.getName());
        titleLabel.setFont(new Font(titleLabel.getFont().getName(), Font.BOLD, 14));
        headerPanel.add(titleLabel, BorderLayout.NORTH);
        
        JPanel infoPanel = new JPanel(new GridLayout(1, 2, 10, 0));
        infoPanel.add(new JLabel("Contact: " + supplier.getContact()));
        infoPanel.add(new JLabel("Address: " + supplier.getAddress()));
        headerPanel.add(infoPanel, BorderLayout.SOUTH);
        
        // Table for products
        String[] columnNames = {"ID", "SKU", "Name", "Category", "Unit Price", "Stock Qty", "Reorder Level"};
        DefaultTableModel productTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        JTable productTable = new JTable(productTableModel);
        productTable.setFillsViewportHeight(true);
        
        // Get products for this supplier
        List<Product> products = controller.getProductsBySupplier(supplier.getId());
        
        for (Product product : products) {
            Object[] rowData = {
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getCategoryName(),
                String.format("$%.2f", product.getUnitPrice()),
                product.getStockQty(),
                product.getReorderLevel()
            };
            productTableModel.addRow(rowData);
        }
        
        JScrollPane scrollPane = new JScrollPane(productTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JPanel footerPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close");
        closeButton.addActionListener(e -> dialog.dispose());
        footerPanel.add(closeButton);
        
        dialog.add(headerPanel, BorderLayout.NORTH);
        dialog.add(scrollPane, BorderLayout.CENTER);
        dialog.add(footerPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
}