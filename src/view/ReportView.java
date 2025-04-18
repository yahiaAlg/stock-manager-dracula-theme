package view;

import controller.ReportController;
import model.Report;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.io.File;
import java.text.SimpleDateFormat;
import java.util.List;

public class ReportView extends JPanel {
    
    private JTable reportsTable;
    private DefaultTableModel tableModel;
    
    private JComboBox<String> reportTypeCombo;
    private JComboBox<String> formatCombo;
    private JButton generateButton;
    private JButton viewButton;
    
    private ReportController reportController;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    public ReportView(ReportController reportController) {
        this.reportController = reportController;
        
        setLayout(new BorderLayout());
        
        // Create top panel with controls
        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        topPanel.add(new JLabel("Report Type:"));
        reportTypeCombo = new JComboBox<>(new String[] {"Inventory", "LowStock", "Sales", "TopProducts"});
        topPanel.add(reportTypeCombo);
        
        topPanel.add(new JLabel("Format:"));
        formatCombo = new JComboBox<>(new String[] {"CSV", "PDF"});
        topPanel.add(formatCombo);
        
        generateButton = new JButton("Generate Report");
        generateButton.addActionListener(this::generateReport);
        topPanel.add(generateButton);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Create reports table
        String[] columns = {"ID", "Type", "Generated On", "Format", "Actions"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only actions column is editable
            }
        };
        
        reportsTable = new JTable(tableModel);
        reportsTable.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        reportsTable.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(reportsTable));
        
        JScrollPane scrollPane = new JScrollPane(reportsTable);
        add(scrollPane, BorderLayout.CENTER);
        
        // Create bottom panel with view button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        viewButton = new JButton("View Selected Report");
        viewButton.addActionListener(this::viewSelectedReport);
        bottomPanel.add(viewButton);
        
        add(bottomPanel, BorderLayout.SOUTH);
        
        // Load initial data
        loadReports();
    }
    
    private void loadReports() {
        tableModel.setRowCount(0);
        List<Report> reports = reportController.getAllReports();
        
        for (Report report : reports) {
            addReportToTable(report);
        }
    }
    
    private void addReportToTable(Report report) {
        Object[] row = new Object[5];
        row[0] = report.getId();
        row[1] = report.getReportType();
        row[2] = dateFormat.format(report.getGeneratedOn());
        
        // Get format from file extension
        String filePath = report.getFilePath();
        String format = filePath.substring(filePath.lastIndexOf('.') + 1).toUpperCase();
        row[3] = format;
        
        row[4] = "View"; // Placeholder for button
        
        tableModel.addRow(row);
    }
    
    private void generateReport(ActionEvent e) {
        String reportType = reportTypeCombo.getSelectedItem().toString();
        String format = formatCombo.getSelectedItem().toString();
        
        // Simple parameters for now
        String parameters = "{}";
        
        // Generate the report
        Report report = reportController.generateReport(reportType, format, parameters);
        
        if (report != null) {
            JOptionPane.showMessageDialog(this,
                "Report generated successfully:\n" + report.getFilePath(),
                "Report Generated", JOptionPane.INFORMATION_MESSAGE);
            
            // Reload reports to include the new one
            loadReports();
        } else {
            JOptionPane.showMessageDialog(this,
                "Failed to generate report",
                "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void viewSelectedReport(ActionEvent e) {
        int selectedRow = reportsTable.getSelectedRow();
        if (selectedRow >= 0) {
            int reportId = (int) tableModel.getValueAt(selectedRow, 0);
            openReport(reportId);
        } else {
            JOptionPane.showMessageDialog(this,
                "Please select a report to view",
                "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void openReport(int reportId) {
        Report report = reportController.getReportById(reportId);
        
        if (report != null) {
            try {
                File file = new File(report.getFilePath());
                
                if (file.exists()) {
                    // Open the file with default system application
                    Desktop.getDesktop().open(file);
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Report file not found:\n" + report.getFilePath(),
                        "File Not Found", JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    "Error opening report: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    // Custom renderer for view button
    private class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setText("View");
        }
        
        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            if (isSelected) {
                setForeground(table.getSelectionForeground());
                setBackground(table.getSelectionBackground());
            } else {
                setForeground(table.getForeground());
                setBackground(UIManager.getColor("Button.background"));
            }
            return this;
        }
    }
    
    // Custom editor for view button
    private class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private int clickedRow;
        
        public ButtonEditor(JTable table) {
            super(new JTextField());
            
            button = new JButton("View");
            button.setOpaque(true);
            button.addActionListener(e -> {
                fireEditingStopped();
                int reportId = (int) tableModel.getValueAt(clickedRow, 0);
                openReport(reportId);
            });
        }
        
        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            clickedRow = row;
            return button;
        }
        
        @Override
        public Object getCellEditorValue() {
            return "View";
        }
    }
}