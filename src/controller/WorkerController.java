package controller;

import model.WorkerModel;
import java.sql.ResultSet;
import java.util.List;

public class WorkerController extends BaseController<WorkerModel>{
    @Override
    public boolean add(WorkerModel worker) {

        if (!validateInput(worker.getUsername(), worker.getPassword())) {
            return false;
        }

        String query = "INSERT INTO worker (name, email, username, password, role, phone, hire_date) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?)";

        boolean success = executeUpdate(query,
                worker.getName(),
                worker.getEmail(),
                worker.getUsername(),
                worker.getPassword(),
                worker.getRole(),
                worker.getPhone(),
                worker.getHireDate()
        );

        if (success) {
            showSuccess("Success", "Worker registered successfully!");
        }

        return success;
    }

    @Override
    public boolean update(WorkerModel worker) {
        String query = "UPDATE worker SET name = ?, email = ?, role = ?, phone = ? WHERE username = ?";

        return executeUpdate(query,
                worker.getName(),
                worker.getEmail(),
                worker.getRole(),
                worker.getPhone(),
                worker.getUsername()
        );
    }

    @Override
    public boolean delete(int id) {
        String query = "DELETE FROM worker WHERE id = ?";
        return executeUpdate(query, id);
    }

    @Override
    public WorkerModel getById(int id) {
        String query = "SELECT * FROM worker WHERE id = ?";
        WorkerModel worker = null;

        try (ResultSet rs = executeQuery(query, id)) {
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
                worker.setId(rs.getInt("id"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return worker;
    }

    @Override
    public List<WorkerModel> getAll() {
        List<WorkerModel> workers = new java.util.ArrayList<>();
        String query = "SELECT * FROM worker";

        try (ResultSet rs = executeQuery(query)) {
            while (rs.next()) {
                WorkerModel worker = new WorkerModel(
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("username"),
                        rs.getString("password")
                );

                worker.setRole(rs.getString("role"));
                worker.setPhone(rs.getString("phone"));
                worker.setHireDate(rs.getString("hire_date"));
                worker.setId(rs.getInt("id"));

                workers.add(worker);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return workers;
    }

    public boolean validateInput(String username, String password) {
        if (username == null || password == null ||
                username.length() < 5 || password.length() < 5) {
            showError("Validation Error",
                    "Username and password must be at least 5 characters long!");
            return false;
        }

        boolean hasLetter = false;
        boolean hasNumber = false;

        for (char c : username.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            if (Character.isDigit(c)) hasNumber = true;
        }

        if (!hasLetter || !hasNumber) {
            showError("Validation Error",
                    "Username must contain both letters and numbers!");
            return false;
        }

        hasLetter = false;
        hasNumber = false;

        for (char c : password.toCharArray()) {
            if (Character.isLetter(c)) hasLetter = true;
            if (Character.isDigit(c)) hasNumber = true;
        }

        if (!hasLetter || !hasNumber) {
            showError("Validation Error",
                    "Password must contain both letters and numbers!");
            return false;
        }

        return true;
    }

    public boolean usernameExists(String username) {
        return recordExists("Worker", "username", username);
    }
}
