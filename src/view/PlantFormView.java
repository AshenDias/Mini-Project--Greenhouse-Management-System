package view;

import model.PlantModel;
import controller.Plantcontroller;
import util.DatabaseConnection;
import javax.swing.*;
import java.awt.*;
import java.sql.*;

public class PlantFormView extends JDialog {
    private PlantModel plant;
    private Plantview parentView;
    private boolean isEditMode;

    private JTextField idField, nameField, waterField, nutrientField;
    private JComboBox<String> typeCombo, greenhouseCombo;
    private JTextArea notesArea;
    private JButton saveButton, cancelButton;
    private Plantcontroller plantController;

    public PlantFormView(PlantModel plant, Plantview parentView) {
        super(parentView, plant == null ? "Add New Plant" : "Edit Plant", true);
        this.plant = plant;
        this.parentView = parentView;
        this.isEditMode = plant != null;
        this.plantController = new Plantcontroller();

        initializeUI();
        if (isEditMode) {
            loadPlantData();
        }
    }

    private void initializeUI() {
        setSize(500, 550);
        setLocationRelativeTo(parentView);
        setLayout(new BorderLayout());

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel titleLabel = new JLabel(isEditMode ? "✏️ Edit Plant" : "➕ Add New Plant");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 20));
        titleLabel.setForeground(new Color(46, 125, 50));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        titleLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createTitledBorder("Plant Information"),
                BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.insets = new Insets(5, 5, 5, 5);

        // Plant ID
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Plant ID:*"), gbc);

        idField = new JTextField();
        idField.setEnabled(!isEditMode);
        gbc.gridx = 1;
        formPanel.add(idField, gbc);

        JButton autoIdButton = new JButton("Auto-generate");
        autoIdButton.addActionListener(e -> generatePlantId());
        gbc.gridx = 2;
        gbc.fill = GridBagConstraints.NONE;
        formPanel.add(autoIdButton, gbc);

        // Plant Name
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        formPanel.add(new JLabel("Plant Name:*"), gbc);

        nameField = new JTextField();
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(nameField, gbc);

        // Plant Type
        gbc.gridx = 0; gbc.gridy = 2; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Plant Type:"), gbc);

        typeCombo = new JComboBox<>(new String[]{"Vegetable", "Flower", "Herb", "Fruit", "Other"});
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(typeCombo, gbc);

        // Water Amount
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Water Amount (L/day):*"), gbc);

        waterField = new JTextField();
        waterField.setText("1.0");
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(waterField, gbc);

        // Nutrients
        gbc.gridx = 0; gbc.gridy = 4;
        formPanel.add(new JLabel("Nutrients (g/day):*"), gbc);

        nutrientField = new JTextField();
        nutrientField.setText("0.5");
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(nutrientField, gbc);

        // Greenhouse
        gbc.gridx = 0; gbc.gridy = 5; gbc.gridwidth = 1;
        formPanel.add(new JLabel("Greenhouse:"), gbc);

        // Load greenhouses from database
        greenhouseCombo = new JComboBox<>();
        loadGreenhouses();
        gbc.gridx = 1; gbc.gridwidth = 2;
        formPanel.add(greenhouseCombo, gbc);

        // Notes
        gbc.gridx = 0; gbc.gridy = 6;
        formPanel.add(new JLabel("Notes:"), gbc);

        notesArea = new JTextArea(3, 20);
        notesArea.setLineWrap(true);
        JScrollPane notesScroll = new JScrollPane(notesArea);
        gbc.gridx = 1; gbc.gridwidth = 2;
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weighty = 1.0;
        formPanel.add(notesScroll, gbc);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        saveButton = new JButton(isEditMode ? "Update Plant" : "Save Plant");
        saveButton.setBackground(new Color(46, 125, 50));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 12));

        cancelButton = new JButton("Cancel");
        cancelButton.setBackground(new Color(108, 117, 125));
        cancelButton.setForeground(Color.WHITE);

        buttonPanel.add(cancelButton);
        buttonPanel.add(saveButton);

        mainPanel.add(titleLabel);
        mainPanel.add(formPanel);
        mainPanel.add(Box.createVerticalStrut(20));
        mainPanel.add(buttonPanel);

        add(mainPanel, BorderLayout.CENTER);
        setupListeners();

        // Generate ID if adding new plant
        if (!isEditMode) {
            generatePlantId();
        }
    }

    private void generatePlantId() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            // Get the maximum plant ID and add 1
            String query = "SELECT COALESCE(MAX(Plant_ID), 100) + 1 as next_id FROM Plant";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            if (rs.next()) {
                int nextId = rs.getInt("next_id");
                idField.setText(String.valueOf(nextId));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            // Generate random ID if database fails
            idField.setText(String.valueOf(1000 + (int)(Math.random() * 9000)));
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
    }

    private void loadGreenhouses() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;

        try {
            conn = DatabaseConnection.getConnection();
            String query = "SELECT greenhouse_id, name FROM Greenhouse ORDER BY name";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            greenhouseCombo.addItem("Not Assigned");
            while (rs.next()) {
                greenhouseCombo.addItem(rs.getString("name") + " [" + rs.getInt("greenhouse_id") + "]");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            // Add default options if database fails
            greenhouseCombo.addItem("Main Greenhouse [1]");
            greenhouseCombo.addItem("Research Greenhouse [2]");
            greenhouseCombo.addItem("Tropical Greenhouse [3]");
        } finally {
            try { if (rs != null) rs.close(); } catch (SQLException e) {}
            try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
            DatabaseConnection.closeConnection(conn);
        }
    }

    private void loadPlantData() {
        if (plant != null) {
            idField.setText(String.valueOf(plant.getPlantId()));
            nameField.setText(plant.getName());
            waterField.setText(String.valueOf(plant.getWaterAmount()));
            nutrientField.setText(String.valueOf(plant.getNutrientAmount()));

            if (plant.getPlantType() != null) {
                typeCombo.setSelectedItem(plant.getPlantType());
            }
        }
    }

    private void setupListeners() {
        saveButton.addActionListener(e -> savePlant());
        cancelButton.addActionListener(e -> dispose());
    }

    private void savePlant() {
        if (!validateForm()) {
            return;
        }

        try {
            int plantId = Integer.parseInt(idField.getText().trim());
            String name = nameField.getText().trim();
            double waterAmount = Double.parseDouble(waterField.getText().trim());
            double nutrientAmount = Double.parseDouble(nutrientField.getText().trim());
            String plantType = (String) typeCombo.getSelectedItem();

            // Parse greenhouse ID from selection
            int greenhouseId = 0;
            String greenhouseSelection = (String) greenhouseCombo.getSelectedItem();
            if (greenhouseSelection != null && !greenhouseSelection.equals("Not Assigned")) {
                // Extract ID from format "Name [ID]"
                int start = greenhouseSelection.indexOf("[");
                int end = greenhouseSelection.indexOf("]");
                if (start != -1 && end != -1) {
                    String idStr = greenhouseSelection.substring(start + 1, end);
                    greenhouseId = Integer.parseInt(idStr);
                }
            }

            // Create PlantModel object
            PlantModel newPlant = new PlantModel();
            newPlant.setPlantId(plantId);
            newPlant.setName(name);
            newPlant.setWaterAmount(waterAmount);
            newPlant.setNutrientAmount(nutrientAmount);
            newPlant.setPlantType(plantType);
            if (greenhouseId > 0) {
                newPlant.setGreenhouseId(greenhouseId);
            }

            Connection conn = null;
            PreparedStatement stmt = null;

            try {
                conn = DatabaseConnection.getConnection();

                if (isEditMode) {
                    // Update existing plant
                    String query = "UPDATE Plant SET name = ?, wateramount = ?, nut_amount = ?, " +
                            "plant_type = ?, greenhouse_id = ? WHERE Plant_ID = ?";
                    stmt = conn.prepareStatement(query);
                    stmt.setString(1, name);
                    stmt.setDouble(2, waterAmount);
                    stmt.setDouble(3, nutrientAmount);
                    stmt.setString(4, plantType);
                    if (greenhouseId > 0) {
                        stmt.setInt(5, greenhouseId);
                    } else {
                        stmt.setNull(5, Types.INTEGER);
                    }
                    stmt.setInt(6, plantId);

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(this,
                                "Plant updated successfully!",
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        parentView.refreshTable();
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Failed to update plant. Plant may not exist.",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                } else {
                    // Insert new plant
                    // Check if plant ID already exists
                    String checkQuery = "SELECT COUNT(*) as count FROM Plant WHERE Plant_ID = ?";
                    PreparedStatement checkStmt = conn.prepareStatement(checkQuery);
                    checkStmt.setInt(1, plantId);
                    ResultSet rs = checkStmt.executeQuery();

                    if (rs.next() && rs.getInt("count") > 0) {
                        JOptionPane.showMessageDialog(this,
                                "Plant ID " + plantId + " already exists. Please use a different ID.",
                                "Duplicate ID",
                                JOptionPane.ERROR_MESSAGE);
                        checkStmt.close();
                        return;
                    }
                    checkStmt.close();

                    // Insert the new plant
                    String query = "INSERT INTO Plant (Plant_ID, name, wateramount, nut_amount, " +
                            "plant_type, greenhouse_id, active) VALUES (?, ?, ?, ?, ?, ?, 1)";
                    stmt = conn.prepareStatement(query);
                    stmt.setInt(1, plantId);
                    stmt.setString(2, name);
                    stmt.setDouble(3, waterAmount);
                    stmt.setDouble(4, nutrientAmount);
                    stmt.setString(5, plantType);
                    if (greenhouseId > 0) {
                        stmt.setInt(6, greenhouseId);
                    } else {
                        stmt.setNull(6, Types.INTEGER);
                    }

                    int rowsAffected = stmt.executeUpdate();
                    if (rowsAffected > 0) {
                        JOptionPane.showMessageDialog(this,
                                "Plant added successfully!\nID: " + plantId + "\nName: " + name,
                                "Success",
                                JOptionPane.INFORMATION_MESSAGE);
                        parentView.refreshTable();
                        dispose();
                    } else {
                        JOptionPane.showMessageDialog(this,
                                "Failed to add plant",
                                "Error",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this,
                        "Database error: " + ex.getMessage(),
                        "Error",
                        JOptionPane.ERROR_MESSAGE);
            } finally {
                try { if (stmt != null) stmt.close(); } catch (SQLException e) {}
                DatabaseConnection.closeConnection(conn);
            }

        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                    "Please enter valid numbers for ID, water amount, and nutrients",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error: " + ex.getMessage(),
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    private boolean validateForm() {
        // Validate Plant ID
        if (idField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Plant ID is required",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            idField.requestFocus();
            return false;
        }

        try {
            int plantId = Integer.parseInt(idField.getText().trim());
            if (plantId <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Plant ID must be a positive number",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                idField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Plant ID must be a number",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            idField.requestFocus();
            return false;
        }

        // Validate Plant Name
        if (nameField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Plant name is required",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            nameField.requestFocus();
            return false;
        }

        // Validate Water Amount
        if (waterField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Water amount is required",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            waterField.requestFocus();
            return false;
        }

        try {
            double waterAmount = Double.parseDouble(waterField.getText().trim());
            if (waterAmount <= 0) {
                JOptionPane.showMessageDialog(this,
                        "Water amount must be greater than 0",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                waterField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Water amount must be a number",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            waterField.requestFocus();
            return false;
        }

        // Validate Nutrients
        if (nutrientField.getText().trim().isEmpty()) {
            JOptionPane.showMessageDialog(this,
                    "Nutrient amount is required",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            nutrientField.requestFocus();
            return false;
        }

        try {
            double nutrientAmount = Double.parseDouble(nutrientField.getText().trim());
            if (nutrientAmount < 0) {
                JOptionPane.showMessageDialog(this,
                        "Nutrient amount cannot be negative",
                        "Validation Error",
                        JOptionPane.ERROR_MESSAGE);
                nutrientField.requestFocus();
                return false;
            }
        } catch (NumberFormatException e) {
            JOptionPane.showMessageDialog(this,
                    "Nutrient amount must be a number",
                    "Validation Error",
                    JOptionPane.ERROR_MESSAGE);
            nutrientField.requestFocus();
            return false;
        }

        return true;
    }
}