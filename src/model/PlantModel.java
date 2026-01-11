package model;

public class PlantModel extends BaseModel{
    private String name;
    private double waterAmount;
    private double nutrientAmount;
    private String plantType;
    private int greenhouseId;

    //Constructors
    public PlantModel(){
        super();
    }
    public PlantModel(int plantId, String name, double waterAmount, double nutrientAmount,String plantType,int greenhouseId){
        super(plantId);
        this.name=name;
        this.waterAmount=waterAmount;
        this.nutrientAmount=nutrientAmount;
        this.plantType=plantType;
        this.greenhouseId=greenhouseId;
    }

    //Getter & Setters
    public String getName(){
        return name;
    }
    public void setName(String name){
        this.name=name;
        updateTimestamp();
    }

    public double getWaterAmount(){
        return waterAmount;
    }
    public void setWaterAmount(double waterAmount){
        this.waterAmount=waterAmount;
        updateTimestamp();
    }

    public double getNutrientAmount(){
        return nutrientAmount;
    }
    public void setNutrientAmount(double nutrientAmount){
        this.nutrientAmount=nutrientAmount;
        updateTimestamp();
    }

    public String getPlantType(){
        return plantType;
    }
    public void setPlantType(String plantType){
        this.plantType=plantType;
        updateTimestamp();
    }

    public int getGreenhouseId(){
        return greenhouseId;
    }
    public void setGreenhouseId(int greenhouseId){
        this.greenhouseId=greenhouseId;
        updateTimestamp();
    }

    public int getPlantId(){
        return getId();
    }
    public void setPlantId(int plantId){
        setId(plantId);
    }
}
