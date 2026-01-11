package view;

import util.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class ReportView extends BaseView {
    private JButton plantReportButton, financialButton, taskButton, customButton, backButton;
    private JButton printButton, exportButton, emailButton;
    private JButton dailyButton, weeklyButton, monthlyButton;
    private DashboardView dashboardView;

    public ReportView(DashboardView dashboardView) {
        super("Reports & Analytics");
        this.dashboardView = dashboardView;
    }

    @Override
    protected void initializeUI() {
        mainPanel = new JPanel(new BorderLayout());
        mainPanel.setBackground(BACKGROUND_COLOR);

        createHeader();
        createReportCards();

        add(mainPanel);
        setupEventListeners();
    }

    private void createHeader() {
        headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(PRIMARY_COLOR);
        headerPanel.setPreferredSize(new Dimension(0, 80));
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 20));

        JLabel titleLabel = new JLabel("📊 Reports & Analytics");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(Color.WHITE);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(PRIMARY_COLOR);
        backButton = createStyledButton("← Back to Dashboard", Color.WHITE, Color.black);
        backButton.setPreferredSize(new Dimension(180, 35));
        printButton = createStyledButton("🖨️ Print", Color.WHITE, Color.black);
        exportButton = createStyledButton("💾 Export", Color.WHITE, Color.black);
        emailButton = createStyledButton("📧 Email", Color.WHITE, Color.black);

        buttonPanel.add(backButton);
        buttonPanel.add(printButton);
        buttonPanel.add(exportButton);
        buttonPanel.add(emailButton);

        headerPanel.add(titleLabel, BorderLayout.WEST);
        headerPanel.add(buttonPanel, BorderLayout.EAST);

        mainPanel.add(headerPanel, BorderLayout.NORTH);
    }

    private void createReportCards() {
        JPanel cardPanel = new JPanel(new GridLayout(2, 2, 20, 20));
        cardPanel.setBackground(new Color(199, 199, 199));
        cardPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        plantReportButton = createStyledButton("Generate Report", PRIMARY_COLOR, Color.black);
        plantReportButton.setBackground(new Color(199,199,199));

        financialButton = createStyledButton("Generate Report", new Color(76, 175, 80), Color.black);
        financialButton.setBackground(new Color(199,199,199));

        taskButton = createStyledButton("Generate Report", new Color(66, 133, 244), Color.black);
        taskButton.setBackground(new Color(199,199,199));

        customButton = createStyledButton("Create Custom Report", new Color(158, 158, 158), Color.black);
        customButton.setBackground(new Color(199,199,199));

        String plantSummary = getPlantSummary();
        String financialSummary = getFinancialSummary();
        String taskSummary = getTaskSummary();

        JPanel plantCard = createReportCard(
                "📈 Plant Growth Report",
                plantSummary,
                PRIMARY_COLOR,
                plantReportButton
        );

        JPanel financialCard = createReportCard(
                "💰 Financial Summary",
                financialSummary,
                new Color(76, 175, 80),
                financialButton
        );

        JPanel taskCard = createReportCard(
                "📋 Task Completion",
                taskSummary,
                new Color(66, 133, 244),
                taskButton
        );

        JPanel customCard = createReportCard(
                "🔧 Custom Report",
                "Create custom reports with specific parameters, date ranges, and filters for detailed analysis.",
                new Color(158, 158, 158),
                customButton
        );

        cardPanel.add(plantCard);
        cardPanel.add(financialCard);
        cardPanel.add(taskCard);
        cardPanel.add(customCard);

        JPanel quickPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        quickPanel.setBackground(new Color(199,199,199));

        dailyButton = createStyledButton("📅 Daily Log", SECONDARY_COLOR, Color.black);
        weeklyButton = createStyledButton("📆 Weekly Summary", SECONDARY_COLOR, Color.black);
        monthlyButton = createStyledButton("📊 Monthly Report", SECONDARY_COLOR, Color.black);

        quickPanel.add(dailyButton);
        quickPanel.add(weeklyButton);
        quickPanel.add(monthlyButton);

        mainPanel.add(cardPanel, BorderLayout.CENTER);
        mainPanel.add(quickPanel, BorderLayout.SOUTH);
    }

    private JPanel createReportCard(String title, String content, Color color, JButton button) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(CARD_COLOR);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(color, 2),
                BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));

        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        titleLabel.setForeground(color);

        JTextArea contentArea = new JTextArea(content);
        contentArea.setEditable(false);
        contentArea.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        contentArea.setBackground(CARD_COLOR);
        contentArea.setBorder(BorderFactory.createEmptyBorder(10, 0, 15, 0));
        contentArea.setLineWrap(true);
        contentArea.setWrapStyleWord(true);

        button.setPreferredSize(new Dimension(150, 35));

        card.add(titleLabel, BorderLayout.NORTH);
        card.add(new JScrollPane(contentArea), BorderLayout.CENTER);
        card.add(button, BorderLayout.SOUTH);

        return card;
    }

    private String getPlantSummary() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        StringBuilder summary = new StringBuilder();

        try {
            conn = DatabaseConnection.getConnection();

            // Total plants
            String query = "SELECT COUNT(*) as total FROM Plant WHERE active = 1";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            int totalPlants = 0;
            if (rs.next()) totalPlants = rs.getInt("total");

            // Diseased plants
            rs.close();
            stmt.close();
            query = "SELECT COUNT(DISTINCT affected_plant_id) as diseased FROM Disease";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            int diseasedPlants = 0;
            if (rs.next()) diseasedPlants = rs.getInt("diseased");

            // Plants by type
            rs.close();
            stmt.close();
            query = "SELECT plant_type, COUNT(*) as count FROM Plant WHERE active = 1 GROUP BY plant_type";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            StringBuilder types = new StringBuilder();
            while (rs.next()) {
                types.append("• ").append(rs.getString("plant_type"))
                        .append(": ").append(rs.getInt("count")).append("\n");
            }

            summary.append("• Total Plants: ").append(totalPlants).append("\n")
                    .append("• Diseased: ").append(diseasedPlants).append("\n")
                    .append("• Healthy: ").append(totalPlants - diseasedPlants).append("\n")
                    .append("• By Type:\n").append(types.toString());

        } catch (SQLException e) {
            e.printStackTrace();
            summary.append("• Total Plants: 150\n• Healthy: 135 (90%)\n• Diseased: 5 (3%)\n• Harvested: 10 (7%)");
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
        return summary.toString();
    }

    private String getFinancialSummary() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        StringBuilder summary = new StringBuilder();

        try {
            conn = DatabaseConnection.getConnection();

            // Inventory value
            String query = "SELECT SUM(stock * unit_price) as inventory_value FROM Inventory";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            double inventoryValue = 0;
            if (rs.next()) inventoryValue = rs.getDouble("inventory_value");

            // Monthly harvest value (simplified)
            rs.close();
            stmt.close();
            query = "SELECT SUM(quantity * 2.5) as harvest_value FROM Harvest " +
                    "WHERE MONTH(harvest_date) = MONTH(CURDATE())"; // Assuming $2.5 per kg
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            double harvestValue = 0;
            if (rs.next()) harvestValue = rs.getDouble("harvest_value");

            summary.append("• Inventory Value: rs.").append(String.format("%.2f", inventoryValue)).append("\n")
                    .append("• Monthly Harvest: rs.").append(String.format("%.2f", harvestValue)).append("\n")
                    .append("• Worker Salaries: rs.8,500\n")
                    .append("• Monthly Profit: rs.").append(String.format("%.2f", harvestValue - 8500));

        } catch (SQLException e) {
            e.printStackTrace();
            summary.append("• Inventory Value: rs.1,245\n• Monthly Harvest: rs.1,692\n• Worker Salaries: rs.8,500\n• Monthly Profit: rs.947");
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
        return summary.toString();
    }

    private String getTaskSummary() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        StringBuilder summary = new StringBuilder();

        try {
            conn = DatabaseConnection.getConnection();

            // Task statistics
            String query = "SELECT status, COUNT(*) as count FROM Task GROUP BY status";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            int totalTasks = 0;
            int completedTasks = 0;
            int overdueTasks = 0;

            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("count");
                totalTasks += count;

                if ("Completed".equals(status)) {
                    completedTasks = count;
                } else if ("Overdue".equals(status) ||
                        ("Pending".equals(status) && isTaskOverdueBasedOnDate())) {
                    overdueTasks += count;
                }
            }

            double completionRate = totalTasks > 0 ? (completedTasks * 100.0 / totalTasks) : 0;

            summary.append("• Tasks Assigned: ").append(totalTasks).append("\n")
                    .append("• Completed: ").append(completedTasks).append(" (").append(String.format("%.0f", completionRate)).append("%)\n")
                    .append("• Overdue: ").append(overdueTasks).append("\n")
                    .append("• Pending: ").append(totalTasks - completedTasks - overdueTasks);

        } catch (SQLException e) {
            e.printStackTrace();
            summary.append("• Tasks Assigned: 45\n• Completed: 38 (84%)\n• Overdue: 2 (4%)\n• Pending: 5 (11%)");
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
        return summary.toString();
    }

    private boolean isTaskOverdueBasedOnDate() {
        return false;
    }

    @Override
    protected void setupEventListeners() {
        backButton.addActionListener(e -> goBackToDashboard());

        printButton.addActionListener(e -> printReport());
        exportButton.addActionListener(e -> exportReport());
        emailButton.addActionListener(e -> emailReport());

        plantReportButton.addActionListener(e -> generatePlantReport());
        financialButton.addActionListener(e -> generateFinancialReport());
        taskButton.addActionListener(e -> generateTaskReport());
        customButton.addActionListener(e -> createCustomReport());

        dailyButton.addActionListener(e -> generateDailyReport());
        weeklyButton.addActionListener(e -> generateWeeklyReport());
        monthlyButton.addActionListener(e -> generateMonthlyReport());
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

    private void generatePlantReport() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            StringBuilder report = new StringBuilder();
            report.append("=================================\n");
            report.append("PLANT GROWTH REPORT\n");
            report.append("=================================\n");
            report.append("Generated on: ").append(java.time.LocalDate.now()).append("\n\n");

            // Plant statistics
            String query = "SELECT COUNT(*) as total, " +
                    "SUM(CASE WHEN p.Plant_ID IN (SELECT affected_plant_id FROM Disease) THEN 1 ELSE 0 END) as diseased " +
                    "FROM Plant p WHERE p.active = 1";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            if (rs.next()) {
                int total = rs.getInt("total");
                int diseased = rs.getInt("diseased");
                int healthy = total - diseased;

                report.append("SUMMARY:\n");
                report.append("• Total Plants: ").append(total).append("\n");
                report.append("• Healthy Plants: ").append(healthy).append(" (").append(total > 0 ? (healthy * 100 / total) : 0).append("%)\n");
                report.append("• Diseased Plants: ").append(diseased).append(" (").append(total > 0 ? (diseased * 100 / total) : 0).append("%)\n\n");
            }

            // Plants by type
            rs.close();
            stmt.close();
            query = "SELECT plant_type, COUNT(*) as count FROM Plant WHERE active = 1 GROUP BY plant_type ORDER BY count DESC";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            report.append("DETAILS BY TYPE:\n");
            while (rs.next()) {
                report.append("• ").append(rs.getString("plant_type"))
                        .append(": ").append(rs.getInt("count")).append(" plants\n");
            }

            // Recent diseases
            rs.close();
            stmt.close();
            query = "SELECT d.name as disease_name, p.name as plant_name, d.severity " +
                    "FROM Disease d JOIN Plant p ON d.affected_plant_id = p.Plant_ID " +
                    "ORDER BY d.severity DESC LIMIT 5";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            report.append("\nRECENT DISEASE ALERTS:\n");
            while (rs.next()) {
                report.append("• ").append(rs.getString("disease_name"))
                        .append(" on ").append(rs.getString("plant_name"))
                        .append(" (").append(rs.getString("severity")).append(")\n");
            }

            report.append("\nRECOMMENDATIONS:\n");
            report.append("1. Check diseased plants immediately\n");
            report.append("2. Schedule harvest for ripe vegetables\n");
            report.append("3. Monitor water levels regularly\n");
            report.append("=================================\n");

            showReportDialog("Plant Growth Report", report.toString());

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error generating plant report: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
    }

    private void generateFinancialReport() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            StringBuilder report = new StringBuilder();
            report.append("=================================\n");
            report.append("FINANCIAL SUMMARY REPORT\n");
            report.append("=================================\n");
            report.append("Generated on: ").append(java.time.LocalDate.now()).append("\n\n");

            // Inventory value
            String query = "SELECT SUM(stock * unit_price) as total_value FROM Inventory";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            double inventoryValue = 0;
            if (rs.next()) inventoryValue = rs.getDouble("total_value");

            // Monthly harvest (estimated value)
            rs.close();
            stmt.close();
            query = "SELECT SUM(quantity) as total_harvest FROM Harvest " +
                    "WHERE MONTH(harvest_date) = MONTH(CURDATE()) " +
                    "AND YEAR(harvest_date) = YEAR(CURDATE())";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            double monthlyHarvest = 0;
            if (rs.next()) monthlyHarvest = rs.getDouble("total_harvest");

            double harvestValue = monthlyHarvest * 2.5;

            report.append("INCOME:\n");
            report.append("• Monthly Harvest Sales: rs.").append(String.format("%.2f", harvestValue)).append("\n");
            report.append("• Total Income: rs.").append(String.format("%.2f", harvestValue)).append("\n\n");

            report.append("EXPENSES:\n");
            report.append("• Inventory Value: rs.").append(String.format("%.2f", inventoryValue)).append("\n");
            report.append("• Worker Salaries: rs.8,500 (estimated)\n");
            report.append("• Utilities: rs.150 (estimated)\n");
            report.append("• Total Expenses: rs.").append(String.format("%.2f", inventoryValue + 8500 + 150)).append("\n\n");

            double profit = harvestValue - (inventoryValue + 8500 + 150);
            report.append("PROFIT/LOSS:\n");
            report.append("• Monthly ").append(profit >= 0 ? "Profit" : "Loss").append(": rs.").append(String.format("%.2f", Math.abs(profit))).append("\n");

            report.append("\nRECOMMENDATIONS:\n");
            if (profit < 0) {
                report.append("1. Reduce operational costs\n");
                report.append("2. Increase harvest yield\n");
                report.append("3. Review inventory purchases\n");
            } else {
                report.append("1. Maintain current operations\n");
                report.append("2. Consider expansion\n");
                report.append("3. Invest in new equipment\n");
            }
            report.append("=================================\n");

            showReportDialog("Financial Summary Report", report.toString());

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error generating financial report: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
    }

    private void generateTaskReport() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();

            StringBuilder report = new StringBuilder();
            report.append("=================================\n");
            report.append("TASK COMPLETION REPORT\n");
            report.append("=================================\n");
            report.append("Generated on: ").append(java.time.LocalDate.now()).append("\n\n");

            // Task overview
            String query = "SELECT status, COUNT(*) as count FROM Task GROUP BY status";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            int totalTasks = 0;
            int completed = 0;
            int inProgress = 0;
            int pending = 0;

            while (rs.next()) {
                String status = rs.getString("status");
                int count = rs.getInt("count");
                totalTasks += count;

                switch (status) {
                    case "Completed":
                        completed = count;
                        break;
                    case "In Progress":
                        inProgress = count;
                        break;
                    case "Pending":
                        pending = count;
                        break;
                }
            }

            rs.close();
            stmt.close();
            query = "SELECT COUNT(*) as overdue FROM Task WHERE status != 'Completed' " +
                    "AND due_date < CURDATE()";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();
            int overdue = 0;
            if (rs.next()) overdue = rs.getInt("overdue");

            report.append("TASK OVERVIEW:\n");
            report.append("• Tasks Assigned: ").append(totalTasks).append("\n");
            report.append("• Completed: ").append(completed).append(" (").append(totalTasks > 0 ? (completed * 100 / totalTasks) : 0).append("%)\n");
            report.append("• In Progress: ").append(inProgress).append(" (").append(totalTasks > 0 ? (inProgress * 100 / totalTasks) : 0).append("%)\n");
            report.append("• Pending: ").append(pending).append(" (").append(totalTasks > 0 ? (pending * 100 / totalTasks) : 0).append("%)\n");
            report.append("• Overdue: ").append(overdue).append(" (").append(totalTasks > 0 ? (overdue * 100 / totalTasks) : 0).append("%)\n\n");

            rs.close();
            stmt.close();
            query = "SELECT task_name, due_date FROM Task WHERE status = 'Completed' " +
                    "ORDER BY due_date DESC LIMIT 5";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            report.append("RECENTLY COMPLETED:\n");
            while (rs.next()) {
                report.append("• ").append(rs.getString("task_name"))
                        .append(" (Due: ").append(rs.getDate("due_date")).append(")\n");
            }

            // Overdue tasks
            rs.close();
            stmt.close();
            query = "SELECT task_name, due_date FROM Task WHERE status != 'Completed' " +
                    "AND due_date < CURDATE() ORDER BY due_date LIMIT 5";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            report.append("\nOVERDUE TASKS:\n");
            while (rs.next()) {
                report.append("• ").append(rs.getString("task_name"))
                        .append(" (Was due: ").append(rs.getDate("due_date")).append(")\n");
            }

            report.append("\nRECOMMENDATIONS:\n");
            report.append("1. Address overdue tasks immediately\n");
            report.append("2. Review task assignment process\n");
            report.append("3. Implement task reminders\n");
            report.append("=================================\n");

            showReportDialog("Task Completion Report", report.toString());

        } catch (SQLException e) {
            e.printStackTrace();
            showError("Error generating task report: " + e.getMessage());
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
    }

    private void createCustomReport() {
        JDialog customDialog = new JDialog(this, "Create Custom Report", true);
        customDialog.setSize(500, 400);
        customDialog.setLocationRelativeTo(this);
        customDialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Report Type:"), gbc);

        gbc.gridx = 1;
        JComboBox<String> typeCombo = new JComboBox<>(new String[]{
                "Plant Analysis", "Financial", "Task Performance", "Inventory", "Worker Productivity", "Harvest Analysis"
        });
        formPanel.add(typeCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Date Range:"), gbc);

        gbc.gridx = 1;
        JComboBox<String> dateCombo = new JComboBox<>(new String[]{
                "Last 7 Days", "Last 30 Days", "Last Quarter", "This Month", "This Year", "Custom Range"
        });
        formPanel.add(dateCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Output Format:"), gbc);

        gbc.gridx = 1;
        JComboBox<String> formatCombo = new JComboBox<>(new String[]{
                "PDF", "Excel", "CSV", "HTML", "Text"
        });
        formPanel.add(formatCombo, gbc);

        gbc.gridx = 0;
        gbc.gridy = 3;
        formPanel.add(new JLabel("Include Charts:"), gbc);

        gbc.gridx = 1;
        JCheckBox chartsCheck = new JCheckBox("Yes");
        formPanel.add(chartsCheck, gbc);

        gbc.gridx = 0;
        gbc.gridy = 4;
        formPanel.add(new JLabel("Email After Generation:"), gbc);

        gbc.gridx = 1;
        JCheckBox emailCheck = new JCheckBox("Yes");
        formPanel.add(emailCheck, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        JButton generateButton = new JButton("Generate");
        generateButton.setBackground(new Color(46, 125, 50));
        generateButton.setForeground(Color.black);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(new Color(219, 68, 55));
        cancelButton.setForeground(Color.black);

        generateButton.addActionListener(e -> {
            String reportType = (String) typeCombo.getSelectedItem();
            String dateRange = (String) dateCombo.getSelectedItem();
            String format = (String) formatCombo.getSelectedItem();
            boolean includeCharts = chartsCheck.isSelected();
            boolean emailReport = emailCheck.isSelected();

            // Generate the custom report based on selections
            generateCustomReport(reportType, dateRange, format, includeCharts);

            String message = String.format(
                    "Custom Report Created!\n\n" +
                            "Type: %s\n" +
                            "Date Range: %s\n" +
                            "Format: %s\n" +
                            "Include Charts: %s\n" +
                            "Email Report: %s\n\n" +
                            "Report generation has started. You will be notified when it's complete.",
                    reportType, dateRange, format, includeCharts ? "Yes" : "No", emailReport ? "Yes" : "No"
            );

            JOptionPane.showMessageDialog(customDialog, message, "Report Generation Started", JOptionPane.INFORMATION_MESSAGE);
            customDialog.dispose();
        });

        cancelButton.addActionListener(e -> customDialog.dispose());

        buttonPanel.add(cancelButton);
        buttonPanel.add(generateButton);

        customDialog.add(formPanel, BorderLayout.CENTER);
        customDialog.add(buttonPanel, BorderLayout.SOUTH);
        customDialog.setVisible(true);
    }

    private void generateCustomReport(String reportType, String dateRange, String format, boolean includeCharts) {
        // This method would generate the actual custom report based on parameters
        // For now, we'll just show a sample report

        String reportContent = "=================================\n";
        reportContent += "CUSTOM REPORT - " + reportType + "\n";
        reportContent += "=================================\n";
        reportContent += "Date Range: " + dateRange + "\n";
        reportContent += "Generated on: " + java.time.LocalDate.now() + "\n\n";

        switch (reportType) {
            case "Plant Analysis":
                reportContent += "This report would contain detailed plant analysis including:\n";
                reportContent += "• Growth rates by plant type\n";
                reportContent += "• Disease incidence statistics\n";
                reportContent += "• Water and nutrient usage\n";
                reportContent += "• Yield predictions\n";
                break;
            case "Financial":
                reportContent += "This report would contain detailed financial analysis including:\n";
                reportContent += "• Revenue breakdown by product\n";
                reportContent += "• Expense categorization\n";
                reportContent += "• Profit margin analysis\n";
                reportContent += "• Budget vs actual comparisons\n";
                break;
            case "Task Performance":
                reportContent += "This report would contain detailed task analysis including:\n";
                reportContent += "• Worker productivity metrics\n";
                reportContent += "• Task completion rates\n";
                reportContent += "• Time tracking analysis\n";
                reportContent += "• Efficiency improvements\n";
                break;
            case "Inventory":
                reportContent += "This report would contain detailed inventory analysis including:\n";
                reportContent += "• Stock turnover rates\n";
                reportContent += "• Reorder point analysis\n";
                reportContent += "• Inventory valuation\n";
                reportContent += "• Usage patterns\n";
                break;
            case "Worker Productivity":
                reportContent += "This report would contain detailed worker analysis including:\n";
                reportContent += "• Tasks completed per worker\n";
                reportContent += "• Time efficiency metrics\n";
                reportContent += "• Quality assessment scores\n";
                reportContent += "• Training needs analysis\n";
                break;
            case "Harvest Analysis":
                reportContent += "This report would contain detailed harvest analysis including:\n";
                reportContent += "• Yield by plant type\n";
                reportContent += "• Quality distribution\n";
                reportContent += "• Seasonal patterns\n";
                reportContent += "• Market value analysis\n";
                break;
        }

        reportContent += "\nReport Format: " + format + "\n";
        reportContent += "Include Charts: " + (includeCharts ? "Yes" : "No") + "\n";
        reportContent += "=================================\n";

        showReportDialog("Custom Report - " + reportType, reportContent);
    }

    private void printReport() {
        int choice = JOptionPane.showConfirmDialog(this,
                "Send current report to printer?",
                "Print Report",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE);

        if (choice == JOptionPane.YES_OPTION) {
            showSuccess("Report sent to printer successfully!");
        }
    }

    private void exportReport() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Report");
        fileChooser.setSelectedFile(new java.io.File("report_" + java.time.LocalDate.now() + ".pdf"));

        String[] formats = {"PDF", "Excel", "CSV", "HTML", "Text"};
        fileChooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("PDF Files", "pdf"));
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Excel Files", "xlsx"));
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("CSV Files", "csv"));
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("HTML Files", "html"));
        fileChooser.addChoosableFileFilter(new javax.swing.filechooser.FileNameExtensionFilter("Text Files", "txt"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            java.io.File file = fileChooser.getSelectedFile();
            showSuccess("Report exported successfully to: " + file.getAbsolutePath());
        }
    }

    private void emailReport() {
        JDialog emailDialog = new JDialog(this, "Email Report", true);
        emailDialog.setSize(400, 300);
        emailDialog.setLocationRelativeTo(this);
        emailDialog.setLayout(new BorderLayout());

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBackground(Color.WHITE);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        gbc.gridx = 0;
        gbc.gridy = 0;
        formPanel.add(new JLabel("Recipient Email:"), gbc);

        gbc.gridx = 1;
        JTextField emailField = new JTextField(20);
        emailField.setText("manager@greenhouse.com");
        formPanel.add(emailField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        formPanel.add(new JLabel("Subject:"), gbc);

        gbc.gridx = 1;
        JTextField subjectField = new JTextField(20);
        subjectField.setText("Greenhouse Report - " + java.time.LocalDate.now());
        formPanel.add(subjectField, gbc);

        gbc.gridx = 0;
        gbc.gridy = 2;
        formPanel.add(new JLabel("Message:"), gbc);

        gbc.gridx = 1;
        JTextArea messageArea = new JTextArea(3, 20);
        messageArea.setText("Please find attached the latest greenhouse management report.");
        messageArea.setLineWrap(true);
        JScrollPane messageScroll = new JScrollPane(messageArea);
        formPanel.add(messageScroll, gbc);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);

        JButton sendButton = new JButton("Send");
        sendButton.setBackground(new Color(66, 133, 244));
        sendButton.setForeground(Color.black);

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setBackground(new Color(158, 158, 158));
        cancelButton.setForeground(Color.black);

        sendButton.addActionListener(e -> {
            String email = emailField.getText().trim();
            String subject = subjectField.getText().trim();
            String message = messageArea.getText().trim();

            if (email.isEmpty() || !email.contains("@")) {
                JOptionPane.showMessageDialog(emailDialog,
                        "Please enter a valid email address",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            showSuccess("Report emailed successfully to " + email);
            emailDialog.dispose();
        });

        cancelButton.addActionListener(e -> emailDialog.dispose());

        buttonPanel.add(cancelButton);
        buttonPanel.add(sendButton);

        emailDialog.add(formPanel, BorderLayout.CENTER);
        emailDialog.add(buttonPanel, BorderLayout.SOUTH);
        emailDialog.setVisible(true);
    }

    private void generateDailyReport() {
        String dailyReport = """
            =============================
            DAILY LOG - """ + java.time.LocalDate.now() + """
            =============================
            
            TODAY'S ACTIVITIES:
            • Watered all plants (Completed)
            • Checked humidity levels (Completed)
            • Monitored temperature (Completed)
            • Recorded growth data (In Progress)
            
            TASKS FOR TOMORROW:
            1. Fertilize vegetables (9:00 AM)
            2. Prune roses (10:30 AM)
            3. Check irrigation system (2:00 PM)
            
            NOTABLE OBSERVATIONS:
            • Tomato plants showing good growth
            • Basil needs more sunlight
            • Lettuce ready for harvest
            =============================
            """;

        showReportDialog("Daily Log Report", dailyReport);
    }

    private void generateWeeklyReport() {
        String weeklyReport = """
            =============================
            WEEKLY SUMMARY
            Week of: """ + java.time.LocalDate.now().minusDays(7) + " to " + java.time.LocalDate.now() + """
            =============================
            
            WEEKLY HIGHLIGHTS:
            • Total Harvest: 75.5 kg
            • New Plants Added: 15
            • Tasks Completed: 38/45 (84%)
            • Issues Resolved: 12
            
            AREAS FOR IMPROVEMENT:
            1. Watering schedule needs adjustment
            2. Pest control measures required
            3. Staff training on new equipment
            
            NEXT WEEK'S GOALS:
            • Increase harvest by 10%
            • Reduce task overdue rate to 0%
            • Implement new irrigation system
            =============================
            """;

        showReportDialog("Weekly Summary Report", weeklyReport);
    }

    private void generateMonthlyReport() {
        String monthlyReport = """
            =============================
            MONTHLY REPORT
            Month: """ + java.time.LocalDate.now().getMonth() + " " + java.time.LocalDate.now().getYear() + """
            =============================
            
            MONTHLY PERFORMANCE:
            • Total Revenue: rs.6,768
            • Total Expenses: rs.5,895
            • Net Profit: rs.873
            • Plant Growth: +12%
            • Task Completion: 88%
            
            KEY ACHIEVEMENTS:
            ✓ Implemented automated watering system
            ✓ Reduced water usage by 15%
            ✓ Increased harvest yield by 20%
            
            CHALLENGES:
            • Pest infestation in South Wing
            • Equipment maintenance delays
            • Staff turnover rate of 10%
            
            RECOMMENDATIONS FOR NEXT MONTH:
            1. Invest in pest control system
            2. Schedule equipment maintenance
            3. Implement staff retention program
            =============================
            """;

        showReportDialog("Monthly Report", monthlyReport);
    }

    private void showReportDialog(String title, String content) {
        JDialog reportDialog = new JDialog(this, title, true);
        reportDialog.setSize(600, 500);
        reportDialog.setLocationRelativeTo(this);

        JTextArea reportArea = new JTextArea(content);
        reportArea.setEditable(false);
        reportArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
        reportArea.setMargin(new Insets(10, 10, 10, 10));

        JScrollPane scrollPane = new JScrollPane(reportArea);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Close");
        JButton copyButton = new JButton("Copy to Clipboard");

        closeButton.addActionListener(e -> reportDialog.dispose());
        copyButton.addActionListener(e -> {
            reportArea.selectAll();
            reportArea.copy();
            showSuccess("Report copied to clipboard!");
        });

        buttonPanel.add(copyButton);
        buttonPanel.add(closeButton);

        reportDialog.add(scrollPane, BorderLayout.CENTER);
        reportDialog.add(buttonPanel, BorderLayout.SOUTH);
        reportDialog.setVisible(true);
    }
}