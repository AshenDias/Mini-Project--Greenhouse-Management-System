package view;

import controller.DiseaseController;
import util.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DiseaseView extends BaseView {
    private DiseaseController diseaseController;
    private JTable diseaseTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JButton addButton, treatButton, deleteButton, refreshButton, searchButton, backButton;
    private JPanel headerPanel;
    private DashboardView dashboardView;

    private List<Object[]> originalData = new ArrayList<>();

    public DiseaseView(DashboardView dashboardView) {
        super("Disease Management");
        diseaseController = new DiseaseController();
        this.dashboardView = dashboardView;
    }

    @Override
    protected void initializeUI() {
        // Create gradient main panel
        mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Color color1 = new Color(255, 245, 245); // Light red tint
                Color color2 = new Color(255, 235, 235); // Slightly darker red tint
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);

        createHeader();
        createToolbar();

        topContainer.add(headerPanel, BorderLayout.NORTH);
        topContainer.add(toolbar, BorderLayout.SOUTH);

        mainPanel.add(topContainer, BorderLayout.NORTH);

        createTable();
        createStatsPanel();

        add(mainPanel);
        loadDiseaseData();
    }

    private void createHeader() {
        headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Color color1 = new Color(219, 68, 55); // Dark red
                Color color2 = new Color(244, 67, 54); // Red
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), 0, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JLabel titleLabel = new JLabel("🦠 Disease Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        backButton = createStyledButton("← Back to Dashboard", new Color(255, 255, 255), new Color(219, 68, 55));
        backButton.setPreferredSize(new Dimension(180, 35));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(backButton, BorderLayout.EAST);
    }

    private JPanel toolbar;

    private void createToolbar() {
        toolbar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Color color1 = new Color(255, 245, 245); // Light red tint
                Color color2 = new Color(255, 240, 240); // Slightly darker
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), 0, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        toolbar.setOpaque(false);
        toolbar.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 2, 0, new Color(219, 68, 55, 150)),
                BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));

        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        searchPanel.setOpaque(false);

        JLabel searchLabel = new JLabel("Search:");
        searchLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        searchLabel.setForeground(new Color(219, 68, 55));

        searchField = new JTextField(20);
        searchField.setPreferredSize(new Dimension(200, 35));
        searchField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        searchField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(219, 68, 55, 150), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));

        searchButton = createStyledButton("🔍 Search", new Color(219, 68, 55), Color.WHITE);
        searchButton.setPreferredSize(new Dimension(100, 35));

        searchPanel.add(searchLabel);
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setOpaque(false);

        refreshButton = createStyledButton("🔄 Refresh", new Color(66, 133, 244), Color.WHITE);
        refreshButton.setPreferredSize(new Dimension(100, 35));

        addButton = createStyledButton("➕ Add Disease", new Color(219, 68, 55), Color.WHITE);
        addButton.setPreferredSize(new Dimension(130, 35));

        treatButton = createStyledButton("💊 Mark as Treated", new Color(46, 125, 50), Color.WHITE);
        treatButton.setPreferredSize(new Dimension(150, 35));

        deleteButton = createStyledButton("🗑️ Delete", new Color(158, 158, 158), Color.WHITE);
        deleteButton.setPreferredSize(new Dimension(100, 35));

        actionPanel.add(refreshButton);
        actionPanel.add(addButton);
        actionPanel.add(treatButton);
        actionPanel.add(deleteButton);

        toolbar.add(searchPanel, BorderLayout.WEST);
        toolbar.add(actionPanel, BorderLayout.EAST);
    }

    private void createTable() {
        String[] columns = {"ID", "Name", "Symptoms", "Severity", "Affected Plant", "Treatment"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }

            @Override
            public Class<?> getColumnClass(int column) {
                return column == 0 ? Integer.class : String.class;
            }
        };

        diseaseTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (c instanceof JComponent) {
                    ((JComponent) c).setOpaque(false);
                }
                return c;
            }
        };

        diseaseTable.setRowHeight(35);
        diseaseTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        diseaseTable.setOpaque(false);
        diseaseTable.setShowGrid(false);
        diseaseTable.setIntercellSpacing(new Dimension(0, 0));

        // Custom table header with gradient
        diseaseTable.getTableHeader().setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JComponent c = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setOpaque(false);
                c.setBackground(new Color(219, 68, 55));
                c.setForeground(Color.WHITE);
                c.setFont(new Font("Segoe UI", Font.BOLD, 12));
                c.setBorder(BorderFactory.createCompoundBorder(
                        BorderFactory.createMatteBorder(0, 0, 2, 0, Color.WHITE),
                        BorderFactory.createEmptyBorder(8, 10, 8, 10)
                ));
                return c;
            }
        });

        diseaseTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // Fix: Only call setOpaque if it's a JComponent
                if (c instanceof JComponent) {
                    ((JComponent) c).setOpaque(false);
                }

                // Gradient row background
                if (!isSelected) {
                    if (row % 2 == 0) {
                        c.setBackground(new Color(255, 250, 250));
                    } else {
                        c.setBackground(new Color(255, 245, 245));
                    }
                } else {
                    c.setBackground(new Color(219, 68, 55, 50));
                }

                if (column == 3) {
                    String severity = (String) value;
                    if (severity != null) {
                        switch (severity) {
                            case "Critical":
                                c.setForeground(new Color(219, 68, 55));
                                c.setFont(c.getFont().deriveFont(Font.BOLD));
                                break;
                            case "High":
                                c.setForeground(new Color(244, 180, 0));
                                break;
                            case "Medium":
                                c.setForeground(new Color(66, 133, 244));
                                break;
                            case "Low":
                                c.setForeground(new Color(46, 125, 50));
                                break;
                        }
                    }
                } else {
                    c.setForeground(Color.DARK_GRAY);
                }

                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(diseaseTable) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Color color1 = new Color(255, 250, 250);
                Color color2 = new Color(255, 245, 245);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(219, 68, 55, 100), 1));

        mainPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void createStatsPanel() {
        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Color color1 = new Color(255, 240, 240);
                Color color2 = new Color(255, 235, 235);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), 0, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        statsPanel.setOpaque(false);
        statsPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        // Fetch real stats from database
        int activeCount = getDiseaseCountBySeverity("High", "Critical");
        int totalCount = getTotalDiseaseCount();
        int treatedCount = totalCount - activeCount;

        statsPanel.add(createStatCard("Active Diseases", String.valueOf(activeCount), "🦠", new Color(219, 68, 55)));
        statsPanel.add(createStatCard("Treated", String.valueOf(treatedCount), "💊", new Color(46, 125, 50)));
        statsPanel.add(createStatCard("Total", String.valueOf(totalCount), "📊", new Color(66, 133, 244)));

        mainPanel.add(statsPanel, BorderLayout.SOUTH);
    }

    private JPanel createStatCard(String title, String value, String icon, Color color) {
        JPanel card = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Card gradient
                Color color1 = Color.WHITE;
                Color color2 = new Color(255, 250, 250);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                // Border gradient
                Graphics2D g2d2 = (Graphics2D) g.create();
                g2d2.setStroke(new BasicStroke(2));
                GradientPaint borderGp = new GradientPaint(0, 0, color.brighter(), 0, getHeight(), color);
                g2d2.setPaint(borderGp);
                g2d2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 10, 10);
                g2d2.dispose();
            }
        };

        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel valueLabel = new JLabel(icon + " " + value, SwingConstants.CENTER);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(color);

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(Color.DARK_GRAY);

        JPanel contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.add(valueLabel, BorderLayout.CENTER);
        contentPanel.add(titleLabel, BorderLayout.SOUTH);

        card.add(contentPanel, BorderLayout.CENTER);
        return card;
    }

    private void loadDiseaseData() {
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
            String query = "SELECT d.Disease_ID, d.name, d.symptoms, d.severity, " +
                    "p.name as plant_name, d.treatment " +
                    "FROM Disease d " +
                    "LEFT JOIN Plant p ON d.affected_plant_id = p.Plant_ID " +
                    "ORDER BY d.severity DESC, d.Disease_ID";

            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                Object[] row = new Object[]{
                        rs.getInt("Disease_ID"),
                        rs.getString("name"),
                        rs.getString("symptoms"),
                        rs.getString("severity"),
                        rs.getString("plant_name"),
                        rs.getString("treatment")
                };
                tableModel.addRow(row);
                if (originalData != null) {
                    originalData.add(row);
                }
            }

            showSuccess("Loaded " + tableModel.getRowCount() + " diseases from database");

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error loading disease data: " + e.getMessage());

            Object[][] data = {
                    {201, "Powdery Mildew", "White spots on leaves", "Medium", "Tomato Plant", "Apply fungicide weekly"},
                    {202, "Leaf Spot", "Brown circular spots", "Low", "Rose Bush", "Remove affected leaves"},
                    {203, "Root Rot", "Wilting, yellow leaves", "High", "Lemon Tree", "Improve drainage"},
                    {204, "Aphid Infestation", "Small green insects", "Medium", "Basil Herb", "Spray with soap"},
                    {205, "Blossom End Rot", "Dark spots on fruit", "Medium", "Tomato Plant", "Add calcium"}
            };

            for (Object[] row : data) {
                tableModel.addRow(row);
                if (originalData != null) {
                    originalData.add(row);
                }
            }
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
        updateButtonStates();
    }

    private int getDiseaseCountBySeverity(String... severities) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int count = 0;

        try {
            conn = DatabaseConnection.getConnection();
            String query = "SELECT COUNT(*) as count FROM Disease WHERE severity IN (";
            for (int i = 0; i < severities.length; i++) {
                query += "?";
                if (i < severities.length - 1) query += ",";
            }
            query += ")";

            stmt = conn.prepareStatement(query);
            for (int i = 0; i < severities.length; i++) {
                stmt.setString(i + 1, severities[i]);
            }

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

    private int getTotalDiseaseCount() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int count = 0;

        try {
            conn = DatabaseConnection.getConnection();
            String query = "SELECT COUNT(*) as count FROM Disease";
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

    @Override
    protected void setupEventListeners() {
        addButton.addActionListener(e -> addDisease());
        treatButton.addActionListener(e -> markAsTreated());
        deleteButton.addActionListener(e -> deleteDisease());
        refreshButton.addActionListener(e -> {
            searchField.setText(""); // Clear search field
            loadDiseaseData();
        });
        searchButton.addActionListener(e -> searchDiseases());
        backButton.addActionListener(e -> goBackToDashboard());

        searchField.addActionListener(e -> searchDiseases());

        diseaseTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                updateButtonStates();
            }
        });

        diseaseTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                if (evt.getClickCount() == 2) {
                    viewDiseaseDetails();
                }
            }
        });

        treatButton.setEnabled(false);
        deleteButton.setEnabled(false);
    }

    private void updateButtonStates() {
        boolean hasSelection = diseaseTable.getSelectedRow() >= 0;
        treatButton.setEnabled(hasSelection);
        deleteButton.setEnabled(hasSelection);
    }

    private void searchDiseases() {
        String searchText = searchField.getText().trim().toLowerCase();

        if (searchText.isEmpty()) {
            tableModel.setRowCount(0);
            if (originalData != null) {
                for (Object[] row : originalData) {
                    tableModel.addRow(row);
                }
            } else {
                loadDiseaseData();
            }
            return;
        }

        tableModel.setRowCount(0);
        int matchCount = 0;

        if (originalData != null) {
            for (Object[] row : originalData) {
                boolean match = false;

                for (int j = 1; j < row.length; j++) {
                    if (row[j] != null && row[j].toString().toLowerCase().contains(searchText)) {
                        match = true;
                        break;
                    }
                }

                if (match) {
                    tableModel.addRow(row);
                    matchCount++;
                }
            }
        }

        if (matchCount == 0) {
            JOptionPane.showMessageDialog(this,
                    "No diseases found matching: " + searchText,
                    "Search Results",
                    JOptionPane.INFORMATION_MESSAGE);
        } else {
            showSuccess("Found " + matchCount + " disease(s) matching: " + searchText);
        }
    }

    private void addDisease() {
        JDialog addDialog = new JDialog(this, "Add New Disease", true);
        addDialog.setLayout(new BorderLayout());
        addDialog.setSize(450, 500);
        addDialog.setLocationRelativeTo(this);

        // Create gradient background for dialog
        JPanel dialogPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Color color1 = new Color(255, 250, 250);
                Color color2 = new Color(255, 245, 245);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel nameLabel = new JLabel("Disease Name*:");
        nameLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        nameLabel.setForeground(new Color(219, 68, 55));
        formPanel.add(nameLabel, gbc);

        gbc.gridx = 1;
        JTextField nameField = new JTextField(20);
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        nameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(219, 68, 55, 150), 1),
                BorderFactory.createEmptyBorder(5, 10, 5, 10)
        ));
        formPanel.add(nameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel symptomsLabel = new JLabel("Symptoms*:");
        symptomsLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        symptomsLabel.setForeground(new Color(219, 68, 55));
        formPanel.add(symptomsLabel, gbc);

        gbc.gridx = 1;
        JTextArea symptomsArea = new JTextArea(3, 20);
        symptomsArea.setLineWrap(true);
        symptomsArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JScrollPane symptomsScroll = new JScrollPane(symptomsArea);
        symptomsScroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(219, 68, 55, 150), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        formPanel.add(symptomsScroll, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel severityLabel = new JLabel("Severity*:");
        severityLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        severityLabel.setForeground(new Color(219, 68, 55));
        formPanel.add(severityLabel, gbc);

        gbc.gridx = 1;
        String[] severities = {"Low", "Medium", "High", "Critical"};
        JComboBox<String> severityCombo = new JComboBox<>(severities);
        severityCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        severityCombo.setBackground(Color.WHITE);
        severityCombo.setSelectedIndex(1);
        formPanel.add(severityCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel plantLabel = new JLabel("Affected Plant:");
        plantLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        plantLabel.setForeground(new Color(219, 68, 55));
        formPanel.add(plantLabel, gbc);

        gbc.gridx = 1;
        JComboBox<String> plantCombo = new JComboBox<>();
        plantCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        plantCombo.setBackground(Color.WHITE);
        loadPlantsIntoComboBox(plantCombo);
        formPanel.add(plantCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel treatmentLabel = new JLabel("Treatment:");
        treatmentLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        treatmentLabel.setForeground(new Color(219, 68, 55));
        formPanel.add(treatmentLabel, gbc);

        gbc.gridx = 1;
        JTextArea treatmentArea = new JTextArea(3, 20);
        treatmentArea.setLineWrap(true);
        treatmentArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        JScrollPane treatmentScroll = new JScrollPane(treatmentArea);
        treatmentScroll.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(219, 68, 55, 150), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        formPanel.add(treatmentScroll, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        JButton saveButton = createStyledButton("Save", new Color(46, 125, 50), Color.WHITE);
        saveButton.setPreferredSize(new Dimension(100, 35));

        JButton cancelButton = createStyledButton("Cancel", new Color(158, 158, 158), Color.WHITE);
        cancelButton.setPreferredSize(new Dimension(100, 35));

        saveButton.addActionListener(e -> {
            String name = nameField.getText().trim();
            String symptoms = symptomsArea.getText().trim();
            String severity = (String) severityCombo.getSelectedItem();
            String selectedPlant = (String) plantCombo.getSelectedItem();
            String treatment = treatmentArea.getText().trim();

            if (name.isEmpty() || symptoms.isEmpty()) {
                JOptionPane.showMessageDialog(addDialog,
                        "Please fill in all required fields (*)",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            int plantId = 0;
            if (selectedPlant != null && !selectedPlant.equals("None") && selectedPlant.contains("[")) {
                String idStr = selectedPlant.substring(selectedPlant.indexOf("[") + 1, selectedPlant.indexOf("]"));
                plantId = Integer.parseInt(idStr);
            }

            boolean success = diseaseController.addDisease(name, symptoms, severity,
                    plantId > 0 ? plantId : null, treatment);

            if (success) {
                loadDiseaseData();
                addDialog.dispose();
                JOptionPane.showMessageDialog(this,
                        "Disease added successfully!",
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(addDialog,
                        "Failed to add disease. Please check database connection.",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> addDialog.dispose());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        dialogPanel.add(formPanel, BorderLayout.CENTER);
        dialogPanel.add(buttonPanel, BorderLayout.SOUTH);

        addDialog.add(dialogPanel);
        addDialog.setVisible(true);
    }

    private void loadPlantsIntoComboBox(JComboBox<String> combo) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            String query = "SELECT Plant_ID, name FROM Plant WHERE active = 1 ORDER BY name";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            combo.addItem("None");
            while (rs.next()) {
                combo.addItem(rs.getString("name") + " [" + rs.getInt("Plant_ID") + "]");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
    }

    private void markAsTreated() {
        int row = diseaseTable.getSelectedRow();
        if (row >= 0) {
            int modelRow = diseaseTable.convertRowIndexToModel(row);
            int diseaseId = (int) tableModel.getValueAt(modelRow, 0);
            String diseaseName = (String) tableModel.getValueAt(modelRow, 1);

            int response = JOptionPane.showConfirmDialog(this,
                    "Mark '" + diseaseName + "' as treated?\n" +
                            "This will remove the disease from the active list.",
                    "Confirm Treatment",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.QUESTION_MESSAGE);

            if (response == JOptionPane.YES_OPTION) {
                boolean success = diseaseController.deleteDisease(diseaseId);

                if (success) {
                    tableModel.removeRow(modelRow);
                    JOptionPane.showMessageDialog(this,
                            "Disease '" + diseaseName + "' marked as treated and removed",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    updateButtonStates();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Error removing disease. Please try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a disease to mark as treated",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteDisease() {
        int row = diseaseTable.getSelectedRow();
        if (row >= 0) {
            int modelRow = diseaseTable.convertRowIndexToModel(row);
            String name = (String) tableModel.getValueAt(modelRow, 1);
            int id = (int) tableModel.getValueAt(modelRow, 0);

            int response = JOptionPane.showConfirmDialog(this,
                    "Are you sure you want to delete disease '" + name + "' (ID: " + id + ")?",
                    "Confirm Delete",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);

            if (response == JOptionPane.YES_OPTION) {
                boolean success = diseaseController.deleteDisease(id);

                if (success) {
                    tableModel.removeRow(modelRow);
                    JOptionPane.showMessageDialog(this,
                            "Disease '" + name + "' deleted successfully",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                    updateButtonStates();
                } else {
                    JOptionPane.showMessageDialog(this,
                            "Error deleting disease. Please try again.",
                            "Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this,
                    "Please select a disease to delete",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void viewDiseaseDetails() {
        int row = diseaseTable.getSelectedRow();
        if (row >= 0) {
            int modelRow = diseaseTable.convertRowIndexToModel(row);
            int diseaseId = (int) tableModel.getValueAt(modelRow, 0);

            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                conn = DatabaseConnection.getConnection();
                String query = "SELECT d.*, p.name as plant_name, g.name as greenhouse_name " +
                        "FROM Disease d " +
                        "LEFT JOIN Plant p ON d.affected_plant_id = p.Plant_ID " +
                        "LEFT JOIN Greenhouse g ON p.greenhouse_id = g.greenhouse_id " +
                        "WHERE d.Disease_ID = ?";

                stmt = conn.prepareStatement(query);
                stmt.setInt(1, diseaseId);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    String details = String.format(
                            "=================================\n" +
                                    "DISEASE DETAILS\n" +
                                    "=================================\n" +
                                    "ID:           %d\n" +
                                    "Name:         %s\n" +
                                    "Symptoms:     %s\n" +
                                    "Severity:     %s\n" +
                                    "Affected Plant: %s\n" +
                                    "Plant Location: %s\n\n" +
                                    "Treatment:\n%s\n" +
                                    "=================================",
                            rs.getInt("Disease_ID"),
                            rs.getString("name"),
                            rs.getString("symptoms"),
                            rs.getString("severity"),
                            rs.getString("plant_name"),
                            rs.getString("greenhouse_name"),
                            rs.getString("treatment")
                    );

                    JDialog detailDialog = new JDialog(this, "Disease Details", true);
                    detailDialog.setSize(500, 400);
                    detailDialog.setLocationRelativeTo(this);

                    // Gradient background for details dialog
                    JPanel detailPanel = new JPanel(new BorderLayout()) {
                        @Override
                        protected void paintComponent(Graphics g) {
                            super.paintComponent(g);
                            Graphics2D g2d = (Graphics2D) g;
                            g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                            Color color1 = new Color(255, 250, 250);
                            Color color2 = new Color(255, 245, 245);
                            GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                            g2d.setPaint(gp);
                            g2d.fillRect(0, 0, getWidth(), getHeight());
                        }
                    };

                    JTextArea detailsArea = new JTextArea(details);
                    detailsArea.setEditable(false);
                    detailsArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                    detailsArea.setMargin(new Insets(10, 10, 10, 10));
                    detailsArea.setOpaque(false);

                    detailPanel.add(new JScrollPane(detailsArea), BorderLayout.CENTER);
                    detailDialog.add(detailPanel);
                    detailDialog.setVisible(true);
                }

            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Error loading disease details: " + e.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            } finally {
                try { if (rs != null) rs.close(); } catch (SQLException e) {}
                try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
                DatabaseConnection.closeConnection(conn);
            }
        }
    }

    private void goBackToDashboard() {
        int response = JOptionPane.showConfirmDialog(this,
                "Are you sure you want to return to the dashboard?",
                "Return to Dashboard",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (response == JOptionPane.YES_OPTION) {
            if (dashboardView != null) {
                dashboardView.setVisible(true);
                dashboardView.refreshDashboardData();
            }
            this.dispose();
        }
    }
}