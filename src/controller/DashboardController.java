package controller;

import model.Product;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.general.DefaultPieDataset;
import util.DataUtil;
import util.DataUtil.ResultSetMapper;

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
}