package view;

import controller.InventoryAdjustmentController;
import model.InventoryAdjustment;
import model.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class InventoryAdjustmentView extends JPanel {
    
    private InventoryAdjustmentController controller;
    
    // Table components
    private JTable adjustmentTable;
    private DefaultTableModel tableModel;
    
    // Search components
    private JComboBox<Product> productComboBox;
    private JTextField startDateField;
    private JTextField endDateField;
    private JTextField reasonField;
    private JTextField minQtyField;
    private JTextField maxQtyField;
    
    // Action buttons
    private JButton searchButton;
    private JButton clearButton;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    
    private List<InventoryAdjustment> currentAdjustments;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    
    public InventoryAdjustmentView(InventoryAdjustmentController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        initComponents();
        loadAllAdjustments();
    }
    
    private void initComponents() {
        // Search panel
        JPanel searchPanel = createSearchPanel();
        
        // Table panel
        JPanel tablePanel = createTablePanel();
        
        // Buttons panel
        JPanel buttonsPanel = createButtonsPanel();
        
        // Add all panels to main view
        add(searchPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
    }
    
    private JPanel createSearchPanel() {
        JPanel searchPanel = new JPanel(new BorderLayout(5, 10));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Inventory Adjustments"));
        
        // Product filter
        JPanel productPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        productPanel.add(new JLabel("Product:"));
        productComboBox = new JComboBox<>();
        productComboBox.setPreferredSize(new Dimension(200, 25));
        
        // Add "All Products" option
        productComboBox.addItem(new Product(0, "All Products", "ALL", 0, 0, 0.0, 0, 0));
        // Load products from database
        List<Product> products = controller.getAllProducts();
        for (Product product : products) {
            productComboBox.addItem(product);
        }
        
        productPanel.add(productComboBox);
        
        // Date range filter
        JPanel datePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        datePanel.add(new JLabel("Date Range:"));
        startDateField = new JTextField(10);
        startDateField.setToolTipText("YYYY-MM-DD");
        datePanel.add(startDateField);
        datePanel.add(new JLabel("to"));
        endDateField = new JTextField(10);
        endDateField.setToolTipText("YYYY-MM-DD");
        datePanel.add(endDateField);
        
        // Reason filter
        JPanel reasonPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        reasonPanel.add(new JLabel("Reason:"));
        reasonField = new JTextField(20);
        reasonPanel.add(reasonField);
        
        // Quantity range filter
        JPanel qtyPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        qtyPanel.add(new JLabel("Quantity Range:"));
        minQtyField = new JTextField(5);
        qtyPanel.add(minQtyField);
        qtyPanel.add(new JLabel("to"));
        maxQtyField = new JTextField(5);
        qtyPanel.add(maxQtyField);
        
        // Search and clear buttons
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchButton = new JButton("Search");
        clearButton = new JButton("Clear");
        
        searchButton.addActionListener(this::onSearchButtonClicked);
        clearButton.addActionListener(e -> {
            productComboBox.setSelectedIndex(0);
            startDateField.setText("");
            endDateField.setText("");
            reasonField.setText("");
            minQtyField.setText("");
            maxQtyField.setText("");
            loadAllAdjustments();
        });
        
        buttonsPanel.add(searchButton);
        buttonsPanel.add(clearButton);
        
        // Arrange search components
        JPanel filtersPanel = new JPanel(new GridLayout(3, 2, 5, 5));
        filtersPanel.add(productPanel);
        filtersPanel.add(datePanel);
        filtersPanel.add(reasonPanel);
        filtersPanel.add(qtyPanel);
        
        searchPanel.add(filtersPanel, BorderLayout.CENTER);
        searchPanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        return searchPanel;
    }
    
    private JPanel createTablePanel() {
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder("Adjustment History"));
        
        String[] columnNames = {"ID", "Product", "Date", "Quantity Change", "Reason"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
            
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 3) return Integer.class; // Quantity column
                return Object.class;
            }
        };
        
        adjustmentTable = new JTable(tableModel);
        adjustmentTable.setFillsViewportHeight(true);
        adjustmentTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Double-click to edit
        adjustmentTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onEditButtonClicked(null);
                }
            }
        });
        
        // Set column widths
        adjustmentTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        adjustmentTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        adjustmentTable.getColumnModel().getColumn(2).setPreferredWidth(100);
        adjustmentTable.getColumnModel().getColumn(3).setPreferredWidth(100);
        adjustmentTable.getColumnModel().getColumn(4).setPreferredWidth(250);
        
        // Custom renderer for quantity changes (positive in green, negative in red)
        DefaultTableCellRenderer quantityRenderer = new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value, 
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(
                        table, value, isSelected, hasFocus, row, column);
                
                if (value != null) {
                    int qty = (Integer) value;
                    if (qty > 0) {
                        c.setForeground(new Color(0, 128, 0)); // Green
                        setText("+" + qty);
                    } else if (qty < 0) {
                        c.setForeground(Color.RED);
                    } else {
                        c.setForeground(Color.BLACK);
                    }
                }
                
                return c;
            }
        };
        quantityRenderer.setHorizontalAlignment(SwingConstants.RIGHT);
        adjustmentTable.getColumnModel().getColumn(3).setCellRenderer(quantityRenderer);
        
        JScrollPane scrollPane = new JScrollPane(adjustmentTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        return tablePanel;
    }
    
    private JPanel createButtonsPanel() {
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        addButton = new JButton("Add Adjustment");
        editButton = new JButton("Edit");
        deleteButton = new JButton("Delete");
        
        addButton.addActionListener(e -> showAdjustmentDialog(null));
        editButton.addActionListener(this::onEditButtonClicked);
        deleteButton.addActionListener(this::onDeleteButtonClicked);
        
        buttonsPanel.add(addButton);
        buttonsPanel.add(editButton);
        buttonsPanel.add(deleteButton);
        
        return buttonsPanel;
    }
    
    public void loadAllAdjustments() {
        currentAdjustments = controller.getAllAdjustments();
        refreshAdjustmentTable();
    }
    
    private void refreshAdjustmentTable() {
        // Clear existing data
        tableModel.setRowCount(0);
        
        // Populate table with adjustments
        for (InventoryAdjustment adjustment : currentAdjustments) {
            Object[] rowData = {
                adjustment.getId(),
                adjustment.getProductName(),
                dateFormat.format(adjustment.getDate()),
                adjustment.getChangeQty(),
                adjustment.getReason()
            };
            tableModel.addRow(rowData);
        }
    }
    
    private void onSearchButtonClicked(ActionEvent e) {
        try {
            // Get selected product
            Product selectedProduct = (Product) productComboBox.getSelectedItem();
            Integer productId = selectedProduct.getId() > 0 ? selectedProduct.getId() : null;
            
            // Parse date ranges
            Date startDate = null;
            if (!startDateField.getText().trim().isEmpty()) {
                startDate = dateFormat.parse(startDateField.getText().trim());
            }
            
            Date endDate = null;
            if (!endDateField.getText().trim().isEmpty()) {
                endDate = dateFormat.parse(endDateField.getText().trim());
            }
            
            // Get reason filter
            String reason = reasonField.getText().trim().isEmpty() ? null : reasonField.getText().trim();
            
            // Parse quantity ranges
            Integer minQty = null;
            if (!minQtyField.getText().trim().isEmpty()) {
                minQty = Integer.parseInt(minQtyField.getText().trim());
            }
            
            Integer maxQty = null;
            if (!maxQtyField.getText().trim().isEmpty()) {
                maxQty = Integer.parseInt(maxQtyField.getText().trim());
            }
            
            // Perform search
            currentAdjustments = controller.searchAdjustments(
                productId, startDate, endDate, reason, minQty, maxQty);
            refreshAdjustmentTable();
            
        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(this,
                "Invalid date format. Please use YYYY-MM-DD format.",
                "Input Error",
                JOptionPane.ERROR_MESSAGE);
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Invalid number format for quantity filters.",
                "Input Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void onEditButtonClicked(ActionEvent e) {
        int selectedRow = adjustmentTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < currentAdjustments.size()) {
            InventoryAdjustment selectedAdjustment = currentAdjustments.get(selectedRow);
            showAdjustmentDialog(selectedAdjustment);
        } else {
            JOptionPane.showMessageDialog(this, 
                "Please select an adjustment to edit.", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void onDeleteButtonClicked(ActionEvent e) {
        int selectedRow = adjustmentTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < currentAdjustments.size()) {
            InventoryAdjustment selectedAdjustment = currentAdjustments.get(selectedRow);
            
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete this adjustment?\n" +
                "This will revert the inventory change of " + selectedAdjustment.getChangeQty() + 
                " units for " + selectedAdjustment.getProductName() + ".",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = controller.deleteAdjustment(selectedAdjustment.getId());
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "Adjustment deleted successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    loadAllAdjustments();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Error deleting adjustment.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "Please select an adjustment to delete.", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void showAdjustmentDialog(InventoryAdjustment adjustment) {
        // Create dialog
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Adjustment Details");
        dialog.setLayout(new BorderLayout());
        dialog.setSize(450, 300);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Form fields
        JTextField idField = new JTextField();
        JComboBox<Product> productField = new JComboBox<>();
        JTextField dateField = new JTextField();
        JSpinner quantityField = new JSpinner(new SpinnerNumberModel(0, -1000, 1000, 1));
        JTextArea reasonField = new JTextArea();
        
        // ID is not editable
        idField.setEditable(false);
        
        // Load products for dropdown
        List<Product> products = controller.getAllProducts();
        for (Product product : products) {
            productField.addItem(product);
        }
        
        // Set current date for new adjustments
        if (adjustment == null) {
            idField.setText("0");
            dateField.setText(dateFormat.format(new Date()));
        } else {
            // Populate fields for editing
            idField.setText(String.valueOf(adjustment.getId()));
            
            // Find and select the product
            for (int i = 0; i < productField.getItemCount(); i++) {
                Product product = productField.getItemAt(i);
                if (product.getId() == adjustment.getProductId()) {
                    productField.setSelectedIndex(i);
                    break;
                }
            }
            
            dateField.setText(dateFormat.format(adjustment.getDate()));
            quantityField.setValue(adjustment.getChangeQty());
            reasonField.setText(adjustment.getReason());
            
            // Disable product change for existing adjustments
            productField.setEnabled(false);
        }
        
        // Add fields to form
        formPanel.add(new JLabel("ID:"));
        formPanel.add(idField);
        formPanel.add(new JLabel("Product:"));
        formPanel.add(productField);
        formPanel.add(new JLabel("Date (YYYY-MM-DD):"));
        formPanel.add(dateField);
        formPanel.add(new JLabel("Quantity Change:"));
        formPanel.add(quantityField);
        formPanel.add(new JLabel("Reason:"));
        
        // Use scroll pane for reason text area
        JScrollPane reasonScrollPane = new JScrollPane(reasonField);
        reasonField.setLineWrap(true);
        reasonField.setWrapStyleWord(true);
        formPanel.add(reasonScrollPane);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            try {
                // Validate input
                if (reasonField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Reason is required.", 
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Parse date
                Date date = dateFormat.parse(dateField.getText().trim());
                
                // Create or update adjustment
                InventoryAdjustment adj = new InventoryAdjustment();
                adj.setId(Integer.parseInt(idField.getText()));
                
                Product selectedProduct = (Product) productField.getSelectedItem();
                adj.setProductId(selectedProduct.getId());
                
                adj.setDate(date);
                adj.setChangeQty((Integer) quantityField.getValue());
                adj.setReason(reasonField.getText().trim());
                
                // Save adjustment
                boolean success = controller.saveAdjustment(adj);
                
                if (success) {
                    JOptionPane.showMessageDialog(dialog,
                        "Adjustment saved successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadAllAdjustments();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                        "Error saving adjustment.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Invalid date format. Please use YYYY-MM-DD format.", 
                    "Input Error", JOptionPane.ERROR_MESSAGE);
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Please enter valid numeric values.", 
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
    
    public void refreshData() {
        loadAllAdjustments();
    }
}
