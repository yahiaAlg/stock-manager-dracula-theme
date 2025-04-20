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
        
        // Charts panel - changed to 2x2 grid for four charts
        chartsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
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
        
        // Use BorderLayout for better space allocation
        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.add(metricsPanel, BorderLayout.NORTH);
        
        // Set fixed height for chartsPanel - increased height for 2x2 grid
        chartsPanel.setPreferredSize(new Dimension(800, 600));
        mainPanel.add(chartsPanel, BorderLayout.CENTER);
        
        // Create a split pane to divide space between charts and low stock
        JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, mainPanel, lowStockPanel);
        splitPane.setResizeWeight(0.7); // Give 70% of space to the top component
        splitPane.setDividerLocation(700); // Initial divider location - increased for more chart space
        splitPane.setOneTouchExpandable(true);
        
        // Add to the main view
        add(splitPane, BorderLayout.CENTER);
        
        // Add tab panel for additional analysis views
        JTabbedPane tabbedAnalytics = new JTabbedPane();
        tabbedAnalytics.addTab("Main Dashboard", splitPane);
        tabbedAnalytics.addTab("Product Analysis", createProductAnalysisPanel());
        tabbedAnalytics.addTab("Order Analysis", createOrderAnalysisPanel());
        
        add(tabbedAnalytics, BorderLayout.CENTER);
    }
    
    private JPanel createProductAnalysisPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create metrics panel for product statistics
        JPanel metricsPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        metricsPanel.setBorder(BorderFactory.createTitledBorder("Product Metrics"));
        
        Map<String, Object> priceStats = controller.getProductPriceStats();
        Map<String, Object> stockStats = controller.getProductStockStats();
        
        JLabel minPriceLabel = createMetricLabel("Min Price", 
            String.format("$%.2f", priceStats.get("minPrice") != null ? 
                ((Number)priceStats.get("minPrice")).doubleValue() : 0.0));
                
        JLabel maxPriceLabel = createMetricLabel("Max Price", 
            String.format("$%.2f", priceStats.get("maxPrice") != null ? 
                ((Number)priceStats.get("maxPrice")).doubleValue() : 0.0));
                
        JLabel avgPriceLabel = createMetricLabel("Avg Price", 
            String.format("$%.2f", priceStats.get("avgPrice") != null ? 
                ((Number)priceStats.get("avgPrice")).doubleValue() : 0.0));
                
        JLabel medianPriceLabel = createMetricLabel("Median Price", 
            String.format("$%.2f", priceStats.get("medianPrice") != null ? 
                ((Number)priceStats.get("medianPrice")).doubleValue() : 0.0));
        
        JLabel minStockLabel = createMetricLabel("Min Stock", 
            String.valueOf(stockStats.get("minStock") != null ? 
                ((Number)stockStats.get("minStock")).intValue() : 0));
                
        JLabel maxStockLabel = createMetricLabel("Max Stock", 
            String.valueOf(stockStats.get("maxStock") != null ? 
                ((Number)stockStats.get("maxStock")).intValue() : 0));
                
        JLabel avgStockLabel = createMetricLabel("Avg Stock", 
            String.format("%.1f", stockStats.get("avgStock") != null ? 
                ((Number)stockStats.get("avgStock")).doubleValue() : 0.0));
                
        JLabel zeroStockLabel = createMetricLabel("Out of Stock", 
            String.valueOf(stockStats.get("zeroStock") != null ? 
                ((Number)stockStats.get("zeroStock")).intValue() : 0) + " products");
        
        metricsPanel.add(minPriceLabel);
        metricsPanel.add(maxPriceLabel);
        metricsPanel.add(avgPriceLabel);
        metricsPanel.add(medianPriceLabel);
        metricsPanel.add(minStockLabel);
        metricsPanel.add(maxStockLabel);
        metricsPanel.add(avgStockLabel);
        metricsPanel.add(zeroStockLabel);
        
        // Create charts panel for product analysis
        JPanel chartsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        chartsPanel.setBorder(BorderFactory.createTitledBorder("Product Analysis"));
        
        // Create and add price histogram chart
        ChartPanel priceHistogramPanel = new ChartPanel(controller.createProductPriceHistogram());
        priceHistogramPanel.setPreferredSize(new Dimension(350, 250));
        
        // Create and add products by category chart
        ChartPanel categoryChartPanel = new ChartPanel(controller.createProductsByCategory());
        categoryChartPanel.setPreferredSize(new Dimension(350, 250));
        
        // Create and add products by supplier chart
        ChartPanel supplierChartPanel = new ChartPanel(controller.createProductsBySupplier());
        supplierChartPanel.setPreferredSize(new Dimension(350, 250));
        
        // Create and add price vs stock chart
        ChartPanel priceVsStockPanel = new ChartPanel(controller.createPriceVsStockChart());
        priceVsStockPanel.setPreferredSize(new Dimension(350, 250));
        
        chartsPanel.add(priceHistogramPanel);
        chartsPanel.add(categoryChartPanel);
        chartsPanel.add(supplierChartPanel);
        chartsPanel.add(priceVsStockPanel);
        
        // Add to main panel
        panel.add(metricsPanel, BorderLayout.NORTH);
        panel.add(chartsPanel, BorderLayout.CENTER);
        
        return panel;
    }
    
    private JPanel createOrderAnalysisPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Create metrics panel for order statistics
        JPanel metricsPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        metricsPanel.setBorder(BorderFactory.createTitledBorder("Order Metrics"));
        
        Map<String, Object> orderStats = controller.getOrderStats();
        Map<String, Object> dateRangeStats = controller.getOrdersByDateRange();
        
        JLabel totalOrdersLabel = createMetricLabel("Total Orders", 
            String.valueOf(orderStats.get("totalOrders") != null ? 
                ((Number)orderStats.get("totalOrders")).intValue() : 0));
                
        JLabel avgOrderValueLabel = createMetricLabel("Avg Order Value", 
            String.format("$%.2f", orderStats.get("avgOrderValue") != null ? 
                ((Number)orderStats.get("avgOrderValue")).doubleValue() : 0.0));
                
        JLabel maxOrderValueLabel = createMetricLabel("Max Order Value", 
            String.format("$%.2f", orderStats.get("maxOrderValue") != null ? 
                ((Number)orderStats.get("maxOrderValue")).doubleValue() : 0.0));
                
        JLabel totalRevenueLabel = createMetricLabel("Total Revenue", 
            String.format("$%.2f", orderStats.get("totalRevenue") != null ? 
                ((Number)orderStats.get("totalRevenue")).doubleValue() : 0.0));
        
        JLabel todayOrdersLabel = createMetricLabel("Today's Orders", 
            String.valueOf(dateRangeStats.get("todayCount") != null ? 
                ((Number)dateRangeStats.get("todayCount")).intValue() : 0) + 
            " ($" + String.format("%.2f", dateRangeStats.get("todayTotal") != null ? 
                ((Number)dateRangeStats.get("todayTotal")).doubleValue() : 0.0) + ")");
                
        JLabel weekOrdersLabel = createMetricLabel("7-Day Orders", 
            String.valueOf(dateRangeStats.get("weekCount") != null ? 
                ((Number)dateRangeStats.get("weekCount")).intValue() : 0) + 
            " ($" + String.format("%.2f", dateRangeStats.get("weekTotal") != null ? 
                ((Number)dateRangeStats.get("weekTotal")).doubleValue() : 0.0) + ")");
                
        JLabel monthOrdersLabel = createMetricLabel("30-Day Orders", 
            String.valueOf(dateRangeStats.get("monthCount") != null ? 
                ((Number)dateRangeStats.get("monthCount")).intValue() : 0) + 
            " ($" + String.format("%.2f", dateRangeStats.get("monthTotal") != null ? 
                ((Number)dateRangeStats.get("monthTotal")).doubleValue() : 0.0) + ")");
                
        JLabel avgItemsLabel = createMetricLabel("Avg Items/Order", 
            String.format("%.1f", orderStats.get("avgItemsPerOrder") != null ? 
                ((Number)orderStats.get("avgItemsPerOrder")).doubleValue() : 0.0));
        
        metricsPanel.add(totalOrdersLabel);
        metricsPanel.add(avgOrderValueLabel);
        metricsPanel.add(maxOrderValueLabel);
        metricsPanel.add(totalRevenueLabel);
        metricsPanel.add(todayOrdersLabel);
        metricsPanel.add(weekOrdersLabel);
        metricsPanel.add(monthOrdersLabel);
        metricsPanel.add(avgItemsLabel);
        
        // Create charts panel for order analysis
        JPanel chartsPanel = new JPanel(new GridLayout(2, 2, 10, 10));
        chartsPanel.setBorder(BorderFactory.createTitledBorder("Order Analysis"));
        
        // Create and add order status chart
        ChartPanel orderStatusPanel = new ChartPanel(controller.createOrdersByStatusChart());
        orderStatusPanel.setPreferredSize(new Dimension(350, 250));
        
        // Create and add orders by customer chart
        ChartPanel customerChartPanel = new ChartPanel(controller.createOrdersByCustomer());
        customerChartPanel.setPreferredSize(new Dimension(350, 250));
        
        // Create and add order value distribution chart
        ChartPanel orderValuePanel = new ChartPanel(controller.createOrderValueDistribution());
        orderValuePanel.setPreferredSize(new Dimension(350, 250));
        
        // Create and add order status trend chart
        ChartPanel orderTrendPanel = new ChartPanel(controller.createOrderStatusTrend());
        orderTrendPanel.setPreferredSize(new Dimension(350, 250));
        
        chartsPanel.add(orderStatusPanel);
        chartsPanel.add(customerChartPanel);
        chartsPanel.add(orderValuePanel);
        chartsPanel.add(orderTrendPanel);
        
        // Add to main panel
        panel.add(metricsPanel, BorderLayout.NORTH);
        panel.add(chartsPanel, BorderLayout.CENTER);
        
        return panel;
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

        // Remove and re-create the analysis tabs to refresh them
        JTabbedPane tabbedAnalytics = (JTabbedPane) getComponent(0);
        tabbedAnalytics.removeTabAt(2); // Order Analysis tab
        tabbedAnalytics.removeTabAt(1); // Product Analysis tab
        
        tabbedAnalytics.addTab("Product Analysis", createProductAnalysisPanel());
        tabbedAnalytics.addTab("Order Analysis", createOrderAnalysisPanel());        
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
        
        // Create and add sales chart with proper sizing
        ChartPanel salesChartPanel = new ChartPanel(controller.createSalesChart(30));
        salesChartPanel.setPreferredSize(new Dimension(350, 250));
        salesChartPanel.setMinimumSize(new Dimension(300, 200));
        
        // Create and add category chart with proper sizing
        ChartPanel categoryChartPanel = new ChartPanel(controller.createStockByCategory());
        categoryChartPanel.setPreferredSize(new Dimension(350, 250));
        categoryChartPanel.setMinimumSize(new Dimension(300, 200));
        
        // Create and add top products pie chart with proper sizing
        ChartPanel topProductsChartPanel = new ChartPanel(controller.createTopProductsChart());
        topProductsChartPanel.setPreferredSize(new Dimension(350, 250));
        topProductsChartPanel.setMinimumSize(new Dimension(300, 200));
        
        // Create and add inventory trend chart with proper sizing
        ChartPanel inventoryTrendChartPanel = new ChartPanel(controller.createInventoryTrendChart(30));
        inventoryTrendChartPanel.setPreferredSize(new Dimension(350, 250));
        inventoryTrendChartPanel.setMinimumSize(new Dimension(300, 200));
        
        // Add all charts to panel
        chartsPanel.add(salesChartPanel);
        chartsPanel.add(categoryChartPanel);
        chartsPanel.add(topProductsChartPanel);
        chartsPanel.add(inventoryTrendChartPanel);
        
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