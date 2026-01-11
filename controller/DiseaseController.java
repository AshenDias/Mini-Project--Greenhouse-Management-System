package controller;

import model.DiseaseModel;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DiseaseController extends BaseController<DiseaseModel> {

    public boolean addDisease(String name, String symptoms, String severity, Integer plantId, String treatment) {
        try {
            int newId = getNextDiseaseId();

            DiseaseModel disease = new DiseaseModel(newId, name, symptoms, treatment, severity,
                    plantId != null ? plantId : 0);

            return add(disease);
        } catch (Exception e) {
            e.printStackTrace();
            showError("Error", "Failed to add disease: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteDisease(int diseaseId) {
        return delete(diseaseId);
    }

    private int getNextDiseaseId() {
        Connection conn = null;
        PreparedStatement stmt = null;
        ResultSet rs = null;
        int nextId = 1;

        try {
            conn = getConnection();
            String query = "SELECT MAX(Disease_ID) as max_id FROM Disease";
            stmt = conn.prepareStatement(query);
            rs = stmt.executeQuery();

            if (rs.next()) {
                nextId = rs.getInt("max_id") + 1;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            closeResources(rs, stmt, conn);
        }
        return nextId;
    }

    @Override
    public boolean add(DiseaseModel disease) {
        System.out.println("\n=== Adding Disease ===");
        System.out.println("ID: " + disease.getDiseaseId());
        System.out.println("Name: " + disease.getName());
        System.out.println("Symptoms: " + disease.getSymptoms());
        System.out.println("Treatment: " + disease.getTreatment());
        System.out.println("Severity: " + disease.getSeverity());
        System.out.println("Plant ID: " + disease.getAffectedPlantId());

        String query = "INSERT INTO disease (Disease_ID, name, symptoms, treatment, severity, affected_plant_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        String treatment = disease.getTreatment();
        if (treatment == null || treatment.trim().isEmpty()) {
            treatment = "No treatment specified";
            System.out.println("INFO: Treatment set to default: " + treatment);
        }

        String severity = disease.getSeverity();
        if (severity == null || severity.trim().isEmpty()) {
            severity = "Medium";
            System.out.println("INFO: Severity set to default: " + severity);
        }

        String symptoms = disease.getSymptoms();
        if (symptoms == null || symptoms.trim().isEmpty()) {
            symptoms = "No symptoms recorded";
            System.out.println("INFO: Symptoms set to default: " + symptoms);
        }

        Integer plantId = disease.getAffectedPlantId() > 0 ? disease.getAffectedPlantId() : null;

        boolean result = executeUpdate(query,
                disease.getDiseaseId(),
                disease.getName(),
                symptoms,
                treatment,
                severity,
                plantId
        );

        if (result) {
            showSuccess("Success", "Disease added successfully!");
        }

        return result;
    }

    @Override
    public boolean update(DiseaseModel disease) {
        System.out.println("\n=== Updating Disease ===");
        System.out.println("Updating Disease ID: " + disease.getDiseaseId());

        String query = "UPDATE disease SET name = ?, symptoms = ?, treatment = ?, " +
                "severity = ?, affected_plant_id = ? WHERE Disease_ID = ?";

        String treatment = disease.getTreatment();
        if (treatment == null || treatment.trim().isEmpty()) {
            treatment = "No treatment specified";
        }

        String severity = disease.getSeverity();
        if (severity == null || severity.trim().isEmpty()) {
            severity = "Medium";
        }

        String symptoms = disease.getSymptoms();
        if (symptoms == null || symptoms.trim().isEmpty()) {
            symptoms = "No symptoms recorded";
        }

        Integer plantId = disease.getAffectedPlantId() > 0 ? disease.getAffectedPlantId() : null;

        boolean result = executeUpdate(query,
                disease.getName(),
                symptoms,
                treatment,
                severity,
                plantId,
                disease.getDiseaseId()
        );

        if (result) {
            showSuccess("Success", "Disease updated successfully!");
        }

        return result;
    }

    @Override
    public boolean delete(int diseaseId) {
        System.out.println("\n=== Deleting Disease ===");
        System.out.println("Deleting Disease ID: " + diseaseId);

        String query = "DELETE FROM disease WHERE Disease_ID = ?";
        boolean result = executeUpdate(query, diseaseId);

        if (result) {
            showSuccess("Success", "Disease deleted successfully!");
        } else {
            showError("Delete Failed", "Could not delete disease. It may not exist.");
        }

        return result;
    }

    @Override
    public DiseaseModel getById(int diseaseId) {
        System.out.println("\n=== Getting Disease by ID ===");
        System.out.println("Fetching Disease ID: " + diseaseId);

        String query = "SELECT * FROM disease WHERE Disease_ID = ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        DiseaseModel disease = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, diseaseId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("Disease_ID");
                String name = rs.getString("name");
                String symptoms = rs.getString("symptoms");
                String treatment = rs.getString("treatment");
                String severity = rs.getString("severity");
                int plantId = rs.getInt("affected_plant_id");

                if (rs.wasNull()) {
                    plantId = 0;
                }

                disease = new DiseaseModel(id, name, symptoms, treatment, severity, plantId);
                System.out.println("✓ Found disease: " + name);
            } else {
                System.out.println("✗ No disease found with ID: " + diseaseId);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching disease: " + e.getMessage());
            e.printStackTrace();
            showError("Database Error", "Error fetching disease: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return disease;
    }

    @Override
    public List<DiseaseModel> getAll() {
        System.out.println("\n=== Getting All Diseases ===");

        List<DiseaseModel> diseases = new ArrayList<>();
        String query = "SELECT * FROM disease ORDER BY Disease_ID";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                int id = rs.getInt("Disease_ID");
                String name = rs.getString("name");
                String symptoms = rs.getString("symptoms");
                String treatment = rs.getString("treatment");
                String severity = rs.getString("severity");
                int plantId = rs.getInt("affected_plant_id");

                if (rs.wasNull()) {
                    plantId = 0;
                }

                DiseaseModel disease = new DiseaseModel(id, name, symptoms, treatment, severity, plantId);
                diseases.add(disease);
                count++;
            }
            System.out.println("✓ Found " + count + " diseases");
        } catch (SQLException e) {
            System.err.println("Error fetching diseases: " + e.getMessage());
            e.printStackTrace();
            showError("Database Error", "Error fetching diseases: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return diseases;
    }

    public List<DiseaseModel> searchBySymptoms(String symptom) {
        System.out.println("\n=== Searching Diseases by Symptom ===");
        System.out.println("Searching for: " + symptom);

        List<DiseaseModel> diseases = new ArrayList<>();
        String query = "SELECT * FROM disease WHERE symptoms LIKE ? ORDER BY Disease_ID";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, "%" + symptom + "%");
            rs = pstmt.executeQuery();

            int count = 0;
            while (rs.next()) {
                DiseaseModel disease = new DiseaseModel(
                        rs.getInt("Disease_ID"),
                        rs.getString("name"),
                        rs.getString("symptoms"),
                        rs.getString("treatment"),
                        rs.getString("severity"),
                        rs.getInt("affected_plant_id")
                );
                diseases.add(disease);
                count++;
            }
            System.out.println("✓ Found " + count + " diseases with symptom: " + symptom);
        } catch (SQLException e) {
            System.err.println("Error searching diseases: " + e.getMessage());
            e.printStackTrace();
            showError("Search Error", "Error searching diseases: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return diseases;
    }

    public void updateTableWithDisease(DefaultTableModel model, DiseaseModel disease) {
        model.setRowCount(0);

        if (disease != null) {
            model.addRow(new Object[]{
                    disease.getDiseaseId(),
                    disease.getName(),
                    disease.getSymptoms(),
                    disease.getTreatment(),
                    disease.getSeverity() != null ? disease.getSeverity() : "N/A",
                    disease.getAffectedPlantId() > 0 ? disease.getAffectedPlantId() : "N/A"
            });
        }
    }

    public boolean diseaseExists(int diseaseId) {
        System.out.println("\n=== Checking if Disease Exists ===");
        System.out.println("Checking Disease ID: " + diseaseId);

        boolean exists = recordExists("disease", "Disease_ID", diseaseId);
        System.out.println("Disease exists: " + exists);
        return exists;
    }
}