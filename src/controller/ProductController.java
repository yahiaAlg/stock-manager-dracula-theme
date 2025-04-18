package controller;

import model.Product;
import model.Category;
import model.Supplier;
import util.DataUtil;
import util.DataUtil.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class ProductController {
    
    /**
     * Get all products with category and supplier names
     */
    public List<Product> getAllProducts() {
        String sql = "SELECT p.*, c.name as category_name, s.name as supplier_name " +
                     "FROM Product p " +
                     "LEFT JOIN Category c ON p.category_id = c.id " +
                     "LEFT JOIN Supplier s ON p.supplier_id = s.id " +
                     "ORDER BY p.name";
        
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
     * Get products that match search criteria
     */
    public List<Product> searchProducts(String searchTerm, Integer categoryId, Integer supplierId, Boolean lowStock) {
        // Build the query with potential WHERE clauses
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();
        
        sql.append("SELECT p.*, c.name as category_name, s.name as supplier_name ");
        sql.append("FROM Product p ");
        sql.append("LEFT JOIN Category c ON p.category_id = c.id ");
        sql.append("LEFT JOIN Supplier s ON p.supplier_id = s.id ");
        
        // Start building WHERE clause if needed
        boolean hasWhere = false;
        
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            sql.append("WHERE (p.name LIKE ? OR p.sku LIKE ?) ");
            params.add("%" + searchTerm + "%");
            params.add("%" + searchTerm + "%");
            hasWhere = true;
        }
        
        if (categoryId != null && categoryId > 0) {
            if (hasWhere) {
                sql.append("AND p.category_id = ? ");
            } else {
                sql.append("WHERE p.category_id = ? ");
                hasWhere = true;
            }
            params.add(categoryId);
        }
        
        if (supplierId != null && supplierId > 0) {
            if (hasWhere) {
                sql.append("AND p.supplier_id = ? ");
            } else {
                sql.append("WHERE p.supplier_id = ? ");
                hasWhere = true;
            }
            params.add(supplierId);
        }
        
        if (lowStock != null && lowStock) {
            if (hasWhere) {
                sql.append("AND p.stock_qty <= p.reorder_level ");
            } else {
                sql.append("WHERE p.stock_qty <= p.reorder_level ");
            }
        }
        
        sql.append("ORDER BY p.name");
        
        return DataUtil.query(sql.toString(), new ResultSetMapper<Product>() {
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
        }, params.toArray());
    }
    
    /**
     * Get a product by ID
     */
    public Product getProductById(int id) {
        String sql = "SELECT p.*, c.name as category_name, s.name as supplier_name " +
                     "FROM Product p " +
                     "LEFT JOIN Category c ON p.category_id = c.id " +
                     "LEFT JOIN Supplier s ON p.supplier_id = s.id " +
                     "WHERE p.id = ?";
        
        List<Product> products = DataUtil.query(sql, new ResultSetMapper<Product>() {
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
        }, id);
        
        return products.isEmpty() ? null : products.get(0);
    }
    
    /**
     * Save a product (insert or update)
     */
    public boolean saveProduct(Product product) {
        if (product.getId() > 0) {
            // Update existing product
            return DataUtil.update("Product", product, "id");
        } else {
            // Insert new product
            int id = DataUtil.insert("Product", product, "id");
            if (id > 0) {
                product.setId(id);
                return true;
            }
            return false;
        }
    }
    
    /**
     * Delete a product
     */
    public boolean deleteProduct(int id) {
        return DataUtil.delete("Product", id, "id");
    }
    
    /**
     * Adjust inventory for a product
     */
    public boolean adjustInventory(int productId, int changeQty, String reason) {
        if (changeQty == 0) {
            return false;
        }
        
        try {
            // Begin transaction
            DataUtil.beginTransaction();
            
            // Update product stock
            Product product = getProductById(productId);
            if (product != null) {
                product.setStockQty(product.getStockQty() + changeQty);
                
                if (!DataUtil.update("Product", product, "id")) {
                    DataUtil.rollbackTransaction();
                    return false;
                }
                
                // Create adjustment record
                model.InventoryAdjustment adjustment = new model.InventoryAdjustment();
                adjustment.setProductId(productId);
                adjustment.setChangeQty(changeQty);
                adjustment.setReason(reason);
                adjustment.setDate(new java.util.Date());
                
                int id = DataUtil.insert("InventoryAdjustment", adjustment, "id");
                if (id <= 0) {
                    DataUtil.rollbackTransaction();
                    return false;
                }
                
                // Commit transaction
                DataUtil.commitTransaction();
                return true;
            }
            
            DataUtil.rollbackTransaction();
            return false;
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
     * Get all categories
     */
    public List<Category> getAllCategories() {
        String sql = "SELECT * FROM Category ORDER BY name";
        
        return DataUtil.query(sql, new ResultSetMapper<Category>() {
            @Override
            public Category map(ResultSet rs) throws SQLException {
                Category category = new Category();
                category.setId(rs.getInt("id"));
                category.setName(rs.getString("name"));
                category.setDescription(rs.getString("description"));
                return category;
            }
        });
    }

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

                return supplier;
            }
        });
    }
    /**
     * Save a category (insert or update)
     */
    public boolean saveCategory(Category category) {
        if (category.getId() > 0) {
            // Update existing category
            return DataUtil.update("Category", category, "id");
        } else {
            // Insert new category
            int id = DataUtil.insert("Category", category, "id");
            if (id > 0) {
                category.setId(id);
                return true;
            }
            return false;
        }
    }
    
    /**
     * Delete a category
     */
    public boolean deleteCategory(int id) {
        return DataUtil.delete("Category", id, "id");
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
                     "ORDER BY p.name";
        
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


}