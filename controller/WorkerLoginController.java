package controller;

import model.WorkerModel;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class WorkerLoginController extends BaseController<WorkerModel> {
    private static final Logger logger = Logger.getLogger(WorkerLoginController.class.getName());

    public boolean validateLogin(String username, String password) {
        String query = "SELECT * FROM worker WHERE username = ? AND password = ?";

        try (ResultSet rs = executeQuery(query, username, password)) {
            if (rs.next()) {
                showSuccess("Login Successful", "Welcome, " + rs.getString("name") + "!");
                return true;
            } else {
                showError("Login Failed", "Invalid username or password!");
                return false;
            }
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Database error during login", e);
            showError("Database Error", "Error connecting to database: " + e.getMessage());
            return false;
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Unexpected error during login", e);
            showError("Login Error", "An unexpected error occurred: " + e.getMessage());
            return false;
        }
    }

    public WorkerModel getWorkerByUsername(String username) {
        String query = "SELECT * FROM worker WHERE username = ?";
        WorkerModel worker = null;

        try (ResultSet rs = executeQuery(query, username)) {
            if (rs.next()) {
                worker = new WorkerModel(
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("username"),
                        rs.getString("password")
                );

                worker.setRole(rs.getString("role"));
                worker.setPhone(rs.getString("phone"));
                worker.setHireDate(rs.getString("hire_date"));

                try {
                    worker.setId(rs.getInt("id"));
                } catch (SQLException e1) {
                    try {
                        worker.setId(rs.getInt("worker_id"));
                    } catch (SQLException e2) {
                        try {
                            worker.setId(rs.getInt("ID"));
                        } catch (SQLException e3) {
                            worker.setId(0);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error getting worker by username", e);
        }

        return worker;
    }

    @SuppressWarnings("unused")
    public boolean changePassword(String username, String oldPassword, String newPassword) {
        if (!validateLogin(username, oldPassword)) {
            showError("Password Change", "Old password is incorrect!");
            return false;
        }

        WorkerController workerController = new WorkerController();
        if (!workerController.validateInput(username, newPassword)) {
            return false;
        }

        String query = "UPDATE worker SET password = ? WHERE username = ?";
        boolean success = executeUpdate(query, newPassword, username);

        if (success) {
            showSuccess("Password Change", "Password updated successfully!");
        } else {
            showError("Password Change", "Failed to update password.");
        }

        return success;
    }

    @Override
    public boolean add(WorkerModel model) {
        return false;
    }

    @Override
    public boolean update(WorkerModel model) {
        return false;
    }

    @Override
    public boolean delete(int id) {
        return false;
    }

    @Override
    public WorkerModel getById(int id) {
        WorkerController workerController = new WorkerController();
        return workerController.getById(id);
    }

    @Override
    public List<WorkerModel> getAll() {
        WorkerController workerController = new WorkerController();
        return workerController.getAll();
    }
}