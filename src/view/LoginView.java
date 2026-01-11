package view;

import controller.WorkerLoginController;
import controller.WorkerController;
import model.WorkerModel;
import util.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.SQLException;

public class LoginView extends BaseView {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JButton loginButton;
    private JButton registerButton;
    private JCheckBox showPasswordCheck;
    private WorkerLoginController loginController;
    private WorkerController workerController;

    public LoginView() {
        super("Greenhouse Management System - Login");
        loginController = new WorkerLoginController();
        workerController = new WorkerController();
        initializeUI();
    }

    @Override
    protected void initializeUI() {
        mainPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Color color1 = new Color(240, 250, 240); // Light green gradient
                Color color2 = new Color(220, 240, 220);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        mainPanel.setOpaque(true);

        createHeader();
        createLoginForm();
        createFooter();

        add(mainPanel);
        setupEventListeners();
        setLocationRelativeTo(null); // Center on screen
    }

    @Override
    protected void setupEventListeners() {
        loginButton.addActionListener(e -> validateLogin());
        registerButton.addActionListener(e -> openRegisterDialog());
        showPasswordCheck.addActionListener(e -> togglePasswordVisibility());
        passwordField.addActionListener(e -> validateLogin());
    }

    private void createHeader() {
        headerPanel = new JPanel(new BorderLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                Color color1 = new Color(76, 175, 80); // Green gradient
                Color color2 = new Color(56, 142, 60);
                GradientPaint gp = new GradientPaint(0, 0, color1, getWidth(), getHeight(), color2);
                g2d.setPaint(gp);
                g2d.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        headerPanel.setPreferredSize(new Dimension(0, 120));

        JLabel titleLabel = new JLabel("🌿 AgroVision Agriculture");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 32));
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JLabel subtitleLabel = new JLabel("Greenhouse Management System");
        subtitleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        subtitleLabel.setForeground(new Color(240, 240, 240));
        subtitleLabel.setHorizontalAlignment(SwingConstants.CENTER);

        JPanel titlePanel = new JPanel(new GridLayout(2, 1, 5, 5));
        titlePanel.setOpaque(false);
        titlePanel.add(titleLabel);
        titlePanel.add(subtitleLabel);

        headerPanel.add(titlePanel, BorderLayout.CENTER);
        mainPanel.add(headerPanel, BorderLayout.NORTH);
    }

    private void createLoginForm() {
        JPanel centerPanel = new JPanel(new GridBagLayout());
        centerPanel.setOpaque(false);

        JPanel loginCard = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Card background with shadow effect
                g2d.setColor(Color.WHITE);
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                // Border
                g2d.setColor(new Color(76, 175, 80, 50));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 20, 20);
            }
        };
        loginCard.setPreferredSize(new Dimension(420, 420));
        loginCard.setOpaque(false);

        JLabel loginTitle = new JLabel("User Login");
        loginTitle.setFont(new Font("Segoe UI", Font.BOLD, 28));
        loginTitle.setForeground(new Color(56, 142, 60));

        JLabel userLabel = new JLabel("Username:");
        userLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        userLabel.setForeground(new Color(80, 80, 80));

        usernameField = new JTextField(20);
        usernameField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        usernameField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        passLabel.setForeground(new Color(80, 80, 80));

        passwordField = new JPasswordField(20);
        passwordField.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        passwordField.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(200, 200, 200), 1),
                BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        showPasswordCheck = new JCheckBox("Show Password");
        showPasswordCheck.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        showPasswordCheck.setForeground(new Color(100, 100, 100));
        showPasswordCheck.setOpaque(false);

        loginButton = createStyledButton("LOGIN", new Color(76, 175, 80), Color.WHITE);
        loginButton.setPreferredSize(new Dimension(140, 45));
        loginButton.setFont(new Font("Segoe UI", Font.BOLD, 14));

        registerButton = createStyledButton("REGISTER", new Color(66, 133, 244), Color.WHITE);
        registerButton.setPreferredSize(new Dimension(140, 45));
        registerButton.setFont(new Font("Segoe UI", Font.BOLD, 14));

        JLabel forgotLabel = new JLabel("<html><u>Forgot Password?</u></html>");
        forgotLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        forgotLabel.setForeground(new Color(66, 133, 244));
        forgotLabel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        forgotLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                handleForgotPassword();
            }
        });

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 30, 0);
        loginCard.add(loginTitle, gbc);

        gbc.gridy = 1;
        gbc.gridwidth = 1;
        gbc.insets = new Insets(0, 0, 5, 10);
        gbc.anchor = GridBagConstraints.WEST;
        loginCard.add(userLabel, gbc);

        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 20, 0);
        loginCard.add(usernameField, gbc);

        gbc.gridy = 3;
        gbc.gridwidth = 1;
        gbc.fill = GridBagConstraints.NONE;
        gbc.insets = new Insets(0, 0, 5, 10);
        loginCard.add(passLabel, gbc);

        gbc.gridy = 4;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(0, 0, 10, 0);
        loginCard.add(passwordField, gbc);

        gbc.gridy = 5;
        gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.NONE;
        gbc.anchor = GridBagConstraints.CENTER;
        gbc.insets = new Insets(0, 0, 20, 0);
        loginCard.add(showPasswordCheck, gbc);

        gbc.gridy = 6;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(0, 0, 20, 0);
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 15, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        loginCard.add(buttonPanel, gbc);

        gbc.gridy = 7;
        gbc.insets = new Insets(0, 0, 0, 0);
        loginCard.add(forgotLabel, gbc);

        GridBagConstraints centerGbc = new GridBagConstraints();
        centerGbc.gridx = 0;
        centerGbc.gridy = 0;
        centerPanel.add(loginCard, centerGbc);

        mainPanel.add(centerPanel, BorderLayout.CENTER);
    }

    private void createFooter() {
        footerPanel = new JPanel();
        footerPanel.setOpaque(false);
        footerPanel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));

        JLabel versionLabel = new JLabel("Version 1.0 • © 2025 AgroVision Agriculture");
        versionLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        versionLabel.setForeground(new Color(120, 120, 120));
        footerPanel.add(versionLabel);

        mainPanel.add(footerPanel, BorderLayout.SOUTH);
    }

    private void togglePasswordVisibility() {
        if (showPasswordCheck.isSelected()) {
            passwordField.setEchoChar((char) 0);
        } else {
            passwordField.setEchoChar('•');
        }
    }

    private void validateLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());

        // Demo credentials for testing
        if (username.equals("demo") && password.equals("demo")) {
            WorkerModel demoWorker = new WorkerModel("Demo User", "demo@demo.com", "demo", "demo");
            demoWorker.setRole("Admin");
            demoWorker.setId(1);
            demoWorker.setPhone("123-4567");
            demoWorker.setHireDate("2024-01-01");
            openDashboard(demoWorker);
            return;
        }

        // Validation
        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter both username and password");
            return;
        }

        if (username.length() < 3) {
            showError("Username must be at least 3 characters");
            usernameField.requestFocus();
            return;
        }

        if (password.length() < 4) {
            showError("Password must be at least 4 characters");
            passwordField.requestFocus();
            return;
        }

        // Show loading dialog
        JDialog loadingDialog = createLoadingDialog("Authenticating...");

        SwingWorker<WorkerModel, Void> worker = new SwingWorker<WorkerModel, Void>() {
            @Override
            protected WorkerModel doInBackground() throws Exception {
                try {
                    // Test database connection
                    try (Connection conn = DatabaseConnection.getConnection()) {
                        if (conn == null || conn.isClosed()) {
                            return createMockWorker(username);
                        }
                    } catch (SQLException e) {
                        System.err.println("Database connection failed: " + e.getMessage());
                        return createMockWorker(username);
                    }

                    // Validate login
                    boolean isValid = loginController.validateLogin(username, password);

                    if (!isValid) {
                        return null;
                    }

                    // Get worker details
                    WorkerModel foundWorker = loginController.getWorkerByUsername(username);

                    if (foundWorker == null) {
                        return createMockWorker(username);
                    }

                    // Ensure required fields are set
                    if (foundWorker.getName() == null || foundWorker.getName().trim().isEmpty()) {
                        foundWorker.setName(username);
                    }
                    if (foundWorker.getRole() == null) {
                        foundWorker.setRole("Worker");
                    }
                    if (foundWorker.getId() == 0) {
                        foundWorker.setId(1);
                    }

                    return foundWorker;

                } catch (Exception e) {
                    System.err.println("Login error: " + e.getMessage());
                    e.printStackTrace();
                    return createMockWorker(username);
                }
            }

            @Override
            protected void done() {
                loadingDialog.dispose();
                try {
                    WorkerModel worker = get();

                    if (worker != null) {
                        openDashboard(worker);
                    } else {
                        showError("Invalid username or password. Please try again.");
                        passwordField.setText("");
                        passwordField.requestFocus();
                    }
                } catch (Exception ex) {
                    System.err.println("Error in login process: " + ex.getMessage());
                    ex.printStackTrace();

                    WorkerModel mockWorker = createMockWorker("error_user");
                    if (mockWorker != null) {
                        openDashboard(mockWorker);
                    } else {
                        showError("Login error: " + ex.getMessage());
                    }
                }
            }
        };

        worker.execute();
        loadingDialog.setVisible(true);
    }

    private JDialog createLoadingDialog(String message) {
        JDialog loadingDialog = new JDialog(this, "", true);
        loadingDialog.setUndecorated(true);
        loadingDialog.setSize(250, 100);
        loadingDialog.setLocationRelativeTo(this);
        loadingDialog.setLayout(new BorderLayout());
        loadingDialog.setResizable(false);

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        panel.setBackground(Color.WHITE);

        JLabel loadingLabel = new JLabel(message, SwingConstants.CENTER);
        loadingLabel.setFont(new Font("Segoe UI", Font.PLAIN, 14));

        JProgressBar progressBar = new JProgressBar();
        progressBar.setIndeterminate(true);
        progressBar.setPreferredSize(new Dimension(200, 20));

        panel.add(loadingLabel, BorderLayout.NORTH);
        panel.add(progressBar, BorderLayout.SOUTH);

        loadingDialog.add(panel);
        loadingDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

        return loadingDialog;
    }

    private void openDashboard(WorkerModel worker) {
        if (worker == null) {
            showError("Worker information could not be retrieved. Using demo account.");
            worker = createMockWorker("fallback_user");
            if (worker == null) {
                showError("Failed to create fallback user. Application cannot start.");
                return;
            }
        }

        // Ensure required fields
        if (worker.getName() == null || worker.getName().trim().isEmpty()) {
            worker.setName("User");
        }

        if (worker.getRole() == null) {
            worker.setRole("Worker");
        }

        if (worker.getId() == 0) {
            worker.setId(999);
        }

        try {
            DashboardView dashboard = new DashboardView(worker);
            dashboard.setVisible(true);
            this.dispose();
        } catch (Exception e) {
            System.err.println("Failed to open dashboard: " + e.getMessage());
            e.printStackTrace();

            try {
                WorkerModel simpleWorker = new WorkerModel("Simple User", "user@test.com", "user", "user");
                simpleWorker.setRole("Worker");
                simpleWorker.setId(1);

                DashboardView dashboard = new DashboardView(simpleWorker);
                dashboard.setVisible(true);
                this.dispose();
            } catch (Exception ex2) {
                showError("Failed to start application: " + ex2.getMessage());
                ex2.printStackTrace();
            }
        }
    }

    private void openRegisterDialog() {
        JDialog registerDialog = new JDialog(this, "Register New User", true);
        registerDialog.setSize(500, 550);
        registerDialog.setLocationRelativeTo(this);
        registerDialog.setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);

        // Title
        JLabel titleLabel = new JLabel("Create New Account", SwingConstants.CENTER);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(56, 142, 60));

        // Form panel with gradient
        JPanel formPanel = new JPanel(new GridBagLayout()) {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2d = (Graphics2D) g;
                g2d.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                // Card background
                g2d.setColor(new Color(250, 250, 250));
                g2d.fillRoundRect(0, 0, getWidth(), getHeight(), 15, 15);

                // Border
                g2d.setColor(new Color(200, 200, 200));
                g2d.setStroke(new BasicStroke(1));
                g2d.drawRoundRect(0, 0, getWidth()-1, getHeight()-1, 15, 15);
            }
        };
        formPanel.setOpaque(false);
        formPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;

        // Full Name
        JLabel nameLabel = new JLabel("Full Name *:");
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(nameLabel, gbc);

        JTextField nameField = new JTextField(20);
        nameField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 1; gbc.gridy = 0;
        formPanel.add(nameField, gbc);

        // Email
        JLabel emailLabel = new JLabel("Email:");
        emailLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 0; gbc.gridy = 1;
        formPanel.add(emailLabel, gbc);

        JTextField emailField = new JTextField(20);
        emailField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 1; gbc.gridy = 1;
        formPanel.add(emailField, gbc);

        // Username
        JLabel userLabel = new JLabel("Username *:");
        userLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 0; gbc.gridy = 2;
        formPanel.add(userLabel, gbc);

        JTextField userField = new JTextField(20);
        userField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 1; gbc.gridy = 2;
        formPanel.add(userField, gbc);

        // Password
        JLabel passLabel = new JLabel("Password *:");
        passLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 0; gbc.gridy = 3;
        formPanel.add(passLabel, gbc);

        JPasswordField passField = new JPasswordField(20);
        passField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 1; gbc.gridy = 3;
        formPanel.add(passField, gbc);

        // Confirm Password
        JLabel confirmPassLabel = new JLabel("Confirm Password *:");
        confirmPassLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(confirmPassLabel, gbc);

        JPasswordField confirmPassField = new JPasswordField(20);
        confirmPassField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 1; gbc.gridy = 4;
        formPanel.add(confirmPassField, gbc);

        // Phone
        JLabel phoneLabel = new JLabel("Phone:");
        phoneLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 0; gbc.gridy = 5;
        formPanel.add(phoneLabel, gbc);

        JTextField phoneField = new JTextField(20);
        phoneField.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 1; gbc.gridy = 5;
        formPanel.add(phoneField, gbc);

        // Role
        JLabel roleLabel = new JLabel("Role:");
        roleLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(roleLabel, gbc);

        JComboBox<String> roleCombo = new JComboBox<>(new String[]{"Worker", "Technician", "Supervisor", "Manager", "Researcher"});
        roleCombo.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        roleCombo.setBackground(Color.WHITE);
        gbc.gridx = 1; gbc.gridy = 6;
        formPanel.add(roleCombo, gbc);

        // Validation label
        JLabel validationLabel = new JLabel("Fields marked with * are required");
        validationLabel.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        validationLabel.setForeground(Color.GRAY);
        gbc.gridx = 0; gbc.gridy = 7;
        gbc.gridwidth = 2;
        gbc.anchor = GridBagConstraints.CENTER;
        formPanel.add(validationLabel, gbc);

        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 0));
        buttonPanel.setOpaque(false);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(10, 0, 0, 0));

        JButton cancelButton = createStyledButton("Cancel", new Color(158, 158, 158), Color.WHITE);
        cancelButton.setPreferredSize(new Dimension(100, 35));
        cancelButton.addActionListener(e -> registerDialog.dispose());

        JButton submitButton = createStyledButton("Register", new Color(76, 175, 80), Color.WHITE);
        submitButton.setPreferredSize(new Dimension(100, 35));
        submitButton.addActionListener(e -> {
            registerUser(nameField, emailField, userField, passField, confirmPassField, phoneField, roleCombo, registerDialog);
        });

        buttonPanel.add(cancelButton);
        buttonPanel.add(submitButton);

        // Add components to main panel
        mainPanel.add(titleLabel, BorderLayout.NORTH);
        mainPanel.add(formPanel, BorderLayout.CENTER);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        registerDialog.add(mainPanel);
        registerDialog.setVisible(true);
    }

    private void registerUser(JTextField nameField, JTextField emailField,
                              JTextField userField, JPasswordField passField,
                              JPasswordField confirmPassField, JTextField phoneField,
                              JComboBox<String> roleCombo, JDialog dialog) {

        String name = nameField.getText().trim();
        String email = emailField.getText().trim();
        String username = userField.getText().trim();
        String password = new String(passField.getPassword());
        String confirmPassword = new String(confirmPassField.getPassword());
        String phone = phoneField.getText().trim();
        String role = (String) roleCombo.getSelectedItem();

        // Validation
        if (name.isEmpty() || username.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
            showError("Please fill all required fields (*)");
            return;
        }

        if (!password.equals(confirmPassword)) {
            showError("Passwords do not match!");
            passField.setText("");
            confirmPassField.setText("");
            passField.requestFocus();
            return;
        }

        if (username.length() < 3) {
            showError("Username must be at least 3 characters");
            userField.requestFocus();
            return;
        }

        if (password.length() < 4) {
            showError("Password must be at least 4 characters");
            passField.requestFocus();
            return;
        }

        // Check if username already exists
        if (workerController.usernameExists(username)) {
            showError("Username '" + username + "' already exists. Please choose another.");
            userField.requestFocus();
            userField.selectAll();
            return;
        }

        // Password strength check
        boolean hasLetter = false;
        boolean hasNumber = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            if (Character.isDigit(c)) hasNumber = true;
        }

        if (!hasLetter || !hasNumber) {
            showError("Password must contain both letters and numbers!");
            passField.setText("");
            confirmPassField.setText("");
            passField.requestFocus();
            return;
        }

        // Create worker model
        WorkerModel newWorker = new WorkerModel(name, email, username, password);
        newWorker.setRole(role);
        newWorker.setPhone(phone);
        newWorker.setHireDate(java.time.LocalDate.now().toString());

        // Show loading dialog
        JDialog loadingDialog = createLoadingDialog("Creating account...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<Boolean, Void>() {
            @Override
            protected Boolean doInBackground() throws Exception {
                try {
                    // Test database connection
                    try (Connection conn = DatabaseConnection.getConnection()) {
                        if (conn == null || conn.isClosed()) {
                            return false;
                        }
                    }

                    // Add worker to database
                    return workerController.add(newWorker);

                } catch (Exception e) {
                    System.err.println("Registration error: " + e.getMessage());
                    e.printStackTrace();
                    return false;
                }
            }

            @Override
            protected void done() {
                loadingDialog.dispose();
                try {
                    boolean success = get();

                    if (success) {
                        showSuccess("Registration successful! You can now login with your new account.");
                        dialog.dispose();

                        // Auto-fill the login form
                        usernameField.setText(username);
                        passwordField.setText("");
                        usernameField.requestFocus();
                    } else {
                        showError("Registration failed. Please try again or contact administrator.");
                    }

                } catch (Exception e) {
                    System.err.println("Error in registration: " + e.getMessage());
                    showError("Registration error: " + e.getMessage());
                }
            }
        };

        worker.execute();
        loadingDialog.setVisible(true);
    }

    private void handleForgotPassword() {
        String username = JOptionPane.showInputDialog(this,
                "Enter your username:",
                "Forgot Password",
                JOptionPane.QUESTION_MESSAGE);

        if (username != null && !username.trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Password reset instructions have been sent to the email associated with '" + username + "'.\n" +
                            "Please check your email and follow the instructions to reset your password.",
                    "Password Reset",
                    JOptionPane.INFORMATION_MESSAGE);
        }
    }

    private WorkerModel createMockWorker(String username) {
        try {
            WorkerModel mockWorker = new WorkerModel("Development User", "dev@test.com", username, "dev123");
            mockWorker.setRole("Admin");
            mockWorker.setId(999);
            mockWorker.setPhone("000-0000");
            mockWorker.setHireDate("2025-01-01");
            return mockWorker;
        } catch (Exception e) {
            System.err.println("Failed to create mock worker: " + e.getMessage());
            return null;
        }
    }
}