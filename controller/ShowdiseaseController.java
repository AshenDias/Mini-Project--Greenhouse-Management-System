package controller;

import model.DiseaseSymptomModel;
import javax.swing.table.DefaultTableModel;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ShowdiseaseController extends BaseController<DiseaseSymptomModel> {

    @Override
    public boolean add(DiseaseSymptomModel model) {
        showError("Operation Not Supported",
                "Add operation is not supported by Show Disease Controller. Use DiseaseController instead.");
        return false;
    }

    @Override
    public boolean update(DiseaseSymptomModel model) {
        showError("Operation Not Supported",
                "Update operation is not supported by Show Disease Controller. Use DiseaseController instead.");
        return false;
    }

    @Override
    public boolean delete(int id) {
        showError("Operation Not Supported",
                "Delete operation is not supported by Show Disease Controller. Use DiseaseController instead.");
        return false;
    }

    @Override
    public DiseaseSymptomModel getById(int diseaseId) {
        String query = "SELECT Disease_ID, symptoms FROM Disease WHERE Disease_ID = ?";
        DiseaseSymptomModel disease = null;

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(query);
            pstmt.setInt(1, diseaseId);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                int id = rs.getInt("Disease_ID");
                String symptoms = rs.getString("symptoms");

                disease = new DiseaseSymptomModel(id, symptoms);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", "Error fetching disease: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return disease;
    }

    @Override
    public List<DiseaseSymptomModel> getAll() {
        List<DiseaseSymptomModel> diseases = new ArrayList<>();
        String query = "SELECT Disease_ID, symptoms FROM Disease";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("Disease_ID");
                String symptoms = rs.getString("symptoms");

                DiseaseSymptomModel disease = new DiseaseSymptomModel(id, symptoms);
                diseases.add(disease);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", "Error fetching diseases: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return diseases;
    }

    public void displayAllDiseases(DefaultTableModel tableModel) {
        List<DiseaseSymptomModel> diseases = getAll();
        tableModel.setRowCount(0);

        for (DiseaseSymptomModel disease : diseases) {
            tableModel.addRow(new Object[]{
                    disease.getDiseaseId(),
                    disease.getSymptom() != null ? disease.getSymptom() : "No symptoms"
            });
        }
    }

    public String getSymptomsById(int diseaseId) {
        DiseaseSymptomModel disease = getById(diseaseId);
        return disease != null ? disease.getSymptom() : "No symptoms found";
    }

    public List<DiseaseSymptomModel> searchBySymptomKeyword(String keyword) {
        List<DiseaseSymptomModel> diseases = new ArrayList<>();
        String query = "SELECT Disease_ID, symptoms FROM Disease WHERE symptoms LIKE ?";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, "%" + keyword + "%");
            rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("Disease_ID");
                String symptoms = rs.getString("symptoms");

                DiseaseSymptomModel disease = new DiseaseSymptomModel(id, symptoms);
                diseases.add(disease);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Search Error", "Error searching diseases: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return diseases;
    }

    public List<DiseaseSymptomModel> getDiseasesWithSymptoms() {
        List<DiseaseSymptomModel> diseases = new ArrayList<>();
        String query = "SELECT Disease_ID, symptoms FROM Disease WHERE symptoms IS NOT NULL AND symptoms != ''";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("Disease_ID");
                String symptoms = rs.getString("symptoms");

                DiseaseSymptomModel disease = new DiseaseSymptomModel(id, symptoms);
                diseases.add(disease);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", "Error fetching diseases with symptoms: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return diseases;
    }

    public int getDiseaseCount() {
        String query = "SELECT COUNT(*) FROM Disease";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                return rs.getInt(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", "Error counting diseases: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return 0;
    }

    public List<String> getAllSymptomKeywords() {
        List<String> keywords = new ArrayList<>();
        String query = "SELECT DISTINCT symptoms FROM Disease WHERE symptoms IS NOT NULL";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                String symptoms = rs.getString("symptoms");
                if (symptoms != null && !symptoms.trim().isEmpty()) {
                    // Split symptoms into individual words
                    String[] words = symptoms.split("[,\\s]+");
                    for (String word : words) {
                        String trimmed = word.trim().toLowerCase();
                        if (!trimmed.isEmpty() && !keywords.contains(trimmed)) {
                            keywords.add(trimmed);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", "Error extracting symptom keywords: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return keywords;
    }

    public List<DiseaseSymptomModel> getDiseasesWithoutSymptoms() {
        List<DiseaseSymptomModel> diseases = new ArrayList<>();
        String query = "SELECT Disease_ID, symptoms FROM Disease WHERE symptoms IS NULL OR symptoms = ''";

        Connection conn = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            conn = getConnection();
            pstmt = conn.prepareStatement(query);
            rs = pstmt.executeQuery();

            while (rs.next()) {
                int id = rs.getInt("Disease_ID");
                String symptoms = rs.getString("symptoms");

                DiseaseSymptomModel disease = new DiseaseSymptomModel(id, symptoms);
                diseases.add(disease);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Database Error", "Error fetching diseases without symptoms: " + e.getMessage());
        } finally {
            closeResources(rs, pstmt, conn);
        }

        return diseases;
    }
}