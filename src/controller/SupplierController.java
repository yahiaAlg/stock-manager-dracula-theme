package controller;

import model.Supplier;
import util.DataUtil;
import util.DataUtil.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class SupplierController {
    
    /**
     * Get all suppliers
     */
    public List<Supplier> getAllSuppliers() {
        String sql = "SELECT * FROM Supplier ORDER BY name";
        
        return DataUtil.query(sql, new ResultSetMapper<Supplier>() {
            @Override
            public Supplier map(ResultSet rs) throws SQLException {
                Supplier supplier = new Supplier();
                supplier.setId(rs.getInt("id"));
                supplier.setName(rs.getString("name"));
                supplier.setContact(rs.getString("contact"));
                supplier.setAddress(rs.getString("address"));
                return supplier;
            }
        });
    }
    
    /**
     * Search suppliers by name
     */
    public List<Supplier> searchSuppliers(String searchTerm) {
        if (searchTerm == null || searchTerm.trim().isEmpty()) {
            return getAllSuppliers();
        }
        
        String sql = "SELECT * FROM Supplier WHERE name LIKE ? OR contact LIKE ? OR address LIKE ? ORDER BY name";
        String searchPattern = "%" + searchTerm + "%";
        
        return DataUtil.query(sql, new ResultSetMapper<Supplier>() {
            @Override
            public Supplier map(ResultSet rs) throws SQLException {
                Supplier supplier = new Supplier();
                supplier.setId(rs.getInt("id"));
                supplier.setName(rs.getString("name"));
                supplier.setContact(rs.getString("contact"));
                supplier.setAddress(rs.getString("address"));
                return supplier;
            }
        }, searchPattern, searchPattern, searchPattern);
    }
    
    /**
     * Get a supplier by ID
     */
    public Supplier getSupplierById(int id) {
        String sql = "SELECT * FROM Supplier WHERE id = ?";
        
        List<Supplier> suppliers = DataUtil.query(sql, new ResultSetMapper<Supplier>() {
            @Override
            public Supplier map(ResultSet rs) throws SQLException {
                Supplier supplier = new Supplier();
                supplier.setId(rs.getInt("id"));
                supplier.setName(rs.getString("name"));
                supplier.setContact(rs.getString("contact"));
                supplier.setAddress(rs.getString("address"));
                return supplier;
            }
        }, id);
        
        return suppliers.isEmpty() ? null : suppliers.get(0);
    }
    
    /**
     * Save a supplier (insert or update)
     */
    public boolean saveSupplier(Supplier supplier) {
        if (supplier.getId() > 0) {
            // Update existing supplier
            return DataUtil.update("Supplier", supplier, "id");
        } else {
            // Insert new supplier
            int id = DataUtil.insert("Supplier", supplier, "id");
            if (id > 0) {
                supplier.setId(id);
                return true;
            }
            return false;
        }
    }
    
    /**
     * Delete a supplier
     */
    public boolean deleteSupplier(int id) {
        return DataUtil.delete("Supplier", id, "id");
    }
    
    /**
     * Get products for a supplier
     */
    public List<model.Product> getProductsBySupplier(int supplierId) {
        String sql = "SELECT p.*, c.name as category_name " +
                     "FROM Product p " +
                     "LEFT JOIN Category c ON p.category_id = c.id " +
                     "WHERE p.supplier_id = ? " +
                     "ORDER BY p.name";
        
        return DataUtil.query(sql, new ResultSetMapper<model.Product>() {
            @Override
            public model.Product map(ResultSet rs) throws SQLException {
                model.Product product = new model.Product();
                product.setId(rs.getInt("id"));
                product.setSku(rs.getString("sku"));
                product.setName(rs.getString("name"));
                product.setCategoryId(rs.getInt("category_id"));
                product.setSupplierId(supplierId);
                product.setUnitPrice(rs.getDouble("unit_price"));
                product.setStockQty(rs.getInt("stock_qty"));
                product.setReorderLevel(rs.getInt("reorder_level"));
                
                // Set the joined field
                product.setCategoryName(rs.getString("category_name"));
                
                return product;
            }
        }, supplierId);
    }
}