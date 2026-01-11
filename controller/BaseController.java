package controller;

import javax.swing.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import model.BaseModel;

public abstract class BaseController<T extends BaseModel> {
    protected static final String DB_URL = "jdbc:mysql://localhost:3306/dias";
    protected static final String DB_USER = "root";
    protected static final String DB_PASSWORD = "asdf4444";

    static {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            System.out.println("MySQL JDBC Driver loaded successfully");
        } catch (ClassNotFoundException e) {
            System.err.println("MySQL JDBC Driver not found!");
            e.printStackTrace();
        }
    }

    protected Connection getConnection() throws SQLException {
        return DriverManager.getConnection(DB_URL, DB_USER, DB_PASSWORD);
    }

    protected void closeResources(AutoCloseable... resources) {
        for (AutoCloseable resource : resources) {
            if (resource != null) {
                try {
                    resource.close();
                } catch (Exception e) {
                    System.err.println("Error closing resource: " + e.getMessage());
                }
            }
        }
    }

    protected boolean executeUpdate(String query, Object... params) {
        System.out.println("\n=== DEBUG: Executing Update ===");
        System.out.println("Query: " + query);

        if (params != null) {
            System.out.println("Params count: " + params.length);
            for (int i = 0; i < params.length; i++) {
                System.out.println("  Param " + (i + 1) + ": " + params[i] +
                        " (Type: " + (params[i] != null ? params[i].getClass().getSimpleName() : "null") + ")");
            }
        }

        Connection conn = null;
        PreparedStatement pstmt = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(true);

            pstmt = conn.prepareStatement(query);

            // Set parameters
            if (params != null) {
                for (int i = 0; i < params.length; i++) {
                    if (params[i] == null) {
                        // Check if the parameter is for a number or string
                        if (query.toLowerCase().contains("wateramount") && i == 2) {
                            pstmt.setDouble(i + 1, 0.0);
                        } else if (query.toLowerCase().contains("nut_amount") && i == 3) {
                            pstmt.setDouble(i + 1, 0.0);
                        } else if (query.toLowerCase().contains("stock") && i == 2) {
                            pstmt.setInt(i + 1, 0);
                        } else if (query.toLowerCase().contains("number") && i == 3) {
                            pstmt.setInt(i + 1, 1);
                        } else {
                            pstmt.setNull(i + 1, Types.VARCHAR);
                        }
                    } else {
                        pstmt.setObject(i + 1, params[i]);
                    }
                }
            }

            int rowsAffected = pstmt.executeUpdate();
            System.out.println("✓ Rows affected: " + rowsAffected);

            return rowsAffected > 0;

        } catch (SQLException e) {
            System.err.println("✗ SQL Error executing query:");
            System.err.println("  Error Code: " + e.getErrorCode());
            System.err.println("  SQL State: " + e.getSQLState());
            System.err.println("  Message: " + e.getMessage());
            System.err.println("  Query: " + query);
            e.printStackTrace();

            String userMessage = "Database error: ";
            if (e.getMessage().contains("doesn't have a default value")) {
                userMessage = "Missing required field. Please fill all required fields.";
            } else if (e.getMessage().contains("Duplicate entry")) {
                userMessage = "Record with this ID already exists.";
            } else if (e.getMessage().contains("foreign key constraint fails")) {
                userMessage = "Referenced record does not exist (foreign key violation).";
            } else {
                userMessage = e.getMessage();
            }

            showError("Database Error", userMessage);
            return false;
        } finally {
            closeResources(pstmt, conn);
            System.out.println("=== DEBUG: Update Complete ===\n");
        }
    }

    protected ResultSet executeQuery(String query, Object... params) throws SQLException {
        Connection conn = getConnection();
        conn.setAutoCommit(true);
        PreparedStatement pstmt = conn.prepareStatement(query);

        if (params != null) {
            for (int i = 0; i < params.length; i++) {
                pstmt.setObject(i + 1, params[i]);
            }
        }

        return pstmt.executeQuery();
    }

    protected boolean recordExists(String table, String column, Object value) {
        String query = "SELECT COUNT(*) FROM " + table + " WHERE " + column + " = ?";

        System.out.println("Checking if record exists: " + query + " value=" + value);

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            conn.setAutoCommit(true);
            pstmt = conn.prepareStatement(query);
            pstmt.setObject(1, value);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                int count = rs.getInt(1);
                System.out.println("Record count: " + count);
                return count > 0;
            }
        } catch (SQLException e) {
            System.err.println("Error checking record existence: " + e.getMessage());
            e.printStackTrace();
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return false;
    }

    protected void showError(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.ERROR_MESSAGE);
    }

    protected void showSuccess(String title, String message) {
        JOptionPane.showMessageDialog(null, message, title, JOptionPane.INFORMATION_MESSAGE);
    }

    protected boolean showConfirm(String title, String message) {
        int response = JOptionPane.showConfirmDialog(null, message, title,
                JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
        return response == JOptionPane.YES_OPTION;
    }

    public abstract boolean add(T model);
    public abstract boolean update(T model);
    public abstract boolean delete(int id);
    public abstract T getById(int id);
    public abstract List<T> getAll();
}