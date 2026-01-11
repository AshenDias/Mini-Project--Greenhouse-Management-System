package view;

import util.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;

public class HarvestView extends BaseView {
    private JTable harvestTable;
    private DefaultTableModel tableModel;
    private JButton addButton, reportButton, exportButton, backButton;
    private JComboBox<String> filterCombo;
    private DashboardView dashboardView;

    public HarvestView(DashboardView dashboardView) {
        super("Harvest Management");
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
                Color color1 = new Color(240, 250, 240); // Very light green
                Color color2 = new Color(210, 240, 210); // Light green
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };

        createHeader();
        createToolbar();
        createTable();
        createSummaryPanel();

        add(mainPanel);
        loadHarvestData();
    }

    @Override
    protected void setupEventListeners() {
        addButton.addActionListener(e -> addHarvest());
        reportButton.addActionListener(e -> generateReport());
        exportButton.addActionListener(e -> exportData());
        backButton.addActionListener(e -> goBackToDashboard());
        filterCombo.addActionListener(e -> filterHarvestData());
    }

    private void createHeader() {
        headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Color color1 = new Color(121, 85, 72); // Brown color for harvest
                Color color2 = new Color(141, 110, 99); // Lighter brown
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), 0, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JLabel titleLabel = new JLabel("🌾 Harvest Records");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        backButton = createStyledButton("← Back to Dashboard", new Color(240, 240, 240), new Color(121, 85, 72));
        backButton.setFont(new Font("Segoe UI", Font.BOLD, 12));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(backButton, BorderLayout.EAST);
    }

    private void createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Color color1 = new Color(230, 240, 230);
                Color color2 = new Color(220, 230, 220);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), 0, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        toolbar.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        // Filter
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        filterPanel.setOpaque(false);

        JLabel filterLabel = new JLabel("Filter:");
        filterLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        filterPanel.add(filterLabel);

        filterCombo = new JComboBox<>(new String[]{"All", "This Month", "This Week", "By Plant", "By Greenhouse", "By Quality"});
        filterCombo.setPreferredSize(new Dimension(150, 32));
        filterCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        filterCombo.setBackground(Color.WHITE);
        filterCombo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(121, 85, 72, 150), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        filterPanel.add(filterCombo);

        // Actions
        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        actionPanel.setOpaque(false);

        addButton = createStyledButton("➕ Record Harvest", new Color(121, 85, 72), Color.WHITE);
        addButton.setPreferredSize(new Dimension(160, 35));

        reportButton = createStyledButton("📈 Yield Report", new Color(76, 175, 80), Color.WHITE);
        reportButton.setPreferredSize(new Dimension(140, 35));

        exportButton = createStyledButton("📤 Export", new Color(66, 133, 244), Color.WHITE);
        exportButton.setPreferredSize(new Dimension(100, 35));

        actionPanel.add(addButton);
        actionPanel.add(Box.createHorizontalStrut(10));
        actionPanel.add(reportButton);
        actionPanel.add(Box.createHorizontalStrut(10));
        actionPanel.add(exportButton);

        toolbar.add(filterPanel, BorderLayout.WEST);
        toolbar.add(actionPanel, BorderLayout.EAST);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.setOpaque(false);
        topContainer.add(headerPanel, BorderLayout.NORTH);
        topContainer.add(toolbar, BorderLayout.SOUTH);

        mainPanel.add(topContainer, BorderLayout.NORTH);
    }

    private void createTable() {
        String[] columns = {"ID", "Plant", "Greenhouse", "Date", "Quantity", "Unit", "Quality"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        harvestTable = new JTable(tableModel) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (c instanceof JComponent) {
                    ((JComponent) c).setOpaque(false);
                }
                return c;
            }
        };

        harvestTable.setRowHeight(35);
        harvestTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        harvestTable.setOpaque(false);
        harvestTable.setShowGrid(false);
        harvestTable.setIntercellSpacing(new Dimension(0, 0));

        // Custom table header with gradient
        harvestTable.getTableHeader().setDefaultRenderer(new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                JComponent c = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setOpaque(false);
                c.setBackground(new Color(121, 85, 72));
                c.setForeground(Color.WHITE);
                c.setFont(new Font("Segoe UI", Font.BOLD, 12));
                c.setBorder(BorderFactory.createEmptyBorder(10, 8, 10, 8));
                return c;
            }
        });

        harvestTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected,
                        hasFocus, row, column);

                // FIX: Check if component is a JComponent before calling setOpaque
                if (c instanceof JComponent) {
                    ((JComponent) c).setOpaque(false);

                    if (!isSelected) {
                        if (row % 2 == 0) {
                            ((JComponent) c).setBackground(new Color(250, 250, 250));
                        } else {
                            ((JComponent) c).setBackground(new Color(245, 250, 245));
                        }
                    } else {
                        ((JComponent) c).setBackground(new Color(121, 85, 72, 50));
                    }
                }

                if (column == 6) {
                    String quality = (String) value;
                    if (quality != null) {
                        switch (quality) {
                            case "Excellent":
                                c.setForeground(new Color(46, 125, 50));
                                c.setFont(c.getFont().deriveFont(Font.BOLD));
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
                        }
                    }
                } else {
                    c.setForeground(Color.DARK_GRAY);
                }

                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(harvestTable) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Color color1 = new Color(250, 255, 250);
                Color color2 = new Color(245, 250, 245);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(121, 85, 72, 100), 1));
        mainPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void createSummaryPanel() {
        JPanel summaryPanel = new JPanel(new GridLayout(1, 2, 10, 0)) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Color color1 = new Color(240, 245, 240);
                Color color2 = new Color(230, 240, 230);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        summaryPanel.setOpaque(false);
        summaryPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));

        double monthlyHarvest = getMonthlyHarvestTotal();
        int harvestCount = getMonthlyHarvestCount();

        JPanel totalPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Card gradient
                Color color1 = Color.WHITE;
                Color color2 = new Color(255, 255, 255);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // Border gradient
                Graphics2D g2d2 = (Graphics2D) g.create();
                g2d2.setStroke(new BasicStroke(1));
                GradientPaint borderGp = new GradientPaint(0, 0, new Color(121, 85, 72).brighter(), 0, getHeight(), new Color(121, 85, 72));
                g2d2.setPaint(borderGp);
                g2d2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                g2d2.dispose();
            }
        };
        totalPanel.setOpaque(false);
        totalPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(121, 85, 72), 1),
                "Harvest This Month"
        ));

        JLabel totalLabel = new JLabel(String.format("%.1f kg", monthlyHarvest), SwingConstants.CENTER);
        totalLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        totalLabel.setForeground(new Color(121, 85, 72));
        totalPanel.add(totalLabel, BorderLayout.CENTER);

        JPanel countPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Card gradient
                Color color1 = Color.WHITE;
                Color color2 = new Color(255, 255, 255);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // Border gradient
                Graphics2D g2d2 = (Graphics2D) g.create();
                g2d2.setStroke(new BasicStroke(1));
                GradientPaint borderGp = new GradientPaint(0, 0, new Color(46, 125, 50).brighter(), 0, getHeight(), new Color(46, 125, 50));
                g2d2.setPaint(borderGp);
                g2d2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                g2d2.dispose();
            }
        };
        countPanel.setOpaque(false);
        countPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(46, 125, 50), 1),
                "Harvest Count"
        ));

        JLabel countLabel = new JLabel(harvestCount + " records", SwingConstants.CENTER);
        countLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        countLabel.setForeground(new Color(46, 125, 50));
        countPanel.add(countLabel, BorderLayout.CENTER);

        summaryPanel.add(totalPanel);
        summaryPanel.add(countPanel);

        mainPanel.add(summaryPanel, BorderLayout.SOUTH);
    }

    private void loadHarvestData() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            tableModel.setRowCount(0);

            conn = DatabaseConnection.getConnection();
            String query = "SELECT h.harvest_id, p.name as plant_name, g.name as greenhouse_name, " +
                    "h.harvest_date, h.quantity, h.quality " +
                    "FROM Harvest h " +
                    "JOIN Plant p ON h.plant_id = p.Plant_ID " +
                    "JOIN Greenhouse g ON h.greenhouse_id = g.greenhouse_id " +
                    "ORDER BY h.harvest_date DESC";

            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                tableModel.addRow(new Object[]{
                        rs.getInt("harvest_id"),
                        rs.getString("plant_name"),
                        rs.getString("greenhouse_name"),
                        rs.getDate("harvest_date"),
                        String.format("%.1f", rs.getDouble("quantity")),
                        "kg",
                        rs.getString("quality")
                });
            }

            showSuccess("Loaded " + tableModel.getRowCount() + " harvest records from database");

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error loading harvest data: " + e.getMessage());

            Object[][] data = {
                    {1, "Tomato Plant", "Main Greenhouse", java.sql.Date.valueOf("2024-03-15"), "12.5", "kg", "Excellent"},
                    {2, "Basil Herb", "Research Greenhouse", java.sql.Date.valueOf("2024-03-10"), "2.3", "kg", "Good"},
                    {3, "Lemon Tree", "Seedling Greenhouse", java.sql.Date.valueOf("2024-03-05"), "8.7", "kg", "Excellent"},
                    {4, "Lettuce", "Hydroponic Greenhouse", java.sql.Date.valueOf("2024-03-12"), "5.4", "kg", "Good"}
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

    private double getMonthlyHarvestTotal() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        double total = 0;

        try {
            conn = DatabaseConnection.getConnection();
            String query = "SELECT SUM(quantity) as total FROM Harvest " +
                    "WHERE MONTH(harvest_date) = MONTH(CURDATE()) " +
                    "AND YEAR(harvest_date) = YEAR(CURDATE())";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            if (rs.next()) {
                total = rs.getDouble("total");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
        return total;
    }

    private int getMonthlyHarvestCount() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int count = 0;

        try {
            conn = DatabaseConnection.getConnection();
            String query = "SELECT COUNT(*) as count FROM Harvest " +
                    "WHERE MONTH(harvest_date) = MONTH(CURDATE()) " +
                    "AND YEAR(harvest_date) = YEAR(CURDATE())";
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

    private void addHarvest() {
        JDialog harvestDialog = new JDialog(this, "Record Harvest", true);
        harvestDialog.setSize(400, 500);
        harvestDialog.setLocationRelativeTo(this);

        // Create gradient background for dialog
        JPanel dialogPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Color color1 = new Color(245, 250, 245);
                Color color2 = new Color(235, 245, 235);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        harvestDialog.setContentPane(dialogPanel);

        JPanel formPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Card gradient
                Color color1 = Color.WHITE;
                Color color2 = new Color(250, 255, 250);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRoundRect(10, 10, getWidth()-20, getHeight()-20, 15, 15);

                // Border gradient
                Graphics2D g2d2 = (Graphics2D) g.create();
                g2d2.setStroke(new BasicStroke(1));
                GradientPaint borderGp = new GradientPaint(0, 0, new Color(121, 85, 72).brighter(), 0, getHeight(), new Color(121, 85, 72));
                g2d2.setPaint(borderGp);
                g2d2.drawRoundRect(10, 10, getWidth()-20, getHeight()-20, 15, 15);
                g2d2.dispose();
            }
        };
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Plant selection
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel plantLabel = new JLabel("Plant:*");
        plantLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        plantLabel.setForeground(new Color(121, 85, 72));
        formPanel.add(plantLabel, gbc);

        gbc.gridx = 1;
        JComboBox<String> plantCombo = new JComboBox<>();
        plantCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        plantCombo.setBackground(Color.WHITE);
        plantCombo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(121, 85, 72, 150), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        loadPlantsIntoComboBox(plantCombo);
        formPanel.add(plantCombo, gbc);

        // Greenhouse selection
        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel greenhouseLabel = new JLabel("Greenhouse:*");
        greenhouseLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        greenhouseLabel.setForeground(new Color(121, 85, 72));
        formPanel.add(greenhouseLabel, gbc);

        gbc.gridx = 1;
        JComboBox<String> greenhouseCombo = new JComboBox<>();
        greenhouseCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        greenhouseCombo.setBackground(Color.WHITE);
        greenhouseCombo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(121, 85, 72, 150), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        loadGreenhousesIntoComboBox(greenhouseCombo);
        formPanel.add(greenhouseCombo, gbc);

        // Date
        gbc.gridx = 0;
        gbc.gridy = 2;
        JLabel dateLabel = new JLabel("Date (YYYY-MM-DD):*");
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        dateLabel.setForeground(new Color(121, 85, 72));
        formPanel.add(dateLabel, gbc);

        gbc.gridx = 1;
        JTextField dateField = new JTextField(15);
        dateField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dateField.setText(java.time.LocalDate.now().toString());
        dateField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(121, 85, 72, 150), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        formPanel.add(dateField, gbc);

        // Quantity
        gbc.gridx = 0;
        gbc.gridy = 3;
        JLabel quantityLabel = new JLabel("Quantity:*");
        quantityLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        quantityLabel.setForeground(new Color(121, 85, 72));
        formPanel.add(quantityLabel, gbc);

        gbc.gridx = 1;
        JTextField quantityField = new JTextField(10);
        quantityField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        quantityField.setText("0.0");
        quantityField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(121, 85, 72, 150), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        formPanel.add(quantityField, gbc);

        // Quality
        gbc.gridx = 0;
        gbc.gridy = 4;
        JLabel qualityLabel = new JLabel("Quality:*");
        qualityLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        qualityLabel.setForeground(new Color(121, 85, 72));
        formPanel.add(qualityLabel, gbc);

        gbc.gridx = 1;
        JComboBox<String> qualityCombo = new JComboBox<>(new String[]{"Excellent", "Good", "Fair", "Poor"});
        qualityCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        qualityCombo.setBackground(Color.WHITE);
        qualityCombo.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(121, 85, 72, 150), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        formPanel.add(qualityCombo, gbc);

        // Notes
        gbc.gridx = 0;
        gbc.gridy = 5;
        JLabel notesLabel = new JLabel("Notes:");
        notesLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        notesLabel.setForeground(new Color(121, 85, 72));
        formPanel.add(notesLabel, gbc);

        gbc.gridx = 1;
        gbc.gridheight = 2;
        JTextArea notesArea = new JTextArea(3, 15);
        notesArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        notesArea.setLineWrap(true);
        notesArea.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(121, 85, 72, 150), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        JScrollPane notesScroll = new JScrollPane(notesArea);
        notesScroll.setBorder(BorderFactory.createEmptyBorder());
        formPanel.add(notesScroll, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        JButton saveButton = createStyledButton("Save Harvest", new Color(46, 125, 50), Color.WHITE);
        saveButton.setPreferredSize(new Dimension(120, 35));

        JButton cancelButton = createStyledButton("Cancel", new Color(219, 68, 55), Color.WHITE);
        cancelButton.setPreferredSize(new Dimension(100, 35));

        saveButton.addActionListener(e -> {
            String selectedPlant = (String) plantCombo.getSelectedItem();
            String selectedGreenhouse = (String) greenhouseCombo.getSelectedItem();
            String date = dateField.getText().trim();
            String quantity = quantityField.getText().trim();
            String quality = (String) qualityCombo.getSelectedItem();
            String notes = notesArea.getText().trim();

            if (selectedPlant == null || selectedGreenhouse == null ||
                    date.isEmpty() || quantity.isEmpty()) {
                JOptionPane.showMessageDialog(harvestDialog,
                        "Please fill in all required fields",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            try {
                double qty = Double.parseDouble(quantity);
                if (qty <= 0) {
                    JOptionPane.showMessageDialog(harvestDialog,
                            "Quantity must be positive",
                            "Validation Error",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }

                int plantId = extractIdFromSelection(selectedPlant);
                int greenhouseId = extractIdFromSelection(selectedGreenhouse);

                Connection conn = null;
                PreparedStatement stmt = null;

                try {
                    conn = DatabaseConnection.getConnection();

                    int nextId = getNextHarvestId();

                    String query = "INSERT INTO Harvest (harvest_id, plant_id, greenhouse_id, harvest_date, quantity, quality) " +
                            "VALUES (?, ?, ?, ?, ?, ?)";
                    stmt = conn.prepareStatement(query);
                    stmt.setInt(1, nextId);
                    stmt.setInt(2, plantId);
                    stmt.setInt(3, greenhouseId);
                    stmt.setDate(4, java.sql.Date.valueOf(date));
                    stmt.setDouble(5, qty);
                    stmt.setString(6, quality);

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        String plantName = selectedPlant.replaceAll(" \\[\\d+\\]$", "");
                        String greenhouseName = selectedGreenhouse.replaceAll(" \\[\\d+\\]$", "");

                        tableModel.addRow(new Object[]{
                                nextId,
                                plantName,
                                greenhouseName,
                                java.sql.Date.valueOf(date),
                                String.format("%.1f", qty),
                                "kg",
                                quality
                        });

                        showSuccess("Harvest recorded successfully!");
                        harvestDialog.dispose();
                        loadHarvestData(); // Reload to refresh stats
                    }

                } catch (SQLException ex) {
                    ex.printStackTrace();
                    showError("Error recording harvest: " + ex.getMessage());
                } finally {
                    try { if (stmt != null) stmt.close(); } catch (SQLException ex) {}
                    DatabaseConnection.closeConnection(conn);
                }

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(harvestDialog,
                        "Quantity must be a number",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(harvestDialog,
                        "Invalid date format. Use YYYY-MM-DD",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        cancelButton.addActionListener(e -> harvestDialog.dispose());

        buttonPanel.add(cancelButton);
        buttonPanel.add(Box.createHorizontalStrut(10));
        buttonPanel.add(saveButton);

        harvestDialog.add(formPanel, BorderLayout.CENTER);
        harvestDialog.add(buttonPanel, BorderLayout.SOUTH);
        harvestDialog.setVisible(true);
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

    private void loadGreenhousesIntoComboBox(JComboBox<String> combo) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            String query = "SELECT greenhouse_id, name FROM Greenhouse ORDER BY name";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                combo.addItem(rs.getString("name") + " [" + rs.getInt("greenhouse_id") + "]");
            }

        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
    }

    private int extractIdFromSelection(String selection) {
        if (selection.contains("[")) {
            String idStr = selection.substring(selection.indexOf("[") + 1, selection.indexOf("]"));
            return Integer.parseInt(idStr);
        }
        return 0;
    }

    private int getNextHarvestId() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int nextId = 1;

        try {
            conn = DatabaseConnection.getConnection();
            String query = "SELECT MAX(harvest_id) as max_id FROM Harvest";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            if (rs.next()) {
                nextId = rs.getInt("max_id") + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
        return nextId;
    }

    private void generateReport() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            String query = "SELECT SUM(quantity) as total, COUNT(*) as count, " +
                    "AVG(quantity) as average " +
                    "FROM Harvest " +
                    "WHERE MONTH(harvest_date) = MONTH(CURDATE())";

            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            if (rs.next()) {
                double total = rs.getDouble("total");
                int count = rs.getInt("count");
                double average = rs.getDouble("average");

                query = "SELECT p.name, SUM(h.quantity) as total " +
                        "FROM Harvest h " +
                        "JOIN Plant p ON h.plant_id = p.Plant_ID " +
                        "WHERE MONTH(h.harvest_date) = MONTH(CURDATE()) " +
                        "GROUP BY p.name " +
                        "ORDER BY total DESC " +
                        "LIMIT 5";

                stmt.close();
                stmt = conn.prepareStatement(query);
                rs.close();
                rs = stmt.executeQuery();

                StringBuilder topPlants = new StringBuilder();
                while (rs.next()) {
                    topPlants.append("• ").append(rs.getString("name"))
                            .append(": ").append(String.format("%.1f", rs.getDouble("total")))
                            .append(" kg\n");
                }

                String report = String.format(
                        "Harvest Report - %s\n\n" +
                                "Monthly Summary:\n" +
                                "• Total Harvest: %.1f kg\n" +
                                "• Harvest Count: %d records\n" +
                                "• Average per Harvest: %.1f kg\n\n" +
                                "Top Plants This Month:\n%s\n" +
                                "Generated on: %s\n\n" +
                                "This report can be exported to PDF or Excel.",
                        java.time.LocalDate.now().getMonth(),
                        total, count, average,
                        topPlants.toString(),
                        java.time.LocalDate.now()
                );

                // Create gradient panel for report dialog
                JPanel reportPanel = new JPanel(new BorderLayout()) {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2d = (Graphics2D) g;
                        g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                        Color color1 = new Color(245, 255, 245);
                        Color color2 = new Color(235, 250, 235);
                        GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                        g2d.setPaint(gp);
                        g2d.fillRect(0, 0, getWidth(), getHeight());
                    }
                };
                reportPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
                reportPanel.setPreferredSize(new Dimension(400, 300));

                JTextArea reportArea = new JTextArea(report);
                reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
                reportArea.setEditable(false);
                reportArea.setOpaque(false);
                reportArea.setLineWrap(true);
                reportArea.setWrapStyleWord(true);

                JScrollPane scrollPane = new JScrollPane(reportArea);
                scrollPane.setOpaque(false);
                scrollPane.getViewport().setOpaque(false);
                scrollPane.setBorder(BorderFactory.createLineBorder(new Color(121, 85, 72, 100), 1));

                reportPanel.add(scrollPane, BorderLayout.CENTER);

                JOptionPane.showMessageDialog(this, reportPanel, "Yield Report",
                        JOptionPane.INFORMATION_MESSAGE);
            }

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error generating report: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
    }

    private void exportData() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Harvest Data");
        fileChooser.setSelectedFile(new java.io.File("harvest_data.csv"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            try (java.io.PrintWriter writer = new java.io.PrintWriter(file)) {
                for (int i = 0; i < tableModel.getColumnCount(); i++) {
                    writer.print(tableModel.getColumnName(i));
                    if (i < tableModel.getColumnCount() - 1) {
                        writer.print(",");
                    }
                }
                writer.println();

                for (int row = 0; row < tableModel.getRowCount(); row++) {
                    for (int col = 0; col < tableModel.getColumnCount(); col++) {
                        Object value = tableModel.getValueAt(row, col);
                        writer.print(value != null ? value.toString().replace(",", "") : "");
                        if (col < tableModel.getColumnCount() - 1) {
                            writer.print(",");
                        }
                    }
                    writer.println();
                }

                showSuccess("Data exported successfully to: " + file.getAbsolutePath());
            } catch (Exception e) {
                showError("Failed to export data: " + e.getMessage());
            }
        }
    }

    private void filterHarvestData() {
        String filter = (String) filterCombo.getSelectedItem();
        if (filter == null || filter.equals("All")) {
            loadHarvestData();
        } else {
            Connection conn = null;
            PreparedStatement stmt = null;
            ResultSet rs = null;

            try {
                tableModel.setRowCount(0);

                conn = DatabaseConnection.getConnection();
                String query = "SELECT h.harvest_id, p.name as plant_name, g.name as greenhouse_name, " +
                        "h.harvest_date, h.quantity, h.quality " +
                        "FROM Harvest h " +
                        "JOIN Plant p ON h.plant_id = p.Plant_ID " +
                        "JOIN Greenhouse g ON h.greenhouse_id = g.greenhouse_id " +
                        "WHERE ";

                switch (filter) {
                    case "This Month":
                        query += "MONTH(h.harvest_date) = MONTH(CURDATE()) " +
                                "AND YEAR(h.harvest_date) = YEAR(CURDATE()) ";
                        break;
                    case "This Week":
                        query += "h.harvest_date >= DATE_SUB(CURDATE(), INTERVAL 7 DAY) ";
                        break;
                    case "By Plant":
                        // You might want to add a sub-selection for specific plant
                        query += "p.name LIKE '%Tomato%' ";
                        break;
                    case "By Greenhouse":
                        query += "g.greenhouse_id = 1 "; // Example: Main Greenhouse
                        break;
                    case "By Quality":
                        query += "h.quality = 'Excellent' ";
                        break;
                    default:
                        query += "1=1 ";
                }

                query += "ORDER BY h.harvest_date DESC";

                stmt = conn.prepareStatement(query);
                rs = stmt.executeQuery();

                while (rs.next()) {
                    tableModel.addRow(new Object[]{
                            rs.getInt("harvest_id"),
                            rs.getString("plant_name"),
                            rs.getString("greenhouse_name"),
                            rs.getDate("harvest_date"),
                            String.format("%.1f", rs.getDouble("quantity")),
                            "kg",
                            rs.getString("quality")
                    });
                }

                showSuccess("Filtered: " + filter + " (" + tableModel.getRowCount() + " records)");

            } catch (SQLException e) {
                e.printStackTrace();
                showError("Error filtering harvest data: " + e.getMessage());
            } finally {
                try { if (rs != null) rs.close(); } catch (SQLException e) {}
                try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
                DatabaseConnection.closeConnection(conn);
            }
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