package controller;

import model.InventoryAdjustment;
import model.Product;
import util.DBConnection;
import util.DataUtil;
import util.DataUtil.ResultSetMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class InventoryAdjustmentController {
    
    private ProductController productController;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    private SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public InventoryAdjustmentController() {
        this.productController = new ProductController();
    }
    
    /**
     * Get all inventory adjustments with product names
     */
    public List<InventoryAdjustment> getAllAdjustments() {
        String sql = "SELECT a.*, p.name as product_name FROM InventoryAdjustment a " +
                     "JOIN Product p ON a.product_id = p.id " +
                     "ORDER BY a.date DESC";
        
        return queryAdjustments(sql);
    }
    
    /**
     * Get inventory adjustments for a specific product
     */
    public List<InventoryAdjustment> getAdjustmentsByProduct(int productId) {
        String sql = "SELECT a.*, p.name as product_name FROM InventoryAdjustment a " +
                     "JOIN Product p ON a.product_id = p.id " +
                     "WHERE a.product_id = ? " +
                     "ORDER BY a.date DESC";
        
        return queryAdjustments(sql, productId);
    }
    
    /**
     * Get adjustments within a date range
     */
    public List<InventoryAdjustment> getAdjustmentsByDateRange(Date startDate, Date endDate) {
        String sql = "SELECT a.*, p.name as product_name FROM InventoryAdjustment a " +
                     "JOIN Product p ON a.product_id = p.id " +
                     "WHERE a.date BETWEEN ? AND ? " +
                     "ORDER BY a.date DESC";
        
        return queryAdjustments(sql, startDate, endDate);
    }
    
    /**
     * Search adjustments with advanced filtering
     */
    public List<InventoryAdjustment> searchAdjustments(Integer productId, Date startDate, Date endDate, 
                                                    String reason, Integer minQty, Integer maxQty) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();
        
        sql.append("SELECT a.*, p.name as product_name FROM InventoryAdjustment a ");
        sql.append("JOIN Product p ON a.product_id = p.id WHERE 1=1 ");
        
        if (productId != null && productId > 0) {
            sql.append("AND a.product_id = ? ");
            params.add(productId);
        }
        
        
        
        if (startDate != null) {
            // Convert to start of day for proper comparison
            Calendar cal = Calendar.getInstance();
            cal.setTime(startDate);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            startDate = cal.getTime();
            String startDateStr = dateFormat.format(startDate);
            sql.append("AND date(a.date) >= date(?) ");
            params.add(startDateStr);
            System.out.println("DEBUG - Using start date string: " + startDateStr);
        }

        if (endDate != null) {
            // Convert to end of day for proper comparison
            Calendar cal = Calendar.getInstance();
            cal.setTime(endDate);
            cal.set(Calendar.HOUR_OF_DAY, 23);
            cal.set(Calendar.MINUTE, 59);
            cal.set(Calendar.SECOND, 59);
            endDate = cal.getTime();
            
            String endDateStr = dateFormat.format(endDate);
            sql.append("AND date(a.date) <= date(?) ");
            params.add(endDateStr);
            System.out.println("DEBUG - Using end date string: " + endDateStr);
        }
        
        if (reason != null && !reason.trim().isEmpty()) {
            sql.append("AND a.reason LIKE ? ");
            params.add("%" + reason + "%");
        }
        
        if (minQty != null) {
            sql.append("AND a.change_qty >= ? ");
            params.add(minQty);
        }
        
        if (maxQty != null) {
            sql.append("AND a.change_qty <= ? ");
            params.add(maxQty);
        }
        
        sql.append("ORDER BY a.date DESC");
        // At the end of the searchAdjustments method, before the return statement
        System.out.println("DEBUG - SQL Query: " + sql.toString());
        System.out.println("DEBUG - SQL Params: " + params);
        return queryAdjustments(sql.toString(), params.toArray());
    }
    
    /**
     * Get an adjustment by ID
     */
    public InventoryAdjustment getAdjustmentById(int id) {
        String sql = "SELECT a.*, p.name as product_name FROM InventoryAdjustment a " +
                     "JOIN Product p ON a.product_id = p.id " +
                     "WHERE a.id = ?";
        
        List<InventoryAdjustment> adjustments = queryAdjustments(sql, id);
        return adjustments.isEmpty() ? null : adjustments.get(0);
    }
    
    /**
     * Save an adjustment (insert or update)
     */
    public boolean saveAdjustment(InventoryAdjustment adjustment) {
        boolean success = false;
        
        try {
            // Begin transaction
            DataUtil.beginTransaction();
            
            if (adjustment.getId() > 0) {
                // Get old adjustment to calculate inventory difference
                InventoryAdjustment oldAdjustment = getAdjustmentById(adjustment.getId());
                int qtyDifference = adjustment.getChangeQty() - oldAdjustment.getChangeQty();
                
                // Update inventory
                if (qtyDifference != 0) {
                    updateProductStock(adjustment.getProductId(), qtyDifference);
                }
                
                // Update adjustment record
                success = DataUtil.update("InventoryAdjustment", adjustment, "id");
            } else {
                // Update inventory for new adjustment
                updateProductStock(adjustment.getProductId(), adjustment.getChangeQty());
                
                // Insert new adjustment record
                int id = DataUtil.insert("InventoryAdjustment", adjustment, "id");
                if (id > 0) {
                    adjustment.setId(id);
                    success = true;
                }
            }
            
            // Commit transaction
            if (success) {
                DataUtil.commitTransaction();
            } else {
                DataUtil.rollbackTransaction();
            }
        } catch (SQLException e) {
            try {
                DataUtil.rollbackTransaction();
            } catch (SQLException ex) {
                System.err.println("Error rolling back transaction: " + ex.getMessage());
            }
            System.err.println("Error saving adjustment: " + e.getMessage());
            e.printStackTrace();
        }
        
        return success;
    }
    
    /**
     * Delete an adjustment
     */
    public boolean deleteAdjustment(int id) {
        boolean success = false;
        
        try {
            // Begin transaction
            DataUtil.beginTransaction();
            
            // Get the adjustment to reverse its effect on inventory
            InventoryAdjustment adjustment = getAdjustmentById(id);
            if (adjustment != null) {
                // Reverse the inventory change
                updateProductStock(adjustment.getProductId(), -adjustment.getChangeQty());
                
                // Delete the adjustment record
                success = DataUtil.delete("InventoryAdjustment", id, "id");
            }
            
            // Commit or rollback transaction
            if (success) {
                DataUtil.commitTransaction();
            } else {
                DataUtil.rollbackTransaction();
            }
        } catch (SQLException e) {
            try {
                DataUtil.rollbackTransaction();
            } catch (SQLException ex) {
                System.err.println("Error rolling back transaction: " + ex.getMessage());
            }
            System.err.println("Error deleting adjustment: " + e.getMessage());
            e.printStackTrace();
        }
        
        return success;
    }
    
    /**
     * Update product stock quantity
     */
    private void updateProductStock(int productId, int changeQty) throws SQLException {
        String sql = "UPDATE Product SET stock_qty = stock_qty + ? WHERE id = ?";
        
        Connection conn = DBConnection.getConnection();
        PreparedStatement stmt = conn.prepareStatement(sql);
        stmt.setInt(1, changeQty);
        stmt.setInt(2, productId);
        stmt.executeUpdate();
        stmt.close();
    }
    
    /**
     * Get all products for dropdown
     */
    public List<Product> getAllProducts() {
        return productController.getAllProducts();
    }
    
    /**
     * Helper method to query adjustments
     */
    private List<InventoryAdjustment> queryAdjustments(String sql, Object... params) {

        return DataUtil.query(sql, new ResultSetMapper<InventoryAdjustment>() {
            @Override
            public InventoryAdjustment map(ResultSet rs) throws SQLException {
                InventoryAdjustment adjustment = new InventoryAdjustment();
                adjustment.setId(rs.getInt("id"));
                adjustment.setProductId(rs.getInt("product_id"));
                
                // Fix for the timestamp parsing issue
                try {
                    // After getting the date string
                    String dateStr = rs.getString("date");
                    System.out.println("DEBUG - Date from database: " + dateStr);

                    if (dateStr != null) {
                        try {
                            // Try parsing with dateTimeFormat first
                            adjustment.setDate(dateTimeFormat.parse(dateStr));
                        } catch (ParseException e) {
                            try {
                                // If that fails, try with simple date format
                                adjustment.setDate(dateFormat.parse(dateStr));
                            } catch (ParseException e2) {
                                // If both fail, default to current date
                                adjustment.setDate(new Date());
                                System.err.println("Error parsing date: " + dateStr + " - " + e2.getMessage());
                            }
                        }
                    } else {
                        adjustment.setDate(new Date());
                    }
                } catch (SQLException e) {
                    adjustment.setDate(new Date());
                    System.err.println("Error getting date from database: " + e.getMessage());
                }
                
                adjustment.setChangeQty(rs.getInt("change_qty"));
                adjustment.setReason(rs.getString("reason"));
                
                // Add product name from join
                adjustment.setProductName(rs.getString("product_name"));
                
                return adjustment;
            }
        }, params);
    }
}