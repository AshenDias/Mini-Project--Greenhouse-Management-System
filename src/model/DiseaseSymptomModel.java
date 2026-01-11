package model;

public class DiseaseSymptomModel extends BaseModel{
    private String symptom;

    public DiseaseSymptomModel() {
        super();
    }

    public DiseaseSymptomModel(int diseaseId, String symptom) {
        super(diseaseId);
        this.symptom = symptom;
    }

    // Getters and Setters
    public String getSymptom() { return symptom; }
    public void setSymptom(String symptom) {
        this.symptom = symptom;
        updateTimestamp();
    }

    public int getDiseaseId() { return getId(); }
    public void setDiseaseId(int diseaseId) { setId(diseaseId); }
}
