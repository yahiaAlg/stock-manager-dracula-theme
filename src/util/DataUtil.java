package util;

import java.lang.reflect.Field;
import java.sql.*;
import java.util.*;
import java.util.Date;
import java.text.SimpleDateFormat;

public class DataUtil {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    /**
     * Generic method to insert an object into the database
     * @param tableName The table to insert into
     * @param object The object containing data to insert
     * @param excludeFields Fields to exclude from insert (e.g., "id")
     * @return The generated ID of the inserted record, or -1 if failed
     */
    public static int insert(String tableName, Object object, String... excludeFields) {
        Set<String> excluded = new HashSet<>(Arrays.asList(excludeFields));
        
        try {
            Class<?> clazz = object.getClass();
            Field[] fields = clazz.getDeclaredFields();
            
            // Build column and value lists for SQL
            StringBuilder columns = new StringBuilder();
            StringBuilder placeholders = new StringBuilder();
            List<Object> values = new ArrayList<>();
            
            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                
                // Skip excluded fields and null values
                if (!excluded.contains(fieldName) && field.get(object) != null) {
                    if (columns.length() > 0) {
                        columns.append(", ");
                        placeholders.append(", ");
                    }
                    columns.append(camelToSnake(fieldName));
                    placeholders.append("?");
                    values.add(field.get(object));
                }
            }
            
            // Create SQL statement
            String sql = "INSERT INTO " + tableName + " (" + columns.toString() + ") VALUES (" + placeholders.toString() + ")";
            
            // Execute the insert
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            
            setParameters(stmt, values);
            
            int affectedRows = stmt.executeUpdate();
            
            if (affectedRows > 0) {
                ResultSet generatedKeys = stmt.getGeneratedKeys();
                if (generatedKeys.next()) {
                    return generatedKeys.getInt(1);
                }
            }
            
            stmt.close();
        } catch (Exception e) {
            System.err.println("Error inserting record: " + e.getMessage());
            e.printStackTrace();
        }
        
        return -1;
    }
    
    /**
     * Generic method to update an object in the database
     * @param tableName The table to update
     * @param object The object containing updated data
     * @param idFieldName The name of the ID field used in WHERE clause
     * @param excludeFields Fields to exclude from update
     * @return true if update was successful, false otherwise
     */
    public static boolean update(String tableName, Object object, String idFieldName, String... excludeFields) {
        Set<String> excluded = new HashSet<>(Arrays.asList(excludeFields));
        excluded.add(idFieldName); // Don't update the ID field
        
        try {
            Class<?> clazz = object.getClass();
            Field[] fields = clazz.getDeclaredFields();
            
            // Get the ID value for the WHERE clause
            Field idField = clazz.getDeclaredField(idFieldName);
            idField.setAccessible(true);
            Object idValue = idField.get(object);
            
            // Build SET clause and values list for SQL
            StringBuilder setClause = new StringBuilder();
            List<Object> values = new ArrayList<>();
            
            for (Field field : fields) {
                field.setAccessible(true);
                String fieldName = field.getName();
                
                // Skip excluded fields and null values
                if (!excluded.contains(fieldName) && field.get(object) != null) {
                    if (setClause.length() > 0) {
                        setClause.append(", ");
                    }
                    setClause.append(camelToSnake(fieldName)).append(" = ?");
                    values.add(field.get(object));
                }
            }
            
            // Add ID value for WHERE clause
            values.add(idValue);
            
            // Create SQL statement
            String sql = "UPDATE " + tableName + " SET " + setClause.toString() + 
                         " WHERE " + camelToSnake(idFieldName) + " = ?";
            
            // Execute the update
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            setParameters(stmt, values);
            
            int affectedRows = stmt.executeUpdate();
            stmt.close();
            
            return affectedRows > 0;
        } catch (Exception e) {
            System.err.println("Error updating record: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Generic method to delete an object from the database
     * @param tableName The table to delete from
     * @param id The ID of the record to delete
     * @param idFieldName The name of the ID field in the table
     * @return true if deletion was successful, false otherwise
     */
    public static boolean delete(String tableName, int id, String idFieldName) {
        try {
            String sql = "DELETE FROM " + tableName + " WHERE " + camelToSnake(idFieldName) + " = ?";
            
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            stmt.setInt(1, id);
            
            int affectedRows = stmt.executeUpdate();
            stmt.close();
            
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting record: " + e.getMessage());
            e.printStackTrace();
        }
        
        return false;
    }
    
    /**
     * Generic method to query the database and map results to objects
     * @param sql The SQL query to execute
     * @param mapper A ResultSet mapper function to convert rows to objects
     * @param params Parameters for the prepared statement
     * @return List of objects returned by the query
     */
    public static <T> List<T> query(String sql, ResultSetMapper<T> mapper, Object... params) {
        List<T> results = new ArrayList<>();
        
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            // Set parameters if any
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            ResultSet rs = stmt.executeQuery();
            
            while (rs.next()) {
                T obj = mapper.map(rs);
                if (obj != null) {
                    results.add(obj);
                }
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error executing query: " + e.getMessage());
            e.printStackTrace();
        }
        
        return results;
    }
    
    /**
     * Query a single value from the database
     * @param sql The SQL query to execute
     * @param params Parameters for the prepared statement
     * @return The first column of the first row, or null if no results
     */
    public static Object queryScalar(String sql, Object... params) {
        try {
            Connection conn = DBConnection.getConnection();
            PreparedStatement stmt = conn.prepareStatement(sql);
            
            // Set parameters if any
            for (int i = 0; i < params.length; i++) {
                stmt.setObject(i + 1, params[i]);
            }
            
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                Object result = rs.getObject(1);
                rs.close();
                stmt.close();
                return result;
            }
            
            rs.close();
            stmt.close();
        } catch (SQLException e) {
            System.err.println("Error executing scalar query: " + e.getMessage());
            e.printStackTrace();
        }
        
        return null;
    }
    
    /**
     * Execute a raw SQL statement (for CREATE, ALTER, etc.)
     */
    public static boolean executeRawSql(String sql) {
        try {
            Connection conn = DBConnection.getConnection();
            Statement stmt = conn.createStatement();
            stmt.execute(sql);
            stmt.close();
            return true;
        } catch (SQLException e) {
            System.err.println("Error executing SQL: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Begin a database transaction
     */
    public static void beginTransaction() throws SQLException {
        Connection conn = DBConnection.getConnection();
        conn.setAutoCommit(false);
    }
    
    /**
     * Commit a database transaction
     */
    public static void commitTransaction() throws SQLException {
        Connection conn = DBConnection.getConnection();
        conn.commit();
        conn.setAutoCommit(true);
    }
    
    /**
     * Rollback a database transaction
     */
    public static void rollbackTransaction() throws SQLException {
        Connection conn = DBConnection.getConnection();
        conn.rollback();
        conn.setAutoCommit(true);
    }
    
    /**
     * Convert camelCase field names to snake_case column names
     */
    private static String camelToSnake(String camelCase) {
        return camelCase.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
    }
    
    /**
     * Set parameters for PreparedStatement based on their types
     */
    private static void setParameters(PreparedStatement stmt, List<Object> params) throws SQLException {
        for (int i = 0; i < params.size(); i++) {
            Object param = params.get(i);
            
            if (param == null) {
                stmt.setNull(i + 1, Types.NULL);
            } else if (param instanceof Integer) {
                stmt.setInt(i + 1, (Integer)param);
            } else if (param instanceof Double) {
                stmt.setDouble(i + 1, (Double)param);
            } else if (param instanceof Float) {
                stmt.setFloat(i + 1, (Float)param);
            } else if (param instanceof Long) {
                stmt.setLong(i + 1, (Long)param);
            } else if (param instanceof Boolean) {
                stmt.setBoolean(i + 1, (Boolean)param);
            } else if (param instanceof Date) {
                stmt.setString(i + 1, DATE_FORMAT.format((Date)param));
            } else {
                stmt.setString(i + 1, param.toString());
            }
        }
    }
    
    /**
     * Interface for mapping ResultSet rows to objects
     */
    public interface ResultSetMapper<T> {
        T map(ResultSet rs) throws SQLException;
    }
}