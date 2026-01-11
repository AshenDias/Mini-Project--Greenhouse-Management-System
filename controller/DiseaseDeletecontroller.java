package controller;

import model.DiseaseModel;
import java.util.List;

public class DiseaseDeletecontroller extends BaseController<DiseaseModel> {
    private DiseaseController diseaseController;

    public DiseaseDeletecontroller() {
        this.diseaseController = new DiseaseController();
    }

    @Override
    public boolean add(DiseaseModel model) {
        showError("Operation Not Supported",
                "Add operation is not supported by Delete Controller. Use DiseaseController instead.");
        return false;
    }

    @Override
    public boolean update(DiseaseModel model) {
        showError("Operation Not Supported",
                "Update operation is not supported by Delete Controller. Use DiseaseController instead.");
        return false;
    }

    @Override
    public boolean delete(int diseaseId) {
        if (!recordExists("Disease", "Disease_ID", diseaseId)) {
            showError("Delete Error", "Disease with ID " + diseaseId + " does not exist!");
            return false;
        }

        if (!showConfirm("Confirm Delete",
                "Are you sure you want to delete disease with ID " + diseaseId + "?")) {
            return false;
        }

        String query = "DELETE FROM disease WHERE Disease_ID = ?";
        boolean success = executeUpdate(query, diseaseId);

        if (success) {
            showSuccess("Success", "Disease deleted successfully!");
        } else {
            showError("Delete Error", "Failed to delete disease.");
        }

        return success;
    }

    @Override
    public DiseaseModel getById(int id) {
        try {
            return diseaseController.getById(id);
        } catch (Exception e) {
            showError("Error", "Failed to get disease: " + e.getMessage());
            return null;
        }
    }

    @Override
    public List<DiseaseModel> getAll() {
        try {
            return diseaseController.getAll();
        } catch (Exception e) {
            showError("Error", "Failed to get diseases: " + e.getMessage());
            return new java.util.ArrayList<>();
        }
    }

}