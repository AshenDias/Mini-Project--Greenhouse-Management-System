package view;

import util.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class WorkerView extends BaseView {
    private JTable workerTable;
    private DefaultTableModel tableModel;
    private JButton addButton, editButton, deleteButton, backButton;
    private JTextField searchField;
    private DashboardView dashboardView;
    private JComboBox<String> filterCombo;

    public WorkerView(DashboardView dashboardView) {
        super("Worker Management");
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
        loadWorkerData();
    }

    @Override
    protected void setupEventListeners() {
        addButton.addActionListener(e -> addWorker());
        editButton.addActionListener(e -> editWorker());
        deleteButton.addActionListener(e -> deleteWorker());
        backButton.addActionListener(e -> goBackToDashboard());

        searchField.addActionListener(e -> searchWorkers());
        filterCombo.addActionListener(e -> filterWorkers());

        workerTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                boolean hasSelection = workerTable.getSelectedRow() >= 0;
                editButton.setEnabled(hasSelection);
                deleteButton.setEnabled(hasSelection);
            }
        });

        editButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }

    private void createHeader() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JLabel titleLabel = new JLabel("👷 Worker Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        backButton = createStyledButton("← Back to Dashboard", Color.WHITE, new Color(171, 71, 188));
        backButton.setPreferredSize(new Dimension(180, 35));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(backButton, BorderLayout.EAST);
    }

    private void createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Color.lightGray);
        toolbar.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBackground(Color.lightGray);

        searchField = new JTextField(20);
        searchField.setPreferredSize(new Dimension(200, 30));

        filterCombo = new JComboBox<>(new String[]{"All", "Manager", "Supervisor", "Technician", "Researcher", "Maintenance"});
        filterCombo.setPreferredSize(new Dimension(150, 30));

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(Box.createHorizontalStrut(20));
        searchPanel.add(new JLabel("Filter:"));
        searchPanel.add(filterCombo);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setBackground(Color.lightGray);

        addButton = createStyledButton("➕ Add Worker", new Color(171, 71, 188), Color.black);
        editButton = createStyledButton("✏️ Edit Worker", SECONDARY_COLOR, Color.black);
        deleteButton = createStyledButton("🗑️ Delete Worker", new Color(219, 68, 55), Color.black);

        actionPanel.add(addButton);
        actionPanel.add(editButton);
        actionPanel.add(deleteButton);

        toolbar.add(searchPanel, BorderLayout.WEST);
        toolbar.add(actionPanel, BorderLayout.EAST);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(headerPanel, BorderLayout.NORTH);
        topContainer.add(toolbar, BorderLayout.SOUTH);

        mainPanel.add(topContainer, BorderLayout.NORTH);
    }

    private void createTable() {
        String[] columns = {"ID", "Name", "Email", "Username", "Role", "Phone", "Hire Date"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        workerTable = new JTable(tableModel);
        workerTable.setRowHeight(35);
        workerTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        workerTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        workerTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));

        workerTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (column == 4) {
                    String role = (String) value;
                    if (role != null) {
                        switch (role) {
                            case "Manager":
                                c.setForeground(new Color(219, 68, 55));
                                c.setFont(c.getFont().deriveFont(Font.BOLD));
                                break;
                            case "Supervisor":
                                c.setForeground(new Color(244, 180, 0));
                                break;
                            case "Technician":
                                c.setForeground(new Color(66, 133, 244));
                                break;
                            case "Researcher":
                                c.setForeground(new Color(46, 125, 50));
                                break;
                            case "Maintenance":
                                c.setForeground(new Color(158, 158, 158));
                                break;
                        }
                    }
                }

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 248));
                }

                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(workerTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        mainPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void createStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 0));
        statsPanel.setBackground(Color.lightGray);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        int totalWorkers = getTotalWorkersCount();
        int managersCount = getWorkersByRoleCount("Manager");
        int activeToday = getActiveWorkersToday();

        JPanel totalPanel = createStatCard("Total Workers", String.valueOf(totalWorkers), "👷", new Color(171, 71, 188));
        JPanel managerPanel = createStatCard("Managers", String.valueOf(managersCount), "👑", new Color(219, 68, 55));
        JPanel activePanel = createStatCard("Active Today", String.valueOf(activeToday), "✅", new Color(46, 125, 50));

        statsPanel.add(totalPanel);
        statsPanel.add(managerPanel);
        statsPanel.add(activePanel);

        mainPanel.add(statsPanel, BorderLayout.SOUTH);
    }

    private JPanel createStatCard(String title, String value, String icon, Color color) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder(title),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        JLabel valueLabel = new JLabel(icon + " " + value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 16));
        valueLabel.setForeground(color);
        card.add(valueLabel, BorderLayout.CENTER);
        return card;
    }

    private void loadWorkerData() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            tableModel.setRowCount(0);

            conn = DatabaseConnection.getConnection();
            String query = "SELECT id, name, email, username, role, phone, hire_date " +
                    "FROM worker ORDER BY role, name";

            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("phone"),
                        rs.getDate("hire_date")
                });
            }

            showSuccess("Loaded " + tableModel.getRowCount() + " workers from database");

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error loading worker data: " + e.getMessage());

            // Load sample data if database fails
            Object[][] data = {
                    {1, "John Doe", "john.doe@agrovision.com", "johndoe123", "Manager", "555-0101", java.sql.Date.valueOf("2023-01-15")},
                    {2, "Jane Smith", "jane.smith@agrovision.com", "janesmith456", "Supervisor", "555-0102", java.sql.Date.valueOf("2023-02-20")},
                    {3, "Bob Wilson", "bob.wilson@agrovision.com", "bobwilson789", "Technician", "555-0103", java.sql.Date.valueOf("2023-03-10")},
                    {4, "Alice Brown", "alice.brown@agrovision.com", "alicebrown101", "Researcher", "555-0104", java.sql.Date.valueOf("2023-04-05")}
            };

            for (Object[] row : data) {
                tableModel.addRow(row);
            }
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
    }

    private int getTotalWorkersCount() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int count = 0;

        try {
            conn = DatabaseConnection.getConnection();
            String query = "SELECT COUNT(*) as count FROM worker";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
        return count;
    }

    private int getWorkersByRoleCount(String role) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int count = 0;

        try {
            conn = DatabaseConnection.getConnection();
            String query = "SELECT COUNT(*) as count FROM worker WHERE role = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, role);
            rs = stmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt("count");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
        return count;
    }

    private int getActiveWorkersToday() {
        return (int) (Math.random() * 5) + 3;
    }

    private void addWorker() {
        JDialog addDialog = new JDialog(this, "Add New Worker", true);
        addDialog.setSize(450, 500);
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
        formPanel.add(new JLabel("Full Name:*"), gbc);

        gbc.gridx = 1;
        JTextField nameField = new JTextField(20);
        formPanel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Email:*"), gbc);

        gbc.gridx = 1;
        JTextField emailField = new JTextField(20);
        formPanel.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Username:*"), gbc);

        gbc.gridx = 1;
        JTextField usernameField = new JTextField(20);
        formPanel.add(usernameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Password:*"), gbc);

        gbc.gridx = 1;
        JPasswordField passwordField = new JPasswordField(20);
        formPanel.add(passwordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Confirm Password:*"), gbc);

        gbc.gridx = 1;
        JPasswordField confirmPasswordField = new JPasswordField(20);
        formPanel.add(confirmPasswordField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 5;
        formPanel.add(new JLabel("Role:*"), gbc);

        gbc.gridx = 1;
        JComboBox<String> roleCombo = new JComboBox<>(new String[]{
                "Worker", "Technician", "Supervisor", "Manager", "Researcher", "Maintenance"
        });
        formPanel.add(roleCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 6;
        formPanel.add(new JLabel("Phone:"), gbc);

        gbc.gridx = 1;
        JTextField phoneField = new JTextField(20);
        formPanel.add(phoneField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 7;
        formPanel.add(new JLabel("Hire Date (YYYY-MM-DD):"), gbc);

        gbc.gridx = 1;
        JTextField hireDateField = new JTextField(15);
        hireDateField.setText(java.time.LocalDate.now().toString());
        formPanel.add(hireDateField, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        JButton saveButton = new JButton("Save Worker");
        saveButton.setBackground(new Color(46, 125, 50));
        saveButton.setForeground(Color.BLACK);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(new Color(219, 68, 55));
        cancelButton.setForeground(Color.BLACK);

        saveButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();
            String username = usernameField.getText().trim();
            String password = new String(passwordField.getPassword());
            String confirmPassword = new String(confirmPasswordField.getPassword());
            String role = (String) roleCombo.getSelectedItem();
            String phone = phoneField.getText().trim();
            String hireDate = hireDateField.getText().trim();

            // Validation
            if (name.isEmpty() || email.isEmpty() || username.isEmpty() ||
                    password.isEmpty() || confirmPassword.isEmpty()) {
                JOptionPane.showMessageDialog(addDialog,
                        "Please fill in all required fields",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (!password.equals(confirmPassword)) {
                JOptionPane.showMessageDialog(addDialog,
                        "Passwords do not match",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            if (password.length() < 6) {
                JOptionPane.showMessageDialog(addDialog,
                        "Password must be at least 6 characters",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Check if username already exists
            if (usernameExists(username)) {
                JOptionPane.showMessageDialog(addDialog,
                        "Username already exists",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Check if email already exists
            if (emailExists(email)) {
                JOptionPane.showMessageDialog(addDialog,
                        "Email already exists",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Save to database
            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = DatabaseConnection.getConnection();
                String query = "INSERT INTO worker (name, email, username, password, role, phone, hire_date) " +
                        "VALUES (?, ?, ?, ?, ?, ?, ?)";
                stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                stmt.setString(1, name);
                stmt.setString(2, email);
                stmt.setString(3, username);
                stmt.setString(4, password); // Note: In production, hash the password!
                stmt.setString(5, role);
                stmt.setString(6, phone.isEmpty() ? null : phone);
                stmt.setDate(7, hireDate.isEmpty() ? null : java.sql.Date.valueOf(hireDate));

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    ResultSet generatedKeys = stmt.getGeneratedKeys();
                    int newId = 0;
                    if (generatedKeys.next()) {
                        newId = generatedKeys.getInt(1);
                    }

                    tableModel.addRow(new Object[]{
                            newId,
                            name,
                            email,
                            username,
                            role,
                            phone.isEmpty() ? "-" : phone,
                            hireDate.isEmpty() ? "-" : java.sql.Date.valueOf(hireDate)
                    });

                    showSuccess("Worker '" + name + "' added successfully!");
                    addDialog.dispose();
                    loadWorkerData();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(addDialog,
                        "Error: " + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            } finally {
                try { if (stmt != null) stmt.close(); } catch (SQLException ex) {}
                DatabaseConnection.closeConnection(conn);
            }
        });

        cancelButton.addActionListener(e -> addDialog.dispose());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        addDialog.add(formPanel, BorderLayout.CENTER);
        addDialog.add(buttonPanel, BorderLayout.SOUTH);
        addDialog.setVisible(true);
    }

    private boolean usernameExists(String username) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean exists = false;

        try {
            conn = DatabaseConnection.getConnection();
            String query = "SELECT 1 FROM worker WHERE username = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, username);
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

    private boolean emailExists(String email) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean exists = false;

        try {
            conn = DatabaseConnection.getConnection();
            String query = "SELECT 1 FROM worker WHERE email = ?";
            stmt = conn.prepareStatement(query);
            stmt.setString(1, email);
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

    private void editWorker() {
        int row = workerTable.getSelectedRow();
        if (row >= 0) {
            int modelRow = workerTable.convertRowIndexToModel(row);
            int workerId = (int) tableModel.getValueAt(modelRow, 0);
            String currentName = (String) tableModel.getValueAt(modelRow, 1);

            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = DatabaseConnection.getConnection();
                String query = "SELECT * FROM worker WHERE id = ?";
                stmt = conn.prepareStatement(query);
                stmt.setInt(1, workerId);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    JDialog editDialog = new JDialog(this, "Edit Worker: " + currentName, true);
                    editDialog.setSize(450, 500);
                    editDialog.setLocationRelativeTo(this);
                    editDialog.setLayout(new BorderLayout());

                    JPanel formPanel = new JPanel(new GridBagLayout());
                    formPanel.setBackground(Color.WHITE);
                    formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

                    GridBagConstraints gbc = new GridBagConstraints();
                    gbc.fill = GridBagConstraints.HORIZONTAL;
                    gbc.insets = new Insets(5, 5, 5, 5);

                    gbc.gridx = 0;
                    gbc.gridy = 0;
                    formPanel.add(new JLabel("Full Name:*"), gbc);

                    gbc.gridx = 1;
                    JTextField nameField = new JTextField(20);
                    nameField.setText(rs.getString("name"));
                    formPanel.add(nameField, gbc);

                    gbc.gridx = 0;
                    gbc.gridy = 1;
                    formPanel.add(new JLabel("Email:*"), gbc);

                    gbc.gridx = 1;
                    JTextField emailField = new JTextField(20);
                    emailField.setText(rs.getString("email"));
                    formPanel.add(emailField, gbc);

                    gbc.gridx = 0;
                    gbc.gridy = 2;
                    formPanel.add(new JLabel("Username:*"), gbc);

                    gbc.gridx = 1;
                    JTextField usernameField = new JTextField(20);
                    usernameField.setText(rs.getString("username"));
                    usernameField.setEnabled(false); // Username shouldn't be changed
                    formPanel.add(usernameField, gbc);

                    gbc.gridx = 0;
                    gbc.gridy = 3;
                    formPanel.add(new JLabel("Role:*"), gbc);

                    gbc.gridx = 1;
                    JComboBox<String> roleCombo = new JComboBox<>(new String[]{
                            "Worker", "Technician", "Supervisor", "Manager", "Researcher", "Maintenance"
                    });
                    roleCombo.setSelectedItem(rs.getString("role"));
                    formPanel.add(roleCombo, gbc);

                    gbc.gridx = 0;
                    gbc.gridy = 4;
                    formPanel.add(new JLabel("Phone:"), gbc);

                    gbc.gridx = 1;
                    JTextField phoneField = new JTextField(20);
                    phoneField.setText(rs.getString("phone") != null ? rs.getString("phone") : "");
                    formPanel.add(phoneField, gbc);

                    gbc.gridx = 0;
                    gbc.gridy = 5;
                    formPanel.add(new JLabel("Hire Date (YYYY-MM-DD):"), gbc);

                    gbc.gridx = 1;
                    JTextField hireDateField = new JTextField(15);
                    hireDateField.setText(rs.getDate("hire_date") != null ? rs.getDate("hire_date").toString() : "");
                    formPanel.add(hireDateField, gbc);

                    JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
                    buttonPanel.setBackground(Color.WHITE);

                    JButton saveButton = new JButton("Save Changes");
                    saveButton.setBackground(new Color(46, 125, 50));
                    saveButton.setForeground(Color.BLACK);

                    JButton cancelButton = new JButton("Cancel");
                    cancelButton.setBackground(new Color(219, 68, 55));
                    cancelButton.setForeground(Color.BLACK);

                    saveButton.addActionListener(evt -> {
                        String name = nameField.getText().trim();
                        String email = emailField.getText().trim();
                        String role = (String) roleCombo.getSelectedItem();
                        String phone = phoneField.getText().trim();
                        String hireDate = hireDateField.getText().trim();

                        if (name.isEmpty() || email.isEmpty()) {
                            JOptionPane.showMessageDialog(editDialog,
                                    "Please fill in all required fields",
                                    "Validation Error",
                                    JOptionPane.ERROR_MESSAGE);
                            return;
                        }

                        // Update in database
                        Connection conn2 = null;
                        PreparedStatement stmt2 = null;

                        try {
                            conn2 = DatabaseConnection.getConnection();
                            String query2 = "UPDATE worker SET name = ?, email = ?, role = ?, phone = ?, hire_date = ? WHERE id = ?";
                            stmt2 = conn2.prepareStatement(query2);
                            stmt2.setString(1, name);
                            stmt2.setString(2, email);
                            stmt2.setString(3, role);
                            stmt2.setString(4, phone.isEmpty() ? null : phone);
                            stmt2.setDate(5, hireDate.isEmpty() ? null : java.sql.Date.valueOf(hireDate));
                            stmt2.setInt(6, workerId);

                            int rowsAffected = stmt2.executeUpdate();
                            if (rowsAffected > 0) {
                                // Update table
                                tableModel.setValueAt(name, modelRow, 1);
                                tableModel.setValueAt(email, modelRow, 2);
                                tableModel.setValueAt(role, modelRow, 4);
                                tableModel.setValueAt(phone.isEmpty() ? "-" : phone, modelRow, 5);
                                tableModel.setValueAt(hireDate.isEmpty() ? "-" : java.sql.Date.valueOf(hireDate), modelRow, 6);

                                showSuccess("Worker '" + name + "' updated successfully!");
                                editDialog.dispose();
                            }

                        } catch (Exception ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(editDialog,
                                    "Error: " + ex.getMessage(),
                                    "Database Error",
                                    JOptionPane.ERROR_MESSAGE);
                        } finally {
                            try { if (stmt2 != null) stmt2.close(); } catch (SQLException ex) {}
                            DatabaseConnection.closeConnection(conn2);
                        }
                    });

                    cancelButton.addActionListener(evt -> editDialog.dispose());

                    buttonPanel.add(cancelButton);
                    buttonPanel.add(saveButton);

                    editDialog.add(formPanel, BorderLayout.CENTER);
                    editDialog.add(buttonPanel, BorderLayout.SOUTH);
                    editDialog.setVisible(true);
                }

            } catch (SQLException e) {
                e.printStackTrace();
                showError("Error loading worker data: " + e.getMessage());
            } finally {
                try { if (rs != null) rs.close(); } catch (SQLException e) {}
                try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
                DatabaseConnection.closeConnection(conn);
            }
        } else {
            showError("Please select a worker to edit");
        }
    }

    private void deleteWorker() {
        int row = workerTable.getSelectedRow();
        if (row >= 0) {
            int modelRow = workerTable.convertRowIndexToModel(row);
            int workerId = (int) tableModel.getValueAt(modelRow, 0);
            String workerName = (String) tableModel.getValueAt(modelRow, 1);

            int response = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete worker '" + workerName + "'?\n" +
                            "This action cannot be undone.",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (response == JOptionPane.YES_OPTION) {
                if (workerHasAssignments(workerId)) {
                    showError("Cannot delete worker. They have assigned tasks or tools.");
                    return;
                }

                Connection conn = null;
                PreparedStatement stmt = null;

                try {
                    conn = DatabaseConnection.getConnection();
                    String query = "DELETE FROM worker WHERE id = ?";
                    stmt = conn.prepareStatement(query);
                    stmt.setInt(1, workerId);

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        tableModel.removeRow(modelRow);
                        showSuccess("Worker '" + workerName + "' deleted successfully");
                        loadWorkerData();
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                    showError("Error deleting worker: " + e.getMessage());
                } finally {
                    try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
                    DatabaseConnection.closeConnection(conn);
                }
            }
        } else {
            showError("Please select a worker to delete");
        }
    }

    private boolean workerHasAssignments(int workerId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean hasAssignments = false;

        try {
            conn = DatabaseConnection.getConnection();
            String query = "SELECT 1 FROM tool WHERE handovertime IS NOT NULL " +
                    "LIMIT 1"; // Simplified for now
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            hasAssignments = rs.next();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
        return hasAssignments;
    }

    private void searchWorkers() {
        String searchText = searchField.getText().trim().toLowerCase();

        if (searchText.isEmpty()) {
            loadWorkerData();
            return;
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            tableModel.setRowCount(0);

            conn = DatabaseConnection.getConnection();
            String query = "SELECT id, name, email, username, role, phone, hire_date " +
                    "FROM worker WHERE LOWER(name) LIKE ? OR LOWER(email) LIKE ? " +
                    "OR LOWER(username) LIKE ? OR LOWER(role) LIKE ? " +
                    "ORDER BY name";

            stmt = conn.prepareStatement(query);
            String likePattern = "%" + searchText + "%";
            stmt.setString(1, likePattern);
            stmt.setString(2, likePattern);
            stmt.setString(3, likePattern);
            stmt.setString(4, likePattern);

            rs = stmt.executeQuery();

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("phone"),
                        rs.getDate("hire_date")
                });
            }

            showSuccess("Found " + tableModel.getRowCount() + " workers matching '" + searchText + "'");

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error searching workers: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
    }

    private void filterWorkers() {
        String filter = (String) filterCombo.getSelectedItem();
        if (filter == null || filter.equals("All")) {
            loadWorkerData();
            return;
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            tableModel.setRowCount(0);

            conn = DatabaseConnection.getConnection();
            String query = "SELECT id, name, email, username, role, phone, hire_date " +
                    "FROM worker WHERE role = ? ORDER BY name";

            stmt = conn.prepareStatement(query);
            stmt.setString(1, filter);
            rs = stmt.executeQuery();

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("username"),
                        rs.getString("role"),
                        rs.getString("phone"),
                        rs.getDate("hire_date")
                });
            }

            showSuccess("Filtered by role: " + filter + " (" + tableModel.getRowCount() + " workers)");

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error filtering workers: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
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