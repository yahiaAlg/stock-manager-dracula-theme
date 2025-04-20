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
    
    public CategoryView(CategoryController controller) {
        this.controller = controller;
        setLayout(new BorderLayout(10, 10));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        initComponents();
        loadAllCategories();
    }
    
    private void initComponents() {
        // Search panel (top)
        JPanel searchPanel = new JPanel(new BorderLayout(5, 0));
        searchPanel.setBorder(BorderFactory.createTitledBorder("Search Categories"));
        
        JPanel searchFieldPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        
        searchField = new JTextField(30);
        searchFieldPanel.add(new JLabel("Search:"));
        searchFieldPanel.add(searchField);
        
        JPanel searchButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        searchButton = new JButton("Search");
        clearButton = new JButton("Clear");
        
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
        tablePanel.setBorder(BorderFactory.createTitledBorder("Categories"));
        
        String[] columnNames = {"ID", "Name", "Description"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        
        categoryTable = new JTable(tableModel);
        categoryTable.setFillsViewportHeight(true);
        categoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
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
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        addButton = new JButton("Add Category");
        editButton = new JButton("Edit");
        deleteButton = new JButton("Delete");
        
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
                "Please select a category to edit.", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void onDeleteButtonClicked(ActionEvent e) {
        int selectedRow = categoryTable.getSelectedRow();
        if (selectedRow >= 0 && selectedRow < currentCategories.size()) {
            Category selectedCategory = currentCategories.get(selectedRow);
            
            // Check if category is in use
            if (controller.isCategoryInUse(selectedCategory.getId())) {
                JOptionPane.showMessageDialog(this,
                    "This category cannot be deleted because it is in use by one or more products.",
                    "Category In Use",
                    JOptionPane.WARNING_MESSAGE);
                return;
            }
            
            int confirm = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to delete the category: " + selectedCategory.getName() + "?",
                "Confirm Deletion",
                JOptionPane.YES_NO_OPTION);
                
            if (confirm == JOptionPane.YES_OPTION) {
                boolean success = controller.deleteCategory(selectedCategory.getId());
                if (success) {
                    JOptionPane.showMessageDialog(this,
                        "Category deleted successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    loadAllCategories();
                } else {
                    JOptionPane.showMessageDialog(this,
                        "Error deleting category.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, 
                "Please select a category to delete.", 
                "No Selection", JOptionPane.WARNING_MESSAGE);
        }
    }
    
    private void showCategoryDialog(Category category) {
        // Create a dialog for adding/editing categories
        JDialog dialog = new JDialog(SwingUtilities.getWindowAncestor(this), "Category Details");
        dialog.setLayout(new BorderLayout());
        dialog.setSize(400, 250);
        dialog.setLocationRelativeTo(this);
        
        JPanel formPanel = new JPanel(new GridLayout(3, 2, 10, 10));
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JTextField idField = new JTextField();
        JTextField nameField = new JTextField();
        JTextArea descriptionField = new JTextArea();
        
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
        
        formPanel.add(new JLabel("ID:"));
        formPanel.add(idField);
        formPanel.add(new JLabel("Name:"));
        formPanel.add(nameField);
        formPanel.add(new JLabel("Description:"));
        
        // Use scroll pane for description
        JScrollPane descScrollPane = new JScrollPane(descriptionField);
        descriptionField.setLineWrap(true);
        descriptionField.setWrapStyleWord(true);
        formPanel.add(descScrollPane);
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton saveButton = new JButton("Save");
        JButton cancelButton = new JButton("Cancel");
        
        saveButton.addActionListener(e -> {
            try {
                // Validate input
                if (nameField.getText().trim().isEmpty()) {
                    JOptionPane.showMessageDialog(dialog, 
                        "Name is a required field.", 
                        "Validation Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
                
                Category cat = new Category();
                cat.setId(Integer.parseInt(idField.getText()));
                cat.setName(nameField.getText().trim());
                cat.setDescription(descriptionField.getText().trim());
                
                boolean success = controller.saveCategory(cat);
                
                if (success) {
                    JOptionPane.showMessageDialog(dialog,
                        "Category saved successfully.",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                    dialog.dispose();
                    loadAllCategories();
                } else {
                    JOptionPane.showMessageDialog(dialog,
                        "Error saving category. Please check your inputs.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                }
            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(dialog, 
                    "Please enter valid numeric values.", 
                    "Input Error", JOptionPane.ERROR_MESSAGE);
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
