package view;

import controller.Plantcontroller;
import model.PlantModel;
import util.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableRowSorter;
import java.awt.*;
import javax.swing.RowFilter;
import java.sql.*;
import java.util.List;

public class Plantview extends BaseView {
    private Plantcontroller plantController;
    private JTable plantTable;
    private DefaultTableModel tableModel;
    private TableRowSorter<DefaultTableModel> sorter;
    private JTextField searchField;
    private JComboBox<String> filterCombo;
    private JButton addButton, editButton, deleteButton, viewButton, refreshButton, backButton;
    private JButton scheduleButton;
    private DashboardView dashboardView;

    public Plantview(DashboardView dashboardView) {
        super("Plant Management");
        plantController = new Plantcontroller();
        this.dashboardView = dashboardView;
    }

    @Override
    protected void initializeUI() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);

        createHeader();
        createToolbar();
        createTable();
        createSidePanel();

        add(mainPanel);
        loadPlantData();
        setupEventListeners();
    }

    private void createHeader() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JLabel titleLabel = new JLabel("🌱 Plant Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        backButton = createStyledButton("← Back to Dashboard", Color.WHITE, PRIMARY_COLOR);
        backButton.setPreferredSize(new Dimension(180, 35));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(backButton, BorderLayout.EAST);
    }

    @Override
    protected void setupEventListeners() {
        addButton.addActionListener(e -> addPlant());
        editButton.addActionListener(e -> editPlant());
        deleteButton.addActionListener(e -> deletePlant());
        viewButton.addActionListener(e -> viewPlant());
        refreshButton.addActionListener(e -> loadPlantData());
        backButton.addActionListener(e -> goBackToDashboard());
        scheduleButton.addActionListener(e -> scheduleTask());

        // Search field listener
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                filterTable();
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                filterTable();
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
                filterTable();
            }
        });

        // Filter combo listener
        filterCombo.addActionListener(e -> applyFilter());

        plantTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateSelectionInfo();
            }
        });

        plantTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    viewPlant();
                }
            }
        });
    }

    private void createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Color.lightGray);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, new Color(199, 199, 199)),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        leftPanel.setBackground(Color.lightGray);

        JLabel searchLabel = new JLabel("Search:");
        searchField = new JTextField(20);
        searchField.setPreferredSize(new Dimension(200, 30));

        JLabel filterLabel = new JLabel("Filter:");
        filterCombo = new JComboBox<>(new String[]{"All Plants", "Vegetables", "Flowers", "Herbs", "Fruits", "By Greenhouse"});
        filterCombo.setPreferredSize(new Dimension(150, 30));

        leftPanel.add(searchLabel);
        leftPanel.add(searchField);
        leftPanel.add(Box.createHorizontalStrut(20));
        leftPanel.add(filterLabel);
        leftPanel.add(filterCombo);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setBackground(Color.lightGray);

        refreshButton = createStyledButton("🔄 Refresh", SECONDARY_COLOR, Color.black);
        refreshButton.setPreferredSize(new Dimension(100, 30));

        addButton = createStyledButton("➕ Add Plant", PRIMARY_COLOR, Color.black);
        addButton.setPreferredSize(new Dimension(120, 30));

        rightPanel.add(refreshButton);
        rightPanel.add(addButton);

        toolbar.add(leftPanel, BorderLayout.WEST);
        toolbar.add(rightPanel, BorderLayout.EAST);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(headerPanel, BorderLayout.NORTH);
        topContainer.add(toolbar, BorderLayout.SOUTH);

        mainPanel.add(topContainer, BorderLayout.NORTH);
    }

    private void createTable() {
        String[] columns = {"ID", "Name", "Type", "Water (L)", "Nutrients (g)", "Greenhouse", "Status"};

        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Integer.class : String.class;
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        plantTable = new JTable(tableModel);
        sorter = new TableRowSorter<>(tableModel);
        plantTable.setRowSorter(sorter);

        plantTable.setRowHeight(40);
        plantTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        plantTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));
        plantTable.getTableHeader().setBackground(new Color(250, 250, 250));
        plantTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        plantTable.setShowGrid(true);
        plantTable.setGridColor(new Color(240, 240, 240));
        plantTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (column == 6) {
                    String status = (String) value;
                    if (status != null) {
                        switch (status) {
                            case "Healthy":
                                c.setForeground(new Color(46, 125, 50));
                                break;
                            case "Diseased":
                                c.setForeground(new Color(219, 68, 55));
                                break;
                            case "Ready":
                                c.setForeground(new Color(66, 133, 244));
                                break;
                            default:
                                c.setForeground(Color.GRAY);
                        }
                    }
                }

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 248));
                }

                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(plantTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());

        mainPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void createSidePanel() {
        JPanel sidePanel = new JPanel(new BorderLayout());
        sidePanel.setPreferredSize(new Dimension(300, 0));
        sidePanel.setBackground(new Color(250, 250, 250));
        sidePanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, new Color(230, 230, 230)));

        JPanel infoPanel = createCardPanel();
        infoPanel.setLayout(new BorderLayout());

        JLabel infoTitle = new JLabel("Selected Plant");
        infoTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        infoTitle.setForeground(PRIMARY_COLOR);
        infoTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JPanel detailsPanel = new JPanel();
        detailsPanel.setLayout(new BoxLayout(detailsPanel, BoxLayout.Y_AXIS));
        detailsPanel.setBackground(CARD_COLOR);
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        addDetail(detailsPanel, "ID:", "-");
        addDetail(detailsPanel, "Name:", "-");
        addDetail(detailsPanel, "Type:", "-");
        addDetail(detailsPanel, "Water:", "-");
        addDetail(detailsPanel, "Nutrients:", "-");
        addDetail(detailsPanel, "Greenhouse:", "-");
        addDetail(detailsPanel, "Status:", "-");

        infoPanel.add(infoTitle, BorderLayout.NORTH);
        infoPanel.add(new JScrollPane(detailsPanel), BorderLayout.CENTER);

        JPanel actionPanel = new JPanel(new GridLayout(4, 1, 0, 10));
        actionPanel.setBackground(new Color(250, 250, 250));
        actionPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        viewButton = createStyledButton("👁️ View Details", new Color(66, 133, 244), Color.black);
        editButton = createStyledButton("✏️ Edit Plant", PRIMARY_COLOR, Color.black);
        deleteButton = createStyledButton("🗑️ Delete Plant", new Color(219, 68, 55), Color.black);
        scheduleButton = createStyledButton("📅 Schedule Task", new Color(244, 180, 0), Color.black);

        actionPanel.add(viewButton);
        actionPanel.add(editButton);
        actionPanel.add(deleteButton);
        actionPanel.add(scheduleButton);

        sidePanel.add(infoPanel, BorderLayout.NORTH);
        sidePanel.add(actionPanel, BorderLayout.SOUTH);

        mainPanel.add(sidePanel, BorderLayout.EAST);
    }

    private void addDetail(JPanel panel, String label, String value) {
        JPanel detailPanel = new JPanel(new BorderLayout());
        detailPanel.setBackground(CARD_COLOR);
        detailPanel.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

        JLabel labelComp = new JLabel(label);
        labelComp.setFont(new Font("Segoe UI", Font.BOLD, 12));
        labelComp.setForeground(Color.GRAY);

        JLabel valueComp = new JLabel(value);
        valueComp.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        valueComp.setForeground(Color.DARK_GRAY);

        detailPanel.add(labelComp, BorderLayout.WEST);
        detailPanel.add(valueComp, BorderLayout.EAST);
        panel.add(detailPanel);
    }

    private void loadPlantData() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            tableModel.setRowCount(0);

            conn = DatabaseConnection.getConnection();
            String query = "SELECT p.Plant_ID, p.name, p.plant_type, p.wateramount, p.nut_amount, " +
                    "g.name as greenhouse_name, " +
                    "CASE WHEN EXISTS (SELECT 1 FROM Disease d WHERE d.affected_plant_id = p.Plant_ID) " +
                    "THEN 'Diseased' ELSE 'Healthy' END as status " +
                    "FROM Plant p " +
                    "LEFT JOIN Greenhouse g ON p.greenhouse_id = g.greenhouse_id " +
                    "WHERE p.active = 1 " +
                    "ORDER BY p.Plant_ID";

            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            int rowCount = 0;
            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("Plant_ID"),
                        rs.getString("name"),
                        rs.getString("plant_type"),
                        String.format("%.1f", rs.getDouble("wateramount")),
                        String.format("%.1f", rs.getDouble("nut_amount")),
                        rs.getString("greenhouse_name"),
                        rs.getString("status")
                });
                rowCount++;
            }

            if (rowCount > 0) {
                showSuccess("Loaded " + rowCount + " plants from database");
            } else {
                showError("No plants found in database. Click 'Add Plant' to create one.");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error loading plant data: " + e.getMessage());

            // Load sample data if database fails
            Object[][] sampleData = {
                    {101, "Tomato", "Vegetable", "2.5", "1.2", "Main Greenhouse", "Healthy"},
                    {102, "Rose Bush", "Flower", "1.8", "0.8", "Main Greenhouse", "Diseased"},
                    {103, "Basil Herb", "Herb", "1.2", "0.5", "Research Greenhouse", "Healthy"},
                    {104, "Orchid", "Flower", "0.8", "0.3", "Tropical Greenhouse", "Healthy"},
                    {105, "Lemon Tree", "Fruit", "3.5", "2.0", "Seedling Greenhouse", "Healthy"},
                    {106, "Lettuce", "Vegetable", "1.5", "0.6", "Hydroponic Greenhouse", "Healthy"}
            };

            for (Object[] row : sampleData) {
                tableModel.addRow(row);
            }
            showSuccess("Loaded sample data (database connection failed)");

        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }

        updateSelectionInfo();
    }

    private void updateSelectionInfo() {
        int selectedRow = plantTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = plantTable.convertRowIndexToModel(selectedRow);

            JPanel detailsPanel = (JPanel) ((JScrollPane) ((JPanel) ((JPanel) mainPanel.getComponent(2)).getComponent(0)).getComponent(1)).getViewport().getView();
            detailsPanel.removeAll();

            addDetail(detailsPanel, "ID:", tableModel.getValueAt(modelRow, 0).toString());
            addDetail(detailsPanel, "Name:", tableModel.getValueAt(modelRow, 1).toString());
            addDetail(detailsPanel, "Type:", tableModel.getValueAt(modelRow, 2).toString());
            addDetail(detailsPanel, "Water:", tableModel.getValueAt(modelRow, 3).toString() + " L/day");
            addDetail(detailsPanel, "Nutrients:", tableModel.getValueAt(modelRow, 4).toString() + " g/day");
            addDetail(detailsPanel, "Greenhouse:", tableModel.getValueAt(modelRow, 5) != null ? tableModel.getValueAt(modelRow, 5).toString() : "Not Assigned");
            addDetail(detailsPanel, "Status:", tableModel.getValueAt(modelRow, 6).toString());

            detailsPanel.revalidate();
            detailsPanel.repaint();

            viewButton.setEnabled(true);
            editButton.setEnabled(true);
            deleteButton.setEnabled(true);
            scheduleButton.setEnabled(true);
        } else {
            viewButton.setEnabled(false);
            editButton.setEnabled(false);
            deleteButton.setEnabled(false);
            scheduleButton.setEnabled(false);
        }
    }

    private void filterTable() {
        String text = searchField.getText();
        if (text.trim().length() == 0) {
            sorter.setRowFilter(null);
        } else {
            sorter.setRowFilter(RowFilter.regexFilter("(?i)" + text, 1, 2, 5));
        }
    }

    private void applyFilter() {
        String selectedFilter = (String) filterCombo.getSelectedItem();
        if (selectedFilter == null) {
            return;
        }

        if (selectedFilter.equals("All Plants")) {
            sorter.setRowFilter(null);
            return;
        }
        RowFilter<DefaultTableModel, Integer> filter = new RowFilter<DefaultTableModel, Integer>() {
            @Override
            public boolean include(Entry<? extends DefaultTableModel, ? extends Integer> entry) {
                String plantType = (String) entry.getValue(2);

                if (plantType == null) {
                    return false;
                }

                switch (selectedFilter) {
                    case "Vegetables":
                        return plantType.equalsIgnoreCase("Vegetable");
                    case "Flowers":
                        return plantType.equalsIgnoreCase("Flower");
                    case "Herbs":
                        return plantType.equalsIgnoreCase("Herb");
                    case "Fruits":
                        return plantType.equalsIgnoreCase("Fruit");
                    case "By Greenhouse":
                        String greenhouse = (String) entry.getValue(5);
                        return greenhouse != null && !greenhouse.isEmpty() && !greenhouse.equals("null");
                    default:
                        return true;
                }
            }
        };

        sorter.setRowFilter(filter);
    }

    private void addPlant() {
        PlantFormView form = new PlantFormView(null, this);
        form.setVisible(true);
    }

    private void editPlant() {
        int selectedRow = plantTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = plantTable.convertRowIndexToModel(selectedRow);
            int plantId = (int) tableModel.getValueAt(modelRow, 0);

            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = DatabaseConnection.getConnection();
                String query = "SELECT * FROM Plant WHERE Plant_ID = ?";
                stmt = conn.prepareStatement(query);
                stmt.setInt(1, plantId);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    PlantModel plant = new PlantModel();
                    plant.setPlantId(rs.getInt("Plant_ID"));
                    plant.setName(rs.getString("name"));
                    plant.setWaterAmount(rs.getDouble("wateramount"));
                    plant.setNutrientAmount(rs.getDouble("nut_amount"));
                    plant.setPlantType(rs.getString("plant_type"));
                    plant.setGreenhouseId(rs.getInt("greenhouse_id"));

                    PlantFormView form = new PlantFormView(plant, this);
                    form.setVisible(true);
                } else {
                    showError("Plant not found in database");
                }

            } catch (SQLException e) {
                e.printStackTrace();
                showError("Error loading plant for editing: " + e.getMessage());
            } finally {
                try { if (rs != null) rs.close(); } catch (SQLException e) {}
                try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
                DatabaseConnection.closeConnection(conn);
            }
        } else {
            showError("Please select a plant to edit");
        }
    }

    private void deletePlant() {
        int selectedRow = plantTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = plantTable.convertRowIndexToModel(selectedRow);
            int plantId = (int) tableModel.getValueAt(modelRow, 0);
            String plantName = (String) tableModel.getValueAt(modelRow, 1);

            int confirm = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to permanently delete plant '" + plantName + "' (ID: " + plantId + ")?\n" +
                            "⚠️ This action will:\n" +
                            "• Remove the plant from the database\n" +
                            "• Delete all associated diseases\n" +
                            "• Delete all associated tasks\n" +
                            "• Delete all associated harvest records\n" +
                            "\nThis action cannot be undone!",
                    "⚠️ Confirm Permanent Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (confirm == JOptionPane.YES_OPTION) {
                Connection conn = null;
                PreparedStatement stmt = null;

                try {
                    conn = DatabaseConnection.getConnection();
                    conn.setAutoCommit(false);

                    try {
                        boolean hasDependencies = checkPlantDependencies(conn, plantId);

                        if (hasDependencies) {
                            int confirmDeps = JOptionPane.showConfirmDialog(this,
                                    "This plant has associated records (diseases/tasks/harvest).\n" +
                                            "Do you want to delete all associated records as well?",
                                    "Delete Associated Records",
                                    JOptionPane.YES_NO_OPTION,
                                    JOptionPane.QUESTION_MESSAGE);

                            if (confirmDeps == JOptionPane.NO_OPTION) {
                                JOptionPane.showMessageDialog(this,
                                        "Cannot delete plant. It has associated records.\n" +
                                                "Please delete associated records first.",
                                        "Delete Failed",
                                        JOptionPane.ERROR_MESSAGE);
                                conn.rollback();
                                return;
                            }
                        }

                        // Delete associated diseases first (due to foreign key)
                        String deleteDiseasesQuery = "DELETE FROM Disease WHERE affected_plant_id = ?";
                        stmt = conn.prepareStatement(deleteDiseasesQuery);
                        stmt.setInt(1, plantId);
                        stmt.executeUpdate();
                        stmt.close();

                        // Delete associated tasks
                        String deleteTasksQuery = "DELETE FROM Task WHERE plant_id = ?";
                        stmt = conn.prepareStatement(deleteTasksQuery);
                        stmt.setInt(1, plantId);
                        stmt.executeUpdate();
                        stmt.close();

                        // Delete associated harvest records
                        String deleteHarvestQuery = "DELETE FROM Harvest WHERE plant_id = ?";
                        stmt = conn.prepareStatement(deleteHarvestQuery);
                        stmt.setInt(1, plantId);
                        stmt.executeUpdate();
                        stmt.close();

                        // Finally delete the plant
                        String deletePlantQuery = "DELETE FROM Plant WHERE Plant_ID = ?";
                        stmt = conn.prepareStatement(deletePlantQuery);
                        stmt.setInt(1, plantId);

                        int rowsAffected = stmt.executeUpdate();

                        if (rowsAffected > 0) {
                            conn.commit();
                            tableModel.removeRow(modelRow);
                            showSuccess("Plant '" + plantName + "' and all associated records deleted successfully!");
                            updateSelectionInfo();
                        } else {
                            conn.rollback();
                            showError("Failed to delete plant. It may not exist.");
                        }

                    } catch (SQLException e) {
                        conn.rollback();
                        throw e;
                    }

                } catch (SQLException e) {
                    e.printStackTrace();
                    if (e.getMessage().contains("foreign key constraint")) {
                        showError("Cannot delete plant. It has associated records.\n" +
                                "Please delete associated diseases, tasks, and harvest records first.");
                    } else {
                        showError("Error deleting plant: " + e.getMessage());
                    }
                } finally {
                    try {
                        if (stmt != null) stmt.close();
                        if (conn != null) {
                            conn.setAutoCommit(true);
                            DatabaseConnection.closeConnection(conn);
                        }
                    } catch (SQLException e) {}
                }
            }
        } else {
            showError("Please select a plant to delete");
        }
    }

    private boolean checkPlantDependencies(Connection conn, int plantId) throws SQLException {
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            String query = "SELECT COUNT(*) as count FROM Disease WHERE affected_plant_id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, plantId);
            rs = stmt.executeQuery();

            if (rs.next() && rs.getInt("count") > 0) {
                return true;
            }

            rs.close();
            stmt.close();

            // Check for tasks
            query = "SELECT COUNT(*) as count FROM Task WHERE plant_id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, plantId);
            rs = stmt.executeQuery();

            if (rs.next() && rs.getInt("count") > 0) {
                return true;
            }

            rs.close();
            stmt.close();

            // Check for harvest records
            query = "SELECT COUNT(*) as count FROM Harvest WHERE plant_id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, plantId);
            rs = stmt.executeQuery();

            if (rs.next() && rs.getInt("count") > 0) {
                return true;
            }

        } finally {
            if (rs != null) rs.close();
            if (stmt != null) stmt.close();
        }

        return false;
    }

    private void viewPlant() {
        int selectedRow = plantTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = plantTable.convertRowIndexToModel(selectedRow);
            int plantId = (int) tableModel.getValueAt(modelRow, 0);

            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = DatabaseConnection.getConnection();
                String query = "SELECT p.*, g.name as greenhouse_name, " +
                        "(SELECT COUNT(*) FROM Disease d WHERE d.affected_plant_id = p.Plant_ID) as disease_count, " +
                        "(SELECT COUNT(*) FROM Task t WHERE t.plant_id = p.Plant_ID) as task_count, " +
                        "(SELECT COUNT(*) FROM Harvest h WHERE h.plant_id = p.Plant_ID) as harvest_count " +
                        "FROM Plant p " +
                        "LEFT JOIN Greenhouse g ON p.greenhouse_id = g.greenhouse_id " +
                        "WHERE p.Plant_ID = ?";

                stmt = conn.prepareStatement(query);
                stmt.setInt(1, plantId);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    String greenhouseName = rs.getString("greenhouse_name");
                    if (greenhouseName == null) greenhouseName = "Not Assigned";

                    String details = String.format(
                            "=================================\n" +
                                    "PLANT DETAILS\n" +
                                    "=================================\n" +
                                    "ID:           %d\n" +
                                    "Name:         %s\n" +
                                    "Type:         %s\n" +
                                    "Water:        %.1f L/day\n" +
                                    "Nutrients:    %.1f g/day\n" +
                                    "Greenhouse:   %s\n" +
                                    "Active:       %s\n\n" +
                                    "Statistics:\n" +
                                    "Diseases:     %d active\n" +
                                    "Tasks:        %d assigned\n" +
                                    "Harvests:     %d records\n" +
                                    "=================================",
                            rs.getInt("Plant_ID"),
                            rs.getString("name"),
                            rs.getString("plant_type") != null ? rs.getString("plant_type") : "Not specified",
                            rs.getDouble("wateramount"),
                            rs.getDouble("nut_amount"),
                            greenhouseName,
                            rs.getInt("active") == 1 ? "Yes" : "No",
                            rs.getInt("disease_count"),
                            rs.getInt("task_count"),
                            rs.getInt("harvest_count")
                    );

                    JDialog detailDialog = new JDialog(this, "Plant Details - " + rs.getString("name"), true);
                    detailDialog.setSize(500, 450);
                    detailDialog.setLocationRelativeTo(this);

                    JTextArea detailsArea = new JTextArea(details);
                    detailsArea.setEditable(false);
                    detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                    detailsArea.setMargin(new Insets(10, 10, 10, 10));

                    detailDialog.add(new JScrollPane(detailsArea));
                    detailDialog.setVisible(true);
                } else {
                    showError("Plant not found in database");
                }

            } catch (SQLException e) {
                e.printStackTrace();
                showError("Error loading plant details: " + e.getMessage());
            } finally {
                try { if (rs != null) rs.close(); } catch (SQLException e) {}
                try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
                DatabaseConnection.closeConnection(conn);
            }
        }
    }

    private void scheduleTask() {
        int selectedRow = plantTable.getSelectedRow();
        if (selectedRow >= 0) {
            int modelRow = plantTable.convertRowIndexToModel(selectedRow);
            String plantName = (String) tableModel.getValueAt(modelRow, 1);
            int plantId = (int) tableModel.getValueAt(modelRow, 0);

            JDialog scheduleDialog = new JDialog(this, "Schedule Task for " + plantName, true);
            scheduleDialog.setSize(400, 400);
            scheduleDialog.setLocationRelativeTo(this);
            scheduleDialog.setLayout(new BorderLayout());

            JPanel formPanel = new JPanel(new GridBagLayout());
            formPanel.setBackground(Color.WHITE);
            formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

            GridBagConstraints gbc = new GridBagConstraints();
            gbc.fill = GridBagConstraints.HORIZONTAL;
            gbc.insets = new Insets(5, 5, 5, 5);

            // Task Type
            gbc.gridx = 0;
            gbc.gridy = 0;
            formPanel.add(new JLabel("Task Type:*"), gbc);

            gbc.gridx = 1;
            JComboBox<String> taskTypeCombo = new JComboBox<>(new String[]{
                    "Watering", "Fertilizing", "Pruning", "Harvesting", "Pest Control", "Other"
            });
            formPanel.add(taskTypeCombo, gbc);

            // Date
            gbc.gridx = 0;
            gbc.gridy = 1;
            formPanel.add(new JLabel("Due Date:*"), gbc);

            gbc.gridx = 1;
            JTextField dateField = new JTextField(15);
            dateField.setText(java.time.LocalDate.now().plusDays(1).toString());
            formPanel.add(dateField, gbc);

            // Priority
            gbc.gridx = 0;
            gbc.gridy = 2;
            formPanel.add(new JLabel("Priority:"), gbc);

            gbc.gridx = 1;
            JComboBox<String> priorityCombo = new JComboBox<>(new String[]{
                    "Low", "Medium", "High", "Critical"
            });
            formPanel.add(priorityCombo, gbc);

            // Notes
            gbc.gridx = 0;
            gbc.gridy = 3;
            formPanel.add(new JLabel("Notes:"), gbc);

            gbc.gridx = 1;
            gbc.gridheight = 2;
            JTextArea notesArea = new JTextArea(3, 15);
            notesArea.setLineWrap(true);
            JScrollPane notesScroll = new JScrollPane(notesArea);
            formPanel.add(notesScroll, gbc);

            // Buttons
            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBackground(Color.WHITE);

            JButton saveButton = new JButton("Schedule Task");
            saveButton.setBackground(new Color(46, 125, 50));
            saveButton.setForeground(Color.BLACK);

            JButton cancelButton = new JButton("Cancel");
            cancelButton.setBackground(new Color(219, 68, 55));
            cancelButton.setForeground(Color.BLACK);

            saveButton.addActionListener(e -> {
                String taskType = (String) taskTypeCombo.getSelectedItem();
                String date = dateField.getText().trim();
                String priority = (String) priorityCombo.getSelectedItem();
                String notes = notesArea.getText().trim();

                if (date.isEmpty()) {
                    JOptionPane.showMessageDialog(scheduleDialog,
                            "Please enter a due date",
                            "Validation Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                // Save to database
                Connection conn = null;
                PreparedStatement stmt = null;

                try {
                    conn = DatabaseConnection.getConnection();
                    String query = "INSERT INTO Task (plant_id, task_name, due_date, status, notes) VALUES (?, ?, ?, 'Pending', ?)";
                    stmt = conn.prepareStatement(query);
                    stmt.setInt(1, plantId);
                    stmt.setString(2, taskType + " - " + plantName + " (" + priority + ")");
                    stmt.setString(3, date);
                    stmt.setString(4, notes);

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        String taskDetails = String.format(
                                "Task Scheduled Successfully!\n\n" +
                                        "Plant: %s\n" +
                                        "Task: %s\n" +
                                        "Due Date: %s\n" +
                                        "Priority: %s\n" +
                                        "Status: Pending\n" +
                                        "Notes: %s",
                                plantName, taskType, date, priority,
                                notes.isEmpty() ? "None" : notes
                        );

                        JOptionPane.showMessageDialog(this, taskDetails, "Task Scheduled", JOptionPane.INFORMATION_MESSAGE);
                        scheduleDialog.dispose();
                    } else {
                        JOptionPane.showMessageDialog(scheduleDialog,
                                "Failed to save task",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(scheduleDialog,
                            "Database error: " + ex.getMessage(),
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                } finally {
                    try { if (stmt != null) stmt.close(); } catch (SQLException ex) {}
                    DatabaseConnection.closeConnection(conn);
                }
            });

            cancelButton.addActionListener(e -> scheduleDialog.dispose());

            buttonPanel.add(cancelButton);
            buttonPanel.add(saveButton);

            scheduleDialog.add(formPanel, BorderLayout.CENTER);
            scheduleDialog.add(buttonPanel, BorderLayout.SOUTH);
            scheduleDialog.setVisible(true);
        } else {
            showError("Please select a plant to schedule a task");
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

    public void refreshTable() {
        loadPlantData();
    }
}