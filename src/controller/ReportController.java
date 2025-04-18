package controller;

import model.Report;
import util.DataUtil;
import util.DataUtil.ResultSetMapper;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Map;

public class ReportController {
    
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
    private static final String REPORTS_DIR = "reports";
    
    /**
     * Generate a report
     * @param reportType Type of report (e.g., "Inventory", "Sales")
     * @param format Output format ("CSV" or "PDF")
     * @param parameters Report parameters as JSON string
     * @return Generated report object with file path
     */
    public Report generateReport(String reportType, String format, String parameters) {
        // Create reports directory if it doesn't exist
        createReportsDirectory();
        
        Report report = new Report();
        report.setReportType(reportType);
        report.setGeneratedOn(new Date());
        report.setParameters(parameters);
        
        String fileName = reportType + "_" + DATE_FORMAT.format(report.getGeneratedOn()) + "." + format.toLowerCase();
        String filePath = REPORTS_DIR + File.separator + fileName;
        report.setFilePath(filePath);
        
        // Generate the actual report file
        boolean success = false;
        if ("CSV".equalsIgnoreCase(format)) {
            success = generateCsvReport(report);
        } else if ("PDF".equalsIgnoreCase(format)) {
            // For simplicity, we'll just create a text file
            success = generateTextReport(report);
        }
        
        if (success) {
            // Save report metadata in database
            int id = DataUtil.insert("Report", report, "id");
            if (id > 0) {
                report.setId(id);
                return report;
            }
        }
        
        return null;
    }
    
    /**
     * Get all reports
     */
    public List<Report> getAllReports() {
        String sql = "SELECT * FROM Report ORDER BY generated_on DESC";
        
        return DataUtil.query(sql, new ResultSetMapper<Report>() {
            @Override
            public Report map(ResultSet rs) throws SQLException {
                Report report = new Report();
                report.setId(rs.getInt("id"));
                report.setReportType(rs.getString("report_type"));
                
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    report.setGeneratedOn(dateFormat.parse(rs.getString("generated_on")));
                } catch (Exception e) {
                    report.setGeneratedOn(new Date());
                }
                
                report.setParameters(rs.getString("parameters"));
                report.setFilePath(rs.getString("file_path"));
                
                return report;
            }
        });
    }
    
    /**
     * Get a report by ID
     */
    public Report getReportById(int id) {
        String sql = "SELECT * FROM Report WHERE id = ?";
        
        List<Report> reports = DataUtil.query(sql, new ResultSetMapper<Report>() {
            @Override
            public Report map(ResultSet rs) throws SQLException {
                Report report = new Report();
                report.setId(rs.getInt("id"));
                report.setReportType(rs.getString("report_type"));
                
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    report.setGeneratedOn(dateFormat.parse(rs.getString("generated_on")));
                } catch (Exception e) {
                    report.setGeneratedOn(new Date());
                }
                
                report.setParameters(rs.getString("parameters"));
                report.setFilePath(rs.getString("file_path"));
                
                return report;
            }
        }, id);
        
        return reports.isEmpty() ? null : reports.get(0);
    }
    
    /**
     * Create reports directory if it doesn't exist
     */
    private void createReportsDirectory() {
        File reportsDir = new File(REPORTS_DIR);
        if (!reportsDir.exists()) {
            reportsDir.mkdirs();
        }
    }
    
    /**
     * Generate a CSV report file
     */
    private boolean generateCsvReport(Report report) {
        String sql = getReportQuery(report.getReportType(), report.getParameters());
        
        try (FileWriter writer = new FileWriter(report.getFilePath())) {
            // Execute query and write results to CSV
            List<Map<String, Object>> results = DataUtil.query(sql, rs -> {
                Map<String, Object> row = new java.util.HashMap<>();
                ResultSetMetaData metaData = rs.getMetaData();
                
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    String columnName = metaData.getColumnName(i);
                    row.put(columnName, rs.getObject(i));
                }
                
                return row;
            });
            
            if (!results.isEmpty()) {
                // Write header
                Map<String, Object> firstRow = results.get(0);
                boolean first = true;
                for (String columnName : firstRow.keySet()) {
                    if (!first) {
                        writer.append(",");
                    }
                    writer.append('"').append(escapeCsv(columnName)).append('"');
                    first = false;
                }
                writer.append("\n");
                
                // Write data rows
                for (Map<String, Object> row : results) {
                    first = true;
                    for (Object value : row.values()) {
                        if (!first) {
                            writer.append(",");
                        }
                        
                        String valueStr = (value != null) ? value.toString() : "";
                        writer.append('"').append(escapeCsv(valueStr)).append('"');
                        
                        first = false;
                    }
                    writer.append("\n");
                }
            }
            
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Generate a simple text report file
     */
    private boolean generateTextReport(Report report) {
        String sql = getReportQuery(report.getReportType(), report.getParameters());
        
        try (FileWriter writer = new FileWriter(report.getFilePath())) {
            // Write report header
            writer.write("Report Type: " + report.getReportType() + "\n");
            writer.write("Generated On: " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(report.getGeneratedOn()) + "\n");
            writer.write("Parameters: " + report.getParameters() + "\n\n");
            
            // Execute query and write results
            List<Map<String, Object>> results = DataUtil.query(sql, rs -> {
                Map<String, Object> row = new java.util.HashMap<>();
                ResultSetMetaData metaData = rs.getMetaData();
                
                for (int i = 1; i <= metaData.getColumnCount(); i++) {
                    String columnName = metaData.getColumnName(i);
                    row.put(columnName, rs.getObject(i));
                }
                
                return row;
            });
            
            if (!results.isEmpty()) {
                // Write header
                Map<String, Object> firstRow = results.get(0);
                for (String columnName : firstRow.keySet()) {
                    writer.append(columnName).append("\t");
                }
                writer.append("\n");
                
                // Write separator
                for (int i = 0; i < firstRow.size() * 15; i++) {
                    writer.append("-");
                }
                writer.append("\n");
                
                // Write data rows
                for (Map<String, Object> row : results) {
                    for (Object value : row.values()) {
                        String valueStr = (value != null) ? value.toString() : "";
                        writer.append(valueStr).append("\t");
                    }
                    writer.append("\n");
                }
            }
            
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
    }
    
    /**
     * Get SQL query for the report type
     */
    private String getReportQuery(String reportType, String parameters) {
        switch (reportType) {
            case "Inventory":
                return "SELECT p.id, p.sku, p.name, c.name as category, s.name as supplier, " +
                       "p.unit_price, p.stock_qty, p.reorder_level " +
                       "FROM Product p " +
                       "LEFT JOIN Category c ON p.category_id = c.id " +
                       "LEFT JOIN Supplier s ON p.supplier_id = s.id " +
                       "ORDER BY p.name";
                
            case "LowStock":
                return "SELECT p.id, p.sku, p.name, c.name as category, s.name as supplier, " +
                       "p.unit_price, p.stock_qty, p.reorder_level " +
                       "FROM Product p " +
                       "LEFT JOIN Category c ON p.category_id = c.id " +
                       "LEFT JOIN Supplier s ON p.supplier_id = s.id " +
                       "WHERE p.stock_qty <= p.reorder_level " +
                       "ORDER BY (p.reorder_level - p.stock_qty) DESC";
                
            case "Sales":
                return "SELECT o.id, o.order_date, c.name as customer, o.total_amount, o.status " +
                       "FROM \"Order\" o " +
                       "LEFT JOIN Customer c ON o.customer_id = c.id " +
                       "ORDER BY o.order_date DESC";
                
            case "TopProducts":
                return "SELECT p.name, SUM(oi.quantity) as quantity_sold, " +
                       "SUM(oi.quantity * oi.unit_price) as total_sales " +
                       "FROM OrderItem oi " +
                       "JOIN Product p ON oi.product_id = p.id " +
                       "GROUP BY p.name " +
                       "ORDER BY quantity_sold DESC " +
                       "LIMIT 10";
                
            default:
                return "SELECT * FROM Product";
        }
    }
    
    /**
     * Escape double quotes in CSV
     */
    private String escapeCsv(String value) {
        return value.replace("\"", "\"\"");
    }
}