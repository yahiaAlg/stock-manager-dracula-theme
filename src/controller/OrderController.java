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
    public List<Order> searchOrders(Integer customerId, String status, Date startDate, Date endDate) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();
        
        sql.append("SELECT o.*, c.name as customer_name ");
        sql.append("FROM \"Order\" o ");
        sql.append("LEFT JOIN Customer c ON o.customer_id = c.id ");
        
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
            }
            params.add(DATE_FORMAT.format(endDate));
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
            // Begin transaction
            DataUtil.beginTransaction();
            
            // Set formatted date string
            if (order.getOrderDate() == null) {
                order.setOrderDate(new Date());
            }
            
            // Calculate total amount
            order.calculateTotal();
            
            if (order.getId() > 0) {
                // Update existing order
                if (!DataUtil.update("\"Order\"", order, "id")) {
                    DataUtil.rollbackTransaction();
                    return false;
                }
                
                // Delete existing order items
                String deleteSql = "DELETE FROM OrderItem WHERE order_id = ?";
                Connection conn = DBConnection.getConnection();
                PreparedStatement deleteStmt = conn.prepareStatement(deleteSql);
                deleteStmt.setInt(1, order.getId());
                deleteStmt.executeUpdate();
                deleteStmt.close();
            } else {
                // Insert new order
                int orderId = DataUtil.insert("\"Order\"", order, "id");
                if (orderId <= 0) {
                    DataUtil.rollbackTransaction();
                    return false;
                }
                
                order.setId(orderId);
            }
            
            // Insert order items
            for (OrderItem item : order.getOrderItems()) {
                item.setOrderId(order.getId());
                
                int itemId = DataUtil.insert("OrderItem", item, "id");
                if (itemId <= 0) {
                    DataUtil.rollbackTransaction();
                    return false;
                }
                
                item.setId(itemId);
                
                // Update product stock
                String updateStockSql = "UPDATE Product SET stock_qty = stock_qty - ? WHERE id = ?";
                Connection conn = DBConnection.getConnection();
                PreparedStatement updateStmt = conn.prepareStatement(updateStockSql);
                updateStmt.setInt(1, item.getQuantity());
                updateStmt.setInt(2, item.getProductId());
                updateStmt.executeUpdate();
                updateStmt.close();
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