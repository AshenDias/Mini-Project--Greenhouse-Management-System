package view;

import util.DatabaseConnection;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.awt.event.ActionEvent;

public class TaskView extends BaseView {
    private JTable taskTable;
    private DefaultTableModel tableModel;
    private JButton addButton, completeButton, calendarButton, backButton;
    private DashboardView dashboardView;
    private java.time.YearMonth currentYearMonth;
    private JLabel calendarTitle;
    private JPanel calendarGrid;
    private JPanel calendarPanel;

    public TaskView(DashboardView dashboardView) {
        super("Task Management");
        this.dashboardView = dashboardView;
        this.currentYearMonth = java.time.YearMonth.now();
    }

    @Override
    protected void initializeUI() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);

        createHeader();
        createToolbar();
        createTable();
        createCalendarPanel();

        add(mainPanel);
        loadTaskData();
        setupEventListeners();
    }

    @Override
    protected void setupEventListeners() {
        addButton.addActionListener(e -> addTask());
        completeButton.addActionListener(e -> markComplete());
        calendarButton.addActionListener(e -> showCalendar());
        backButton.addActionListener(e -> goBackToDashboard());

        taskTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                completeButton.setEnabled(taskTable.getSelectedRow() >= 0);
            }
        });
        completeButton.setEnabled(false);
    }

    private void createHeader() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JLabel titleLabel = new JLabel("✅ Task Management");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        backButton = createStyledButton("← Back to Dashboard", Color.lightGray, new Color(0, 0, 0));
        backButton.setPreferredSize(new Dimension(180, 35));

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(backButton, BorderLayout.EAST);
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

    private void createToolbar() {
        JPanel toolbar = new JPanel(new BorderLayout());
        toolbar.setBackground(Color.lightGray);
        toolbar.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        filterPanel.setBackground(Color.lightGray);

        String[] filters = {"All", "Today", "This Week", "Overdue", "By Worker", "By Greenhouse"};
        for (String filter : filters) {
            JButton btn = new JButton(filter);
            btn.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            btn.addActionListener(e -> filterTasks(filter));
            filterPanel.add(btn);
        }

        JPanel actionPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        actionPanel.setBackground(Color.lightGray);

        calendarButton = createStyledButton("📅 Calendar View", SECONDARY_COLOR, Color.black);
        calendarButton.setPreferredSize(new Dimension(150, 30));

        addButton = createStyledButton("➕ New Task", new Color(244, 180, 0), Color.black);
        addButton.setPreferredSize(new Dimension(120, 30));

        completeButton = createStyledButton("✓ Mark Complete", new Color(46, 125, 50), Color.black);
        completeButton.setPreferredSize(new Dimension(140, 30));

        actionPanel.add(calendarButton);
        actionPanel.add(addButton);
        actionPanel.add(completeButton);

        toolbar.add(filterPanel, BorderLayout.WEST);
        toolbar.add(actionPanel, BorderLayout.EAST);

        JPanel topContainer = new JPanel(new BorderLayout());
        topContainer.add(headerPanel, BorderLayout.NORTH);
        topContainer.add(toolbar, BorderLayout.SOUTH);

        mainPanel.add(topContainer, BorderLayout.NORTH);
    }

    private void createTable() {
        String[] columns = {"ID", "Task", "Plant", "Assigned To", "Due Date", "Status"};
        tableModel = new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };

        taskTable = new JTable(tableModel);
        taskTable.setRowHeight(35);
        taskTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        taskTable.getTableHeader().setFont(new Font("Segoe UI", Font.BOLD, 12));

        taskTable.setDefaultRenderer(Object.class, new javax.swing.table.DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus,
                                                           int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);

                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Color.WHITE : new Color(248, 248, 248));
                }

                if (column == 5) {
                    String status = (String) value;
                    if (status != null) {
                        if ("Overdue".equals(status)) {
                            c.setBackground(new Color(255, 235, 238));
                            c.setForeground(new Color(219, 68, 55));
                            c.setFont(c.getFont().deriveFont(Font.BOLD));
                        } else if ("Completed".equals(status)) {
                            c.setBackground(new Color(232, 245, 233));
                            c.setForeground(new Color(46, 125, 50));
                        } else if ("In Progress".equals(status)) {
                            c.setBackground(new Color(232, 244, 253));
                            c.setForeground(new Color(66, 133, 244));
                        }
                    }
                }

                return c;
            }
        });

        JScrollPane scrollPane = new JScrollPane(taskTable);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        mainPanel.add(scrollPane, BorderLayout.CENTER);
    }

    private void createCalendarPanel() {
        calendarPanel = new JPanel(new BorderLayout());
        calendarPanel.setPreferredSize(new Dimension(300, 0));
        calendarPanel.setBackground(Color.WHITE);
        calendarPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        currentYearMonth = java.time.YearMonth.now();

        calendarTitle = new JLabel(currentYearMonth.getMonth().toString() + " " + currentYearMonth.getYear());
        calendarTitle.setFont(new Font("Segoe UI", Font.BOLD, 16));
        calendarTitle.setBorder(BorderFactory.createEmptyBorder(0, 0, 15, 0));
        calendarGrid = new JPanel();
        calendarGrid.setBackground(Color.WHITE);
        updateCalendarDisplay();
        JPanel navPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        navPanel.setBackground(Color.WHITE);

        JButton prevButton = new JButton("← Prev");
        JButton nextButton = new JButton("Next →");

        prevButton.addActionListener(e -> {
            currentYearMonth = currentYearMonth.minusMonths(1);
            updateCalendarDisplay();
        });

        nextButton.addActionListener(e -> {
            currentYearMonth = currentYearMonth.plusMonths(1);
            updateCalendarDisplay();
        });

        navPanel.add(prevButton);
        navPanel.add(nextButton);

        JPanel calendarContainer = new JPanel(new BorderLayout());
        calendarContainer.setBackground(Color.lightGray);
        calendarContainer.add(calendarTitle, BorderLayout.NORTH);
        calendarContainer.add(calendarGrid, BorderLayout.CENTER);
        calendarContainer.add(navPanel, BorderLayout.SOUTH);

        calendarPanel.add(calendarContainer, BorderLayout.CENTER);

        mainPanel.add(calendarPanel, BorderLayout.EAST);
    }

    private void updateCalendarDisplay() {
        calendarTitle.setText(currentYearMonth.getMonth().toString() + " " + currentYearMonth.getYear());

        calendarGrid.removeAll();
        calendarGrid.setLayout(new GridLayout(6, 7, 5, 5));

        java.time.LocalDate currentDate = java.time.LocalDate.now();

        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

        for (String day : days) {
            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
            dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 12));
            dayLabel.setForeground(new Color(100, 100, 100));
            calendarGrid.add(dayLabel);
        }

        java.time.LocalDate firstDayOfMonth = currentYearMonth.atDay(1);
        int firstDayOfWeek = firstDayOfMonth.getDayOfWeek().getValue() % 7;

        for (int i = 0; i < firstDayOfWeek; i++) {
            calendarGrid.add(new JLabel(""));
        }

        int daysInMonth = currentYearMonth.lengthOfMonth();

        for (int day = 1; day <= daysInMonth; day++) {
            final int currentDay = day;
            JLabel dayNum = new JLabel(String.valueOf(day), SwingConstants.CENTER);
            dayNum.setFont(new Font("Segoe UI", Font.PLAIN, 12));
            dayNum.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));

            if (day == currentDate.getDayOfMonth() &&
                    currentYearMonth.getYear() == currentDate.getYear() &&
                    currentYearMonth.getMonthValue() == currentDate.getMonthValue()) {
                dayNum.setBackground(new Color(46, 125, 50));
                dayNum.setOpaque(true);
                dayNum.setForeground(Color.WHITE);
                dayNum.setFont(dayNum.getFont().deriveFont(Font.BOLD));
            }

            java.time.LocalDate checkDate = java.time.LocalDate.of(
                    currentYearMonth.getYear(),
                    currentYearMonth.getMonth(),
                    currentDay
            );
            int taskCount = getTaskCountForDate(checkDate);
            if (taskCount > 0) {
                dayNum.setToolTipText(taskCount + " tasks on " + checkDate);
                dayNum.setBorder(BorderFactory.createLineBorder(new Color(244, 180, 0), 2));
            }

            dayNum.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    java.time.LocalDate selectedDate = java.time.LocalDate.of(
                            currentYearMonth.getYear(),
                            currentYearMonth.getMonth(),
                            currentDay
                    );

                    showTasksForDate(selectedDate);
                }

                public void mouseEntered(java.awt.event.MouseEvent evt) {
                    dayNum.setBackground(new Color(199, 199, 199));
                    dayNum.setOpaque(true);
                }

                public void mouseExited(java.awt.event.MouseEvent evt) {
                    if (!(currentDay == currentDate.getDayOfMonth() &&
                            currentYearMonth.getYear() == currentDate.getYear() &&
                            currentYearMonth.getMonthValue() == currentDate.getMonthValue())) {
                        dayNum.setBackground(Color.WHITE);
                        dayNum.setOpaque(false);
                    }
                }
            });

            calendarGrid.add(dayNum);
        }

        int totalCells = 42;
        int usedCells = firstDayOfWeek + daysInMonth;
        for (int i = usedCells; i < totalCells; i++) {
            calendarGrid.add(new JLabel(""));
        }
        calendarGrid.revalidate();
        calendarGrid.repaint();
    }

    private int getTaskCountForDate(java.time.LocalDate date) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int count = 0;

        try {
            conn = DatabaseConnection.getConnection();
            String query = "SELECT COUNT(*) as count FROM Task WHERE due_date = ? AND status != 'Completed'";
            stmt = conn.prepareStatement(query);
            stmt.setDate(1, java.sql.Date.valueOf(date));
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

    private void loadTaskData() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            tableModel.setRowCount(0);

            conn = DatabaseConnection.getConnection();
            String query = "SELECT t.task_id, t.task_name, p.name as plant_name, " +
                    "w.name as worker_name, t.due_date, t.status " +
                    "FROM Task t " +
                    "LEFT JOIN Plant p ON t.plant_id = p.Plant_ID " +
                    "LEFT JOIN worker w ON (SELECT w2.id FROM worker w2 WHERE w2.id = 1) " + // Placeholder for assigned worker
                    "ORDER BY " +
                    "CASE WHEN t.status = 'Overdue' THEN 1 " +
                    "WHEN t.status = 'In Progress' THEN 2 " +
                    "WHEN t.status = 'Pending' THEN 3 " +
                    "ELSE 4 END, t.due_date";

            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                String status = rs.getString("status");
                java.sql.Date dueDate = rs.getDate("due_date");

                if (!"Completed".equals(status) && dueDate != null &&
                        dueDate.toLocalDate().isBefore(java.time.LocalDate.now())) {
                    status = "Overdue";
                }

                tableModel.addRow(new Object[]{
                        rs.getInt("task_id"),
                        rs.getString("task_name"),
                        rs.getString("plant_name"),
                        rs.getString("worker_name"),
                        rs.getDate("due_date"),
                        status
                });
            }

            showSuccess("Loaded " + tableModel.getRowCount() + " tasks from database");

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error loading task data: " + e.getMessage());

            Object[][] data = {
                    {1, "Watering - Tomato Plant", "Tomato Plant", "John Doe", java.sql.Date.valueOf("2024-03-25"), "Completed"},
                    {2, "Fertilize - Tomato Plant", "Tomato Plant", "Jane Smith", java.sql.Date.valueOf("2024-03-28"), "Pending"},
                    {3, "Pruning - Rose Bush", "Rose Bush", "Bob Wilson", java.sql.Date.valueOf("2024-03-26"), "In Progress"},
                    {4, "Harvest - Basil Herb", "Basil Herb", "Alice Brown", java.sql.Date.valueOf("2024-03-24"), "Completed"},
                    {5, "Soil Test - Lemon Tree", "Lemon Tree", "Charlie Davis", java.sql.Date.valueOf("2024-03-29"), "Pending"}
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

    private void addTask() {
        JDialog taskDialog = new JDialog(this, "Add New Task", true);
        taskDialog.setSize(400, 450);
        taskDialog.setLocationRelativeTo(this);
        taskDialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Task Name:"), gbc);

        gbc.gridx = 1;
        JTextField taskNameField = new JTextField(20);
        formPanel.add(taskNameField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Plant:"), gbc);

        gbc.gridx = 1;
        JComboBox<String> plantCombo = new JComboBox<>();
        loadPlantsIntoComboBox(plantCombo);
        formPanel.add(plantCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Due Date (YYYY-MM-DD):"), gbc);

        gbc.gridx = 1;
        JTextField dateField = new JTextField(15);
        dateField.setText(java.time.LocalDate.now().plusDays(3).toString());
        formPanel.add(dateField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Status:"), gbc);

        gbc.gridx = 1;
        JComboBox<String> statusCombo = new JComboBox<>(new String[]{
                "Pending", "In Progress", "Completed"
        });
        formPanel.add(statusCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Description:"), gbc);

        gbc.gridx = 1;
        gbc.gridheight = 2;
        JTextArea descArea = new JTextArea(3, 20);
        descArea.setLineWrap(true);
        JScrollPane descScroll = new JScrollPane(descArea);
        formPanel.add(descScroll, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        JButton saveButton = new JButton("Save Task");
        saveButton.setBackground(new Color(46, 125, 50));
        saveButton.setForeground(Color.BLACK);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(new Color(219, 68, 55));
        cancelButton.setForeground(Color.BLACK);

        saveButton.addActionListener(e -> {
            String taskName = taskNameField.getText().trim();
            String selectedPlant = (String) plantCombo.getSelectedItem();
            String dueDate = dateField.getText().trim();
            String status = (String) statusCombo.getSelectedItem();
            String description = descArea.getText().trim();

            if (taskName.isEmpty()) {
                JOptionPane.showMessageDialog(taskDialog,
                        "Please enter a task name",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            // Extract plant ID
            int plantId = 0;
            if (selectedPlant != null && selectedPlant.contains("[")) {
                String idStr = selectedPlant.substring(selectedPlant.indexOf("[") + 1, selectedPlant.indexOf("]"));
                plantId = Integer.parseInt(idStr);
            }

            // Save to database
            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = DatabaseConnection.getConnection();
                String query = "INSERT INTO Task (plant_id, task_name, due_date, status) VALUES (?, ?, ?, ?)";
                stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
                stmt.setInt(1, plantId > 0 ? plantId : null);
                stmt.setString(2, taskName);
                stmt.setDate(3, java.sql.Date.valueOf(dueDate));
                stmt.setString(4, status);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    ResultSet generatedKeys = stmt.getGeneratedKeys();
                    int newId = 0;
                    if (generatedKeys.next()) {
                        newId = generatedKeys.getInt(1);
                    }

                    String plantName = selectedPlant != null ?
                            selectedPlant.replaceAll(" \\[\\d+\\]$", "") : "None";

                    tableModel.addRow(new Object[]{
                            newId, taskName, plantName, "Unassigned",
                            java.sql.Date.valueOf(dueDate), status
                    });

                    showSuccess("Task '" + taskName + "' added successfully!");
                    taskDialog.dispose();
                }

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(taskDialog,
                        "Error: " + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            } finally {
                try { if (stmt != null) stmt.close(); } catch (SQLException ex) {}
                DatabaseConnection.closeConnection(conn);
            }
        });

        cancelButton.addActionListener(e -> taskDialog.dispose());

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        taskDialog.add(formPanel, BorderLayout.CENTER);
        taskDialog.add(buttonPanel, BorderLayout.SOUTH);
        taskDialog.setVisible(true);
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

    private void markComplete() {
        int row = taskTable.getSelectedRow();
        if (row >= 0) {
            int modelRow = taskTable.convertRowIndexToModel(row);
            int taskId = (int) tableModel.getValueAt(modelRow, 0);
            String taskName = (String) tableModel.getValueAt(modelRow, 1);

            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = DatabaseConnection.getConnection();
                String query = "UPDATE Task SET status = 'Completed' WHERE task_id = ?";
                stmt = conn.prepareStatement(query);
                stmt.setInt(1, taskId);

                int rowsAffected = stmt.executeUpdate();
                if (rowsAffected > 0) {
                    tableModel.setValueAt("Completed", modelRow, 5);
                    taskTable.repaint();

                    JOptionPane.showMessageDialog(this,
                            "Task '" + taskName + "' marked as complete!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);
                }

            } catch (SQLException e) {
                e.printStackTrace();
                showError("Error updating task: " + e.getMessage());
            } finally {
                try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
                DatabaseConnection.closeConnection(conn);
            }
        }
    }

    private void showCalendar() {
        JDialog calendarDialog = new JDialog(this, "Calendar View", true);
        calendarDialog.setSize(600, 500);
        calendarDialog.setLocationRelativeTo(this);
        calendarDialog.setLayout(new BorderLayout());

        java.time.YearMonth dialogYearMonth = java.time.YearMonth.now();

        JLabel dialogTitle = new JLabel(dialogYearMonth.getMonth().toString() + " " + dialogYearMonth.getYear(), SwingConstants.CENTER);
        dialogTitle.setFont(new Font("Segoe UI", Font.BOLD, 20));
        dialogTitle.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JPanel dialogCalendarGrid = new JPanel(new GridLayout(7, 7, 10, 10));
        dialogCalendarGrid.setBackground(Color.WHITE);
        dialogCalendarGrid.setBorder(BorderFactory.createEmptyBorder(0, 20, 20, 20));

        String[] days = {"Sun", "Mon", "Tue", "Wed", "Thu", "Fri", "Sat"};

        for (String day : days) {
            JLabel dayLabel = new JLabel(day, SwingConstants.CENTER);
            dayLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
            dayLabel.setForeground(new Color(66, 133, 244));
            dialogCalendarGrid.add(dayLabel);
        }

        java.time.LocalDate firstDayOfMonth = dialogYearMonth.atDay(1);
        int firstDayOfWeek = firstDayOfMonth.getDayOfWeek().getValue() % 7;

        for (int i = 0; i < firstDayOfWeek; i++) {
            dialogCalendarGrid.add(new JLabel(""));
        }

        int daysInMonth = dialogYearMonth.lengthOfMonth();

        for (int day = 1; day <= daysInMonth; day++) {
            final int currentDay = day;
            JPanel dayPanel = new JPanel(new BorderLayout());
            dayPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 1));
            dayPanel.setBackground(Color.WHITE);

            JLabel dayNum = new JLabel(String.valueOf(day), SwingConstants.CENTER);
            dayNum.setFont(new Font("Segoe UI", Font.PLAIN, 14));

            java.time.LocalDate currentDate = java.time.LocalDate.now();
            if (day == currentDate.getDayOfMonth() &&
                    dialogYearMonth.getYear() == currentDate.getYear() &&
                    dialogYearMonth.getMonthValue() == currentDate.getMonthValue()) {
                dayPanel.setBackground(new Color(46, 125, 50, 30));
                dayNum.setFont(dayNum.getFont().deriveFont(Font.BOLD));
                dayNum.setForeground(new Color(46, 125, 50));
            }

            java.time.LocalDate checkDate = java.time.LocalDate.of(
                    dialogYearMonth.getYear(),
                    dialogYearMonth.getMonth(),
                    currentDay
            );
            int taskCount = getTaskCountForDate(checkDate);
            if (taskCount > 0) {
                JLabel taskDot = new JLabel("•");
                taskDot.setFont(new Font("Arial", Font.BOLD, 24));
                taskDot.setForeground(new Color(244, 180, 0));
                taskDot.setHorizontalAlignment(SwingConstants.CENTER);
                dayPanel.add(taskDot, BorderLayout.SOUTH);

                dayPanel.setToolTipText(taskCount + " tasks on " + checkDate);
            }

            dayPanel.addMouseListener(new java.awt.event.MouseAdapter() {
                public void mouseClicked(java.awt.event.MouseEvent evt) {
                    java.time.LocalDate selectedDate = java.time.LocalDate.of(
                            dialogYearMonth.getYear(),
                            dialogYearMonth.getMonth(),
                            currentDay
                    );
                    showTasksForDate(selectedDate);
                }
            });

            dayPanel.add(dayNum, BorderLayout.CENTER);
            dialogCalendarGrid.add(dayPanel);
        }

        JPanel dialogNavPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
        dialogNavPanel.setBackground(Color.WHITE);

        JButton dialogPrevButton = new JButton("← Previous Month");
        JButton dialogNextButton = new JButton("Next Month →");
        JButton closeButton = new JButton("Close");

        dialogPrevButton.addActionListener(e -> {
            calendarDialog.dispose();
            showSuccess("In a full implementation, this would navigate to previous month");
        });

        dialogNextButton.addActionListener(e -> {
            calendarDialog.dispose();
            showSuccess("In a full implementation, this would navigate to next month");
        });

        closeButton.addActionListener(e -> calendarDialog.dispose());

        dialogNavPanel.add(dialogPrevButton);
        dialogNavPanel.add(dialogNextButton);
        dialogNavPanel.add(closeButton);

        calendarDialog.add(dialogTitle, BorderLayout.NORTH);
        calendarDialog.add(new JScrollPane(dialogCalendarGrid), BorderLayout.CENTER);
        calendarDialog.add(dialogNavPanel, BorderLayout.SOUTH);
        calendarDialog.setVisible(true);
    }

    private void showTasksForDate(java.time.LocalDate date) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            String query = "SELECT t.task_name, p.name as plant_name, t.status " +
                    "FROM Task t " +
                    "LEFT JOIN Plant p ON t.plant_id = p.Plant_ID " +
                    "WHERE t.due_date = ? " +
                    "ORDER BY t.status";

            stmt = conn.prepareStatement(query);
            stmt.setDate(1, java.sql.Date.valueOf(date));
            rs = stmt.executeQuery();

            StringBuilder tasks = new StringBuilder("Tasks for " + date + ":\n\n");
            int count = 0;

            while (rs.next()) {
                count++;
                tasks.append("• ").append(rs.getString("task_name"))
                        .append(" (").append(rs.getString("plant_name")).append(") - ")
                        .append(rs.getString("status")).append("\n");
            }

            if (count == 0) {
                tasks.append("No tasks scheduled for this date.");
            }

            JOptionPane.showMessageDialog(this, tasks.toString(),
                    "Tasks for " + date, JOptionPane.INFORMATION_MESSAGE);

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error loading tasks for date: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
    }

    private void filterTasks(String filter) {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            tableModel.setRowCount(0);

            conn = DatabaseConnection.getConnection();
            String query = "SELECT t.task_id, t.task_name, p.name as plant_name, " +
                    "w.name as worker_name, t.due_date, t.status " +
                    "FROM Task t " +
                    "LEFT JOIN Plant p ON t.plant_id = p.Plant_ID " +
                    "LEFT JOIN worker w ON (SELECT w2.id FROM worker w2 WHERE w2.id = 1) " +
                    "WHERE 1=1 ";

            switch (filter) {
                case "Today":
                    query += "AND t.due_date = CURDATE() ";
                    break;
                case "This Week":
                    query += "AND t.due_date BETWEEN CURDATE() AND DATE_ADD(CURDATE(), INTERVAL 7 DAY) ";
                    break;
                case "Overdue":
                    query += "AND t.due_date < CURDATE() AND t.status != 'Completed' ";
                    break;
                case "By Worker":
                    query += "AND w.name = 'John Doe' ";
                    break;
                case "By Greenhouse":

                    query += "AND p.greenhouse_id = 1 ";
                    break;

            }

            query += "ORDER BY t.due_date, t.status";

            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            while (rs.next()) {
                String status = rs.getString("status");
                java.sql.Date dueDate = rs.getDate("due_date");

                if (!"Completed".equals(status) && dueDate != null &&
                        dueDate.toLocalDate().isBefore(java.time.LocalDate.now())) {
                    status = "Overdue";
                }

                tableModel.addRow(new Object[]{
                        rs.getInt("task_id"),
                        rs.getString("task_name"),
                        rs.getString("plant_name"),
                        rs.getString("worker_name"),
                        rs.getDate("due_date"),
                        status
                });
            }

            showSuccess("Filtered: " + filter + " (" + tableModel.getRowCount() + " tasks)");

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error filtering tasks: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
    }
}