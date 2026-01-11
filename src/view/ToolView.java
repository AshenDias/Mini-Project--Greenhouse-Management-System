package view;

import controller.ToolController;
import util.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ToolView extends BaseView {
    private ToolController toolController;
    private JTable toolTable;
    private DefaultTableModel tableModel;
    private JButton borrowButton, returnButton, maintenanceButton, backButton;
    private DashboardView dashboardView;
    private JComboBox<String> filterCombo;
    private JButton addButton;

    private List<Object[]> originalData = new ArrayList<>();
    private JPanel statsPanel;

    public ToolView(DashboardView dashboardView) {
        super("Tool Management");
        toolController = new ToolController();
        this.dashboardView = dashboardView;
    }

    @Override
    protected void initializeUI() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);

        createHeader();
        createToolbar();
        createTable();
        createStatusPanel();

        add(mainPanel);
        loadToolData();
        setupEventListeners();
    }

    private void createHeader() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JLabel titleLabel = new JLabel("🔧 Tool Monitoring");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        backButton = createStyledButton("← Back to Dashboard", Color.WHITE, new Color(0, 0, 0));
        backButton.setPreferredSize(new Dimension(180, 35));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(backButton, BorderLayout.EAST);
    }

    private void createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Color.lightGray);
        toolbar.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterCombo = new JComboBox<>(new String[]{"All", "In Use", "Available", "Needs Maintenance"});
        filterPanel.add(new JLabel("Filter:"));
        filterPanel.setBackground(Color.lightGray);
        filterPanel.add(filterCombo);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(Color.lightGray);
        borrowButton = createStyledButton("📝 Check Out", new Color(0, 150, 136), Color.black);
        returnButton = createStyledButton("↩️ Check In", SECONDARY_COLOR, Color.black);
        maintenanceButton = createStyledButton("🔧 Maintenance", new Color(244, 180, 0), Color.black);
        addButton = createStyledButton("➕ Add Tool", PRIMARY_COLOR, Color.black);

        actionPanel.add(borrowButton);
        actionPanel.add(returnButton);
        actionPanel.add(maintenanceButton);
        actionPanel.add(addButton);

        toolbar.add(filterPanel, BorderLayout.WEST);
        toolbar.add(actionPanel, BorderLayout.EAST);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(headerPanel, BorderLayout.NORTH);
        topContainer.add(toolbar, BorderLayout.SOUTH);

        mainPanel.add(topContainer, BorderLayout.NORTH);
    }

    private void createTable() {
        String[] columns = {"Tool Name", "Quantity", "Assigned To", "Condition", "Status", "Last Maintenance"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        toolTable = new JTable(tableModel);
        toolTable.setRowHeight(35);
        toolTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        toolTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (column == 4) {
                    String status = (String) value;
                    if ("In Use".equals(status)) {
                        c.setBackground(new Color(232, 244, 253));
                    } else if ("Under Maintenance".equals(status)) {
                        c.setBackground(new Color(255, 243, 205));
                    }
                }

                if (column == 3) {
                    String condition = (String) value;
                    switch (condition) {
                        case "Excellent":
                        case "New":
                            c.setForeground(new Color(46, 125, 50));
                            break;
                        case "Good":
                            c.setForeground(new Color(76, 175, 80));
                            break;
                        case "Fair":
                            c.setForeground(new Color(244, 180, 0));
                            break;
                        case "Poor":
                            c.setForeground(new Color(244, 67, 54));
                            break;
                        case "Broken":
                            c.setForeground(new Color(183, 28, 28));
                            break;
                    }
                }

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 248));
                }

                return c;
            }
        });
        JScrollPane scrollPane = new JScrollPane(toolTable);
        mainPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void createStatusPanel() {
        statsPanel = new JPanel(new GridLayout(1, 4, 10, 0));
        statsPanel.setBackground(Color.lightGray);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        JPanel totalCard = createStatusCard("Total Tools", "0", "🔧");
        JPanel inUseCard = createStatusCard("In Use", "0", "📝");
        JPanel availableCard = createStatusCard("Available", "0", "✅");
        JPanel maintenanceCard = createStatusCard("Maintenance", "0", "⚠️");

        statsPanel.add(totalCard);
        statsPanel.add(inUseCard);
        statsPanel.add(availableCard);
        statsPanel.add(maintenanceCard);

        mainPanel.add(statsPanel, BorderLayout.SOUTH);
    }

    private JPanel createStatusCard(String title, String value, String icon) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel valueLabel = new JLabel(icon + " " + value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));

        if (title.equals("In Use")) {
            valueLabel.setForeground(new Color(0, 150, 136));
        } else if (title.equals("Maintenance")) {
            valueLabel.setForeground(new Color(244, 180, 0));
        } else if (title.equals("Available")) {
            valueLabel.setForeground(new Color(46, 125, 50));
        } else {
            valueLabel.setForeground(new Color(66, 133, 244));
        }

        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private void loadToolData() {
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

            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "tool", null);
            if (!tables.next()) {
                showError("Tool table not found in database");
                addSampleData();
                updateStatsFromTableData();
                return;
            }
            tables.close();

            String query = "SELECT toolname, connu, " +
                    "CASE WHEN handovertime IS NOT NULL THEN 'In Use' ELSE 'Available' END as status, " +
                    "tool_type, `condition`, last_maintenance " +
                    "FROM tool " +
                    "ORDER BY status, toolname";

            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = new Object[]{
                        rs.getString("toolname"),
                        rs.getInt("connu"),
                        rs.getString("status").equals("In Use") ? "Worker Assigned" : "-",
                        rs.getString("condition"),
                        rs.getString("status"),
                        rs.getDate("last_maintenance")
                };
                tableModel.addRow(row);
                originalData.add(row);
            }

            if (tableModel.getRowCount() == 0) {
                addSampleData();
            } else {
                showSuccess("Loaded " + tableModel.getRowCount() + " tools from database");
            }

            updateStatsFromTableData();

        } catch (SQLException e) {
            System.err.println("SQL Error loading tool data: " + e.getMessage());
            e.printStackTrace();
            showError("Database error: " + e.getMessage());
            addSampleData();
            updateStatsFromTableData();
        } catch (Exception e) {
            System.err.println("General Error loading tool data: " + e.getMessage());
            e.printStackTrace();
            showError("Error: " + e.getMessage());
            addSampleData();
            updateStatsFromTableData();
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
    }

    private void addSampleData() {
        Object[][] sampleData = {
                {"Pruning Shears", 8, "-", "Good", "Available", java.sql.Date.valueOf("2024-01-15")},
                {"Watering Hose", 4, "Worker Assigned", "Fair", "In Use", java.sql.Date.valueOf("2024-01-10")},
                {"Soil Thermometer", 6, "-", "Excellent", "Available", java.sql.Date.valueOf("2024-02-01")},
                {"Gardening Gloves", 20, "-", "Good", "Available", null},
                {"Spade", 5, "-", "Good", "Available", java.sql.Date.valueOf("2024-01-30")}
        };

        for (Object[] row : sampleData) {
            tableModel.addRow(row);
            originalData.add(row);
        }

        showSuccess("Loaded sample tool data");
    }

    @Override
    protected void setupEventListeners() {
        addButton.addActionListener(e -> addTool());
        borrowButton.addActionListener(e -> borrowTool());
        returnButton.addActionListener(e -> returnTool());
        maintenanceButton.addActionListener(e -> reportMaintenance());
        backButton.addActionListener(e -> goBackToDashboard());
        filterCombo.addActionListener(e -> filterTools());

        toolTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = toolTable.getSelectedRow() >= 0;
                borrowButton.setEnabled(hasSelection);
                returnButton.setEnabled(hasSelection);
                maintenanceButton.setEnabled(hasSelection);
            }
        });

        boolean hasSelection = toolTable.getSelectedRow() >= 0;
        borrowButton.setEnabled(hasSelection);
        returnButton.setEnabled(hasSelection);
        maintenanceButton.setEnabled(hasSelection);
    }

    private int getTotalToolsCount() {
        int totalTools = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            try {
                Object quantityObj = tableModel.getValueAt(i, 1);
                if (quantityObj != null) {
                    totalTools += Integer.parseInt(quantityObj.toString());
                }
            } catch (NumberFormatException e) {
            }
        }
        return totalTools;
    }

    private int getToolsInUseCount() {
        int inUseCount = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object statusObj = tableModel.getValueAt(i, 4);
            if (statusObj != null && "In Use".equals(statusObj.toString())) {
                try {
                    Object quantityObj = tableModel.getValueAt(i, 1);
                    if (quantityObj != null) {
                        inUseCount += Integer.parseInt(quantityObj.toString());
                    }
                } catch (NumberFormatException e) {
                }
            }
        }
        return inUseCount;
    }

    private int getAvailableToolsCount() {
        int availableCount = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object statusObj = tableModel.getValueAt(i, 4);
            if (statusObj != null && "Available".equals(statusObj.toString())) {
                try {
                    Object quantityObj = tableModel.getValueAt(i, 1);
                    if (quantityObj != null) {
                        availableCount += Integer.parseInt(quantityObj.toString());
                    }
                } catch (NumberFormatException e) {
                }
            }
        }
        return availableCount;
    }

    private int getMaintenanceToolsCount() {
        int maintenanceCount = 0;
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Object conditionObj = tableModel.getValueAt(i, 3);
            if (conditionObj != null) {
                String condition = conditionObj.toString();
                if ("Poor".equals(condition) || "Broken".equals(condition)) {
                    try {
                        Object quantityObj = tableModel.getValueAt(i, 1);
                        if (quantityObj != null) {
                            maintenanceCount += Integer.parseInt(quantityObj.toString());
                        }
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }
        return maintenanceCount;
    }

    private void updateStatsFromTableData() {
        SwingUtilities.invokeLater(() -> {
            int totalTools = getTotalToolsCount();
            int inUseCount = getToolsInUseCount();
            int availableCount = getAvailableToolsCount();
            int maintenanceCount = getMaintenanceToolsCount();

            if (statsPanel != null) {
                statsPanel.removeAll();

                JPanel totalCard = createStatusCard("Total Tools", String.valueOf(totalTools), "🔧");
                JPanel inUseCard = createStatusCard("In Use", String.valueOf(inUseCount), "📝");
                JPanel availableCard = createStatusCard("Available", String.valueOf(availableCount), "✅");
                JPanel maintenanceCard = createStatusCard("Maintenance", String.valueOf(maintenanceCount), "⚠️");

                statsPanel.add(totalCard);
                statsPanel.add(inUseCard);
                statsPanel.add(availableCard);
                statsPanel.add(maintenanceCard);

                statsPanel.revalidate();
                statsPanel.repaint();
            }
        });
    }

    private void borrowTool() {
        int row = toolTable.getSelectedRow();
        if (row >= 0) {
            int modelRow = toolTable.convertRowIndexToModel(row);
            String toolName = (String) tableModel.getValueAt(modelRow, 0);
            String currentStatus = (String) tableModel.getValueAt(modelRow, 4);

            if ("In Use".equals(currentStatus)) {
                showError("This tool is already checked out");
                return;
            }
            String[] workers = getWorkerNames();
            if (workers.length == 0) {
                workers = new String[]{"John Doe", "Jane Smith", "Bob Wilson"};
            }

            JComboBox<String> workerCombo = new JComboBox<>(workers);

            JPanel panel = new JPanel(new GridLayout(2, 1, 5, 5));
            panel.add(new JLabel("Select worker for " + toolName + ":"));
            panel.add(workerCombo);

            int result = JOptionPane.showConfirmDialog(this, panel, "Check Out Tool",
                    JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);

            if (result == JOptionPane.OK_OPTION) {
                String worker = (String) workerCombo.getSelectedItem();

                Connection conn = null;
                PreparedStatement stmt = null;

                try {
                    conn = DatabaseConnection.getConnection();
                    String query = "UPDATE tool SET handovertime = ? WHERE toolname = ?";
                    stmt = conn.prepareStatement(query);
                    stmt.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
                    stmt.setString(2, toolName);

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        tableModel.setValueAt(worker, modelRow, 2);
                        tableModel.setValueAt("In Use", modelRow, 4);

                        if (originalData != null && modelRow < originalData.size()) {
                            originalData.get(modelRow)[2] = worker;
                            originalData.get(modelRow)[4] = "In Use";
                        }

                        toolTable.repaint();
                        updateStatsFromTableData();
                        showSuccess("Tool checked out to " + worker);
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                    showError("Error checking out tool: " + e.getMessage());
                } finally {
                    try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
                    DatabaseConnection.closeConnection(conn);
                }
            }
        } else {
            showError("Please select a tool to check out");
        }
    }

    private String[] getWorkerNames() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        List<String> workers = new ArrayList<>();

        try {
            conn = DatabaseConnection.getConnection();
            String query = "SELECT name FROM worker ORDER BY name";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                workers.add(rs.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
        return workers.toArray(new String[0]);
    }

    private void returnTool() {
        int row = toolTable.getSelectedRow();
        if (row >= 0) {
            int modelRow = toolTable.convertRowIndexToModel(row);
            String toolName = (String) tableModel.getValueAt(modelRow, 0);
            String currentStatus = (String) tableModel.getValueAt(modelRow, 4);

            if ("Available".equals(currentStatus)) {
                showError("This tool is already available");
                return;
            }

            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = DatabaseConnection.getConnection();
                String query = "UPDATE tool SET handovertime = NULL WHERE toolname = ?";
                stmt = conn.prepareStatement(query);
                stmt.setString(1, toolName);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    tableModel.setValueAt("-", modelRow, 2);
                    tableModel.setValueAt("Available", modelRow, 4);

                    if (originalData != null && modelRow < originalData.size()) {
                        originalData.get(modelRow)[2] = "-";
                        originalData.get(modelRow)[4] = "Available";
                    }

                    toolTable.repaint();
                    updateStatsFromTableData();
                    showSuccess(toolName + " checked in successfully");
                }

            } catch (SQLException e) {
                e.printStackTrace();
                showError("Error checking in tool: " + e.getMessage());
            } finally {
                try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
                DatabaseConnection.closeConnection(conn);
            }
        } else {
            showError("Please select a tool to check in");
        }
    }

    private void reportMaintenance() {
        int row = toolTable.getSelectedRow();
        if (row >= 0) {
            int modelRow = toolTable.convertRowIndexToModel(row);
            String toolName = (String) tableModel.getValueAt(modelRow, 0);
            String currentCondition = (String) tableModel.getValueAt(modelRow, 3);

            String issue = JOptionPane.showInputDialog(this,
                    "Describe maintenance issue for " + toolName + ":\n" +
                            "Current condition: " + currentCondition,
                    "Report Maintenance", JOptionPane.QUESTION_MESSAGE);

            if (issue != null && !issue.isEmpty()) {
                Connection conn = null;
                PreparedStatement stmt = null;

                try {
                    conn = DatabaseConnection.getConnection();

                    String newCondition = "Poor";
                    if (currentCondition.equals("Excellent") || currentCondition.equals("New")) {
                        newCondition = "Good";
                    } else if (currentCondition.equals("Good")) {
                        newCondition = "Fair";
                    } else if (currentCondition.equals("Fair")) {
                        newCondition = "Poor";
                    } else if (currentCondition.equals("Poor")) {
                        newCondition = "Broken";
                    }

                    String query = "UPDATE tool SET `condition` = ?, last_maintenance = ? WHERE toolname = ?";
                    stmt = conn.prepareStatement(query);
                    stmt.setString(1, newCondition);
                    stmt.setTimestamp(2, new Timestamp(System.currentTimeMillis()));
                    stmt.setString(3, toolName);

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        tableModel.setValueAt(newCondition, modelRow, 3);
                        tableModel.setValueAt(new java.sql.Date(System.currentTimeMillis()), modelRow, 5);

                        if (originalData != null && modelRow < originalData.size()) {
                            originalData.get(modelRow)[3] = newCondition;
                            originalData.get(modelRow)[5] = new java.sql.Date(System.currentTimeMillis());
                        }

                        toolTable.repaint();
                        updateStatsFromTableData();
                        showSuccess("Maintenance reported for " + toolName + ". Condition updated to: " + newCondition);
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                    showError("Error reporting maintenance: " + e.getMessage());
                } finally {
                    try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
                    DatabaseConnection.closeConnection(conn);
                }
            }
        } else {
            showError("Please select a tool to report maintenance");
        }
    }

    private void addTool() {
        JDialog addDialog = new JDialog(this, "Add New Tool", true);
        addDialog.setSize(400, 350);
        addDialog.setLocationRelativeTo(this);
        addDialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Tool Name:"), gbc);

        gbc.gridx = 1;
        JTextField nameField = new JTextField(20);
        formPanel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Quantity:"), gbc);

        gbc.gridx = 1;
        JTextField quantityField = new JTextField(10);
        quantityField.setText("1");
        formPanel.add(quantityField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Condition:"), gbc);

        gbc.gridx = 1;
        JComboBox<String> conditionCombo = new JComboBox<>(new String[]{"New", "Excellent", "Good", "Fair", "Poor", "Broken"});
        conditionCombo.setSelectedIndex(2);
        formPanel.add(conditionCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Tool Type:"), gbc);

        gbc.gridx = 1;
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{
                "Cutting", "Watering", "Measuring", "Safety", "Digging", "Transport", "Application", "Testing"
        });
        formPanel.add(typeCombo, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        JButton saveButton = new JButton("Save");
        saveButton.setBackground(new Color(46, 125, 50));
        saveButton.setForeground(Color.BLACK);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(new Color(219, 68, 55));
        cancelButton.setForeground(Color.BLACK);

        saveButton.addActionListener(e -> {
            String toolName = nameField.getText().trim();
            String quantity = quantityField.getText().trim();
            String condition = (String) conditionCombo.getSelectedItem();
            String toolType = (String) typeCombo.getSelectedItem();

            if (toolName.isEmpty()) {
                JOptionPane.showMessageDialog(addDialog,
                        "Please enter a tool name",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                int qty = Integer.parseInt(quantity);
                if (qty <= 0) {
                    JOptionPane.showMessageDialog(addDialog,
                            "Quantity must be positive",
                            "Validation Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                if (toolExists(toolName)) {
                    JOptionPane.showMessageDialog(addDialog,
                            "Tool with this name already exists",
                            "Validation Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                Connection conn = null;
                PreparedStatement stmt = null;

                try {
                    conn = DatabaseConnection.getConnection();
                    String query = "INSERT INTO tool (toolname, connu, tool_type, `condition`, last_maintenance) " +
                            "VALUES (?, ?, ?, ?, ?)";
                    stmt = conn.prepareStatement(query);
                    stmt.setString(1, toolName);
                    stmt.setInt(2, qty);
                    stmt.setString(3, toolType);
                    stmt.setString(4, condition);
                    stmt.setDate(5, new java.sql.Date(System.currentTimeMillis()));

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        Object[] newRow = new Object[]{
                                toolName,
                                qty,
                                "-",
                                condition,
                                "Available",
                                new java.sql.Date(System.currentTimeMillis())
                        };
                        tableModel.addRow(newRow);
                        originalData.add(newRow);

                        showSuccess("Tool '" + toolName + "' added successfully!");
                        updateStatsFromTableData();
                        addDialog.dispose();
                    }

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    showError("Error adding tool: " + ex.getMessage());
                } finally {
                    try { if (stmt != null) stmt.close(); } catch (SQLException ex) {}
                    DatabaseConnection.closeConnection(conn);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(addDialog,
                        "Quantity must be a number",
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

    private boolean toolExists(String toolName) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean exists = false;

        try {
            conn = DatabaseConnection.getConnection();
            String query = "SELECT 1 FROM tool WHERE toolname = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, toolName);
            rs = stmt.executeQuery();

            exists = rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
        return exists;
    }

    private void filterTools() {
        String filter = (String) filterCombo.getSelectedItem();
        if (filter == null || filter.equals("All")) {
            tableModel.setRowCount(0);
            for (Object[] row : originalData) {
                tableModel.addRow(row);
            }
        } else {
            tableModel.setRowCount(0);

            for (Object[] row : originalData) {
                Object statusObj = row[4];
                Object conditionObj = row[3];

                boolean shouldShow = false;

                switch (filter) {
                    case "In Use":
                        shouldShow = "In Use".equals(statusObj);
                        break;
                    case "Available":
                        shouldShow = "Available".equals(statusObj);
                        break;
                    case "Needs Maintenance":
                        if (conditionObj != null) {
                            String condition = conditionObj.toString();
                            shouldShow = "Poor".equals(condition) || "Broken".equals(condition);
                        }
                        break;
                }

                if (shouldShow) {
                    tableModel.addRow(row);
                }
            }
        }

        updateStatsFromTableData();
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