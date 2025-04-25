package view;

import controller.InventoryAdjustmentController;
import model.InventoryAdjustment;
import model.Product;
import com.toedter.calendar.JDateChooser;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.DefaultTableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import util.ArabicFontHelper;

public class InventoryAdjustmentView extends JPanel {
    
    private InventoryAdjustmentController controller;
    
    // Table components
    private JTable adjustmentTable;
    private DefaultTableModel tableModel;
    
    // Search components
    private JComboBox<Product> productComboBox;
    private JDateChooser startDateChooser;
    private JDateChooser endDateChooser;
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
    private ResourceBundle messages;
    private boolean isRightToLeft;
    
    public InventoryAdjustmentView(InventoryAdjustmentController controller) {
        this.controller = controller;
        
        // Load localization resources
        loadLocalization();
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        initComponents();
        loadAllAdjustments();
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
        searchPanel.setBorder(BorderFactory.createTitledBorder(messages.getString("adjustments.searchTitle")));
        
        // Product filter
        JPanel productPanel = new JPanel(new FlowLayout(isRightToLeft ? FlowLayout.RIGHT : FlowLayout.LEFT));
        productPanel.add(new JLabel(messages.getString("adjustments.filter.product") + ":"));
        productComboBox = new JComboBox<>();
        productComboBox.setPreferredSize(new Dimension(200, 25));
        
        // Apply RTL to combo box if needed
        if (isRightToLeft) {
            productComboBox.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        
        // Add "All Products" option
        productComboBox.addItem(new Product(0, messages.getString("adjustments.allProducts"), "ALL", 0, 0, 0.0, 0, 0));
        // Load products from database
        List<Product> products = controller.getAllProducts();
        for (Product product : products) {
            productComboBox.addItem(product);
        }
        
        productPanel.add(productComboBox);
        
        // Date range filter
        JPanel datePanel = new JPanel(new FlowLayout(isRightToLeft ? FlowLayout.RIGHT : FlowLayout.LEFT));
        datePanel.add(new JLabel(messages.getString("adjustments.filter.dateRange") + ":"));

        // Initialize the class fields
        startDateChooser = new JDateChooser();
        startDateChooser.setPreferredSize(new Dimension(130, 25));
        startDateChooser.setDateFormatString("yyyy-MM-dd");

        endDateChooser = new JDateChooser();
        endDateChooser.setPreferredSize(new Dimension(130, 25));
        endDateChooser.setDateFormatString("yyyy-MM-dd");

        datePanel.add(startDateChooser);
        datePanel.add(new JLabel(messages.getString("common.to")));
        datePanel.add(endDateChooser);
        
        // Reason filter
        JPanel reasonPanel = new JPanel(new FlowLayout(isRightToLeft ? FlowLayout.RIGHT : FlowLayout.LEFT));
        reasonPanel.add(new JLabel(messages.getString("adjustments.filter.reason") + ":"));
        reasonField = new JTextField(20);
        
        // Apply RTL to text field if needed
        if (isRightToLeft) {
            reasonField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        
        reasonPanel.add(reasonField);
        
        // Quantity range filter
        JPanel qtyPanel = new JPanel(new FlowLayout(isRightToLeft ? FlowLayout.RIGHT : FlowLayout.LEFT));
        qtyPanel.add(new JLabel(messages.getString("adjustments.filter.quantityRange") + ":"));
        minQtyField = new JTextField(5);
        
        qtyPanel.add(minQtyField);
        qtyPanel.add(new JLabel(messages.getString("common.to")));
        maxQtyField = new JTextField(5);
        qtyPanel.add(maxQtyField);


        // Apply RTL to text fields if needed
        if (isRightToLeft) {
            minQtyField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            maxQtyField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        

        
        // Search and clear buttons
        JPanel buttonsPanel = new JPanel(new FlowLayout(isRightToLeft ? FlowLayout.LEFT : FlowLayout.RIGHT));
        searchButton = new JButton(messages.getString("button.search"));
        clearButton = new JButton(messages.getString("button.clear"));
        
        searchButton.addActionListener(this::onSearchButtonClicked);
        clearButton.addActionListener(e -> {
            productComboBox.setSelectedIndex(0);
            startDateChooser.setDate(null);
            endDateChooser.setDate(null);
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
        tablePanel.setBorder(BorderFactory.createTitledBorder(messages.getString("adjustments.historyTitle")));
        
        String[] columnNames = {
            messages.getString("column.id"),
            messages.getString("column.product"),
            messages.getString("column.date"),
            messages.getString("adjustments.column.quantityChange"),
            messages.getString("adjustments.column.reason")
        };
        
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
        
        // Apply RTL to table if needed
        if (isRightToLeft) {
            adjustmentTable.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            adjustmentTable.getTableHeader().setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        
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
        JPanel buttonsPanel = new JPanel(new FlowLayout(isRightToLeft ? FlowLayout.LEFT : FlowLayout.RIGHT));
        
        addButton = new JButton(messages.getString("adjustments.button.add"));
        editButton = new JButton(messages.getString("button.edit"));
        deleteButton = new JButton(messages.getString("button.delete"));
        
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
            
            // Get dates from date choosers
            Date startDate = startDateChooser.getDate();
            Date endDate = endDateChooser.getDate();
            
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
            
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                messages.getString("adjustments.error.invalidNumber"),
                messages.getString("dialog.inputError"),
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
                messages.getString("adjustments.error.selectToEdit"), 
                messages.getString("dialog.noSelection"), 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void onDeleteButtonClicked(ActionEvent e) {
        int selectedRow = adjustmentTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < currentAdjustments.size()) {
            InventoryAdjustment selectedAdjustment = currentAdjustments.get(selectedRow);
            
            int confirm = JOptionPane.showConfirmDialog(this,
                messages.getString("adjustments.confirm.delete")
                    .replace("{0}", String.valueOf(selectedAdjustment.getChangeQty()))
                    .replace("{1}", selectedAdjustment.getProductName()),
                messages.getString("dialog.confirmDeletion"),
                JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = controller.deleteAdjustment(selectedAdjustment.getId());
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        messages.getString("adjustments.success.deleted"),
                        messages.getString("dialog.success"),
                        JOptionPane.INFORMATION_MESSAGE);
                    loadAllAdjustments();
                } else {
                    JOptionPane.showMessageDialog(this,
                        messages.getString("adjustments.error.delete"),
                        messages.getString("dialog.error"),
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                messages.getString("adjustments.error.selectToDelete"), 
                messages.getString("dialog.noSelection"), 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void showAdjustmentDialog(InventoryAdjustment adjustment) {
        // Create dialog
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), 
            messages.getString("dialog.adjustmentDetails"));
        dialog.setLayout(new BorderLayout());
        dialog.setSize(450, 300);
        dialog.setLocationRelativeTo(this);
        
        // Apply RTL orientation if needed
        if (isRightToLeft) {
            dialog.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            ArabicFontHelper.applyArabicFont(dialog);
        }
        
        JPanel formPanel = new JPanel();
        formPanel.setLayout(new GridLayout(5, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Form fields
        JTextField idField = new JTextField();
        JComboBox<Product> productField = new JComboBox<>();
        JDateChooser dateChooser = new JDateChooser();
        JSpinner quantityField = new JSpinner(new SpinnerNumberModel(0, -1000, 1000, 1));
        JTextArea reasonField = new JTextArea();
        
        // Apply RTL to text components if needed
        if (isRightToLeft) {
            idField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            productField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            reasonField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        
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
            dateChooser.setDate(new Date());
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
            
            dateChooser.setDate(adjustment.getDate());
            quantityField.setValue(adjustment.getChangeQty());
            reasonField.setText(adjustment.getReason());
            
            // Disable product change for existing adjustments
            productField.setEnabled(false);
        }
        
        // Add fields to form
        formPanel.add(new JLabel(messages.getString("column.id") + ":"));
        formPanel.add(idField);
        formPanel.add(new JLabel(messages.getString("column.product") + ":"));
        formPanel.add(productField);
        formPanel.add(new JLabel(messages.getString("column.date") + ":"));
        formPanel.add(dateChooser);
        formPanel.add(new JLabel(messages.getString("adjustments.label.quantityChange") + ":"));
        formPanel.add(quantityField);
        formPanel.add(new JLabel(messages.getString("adjustments.column.reason") + ":"));
        
        // Use scroll pane for reason text area
        JScrollPane reasonScrollPane = new JScrollPane(reasonField);
        reasonField.setLineWrap(true);
        reasonField.setWrapStyleWord(true);
        formPanel.add(reasonScrollPane);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(isRightToLeft ? FlowLayout.LEFT : FlowLayout.RIGHT));
        JButton saveButton = new JButton(messages.getString("button.save"));
        JButton cancelButton = new JButton(messages.getString("button.cancel"));
        
        saveButton.addActionListener(e -> {
            try {
                // Validate input
                if (reasonField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, 
                        messages.getString("adjustments.error.reasonRequired"), 
                        messages.getString("dialog.validationError"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                // Get date from date chooser
                Date date = dateChooser.getDate();
                if (date == null) {
                    JOptionPane.showMessageDialog(dialog, 
                        messages.getString("adjustments.error.dateRequired"), 
                        messages.getString("dialog.validationError"), JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
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
                        messages.getString("adjustments.success.saved"),
                        messages.getString("dialog.success"),
                        JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadAllAdjustments();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                        messages.getString("adjustments.error.save"),
                        messages.getString("dialog.error"),
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, 
                    messages.getString("error.invalidNumber"), 
                    messages.getString("dialog.inputError"), JOptionPane.ERROR_MESSAGE);
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