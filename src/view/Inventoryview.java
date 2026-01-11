package view;

import controller.InventoryController;
import util.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Inventoryview extends BaseView {
    private InventoryController inventoryController;
    private JTable inventoryTable;
    private DefaultTableModel tableModel;
    private JButton addButton, restockButton, exportButton, lowStockButton, backButton;
    private JComboBox<String> filterCombo;
    private DashboardView dashboardView;

    private List<Object[]> originalData = new ArrayList<>();
    private JPanel statsPanel;

    public Inventoryview(DashboardView dashboardView) {
        super("Inventory Management");
        inventoryController = new InventoryController();
        this.dashboardView = dashboardView;
    }

    @Override
    protected void initializeUI() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);

        createHeader();
        createToolbar();
        createTable();
        createStatsPanel();

        add(mainPanel);
        loadInventoryData();
        setupEventListeners();
    }

    @Override
    protected void setupEventListeners() {
        addButton.addActionListener(e -> addInventoryItem());
        restockButton.addActionListener(e -> restockItem());
        lowStockButton.addActionListener(e -> showLowStockItems());
        exportButton.addActionListener(e -> exportInventory());
        backButton.addActionListener(e -> goBackToDashboard());

        filterCombo.addActionListener(e -> {
            String filter = (String) filterCombo.getSelectedItem();
            if (filter != null) {
                filterInventory(filter);
            }
        });
    }

    private void createHeader() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JLabel titleLabel = new JLabel("📦 Inventory Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        backButton = createStyledButton("← Back to Dashboard", Color.WHITE, PRIMARY_COLOR);
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        backButton.setPreferredSize(new Dimension(180, 35));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(backButton, BorderLayout.EAST);
    }

    private void createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Color.lightGray);
        toolbar.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setBackground(Color.lightGray);

        filterCombo = new JComboBox<>(new String[]{"All Items", "Low Stock", "Fertilizer", "Equipment", "Tools", "Seeds", "Chemicals"});
        filterCombo.setPreferredSize(new Dimension(150, 30));
        filterPanel.add(new JLabel("Filter:"));
        filterPanel.add(filterCombo);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setBackground(Color.lightGray);

        lowStockButton = createStyledButton("⚠️ Low Stock", new Color(244, 180, 0), Color.black);
        lowStockButton.setPreferredSize(new Dimension(120, 30));

        addButton = createStyledButton("➕ Add Item", new Color(66, 133, 244), Color.black);
        addButton.setPreferredSize(new Dimension(120, 30));

        restockButton = createStyledButton("🔄 Restock", SECONDARY_COLOR, Color.black);
        restockButton.setPreferredSize(new Dimension(120, 30));

        exportButton = createStyledButton("📤 Export", Color.DARK_GRAY, Color.black);
        exportButton.setPreferredSize(new Dimension(120, 30));

        actionPanel.add(lowStockButton);
        actionPanel.add(addButton);
        actionPanel.add(restockButton);
        actionPanel.add(exportButton);

        toolbar.add(filterPanel, BorderLayout.WEST);
        toolbar.add(actionPanel, BorderLayout.EAST);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(headerPanel, BorderLayout.NORTH);
        topContainer.add(toolbar, BorderLayout.SOUTH);

        mainPanel.add(topContainer, BorderLayout.NORTH);
    }

    private void createTable() {
        String[] columns = {"Greenhouse", "Item", "Stock", "Category", "Price", "Supplier"};

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        inventoryTable = new JTable(tableModel);
        inventoryTable.setRowHeight(35);
        inventoryTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        inventoryTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        inventoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        inventoryTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 248));
                } else {
                    c.setBackground(table.getSelectionBackground());
                }

                if (column == 2) {
                    try {
                        String stockStr = value.toString();
                        if (!stockStr.isEmpty()) {
                            int stock = Integer.parseInt(stockStr);
                            if (stock < 10) {
                                c.setBackground(new Color(255, 243, 205));
                                c.setForeground(new Color(219, 68, 55));
                                c.setFont(c.getFont().deriveFont(Font.BOLD));
                            }
                        }
                    } catch (NumberFormatException e) {
                        // Ignore format errors
                    }
                }

                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(inventoryTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        mainPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void createStatsPanel() {
        statsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        statsPanel.setBackground(Color.lightGray);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel lowStockPanel = createStatCard("Low Stock", "0", "⚠️", new Color(244, 180, 0));
        JPanel totalPanel = createStatCard("Total Items", "0", "📦", new Color(46, 125, 50));
        JPanel valuePanel = createStatCard("Total Value", "$0.00", "💰", new Color(66, 133, 244));

        statsPanel.add(lowStockPanel);
        statsPanel.add(totalPanel);
        statsPanel.add(valuePanel);

        mainPanel.add(statsPanel, BorderLayout.SOUTH);
    }

    private JPanel createStatCard(String title, String value, String icon, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 1),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        titleLabel.setForeground(Color.DARK_GRAY);

        JLabel valueLabel = new JLabel(icon + " " + value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        valueLabel.setForeground(color);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setBackground(CARD_COLOR);
        contentPanel.add(titleLabel, BorderLayout.NORTH);
        contentPanel.add(valueLabel, BorderLayout.CENTER);

        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }

    private void loadInventoryData() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            tableModel.setRowCount(0);
            if (originalData != null) {
                originalData.clear();
            } else {
                originalData = new ArrayList<>();
            }

            conn = DatabaseConnection.getConnection();

            String query = "SELECT g.name as greenhouse_name, i.Description, i.stock, " +
                    "COALESCE(i.category, 'Unknown') as category, " +
                    "COALESCE(i.unit_price, 0) as unit_price, " +
                    "'N/A' as supplier " +
                    "FROM Inventory i " +
                    "LEFT JOIN Greenhouse g ON i.greenhouse_ID = g.greenhouse_id " +
                    "ORDER BY g.name, i.Description";

            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            int rowCount = 0;
            while (rs.next()) {
                Object[] row = new Object[]{
                        rs.getString("greenhouse_name"),
                        rs.getString("Description"),
                        rs.getInt("stock"),
                        rs.getString("category"),
                        String.format("$%.2f", rs.getDouble("unit_price")),
                        rs.getString("supplier")
                };
                tableModel.addRow(row);
                originalData.add(row);
                rowCount++;
            }

            if (rowCount == 0) {
                addSampleData();
            } else {
                showSuccess("Loaded " + rowCount + " inventory items");
            }

            updateStatsPanel();

        } catch (SQLException e) {
            System.err.println("SQL Error loading inventory: " + e.getMessage());
            e.printStackTrace();
            showError("Database error: " + e.getMessage());
            addSampleData();
        } catch (Exception e) {
            System.err.println("General Error: " + e.getMessage());
            e.printStackTrace();
            showError("Error: " + e.getMessage());
            addSampleData();
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
    }

    private void addSampleData() {
        Object[][] sampleData = {
                {"Main Greenhouse", "Tomato Seeds", 50, "Seeds", "rs.2.50", "SeedCo Inc."},
                {"Main Greenhouse", "Fertilizer 5kg", 5, "Fertilizer", "rs.15.99", "GreenGrow Ltd."},
                {"Research Greenhouse", "Pruning Shears", 15, "Tools", "rs.8.75", "GardenTools Co."},
                {"Tropical Greenhouse", "Insecticide Spray", 30, "Chemicals", "rs.12.50", "PestControl Corp."},
                {"Seedling Greenhouse", "Watering Hose", 8, "Equipment", "rs.25.00", "Irrigation Supplies"},
                {"Hydroponic Greenhouse", "pH Testing Kit", 25, "Tools", "rs.18.50", "LabSupplies Inc."}
        };

        for (Object[] row : sampleData) {
            tableModel.addRow(row);
            originalData.add(row);
        }

        updateStatsPanel();
        showSuccess("Loaded sample inventory data");
    }

    private void updateStatsPanel() {
        SwingUtilities.invokeLater(() -> {
            int lowStockCount = 0;
            int totalItems = tableModel.getRowCount();
            double totalValue = 0;

            for (int i = 0; i < tableModel.getRowCount(); i++) {
                try {
                    Object stockObj = tableModel.getValueAt(i, 2);
                    if (stockObj != null) {
                        int stock = Integer.parseInt(stockObj.toString());
                        if (stock < 10) {
                            lowStockCount++;
                        }
                    }

                    Object priceObj = tableModel.getValueAt(i, 4);
                    if (priceObj != null) {
                        String priceStr = priceObj.toString().replace("$", "").trim();
                        double price = 0;
                        try {
                            price = Double.parseDouble(priceStr);
                        } catch (NumberFormatException e) {
                            try {
                                price = Double.parseDouble(priceStr.replace(",", ""));
                            } catch (NumberFormatException e2) {
                                // Ignore if still can't parse
                            }
                        }
                        totalValue += price;
                    }
                } catch (NumberFormatException e) {
                    // Ignore format errors
                }
            }

            if (statsPanel != null) {
                statsPanel.removeAll();

                JPanel lowStockCard = createStatCard("Low Stock", String.valueOf(lowStockCount), "⚠️", new Color(244, 180, 0));
                JPanel totalCard = createStatCard("Total Items", String.valueOf(totalItems), "📦", new Color(46, 125, 50));
                JPanel valueCard = createStatCard("Total Value", String.format("rs.%.2f", totalValue), "💰", new Color(66, 133, 244));

                statsPanel.add(lowStockCard);
                statsPanel.add(totalCard);
                statsPanel.add(valueCard);

                statsPanel.revalidate();
                statsPanel.repaint();
            }
        });
    }

    private void addInventoryItem() {
        JDialog addDialog = new JDialog(this, "Add Inventory Item", true);
        addDialog.setSize(400, 500);
        addDialog.setLocationRelativeTo(this);
        addDialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Item Name
        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Item Name:*"), gbc);

        gbc.gridx = 1;
        JTextField nameField = new JTextField(20);
        formPanel.add(nameField, gbc);

        // Stock
        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Stock:*"), gbc);

        gbc.gridx = 1;
        JTextField stockField = new JTextField(10);
        stockField.setText("1");
        formPanel.add(stockField, gbc);

        // Category
        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Category:*"), gbc);

        gbc.gridx = 1;
        JComboBox<String> categoryCombo = new JComboBox<>(new String[]{
                "Seeds", "Fertilizer", "Tools", "Chemicals", "Equipment", "Other"
        });
        formPanel.add(categoryCombo, gbc);

        // Unit Price
        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Unit Price:*"), gbc);

        gbc.gridx = 1;
        JTextField priceField = new JTextField(10);
        priceField.setText("0.00");
        formPanel.add(priceField, gbc);

        // Supplier
        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Supplier:"), gbc);

        gbc.gridx = 1;
        JTextField supplierField = new JTextField(20);
        supplierField.setText("Unknown");
        formPanel.add(supplierField, gbc);

        // Greenhouse
        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(new JLabel("Greenhouse:*"), gbc);

        gbc.gridx = 1;
        JComboBox<String> greenhouseCombo = new JComboBox<>();
        loadGreenhousesIntoComboBox(greenhouseCombo);
        formPanel.add(greenhouseCombo, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        JButton saveButton = new JButton("Save");
        saveButton.setBackground(new Color(46, 125, 50));
        saveButton.setForeground(Color.BLACK);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(new Color(219, 68, 55));
        cancelButton.setForeground(Color.BLACK);

        saveButton.addActionListener(e -> {
            String itemName = nameField.getText().trim();
            String stockStr = stockField.getText().trim();
            String category = (String) categoryCombo.getSelectedItem();
            String priceStr = priceField.getText().trim();
            String supplier = supplierField.getText().trim();
            String selectedGreenhouse = (String) greenhouseCombo.getSelectedItem();

            if (itemName.isEmpty() || stockStr.isEmpty() || priceStr.isEmpty() ||
                    selectedGreenhouse == null || selectedGreenhouse.equals("Select Greenhouse")) {
                JOptionPane.showMessageDialog(addDialog,
                        "Please fill in all required fields (*)",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int stock = Integer.parseInt(stockStr);
                double price = Double.parseDouble(priceStr);

                int greenhouseId = 1;
                if (selectedGreenhouse.contains("[")) {
                    String idStr = selectedGreenhouse.substring(selectedGreenhouse.indexOf("[") + 1, selectedGreenhouse.indexOf("]"));
                    greenhouseId = Integer.parseInt(idStr);
                }

                String greenhouseName = selectedGreenhouse.replaceAll(" \\[\\d+\\]$", "");

                Connection testConn = null;
                try {
                    testConn = DatabaseConnection.getConnection();
                    if (testConn == null || testConn.isClosed()) {
                        JOptionPane.showMessageDialog(addDialog,
                                "Database connection failed. Cannot add item.",
                                "Database Error",
                                JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                } catch (SQLException ex) {
                    JOptionPane.showMessageDialog(addDialog,
                            "Database connection error: " + ex.getMessage(),
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                } finally {
                    DatabaseConnection.closeConnection(testConn);
                }

                Connection conn = null;
                PreparedStatement stmt = null;

                try {
                    conn = DatabaseConnection.getConnection();

                    int nextNumber = getNextItemNumber();

                    System.out.println("DEBUG: Adding item to database:");
                    System.out.println("  Greenhouse ID: " + greenhouseId);
                    System.out.println("  Description: " + itemName);
                    System.out.println("  Stock: " + stock);
                    System.out.println("  Number: " + nextNumber);
                    System.out.println("  Category: " + category);
                    System.out.println("  Unit Price: " + price);

                    String query = "INSERT INTO Inventory (greenhouse_ID, Description, stock, number, category, unit_price) " +
                            "VALUES (?, ?, ?, ?, ?, ?)";

                    stmt = conn.prepareStatement(query);
                    stmt.setInt(1, greenhouseId);
                    stmt.setString(2, itemName);
                    stmt.setInt(3, stock);
                    stmt.setInt(4, nextNumber);
                    stmt.setString(5, category);
                    stmt.setDouble(6, price);

                    int rowsAffected = stmt.executeUpdate();
                    System.out.println("DEBUG: Rows affected: " + rowsAffected);

                    if (rowsAffected > 0) {
                        // Add to table
                        Object[] newRow = new Object[]{
                                greenhouseName,
                                itemName,
                                stock,
                                category,
                                String.format("rs.%.2f", price),
                                supplier
                        };
                        tableModel.addRow(newRow);
                        originalData.add(newRow);

                        showSuccess("Inventory item added successfully!");
                        updateStatsPanel();
                        addDialog.dispose();
                    } else {
                        JOptionPane.showMessageDialog(addDialog,
                                "Failed to add item to database (no rows affected)",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    System.err.println("DEBUG: SQL Error: " + ex.getMessage());
                    System.err.println("DEBUG: SQL State: " + ex.getSQLState());
                    System.err.println("DEBUG: Error Code: " + ex.getErrorCode());

                    String errorMsg = ex.getMessage();
                    if (ex.getMessage().contains("foreign key constraint")) {
                        errorMsg = "Invalid greenhouse ID. Please select a valid greenhouse.";
                    } else if (ex.getMessage().contains("Duplicate entry")) {
                        errorMsg = "Item with this name already exists in this greenhouse.";
                    }

                    JOptionPane.showMessageDialog(addDialog,
                            "Database error: " + errorMsg,
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    try { if (stmt != null) stmt.close(); } catch (SQLException ex) {}
                    DatabaseConnection.closeConnection(conn);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(addDialog,
                        "Stock and Price must be valid numbers",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> addDialog.dispose());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        addDialog.add(formPanel, BorderLayout.CENTER);
        addDialog.add(buttonPanel, BorderLayout.SOUTH);
        addDialog.setVisible(true);
    }

    private void loadGreenhousesIntoComboBox(JComboBox<String> combo) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            String query = "SELECT greenhouse_id, name FROM Greenhouse WHERE active = 1 ORDER BY name";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            combo.addItem("Select Greenhouse");
            while (rs.next()) {
                combo.addItem(rs.getString("name") + " [" + rs.getInt("greenhouse_id") + "]");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            combo.addItem("Main Greenhouse [1]");
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
    }

    private int getNextItemNumber() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int nextNumber = 1;

        try {
            conn = DatabaseConnection.getConnection();
            String query = "SELECT MAX(number) as max_number FROM Inventory";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            if (rs.next()) {
                nextNumber = rs.getInt("max_number") + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            nextNumber = 1;
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
        return nextNumber;
    }

    private void restockItem() {
        int row = inventoryTable.getSelectedRow();
        if (row >= 0) {
            String itemName = (String) tableModel.getValueAt(row, 1);
            String greenhouse = (String) tableModel.getValueAt(row, 0);

            try {
                int currentStock = Integer.parseInt(tableModel.getValueAt(row, 2).toString());

                String qtyStr = JOptionPane.showInputDialog(this,
                        "Enter restock quantity for " + itemName + ":\n" +
                                "Current stock: " + currentStock,
                        "Restock Item",
                        JOptionPane.QUESTION_MESSAGE);

                if (qtyStr != null && !qtyStr.trim().isEmpty()) {
                    try {
                        int addQty = Integer.parseInt(qtyStr.trim());

                        if (addQty <= 0) {
                            showError("Quantity must be positive");
                            return;
                        }

                        int newStock = currentStock + addQty;
                        tableModel.setValueAt(newStock, row, 2);

                        if (originalData != null && row < originalData.size()) {
                            originalData.get(row)[2] = newStock;
                        }

                        showSuccess("Restocked successfully! New stock: " + newStock);
                        updateStatsPanel();

                    } catch (NumberFormatException e) {
                        showError("Invalid quantity. Please enter a number.");
                    }
                }
            } catch (NumberFormatException e) {
                showError("Invalid current stock value");
            }
        } else {
            showError("Please select an item to restock");
        }
    }

    private void showLowStockItems() {
        StringBuilder lowStockItems = new StringBuilder("Low Stock Items:\n\n");
        int lowStockCount = 0;

        for (int i = 0; i < tableModel.getRowCount(); i++) {
            try {
                Object stockObj = tableModel.getValueAt(i, 2);
                if (stockObj != null) {
                    int stock = Integer.parseInt(stockObj.toString());
                    if (stock < 10) {
                        lowStockCount++;
                        lowStockItems.append("• ")
                                .append(tableModel.getValueAt(i, 1))
                                .append(" at ")
                                .append(tableModel.getValueAt(i, 0))
                                .append(": ")
                                .append(stock)
                                .append(" remaining\n");
                    }
                }
            } catch (NumberFormatException e) {
                // Ignore format errors
            }
        }

        if (lowStockCount > 0) {
            JOptionPane.showMessageDialog(this,
                    lowStockItems.toString(),
                    "Low Stock Alerts (" + lowStockCount + " items)",
                    JOptionPane.WARNING_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this,
                    "No low stock items found.",
                    "Low Stock Alerts",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private void exportInventory() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Inventory Data");
        fileChooser.setSelectedFile(new java.io.File("inventory_export.csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    writer.print("\"" + tableModel.getColumnName(i) + "\"");
                    if (i < tableModel.getColumnCount() - 1) {
                        writer.print(",");
                    }
                }
                writer.println();

                for (int row = 0; row < tableModel.getRowCount(); row++) {
                    for (int col = 0; col < tableModel.getColumnCount(); col++) {
                        Object value = tableModel.getValueAt(row, col);
                        writer.print("\"" + (value != null ? value.toString() : "") + "\"");
                        if (col < tableModel.getColumnCount() - 1) {
                            writer.print(",");
                        }
                    }
                    writer.println();
                }

                showSuccess("Inventory data exported to: " + file.getAbsolutePath());
            } catch (Exception e) {
                showError("Failed to export data: " + e.getMessage());
            }
        }
    }

    private void filterInventory(String filter) {
        if (originalData == null || originalData.isEmpty()) {
            loadInventoryData();
            return;
        }

        tableModel.setRowCount(0);

        if (filter.equals("All Items")) {
            for (Object[] row : originalData) {
                tableModel.addRow(row);
            }
        } else if (filter.equals("Low Stock")) {
            for (Object[] row : originalData) {
                try {
                    Object stockObj = row[2];
                    if (stockObj != null) {
                        int stock = Integer.parseInt(stockObj.toString());
                        if (stock < 10) {
                            tableModel.addRow(row);
                        }
                    }
                } catch (NumberFormatException e) {
                    // Ignore format errors
                }
            }
        } else {
            for (Object[] row : originalData) {
                Object categoryObj = row[3];
                if (categoryObj != null && categoryObj.toString().equalsIgnoreCase(filter)) {
                    tableModel.addRow(row);
                }
            }
        }
        updateStatsPanel();
    }

    private void goBackToDashboard() {
        if (showConfirm("Return to Dashboard",
                "Are you sure you want to return to the dashboard?")) {
            if (dashboardView != null) {
                dashboardView.setVisible(true);
                dashboardView.refreshDashboardData();
            }
            this.dispose();
        }
    }
}