package model;

public class ToolModel extends BaseModel{
    private String toolName;
    private int quantity;
    private String handoverTime;
    private String toolType;
    private String condition;
    private String lastMaintenance;

    public ToolModel(String toolName, int quantity, String handoverTime) {
        super();
        this.toolName = toolName;
        this.quantity=quantity;
        this.handoverTime = handoverTime;
    }

    // Getters and Setters
    public String getToolName() { return toolName; }
    public void setToolName(String toolName) {
        this.toolName = toolName;
        updateTimestamp();
    }

    public int getQuantity() { return quantity; }
    public void setQuantity(int quantity) {
        this.quantity=quantity;
        updateTimestamp();
    }

    public String getHandoverTime() { return handoverTime; }
    public void setHandoverTime(String handoverTime) {
        this.handoverTime = handoverTime;
        updateTimestamp();
    }

    public String getToolType() { return toolType; }
    public void setToolType(String toolType) {
        this.toolType = toolType;
        updateTimestamp();
    }

    public String getCondition() { return condition; }
    public void setCondition(String condition) {
        this.condition = condition;
        updateTimestamp();
    }

    public String getLastMaintenance() { return lastMaintenance; }
    public void setLastMaintenance(String lastMaintenance) {
        this.lastMaintenance = lastMaintenance;
        updateTimestamp();
    }
}
