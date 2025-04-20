package controller;

import model.User;
import util.DataUtil;
import util.PasswordUtil;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

public class UserController {
    
    private static final String TABLE_NAME = "users";
    
    /**
     * Authenticate a user with username and password
     * @param username The username
     * @param password The password
     * @return User object if authentication is successful, null otherwise
     */
    public User authenticate(String username, String password) {
        String sql = "SELECT * FROM users WHERE username = ?";
        
        List<User> users = DataUtil.query(sql, rs -> mapResultSetToUser(rs), username);
        
        if (!users.isEmpty()) {
            User user = users.get(0);
            
            // Verify password
            if (PasswordUtil.verifyPassword(password, user.getPassword())) {
                return user;
            }
        }
        
        return null;
    }
    
    /**
     * Register a new user
     * @param user The user to register
     * @return true if registration is successful, false otherwise
     */
    public boolean registerUser(User user) {
        // Check if username already exists
        if (isUsernameExists(user.getUsername())) {
            return false;
        }
        
        // Hash the password
        user.setPassword(PasswordUtil.hashPassword(user.getPassword()));
        
        // Insert user into database
        int userId = DataUtil.insert(TABLE_NAME, user, "id");
        
        return userId > 0;
    }
    
    /**
     * Update an existing user
     * @param user The user to update
     * @return true if update is successful, false otherwise
     */
    public boolean updateUser(User user) {
        // If password was changed, hash the new password
        if (user.getPassword() != null && !user.getPassword().startsWith("$2a$")) {
            user.setPassword(PasswordUtil.hashPassword(user.getPassword()));
        }
        
        // Update user in database
        return DataUtil.update(TABLE_NAME, user, "id");
    }
    
    /**
     * Delete (deactivate) a user
     * @param userId The ID of the user to delete
     * @return true if deletion is successful, false otherwise
     */
    public boolean deleteUser(int userId) {
        // Get the user
        User user = getUserById(userId);
        
        if (user != null) {
            // Deactivate the user instead of deleting
            user.setActive(false);
            return updateUser(user);
        }
        
        return false;
    }
    
    /**
     * Get all users from the database
     * @return List of User objects
     */
    public List<User> getAllUsers() {
        String sql = "SELECT * FROM users ORDER BY username";
        
        return DataUtil.query(sql, rs -> mapResultSetToUser(rs));
    }
    
    /**
     * Get a user by ID
     * @param userId The ID of the user to get
     * @return User object if found, null otherwise
     */
    public User getUserById(int userId) {
        String sql = "SELECT * FROM users WHERE id = ?";
        
        List<User> users = DataUtil.query(sql, rs -> mapResultSetToUser(rs), userId);
        
        return users.isEmpty() ? null : users.get(0);
    }
    
    /**
     * Check if a username already exists
     * @param username The username to check
     * @return true if username exists, false otherwise
     */
    private boolean isUsernameExists(String username) {
        String sql = "SELECT COUNT(*) FROM users WHERE username = ?";
        
        Object result = DataUtil.queryScalar(sql, username);
        
        // Fix: Support both Integer and Long return types
        if (result != null) {
            if (result instanceof Long) {
                return ((Long) result) > 0;
            } else if (result instanceof Integer) {
                return ((Integer) result) > 0;
            }
        }
        return false;
    }
    
    /**
     * Map a ResultSet row to a User object
     * @param rs The ResultSet
     * @return User object
     * @throws SQLException If an error occurs while reading the ResultSet
     */
    private User mapResultSetToUser(ResultSet rs) throws SQLException {
        User user = new User();
        user.setId(rs.getInt("id"));
        user.setUsername(rs.getString("username"));
        user.setPassword(rs.getString("password"));
        user.setEmail(rs.getString("email"));
        user.setFullName(rs.getString("full_name"));
        user.setRole(rs.getString("role"));
        user.setActive(rs.getBoolean("active"));
        
        return user;
    }
}