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
import java.util.Locale;
import java.util.ResourceBundle;
import util.ArabicFontHelper;

public class ReportView extends JPanel {
    
    private JTable reportsTable;
    private DefaultTableModel tableModel;
    
    private JComboBox<String> reportTypeCombo;
    private JComboBox<String> formatCombo;
    private JButton generateButton;
    private JButton viewButton;
    
    private ReportController reportController;
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    
    private ResourceBundle messages;
    private boolean isRightToLeft;
    
    public ReportView(ReportController reportController) {
        this.reportController = reportController;
        
        // Load localization resources
        loadLocalization();
        
        setLayout(new BorderLayout());
        initComponents();
        
        // Load initial data
        loadReports();
    }
    
    private void loadLocalization() {
        // Get current locale from LocaleManager
        Locale currentLocale = util.LocaleManager.getCurrentLocale();
        messages = ResourceBundle.getBundle("resources.Messages", currentLocale);
        
        // Configure component orientation based on locale
        isRightToLeft = currentLocale.getLanguage().equals("ar");
        if (isRightToLeft) {
            applyRightToLeftOrientation();
        }
    }
    
    private void applyRightToLeftOrientation() {
        // Set right-to-left orientation for this panel
        setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        
        // Apply Arabic font to this panel if needed
        if (isRightToLeft) {
            ArabicFontHelper.applyArabicFont(this);
        }
    }
    
    private void initComponents() {
        // Create top panel with controls
        JPanel topPanel = new JPanel(new FlowLayout(isRightToLeft ? FlowLayout.RIGHT : FlowLayout.LEFT));
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        topPanel.add(new JLabel(messages.getString("reports.type") + ":"));
        reportTypeCombo = new JComboBox<>(new String[] {
            messages.getString("reports.type.inventory"),
            messages.getString("reports.type.lowStock"),
            messages.getString("reports.type.sales"),
            messages.getString("reports.type.topProducts")
        });
        if (isRightToLeft) {
            reportTypeCombo.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        topPanel.add(reportTypeCombo);
        
        topPanel.add(new JLabel(messages.getString("reports.format") + ":"));
        formatCombo = new JComboBox<>(new String[] {"CSV", "PDF"});
        if (isRightToLeft) {
            formatCombo.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        topPanel.add(formatCombo);
        
        generateButton = new JButton(messages.getString("reports.button.generate"));
        generateButton.addActionListener(this::generateReport);
        topPanel.add(generateButton);
        
        add(topPanel, BorderLayout.NORTH);
        
        // Create reports table in a panel with a title
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder(messages.getString("reports.title")));
        
        String[] columns = {
            messages.getString("column.id"),
            messages.getString("reports.column.type"),
            messages.getString("reports.column.generatedOn"),
            messages.getString("reports.column.format"),
            messages.getString("reports.column.actions")
        };
        
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4; // Only actions column is editable
            }
        };
        
        reportsTable = new JTable(tableModel);
        reportsTable.getColumnModel().getColumn(4).setCellRenderer(new ButtonRenderer());
        reportsTable.getColumnModel().getColumn(4).setCellEditor(new ButtonEditor(reportsTable));
        
        // Apply RTL to table if needed
        if (isRightToLeft) {
            reportsTable.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            reportsTable.getTableHeader().setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        
        JScrollPane scrollPane = new JScrollPane(reportsTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        add(tablePanel, BorderLayout.CENTER);
        
        // Create bottom panel with view button
        JPanel bottomPanel = new JPanel(new FlowLayout(isRightToLeft ? FlowLayout.LEFT : FlowLayout.RIGHT));
        bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        viewButton = new JButton(messages.getString("reports.button.viewSelected"));
        viewButton.addActionListener(this::viewSelectedReport);
        bottomPanel.add(viewButton);
        
        add(bottomPanel, BorderLayout.SOUTH);
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
        row[1] = getLocalizedReportType(report.getReportType());
        row[2] = dateFormat.format(report.getGeneratedOn());
        
        // Get format from file extension
        String filePath = report.getFilePath();
        String format = filePath.substring(filePath.lastIndexOf('.') + 1).toUpperCase();
        row[3] = format;
        
        row[4] = messages.getString("button.view"); // Placeholder for button
        
        tableModel.addRow(row);
    }
    
    private String getLocalizedReportType(String reportType) {
        // Map the report type to localized strings
        String key = "reports.type." + reportType.toLowerCase();
        try {
            return messages.getString(key);
        } catch (Exception e) {
            return reportType; // Fallback to the original if no translation found
        }
    }
    
    private void generateReport(ActionEvent e) {
        String reportType = getReportTypeValue(reportTypeCombo.getSelectedIndex());
        String format = formatCombo.getSelectedItem().toString();
        
        // Simple parameters for now
        String parameters = "{}";
        
        // Generate the report
        Report report = reportController.generateReport(reportType, format, parameters);
        
        if (report != null) {
            JOptionPane.showMessageDialog(this,
                messages.getString("reports.success.generated") + ":\n" + report.getFilePath(),
                messages.getString("dialog.success"),
                JOptionPane.INFORMATION_MESSAGE);
            
            // Reload reports to include the new one
            loadReports();
        } else {
            JOptionPane.showMessageDialog(this,
                messages.getString("reports.error.generate"),
                messages.getString("dialog.error"),
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private String getReportTypeValue(int selectedIndex) {
        // Convert combobox selection to actual report type value
        switch (selectedIndex) {
            case 0: return "Inventory";
            case 1: return "LowStock";
            case 2: return "Sales";
            case 3: return "TopProducts";
            default: return "Inventory";
        }
    }
    
    private void viewSelectedReport(ActionEvent e) {
        int selectedRow = reportsTable.getSelectedRow();
        if (selectedRow >= 0) {
            int reportId = (int) tableModel.getValueAt(selectedRow, 0);
            openReport(reportId);
        } else {
            JOptionPane.showMessageDialog(this,
                messages.getString("reports.error.selectToView"),
                messages.getString("dialog.noSelection"),
                JOptionPane.WARNING_MESSAGE);
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
                        messages.getString("reports.error.fileNotFound") + ":\n" + report.getFilePath(),
                        messages.getString("dialog.fileNotFound"),
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                    messages.getString("reports.error.opening") + ": " + ex.getMessage(),
                    messages.getString("dialog.error"),
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public void refreshData() {
        loadReports();
    }

    // Custom renderer for view button
    private class ButtonRenderer extends JButton implements javax.swing.table.TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setText(messages.getString("button.view"));
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
            
            button = new JButton(messages.getString("button.view"));
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
            return messages.getString("button.view");
        }
    }
}