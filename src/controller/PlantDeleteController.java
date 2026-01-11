package controller;

import model.PlantModel;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class PlantDeleteController extends BaseController<PlantModel> {
    private Plantcontroller plantController;

    public PlantDeleteController() {
        this.plantController = new Plantcontroller();
    }

    public PlantDeleteController(Plantcontroller plantController) {
        this.plantController = plantController;
    }

    @Override
    public boolean add(PlantModel model) {
        showError("Operation Not Supported",
                "Add operation is not supported by Delete Controller. Use PlantController instead.");
        return false;
    }

    @Override
    public boolean update(PlantModel model) {
        showError("Operation Not Supported",
                "Update operation is not supported by Delete Controller. Use PlantController instead.");
        return false;
    }

    @Override
    public boolean delete(int plantId) {

        PlantModel plant = plantController.getById(plantId);
        if (plant == null) {
            showError("Delete Error", "Plant with ID " + plantId + " does not exist!");
            return false;
        }

        if (!canDeletePlant(plantId)) {
            showError("Delete Error",
                    "Cannot delete plant with ID " + plantId +
                            ". It is referenced in other records (diseases, tasks, or harvest).");
            return false;
        }

        String plantName = plant.getName() != null ? plant.getName() : "Unknown";
        if (!showConfirm("Confirm Delete",
                "Are you sure you want to delete plant '" + plantName + "' (ID: " + plantId + ")?")) {
            return false;
        }

        boolean success = plantController.delete(plantId);

        if (success) {
            showSuccess("Success", "Plant '" + plantName + "' deleted successfully!");
        } else {
            showError("Delete Error", "Failed to delete plant. Database error occurred.");
        }

        return success;
    }

    @Override
    public PlantModel getById(int id) {
        try {
            return plantController.getById(id);
        } catch (Exception e) {
            showError("Error", "Failed to get plant: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<PlantModel> getAll() {
        try {
            return plantController.getAll();
        } catch (Exception e) {
            showError("Error", "Failed to get plants: " + e.getMessage());
            return new ArrayList<>(); // Return empty list instead of null
        }
    }

    public boolean canDeletePlant(int plantId) {

        String[] checkQueries = {
                "SELECT COUNT(*) FROM Disease WHERE affected_plant_id = ?",
                "SELECT COUNT(*) FROM Task WHERE plant_id = ?",
                "SELECT COUNT(*) FROM Harvest WHERE plant_id = ?"
        };

        for (String query : checkQueries) {
            Connection conn = null;
            PreparedStatement pstmt = null;
            ResultSet rs = null;

            try {
                conn = getConnection();
                pstmt = conn.prepareStatement(query);
                pstmt.setInt(1, plantId);
                rs = pstmt.executeQuery();

                if (rs.next() && rs.getInt(1) > 0) {
                    return false;
                }
            } catch (SQLException e) {
                System.err.println("Warning: Could not check table for plant references: " + e.getMessage());
            } catch (Exception e) {

                System.err.println("Error checking plant references: " + e.getMessage());
            } finally {
                closeResources(rs, pstmt, conn);
            }
        }

        return true;
    }

    public boolean deleteByName(String plantName) {

        List<PlantModel> plants = plantController.searchByName(plantName);
        if (plants.isEmpty()) {
            showError("Delete Error", "Plant with name '" + plantName + "' does not exist!");
            return false;
        }

        if (plants.size() > 1) {
            showError("Delete Error",
                    "Multiple plants found with name '" + plantName + "'. Please use plant ID instead.");
            return false;
        }

        return delete(plants.get(0).getPlantId());
    }

    public boolean deleteMultiple(List<Integer> plantIds) {
        if (plantIds == null || plantIds.isEmpty()) {
            showError("Delete Error", "No plant IDs provided for deletion.");
            return false;
        }

        int successCount = 0;
        int failCount = 0;

        for (int plantId : plantIds) {
            if (delete(plantId)) {
                successCount++;
            } else {
                failCount++;
            }
        }

        if (failCount == 0) {
            showSuccess("Batch Delete",
                    "Successfully deleted " + successCount + " plant(s).");
            return true;
        } else {
            showError("Batch Delete",
                    "Deleted " + successCount + " plant(s), failed to delete " + failCount + " plant(s).");
            return false;
        }
    }

    public boolean softDelete(int plantId) {
        PlantModel plant = plantController.getById(plantId);
        if (plant == null) {
            showError("Delete Error", "Plant with ID " + plantId + " does not exist!");
            return false;
        }

        String plantName = plant.getName() != null ? plant.getName() : "Unknown";
        if (!showConfirm("Confirm Soft Delete",
                "Mark plant '" + plantName + "' (ID: " + plantId + ") as inactive?")) {
            return false;
        }

        String query = "UPDATE Plant SET active = 0 WHERE Plant_ID = ?";
        boolean success = executeUpdate(query, plantId);

        if (success) {
            showSuccess("Success", "Plant marked as inactive successfully!");
        } else {
            showError("Delete Error", "Failed to mark plant as inactive.");
        }

        return success;
    }
}