package controller;

import model.ToolModel;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ToolController extends BaseController<ToolModel> {

    @Override
    public boolean add(ToolModel tool) {
        String query = "INSERT INTO tool (toolname, connu, handovertime, tool_type, `condition`, last_maintenance) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        return executeUpdate(query,
                tool.getToolName(),
                tool.getQuantity(),
                tool.getHandoverTime(),
                tool.getToolType(),
                tool.getCondition(),
                tool.getLastMaintenance()
        );
    }

    @Override
    public boolean update(ToolModel tool) {
        String query = "UPDATE tool SET connu = ?, handovertime = ?, tool_type = ?, " +
                "`condition` = ?, last_maintenance = ? WHERE toolname = ?";

        return executeUpdate(query,
                tool.getQuantity(),
                tool.getHandoverTime(),
                tool.getToolType(),
                tool.getCondition(),
                tool.getLastMaintenance(),
                tool.getToolName()
        );
    }

    @Override
    public boolean delete(int id) {
        String query = "DELETE FROM tool WHERE id = ?";
        return executeUpdate(query, id);
    }

    public boolean deleteByName(String toolName) {
        if (toolName == null || toolName.trim().isEmpty()) {
            showError("Validation Error", "Tool name cannot be empty");
            return false;
        }

        String query = "DELETE FROM tool WHERE toolname = ?";
        return executeUpdate(query, toolName);
    }

    @Override
    public ToolModel getById(int id) {

        String query = "SELECT * FROM tool WHERE id = ?";
        ToolModel tool = null;

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                tool = createToolFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", "Error fetching tool by ID: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return tool;
    }

    public ToolModel getByName(String toolName) {
        if (toolName == null || toolName.trim().isEmpty()) {
            showError("Validation Error", "Tool name cannot be empty");
            return null;
        }

        String query = "SELECT * FROM tool WHERE toolname = ?";
        ToolModel tool = null;
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, toolName);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                tool = createToolFromResultSet(rs);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", "Error fetching tool: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return tool;
    }

    @Override
    public List<ToolModel> getAll() {
        List<ToolModel> tools = new ArrayList<>();
        String query = "SELECT * FROM tool";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                ToolModel tool = createToolFromResultSet(rs);
                tools.add(tool);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", "Error fetching tools: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return tools;
    }

    private ToolModel createToolFromResultSet(ResultSet rs) throws SQLException {
        ToolModel tool = new ToolModel(
                rs.getString("toolname"),
                rs.getInt("connu"),
                rs.getString("handovertime")
        );

        tool.setToolType(rs.getString("tool_type"));
        tool.setCondition(rs.getString("condition"));
        tool.setLastMaintenance(rs.getString("last_maintenance"));

        try {
            int id = rs.getInt("id");
            if (!rs.wasNull()) {
                tool.setId(id);
            }
        } catch (SQLException e) {
        }

        return tool;
    }

    public boolean borrowTool(String toolName, String borrower, String handoverTime) {
        ToolModel tool = getByName(toolName);
        if (tool == null) {
            showError("Borrow Error", "Tool '" + toolName + "' not found");
            return false;
        }

        tool.setHandoverTime(handoverTime);

        return update(tool);
    }

    public boolean returnTool(String toolName) {
        ToolModel tool = getByName(toolName);
        if (tool == null) {
            showError("Return Error", "Tool '" + toolName + "' not found");
            return false;
        }

        tool.setHandoverTime(null);
        return update(tool);
    }

    public boolean clearAllTools() {
        if (!showConfirm("Confirm Clear",
                "Are you sure you want to clear ALL tool records?")) {
            return false;
        }

        String query = "DELETE FROM tool";
        boolean success = executeUpdate(query);

        if (success) {
            showSuccess("Success", "All tools cleared successfully!");
        } else {
            showError("Clear Error", "Failed to clear tools.");
        }

        return success;
    }

    public void updateTableModel(DefaultTableModel model) {
        List<ToolModel> tools = getAll();
        model.setRowCount(0);

        for (ToolModel tool : tools) {
            model.addRow(new Object[]{
                    tool.getToolName(),
                    tool.getQuantity(),
                    tool.getHandoverTime() != null ? tool.getHandoverTime() : "N/A",
                    tool.getToolType() != null ? tool.getToolType() : "N/A",
                    tool.getCondition() != null ? tool.getCondition() : "Good",
                    tool.getLastMaintenance() != null ? tool.getLastMaintenance() : "N/A"
            });
        }
    }

    public List<ToolModel> getToolsByCondition(String condition) {
        List<ToolModel> tools = new ArrayList<>();
        String query = "SELECT * FROM tool WHERE `condition` = ?";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, condition);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                ToolModel tool = createToolFromResultSet(rs);
                tools.add(tool);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", "Error fetching tools by condition: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return tools;
    }

    public List<ToolModel> getToolsNeedingMaintenance() {
        List<ToolModel> tools = new ArrayList<>();

        String query = "SELECT * FROM tool WHERE last_maintenance IS NULL OR " +
                "last_maintenance < DATE_SUB(NOW(), INTERVAL 6 MONTH)";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                ToolModel tool = createToolFromResultSet(rs);
                tools.add(tool);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", "Error fetching tools needing maintenance: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return tools;
    }

    public boolean updateToolCondition(String toolName, String newCondition) {
        String query = "UPDATE tool SET `condition` = ? WHERE toolname = ?";
        boolean success = executeUpdate(query, newCondition, toolName);

        if (success) {
            showSuccess("Success", "Tool condition updated successfully!");
        } else {
            showError("Update Error", "Failed to update tool condition.");
        }

        return success;
    }

    public boolean recordMaintenance(String toolName, String maintenanceDate) {
        String query = "UPDATE tool SET last_maintenance = ? WHERE toolname = ?";
        boolean success = executeUpdate(query, maintenanceDate, toolName);

        if (success) {
            showSuccess("Success", "Maintenance recorded successfully!");
        } else {
            showError("Update Error", "Failed to record maintenance.");
        }

        return success;
    }

    public boolean toolExists(String toolName) {
        return recordExists("tool", "toolname", toolName);
    }

    public List<ToolModel> getAvailableTools() {
        List<ToolModel> tools = new ArrayList<>();

        String query = "SELECT * FROM tool WHERE handovertime IS NULL";
        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                ToolModel tool = createToolFromResultSet(rs);
                tools.add(tool);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", "Error fetching available tools: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return tools;
    }
}