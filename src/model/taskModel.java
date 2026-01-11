package model;

public class taskModel extends BaseModel{
    private int plantId;
    private String plantName;
    private String waterAmount;
    private String nutrientAmount;
    private String taskDate;
    private String status;

    public taskModel(int plantId, String plantName, String waterAmount, String nutrientAmount) {
        super();
        this.plantId = plantId;
        this.plantName = plantName;
        this.waterAmount = waterAmount;
        this.nutrientAmount = nutrientAmount;
    }

    // Getters and Setters
    public int getPlantId() { return plantId; }
    public void setPlantId(int plantId) {
        this.plantId = plantId;
        updateTimestamp();
    }

    public String getPlantName() { return plantName; }
    public void setPlantName(String plantName) {
        this.plantName = plantName;
        updateTimestamp();
    }

    public String getWaterAmount() { return waterAmount; }
    public void setWaterAmount(String waterAmount) {
        this.waterAmount = waterAmount;
        updateTimestamp();
    }

    public String getNutrientAmount() { return nutrientAmount; }
    public void setNutrientAmount(String nutrientAmount) {
        this.nutrientAmount = nutrientAmount;
        updateTimestamp();
    }

    public String getTaskDate() { return taskDate; }
    public void setTaskDate(String taskDate) {
        this.taskDate = taskDate;
        updateTimestamp();
    }

    public String getStatus() { return status; }
    public void setStatus(String status) {
        this.status = status;
        updateTimestamp();
    }
}
