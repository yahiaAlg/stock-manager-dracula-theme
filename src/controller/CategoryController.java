package controller;

import model.Category;
import util.DataUtil;
import util.DataUtil.ResultSetMapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class CategoryController {
    
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
     * Get a category by ID
     */
    public Category getCategoryById(int id) {
        String sql = "SELECT * FROM Category WHERE id = ?";
        
        List<Category> categories = DataUtil.query(sql, new ResultSetMapper<Category>() {
            @Override
            public Category map(ResultSet rs) throws SQLException {
                Category category = new Category();
                category.setId(rs.getInt("id"));
                category.setName(rs.getString("name"));
                category.setDescription(rs.getString("description"));
                return category;
            }
        }, id);
        
        return categories.isEmpty() ? null : categories.get(0);
    }
    
    /**
     * Search categories by name or description
     */
    public List<Category> searchCategories(String searchTerm) {
        StringBuilder sql = new StringBuilder();
        List<Object> params = new ArrayList<>();
        
        sql.append("SELECT * FROM Category ");
        
        if (searchTerm != null && !searchTerm.trim().isEmpty()) {
            sql.append("WHERE (name LIKE ? OR description LIKE ?) ");
            params.add("%" + searchTerm + "%");
            params.add("%" + searchTerm + "%");
        }
        
        sql.append("ORDER BY name");
        
        return DataUtil.query(sql.toString(), new ResultSetMapper<Category>() {
            @Override
            public Category map(ResultSet rs) throws SQLException {
                Category category = new Category();
                category.setId(rs.getInt("id"));
                category.setName(rs.getString("name"));
                category.setDescription(rs.getString("description"));
                return category;
            }
        }, params.toArray());
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
     * Check if a category is in use (has products)
     */
    public boolean isCategoryInUse(int id) {
        String sql = "SELECT COUNT(*) FROM Product WHERE category_id = ?";
        
        Object result = DataUtil.queryScalar(sql, id);
        if (result != null) {
            return ((Number) result).intValue() > 0;
        }
        return false;
    }
}
