package controller;

import model.InventoryModel;
import javax.swing.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class InventoryClearController extends BaseController<InventoryModel> {
    private InventoryController inventoryController;

    public InventoryClearController() {
        this.inventoryController = new InventoryController();
    }

    public InventoryClearController(InventoryController inventoryController) {
        this.inventoryController = inventoryController;
    }

    public boolean clearDatabase() {
        if (!showConfirm("Confirm Clear",
                "Are you sure you want to clear ALL inventory data? This action cannot be undone!")) {
            return false;
        }

        String query = "DELETE FROM Inventory";
        boolean success = executeUpdate(query);

        if (success) {
            showSuccess("Success", "Inventory database cleared successfully!");
        } else {
            showError("Clear Error", "Failed to clear inventory database.");
        }

        return success;
    }

    public boolean clearByGreenhouseId(int greenhouseId) {
        if (!showConfirm("Confirm Delete",
                "Are you sure you want to delete all inventory for greenhouse ID " + greenhouseId + "?")) {
            return false;
        }

        String query = "DELETE FROM Inventory WHERE greenhouse_ID = ?";
        boolean success = executeUpdate(query, greenhouseId);

        if (success) {
            showSuccess("Success", "Inventory cleared for greenhouse ID " + greenhouseId);
        } else {
            showError("Clear Error", "Failed to clear inventory for greenhouse ID " + greenhouseId);
        }

        return success;
    }

    @Override
    public boolean add(InventoryModel model) {
        showError("Not Supported", "Add operation is not supported by Clear Controller");
        return false;
    }

    @Override
    public boolean update(InventoryModel model) {
        showError("Not Supported", "Update operation is not supported by Clear Controller");
        return false;
    }

    @Override
    public boolean delete(int id) {
        return clearByGreenhouseId(id);
    }

    @Override
    public InventoryModel getById(int id) {
        return inventoryController.getById(id);

        /*
        String query = "SELECT * FROM Inventory WHERE greenhouse_ID = ?";
        InventoryModel inventory = null;

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                inventory = new InventoryModel(
                    rs.getInt("greenhouse_ID"),
                    rs.getString("Description"),
                    rs.getInt("stock"),
                    rs.getInt("number")
                );
                try {
                    inventory.setCategory(rs.getString("category"));
                } catch (SQLException e) {
                }

                try {
                    inventory.setUnitPrice(rs.getDouble("unit_price"));
                } catch (SQLException e) {
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Database Error", "Error fetching inventory: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return inventory;
        */
    }

    @Override
    public List<InventoryModel> getAll() {
        return inventoryController.getAll();
    }

    public boolean clearByDescription(String description) {
        if (!showConfirm("Confirm Delete",
                "Are you sure you want to delete inventory items containing: " + description + "?")) {
            return false;
        }

        String query = "DELETE FROM Inventory WHERE Description LIKE ?";
        boolean success = executeUpdate(query, "%" + description + "%");

        if (success) {
            showSuccess("Success", "Inventory items deleted successfully");
        }

        return success;
    }

    public boolean backupAndClear() {
        if (!showConfirm("Backup and Clear",
                "This will backup inventory data before clearing. Continue?")) {
            return false;
        }

        String backupQuery = "CREATE TABLE Inventory_Backup_" + System.currentTimeMillis() +
                " AS SELECT * FROM Inventory";

        try {
            if (executeUpdate(backupQuery)) {
                return clearDatabase();
            }
        } catch (Exception e) {
            showError("Backup Failed", "Failed to create backup: " + e.getMessage());
        }

        return false;
    }

    public boolean clearLowStock(int minThreshold) {
        if (!showConfirm("Clear Low Stock",
                "Clear all items with stock less than " + minThreshold + "?")) {
            return false;
        }

        String query = "DELETE FROM Inventory WHERE stock < ?";
        boolean success = executeUpdate(query, minThreshold);

        if (success) {
            showSuccess("Success", "Low stock items cleared successfully");
        }

        return success;
    }
}