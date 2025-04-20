package view;

import javax.swing.*;

import controller.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import util.DBConnection;

public class MainFrame extends JFrame {
    
    private JTabbedPane tabbedPane;
    
    // Controllers
    private DashboardController dashboardController;
    private ProductController productController;
    private SupplierController supplierController;
    private CustomerController customerController;
    private OrderController orderController;
    private ReportController reportController;
    private CategoryController categoryController;
    private InventoryAdjustmentController inventoryAdjustmentController;

    // Views
    private DashboardView dashboardView;
    private ProductView productView;
    private SupplierView supplierView;
    private CustomerView customerView;
    private OrderView orderView;
    private ReportView reportView;
    private CategoryView categoryView;
    private InventoryAdjustmentView inventoryAdjustmentView;

    public MainFrame(MainController mainController) {
        setTitle("Stock Manager");
        setSize(1024, 768);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        
        // Initialize controllers
        initControllers();
        
        // Initialize UI components
        initComponents();
        
        // Close database connection when application exits
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                DBConnection.closeConnection();
            }
        });
    }
    
    private void initControllers() {
        // Initialize all controllers
        dashboardController = new DashboardController();
        categoryController = new CategoryController();
        productController = new ProductController();
        supplierController = new SupplierController();
        customerController = new CustomerController();
        orderController = new OrderController();
        reportController = new ReportController();
        inventoryAdjustmentController = new InventoryAdjustmentController();
    }
    
    private void initComponents() {
        // Initialize main tabbed pane
        tabbedPane = new JTabbedPane();
        
        // Create placeholder panels for lazy loading
        JPanel dashboardPlaceholder = createPlaceholderPanel("Loading Dashboard...");
        JPanel categoriesPlaceholder = createPlaceholderPanel("Loading Categories...");
        JPanel productsPlaceholder = createPlaceholderPanel("Loading Products...");
        JPanel suppliersPlaceholder = createPlaceholderPanel("Loading Suppliers...");
        JPanel customersPlaceholder = createPlaceholderPanel("Loading Customers...");
        JPanel ordersPlaceholder = createPlaceholderPanel("Loading Orders...");
        JPanel reportsPlaceholder = createPlaceholderPanel("Loading Reports...");
        JPanel adjustmentsPlaceholder = createPlaceholderPanel("Loading Inventory Adjustments...");
        
        // Add tabs with placeholders
        tabbedPane.addTab("Dashboard", dashboardPlaceholder);
        tabbedPane.addTab("Category", categoriesPlaceholder);
        tabbedPane.addTab("Products", productsPlaceholder);
        tabbedPane.addTab("Suppliers", suppliersPlaceholder);
        tabbedPane.addTab("Customers", customersPlaceholder);
        tabbedPane.addTab("Orders", ordersPlaceholder);
        tabbedPane.addTab("Reports", reportsPlaceholder);
        tabbedPane.addTab("Inventory Adjustments", adjustmentsPlaceholder);
        
        add(tabbedPane, BorderLayout.CENTER);
        
        // Add change listener to load views when tabs are selected
        tabbedPane.addChangeListener(e -> {
            int selectedIndex = tabbedPane.getSelectedIndex();
            switch (selectedIndex) {
                case 0: // Dashboard
                    if (dashboardView == null) {
                        dashboardView = new DashboardView(dashboardController);
                        tabbedPane.setComponentAt(0, dashboardView);
                    }
                    break;
                case 1: // Categories
                    if (categoryView == null) {
                        categoryView = new CategoryView(categoryController);
                        tabbedPane.setComponentAt(1, categoryView);
                    }
                    break;
                case 2: // Products
                    if (productView == null) {
                        productView = new ProductView(productController);
                        tabbedPane.setComponentAt(2, productView);
                    }
                    break;
                case 3: // Suppliers
                    if (supplierView == null) {
                        supplierView = new SupplierView(supplierController);
                        tabbedPane.setComponentAt(3, supplierView);
                    }
                    break;
                case 4: // Customers
                    if (customerView == null) {
                        customerView = new CustomerView(customerController);
                        tabbedPane.setComponentAt(4, customerView);
                    }
                    break;
                case 5: // Orders
                    if (orderView == null) {
                        orderView = new OrderView(orderController, customerController);
                        tabbedPane.setComponentAt(5, orderView);
                    }
                    break;
                case 6: // Reports
                    if (reportView == null) {
                        reportView = new ReportView(reportController);
                        tabbedPane.setComponentAt(6, reportView);
                    }
                    break;
                case 7: // Inventory Adjustments
                    if (inventoryAdjustmentView == null) {
                        inventoryAdjustmentView = new InventoryAdjustmentView(inventoryAdjustmentController);
                        tabbedPane.setComponentAt(7, inventoryAdjustmentView);
                    }
                    break;
            }
        });
        
        // Initialize the first tab (Dashboard) immediately
        tabbedPane.setSelectedIndex(0);
    }
    // Add this method to refresh the active tab when needed
    public void refreshCurrentTab() {
        int selectedIndex = tabbedPane.getSelectedIndex();
        switch (selectedIndex) {
            case 0: // Dashboard
                if (dashboardView != null) {
                    dashboardView.refreshData();
                }
                break;
            case 1: // Categories
                if (categoryView != null) {
                    categoryView.refreshData();
                }
                break;
            case 2: // Products
                if (productView != null) {
                    productView.refreshData();
                }
                break;
            case 3: // Suppliers
                if (supplierView != null) {
                    supplierView.refreshData();
                }
                break;
            case 4: // Customers
                if (customerView != null) {
                    customerView.refreshData();
                }
                break;

            case 7: // Inventory Adjustments
                if (inventoryAdjustmentView != null) {
                    inventoryAdjustmentView.refreshData();
                }
                break;
        }
    }    
    private JPanel createPlaceholderPanel(String message) {
        JPanel panel = new JPanel(new BorderLayout());
        JLabel label = new JLabel(message, JLabel.CENTER);
        label.setFont(new Font("Arial", Font.BOLD, 18));
        panel.add(label, BorderLayout.CENTER);
        
        // Add a loading spinner or progress indicator
        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        panel.add(progressBar, BorderLayout.SOUTH);
        
        return panel;
    }

    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel("com.formdev.flatlaf.intellijthemes.FlatGradiantoMidnightBlueIJTheme");
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Load database
        DBConnection.getConnection();
        
        // Start application
        SwingUtilities.invokeLater(() -> {
            MainFrame frame = new MainFrame(null);
            frame.setVisible(true);
        });
    }
}