package controller;

import model.Product;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import org.jfree.data.time.Day;
import org.jfree.data.time.TimeSeries;
import org.jfree.data.time.TimeSeriesCollection;
import util.DataUtil;
import util.DataUtil.ResultSetMapper;
import org.jfree.data.statistics.HistogramDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DashboardController {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd");
    
    /**
     * Get stock metrics for dashboard
     */
    public Map<String, Object> getStockMetrics() {
        Map<String, Object> metrics = new HashMap<>();
        
        // Total products
        String productCountSql = "SELECT COUNT(*) FROM Product";
        metrics.put("totalProducts", DataUtil.queryScalar(productCountSql));
        
        // Low stock products count
        String lowStockSql = "SELECT COUNT(*) FROM Product WHERE stock_qty <= reorder_level";
        metrics.put("lowStockCount", DataUtil.queryScalar(lowStockSql));
        
        // Total inventory value
        String inventoryValueSql = "SELECT SUM(stock_qty * unit_price) FROM Product";
        metrics.put("inventoryValue", DataUtil.queryScalar(inventoryValueSql));
        
        // Orders metrics
        String totalOrdersSql = "SELECT COUNT(*) FROM \"Order\"";
        metrics.put("totalOrders", DataUtil.queryScalar(totalOrdersSql));
        
        String pendingOrdersSql = "SELECT COUNT(*) FROM \"Order\" WHERE status IN ('New', 'Processing')";
        metrics.put("pendingOrders", DataUtil.queryScalar(pendingOrdersSql));
        
        String todayOrdersSql = "SELECT COUNT(*) FROM \"Order\" WHERE date(order_date) = date('now')";
        metrics.put("todayOrders", DataUtil.queryScalar(todayOrdersSql));
        
        String todaySalesSql = "SELECT SUM(total_amount) FROM \"Order\" WHERE date(order_date) = date('now')";
        metrics.put("todaySales", DataUtil.queryScalar(todaySalesSql));
        
        // Recent sales
        String recentSalesSql = "SELECT SUM(total_amount) FROM \"Order\" WHERE order_date >= date('now', '-30 days')";
        metrics.put("recentSales", DataUtil.queryScalar(recentSalesSql));
        
        return metrics;
    }
    
    /**
     * Get low stock products
     */
    public List<Product> getLowStockProducts() {
        String sql = "SELECT p.*, c.name as category_name, s.name as supplier_name " +
                     "FROM Product p " +
                     "LEFT JOIN Category c ON p.category_id = c.id " +
                     "LEFT JOIN Supplier s ON p.supplier_id = s.id " +
                     "WHERE p.stock_qty <= p.reorder_level " +
                     "ORDER BY (p.reorder_level - p.stock_qty) DESC";
        
        return DataUtil.query(sql, new ResultSetMapper<Product>() {
            @Override
            public Product map(ResultSet rs) throws SQLException {
                Product product = new Product();
                product.setId(rs.getInt("id"));
                product.setSku(rs.getString("sku"));
                product.setName(rs.getString("name"));
                product.setCategoryId(rs.getInt("category_id"));
                product.setSupplierId(rs.getInt("supplier_id"));
                product.setUnitPrice(rs.getDouble("unit_price"));
                product.setStockQty(rs.getInt("stock_qty"));
                product.setReorderLevel(rs.getInt("reorder_level"));
                
                // Set the joined fields
                product.setCategoryName(rs.getString("category_name"));
                product.setSupplierName(rs.getString("supplier_name"));
                
                return product;
            }
        });
    }
    
    /**
     * Create a sales over time line chart
     */
    public JFreeChart createSalesChart(int days) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Get sales data for the last 'days' days
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        // Go back 'days' days
        cal.add(Calendar.DAY_OF_MONTH, -days);
        
        // For each day, add a data point
        for (int i = 0; i <= days; i++) {
            Date date = cal.getTime();
            String dateStr = DATE_FORMAT.format(date);
            
            String sql = "SELECT SUM(total_amount) FROM \"Order\" WHERE date(order_date) = date(?)";
            Object result = DataUtil.queryScalar(sql, dateStr);
            double sales = (result == null) ? 0.0 : ((Number) result).doubleValue();
            
            dataset.addValue(sales, "Sales", dateStr);
            
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        return ChartFactory.createLineChart(
                "Sales Over Time",
                "Date",
                "Amount",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
    }
    
    /**
     * Create a stock levels by category bar chart
     */
    public JFreeChart createStockByCategory() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        String sql = "SELECT c.name, SUM(p.stock_qty) as total_qty " +
                     "FROM Product p " +
                     "JOIN Category c ON p.category_id = c.id " +
                     "GROUP BY c.name " +
                     "ORDER BY total_qty DESC";
        
        List<Map<String, Object>> results = DataUtil.query(sql, rs -> {
            Map<String, Object> result = new HashMap<>();
            result.put("name", rs.getString("name"));
            result.put("total_qty", rs.getInt("total_qty"));
            return result;
        });
        
        for (Map<String, Object> result : results) {
            dataset.addValue(
                    ((Number) result.get("total_qty")).doubleValue(),
                    "Stock Qty",
                    (String) result.get("name")
            );
        }
        
        return ChartFactory.createBarChart(
                "Stock Levels by Category",
                "Category",
                "Quantity",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
    }
    
    /**
     * Create a pie chart of top selling products
     */
    public JFreeChart createTopProductsChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        
        String sql = "SELECT p.name, SUM(oi.quantity) as total_qty " +
                     "FROM OrderItem oi " +
                     "JOIN Product p ON oi.product_id = p.id " +
                     "GROUP BY p.name " +
                     "ORDER BY total_qty DESC " +
                     "LIMIT 5";
        
        List<Map<String, Object>> results = DataUtil.query(sql, rs -> {
            Map<String, Object> result = new HashMap<>();
            result.put("name", rs.getString("name"));
            result.put("total_qty", rs.getInt("total_qty"));
            return result;
        });
        
        for (Map<String, Object> result : results) {
            dataset.setValue(
                    (String) result.get("name"),
                    ((Number) result.get("total_qty")).doubleValue()
            );
        }
        
        return ChartFactory.createPieChart(
                "Top 5 Products by Sales Quantity",
                dataset,
                true,
                true,
                false
        );
    }
    
    /**
     * Create an inventory trend chart showing stock level changes over time
     */
    public JFreeChart createInventoryTrendChart(int days) {
        TimeSeriesCollection dataset = new TimeSeriesCollection();
        TimeSeries series = new TimeSeries("Inventory Level");
        
        // Get inventory adjustment history for the past 'days' days
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        
        // Go back 'days' days
        cal.add(Calendar.DAY_OF_MONTH, -days);
        
        // Get current total stock level
        String currentStockSql = "SELECT SUM(stock_qty) FROM Product";
        Object currentStock = DataUtil.queryScalar(currentStockSql);
        double totalStock = (currentStock == null) ? 0.0 : ((Number) currentStock).doubleValue();
        
        // For each day, add a data point based on inventory adjustments
        for (int i = 0; i <= days; i++) {
            Date date = cal.getTime();
            
            // Get sum of adjustments for this day
            String sql = "SELECT SUM(change_qty) FROM InventoryAdjustment WHERE date(date) = date(?)";
            Object result = DataUtil.queryScalar(sql, DATE_FORMAT.format(date));
            double change = (result == null) ? 0.0 : ((Number) result).doubleValue();
            
            // Add to series
            series.add(new Day(date), totalStock);
            
            // Adjust totalStock for next day's calculation
            totalStock -= change;
            
            cal.add(Calendar.DAY_OF_MONTH, 1);
        }
        
        dataset.addSeries(series);
        
        return ChartFactory.createTimeSeriesChart(
                "Inventory Level Trend",
                "Date",
                "Total Stock Quantity",
                dataset,
                true,
                true,
                false
        );
    }
    
    /**
     * Create a chart showing orders by status
     */
    public JFreeChart createOrdersByStatusChart() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        
        String sql = "SELECT status, COUNT(*) as count FROM \"Order\" GROUP BY status";
        
        List<Map<String, Object>> results = DataUtil.query(sql, rs -> {
            Map<String, Object> result = new HashMap<>();
            result.put("status", rs.getString("status"));
            result.put("count", rs.getInt("count"));
            return result;
        });
        
        for (Map<String, Object> result : results) {
            dataset.setValue(
                    (String) result.get("status"),
                    ((Number) result.get("count")).doubleValue()
            );
        }
        
        return ChartFactory.createPieChart(
                "Orders by Status",
                dataset,
                true,
                true,
                false
        );
    }
    
    /**
     * Create a chart showing sales by supplier
     */
    public JFreeChart createSalesBySupplierChart() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        String sql = "SELECT s.name as supplier, SUM(oi.quantity * oi.unit_price) as total " +
                    "FROM OrderItem oi " +
                    "JOIN Product p ON oi.product_id = p.id " +
                    "JOIN Supplier s ON p.supplier_id = s.id " +
                    "GROUP BY s.name " +
                    "ORDER BY total DESC";
        
        List<Map<String, Object>> results = DataUtil.query(sql, rs -> {
            Map<String, Object> result = new HashMap<>();
            result.put("supplier", rs.getString("supplier"));
            result.put("total", rs.getDouble("total"));
            return result;
        });
        
        for (Map<String, Object> result : results) {
            dataset.addValue(
                    ((Number) result.get("total")).doubleValue(),
                    "Sales Amount",
                    (String) result.get("supplier")
            );
        }
        
        return ChartFactory.createBarChart(
                "Sales by Supplier",
                "Supplier",
                "Total Sales",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
    }

    /**
     * Get product price statistics
     */
    public Map<String, Object> getProductPriceStats() {
        Map<String, Object> stats = new HashMap<>();
        
        String minPriceSql = "SELECT MIN(unit_price) FROM Product";
        stats.put("minPrice", DataUtil.queryScalar(minPriceSql));
        
        String maxPriceSql = "SELECT MAX(unit_price) FROM Product";
        stats.put("maxPrice", DataUtil.queryScalar(maxPriceSql));
        
        String avgPriceSql = "SELECT AVG(unit_price) FROM Product";
        stats.put("avgPrice", DataUtil.queryScalar(avgPriceSql));
        
        // Median price calculation in SQLite is more complex
        String medianPriceSql = "SELECT unit_price FROM Product ORDER BY unit_price LIMIT 1 OFFSET (SELECT COUNT(*) FROM Product) / 2";
        stats.put("medianPrice", DataUtil.queryScalar(medianPriceSql));
        
        return stats;
    }
    
    /**
     * Get product stock statistics
     */
    public Map<String, Object> getProductStockStats() {
        Map<String, Object> stats = new HashMap<>();
        
        String minStockSql = "SELECT MIN(stock_qty) FROM Product";
        stats.put("minStock", DataUtil.queryScalar(minStockSql));
        
        String maxStockSql = "SELECT MAX(stock_qty) FROM Product";
        stats.put("maxStock", DataUtil.queryScalar(maxStockSql));
        
        String avgStockSql = "SELECT AVG(stock_qty) FROM Product";
        stats.put("avgStock", DataUtil.queryScalar(avgStockSql));
        
        String totalStockSql = "SELECT SUM(stock_qty) FROM Product";
        stats.put("totalStock", DataUtil.queryScalar(totalStockSql));
        
        String zeroStockSql = "SELECT COUNT(*) FROM Product WHERE stock_qty = 0";
        stats.put("zeroStock", DataUtil.queryScalar(zeroStockSql));
        
        return stats;
    }
    
    /**
     * Create a histogram chart of product prices
     */
    public JFreeChart createProductPriceHistogram() {
        HistogramDataset dataset = new HistogramDataset();
        
        String sql = "SELECT unit_price FROM Product";
        List<Double> prices = DataUtil.query(sql, rs -> rs.getDouble("unit_price"));
        
        double[] priceArray = new double[prices.size()];
        for (int i = 0; i < prices.size(); i++) {
            priceArray[i] = prices.get(i);
        }
        
        dataset.addSeries("Unit Price", priceArray, 10);
        
        return ChartFactory.createHistogram(
                "Product Price Distribution",
                "Price Range",
                "Frequency",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
    }
    
    /**
     * Create a chart showing product count by category
     */
    public JFreeChart createProductsByCategory() {
        DefaultPieDataset dataset = new DefaultPieDataset();
        
        String sql = "SELECT c.name, COUNT(*) as count FROM Product p " +
                     "JOIN Category c ON p.category_id = c.id " +
                     "GROUP BY c.name ORDER BY count DESC";
        
        List<Map<String, Object>> results = DataUtil.query(sql, rs -> {
            Map<String, Object> result = new HashMap<>();
            result.put("name", rs.getString("name"));
            result.put("count", rs.getInt("count"));
            return result;
        });
        
        for (Map<String, Object> result : results) {
            dataset.setValue(
                    (String) result.get("name"),
                    ((Number) result.get("count")).doubleValue()
            );
        }
        
        return ChartFactory.createPieChart(
                "Products by Category",
                dataset,
                true,
                true,
                false
        );
    }
    
    /**
     * Create a chart showing product count by supplier
     */
    public JFreeChart createProductsBySupplier() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        String sql = "SELECT s.name, COUNT(*) as count FROM Product p " +
                     "JOIN Supplier s ON p.supplier_id = s.id " +
                     "GROUP BY s.name ORDER BY count DESC";
        
        List<Map<String, Object>> results = DataUtil.query(sql, rs -> {
            Map<String, Object> result = new HashMap<>();
            result.put("name", rs.getString("name"));
            result.put("count", rs.getInt("count"));
            return result;
        });
        
        for (Map<String, Object> result : results) {
            dataset.addValue(
                    ((Number) result.get("count")).doubleValue(),
                    "Product Count",
                    (String) result.get("name")
            );
        }
        
        return ChartFactory.createBarChart(
                "Products by Supplier",
                "Supplier",
                "Count",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
    }
    
    /**
     * Create a scatter plot of product price vs. stock quantity
     */
    public JFreeChart createPriceVsStockChart() {
        XYSeriesCollection dataset = new XYSeriesCollection();
        XYSeries series = new XYSeries("Products");
        
        String sql = "SELECT name, unit_price, stock_qty FROM Product";
        
        List<Product> products = DataUtil.query(sql, rs -> {
            Product product = new Product();
            product.setName(rs.getString("name"));
            product.setUnitPrice(rs.getDouble("unit_price"));
            product.setStockQty(rs.getInt("stock_qty"));
            return product;
        });
        
        for (Product product : products) {
            series.add(product.getUnitPrice(), product.getStockQty());
        }
        
        dataset.addSeries(series);
        
        return ChartFactory.createScatterPlot(
                "Price vs. Stock Quantity",
                "Unit Price",
                "Stock Quantity",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
    }
    
    /**
     * Get order statistics for the Order Analysis tab
     */
    public Map<String, Object> getOrderStats() {
        Map<String, Object> stats = new HashMap<>();
        
        String totalOrdersSql = "SELECT COUNT(*) FROM \"Order\"";
        stats.put("totalOrders", DataUtil.queryScalar(totalOrdersSql));
        
        String avgOrderValueSql = "SELECT AVG(total_amount) FROM \"Order\"";
        stats.put("avgOrderValue", DataUtil.queryScalar(avgOrderValueSql));
        
        String maxOrderValueSql = "SELECT MAX(total_amount) FROM \"Order\"";
        stats.put("maxOrderValue", DataUtil.queryScalar(maxOrderValueSql));
        
        String totalRevenueSql = "SELECT SUM(total_amount) FROM \"Order\"";
        stats.put("totalRevenue", DataUtil.queryScalar(totalRevenueSql));
        
        String avgItemsPerOrderSql = "SELECT AVG(item_count) FROM " +
                                    "(SELECT order_id, COUNT(*) as item_count FROM OrderItem GROUP BY order_id)";
        stats.put("avgItemsPerOrder", DataUtil.queryScalar(avgItemsPerOrderSql));
        
        return stats;
    }
    
    /**
     * Get orders by date range
     */
    public Map<String, Object> getOrdersByDateRange() {
        Map<String, Object> stats = new HashMap<>();
        
        String todayOrdersSql = "SELECT COUNT(*), SUM(total_amount) FROM \"Order\" WHERE date(order_date) = date('now')";
        List<Map<String, Object>> todayResults = DataUtil.query(todayOrdersSql, rs -> {
            Map<String, Object> result = new HashMap<>();
            result.put("count", rs.getInt(1));
            result.put("total", rs.getDouble(2));
            return result;
        });
        
        if (!todayResults.isEmpty()) {
            stats.put("todayCount", todayResults.get(0).get("count"));
            stats.put("todayTotal", todayResults.get(0).get("total"));
        }
        
        String weekOrdersSql = "SELECT COUNT(*), SUM(total_amount) FROM \"Order\" WHERE order_date >= date('now', '-7 days')";
        List<Map<String, Object>> weekResults = DataUtil.query(weekOrdersSql, rs -> {
            Map<String, Object> result = new HashMap<>();
            result.put("count", rs.getInt(1));
            result.put("total", rs.getDouble(2));
            return result;
        });
        
        if (!weekResults.isEmpty()) {
            stats.put("weekCount", weekResults.get(0).get("count"));
            stats.put("weekTotal", weekResults.get(0).get("total"));
        }
        
        String monthOrdersSql = "SELECT COUNT(*), SUM(total_amount) FROM \"Order\" WHERE order_date >= date('now', '-30 days')";
        List<Map<String, Object>> monthResults = DataUtil.query(monthOrdersSql, rs -> {
            Map<String, Object> result = new HashMap<>();
            result.put("count", rs.getInt(1));
            result.put("total", rs.getDouble(2));
            return result;
        });
        
        if (!monthResults.isEmpty()) {
            stats.put("monthCount", monthResults.get(0).get("count"));
            stats.put("monthTotal", monthResults.get(0).get("total"));
        }
        
        return stats;
    }
    
    /**
     * Create a chart showing orders by customer
     */
    public JFreeChart createOrdersByCustomer() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        String sql = "SELECT c.name, COUNT(*) as order_count " +
                    "FROM \"Order\" o " +
                    "JOIN Customer c ON o.customer_id = c.id " +
                    "GROUP BY c.name " +
                    "ORDER BY order_count DESC " +
                    "LIMIT 10";
        
        List<Map<String, Object>> results = DataUtil.query(sql, rs -> {
            Map<String, Object> result = new HashMap<>();
            result.put("name", rs.getString("name"));
            result.put("count", rs.getInt("order_count"));
            return result;
        });
        
        for (Map<String, Object> result : results) {
            dataset.addValue(
                    ((Number) result.get("count")).doubleValue(),
                    "Order Count",
                    (String) result.get("name")
            );
        }
        
        return ChartFactory.createBarChart(
                "Top 10 Customers by Order Count",
                "Customer",
                "Orders",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
    }
    
    /**
     * Create a chart showing order value distribution
     */
    public JFreeChart createOrderValueDistribution() {
        HistogramDataset dataset = new HistogramDataset();
        
        String sql = "SELECT total_amount FROM \"Order\"";
        List<Double> amounts = DataUtil.query(sql, rs -> rs.getDouble("total_amount"));
        
        double[] amountArray = new double[amounts.size()];
        for (int i = 0; i < amounts.size(); i++) {
            amountArray[i] = amounts.get(i);
        }
        
        dataset.addSeries("Order Value", amountArray, 10);
        
        return ChartFactory.createHistogram(
                "Order Value Distribution",
                "Order Value",
                "Frequency",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
    }
    
    /**
     * Create a chart showing orders by status over time
     */
    public JFreeChart createOrderStatusTrend() {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        
        // Get the last 6 months
        String sql = "SELECT strftime('%Y-%m', order_date) as month, status, COUNT(*) as count " +
                    "FROM \"Order\" " +
                    "WHERE order_date >= date('now', '-6 months') " +
                    "GROUP BY month, status " +
                    "ORDER BY month";
        
        List<Map<String, Object>> results = DataUtil.query(sql, rs -> {
            Map<String, Object> result = new HashMap<>();
            result.put("month", rs.getString("month"));
            result.put("status", rs.getString("status"));
            result.put("count", rs.getInt("count"));
            return result;
        });
        
        for (Map<String, Object> result : results) {
            dataset.addValue(
                    ((Number) result.get("count")).doubleValue(),
                    (String) result.get("status"),
                    (String) result.get("month")
            );
        }
        
        return ChartFactory.createLineChart(
                "Order Status Trend",
                "Month",
                "Count",
                dataset,
                PlotOrientation.VERTICAL,
                true,
                true,
                false
        );
    }


}