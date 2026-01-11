package controller;

import model.GreenhouseModel;
import model.harvestModel;
import model.taskModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GreenhouseController extends BaseController<GreenhouseModel> {

    private Plantcontroller plantController;

    public GreenhouseController() {
        this.plantController = new Plantcontroller();
    }

    @Override
    public boolean add(GreenhouseModel greenhouse) {
        String query = "INSERT INTO greenhouse (greenhouse_Id, name, location, temperature) VALUES (?, ?, ?, ?)";

        return executeUpdate(query,
                greenhouse.getId(),
                greenhouse.getName(),
                greenhouse.getLocation(),
                greenhouse.getTemperature()
        );
    }

    @Override
    public boolean update(GreenhouseModel greenhouse) {
        String query = "UPDATE greenhouse SET name = ?, location = ?, temperature = ? WHERE greenhouse_id = ?";

        return executeUpdate(query,
                greenhouse.getName(),
                greenhouse.getLocation(),
                greenhouse.getTemperature(),
                greenhouse.getId()
        );
    }

    @Override
    public boolean delete(int id) {
        String query = "DELETE FROM greenhouse WHERE greenhouse_id = ?";
        return executeUpdate(query, id);
    }

    @Override
    public GreenhouseModel getById(int id) {
        String query = "SELECT * FROM greenhouse WHERE greenhouse_id = ?";
        GreenhouseModel greenhouse = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                greenhouse = new GreenhouseModel(
                        rs.getInt("greenhouse_id"),
                        rs.getString("name"),
                        rs.getString("location"),
                        rs.getString("temperature")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", "Error fetching greenhouse: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return greenhouse;
    }

    @Override
    public List<GreenhouseModel> getAll() {
        List<GreenhouseModel> greenhouses = new ArrayList<>();
        String query = "SELECT * FROM Greenhouse";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                GreenhouseModel greenhouse = new GreenhouseModel(
                        rs.getInt("greenhouse_id"),
                        rs.getString("name"),
                        rs.getString("location"),
                        rs.getString("temperature")
                );
                greenhouses.add(greenhouse);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", "Error fetching greenhouses: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return greenhouses;
    }

    public boolean insertPlantTask(taskModel plant) {

        try {
            double waterAmount = Double.parseDouble(plant.getWaterAmount());
            double nutrientAmount = Double.parseDouble(plant.getNutrientAmount());

            String query = "INSERT INTO Plant (Plant_ID, name, wateramount, nut_amount) VALUES (?, ?, ?, ?)";

            return executeUpdate(query,
                    plant.getPlantId(),
                    plant.getPlantName(),
                    waterAmount,
                    nutrientAmount
            );
        } catch (NumberFormatException e) {
            showError("Input Error", "Water and nutrient amounts must be numbers");
            return false;
        }
    }

    public boolean enterHarvest(harvestModel inventory) {

        String query = "INSERT INTO Inventory (greenhouse_ID, Description, stock) VALUES (?, ?, ?)";

        try {
            int stock = Integer.parseInt(inventory.getStock1());

            return executeUpdate(query,
                    inventory.getGreenhouseId(),
                    inventory.getDescription(),
                    stock
            );
        } catch (NumberFormatException e) {
            showError("Input Error", "Stock must be a number");
            return false;
        }
    }

    public boolean checkGreenhouseExists(int greenhouseId, String name) {

        String query = "SELECT COUNT(*) FROM Greenhouse WHERE greenhouse_id = ? OR name = ?";

        try (Connection conn = getConnection();
             PreparedStatement pstmt = conn.prepareStatement(query)) {

            pstmt.setInt(1, greenhouseId);
            pstmt.setString(2, name);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return false;
    }

    public List<model.PlantModel> getPlantsInGreenhouse(int greenhouseId) {
        return new ArrayList<>();
    }
}