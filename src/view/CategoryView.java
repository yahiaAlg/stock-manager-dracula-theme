package view;

import controller.CategoryController;
import model.Category;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.List;
import java.util.Locale;
import java.util.ResourceBundle;
import util.ArabicFontHelper;

public class CategoryView extends JPanel {
    
    private CategoryController controller;
    
    private JTable categoryTable;
    private DefaultTableModel tableModel;
    
    private JTextField searchField;
    
    private JButton searchButton;
    private JButton clearButton;
    private JButton addButton;
    private JButton editButton;
    private JButton deleteButton;
    
    private List<Category> currentCategories;
    private ResourceBundle messages;
    private boolean isRightToLeft;
    
    public CategoryView(CategoryController controller) {
        this.controller = controller;
        
        // Load localization resources
        loadLocalization();
        
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        initComponents();
        loadAllCategories();
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
        // Search panel (top)
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBorder(BorderFactory.createTitledBorder(messages.getString("categories.searchTitle")));
        
        JPanel searchFieldPanel = new JPanel(new FlowLayout(isRightToLeft ? FlowLayout.RIGHT : FlowLayout.LEFT));
        
        searchField = new JTextField(30);
        if (isRightToLeft) {
            searchField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        
        searchFieldPanel.add(new JLabel(messages.getString("common.search") + ":"));
        searchFieldPanel.add(searchField);
        
        JPanel searchButtonsPanel = new JPanel(new FlowLayout(isRightToLeft ? FlowLayout.LEFT : FlowLayout.RIGHT));
        searchButton = new JButton(messages.getString("button.search"));
        clearButton = new JButton(messages.getString("button.clear"));
        
        searchButton.addActionListener(this::onSearchButtonClicked);
        clearButton.addActionListener(e -> {
            searchField.setText("");
            loadAllCategories();
        });
        
        searchButtonsPanel.add(searchButton);
        searchButtonsPanel.add(clearButton);
        
        searchPanel.add(searchFieldPanel, BorderLayout.CENTER);
        searchPanel.add(searchButtonsPanel, BorderLayout.EAST);
        
        // Table panel (center)
        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setBorder(BorderFactory.createTitledBorder(messages.getString("categories.title")));
        
        String[] columnNames = {
            messages.getString("column.id"), 
            messages.getString("categories.column.name"), 
            messages.getString("categories.column.description")
        };
        
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        categoryTable = new JTable(tableModel);
        categoryTable.setFillsViewportHeight(true);
        categoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Apply RTL to table if needed
        if (isRightToLeft) {
            categoryTable.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            categoryTable.getTableHeader().setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        
        // Add double-click listener for editing
        categoryTable.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    onEditButtonClicked(null);
                }
            }
        });
        
        // Set column widths
        categoryTable.getColumnModel().getColumn(0).setPreferredWidth(50);
        categoryTable.getColumnModel().getColumn(1).setPreferredWidth(150);
        categoryTable.getColumnModel().getColumn(2).setPreferredWidth(300);
        
        JScrollPane scrollPane = new JScrollPane(categoryTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons panel (bottom)
        JPanel buttonsPanel = new JPanel(new FlowLayout(isRightToLeft ? FlowLayout.LEFT : FlowLayout.RIGHT));
        
        addButton = new JButton(messages.getString("categories.button.add"));
        editButton = new JButton(messages.getString("button.edit"));
        deleteButton = new JButton(messages.getString("button.delete"));
        
        addButton.addActionListener(e -> showCategoryDialog(null));
        editButton.addActionListener(this::onEditButtonClicked);
        deleteButton.addActionListener(this::onDeleteButtonClicked);
        
        buttonsPanel.add(addButton);
        buttonsPanel.add(editButton);
        buttonsPanel.add(deleteButton);
        
        // Add all panels to the main view
        add(searchPanel, BorderLayout.NORTH);
        add(tablePanel, BorderLayout.CENTER);
        add(buttonsPanel, BorderLayout.SOUTH);
    }
    
    public void loadAllCategories() {
        currentCategories = controller.getAllCategories();
        refreshCategoryTable();
    }
    
    private void refreshCategoryTable() {
        // Clear existing data
        tableModel.setRowCount(0);
        
        // Populate table with categories
        for (Category category : currentCategories) {
            Object[] rowData = {
                category.getId(),
                category.getName(),
                category.getDescription()
            };
            tableModel.addRow(rowData);
        }
    }
    
    private void onSearchButtonClicked(ActionEvent e) {
        String searchTerm = searchField.getText().trim();
        
        if (searchTerm.isEmpty()) {
            loadAllCategories();
        } else {
            currentCategories = controller.searchCategories(searchTerm);
            refreshCategoryTable();
        }
    }
    
    private void onEditButtonClicked(ActionEvent e) {
        int selectedRow = categoryTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < currentCategories.size()) {
            Category selectedCategory = currentCategories.get(selectedRow);
            showCategoryDialog(selectedCategory);
        } else {
            JOptionPane.showMessageDialog(this, 
                messages.getString("error.noSelection"),
                messages.getString("dialog.noSelection"), 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void onDeleteButtonClicked(ActionEvent e) {
        int selectedRow = categoryTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < currentCategories.size()) {
            Category selectedCategory = currentCategories.get(selectedRow);
            
            // Check if category is in use
            if (controller.isCategoryInUse(selectedCategory.getId())) {
                JOptionPane.showMessageDialog(this,
                    messages.getString("categories.error.inUse"),
                    messages.getString("dialog.categoryInUse"),
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int confirm = JOptionPane.showConfirmDialog(this,
                messages.getString("categories.confirm.delete").replace("{0}", selectedCategory.getName()),
                messages.getString("dialog.confirmDeletion"),
                JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = controller.deleteCategory(selectedCategory.getId());
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        messages.getString("categories.success.deleted"),
                        messages.getString("dialog.success"),
                        JOptionPane.INFORMATION_MESSAGE);
                    loadAllCategories();
                } else {
                    JOptionPane.showMessageDialog(this,
                        messages.getString("categories.error.delete"),
                        messages.getString("dialog.error"),
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                messages.getString("categories.error.selectToDelete"), 
                messages.getString("dialog.noSelection"), 
                JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void showCategoryDialog(Category category) {
        // Create a dialog for adding/editing categories
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), 
            messages.getString("dialog.categoryDetails"));
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        
        // Apply RTL orientation if needed
        if (isRightToLeft) {
            dialog.applyComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            ArabicFontHelper.applyArabicFont(dialog);
        }
        
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextArea descriptionField = new JTextArea();
        
        // Apply RTL to text components if needed
        if (isRightToLeft) {
            idField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            nameField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
            descriptionField.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
        
        // Populate fields if editing
        if (category != null) {
            idField.setText(String.valueOf(category.getId()));
            nameField.setText(category.getName());
            descriptionField.setText(category.getDescription());
        } else {
            // Default values for new category
            idField.setText("0");
        }
        
        // ID is not editable
        idField.setEditable(false);
        
        formPanel.add(new JLabel(messages.getString("column.id") + ":"));
        formPanel.add(idField);
        formPanel.add(new JLabel(messages.getString("categories.column.name") + ":"));
        formPanel.add(nameField);
        formPanel.add(new JLabel(messages.getString("categories.column.description") + ":"));
        
        // Use scroll pane for description
        JScrollPane descScrollPane = new JScrollPane(descriptionField);
        descriptionField.setLineWrap(true);
        descriptionField.setWrapStyleWord(true);
        formPanel.add(descScrollPane);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(isRightToLeft ? FlowLayout.LEFT : FlowLayout.RIGHT));
        JButton saveButton = new JButton(messages.getString("button.save"));
        JButton cancelButton = new JButton(messages.getString("button.cancel"));
        
        saveButton.addActionListener(e -> {
            try {
                // Validate input
                if (nameField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, 
                        messages.getString("categories.error.nameRequired"), 
                        messages.getString("dialog.validationError"), 
                        JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                Category cat = new Category();
                cat.setId(Integer.parseInt(idField.getText()));
                cat.setName(nameField.getText().trim());
                cat.setDescription(descriptionField.getText().trim());
                
                boolean success = controller.saveCategory(cat);
                
                if (success) {
                    JOptionPane.showMessageDialog(dialog,
                        messages.getString("categories.success.saved"),
                        messages.getString("dialog.success"),
                        JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadAllCategories();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                        messages.getString("categories.error.save"),
                        messages.getString("dialog.error"),
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, 
                    messages.getString("error.invalidNumber"), 
                    messages.getString("dialog.inputError"), 
                    JOptionPane.ERROR_MESSAGE);
            }
        });
        
        cancelButton.addActionListener(e -> dialog.dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        dialog.add(formPanel, BorderLayout.CENTER);
        dialog.add(buttonPanel, BorderLayout.SOUTH);
        
        dialog.setVisible(true);
    }
    
    public void refreshData() {
        loadAllCategories();
    }
}