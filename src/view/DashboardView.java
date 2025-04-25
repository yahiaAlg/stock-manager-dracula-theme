package view;

import controller.DashboardController;
import model.Product;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.ResourceBundle;
import org.jfree.chart.ChartPanel;
import util.ArabicFontHelper;
import util.LocaleManager;

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
    
    private ResourceBundle messages;
    private boolean isRightToLeft;
    
    public DashboardView(DashboardController controller) {
        this.controller = controller;
        
        // Load localization resources
        loadLocalization();
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        initComponents();
        refreshData();
    }
    
    private void loadLocalization() {
        // Get current locale from LocaleManager
        Locale currentLocale = LocaleManager.getCurrentLocale();
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
        // Metrics panel
        metricsPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        metricsPanel.setBorder(BorderFactory.createTitledBorder(messages.getString("dashboard.keyMetrics")));
        
        totalProductsLabel = createMetricLabel(messages.getString("dashboard.totalProducts"), "0");
        lowStockCountLabel = createMetricLabel(messages.getString("dashboard.lowStockItems"), "0");
        inventoryValueLabel = createMetricLabel(messages.getString("dashboard.inventoryValue"), "DZD0.00");
        totalOrdersLabel = createMetricLabel(messages.getString("dashboard.totalOrders"), "0");
        pendingOrdersLabel = createMetricLabel(messages.getString("dashboard.pendingOrders"), "0");
        todayOrdersLabel = createMetricLabel(messages.getString("dashboard.todayOrders"), "0");
        todaySalesLabel = createMetricLabel(messages.getString("dashboard.todaySales"), "DZD0.00");
        recentSalesLabel = createMetricLabel(messages.getString("dashboard.recentSales"), "DZD0.00");
        
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
        chartsPanel.setBorder(BorderFactory.createTitledBorder(messages.getString("dashboard.analytics")));
        
        // Low stock panel
        lowStockPanel = new JPanel(new BorderLayout(5, 5));
        lowStockPanel.setBorder(BorderFactory.createTitledBorder(messages.getString("dashboard.lowStockItems")));
        
        String[] columnNames = {
            messages.getString("column.id"), 
            messages.getString("products.column.sku"), 
            messages.getString("products.column.name"), 
            messages.getString("products.column.category"), 
            messages.getString("products.column.supplier"), 
            messages.getString("products.column.stockQty"), 
            messages.getString("products.column.reorderLevel")
        };
        
        lowStockTableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        lowStockTable = new JTable(lowStockTableModel);
        lowStockTable.setFillsViewportHeight(true);
        lowStockTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Apply RTL to table if needed
        if (isRightToLeft) {
            lowStockTable.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            lowStockTable.getTableHeader().setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        
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
        
        // Add tab panel for additional analysis views
        JTabbedPane tabbedAnalytics = new JTabbedPane();
        tabbedAnalytics.addTab(messages.getString("dashboard.mainDashboard"), splitPane);
        tabbedAnalytics.addTab(messages.getString("dashboard.productAnalysis"), createProductAnalysisPanel());
        tabbedAnalytics.addTab(messages.getString("dashboard.orderAnalysis"), createOrderAnalysisPanel());
        
        // Apply RTL to tabbed pane if needed
        if (isRightToLeft) {
            tabbedAnalytics.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        
        add(tabbedAnalytics, BorderLayout.CENTER);
    }
    
    private JPanel createProductAnalysisPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        // Apply RTL if needed
        if (isRightToLeft) {
            panel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        
        // Create metrics panel for product statistics
        JPanel metricsPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        metricsPanel.setBorder(BorderFactory.createTitledBorder(messages.getString("dashboard.productMetrics")));
        
        Map<String, Object> priceStats = controller.getProductPriceStats();
        Map<String, Object> stockStats = controller.getProductStockStats();
        
        JLabel minPriceLabel = createMetricLabel(messages.getString("dashboard.minPrice"), 
            String.format("%.2f", priceStats.get("minPrice") != null ? 
                ((Number)priceStats.get("minPrice")).doubleValue() : 0.0));
                
        JLabel maxPriceLabel = createMetricLabel(messages.getString("dashboard.maxPrice"), 
            String.format("%.2f", priceStats.get("maxPrice") != null ? 
                ((Number)priceStats.get("maxPrice")).doubleValue() : 0.0));
                
        JLabel avgPriceLabel = createMetricLabel(messages.getString("dashboard.avgPrice"), 
            String.format("%.2f", priceStats.get("avgPrice") != null ? 
                ((Number)priceStats.get("avgPrice")).doubleValue() : 0.0));
                
        JLabel medianPriceLabel = createMetricLabel(messages.getString("dashboard.medianPrice"), 
            String.format("%.2f", priceStats.get("medianPrice") != null ? 
                ((Number)priceStats.get("medianPrice")).doubleValue() : 0.0));
        
        JLabel minStockLabel = createMetricLabel(messages.getString("dashboard.minStock"), 
            String.valueOf(stockStats.get("minStock") != null ? 
                ((Number)stockStats.get("minStock")).intValue() : 0));
                
        JLabel maxStockLabel = createMetricLabel(messages.getString("dashboard.maxStock"), 
            String.valueOf(stockStats.get("maxStock") != null ? 
                ((Number)stockStats.get("maxStock")).intValue() : 0));
                
        JLabel avgStockLabel = createMetricLabel(messages.getString("dashboard.avgStock"), 
            String.format("%.1f", stockStats.get("avgStock") != null ? 
                ((Number)stockStats.get("avgStock")).doubleValue() : 0.0));
                
        JLabel zeroStockLabel = createMetricLabel(messages.getString("dashboard.outOfStock"), 
            String.valueOf(stockStats.get("zeroStock") != null ? 
                ((Number)stockStats.get("zeroStock")).intValue() : 0) + " " + messages.getString("dashboard.products"));
        
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
        chartsPanel.setBorder(BorderFactory.createTitledBorder(messages.getString("dashboard.productAnalysis")));
        
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
        
        // Apply RTL if needed
        if (isRightToLeft) {
            panel.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        
        // Create metrics panel for order statistics
        JPanel metricsPanel = new JPanel(new GridLayout(2, 4, 10, 10));
        metricsPanel.setBorder(BorderFactory.createTitledBorder(messages.getString("dashboard.orderMetrics")));
        
        Map<String, Object> orderStats = controller.getOrderStats();
        Map<String, Object> dateRangeStats = controller.getOrdersByDateRange();
        
        JLabel totalOrdersLabel = createMetricLabel(messages.getString("dashboard.totalOrders"), 
            String.valueOf(orderStats.get("totalOrders") != null ? 
                ((Number)orderStats.get("totalOrders")).intValue() : 0));
                
        JLabel avgOrderValueLabel = createMetricLabel(messages.getString("dashboard.avgOrderValue"), 
            String.format("DZD %.2f", orderStats.get("avgOrderValue") != null ? 
                ((Number)orderStats.get("avgOrderValue")).doubleValue() : 0.0));
                
        JLabel maxOrderValueLabel = createMetricLabel(messages.getString("dashboard.maxOrderValue"), 
            String.format("DZD %.2f", orderStats.get("maxOrderValue") != null ? 
                ((Number)orderStats.get("maxOrderValue")).doubleValue() : 0.0));
                
        JLabel totalRevenueLabel = createMetricLabel(messages.getString("dashboard.totalRevenue"), 
            String.format("DZD %.2f", orderStats.get("totalRevenue") != null ? 
                ((Number)orderStats.get("totalRevenue")).doubleValue() : 0.0));
        
        JLabel todayOrdersLabel = createMetricLabel(messages.getString("dashboard.todayOrders"), 
            String.valueOf(dateRangeStats.get("todayCount") != null ? 
                ((Number)dateRangeStats.get("todayCount")).intValue() : 0) + 
            " (DZD" + String.format("%.2f", dateRangeStats.get("todayTotal") != null ? 
                ((Number)dateRangeStats.get("todayTotal")).doubleValue() : 0.0) + ")");
                
        JLabel weekOrdersLabel = createMetricLabel(messages.getString("dashboard.weekOrders"), 
            String.valueOf(dateRangeStats.get("weekCount") != null ? 
                ((Number)dateRangeStats.get("weekCount")).intValue() : 0) + 
            " (DZD" + String.format("%.2f", dateRangeStats.get("weekTotal") != null ? 
                ((Number)dateRangeStats.get("weekTotal")).doubleValue() : 0.0) + ")");
                
        JLabel monthOrdersLabel = createMetricLabel(messages.getString("dashboard.monthOrders"), 
            String.valueOf(dateRangeStats.get("monthCount") != null ? 
                ((Number)dateRangeStats.get("monthCount")).intValue() : 0) + 
            " (DZD" + String.format("%.2f", dateRangeStats.get("monthTotal") != null ? 
                ((Number)dateRangeStats.get("monthTotal")).doubleValue() : 0.0) + ")");
                
        JLabel avgItemsLabel = createMetricLabel(messages.getString("dashboard.avgItemsPerOrder"), 
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
        chartsPanel.setBorder(BorderFactory.createTitledBorder(messages.getString("dashboard.orderAnalysis")));
        
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
        
        totalProductsLabel.setText(formatMetric(messages.getString("dashboard.totalProducts"), metrics.get("totalProducts")));
        lowStockCountLabel.setText(formatMetric(messages.getString("dashboard.lowStockItems"), metrics.get("lowStockCount")));
        
        Double invValue = metrics.get("inventoryValue") != null ? 
                         ((Number)metrics.get("inventoryValue")).doubleValue() : 0.0;
        inventoryValueLabel.setText(formatMetric(messages.getString("dashboard.inventoryValue"), String.format("DZD %.2f", invValue)));
        
        totalOrdersLabel.setText(formatMetric(messages.getString("dashboard.totalOrders"), metrics.get("totalOrders")));
        pendingOrdersLabel.setText(formatMetric(messages.getString("dashboard.pendingOrders"), metrics.get("pendingOrders")));
        todayOrdersLabel.setText(formatMetric(messages.getString("dashboard.todayOrders"), metrics.get("todayOrders")));
        
        Double todaySales = metrics.get("todaySales") != null ? 
                           ((Number)metrics.get("todaySales")).doubleValue() : 0.0;
        todaySalesLabel.setText(formatMetric(messages.getString("dashboard.todaySales"), String.format("DZD %.2f", todaySales)));
        
        Double recentSales = metrics.get("recentSales") != null ? 
                            ((Number)metrics.get("recentSales")).doubleValue() : 0.0;
        recentSalesLabel.setText(formatMetric(messages.getString("dashboard.recentSales"), String.format("DZD %.2f", recentSales)));
        
        // Update low stock table
        refreshLowStockTable();
        
        // Update charts
        refreshCharts();

        // Remove and re-create the analysis tabs to refresh them
        JTabbedPane tabbedAnalytics = (JTabbedPane) getComponent(0);
        tabbedAnalytics.removeTabAt(2); // Order Analysis tab
        tabbedAnalytics.removeTabAt(1); // Product Analysis tab
        
        tabbedAnalytics.addTab(messages.getString("dashboard.productAnalysis"), createProductAnalysisPanel());
        tabbedAnalytics.addTab(messages.getString("dashboard.orderAnalysis"), createOrderAnalysisPanel());        
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
        // Direct HTML text alignment for RTL support
        String textAlign = isRightToLeft ? "right" : "center";
        
        JLabel label = new JLabel("<html><div style='text-align: " + textAlign + ";'>" +
                                  "<b>" + title + "</b><br>" +
                                  "<font size='+1'>" + value + "</font></div></html>", 
                                  JLabel.CENTER);
        
        // Apply RTL if needed
        if (isRightToLeft) {
            label.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        
        label.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Color.LIGHT_GRAY),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        return label;
    }
    
    private String formatMetric(String title, Object value) {
        // Direct HTML text alignment for RTL support
        String textAlign = isRightToLeft ? "right" : "center";
        
        return "<html><div style='text-align: " + textAlign + ";'>" +
               "<b>" + title + "</b><br>" +
               "<font size='+1'>" + (value != null ? value.toString() : "0") + "</font></div></html>";
    }
}