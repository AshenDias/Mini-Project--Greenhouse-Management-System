package controller;

import model.PlantModel;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Plantcontroller extends BaseController<PlantModel> {

    @Override
    public boolean add(PlantModel plant) {
        System.out.println("\n=== Adding Plant ===");
        System.out.println("ID: " + plant.getPlantId());
        System.out.println("Name: " + plant.getName());
        System.out.println("Water Amount: " + plant.getWaterAmount());
        System.out.println("Nutrient Amount: " + plant.getNutrientAmount());
        System.out.println("Plant Type: " + plant.getPlantType());
        System.out.println("Greenhouse ID: " + plant.getGreenhouseId());

        if (plantExists(plant.getPlantId())) {
            showError("Duplicate Error", "Plant with ID " + plant.getPlantId() + " already exists!");
            return false;
        }

        String query = "INSERT INTO Plant (Plant_ID, name, wateramount, nut_amount, plant_type, greenhouse_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        String plantType = plant.getPlantType();
        if (plantType == null || plantType.trim().isEmpty()) {
            plantType = "Unknown";
            System.out.println("INFO: Plant type set to default: " + plantType);
        }

        // Handle greenhouse ID - use null if 0
        Integer greenhouseId = plant.getGreenhouseId();
        if (greenhouseId != null && greenhouseId == 0) {
            greenhouseId = null;
            System.out.println("INFO: Greenhouse ID set to null (not assigned)");
        } else if (greenhouseId != null && greenhouseId > 0) {
            GreenhouseController greenhouseController = new GreenhouseController();
            if (greenhouseController.getById(greenhouseId) == null) {
                showError("Validation Error", "Greenhouse with ID " + greenhouseId + " does not exist!");
                return false;
            }
        }

        boolean result = executeUpdate(query,
                plant.getPlantId(),
                plant.getName(),
                plant.getWaterAmount(),
                plant.getNutrientAmount(),
                plantType,
                greenhouseId
        );

        if (result) {
            showSuccess("Success", "Plant added successfully!");
        }

        return result;
    }

    @Override
    public boolean update(PlantModel plant) {
        System.out.println("\n=== Updating Plant ===");
        System.out.println("Updating Plant ID: " + plant.getPlantId());
        if (!plantExists(plant.getPlantId())) {
            showError("Update Error", "Plant with ID " + plant.getPlantId() + " does not exist!");
            return false;
        }

        String query = "UPDATE Plant SET name = ?, wateramount = ?, nut_amount = ?, " +
                "plant_type = ?, greenhouse_id = ? WHERE Plant_ID = ?";

        // Handle null/empty values
        String plantType = plant.getPlantType();
        if (plantType == null || plantType.trim().isEmpty()) {
            plantType = "Unknown";
        }

        // Handle greenhouse ID - use null if 0
        Integer greenhouseId = plant.getGreenhouseId();
        if (greenhouseId != null && greenhouseId == 0) {
            greenhouseId = null;
        } else if (greenhouseId != null && greenhouseId > 0) {
            GreenhouseController greenhouseController = new GreenhouseController();
            if (greenhouseController.getById(greenhouseId) == null) {
                showError("Validation Error", "Greenhouse with ID " + greenhouseId + " does not exist!");
                return false;
            }
        }

        boolean result = executeUpdate(query,
                plant.getName(),
                plant.getWaterAmount(),
                plant.getNutrientAmount(),
                plantType,
                greenhouseId,
                plant.getPlantId()
        );

        if (result) {
            showSuccess("Success", "Plant updated successfully!");
        }

        return result;
    }

    @Override
    public boolean delete(int plantId) {
        System.out.println("\n=== Deleting Plant ===");
        System.out.println("Deleting Plant ID: " + plantId);
        if (!plantExists(plantId)) {
            showError("Delete Error", "Plant with ID " + plantId + " does not exist!");
            return false;
        }

        String query = "DELETE FROM Plant WHERE Plant_ID = ?";
        boolean result = executeUpdate(query, plantId);

        if (result) {
            showSuccess("Success", "Plant deleted successfully!");
        } else {
            showError("Delete Failed", "Could not delete plant. It may be referenced by other records.");
        }

        return result;
    }

    @Override
    public PlantModel getById(int plantId) {
        System.out.println("\n=== Getting Plant by ID ===");
        System.out.println("Fetching Plant ID: " + plantId);

        String query = "SELECT * FROM Plant WHERE Plant_ID = ?";
        PlantModel plant = null;

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, plantId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("Plant_ID");
                String name = rs.getString("name");
                double waterAmount = rs.getDouble("wateramount");
                double nutrientAmount = rs.getDouble("nut_amount");
                String plantType = rs.getString("plant_type");
                if (rs.wasNull()) {
                    plantType = null;
                }

                int greenhouseId = rs.getInt("greenhouse_id");
                if (rs.wasNull()) {
                    greenhouseId = 0;
                }

                plant = new PlantModel(id, name, waterAmount, nutrientAmount, plantType, greenhouseId);
                System.out.println("✓ Found plant: " + name);
            } else {
                System.out.println("✗ No plant found with ID: " + plantId);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching plant: " + e.getMessage());
            e.printStackTrace();
            showError("Database Error", "Error fetching plant: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return plant;
    }

    @Override
    public List<PlantModel> getAll() {
        System.out.println("\n=== Getting All Plants ===");

        List<PlantModel> plants = new ArrayList<>();
        String query = "SELECT * FROM Plant ORDER BY Plant_ID";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                int id = rs.getInt("Plant_ID");
                String name = rs.getString("name");
                double waterAmount = rs.getDouble("wateramount");
                double nutrientAmount = rs.getDouble("nut_amount");
                String plantType = rs.getString("plant_type");
                if (rs.wasNull()) {
                    plantType = null;
                }

                int greenhouseId = rs.getInt("greenhouse_id");
                if (rs.wasNull()) {
                    greenhouseId = 0;
                }

                PlantModel plant = new PlantModel(id, name, waterAmount, nutrientAmount, plantType, greenhouseId);
                plants.add(plant);
                count++;
            }
            System.out.println("✓ Found " + count + " plants");
        } catch (SQLException e) {
            System.err.println("Error fetching plants: " + e.getMessage());
            e.printStackTrace();
            showError("Database Error", "Error fetching plants: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return plants;
    }

    public void displayPlantsInTable(DefaultTableModel tableModel) {
        List<PlantModel> plants = getAll();
        tableModel.setRowCount(0);

        for (PlantModel plant : plants) {
            tableModel.addRow(new Object[]{
                    plant.getPlantId(),
                    plant.getName(),
                    plant.getWaterAmount(),
                    plant.getNutrientAmount(),
                    plant.getPlantType() != null ? plant.getPlantType() : "N/A",
                    plant.getGreenhouseId() > 0 ? plant.getGreenhouseId() : "N/A"
            });
        }
    }

    public List<PlantModel> searchByName(String name) {
        System.out.println("\n=== Searching Plants by Name ===");
        System.out.println("Searching for: " + name);

        List<PlantModel> plants = new ArrayList<>();
        String query = "SELECT * FROM Plant WHERE name LIKE ? ORDER BY Plant_ID";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, "%" + name + "%");
            rs = pstmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                int id = rs.getInt("Plant_ID");
                String plantName = rs.getString("name");
                double waterAmount = rs.getDouble("wateramount");
                double nutrientAmount = rs.getDouble("nut_amount");
                String plantType = rs.getString("plant_type");
                if (rs.wasNull()) {
                    plantType = null;
                }

                int greenhouseId = rs.getInt("greenhouse_id");
                if (rs.wasNull()) {
                    greenhouseId = 0;
                }

                PlantModel plant = new PlantModel(id, plantName, waterAmount, nutrientAmount, plantType, greenhouseId);
                plants.add(plant);
                count++;
            }
            System.out.println("✓ Found " + count + " plants with name containing: " + name);
        } catch (SQLException e) {
            System.err.println("Error searching plants: " + e.getMessage());
            e.printStackTrace();
            showError("Search Error", "Error searching plants: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return plants;
    }

    public List<PlantModel> getPlantsByGreenhouseId(int greenhouseId) {
        System.out.println("\n=== Getting Plants by Greenhouse ID ===");
        System.out.println("Greenhouse ID: " + greenhouseId);

        List<PlantModel> plants = new ArrayList<>();
        String query = "SELECT * FROM Plant WHERE greenhouse_id = ? ORDER BY Plant_ID";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, greenhouseId);
            rs = pstmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                int id = rs.getInt("Plant_ID");
                String name = rs.getString("name");
                double waterAmount = rs.getDouble("wateramount");
                double nutrientAmount = rs.getDouble("nut_amount");
                String plantType = rs.getString("plant_type");

                PlantModel plant = new PlantModel(id, name, waterAmount, nutrientAmount, plantType, greenhouseId);
                plants.add(plant);
                count++;
            }
            System.out.println("✓ Found " + count + " plants in greenhouse ID: " + greenhouseId);
        } catch (SQLException e) {
            System.err.println("Error fetching plants by greenhouse: " + e.getMessage());
            e.printStackTrace();
            showError("Database Error", "Error fetching plants by greenhouse: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return plants;
    }

    public boolean plantExists(int plantId) {
        System.out.println("\n=== Checking if Plant Exists ===");
        System.out.println("Checking Plant ID: " + plantId);

        boolean exists = recordExists("Plant", "Plant_ID", plantId);
        System.out.println("Plant exists: " + exists);
        return exists;
    }

    public List<PlantModel> getPlantsNeedingWatering(double threshold) {
        System.out.println("\n=== Getting Plants Needing Watering ===");
        System.out.println("Water threshold: " + threshold);

        List<PlantModel> plants = new ArrayList<>();
        String query = "SELECT * FROM Plant WHERE wateramount < ? ORDER BY wateramount";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(query);
            pstmt.setDouble(1, threshold);
            rs = pstmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                int id = rs.getInt("Plant_ID");
                String name = rs.getString("name");
                double waterAmount = rs.getDouble("wateramount");
                double nutrientAmount = rs.getDouble("nut_amount");
                String plantType = rs.getString("plant_type");
                int greenhouseId = rs.getInt("greenhouse_id");

                PlantModel plant = new PlantModel(id, name, waterAmount, nutrientAmount, plantType, greenhouseId);
                plants.add(plant);
                count++;
            }
            System.out.println("✓ Found " + count + " plants needing water (below " + threshold + ")");
        } catch (SQLException e) {
            System.err.println("Error fetching plants needing water: " + e.getMessage());
            e.printStackTrace();
            showError("Database Error", "Error fetching plants needing water: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return plants;
    }
}