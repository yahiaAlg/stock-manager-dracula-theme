package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

public class DBConnection {
    private static final String DB_URL = "jdbc:sqlite:stock-manager.db";
    private static Connection connection = null;
    
    public static Connection getConnection() {
        if (connection == null) {
            try {
                // Load the SQLite JDBC driver
                Class.forName("org.sqlite.JDBC");
                connection = DriverManager.getConnection(DB_URL);
                
                // Set pragmas for better performance
                Statement stmt = connection.createStatement();
                stmt.execute("PRAGMA foreign_keys = ON");
                stmt.close();
                
                // Create tables if they don't exist
                initializeDatabase();
                
                System.out.println("Database connection established.");
            } catch (ClassNotFoundException e) {
                System.err.println("SQLite JDBC driver not found: " + e.getMessage());
            } catch (SQLException e) {
                System.err.println("Database connection error: " + e.getMessage());
            }
        }
        return connection;
    }
    
    public static void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                connection = null;
                System.out.println("Database connection closed.");
            } catch (SQLException e) {
                System.err.println("Error closing database connection: " + e.getMessage());
            }
        }
    }
    
    private static void initializeDatabase() {
        try {
            String rootPath = new File(".").getCanonicalPath();
            String schemaPath = rootPath + "/stock-manager/resources/schema.sql";
            System.out.println("Loading schema from: " + schemaPath);
            
            // Read the file and execute each statement individually
            StringBuilder currentStatement = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(schemaPath))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    // Skip comments
                    if (line.trim().startsWith("--")) {
                        continue;
                    }
                    
                    // Add the line to the current statement
                    currentStatement.append(line).append(" ");
                    
                    // If the line has a semicolon, execute the statement
                    if (line.trim().endsWith(";")) {
                        executeStatement(currentStatement.toString());
                        currentStatement = new StringBuilder();
                    }
                }
            }
            
            System.out.println("Database schema initialized successfully.");
        } catch (IOException e) {
            System.err.println("Error reading schema.sql: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    private static void executeStatement(String sql) {
        if (sql.trim().isEmpty()) {
            return;
        }
        
        Connection conn = getConnection();
        try (Statement stmt = conn.createStatement()) {
            System.out.println("Executing SQL: " + sql.trim());
            stmt.execute(sql);
        } catch (SQLException e) {
            System.err.println("Error executing statement: " + sql.trim());
            System.err.println("Error message: " + e.getMessage());
            // Continue with other statements
        }
    }
}