package controller;

import model.Customer;
import util.DataUtil;
import util.DataUtil.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class CustomerController {
    
    /**
     * Get all customers
     */
    public List<Customer> getAllCustomers() {
        String sql = "SELECT * FROM Customer ORDER BY name";
        
        return DataUtil.query(sql, new ResultSetMapper<Customer>() {
            @Override
            public Customer map(ResultSet rs) throws SQLException {
                Customer customer = new Customer();
                customer.setId(rs.getInt("id"));
                customer.setName(rs.getString("name"));
                customer.setContact(rs.getString("contact"));
                customer.setEmail(rs.getString("email"));
                customer.setAddress(rs.getString("address"));
                return customer;
            }
        });
    }
    
    /**
     * Search customers by name, email, or contact
     */
    public List<Customer> searchCustomers(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllCustomers();
        }
        
        String sql = "SELECT * FROM Customer WHERE name LIKE ? OR email LIKE ? OR contact LIKE ? ORDER BY name";
        String searchPattern = "%" + searchTerm + "%";
        
        return DataUtil.query(sql, new ResultSetMapper<Customer>() {
            @Override
            public Customer map(ResultSet rs) throws SQLException {
                Customer customer = new Customer();
                customer.setId(rs.getInt("id"));
                customer.setName(rs.getString("name"));
                customer.setContact(rs.getString("contact"));
                customer.setEmail(rs.getString("email"));
                customer.setAddress(rs.getString("address"));
                return customer;
            }
        }, searchPattern, searchPattern, searchPattern);
    }
    
    /**
     * Get a customer by ID
     */
    public Customer getCustomerById(int id) {
        String sql = "SELECT * FROM Customer WHERE id = ?";
        
        List<Customer> customers = DataUtil.query(sql, new ResultSetMapper<Customer>() {
            @Override
            public Customer map(ResultSet rs) throws SQLException {
                Customer customer = new Customer();
                customer.setId(rs.getInt("id"));
                customer.setName(rs.getString("name"));
                customer.setContact(rs.getString("contact"));
                customer.setEmail(rs.getString("email"));
                customer.setAddress(rs.getString("address"));
                return customer;
            }
        }, id);
        
        return customers.isEmpty() ? null : customers.get(0);
    }
    
    /**
     * Save a customer (insert or update)
     */
    public boolean saveCustomer(Customer customer) {
        if (customer.getId() > 0) {
            // Update existing customer
            return DataUtil.update("Customer", customer, "id");
        } else {
            // Insert new customer
            int id = DataUtil.insert("Customer", customer, "id");
            if (id > 0) {
                customer.setId(id);
                return true;
            }
            return false;
        }
    }
    
    /**
     * Delete a customer
     */
    public boolean deleteCustomer(int id) {
        return DataUtil.delete("Customer", id, "id");
    }
    
    /**
     * Get orders for a customer
     */
    public List<model.Order> getOrdersByCustomer(int customerId) {
        String sql = "SELECT * FROM \"Order\" WHERE customer_id = ? ORDER BY order_date DESC";
        
        return DataUtil.query(sql, new ResultSetMapper<model.Order>() {
            @Override
            public model.Order map(ResultSet rs) throws SQLException {
                model.Order order = new model.Order();
                order.setId(rs.getInt("id"));
                order.setCustomerId(customerId);
                
                try {
                    // Parse date from string
                    java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    order.setOrderDate(sdf.parse(rs.getString("order_date")));
                } catch (Exception e) {
                    order.setOrderDate(new java.util.Date());
                }
                
                order.setTotalAmount(rs.getDouble("total_amount"));
                order.setStatus(rs.getString("status"));
                
                return order;
            }
        }, customerId);
    }
}