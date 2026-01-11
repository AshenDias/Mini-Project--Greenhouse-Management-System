package model;

public class DiseaseModel extends BaseModel{
    private String name;
    private String symptoms;
    private String treatment;
    private String severity;
    private int affectedPlantId;

    //Constructor
    public DiseaseModel(int diseaseId,String name,String symptoms,String treatment,String severity,int affectedPlantId){
        super(diseaseId);
        this.name=name;
        this.symptoms=symptoms;
        this.treatment=treatment;
        this.severity=severity;
        this.affectedPlantId=affectedPlantId;
    }

    //Getter & Setters
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name=name;
        updateTimestamp();
    }

    public String getSymptoms(){
        return symptoms;
    }
    public void setSymptoms(String symptoms){
        this.symptoms=symptoms;
        updateTimestamp();
    }

    public String getTreatment(){
        return treatment;
    }
    public void setTreatment(String treatment){
        this.treatment=treatment;
        updateTimestamp();
    }

    public String getSeverity(){
        return severity;
    }
    public void setSeverity(String severity){
        this.severity=severity;
        updateTimestamp();
    }

    public int getAffectedPlantId(){
        return affectedPlantId;
    }
    public void setAffectedPlantId(int affectedPlantId){
        this.affectedPlantId=affectedPlantId;
        updateTimestamp();
    }

    public int getDiseaseId(){
        return getId();
    }
    public void setDiseaseId(int diseaseId){
        setId(diseaseId);
    }
}
