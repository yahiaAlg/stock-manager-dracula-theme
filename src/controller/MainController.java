package controller;

import javax.swing.*;
import view.MainFrame;
import util.DBConnection;

public class MainController {
    private MainFrame mainFrame;
    
    public MainController() {
        // Initialize the database connection
        DBConnection.getConnection();
        
        // Create the main application frame
        SwingUtilities.invokeLater(() -> {
            mainFrame = new MainFrame(this);
            mainFrame.setVisible(true);
        });
    }
    
    public ProductController getProductController() {
        return new ProductController();
    }
    
    public SupplierController getSupplierController() {
        return new SupplierController();
    }
    
    public CustomerController getCustomerController() {
        return new CustomerController();
    }
    
    public OrderController getOrderController() {
        return new OrderController();
    }
    
    public DashboardController getDashboardController() {
        return new DashboardController();
    }
    
    public ReportController getReportController() {
        return new ReportController();
    }
    
    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            // call the main frame
            new MainController();
        } catch (Exception e) {
            e.printStackTrace();
        }
        
        // Start the application
        new MainController();
    }
}