package controller;

import model.taskModel;
import model.PlantModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class PlantUpdateController extends BaseController<PlantModel> {
    private Plantcontroller plantController;
    private Plantcontroller plantcontroller;

    public PlantUpdateController() {
        this.plantController = new Plantcontroller();
    }

    public PlantUpdateController(Plantcontroller plantController) {
        this.plantController = plantController;
    }

    public boolean checkPlantExists(int plantId, String plantName) {
        String query = "SELECT COUNT(*) FROM Plant WHERE Plant_ID = ? AND name = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, plantId);
            pstmt.setString(2, plantName);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", "Error checking plant existence: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return false;
    }

    public boolean updatePlant(taskModel plantTask) {

        if (!checkPlantExists(plantTask.getPlantId(), plantTask.getPlantName())) {
            showError("Update Error", "Plant does not exist!");
            return false;
        }

        try {
            double waterAmount = Double.parseDouble(plantTask.getWaterAmount());
            double nutrientAmount = Double.parseDouble(plantTask.getNutrientAmount());

            String query = "UPDATE Plant SET wateramount = ?, nut_amount = ? WHERE Plant_ID = ? AND name = ?";

            boolean success = executeUpdate(query,
                    waterAmount,
                    nutrientAmount,
                    plantTask.getPlantId(),
                    plantTask.getPlantName()
            );

            if (success) {
                showSuccess("Success", "Plant updated successfully!");
            } else {
                showError("Update Error", "Failed to update plant.");
            }

            return success;
        } catch (NumberFormatException e) {
            showError("Input Error", "Water and nutrient amounts must be valid numbers");
            return false;
        } catch (NullPointerException e) {
            showError("Input Error", "Plant task data is incomplete");
            return false;
        }
    }

    @Override
    public boolean add(PlantModel model) {
        showError("Not Supported", "Add operation is not supported by Update Controller");
        return false;
    }

    @Override
    public boolean update(PlantModel model) {
        String query = "UPDATE Plant SET name = ?, wateramount = ?, nut_amount = ?, " +
                "plant_type = ?, greenhouse_id = ? WHERE Plant_ID = ?";

        return executeUpdate(query,
                model.getName(),
                model.getWaterAmount(),
                model.getNutrientAmount(),
                model.getPlantType() != null ? model.getPlantType() : null,
                model.getGreenhouseId() > 0 ? model.getGreenhouseId() : null,
                model.getPlantId()
        );
    }

    @Override
    public boolean delete(int id) {
        showError("Not Supported", "Delete operation is not supported by Update Controller");
        return false;
    }

    @Override
    public PlantModel getById(int id) {
        String query = "SELECT * FROM Plant WHERE Plant_ID = ?";
        PlantModel plant = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                int plantId = rs.getInt("Plant_ID");
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

                plant = new PlantModel(plantId, name, waterAmount, nutrientAmount, plantType, greenhouseId);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", "Error fetching plant: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return plant;
    }

    @Override
    public List<PlantModel> getAll() {
        try {
            return plantController.getAll();
        } catch (Exception e) {
            showError("Error", "Failed to get plants: " + e.getMessage());
            return new ArrayList<>();
        }
    }

    public boolean updatePlantName(int plantId, String newName) {
        if (newName == null || newName.trim().isEmpty()) {
            showError("Validation Error", "Plant name cannot be empty");
            return false;
        }

        String query = "UPDATE Plant SET name = ? WHERE Plant_ID = ?";
        boolean success = executeUpdate(query, newName.trim(), plantId);

        if (success) {
            showSuccess("Success", "Plant name updated successfully!");
        }

        return success;
    }

    public boolean updateWaterAmount(int plantId, double waterAmount) {
        if (waterAmount <= 0) {
            showError("Validation Error", "Water amount must be positive");
            return false;
        }

        String query = "UPDATE Plant SET wateramount = ? WHERE Plant_ID = ?";
        boolean success = executeUpdate(query, waterAmount, plantId);

        if (success) {
            showSuccess("Success", "Plant water amount updated successfully!");
        }

        return success;
    }

    public boolean updateNutrientAmount(int plantId, double nutrientAmount) {
        if (nutrientAmount < 0) {
            showError("Validation Error", "Nutrient amount cannot be negative");
            return false;
        }

        String query = "UPDATE Plant SET nut_amount = ? WHERE Plant_ID = ?";
        boolean success = executeUpdate(query, nutrientAmount, plantId);

        if (success) {
            showSuccess("Success", "Plant nutrient amount updated successfully!");
        }

        return success;
    }

    public boolean updateGreenhouse(int plantId, int greenhouseId) {
        String query = "UPDATE Plant SET greenhouse_id = ? WHERE Plant_ID = ?";
        boolean success = executeUpdate(query, greenhouseId, plantId);

        if (success) {
            showSuccess("Success", "Plant greenhouse assignment updated!");
        }

        return success;
    }

    public boolean updatePlantType(int plantId, String plantType) {
        String query = "UPDATE Plant SET plant_type = ? WHERE Plant_ID = ?";
        boolean success = executeUpdate(query, plantType, plantId);

        if (success) {
            showSuccess("Success", "Plant type updated successfully!");
        }

        return success;
    }

    public boolean bulkUpdateWaterAmount(List<Integer> plantIds, double waterAmount) {
        if (plantIds == null || plantIds.isEmpty()) {
            showError("Validation Error", "No plant IDs provided");
            return false;
        }

        if (waterAmount <= 0) {
            showError("Validation Error", "Water amount must be positive");
            return false;
        }

        StringBuilder queryBuilder = new StringBuilder("UPDATE Plant SET wateramount = ? WHERE Plant_ID IN (");
        for (int i = 0; i < plantIds.size(); i++) {
            queryBuilder.append("?");
            if (i < plantIds.size() - 1) {
                queryBuilder.append(",");
            }
        }
        queryBuilder.append(")");

        Object[] params = new Object[plantIds.size() + 1];
        params[0] = waterAmount;
        for (int i = 0; i < plantIds.size(); i++) {
            params[i + 1] = plantIds.get(i);
        }

        boolean success = executeUpdate(queryBuilder.toString(), params);

        if (success) {
            showSuccess("Success", "Updated water amount for " + plantIds.size() + " plant(s)");
        }

        return success;
    }

    public boolean updatePlantFields(int plantId, java.util.Map<String, Object> updates) {
        if (updates == null || updates.isEmpty()) {
            showError("Validation Error", "No update fields provided");
            return false;
        }

        StringBuilder queryBuilder = new StringBuilder("UPDATE Plant SET ");
        List<Object> params = new ArrayList<>();
        int count = 0;

        for (java.util.Map.Entry<String, Object> entry : updates.entrySet()) {
            if (count > 0) {
                queryBuilder.append(", ");
            }
            queryBuilder.append(entry.getKey()).append(" = ?");
            params.add(entry.getValue());
            count++;
        }

        queryBuilder.append(" WHERE Plant_ID = ?");
        params.add(plantId);

        return executeUpdate(queryBuilder.toString(), params.toArray());
    }
}