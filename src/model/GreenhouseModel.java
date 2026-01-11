package model;

public class GreenhouseModel extends BaseModel{
    private String name;
    private String symptoms;
    private String treatments;
    private String location;
    private int temperature;

    //Constructors
    public GreenhouseModel(int diseaseId, String name, String symptoms,String treatments){
        super(diseaseId);
        this.name=name;
        this.symptoms=symptoms;
        this.treatments=treatments;
    }

    //Getter& Setters
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

    public String getTreatments(){
        return treatments;
    }
    public void setTreatments(String treatments){
        this.treatments=treatments;
        updateTimestamp();
    }

    public String getLocation(){
        return location;
    }
    public void setLocation(String location){
        this.location=location;
        updateTimestamp();
    }

    public int getTemperature(){
        return temperature;
    }
    public void setTemperature(int temperature){
        this.temperature=temperature;
        updateTimestamp();
    }

    public int getDiseaseId(){
        return getId();
    }
    public void setDiseaseId(int diseaseId){
        setId(diseaseId);
    }
}
