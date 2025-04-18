package view;

import controller.DashboardController;
import model.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;
import org.jfree.chart.ChartPanel;

public class DashboardView extends JPanel {
    
    private DashboardController controller;
    
    private JPanel metricsPanel;
    private JPanel chartsPanel;
    private JPanel lowStockPanel;
    
    private JLabel totalProductsLabel;
    private JLabel lowStockCountLabel;
    private JLabel inventoryValueLabel;
    private JLabel totalOrdersLabel;
    private JLabel pendingOrdersLabel;
    private JLabel todayOrdersLabel;
    private JLabel todaySalesLabel;
    private JLabel recentSalesLabel;
    
    private JTable lowStockTable;
    private DefaultTableModel lowStockTableModel;
    
    public DashboardView(DashboardController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        initComponents();
        refreshData();
    }
    
    private void initComponents() {
        // Metrics panel
        metricsPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        metricsPanel.setBorder(BorderFactory.createTitledBorder("Key Metrics"));
        
        totalProductsLabel = createMetricLabel("Total Products", "0");
        lowStockCountLabel = createMetricLabel("Low Stock Items", "0");
        inventoryValueLabel = createMetricLabel("Inventory Value", "$0.00");
        totalOrdersLabel = createMetricLabel("Total Orders", "0");
        pendingOrdersLabel = createMetricLabel("Pending Orders", "0");
        todayOrdersLabel = createMetricLabel("Today's Orders", "0");
        todaySalesLabel = createMetricLabel("Today's Sales", "$0.00");
        recentSalesLabel = createMetricLabel("30-Day Sales", "$0.00");
        
        metricsPanel.add(totalProductsLabel);
        metricsPanel.add(lowStockCountLabel);
        metricsPanel.add(inventoryValueLabel);
        metricsPanel.add(totalOrdersLabel);
        metricsPanel.add(pendingOrdersLabel);
        metricsPanel.add(todayOrdersLabel);
        metricsPanel.add(todaySalesLabel);
        metricsPanel.add(recentSalesLabel);
        
        // Charts panel
        chartsPanel = new JPanel(new GridLayout(1, 2, 10, 10));
        chartsPanel.setBorder(BorderFactory.createTitledBorder("Analytics"));
        
        
        // Low stock panel
        lowStockPanel = new JPanel(new BorderLayout(5, 5));
        lowStockPanel.setBorder(BorderFactory.createTitledBorder("Low Stock Items"));
        
        String[] columnNames = {"ID", "SKU", "Name", "Category", "Supplier", "Stock Qty", "Reorder Level"};
        lowStockTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        lowStockTable = new JTable(lowStockTableModel);
        lowStockTable.setFillsViewportHeight(true);
        lowStockTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        JScrollPane scrollPane = new JScrollPane(lowStockTable);
        lowStockPanel.add(scrollPane, BorderLayout.CENTER);
        
        // Add all panels to the main view
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.add(metricsPanel, BorderLayout.NORTH);
        topPanel.add(chartsPanel, BorderLayout.CENTER);
        
        add(topPanel, BorderLayout.CENTER);
        add(lowStockPanel, BorderLayout.SOUTH);
    }
    
    public void refreshData() {
        // Update metrics
        Map<String, Object> metrics = controller.getStockMetrics();
        
        totalProductsLabel.setText(formatMetric("Total Products", metrics.get("totalProducts")));
        lowStockCountLabel.setText(formatMetric("Low Stock Items", metrics.get("lowStockCount")));
        
        Double invValue = metrics.get("inventoryValue") != null ? 
                         ((Number)metrics.get("inventoryValue")).doubleValue() : 0.0;
        inventoryValueLabel.setText(formatMetric("Inventory Value", String.format("$%.2f", invValue)));
        
        totalOrdersLabel.setText(formatMetric("Total Orders", metrics.get("totalOrders")));
        pendingOrdersLabel.setText(formatMetric("Pending Orders", metrics.get("pendingOrders")));
        todayOrdersLabel.setText(formatMetric("Today's Orders", metrics.get("todayOrders")));
        
        Double todaySales = metrics.get("todaySales") != null ? 
                           ((Number)metrics.get("todaySales")).doubleValue() : 0.0;
        todaySalesLabel.setText(formatMetric("Today's Sales", String.format("$%.2f", todaySales)));
        
        Double recentSales = metrics.get("recentSales") != null ? 
                            ((Number)metrics.get("recentSales")).doubleValue() : 0.0;
        recentSalesLabel.setText(formatMetric("30-Day Sales", String.format("$%.2f", recentSales)));
        
        // Update low stock table
        refreshLowStockTable();
        
        // Update charts
        refreshCharts();
    }
    
    private void refreshLowStockTable() {
        // Clear existing data
        lowStockTableModel.setRowCount(0);
        
        // Get low stock products
        List<Product> lowStockProducts = controller.getLowStockProducts();
        
        // Populate table
        for (Product product : lowStockProducts) {
            Object[] rowData = {
                product.getId(),
                product.getSku(),
                product.getName(),
                product.getCategoryName(),
                product.getSupplierName(),
                product.getStockQty(),
                product.getReorderLevel()
            };
            lowStockTableModel.addRow(rowData);
        }
    }
    
    private void refreshCharts() {
        // Remove existing charts
        chartsPanel.removeAll();
        
        // Add sales chart (last 30 days)
        ChartPanel salesChartPanel = new ChartPanel(controller.createSalesChart(30));
        salesChartPanel.setPreferredSize(new Dimension(400, 300));
        
        // Add product category chart
        ChartPanel categoryChartPanel = new ChartPanel(controller.createStockByCategory());
        categoryChartPanel.setPreferredSize(new Dimension(400, 300));
        
        chartsPanel.add(salesChartPanel);
        chartsPanel.add(categoryChartPanel);
        
        chartsPanel.revalidate();
        chartsPanel.repaint();
    }
    
    private JLabel createMetricLabel(String title, String value) {
        JLabel label = new JLabel("<html><div style='text-align: center;'>" +
                                  "<b>" + title + "</b><br>" +
                                  "<font size='+1'>" + value + "</font></div></html>", 
                                  JLabel.CENTER);
        label.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        return label;
    }
    
    private String formatMetric(String title, Object value) {
        return "<html><div style='text-align: center;'>" +
               "<b>" + title + "</b><br>" +
               "<font size='+1'>" + (value != null ? value.toString() : "0") + "</font></div></html>";
    }
}