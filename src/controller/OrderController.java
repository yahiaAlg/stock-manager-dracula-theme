package controller;

import model.Order;
import model.OrderItem;
import util.DBConnection;
import util.DataUtil;
import util.DataUtil.ResultSetMapper;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class OrderController {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Get all orders with customer info
     */
    public List<Order> getAllOrders() {
        String sql = "SELECT o.*, c.name as customer_name " +
                     "FROM \"Order\" o " +
                     "LEFT JOIN Customer c ON o.customer_id = c.id " +
                     "ORDER BY o.order_date DESC";
        
        return DataUtil.query(sql, new ResultSetMapper<Order>() {
            @Override
            public Order map(ResultSet rs) throws SQLException {
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setCustomerId(rs.getInt("customer_id"));
                
                try {
                    order.setOrderDate(DATE_FORMAT.parse(rs.getString("order_date")));
                } catch (Exception e) {
                    order.setOrderDate(new Date());
                }
                
                order.setTotalAmount(rs.getDouble("total_amount"));
                order.setStatus(rs.getString("status"));
                order.setCustomerName(rs.getString("customer_name"));
                
                return order;
            }
        });
    }
    
    /**
     * Search orders by criteria
     */
    public List<Order> searchOrders(Integer customerId, String status, Date startDate, Date endDate, String searchTerm) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();
        
        // We need to join with OrderItem and Product tables to search by product name and category
        sql.append("SELECT DISTINCT o.id, o.customer_id, o.order_date, o.total_amount, o.status, c.name as customer_name ");
        sql.append("FROM \"Order\" o ");
        sql.append("LEFT JOIN Customer c ON o.customer_id = c.id ");
        sql.append("LEFT JOIN OrderItem oi ON o.id = oi.order_id ");
        sql.append("LEFT JOIN Product p ON oi.product_id = p.id ");
        sql.append("LEFT JOIN Category cat ON p.category_id = cat.id ");
        
        // Build WHERE clause
        boolean hasWhere = false;
        
        if (customerId != null && customerId > 0) {
            sql.append("WHERE o.customer_id = ? ");
            params.add(customerId);
            hasWhere = true;
        }
        
        if (status != null && !status.isEmpty()) {
            if (hasWhere) {
                sql.append("AND o.status = ? ");
            } else {
                sql.append("WHERE o.status = ? ");
                hasWhere = true;
            }
            params.add(status);
        }
        
        if (startDate != null) {
            if (hasWhere) {
                sql.append("AND o.order_date >= ? ");
            } else {
                sql.append("WHERE o.order_date >= ? ");
                hasWhere = true;
            }
            params.add(DATE_FORMAT.format(startDate));
        }
        
        if (endDate != null) {
            if (hasWhere) {
                sql.append("AND o.order_date <= ? ");
            } else {
                sql.append("WHERE o.order_date <= ? ");
                hasWhere = true;
            }
            params.add(DATE_FORMAT.format(endDate));
        }
        
        // Add search term for customer name, product name, or product category
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            String likeParam = "%" + searchTerm.trim() + "%";
            if (hasWhere) {
                sql.append("AND (c.name LIKE ? OR p.name LIKE ? OR cat.name LIKE ?) ");
            } else {
                sql.append("WHERE (c.name LIKE ? OR p.name LIKE ? OR cat.name LIKE ?) ");
                hasWhere = true;
            }
            params.add(likeParam);
            params.add(likeParam);
            params.add(likeParam);
        }
        
        sql.append("ORDER BY o.order_date DESC");
        
        return DataUtil.query(sql.toString(), new ResultSetMapper<Order>() {
            @Override
            public Order map(ResultSet rs) throws SQLException {
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setCustomerId(rs.getInt("customer_id"));
                
                try {
                    order.setOrderDate(DATE_FORMAT.parse(rs.getString("order_date")));
                } catch (Exception e) {
                    order.setOrderDate(new Date());
                }
                
                order.setTotalAmount(rs.getDouble("total_amount"));
                order.setStatus(rs.getString("status"));
                order.setCustomerName(rs.getString("customer_name"));
                
                return order;
            }
        }, params.toArray());
    }    
    
    /**
     * Get an order by ID with all its items
     */
    public Order getOrderById(int id) {
        // Get the order
        String orderSql = "SELECT o.*, c.name as customer_name " +
                          "FROM \"Order\" o " +
                          "LEFT JOIN Customer c ON o.customer_id = c.id " +
                          "WHERE o.id = ?";
        
        List<Order> orders = DataUtil.query(orderSql, new ResultSetMapper<Order>() {
            @Override
            public Order map(ResultSet rs) throws SQLException {
                Order order = new Order();
                order.setId(rs.getInt("id"));
                order.setCustomerId(rs.getInt("customer_id"));
                
                try {
                    order.setOrderDate(DATE_FORMAT.parse(rs.getString("order_date")));
                } catch (Exception e) {
                    order.setOrderDate(new Date());
                }
                
                order.setTotalAmount(rs.getDouble("total_amount"));
                order.setStatus(rs.getString("status"));
                order.setCustomerName(rs.getString("customer_name"));
                
                return order;
            }
        }, id);
        
        if (orders.isEmpty()) {
            return null;
        }
        
        Order order = orders.get(0);
        
        // Get the order items
        String itemsSql = "SELECT oi.*, p.name as product_name " +
                          "FROM OrderItem oi " +
                          "JOIN Product p ON oi.product_id = p.id " +
                          "WHERE oi.order_id = ?";
        
        List<OrderItem> items = DataUtil.query(itemsSql, new ResultSetMapper<OrderItem>() {
            @Override
            public OrderItem map(ResultSet rs) throws SQLException {
                OrderItem item = new OrderItem();
                item.setId(rs.getInt("id"));
                item.setOrderId(id);
                item.setProductId(rs.getInt("product_id"));
                item.setQuantity(rs.getInt("quantity"));
                item.setUnitPrice(rs.getDouble("unit_price"));
                item.setProductName(rs.getString("product_name"));
                
                return item;
            }
        }, id);
        
        order.setOrderItems(items);
        
        return order;
    }
    
    /**
     * Save an order with its items
     */
    public boolean saveOrder(Order order) {
        try {
            System.out.println("DEBUG: Starting order save process for order ID: " + order.getId());
            
            // Begin transaction
            DataUtil.beginTransaction();
            Connection conn = DBConnection.getConnection();
            
            // Set date and calculate total
            if (order.getOrderDate() == null) {
                order.setOrderDate(new Date());
            }
            order.calculateTotal();
            System.out.println("DEBUG: Order total calculated: " + order.getTotalAmount());
            
            // Handle existing order
            if (order.getId() > 0) {
                System.out.println("DEBUG: Updating existing order #" + order.getId());
                
                // Get original order with items
                Order originalOrder = getOrderById(order.getId());
                System.out.println("DEBUG: Original order items count: " + originalOrder.getOrderItems().size());
                
                // Print original order items
                System.out.println("DEBUG: Original order items details:");
                for (OrderItem originalItem : originalOrder.getOrderItems()) {
                    System.out.println("DEBUG:   Product ID: " + originalItem.getProductId() + 
                                    ", Quantity: " + originalItem.getQuantity() +
                                    ", Unit Price: " + originalItem.getUnitPrice());
                    
                    // Get current stock level before restoration
                    String checkStockSql = "SELECT stock_qty FROM Product WHERE id = ?";
                    PreparedStatement checkStmt = conn.prepareStatement(checkStockSql);
                    checkStmt.setInt(1, originalItem.getProductId());
                    ResultSet rs = checkStmt.executeQuery();
                    int currentStock = 0;
                    if (rs.next()) {
                        currentStock = rs.getInt("stock_qty");
                    }
                    rs.close();
                    checkStmt.close();
                    System.out.println("DEBUG:   Current stock before restore: " + currentStock);
                    
                    // First restore all original quantities to stock
                    String restoreStockSql = "UPDATE Product SET stock_qty = stock_qty + ? WHERE id = ?";
                    PreparedStatement restoreStmt = conn.prepareStatement(restoreStockSql);
                    restoreStmt.setInt(1, originalItem.getQuantity());
                    restoreStmt.setInt(2, originalItem.getProductId());
                    int rowsAffected = restoreStmt.executeUpdate();
                    restoreStmt.close();
                    System.out.println("DEBUG:   Restored " + originalItem.getQuantity() + 
                                    " units to product ID: " + originalItem.getProductId() + 
                                    ", Rows affected: " + rowsAffected);
                    
                    // Check stock after restoration
                    checkStmt = conn.prepareStatement(checkStockSql);
                    checkStmt.setInt(1, originalItem.getProductId());
                    rs = checkStmt.executeQuery();
                    if (rs.next()) {
                        int updatedStock = rs.getInt("stock_qty");
                        System.out.println("DEBUG:   Stock after restore: " + updatedStock + 
                                        " (Change: +" + (updatedStock - currentStock) + ")");
                    }
                    rs.close();
                    checkStmt.close();
                }
                
                // Update order record
                System.out.println("DEBUG: Updating order record");
                if (!DataUtil.update("\"Order\"", order, "id", "customerName", "orderItems")) {
                    System.out.println("DEBUG: Failed to update order record");
                    DataUtil.rollbackTransaction();
                    return false;
                }
                
                // Delete old order items
                String deleteSql = "DELETE FROM OrderItem WHERE order_id = ?";
                PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
                deleteStmt.setInt(1, order.getId());
                int deletedCount = deleteStmt.executeUpdate();
                deleteStmt.close();
                System.out.println("DEBUG: Deleted " + deletedCount + " original order items");
                
            } else {
                // Insert new order
                System.out.println("DEBUG: Creating new order");
                int orderId = DataUtil.insert("\"Order\"", order, "id", "customerName", "orderItems");
                if (orderId <= 0) {
                    System.out.println("DEBUG: Failed to create new order");
                    DataUtil.rollbackTransaction();
                    return false;
                }
                order.setId(orderId);
                System.out.println("DEBUG: New order created with ID: " + orderId);
            }
            
            // Print new order items
            System.out.println("DEBUG: New order items to insert: " + order.getOrderItems().size());
            for (OrderItem item : order.getOrderItems()) {
                System.out.println("DEBUG:   Product ID: " + item.getProductId() + 
                                ", Quantity: " + item.getQuantity() +
                                ", Unit Price: " + item.getUnitPrice());
            }
            
            // Insert order items and update stock
            for (OrderItem item : order.getOrderItems()) {
                item.setOrderId(order.getId());
                
                // Get current stock level before deduction
                String checkStockSql = "SELECT stock_qty FROM Product WHERE id = ?";
                PreparedStatement checkStmt = conn.prepareStatement(checkStockSql);
                checkStmt.setInt(1, item.getProductId());
                ResultSet rs = checkStmt.executeQuery();
                int currentStock = 0;
                if (rs.next()) {
                    currentStock = rs.getInt("stock_qty");
                }
                rs.close();
                checkStmt.close();
                System.out.println("DEBUG:   Current stock before deduction for product ID " + 
                                item.getProductId() + ": " + currentStock);
                
                // Insert item
                System.out.println("DEBUG:   Inserting order item for product ID: " + item.getProductId());
                int itemId = DataUtil.insert("OrderItem", item, "id", "productName");
                if (itemId <= 0) {
                    System.out.println("DEBUG:   Failed to insert order item");
                    DataUtil.rollbackTransaction();
                    return false;
                }
                item.setId(itemId);
                System.out.println("DEBUG:   Order item inserted with ID: " + itemId);
                
                // Reduce stock quantity
                String updateStockSql = "UPDATE Product SET stock_qty = stock_qty - ? WHERE id = ?";
                PreparedStatement updateStmt = conn.prepareStatement(updateStockSql);
                updateStmt.setInt(1, item.getQuantity());
                updateStmt.setInt(2, item.getProductId());
                int rowsAffected = updateStmt.executeUpdate();
                updateStmt.close();
                System.out.println("DEBUG:   Deducted " + item.getQuantity() + 
                                " units from product ID: " + item.getProductId() + 
                                ", Rows affected: " + rowsAffected);
                
                // Check stock after deduction
                checkStmt = conn.prepareStatement(checkStockSql);
                checkStmt.setInt(1, item.getProductId());
                rs = checkStmt.executeQuery();
                if (rs.next()) {
                    int updatedStock = rs.getInt("stock_qty");
                    System.out.println("DEBUG:   Stock after deduction: " + updatedStock + 
                                    " (Change: -" + (currentStock - updatedStock) + ")");
                }
                rs.close();
                checkStmt.close();
            }
            
            // Commit transaction
            DataUtil.commitTransaction();
            System.out.println("DEBUG: Transaction committed successfully");
            return true;
        } catch (SQLException e) {
            System.out.println("DEBUG: SQL Exception occurred: " + e.getMessage());
            try {
                DataUtil.rollbackTransaction();
                System.out.println("DEBUG: Transaction rolled back");
            } catch (SQLException ex) {
                System.out.println("DEBUG: Error during rollback: " + ex.getMessage());
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Update order status
     */
    public boolean updateOrderStatus(int orderId, String status) {
        Order order = getOrderById(orderId);
        if (order != null) {
            order.setStatus(status);
            return DataUtil.update("\"Order\"", order, "id");
        }
        return false;
    }
    
    /**
     * Delete an order
     */
    public boolean deleteOrder(int id) {
        try {
            // Begin transaction
            DataUtil.beginTransaction();
            
            // Delete order items first
            String deleteItemsSql = "DELETE FROM OrderItem WHERE order_id = ?";
            Connection conn = DBConnection.getConnection();
            PreparedStatement deleteItemsStmt = conn.prepareStatement(deleteItemsSql);
            deleteItemsStmt.setInt(1, id);
            deleteItemsStmt.executeUpdate();
            deleteItemsStmt.close();
            
            // Delete the order
            if (!DataUtil.delete("\"Order\"", id, "id")) {
                DataUtil.rollbackTransaction();
                return false;
            }
            
            // Commit transaction
            DataUtil.commitTransaction();
            return true;
        } catch (SQLException e) {
            try {
                DataUtil.rollbackTransaction();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            e.printStackTrace();
            return false;
        }
    }
}