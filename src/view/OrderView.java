package view;

import controller.OrderController;
import controller.CustomerController;
import model.Order;
import model.Customer;
import util.PDFGenerator;
import util.ArabicFontHelper;
import java.util.Locale;
import java.util.ResourceBundle;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.text.SimpleDateFormat;
import java.util.List;
import java.io.File;
import java.io.IOException;

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
    private JButton generatePdfButton;
    
    private OrderController orderController;
    private CustomerController customerController;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    private ResourceBundle messages;
    private boolean isRightToLeft;
        
    public OrderView(OrderController orderController, CustomerController customerController) {
        this.orderController = orderController;
        this.customerController = customerController;
        
        // Load localization resources
        loadLocalization();
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        initComponents();
        loadOrders();
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

    private void loadCustomers() {
        List<Customer> customers = customerController.getAllCustomers();
        customerFilter.removeAllItems();
        customerFilter.addItem(new Customer(0, messages.getString("orders.filter.allCustomers"), "", "", ""));
        for (Customer customer : customers) {
            customerFilter.addItem(customer);
        }
    }
    
    private void initComponents() {
        // Create top panel with search controls
        JPanel topPanel = new JPanel(new BorderLayout());
        JPanel searchPanel = new JPanel(new FlowLayout(isRightToLeft ? FlowLayout.RIGHT : FlowLayout.LEFT));
        
        searchField = new JTextField(15);
        if (isRightToLeft) {
            searchField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        
        searchPanel.add(new JLabel(messages.getString("common.search") + ":"));
        searchPanel.add(searchField);
        
        // Status filter
        searchPanel.add(new JLabel(messages.getString("orders.filter.status") + ":"));
        statusFilter = new JComboBox<>(new String[] {
            messages.getString("orders.status.all"), 
            messages.getString("orders.status.new"), 
            messages.getString("orders.status.processing"), 
            messages.getString("orders.status.shipped"), 
            messages.getString("orders.status.delivered"), 
            messages.getString("orders.status.cancelled")
        });
        searchPanel.add(statusFilter);
        
        // Customer filter
        searchPanel.add(new JLabel(messages.getString("orders.filter.customer") + ":"));
        customerFilter = new JComboBox<>();
        loadCustomers();
        searchPanel.add(customerFilter);
        
        searchButton = new JButton(messages.getString("button.search"));
        searchButton.addActionListener(this::searchOrders);
        searchPanel.add(searchButton);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(isRightToLeft ? FlowLayout.LEFT : FlowLayout.RIGHT));
        newOrderButton = new JButton(messages.getString("orders.button.newOrder"));
        newOrderButton.addActionListener(e -> createNewOrder());
        buttonPanel.add(newOrderButton);
        
        topPanel.add(searchPanel, BorderLayout.WEST);
        topPanel.add(buttonPanel, BorderLayout.EAST);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Create table
        String[] columns = {
            messages.getString("column.id"), 
            messages.getString("orders.column.date"), 
            messages.getString("orders.column.customer"), 
            messages.getString("orders.column.totalAmount"), 
            messages.getString("orders.column.status"), 
            messages.getString("orders.column.actions")
        };
        
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 5; // Only actions column is editable
            }
        };
        
        orderTable = new JTable(tableModel);
        
        // Apply RTL to table if needed
        if (isRightToLeft) {
            orderTable.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            orderTable.getTableHeader().setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        
        orderTable.getColumnModel().getColumn(5).setCellRenderer(new ButtonsRenderer());
        orderTable.getColumnModel().getColumn(5).setCellEditor(new ButtonsEditor(orderTable));
        
        JScrollPane scrollPane = new JScrollPane(orderTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Create bottom panel with buttons
        JPanel bottomPanel = new JPanel(new FlowLayout(isRightToLeft ? FlowLayout.LEFT : FlowLayout.RIGHT));
        viewButton = new JButton(messages.getString("orders.button.viewSelected"));
        viewButton.addActionListener(e -> viewSelectedOrder());
        bottomPanel.add(viewButton);
        
        editButton = new JButton(messages.getString("orders.button.editSelected"));
        editButton.addActionListener(e -> editSelectedOrder());
        bottomPanel.add(editButton);
        
        deleteButton = new JButton(messages.getString("orders.button.deleteSelected"));
        deleteButton.addActionListener(e -> deleteSelectedOrder());
        bottomPanel.add(deleteButton);
        
        // Add new PDF generation button
        generatePdfButton = new JButton(messages.getString("orders.button.generatePdf"));
        generatePdfButton.addActionListener(e -> generatePdfTicket());
        bottomPanel.add(generatePdfButton);
        
        add(bottomPanel, BorderLayout.SOUTH);
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
        row[3] = String.format("DZD %.2f", order.getTotalAmount());
        row[4] = order.getStatus();
        row[5] = messages.getString("orders.column.actions");
        
        tableModel.addRow(row);
    }
    
    private void searchOrders(ActionEvent e) {
        tableModel.setRowCount(0);
        
        // Get filter values
        String status = statusFilter.getSelectedItem().toString();
        if (status.equals(messages.getString("orders.status.all"))) {
            status = null;
        }
        
        Customer selectedCustomer = (Customer) customerFilter.getSelectedItem();
        Integer customerId = (selectedCustomer.getId() > 0) ? selectedCustomer.getId() : null;
        
        // Get search term
        String searchTerm = searchField.getText().trim();
        if (searchTerm.isEmpty()) {
            searchTerm = null;
        }
        
        List<Order> orders = orderController.searchOrders(customerId, status, null, null, searchTerm);
        
        for (Order order : orders) {
            addOrderToTable(order);
        }
    }
    
    private void createNewOrder() {
        OrderForm orderForm = new OrderForm(null, orderController, customerController);
        
        // Apply RTL orientation if needed
        if (isRightToLeft) {
            orderForm.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            ArabicFontHelper.applyArabicFont(orderForm);
        }
        
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
                OrderForm orderForm = new OrderForm(order, orderController, customerController);
                
                // Apply RTL orientation if needed
                if (isRightToLeft) {
                    orderForm.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
                    ArabicFontHelper.applyArabicFont(orderForm);
                }
                
                orderForm.setViewOnly(true);
                orderForm.setVisible(true);
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                messages.getString("orders.error.selectToView"), 
                messages.getString("dialog.noSelection"), 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void editSelectedOrder() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow >= 0) {
            int orderId = (int) tableModel.getValueAt(selectedRow, 0);
            Order order = orderController.getOrderById(orderId);
            
            if (order != null) {
                // Check if order can be edited
                if (order.getStatus().equals("New") || 
                    order.getStatus().equals("Pending") ||
                    order.getStatus().equals("Processing")) {
                    
                    OrderForm orderForm = new OrderForm(order, orderController, customerController);
                    
                    // Apply RTL orientation if needed
                    if (isRightToLeft) {
                        orderForm.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
                        ArabicFontHelper.applyArabicFont(orderForm);
                    }
                    
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
                        messages.getString("orders.error.cannotEdit").replace("{0}", order.getStatus()), 
                        messages.getString("dialog.cannotEdit"), 
                        JOptionPane.WARNING_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                messages.getString("orders.error.selectToEdit"), 
                messages.getString("dialog.noSelection"), 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void deleteSelectedOrder() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow >= 0) {
            int orderId = (int) tableModel.getValueAt(selectedRow, 0);
            
            int confirm = JOptionPane.showConfirmDialog(this,
                messages.getString("orders.confirm.delete"),
                messages.getString("dialog.confirmDeletion"),
                JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                
            if (confirm == JOptionPane.YES_OPTION) {
                if (orderController.deleteOrder(orderId)) {
                    tableModel.removeRow(selectedRow);
                    JOptionPane.showMessageDialog(this, 
                        messages.getString("orders.success.deleted"), 
                        messages.getString("dialog.success"), 
                        JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this, 
                        messages.getString("orders.error.delete"), 
                        messages.getString("dialog.error"), 
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                messages.getString("orders.error.selectToDelete"), 
                messages.getString("dialog.noSelection"), 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void generatePdfTicket() {
        int selectedRow = orderTable.getSelectedRow();
        if (selectedRow >= 0) {
            int orderId = (int) tableModel.getValueAt(selectedRow, 0);
            Order order = orderController.getOrderById(orderId);
            
            if (order != null) {
                try {
                    // Show a wait cursor
                    setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

                    PDFGenerator pdfGenerator = new PDFGenerator(
                        util.LocaleManager.getCurrentLocale().getLanguage(), 
                        util.LocaleManager.getCurrentLocale()
                    );
                    // Generate the PDF
                    File pdfFile = pdfGenerator.generateOrderTicket(order);
                    
                    // Restore the cursor
                    setCursor(Cursor.getDefaultCursor());
                    
                    // Show success message with option to open the file
                    int choice = JOptionPane.showConfirmDialog(
                        this,
                        messages.getString("orders.pdf.success").replace("{0}", pdfFile.getAbsolutePath()),
                        messages.getString("dialog.pdfGenerated"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    
                    // Open the file if requested
                    if (choice == JOptionPane.YES_OPTION) {
                        if (Desktop.isDesktopSupported()) {
                            try {
                                Desktop.getDesktop().open(pdfFile);
                            } catch (IOException e) {
                                JOptionPane.showMessageDialog(
                                    this,
                                    messages.getString("orders.pdf.cannotOpen").replace("{0}", pdfFile.getAbsolutePath()),
                                    messages.getString("dialog.cannotOpenFile"),
                                    JOptionPane.WARNING_MESSAGE
                                );
                            }
                        } else {
                            JOptionPane.showMessageDialog(
                                this,
                                messages.getString("orders.pdf.desktopNotSupported").replace("{0}", pdfFile.getAbsolutePath()),
                                messages.getString("dialog.cannotOpenFile"),
                                JOptionPane.INFORMATION_MESSAGE
                            );
                        }
                    }
                } catch (Exception e) {
                    // Restore the cursor
                    setCursor(Cursor.getDefaultCursor());
                    
                    // Show error message
                    JOptionPane.showMessageDialog(
                        this,
                        messages.getString("orders.pdf.error").replace("{0}", e.getMessage()),
                        messages.getString("dialog.pdfError"),
                        JOptionPane.ERROR_MESSAGE
                    );
                    e.printStackTrace();
                }
            }
        } else {
            JOptionPane.showMessageDialog(
                this,
                messages.getString("orders.error.selectForPdf"),
                messages.getString("dialog.noSelection"),
                JOptionPane.WARNING_MESSAGE
            );
        }
    }

    // Custom renderer for the buttons column
    private class ButtonsRenderer extends JPanel implements javax.swing.table.TableCellRenderer {
        private JButton viewButton = new JButton(messages.getString("button.view"));
        private JButton editButton = new JButton(messages.getString("button.edit"));
        private JButton pdfButton = new JButton(messages.getString("button.pdf"));
        
        public ButtonsRenderer() {
            setLayout(new FlowLayout(FlowLayout.CENTER, 2, 0));
            add(viewButton);
            add(editButton);
            add(pdfButton);
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
        private JButton viewButton = new JButton(messages.getString("button.view"));
        private JButton editButton = new JButton(messages.getString("button.edit"));
        private JButton pdfButton = new JButton(messages.getString("button.pdf"));
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
            
            pdfButton.addActionListener(e -> {
                fireEditingStopped();
                generatePdf(clickedRow);
            });
            
            panel.add(viewButton);
            panel.add(editButton);
            panel.add(pdfButton);
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            clickedRow = row;
            panel.setBackground(table.getSelectionBackground());
            return panel;
        }
        
        @Override
        public Object getCellEditorValue() {
            return messages.getString("orders.column.actions");
        }
        
        private void generatePdf(int row) {
            int orderId = (int) tableModel.getValueAt(row, 0);
            Order order = orderController.getOrderById(orderId);
            
            if (order != null) {
                try {
                    // Show a wait cursor
                    OrderView.this.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
                    PDFGenerator pdfGenerator = new PDFGenerator(
                        util.LocaleManager.getCurrentLocale().getLanguage(), 
                        util.LocaleManager.getCurrentLocale()
                    );
                    // Generate the PDF
                    File pdfFile = pdfGenerator.generateOrderTicket(order);
                    
                    // Restore the cursor
                    OrderView.this.setCursor(Cursor.getDefaultCursor());
                    
                    // Show success message with option to open the file
                    int choice = JOptionPane.showConfirmDialog(
                        OrderView.this,
                        messages.getString("orders.pdf.success").replace("{0}", pdfFile.getAbsolutePath()),
                        messages.getString("dialog.pdfGenerated"),
                        JOptionPane.YES_NO_OPTION,
                        JOptionPane.INFORMATION_MESSAGE
                    );
                    
                    // Open the file if requested
                    if (choice == JOptionPane.YES_OPTION) {
                        if (Desktop.isDesktopSupported()) {
                            try {
                                Desktop.getDesktop().open(pdfFile);
                            } catch (IOException e) {
                                JOptionPane.showMessageDialog(
                                    OrderView.this,
                                    messages.getString("orders.pdf.cannotOpen").replace("{0}", pdfFile.getAbsolutePath()),
                                    messages.getString("dialog.cannotOpenFile"),
                                    JOptionPane.WARNING_MESSAGE
                                );
                            }
                        } else {
                            JOptionPane.showMessageDialog(
                                OrderView.this,
                                messages.getString("orders.pdf.desktopNotSupported").replace("{0}", pdfFile.getAbsolutePath()),
                                messages.getString("dialog.cannotOpenFile"),
                                JOptionPane.INFORMATION_MESSAGE
                            );
                        }
                    }
                } catch (Exception e) {
                    // Restore the cursor
                    OrderView.this.setCursor(Cursor.getDefaultCursor());
                    
                    // Show error message
                    JOptionPane.showMessageDialog(
                        OrderView.this,
                        messages.getString("orders.pdf.error").replace("{0}", e.getMessage()),
                        messages.getString("dialog.pdfError"),
                        JOptionPane.ERROR_MESSAGE
                    );
                    e.printStackTrace();
                }
            }
        }
        
        private void viewOrder(int row) {
            int orderId = (int) tableModel.getValueAt(row, 0);
            Order order = orderController.getOrderById(orderId);
            
            if (order != null) {
                OrderForm orderForm = new OrderForm(order, orderController, customerController);
                
                // Apply RTL orientation if needed
                if (isRightToLeft) {
                    orderForm.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
                    ArabicFontHelper.applyArabicFont(orderForm);
                }
                
                orderForm.setViewOnly(true);
                orderForm.setVisible(true);
            }
        }
        
        private void editOrder(int row) {
            int orderId = (int) tableModel.getValueAt(row, 0);
            Order order = orderController.getOrderById(orderId);
            
            if (order != null) {
                // Check if order can be edited
                if (order.getStatus().equals("New") || 
                    order.getStatus().equals("Pending") ||
                    order.getStatus().equals("Processing")) {
                    
                    OrderForm orderForm = new OrderForm(order, orderController, customerController);
                    
                    // Apply RTL orientation if needed
                    if (isRightToLeft) {
                        orderForm.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
                        ArabicFontHelper.applyArabicFont(orderForm);
                    }
                    
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
                        messages.getString("orders.error.cannotEdit").replace("{0}", order.getStatus()), 
                        messages.getString("dialog.cannotEdit"), 
                        JOptionPane.WARNING_MESSAGE);
                }
            }
        }
    }
    
    public void refreshData() {
        loadOrders();
    }
}