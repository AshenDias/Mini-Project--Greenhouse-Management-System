package view;

import model.WorkerModel;
import controller.Plantcontroller;
import controller.DiseaseController;
import controller.InventoryController;
import util.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import javax.swing.Timer;

public class DashboardView extends BaseView {
    private WorkerModel currentWorker;
    private Plantcontroller plantController;
    private DiseaseController diseaseController;
    private InventoryController inventoryController;

    private Timer autoRefreshTimer;

    private JLabel welcomeLabel;
    private JLabel dateLabel;
    private JLabel totalPlantsLabel;
    private JLabel activeDiseasesLabel;
    private JLabel todayHarvestLabel;
    private JLabel workersOnlineLabel;
    private JTable recentTasksTable;
    private JTable alertsTable;

    private JButton plantsButton, diseasesButton, inventoryButton, tasksButton;
    private JButton workersButton, toolsButton, harvestButton, reportsButton;
    private JButton settingsButton, logoutButton;

    private JLabel attendanceStatusLabel;
    private JButton markOwnAttendanceButton;

    private JPanel managerAttendancePanel;
    private JTextField workerIdField;
    private JButton markWorkerAttendanceButton;
    private JComboBox<String> statusCombo;
    private JTextField notesField;

    public DashboardView(WorkerModel worker) {
        super("Greenhouse Management System - Dashboard");
        this.currentWorker = worker;
        this.plantController = new Plantcontroller();
        this.diseaseController = new DiseaseController();
        this.inventoryController = new InventoryController();
    }

    @Override
    protected void initializeUI() {
        setExtendedState(JFrame.MAXIMIZED_BOTH);

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

        createTopBar();
        createMainContent();
        createSidebar();

        add(mainPanel);
        loadDashboardData();
        checkAttendanceStatus();

        startAutoRefresh();
    }

    @Override
    protected void setupEventListeners() {
        logoutButton.addActionListener(e -> logout());
        plantsButton.addActionListener(e -> openPlantView());
        diseasesButton.addActionListener(e -> openDiseaseView());
        inventoryButton.addActionListener(e -> openInventoryView());
        tasksButton.addActionListener(e -> openTaskView());
        workersButton.addActionListener(e -> openWorkerView());
        toolsButton.addActionListener(e -> openToolView());
        harvestButton.addActionListener(e -> openHarvestView());
        reportsButton.addActionListener(e -> openReportView());

        if (markOwnAttendanceButton != null) {
            markOwnAttendanceButton.addActionListener(e -> markOwnAttendance());
        }

        if (markWorkerAttendanceButton != null) {
            markWorkerAttendanceButton.addActionListener(e -> markWorkerAttendance());
        }
    }

    private void startAutoRefresh() {
        autoRefreshTimer = new Timer(60000, e -> {
            refreshDashboardData();

            System.out.println("Dashboard auto-refreshed at: " +
                    LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm:ss")));
        });

        autoRefreshTimer.start();
    }

    private void stopAutoRefresh() {
        if (autoRefreshTimer != null && autoRefreshTimer.isRunning()) {
            autoRefreshTimer.stop();
        }
    }

    private void createTopBar() {
        headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Color color1 = new Color(46, 125, 50); // Dark green
                Color color2 = new Color(76, 175, 80); // Green
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), 0, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setPreferredSize(new Dimension(0, 60));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JPanel leftPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        leftPanel.setOpaque(false);

        JLabel logoLabel = new JLabel("🏠");
        logoLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));

        JLabel titleLabel = new JLabel("Dashboard");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(Color.WHITE);

        leftPanel.add(logoLabel);
        leftPanel.add(Box.createHorizontalStrut(10));
        leftPanel.add(titleLabel);

        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        rightPanel.setOpaque(false);

        String workerName = currentWorker != null ? currentWorker.getName() : "User";
        welcomeLabel = new JLabel("Welcome, " + workerName);
        welcomeLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        welcomeLabel.setForeground(Color.WHITE);

        String role = currentWorker != null && currentWorker.getRole() != null ?
                currentWorker.getRole() : "Worker";
        JLabel roleLabel = new JLabel("(" + role + ")");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleLabel.setForeground(new Color(220, 220, 220));

        attendanceStatusLabel = new JLabel();
        attendanceStatusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        attendanceStatusLabel.setForeground(Color.WHITE);
        attendanceStatusLabel.setVisible(false);

        markOwnAttendanceButton = createStyledButton("Mark My Attendance", new Color(56, 142, 60), Color.WHITE);
        markOwnAttendanceButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        markOwnAttendanceButton.setPreferredSize(new Dimension(160, 35));
        markOwnAttendanceButton.setVisible(false);

        logoutButton = createStyledButton("Logout", new Color(219, 68, 55), Color.WHITE);
        logoutButton.setFont(new Font("Segoe UI", Font.BOLD, 12));
        logoutButton.setPreferredSize(new Dimension(100, 35));

        rightPanel.add(welcomeLabel);
        rightPanel.add(Box.createHorizontalStrut(5));
        rightPanel.add(roleLabel);
        rightPanel.add(Box.createHorizontalStrut(10));
        rightPanel.add(attendanceStatusLabel);
        rightPanel.add(Box.createHorizontalStrut(10));
        rightPanel.add(markOwnAttendanceButton);
        rightPanel.add(Box.createHorizontalStrut(20));
        rightPanel.add(logoutButton);

        headerPanel.add(leftPanel, BorderLayout.WEST);
        headerPanel.add(rightPanel, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
    }

    private void createSidebar() {
        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Color color1 = new Color(230, 245, 230); // Light green
                Color color2 = new Color(210, 235, 210); // Medium light green
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, new Color(180, 225, 180)));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));

        dateLabel = new JLabel(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
        dateLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        dateLabel.setForeground(new Color(46, 125, 50));
        dateLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        dateLabel.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        sidebar.add(dateLabel);
        sidebar.add(Box.createVerticalStrut(10));
        sidebar.add(createSeparator());

        plantsButton = createNavButton("🌱 Manage Plants", new Color(46, 125, 50));
        diseasesButton = createNavButton("🦠 Disease Control", new Color(219, 68, 55));
        inventoryButton = createNavButton("📦 Inventory", new Color(66, 133, 244));
        tasksButton = createNavButton("✅ Task Management", new Color(244, 180, 0));
        workersButton = createNavButton("👷 Worker Management", new Color(171, 71, 188));
        toolsButton = createNavButton("🔧 Tool Monitoring", new Color(0, 150, 136));
        harvestButton = createNavButton("🌾 Harvest Records", new Color(121, 85, 72));
        reportsButton = createNavButton("📊 Reports & Analytics", new Color(158, 158, 158));

        sidebar.add(plantsButton);
        sidebar.add(diseasesButton);
        sidebar.add(inventoryButton);
        sidebar.add(tasksButton);
        sidebar.add(workersButton);
        sidebar.add(toolsButton);
        sidebar.add(harvestButton);
        sidebar.add(reportsButton);

        sidebar.add(Box.createVerticalGlue());

        settingsButton = createNavButton("⚙️ Settings", new Color(96, 96, 96));
        JButton helpButton = createNavButton("❓ Help", new Color(96, 96, 96));

        sidebar.add(createSeparator());
        sidebar.add(settingsButton);
        sidebar.add(helpButton);

        mainPanel.add(sidebar, BorderLayout.WEST);
    }

    private JButton createNavButton(String text, Color color) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);

                if (getModel().isPressed()) {
                    Color color1 = new Color(240, 240, 240);
                    Color color2 = new Color(230, 230, 230);
                    GradientPaint gp = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
                    g2d.setPaint(gp);
                } else if (getModel().isRollover()) {
                    Color color1 = new Color(250, 250, 250);
                    Color color2 = new Color(240, 240, 240);
                    GradientPaint gp = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
                    g2d.setPaint(gp);
                } else {
                    Color color1 = new Color(255, 255, 255);
                    Color color2 = new Color(245, 250, 245);
                    GradientPaint gp = new GradientPaint(0, 0, color1, 0, getHeight(), color2);
                    g2d.setPaint(gp);
                }

                g2d.fillRect(0, 0, getWidth(), getHeight());
                super.paintComponent(g);
            }
        };

        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        button.setForeground(new Color(50, 50, 50));
        button.setContentAreaFilled(false);
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setMaximumSize(new Dimension(220, 45));
        button.setHorizontalAlignment(SwingConstants.LEFT);
        button.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 4, 0, 0, color),
                BorderFactory.createEmptyBorder(12, 16, 12, 20)
        ));

        return button;
    }

    private JSeparator createSeparator() {
        JSeparator separator = new JSeparator() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Color color1 = new Color(180, 225, 180);
                Color color2 = new Color(200, 235, 200);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), 0, color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        separator.setMaximumSize(new Dimension(200, 1));
        return separator;
    }

    private void createMainContent() {
        contentPanel = new JPanel(new BorderLayout());
        contentPanel.setOpaque(false);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel dashboardGrid = new JPanel(new GridBagLayout());
        dashboardGrid.setOpaque(false);
        GridBagConstraints gbc = new GridBagConstraints();

        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1.0;
        gbc.weighty = 0.3;
        gbc.insets = new Insets(0, 0, 20, 0);
        dashboardGrid.add(createStatsPanel(), gbc);

        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.weighty = 0.7;
        gbc.insets = new Insets(0, 0, 0, 10);
        dashboardGrid.add(createAlertsPanel(), gbc);

        gbc.gridx = 1;
        gbc.insets = new Insets(0, 10, 0, 0);
        dashboardGrid.add(createTasksPanel(), gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(20, 0, 0, 0);

        if (isManager()) {
            dashboardGrid.add(createManagerAttendancePanel(), gbc);
        } else {
            dashboardGrid.add(createAttendancePanel(), gbc);
        }

        gbc.gridy = 3;
        gbc.insets = new Insets(20, 0, 0, 0);
        dashboardGrid.add(createSchedulePanel(), gbc);

        contentPanel.add(new JScrollPane(dashboardGrid), BorderLayout.CENTER);
        mainPanel.add(contentPanel, BorderLayout.CENTER);
    }

    private JPanel createStatsPanel() {
        JPanel statsPanel = createCardPanel();
        statsPanel.setLayout(new GridLayout(1, 4, 20, 0));
        statsPanel.setOpaque(false);

        int totalPlants = getTotalPlantsCount();
        int activeDiseases = getActiveDiseasesCount();
        double todayHarvest = getTodayHarvest();
        int workersOnline = getWorkersOnlineCount();

        JPanel plantStat = createStatCard("Total Plants", String.valueOf(totalPlants), "🌱", PRIMARY_COLOR);
        JPanel diseaseStat = createStatCard("Active Diseases", String.valueOf(activeDiseases), "🦠", new Color(219, 68, 55));
        JPanel harvestStat = createStatCard("Today's Harvest", String.format("%.1f kg", todayHarvest), "🌾", new Color(121, 85, 72));
        JPanel workerStat = createStatCard("Workers Online", String.valueOf(workersOnline), "👷", new Color(66, 133, 244));

        statsPanel.add(plantStat);
        statsPanel.add(diseaseStat);
        statsPanel.add(harvestStat);
        statsPanel.add(workerStat);

        return statsPanel;
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
                Color color2 = new Color(245, 255, 245);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // Border gradient
                Graphics2D g2d2 = (Graphics2D) g.create();
                g2d2.setStroke(new BasicStroke(2));
                GradientPaint borderGp = new GradientPaint(0, 0, color.brighter(), 0, getHeight(), color);
                g2d2.setPaint(borderGp);
                g2d2.drawRoundRect(1, 1, getWidth()-2, getHeight()-2, 15, 15);
                g2d2.dispose();
            }
        };

        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        topPanel.setOpaque(false);

        JLabel iconLabel = new JLabel(icon);
        iconLabel.setFont(new Font("Segoe UI", Font.PLAIN, 24));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(Color.DARK_GRAY);

        topPanel.add(iconLabel);
        topPanel.add(Box.createHorizontalStrut(10));
        topPanel.add(titleLabel);

        JLabel valueLabel = new JLabel(value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));

        // Gradient text effect
        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        if (ge.getAllFonts().toString().contains("Segoe UI")) {
            valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 36));
        }

        // Create gradient for text
        valueLabel.setForeground(color);

        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(topPanel, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JPanel createAlertsPanel() {
        JPanel alertsPanel = createCardPanel();
        alertsPanel.setLayout(new BorderLayout());
        alertsPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("⚠️ Alerts & Notifications");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(219, 68, 55));
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        String[] columns = {"Alert", "Priority"};

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        loadAlertsData(model);

        alertsTable = new JTable(model) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (c instanceof JComponent) {
                    ((JComponent) c).setOpaque(false);
                }
                return c;
            }
        };

        alertsTable.setRowHeight(35);
        alertsTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        alertsTable.setOpaque(false);
        alertsTable.setShowGrid(false);
        alertsTable.setIntercellSpacing(new Dimension(0, 0));

        // Set gradient background for table
        alertsTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // Fix: Only call setOpaque if it's a JComponent
                if (c instanceof JComponent) {
                    ((JComponent) c).setOpaque(false);
                }

                // Gradient row background
                JLabel label = (JLabel) c;
                if (!isSelected) {
                    Color color1 = new Color(255, 250, 250); // Light red tint for alerts
                    Color color2 = new Color(255, 245, 245);
                    GradientPaint gp = new GradientPaint(0, 0, color1, table.getWidth(), 0, color2);
                    c.setBackground(new Color(255, 250, 250));
                } else {
                    c.setBackground(new Color(219, 68, 55, 50));
                }

                if (column == 1) {
                    String priority = (String) value;
                    if ("High".equals(priority)) {
                        label.setForeground(new Color(219, 68, 55));
                        label.setFont(label.getFont().deriveFont(Font.BOLD));
                    } else if ("Medium".equals(priority)) {
                        label.setForeground(new Color(244, 180, 0));
                    } else {
                        label.setForeground(new Color(76, 175, 80));
                    }
                } else {
                    label.setForeground(Color.DARK_GRAY);
                }

                return c;
            }
        });

        alertsTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JComponent c = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setOpaque(false);
                c.setBackground(new Color(219, 68, 55));
                c.setForeground(Color.WHITE);
                c.setFont(new Font("Segoe UI", Font.BOLD, 12));
                c.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(alertsTable) {
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

        alertsPanel.add(titleLabel, BorderLayout.NORTH);
        alertsPanel.add(scrollPane, BorderLayout.CENTER);

        return alertsPanel;
    }

    private JPanel createTasksPanel() {
        JPanel tasksPanel = createCardPanel();
        tasksPanel.setLayout(new BorderLayout());
        tasksPanel.setOpaque(false);

        JLabel titleLabel = new JLabel("📋 Today's Tasks");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        String[] columns = {"Task", "Plant", "Assigned To", "Status"};

        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        loadTodayTasksData(model);

        recentTasksTable = new JTable(model) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (c instanceof JComponent) {
                    ((JComponent) c).setOpaque(false);
                }
                return c;
            }
        };

        recentTasksTable.setRowHeight(35);
        recentTasksTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        recentTasksTable.setOpaque(false);
        recentTasksTable.setShowGrid(false);

        recentTasksTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // Fix: Only call setOpaque if it's a JComponent
                if (c instanceof JComponent) {
                    ((JComponent) c).setOpaque(false);
                }

                // Gradient row background
                if (!isSelected) {
                    Color color1 = new Color(250, 255, 250);
                    Color color2 = new Color(245, 250, 245);
                    c.setBackground(new Color(250, 255, 250));
                } else {
                    c.setBackground(new Color(76, 175, 80, 50));
                }

                if (column == 3) {
                    String status = (String) value;
                    if ("Completed".equals(status)) {
                        c.setForeground(new Color(46, 125, 50));
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else if ("In Progress".equals(status)) {
                        c.setForeground(new Color(66, 133, 244));
                    } else {
                        c.setForeground(new Color(244, 180, 0));
                    }
                } else {
                    c.setForeground(Color.DARK_GRAY);
                }

                return c;
            }
        });

        recentTasksTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JComponent c = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setOpaque(false);
                c.setBackground(PRIMARY_COLOR);
                c.setForeground(Color.WHITE);
                c.setFont(new Font("Segoe UI", Font.BOLD, 12));
                c.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(recentTasksTable) {
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
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(76, 175, 80, 100), 1));

        JButton viewAllButton = createStyledButton("View All Tasks", new Color(76, 175, 80), Color.WHITE);
        viewAllButton.setPreferredSize(new Dimension(120, 35));
        viewAllButton.addActionListener(e -> openTaskView());

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setOpaque(false);
        buttonPanel.add(viewAllButton);

        tasksPanel.add(titleLabel, BorderLayout.NORTH);
        tasksPanel.add(scrollPane, BorderLayout.CENTER);
        tasksPanel.add(buttonPanel, BorderLayout.SOUTH);

        return tasksPanel;
    }

    private JPanel createAttendancePanel() {
        JPanel attendancePanel = createCardPanel();
        attendancePanel.setLayout(new BorderLayout());
        attendancePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("📊 Today's Attendance");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JPanel statsPanel = new JPanel(new GridLayout(1, 3, 15, 0));
        statsPanel.setOpaque(false);

        int totalPresent = getTodayPresentCount();
        int totalWorkers = getTotalWorkersCount();
        int absentCount = totalWorkers - totalPresent;

        JPanel presentPanel = createAttendanceStatCard("Present", String.valueOf(totalPresent), "✅", new Color(46, 125, 50));
        JPanel absentPanel = createAttendanceStatCard("Absent", String.valueOf(absentCount), "❌", new Color(219, 68, 55));
        JPanel totalPanel = createAttendanceStatCard("Total Workers", String.valueOf(totalWorkers), "👷", new Color(66, 133, 244));

        statsPanel.add(presentPanel);
        statsPanel.add(absentPanel);
        statsPanel.add(totalPanel);

        String[] columns = {"Worker Name", "Role", "Check-in Time", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        loadTodayAttendanceData(model);

        JTable attendanceTable = new JTable(model) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (c instanceof JComponent) {
                    ((JComponent) c).setOpaque(false);
                }
                return c;
            }
        };

        attendanceTable.setRowHeight(30);
        attendanceTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        attendanceTable.setOpaque(false);
        attendanceTable.setShowGrid(false);

        attendanceTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // Fix: Only call setOpaque if it's a JComponent
                if (c instanceof JComponent) {
                    ((JComponent) c).setOpaque(false);
                }

                // Gradient row background
                if (!isSelected) {
                    Color color1 = new Color(245, 255, 245);
                    Color color2 = new Color(240, 250, 240);
                    c.setBackground(new Color(245, 255, 245));
                } else {
                    c.setBackground(new Color(46, 125, 50, 50));
                }

                if (column == 3) {
                    String status = (String) value;
                    if ("Present".equals(status)) {
                        c.setForeground(new Color(46, 125, 50));
                        c.setFont(c.getFont().deriveFont(Font.BOLD));
                    } else if ("Absent".equals(status)) {
                        c.setForeground(new Color(219, 68, 55));
                    } else if ("Late".equals(status)) {
                        c.setForeground(new Color(244, 180, 0));
                    }
                } else {
                    c.setForeground(Color.DARK_GRAY);
                }

                return c;
            }
        });

        attendanceTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JComponent c = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setOpaque(false);
                c.setBackground(new Color(0, 150, 136));
                c.setForeground(Color.WHITE);
                c.setFont(new Font("Segoe UI", Font.BOLD, 12));
                c.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(attendanceTable) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Color color1 = new Color(245, 255, 245);
                Color color2 = new Color(240, 250, 240);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(0, 150, 136, 100), 1));

        JPanel tablePanel = new JPanel(new BorderLayout());
        tablePanel.setOpaque(false);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        attendancePanel.add(titleLabel, BorderLayout.NORTH);
        attendancePanel.add(statsPanel, BorderLayout.CENTER);
        attendancePanel.add(tablePanel, BorderLayout.SOUTH);

        return attendancePanel;
    }

    private JPanel createManagerAttendancePanel() {
        managerAttendancePanel = createCardPanel();
        managerAttendancePanel.setLayout(new BorderLayout());
        managerAttendancePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("👨‍💼 Manager - Mark Attendance");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(new Color(171, 71, 188)); // Purple for manager
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        JPanel mainPanel = new JPanel(new GridLayout(1, 2, 20, 0));
        mainPanel.setOpaque(false);

        JPanel formPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Card gradient
                Color color1 = Color.WHITE;
                Color color2 = new Color(250, 245, 255);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // Border gradient
                Graphics2D g2d2 = (Graphics2D) g.create();
                g2d2.setStroke(new BasicStroke(1));
                GradientPaint borderGp = new GradientPaint(0, 0, new Color(171, 71, 188).brighter(), 0, getHeight(), new Color(171, 71, 188));
                g2d2.setPaint(borderGp);
                g2d2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                g2d2.dispose();
            }
        };

        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(171, 71, 188), 1),
                "Mark Worker Attendance"
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.weightx = 0.3;

        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel workerIdLabel = new JLabel("Worker ID:");
        workerIdLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(workerIdLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        workerIdField = new JTextField(15);
        workerIdField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        workerIdField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(171, 71, 188, 150), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        formPanel.add(workerIdField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.3;
        JLabel statusLabel = new JLabel("Status:");
        statusLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(statusLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        statusCombo = new JComboBox<>(new String[]{"Present", "Late", "Absent", "Half-day"});
        statusCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        statusCombo.setBackground(Color.WHITE);
        formPanel.add(statusCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.weightx = 0.3;
        JLabel notesLabel = new JLabel("Notes:");
        notesLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
        formPanel.add(notesLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 0.7;
        notesField = new JTextField(15);
        notesField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        notesField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(171, 71, 188, 150), 1),
                BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        formPanel.add(notesField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        markWorkerAttendanceButton = createStyledButton("Mark Attendance", new Color(171, 71, 188), Color.WHITE);
        markWorkerAttendanceButton.setPreferredSize(new Dimension(150, 35));
        formPanel.add(markWorkerAttendanceButton, gbc);

        JPanel statsPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Card gradient
                Color color1 = Color.WHITE;
                Color color2 = new Color(245, 250, 255);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // Border gradient
                Graphics2D g2d2 = (Graphics2D) g.create();
                g2d2.setStroke(new BasicStroke(1));
                GradientPaint borderGp = new GradientPaint(0, 0, new Color(66, 133, 244).brighter(), 0, getHeight(), new Color(66, 133, 244));
                g2d2.setPaint(borderGp);
                g2d2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
                g2d2.dispose();
            }
        };

        statsPanel.setOpaque(false);
        statsPanel.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(66, 133, 244), 1),
                "Today's Summary"
        ));

        JPanel statsGrid = new JPanel(new GridLayout(2, 2, 10, 10));
        statsGrid.setOpaque(false);

        int totalPresent = getTodayPresentCount();
        int totalWorkers = getTotalWorkersCount();
        int absentCount = totalWorkers - totalPresent;
        int lateCount = getLateCount();

        statsGrid.add(createManagerStatCard("Total", String.valueOf(totalWorkers), "📋", new Color(66, 133, 244)));
        statsGrid.add(createManagerStatCard("Present", String.valueOf(totalPresent), "✅", new Color(46, 125, 50)));
        statsGrid.add(createManagerStatCard("Absent", String.valueOf(absentCount), "❌", new Color(219, 68, 55)));
        statsGrid.add(createManagerStatCard("Late", String.valueOf(lateCount), "⏰", new Color(244, 180, 0)));

        statsPanel.add(statsGrid, BorderLayout.CENTER);

        String[] columns = {"ID", "Name", "Time", "Status"};
        DefaultTableModel model = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        loadTodayAttendanceForManager(model);

        JTable recentTable = new JTable(model) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (c instanceof JComponent) {
                    ((JComponent) c).setOpaque(false);
                }
                return c;
            }
        };

        recentTable.setRowHeight(25);
        recentTable.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        recentTable.setOpaque(false);
        recentTable.setShowGrid(false);

        recentTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // Fix: Only call setOpaque if it's a JComponent
                if (c instanceof JComponent) {
                    ((JComponent) c).setOpaque(false);
                }

                // Gradient row background
                if (!isSelected) {
                    Color color1 = new Color(245, 250, 255);
                    Color color2 = new Color(240, 245, 250);
                    c.setBackground(new Color(245, 250, 255));
                } else {
                    c.setBackground(new Color(66, 133, 244, 50));
                }

                if (column == 3) {
                    String status = (String) value;
                    if ("Present".equals(status)) {
                        c.setForeground(new Color(46, 125, 50));
                    } else if ("Late".equals(status)) {
                        c.setForeground(new Color(244, 180, 0));
                    } else {
                        c.setForeground(new Color(219, 68, 55));
                    }
                } else {
                    c.setForeground(Color.DARK_GRAY);
                }

                return c;
            }
        });

        recentTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JComponent c = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setOpaque(false);
                c.setBackground(new Color(96, 96, 96));
                c.setForeground(Color.WHITE);
                c.setFont(new Font("Segoe UI", Font.BOLD, 11));
                c.setBorder(BorderFactory.createEmptyBorder(5, 3, 5, 3));
                return c;
            }
        });

        JScrollPane tableScroll = new JScrollPane(recentTable) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Color color1 = new Color(245, 250, 255);
                Color color2 = new Color(240, 245, 250);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        tableScroll.setOpaque(false);
        tableScroll.getViewport().setOpaque(false);
        tableScroll.setBorder(BorderFactory.createTitledBorder(
                BorderFactory.createLineBorder(new Color(96, 96, 96, 100), 1),
                "Recent Entries"
        ));
        tableScroll.setPreferredSize(new Dimension(300, 120));

        statsPanel.add(tableScroll, BorderLayout.SOUTH);

        mainPanel.add(formPanel);
        mainPanel.add(statsPanel);

        managerAttendancePanel.add(titleLabel, BorderLayout.NORTH);
        managerAttendancePanel.add(mainPanel, BorderLayout.CENTER);

        return managerAttendancePanel;
    }

    private JPanel createManagerStatCard(String title, String value, String icon, Color color) {
        JPanel card = new JPanel(new BorderLayout()) {
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
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                // Border gradient
                Graphics2D g2d2 = (Graphics2D) g.create();
                g2d2.setStroke(new BasicStroke(1));
                GradientPaint borderGp = new GradientPaint(0, 0, color.brighter(), 0, getHeight(), color);
                g2d2.setPaint(borderGp);
                g2d2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2d2.dispose();
            }
        };

        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JLabel valueLabel = new JLabel(icon + " " + value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(color);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        titleLabel.setForeground(Color.DARK_GRAY);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(valueLabel, BorderLayout.CENTER);
        card.add(titleLabel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createAttendanceStatCard(String title, String value, String icon, Color color) {
        JPanel card = new JPanel(new BorderLayout()) {
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
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);

                // Border gradient
                Graphics2D g2d2 = (Graphics2D) g.create();
                g2d2.setStroke(new BasicStroke(1));
                GradientPaint borderGp = new GradientPaint(0, 0, color.brighter(), 0, getHeight(), color);
                g2d2.setPaint(borderGp);
                g2d2.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 10, 10);
                g2d2.dispose();
            }
        };

        card.setOpaque(false);
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel valueLabel = new JLabel(icon + " " + value);
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        valueLabel.setForeground(color);
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        titleLabel.setForeground(Color.DARK_GRAY);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(valueLabel, BorderLayout.CENTER);
        card.add(titleLabel, BorderLayout.SOUTH);

        return card;
    }

    private JPanel createSchedulePanel() {
        JPanel schedulePanel = createCardPanel();
        schedulePanel.setLayout(new BorderLayout());
        schedulePanel.setOpaque(false);

        JLabel titleLabel = new JLabel("📅 Upcoming Schedule");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(PRIMARY_COLOR);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));

        DefaultTableModel model = new DefaultTableModel(new String[]{"Date", "Event", "Location"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        loadScheduleData(model);

        JTable scheduleTable = new JTable(model) {
            @Override
            public Component prepareRenderer(javax.swing.table.TableCellRenderer renderer, int row, int column) {
                Component c = super.prepareRenderer(renderer, row, column);
                if (c instanceof JComponent) {
                    ((JComponent) c).setOpaque(false);
                }
                return c;
            }
        };

        scheduleTable.setRowHeight(35);
        scheduleTable.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        scheduleTable.setOpaque(false);
        scheduleTable.setShowGrid(false);

        scheduleTable.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                // Fix: Only call setOpaque if it's a JComponent
                if (c instanceof JComponent) {
                    ((JComponent) c).setOpaque(false);
                }

                // Gradient row background
                if (!isSelected) {
                    Color color1 = new Color(245, 250, 245);
                    Color color2 = new Color(240, 245, 240);
                    c.setBackground(new Color(245, 250, 245));
                } else {
                    c.setBackground(new Color(121, 85, 72, 50));
                }

                c.setForeground(Color.DARK_GRAY);

                return c;
            }
        });

        scheduleTable.getTableHeader().setDefaultRenderer(new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                JComponent c = (JComponent) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                c.setOpaque(false);
                c.setBackground(new Color(121, 85, 72));
                c.setForeground(Color.WHITE);
                c.setFont(new Font("Segoe UI", Font.BOLD, 12));
                c.setBorder(BorderFactory.createEmptyBorder(8, 5, 8, 5));
                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(scheduleTable) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Color color1 = new Color(245, 250, 245);
                Color color2 = new Color(240, 245, 240);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        scrollPane.setOpaque(false);
        scrollPane.getViewport().setOpaque(false);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(121, 85, 72, 100), 1));

        schedulePanel.add(titleLabel, BorderLayout.NORTH);
        schedulePanel.add(scrollPane, BorderLayout.CENTER);

        return schedulePanel;
    }

    private boolean isManager() {
        return currentWorker != null && "Manager".equalsIgnoreCase(currentWorker.getRole());
    }

    private int getLateCount() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int count = 0;

        try {
            conn = DatabaseConnection.getConnection();
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "Attendance", null);

            if (tables.next()) {
                String query = "SELECT COUNT(*) as late_count FROM Attendance " +
                        "WHERE attendance_date = CURDATE() AND status = 'Late'";
                stmt = conn.prepareStatement(query);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    count = rs.getInt("late_count");
                }
            }
            tables.close();
        } catch (SQLException e) {
            System.err.println("Error getting late count: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
        return count;
    }

    private void loadTodayAttendanceForManager(DefaultTableModel model) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "Attendance", null);

            if (!tables.next()) {
                model.addRow(new Object[]{1, "John Doe", "08:30", "Present"});
                model.addRow(new Object[]{2, "Jane Smith", "09:15", "Late"});
                return;
            }
            tables.close();

            String query = "SELECT a.worker_id, w.name, " +
                    "TIME_FORMAT(a.check_in_time, '%H:%i') as check_time, " +
                    "a.status " +
                    "FROM Attendance a " +
                    "JOIN worker w ON a.worker_id = w.id " +
                    "WHERE a.attendance_date = CURDATE() " +
                    "ORDER BY a.check_in_time DESC " +
                    "LIMIT 4";

            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("worker_id"),
                        rs.getString("name"),
                        rs.getString("check_time"),
                        rs.getString("status")
                });
            }

            if (model.getRowCount() == 0) {
                model.addRow(new Object[]{"-", "No records today", "-", "-"});
            }

        } catch (SQLException e) {
            System.err.println("Error loading manager attendance: " + e.getMessage());
            model.addRow(new Object[]{1, "John Doe", "08:30", "Present"});
            model.addRow(new Object[]{2, "Jane Smith", "09:15", "Late"});
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
    }

    private void checkAttendanceStatus() {
        if (currentWorker == null || currentWorker.getId() == 0) {
            return;
        }

        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            createAttendanceTableIfNotExists(conn);

            String query = "SELECT status, check_in_time FROM Attendance " +
                    "WHERE worker_id = ? AND attendance_date = CURDATE()";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, currentWorker.getId());
            rs = stmt.executeQuery();

            if (rs.next()) {
                String status = rs.getString("status");
                String checkInTime = rs.getString("check_in_time");

                attendanceStatusLabel.setText("My Attendance: " + status + " (" + checkInTime + ")");
                attendanceStatusLabel.setVisible(true);

                if ("Present".equals(status) || "Late".equals(status)) {
                    markOwnAttendanceButton.setText("Checked In");
                    markOwnAttendanceButton.setEnabled(false);
                    markOwnAttendanceButton.setBackground(new Color(158, 158, 158));
                } else {
                    markOwnAttendanceButton.setVisible(true);
                    markOwnAttendanceButton.setEnabled(true);
                }
            } else {
                attendanceStatusLabel.setText("My Attendance: Not Marked");
                attendanceStatusLabel.setForeground(new Color(244, 180, 0));
                attendanceStatusLabel.setVisible(true);
                markOwnAttendanceButton.setVisible(true);
                markOwnAttendanceButton.setEnabled(true);
                markOwnAttendanceButton.setText("Mark My Attendance");
            }

        } catch (SQLException e) {
            System.err.println("Error checking attendance status: " + e.getMessage());
            attendanceStatusLabel.setText("Attendance: Error");
            attendanceStatusLabel.setVisible(true);
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
    }

    private void createAttendanceTableIfNotExists(Connection conn) {
        PreparedStatement stmt = null;
        try {
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "Attendance", null);

            if (!tables.next()) {
                String createTableSQL = "CREATE TABLE IF NOT EXISTS Attendance (" +
                        "attendance_id INT AUTO_INCREMENT PRIMARY KEY, " +
                        "worker_id INT NOT NULL, " +
                        "attendance_date DATE NOT NULL, " +
                        "check_in_time TIME, " +
                        "check_out_time TIME, " +
                        "status VARCHAR(20) NOT NULL, " +
                        "notes TEXT, " +
                        "marked_by INT, " +
                        "marked_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                        "FOREIGN KEY (worker_id) REFERENCES worker(id) ON DELETE CASCADE, " +
                        "UNIQUE KEY unique_attendance (worker_id, attendance_date)" +
                        ")";

                stmt = conn.prepareStatement(createTableSQL);
                stmt.executeUpdate();
                System.out.println("Attendance table created successfully");
            }
            tables.close();
        } catch (SQLException e) {
            System.err.println("Error creating attendance table: " + e.getMessage());
        } finally {
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
        }
    }

    private void markOwnAttendance() {
        if (currentWorker == null || currentWorker.getId() == 0) {
            JOptionPane.showMessageDialog(this,
                    "Cannot mark attendance: Worker not identified",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConnection.getConnection();
            String checkQuery = "SELECT 1 FROM Attendance WHERE worker_id = ? AND attendance_date = CURDATE()";
            stmt = conn.prepareStatement(checkQuery);
            stmt.setInt(1, currentWorker.getId());
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                JOptionPane.showMessageDialog(this,
                        "Attendance already marked for today!",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }
            rs.close();
            stmt.close();
            LocalTime currentTime = LocalTime.now();
            String status = currentTime.isAfter(LocalTime.of(9, 0)) ? "Late" : "Present";

            String insertQuery = "INSERT INTO Attendance (worker_id, attendance_date, check_in_time, status, marked_by) " +
                    "VALUES (?, CURDATE(), ?, ?, ?)";
            stmt = conn.prepareStatement(insertQuery);
            stmt.setInt(1, currentWorker.getId());
            stmt.setTime(2, Time.valueOf(currentTime));
            stmt.setString(3, status);
            stmt.setInt(4, currentWorker.getId());

            int rowsAffected = stmt.executeUpdate();

            if (rowsAffected > 0) {
                JOptionPane.showMessageDialog(this,
                        "Attendance marked successfully! Status: " + status,
                        "Success",
                        JOptionPane.INFORMATION_MESSAGE);
                attendanceStatusLabel.setText("My Attendance: " + status + " (" + currentTime.format(DateTimeFormatter.ofPattern("hh:mm a")) + ")");
                attendanceStatusLabel.setForeground(status.equals("Late") ? new Color(244, 180, 0) : new Color(46, 125, 50));
                markOwnAttendanceButton.setText("Checked In");
                markOwnAttendanceButton.setEnabled(false);

                refreshDashboardData();
            } else {
                JOptionPane.showMessageDialog(this,
                        "Failed to mark attendance",
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            }

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error marking attendance: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
    }

    private void markWorkerAttendance() {
        if (!isManager()) {
            JOptionPane.showMessageDialog(this,
                    "Only managers can mark attendance for other workers",
                    "Access Denied",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String workerIdText = workerIdField.getText().trim();
        if (workerIdText.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Please enter Worker ID",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        int workerId;
        try {
            workerId = Integer.parseInt(workerIdText);
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Invalid Worker ID. Please enter a number",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String status = (String) statusCombo.getSelectedItem();
        String notes = notesField.getText().trim();

        if (!workerExists(workerId)) {
            JOptionPane.showMessageDialog(this,
                    "Worker with ID " + workerId + " does not exist",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        Connection conn = null;
        PreparedStatement stmt = null;

        try {
            conn = DatabaseConnection.getConnection();

            String checkQuery = "SELECT 1 FROM Attendance WHERE worker_id = ? AND attendance_date = CURDATE()";
            stmt = conn.prepareStatement(checkQuery);
            stmt.setInt(1, workerId);
            ResultSet rs = stmt.executeQuery();

            if (rs.next()) {
                int option = JOptionPane.showConfirmDialog(this,
                        "Attendance already marked for this worker today. Update?",
                        "Confirm Update",
                        JOptionPane.YES_NO_OPTION);

                if (option == JOptionPane.YES_OPTION) {
                    rs.close();
                    stmt.close();

                    String updateQuery = "UPDATE Attendance SET status = ?, notes = ?, marked_by = ? " +
                            "WHERE worker_id = ? AND attendance_date = CURDATE()";
                    stmt = conn.prepareStatement(updateQuery);
                    stmt.setString(1, status);
                    stmt.setString(2, notes.isEmpty() ? null : notes);
                    stmt.setInt(3, currentWorker.getId());
                    stmt.setInt(4, workerId);

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(this,
                                "Attendance updated successfully!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                } else {
                    return;
                }
            } else {
                rs.close();
                stmt.close();

                LocalTime currentTime = LocalTime.now();
                String insertQuery = "INSERT INTO Attendance (worker_id, attendance_date, check_in_time, status, notes, marked_by) " +
                        "VALUES (?, CURDATE(), ?, ?, ?, ?)";
                stmt = conn.prepareStatement(insertQuery);
                stmt.setInt(1, workerId);
                stmt.setTime(2, "Absent".equals(status) ? null : Time.valueOf(currentTime));
                stmt.setString(3, status);
                stmt.setString(4, notes.isEmpty() ? null : notes);
                stmt.setInt(5, currentWorker.getId());

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    JOptionPane.showMessageDialog(this,
                            "Attendance marked successfully for worker ID: " + workerId,
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                }
            }

            workerIdField.setText("");
            notesField.setText("");

            refreshDashboardData();

        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                    "Error marking attendance: " + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        } finally {
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
    }

    private boolean workerExists(int workerId) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        boolean exists = false;

        try {
            conn = DatabaseConnection.getConnection();
            String query = "SELECT 1 FROM worker WHERE id = ?";
            stmt = conn.prepareStatement(query);
            stmt.setInt(1, workerId);
            rs = stmt.executeQuery();
            exists = rs.next();
        } catch (SQLException e) {
            System.err.println("Error checking worker existence: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
        return exists;
    }

    private int getTodayPresentCount() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int count = 0;

        try {
            conn = DatabaseConnection.getConnection();

            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "Attendance", null);

            if (tables.next()) {
                String query = "SELECT COUNT(DISTINCT worker_id) as present_count " +
                        "FROM Attendance " +
                        "WHERE attendance_date = CURDATE() AND status IN ('Present', 'Late', 'Half-day')";
                stmt = conn.prepareStatement(query);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    count = rs.getInt("present_count");
                }
            } else {
                count = 0;
            }
            tables.close();

        } catch (SQLException e) {
            System.err.println("Error getting today's present count: " + e.getMessage());
            count = 0;
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
        return count;
    }

    private int getTotalWorkersCount() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int count = 0;

        try {
            conn = DatabaseConnection.getConnection();
            String query = "SELECT COUNT(*) as total FROM worker";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            if (rs.next()) {
                count = rs.getInt("total");
            }
        } catch (SQLException e) {
            System.err.println("Error getting total workers count: " + e.getMessage());
            count = 0;
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
        return count;
    }

    private void loadTodayAttendanceData(DefaultTableModel model) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "Attendance", null);

            if (!tables.next()) {
                model.addRow(new Object[]{"John Doe", "Manager", "08:30 AM", "Present"});
                model.addRow(new Object[]{"Jane Smith", "Supervisor", "09:15 AM", "Late"});
                model.addRow(new Object[]{"Bob Wilson", "Technician", "08:45 AM", "Present"});
                return;
            }
            tables.close();

            String query = "SELECT w.name, w.role, " +
                    "TIME_FORMAT(a.check_in_time, '%h:%i %p') as check_in, " +
                    "a.status " +
                    "FROM Attendance a " +
                    "JOIN worker w ON a.worker_id = w.id " +
                    "WHERE a.attendance_date = CURDATE() " +
                    "ORDER BY a.check_in_time DESC " +
                    "LIMIT 5";

            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("name"),
                        rs.getString("role"),
                        rs.getString("check_in"),
                        rs.getString("status")
                });
            }

            if (model.getRowCount() == 0) {
                model.addRow(new Object[]{"No attendance records for today", "", "", ""});
            }

        } catch (SQLException e) {
            System.err.println("Error loading attendance data: " + e.getMessage());
            model.addRow(new Object[]{"John Doe", "Manager", "08:30 AM", "Present"});
            model.addRow(new Object[]{"Jane Smith", "Supervisor", "09:15 AM", "Late"});
            model.addRow(new Object[]{"Bob Wilson", "Technician", "08:45 AM", "Present"});
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
    }

    public void refreshDashboardData() {
        // Update date and time labels
        dateLabel.setText(LocalDate.now().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));

        try {
            welcomeLabel.setText("Welcome, " + currentWorker.getName() + " - " +
                    LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
        } catch (Exception e) {
            welcomeLabel.setText("Welcome - " + LocalTime.now().format(DateTimeFormatter.ofPattern("hh:mm a")));
        }

        SwingUtilities.invokeLater(() -> {
            try {
                JPanel dashboardGrid = (JPanel) ((JScrollPane) contentPanel.getComponent(0)).getViewport().getView();
                Component[] components = dashboardGrid.getComponents();

                for (Component comp : components) {
                    if (comp instanceof JPanel) {
                        JPanel panel = (JPanel) comp;
                        if (panel.getComponentCount() == 4) {
                            dashboardGrid.remove(panel);
                            dashboardGrid.add(createStatsPanel(), 0);
                            dashboardGrid.revalidate();
                            dashboardGrid.repaint();
                            break;
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Error refreshing dashboard: " + e.getMessage());
            }
        });
    }

    private int getTotalPlantsCount() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int count = 0;

        try {
            conn = DatabaseConnection.getConnection();
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "Plant", null);

            if (tables.next()) {
                String query = "SELECT COUNT(*) as total FROM Plant WHERE active = 1";
                stmt = conn.prepareStatement(query);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    count = rs.getInt("total");
                }
            } else {
                count = 0;
            }
            tables.close();

        } catch (SQLException e) {
            System.err.println("Error loading plant count: " + e.getMessage());
            count = 0;
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
        return count;
    }

    private int getActiveDiseasesCount() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int count = 0;

        try {
            conn = DatabaseConnection.getConnection();
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "Disease", null);

            if (tables.next()) {
                String query = "SELECT COUNT(*) as total FROM Disease WHERE severity IN ('High', 'Critical')";
                stmt = conn.prepareStatement(query);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    count = rs.getInt("total");
                }
            } else {
                count = 0;
            }
            tables.close();

        } catch (SQLException e) {
            System.err.println("Error loading disease count: " + e.getMessage());
            count = 0;
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
        return count;
    }

    private double getTodayHarvest() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        double total = 0;

        try {
            conn = DatabaseConnection.getConnection();
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "Harvest", null);

            if (tables.next()) {
                String query = "SELECT COALESCE(SUM(quantity), 0) as total FROM Harvest WHERE harvest_date = CURDATE()";
                stmt = conn.prepareStatement(query);
                rs = stmt.executeQuery();

                if (rs.next()) {
                    total = rs.getDouble("total");
                }
            } else {
                total = 0.0;
            }
            tables.close();

        } catch (SQLException e) {
            System.err.println("Error loading harvest data: " + e.getMessage());
            total = 0.0;
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
        return total;
    }

    private int getWorkersOnlineCount() {
        return getTodayPresentCount();
    }

    private void loadAlertsData(DefaultTableModel model) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            DatabaseMetaData meta = conn.getMetaData();
            boolean hasInventory = false;
            boolean hasDisease = false;
            boolean hasTask = false;

            ResultSet tables = meta.getTables(null, null, "Inventory", null);
            if (tables.next()) hasInventory = true;
            tables.close();

            tables = meta.getTables(null, null, "Disease", null);
            if (tables.next()) hasDisease = true;
            tables.close();

            tables = meta.getTables(null, null, "Task", null);
            if (tables.next()) hasTask = true;
            tables.close();

            StringBuilder query = new StringBuilder();

            if (hasInventory) {
                query.append("SELECT CONCAT('Low stock: ', Description) as alert, 'High' as priority ")
                        .append("FROM Inventory WHERE stock < 10 ");
            }

            if (hasDisease) {
                if (query.length() > 0) query.append("UNION ");
                query.append("SELECT CONCAT('Disease detected: ', d.name, ' on ', COALESCE(p.name, 'Unknown Plant')) as alert, ")
                        .append("CASE WHEN d.severity IN ('High', 'Critical') THEN 'High' ELSE 'Medium' END as priority ")
                        .append("FROM Disease d ")
                        .append("LEFT JOIN Plant p ON d.affected_plant_id = p.Plant_ID ");
            }

            if (hasTask) {
                if (query.length() > 0) query.append("UNION ");
                query.append("SELECT CONCAT('Task overdue: ', task_name) as alert, 'Medium' as priority ")
                        .append("FROM Task WHERE due_date < CURDATE() AND status != 'Completed' ");
            }

            if (query.length() == 0) {
                model.addRow(new Object[]{"No alerts available", "Low"});
                return;
            }

            query.append("LIMIT 5");

            stmt = conn.prepareStatement(query.toString());
            rs = stmt.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("alert"),
                        rs.getString("priority")
                });
            }

            if (model.getRowCount() == 0) {
                model.addRow(new Object[]{"No alerts at this time", "Low"});
            }

        } catch (SQLException e) {
            System.err.println("Error loading alerts: " + e.getMessage());
            model.addRow(new Object[]{"No alerts available", "Low"});
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
    }

    private void loadTodayTasksData(DefaultTableModel model) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "Task", null);

            if (!tables.next()) {
                model.addRow(new Object[]{"Water all plants", "Main Greenhouse", "Ashen Dias", "Pending"});
                model.addRow(new Object[]{"Check soil pH", "Tropical Greenhouse", "Udantha", "In Progress"});
                model.addRow(new Object[]{"Harvest tomatoes", "Vegetable Section", "Chamod", "Completed"});
                return;
            }
            tables.close();

            tables = meta.getTables(null, null, "Plant", null);
            boolean hasPlant = tables.next();
            tables.close();

            tables = meta.getTables(null, null, "Worker", null);
            boolean hasWorker = tables.next();
            tables.close();

            StringBuilder query = new StringBuilder();

            if (hasPlant && hasWorker) {
                query.append("SELECT t.task_name, COALESCE(p.name, 'General') as plant_name, ")
                        .append("COALESCE(w.name, 'Unassigned') as worker_name, ")
                        .append("COALESCE(t.status, 'Pending') as status ")
                        .append("FROM Task t ")
                        .append("LEFT JOIN Plant p ON t.plant_id = p.Plant_ID ")
                        .append("LEFT JOIN Worker w ON t.assigned_to = w.worker_id ")
                        .append("WHERE t.due_date = CURDATE() ")
                        .append("ORDER BY t.status, t.task_id ")
                        .append("LIMIT 5");
            } else if (hasPlant) {
                query.append("SELECT t.task_name, COALESCE(p.name, 'General') as plant_name, ")
                        .append("'Unassigned' as worker_name, ")
                        .append("COALESCE(t.status, 'Pending') as status ")
                        .append("FROM Task t ")
                        .append("LEFT JOIN Plant p ON t.plant_id = p.Plant_ID ")
                        .append("WHERE t.due_date = CURDATE() ")
                        .append("ORDER BY t.status, t.task_id ")
                        .append("LIMIT 5");
            } else if (hasWorker) {
                query.append("SELECT t.task_name, 'General' as plant_name, ")
                        .append("COALESCE(w.name, 'Unassigned') as worker_name, ")
                        .append("COALESCE(t.status, 'Pending') as status ")
                        .append("FROM Task t ")
                        .append("LEFT JOIN Worker w ON t.assigned_to = w.worker_id ")
                        .append("WHERE t.due_date = CURDATE() ")
                        .append("ORDER BY t.status, t.task_id ")
                        .append("LIMIT 5");
            } else {
                query.append("SELECT task_name, 'General' as plant_name, ")
                        .append("'Unassigned' as worker_name, ")
                        .append("COALESCE(status, 'Pending') as status ")
                        .append("FROM Task ")
                        .append("WHERE due_date = CURDATE() ")
                        .append("ORDER BY status, task_id ")
                        .append("LIMIT 5");
            }

            stmt = conn.prepareStatement(query.toString());
            rs = stmt.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("task_name"),
                        rs.getString("plant_name"),
                        rs.getString("worker_name"),
                        rs.getString("status")
                });
            }

            if (model.getRowCount() == 0) {
                model.addRow(new Object[]{"Water all plants", "Main Greenhouse", "Ashen Dias", "Pending"});
                model.addRow(new Object[]{"Check soil pH", "Tropical Greenhouse", "Udantha", "In Progress"});
                model.addRow(new Object[]{"Harvest tomatoes", "Vegetable Section", "Chamod", "Completed"});
            }

        } catch (SQLException e) {
            System.err.println("Error loading tasks: " + e.getMessage());
            model.addRow(new Object[]{"Water all plants", "Main Greenhouse", "Ashen Dias", "Pending"});
            model.addRow(new Object[]{"Check soil pH", "Tropical Greenhouse", "Udantha", "In Progress"});
            model.addRow(new Object[]{"Harvest tomatoes", "Vegetable Section", "Chamod", "Completed"});
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
    }

    private void loadScheduleData(DefaultTableModel model) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            DatabaseMetaData meta = conn.getMetaData();
            ResultSet tables = meta.getTables(null, null, "Task", null);

            if (!tables.next()) {
                model.addRow(new Object[]{"Today", "Morning watering", "All greenhouses"});
                model.addRow(new Object[]{"Tomorrow", "Pest control", "Tropical section"});
                model.addRow(new Object[]{"Day after", "Equipment check", "Tool shed"});
                model.addRow(new Object[]{"This Friday", "Weekly meeting", "Main office"});
                model.addRow(new Object[]{"Next Monday", "Monthly report", "Admin office"});
                return;
            }
            tables.close();

            String query = "SELECT DATE_FORMAT(due_date, '%Y-%m-%d') as event_date, " +
                    "task_name as event, " +
                    "COALESCE((SELECT name FROM Greenhouse WHERE greenhouse_id = " +
                    "(SELECT greenhouse_id FROM Plant WHERE Plant_ID = t.plant_id)), 'General') as location " +
                    "FROM Task t " +
                    "WHERE due_date >= CURDATE() AND due_date <= DATE_ADD(CURDATE(), INTERVAL 7 DAY) " +
                    "ORDER BY due_date " +
                    "LIMIT 5";

            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("event_date"),
                        rs.getString("event"),
                        rs.getString("location")
                });
            }

            if (model.getRowCount() == 0) {
                model.addRow(new Object[]{"Today", "Morning watering", "All greenhouses"});
                model.addRow(new Object[]{"Tomorrow", "Pest control", "Tropical section"});
                model.addRow(new Object[]{"Day after", "Equipment check", "Tool shed"});
                model.addRow(new Object[]{"This Friday", "Weekly meeting", "Main office"});
                model.addRow(new Object[]{"Next Monday", "Monthly report", "Admin office"});
            }

        } catch (SQLException e) {
            System.err.println("Error loading schedule: " + e.getMessage());
            model.addRow(new Object[]{"Today", "Morning watering", "All greenhouses"});
            model.addRow(new Object[]{"Tomorrow", "Pest control", "Tropical section"});
            model.addRow(new Object[]{"Day after", "Equipment check", "Tool shed"});
            model.addRow(new Object[]{"This Friday", "Weekly meeting", "Main office"});
            model.addRow(new Object[]{"Next Monday", "Monthly report", "Admin office"});
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
    }

    private void loadDashboardData() {
        refreshDashboardData();
    }

    private void logout() {
        stopAutoRefresh();
        if (JOptionPane.showConfirmDialog(this,
                "Are you sure you want to logout?",
                "Confirm Logout",
                JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
            LoginView loginView = new LoginView();
            loginView.setVisible(true);
            this.dispose();
        }
    }

    private void openPlantView() {
        try {
            stopAutoRefresh();
            Plantview plantView = new Plantview(this);
            plantView.setVisible(true);
            this.setVisible(false);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to open Plant View: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openDiseaseView() {
        try {
            stopAutoRefresh();
            DiseaseView diseaseView = new DiseaseView(this);
            diseaseView.setVisible(true);
            this.setVisible(false);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to open Disease View: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openInventoryView() {
        try {
            stopAutoRefresh();
            Inventoryview inventoryView = new Inventoryview(this);
            inventoryView.setVisible(true);
            this.setVisible(false);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to open Inventory View: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openTaskView() {
        try {
            stopAutoRefresh();
            TaskView taskView = new TaskView(this);
            taskView.setVisible(true);
            this.setVisible(false);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to open Task View: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openWorkerView() {
        try {
            stopAutoRefresh();
            WorkerView workerView = new WorkerView(this);
            workerView.setVisible(true);
            this.setVisible(false);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to open Worker View: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openToolView() {
        try {
            stopAutoRefresh();
            ToolView toolView = new ToolView(this);
            toolView.setVisible(true);
            this.setVisible(false);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to open Tool View: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openHarvestView() {
        try {
            stopAutoRefresh();
            HarvestView harvestView = new HarvestView(this);
            harvestView.setVisible(true);
            this.setVisible(false);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to open Harvest View: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private void openReportView() {
        try {
            stopAutoRefresh();
            ReportView reportView = new ReportView(this);
            reportView.setVisible(true);
            this.setVisible(false);
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Failed to open Report View: " + e.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    @Override
    public void dispose() {
        stopAutoRefresh();
        super.dispose();
    }
}