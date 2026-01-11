package controller;

import model.InventoryModel;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class InventoryController extends BaseController<InventoryModel> {
    public boolean addInventoryItem(int greenhouseId, String description, int stock, int number,
                                    String category, double unitPrice) {
        InventoryModel inventory = new InventoryModel(greenhouseId, description, stock, number);
        inventory.setCategory(category);
        inventory.setUnitPrice(unitPrice);

        return add(inventory);
    }

    @Override
    public boolean add(InventoryModel inventory) {
        String query = "INSERT INTO Inventory (greenhouse_ID, Description, stock, number, category, unit_price) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try {
            return executeUpdate(query,
                    inventory.getGreenhouseId(),
                    inventory.getDescription(),
                    inventory.getStock(),
                    inventory.getNumber(),
                    inventory.getCategory(),
                    inventory.getUnitPrice()
            );
        } catch (Exception e) {
            e.printStackTrace();
            showError("Add Error", "Failed to add inventory item: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean update(InventoryModel inventory) {
        String query = "UPDATE Inventory SET Description = ?, stock = ?, number = ?, " +
                "category = ?, unit_price = ? WHERE greenhouse_ID = ? AND Description = ?";

        try {
            return executeUpdate(query,
                    inventory.getDescription(),
                    inventory.getStock(),
                    inventory.getNumber(),
                    inventory.getCategory(),
                    inventory.getUnitPrice(),
                    inventory.getGreenhouseId(),
                    inventory.getDescription()
            );
        } catch (Exception e) {
            e.printStackTrace();
            showError("Update Error", "Failed to update inventory item: " + e.getMessage());
            return false;
        }
    }

    @Override
    public boolean delete(int greenhouseId) {
        String query = "DELETE FROM Inventory WHERE greenhouse_ID = ?";

        try {
            return executeUpdate(query, greenhouseId);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Delete Error", "Failed to delete inventory item: " + e.getMessage());
            return false;
        }
    }

    @Override
    public InventoryModel getById(int greenhouseId) {
        String query = "SELECT * FROM Inventory WHERE greenhouse_ID = ?";
        InventoryModel inventory = null;

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, greenhouseId);
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
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", "Error fetching inventory: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return inventory;
    }

    @Override
    public List<InventoryModel> getAll() {
        List<InventoryModel> inventoryList = new ArrayList<>();
        String query = "SELECT * FROM Inventory ORDER BY greenhouse_ID, Description";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                InventoryModel inventory = new InventoryModel(
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

                inventoryList.add(inventory);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", "Error fetching inventory list: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return inventoryList;
    }

    public void updateTableModel(DefaultTableModel model) {
        List<InventoryModel> inventoryList = getAll();
        model.setRowCount(0);

        for (InventoryModel inventory : inventoryList) {
            model.addRow(new Object[]{
                    inventory.getGreenhouseId(),
                    inventory.getDescription(),
                    inventory.getStock(),
                    inventory.getNumber(),
                    inventory.getCategory() != null ? inventory.getCategory() : "N/A",
                    inventory.getUnitPrice() > 0 ? String.format("$%.2f", inventory.getUnitPrice()) : "N/A"
            });
        }
    }

    public List<InventoryModel> searchByDescription(String description) {
        List<InventoryModel> inventoryList = new ArrayList<>();
        String query = "SELECT * FROM Inventory WHERE Description LIKE ? ORDER BY greenhouse_ID, Description";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, "%" + description + "%");
            rs = pstmt.executeQuery();

            while (rs.next()) {
                InventoryModel inventory = new InventoryModel(
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

                inventoryList.add(inventory);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Search Error", "Error searching inventory: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return inventoryList;
    }
}