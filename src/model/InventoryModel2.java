package model;

public class InventoryModel2 extends BaseModel{
    private int greenhouseId;
    private String description;
    private int stock;
    private int number;

    public InventoryModel2() {
        super();
    }

    // Getters and Setters
    public int getGreenhouseId() { return greenhouseId; }
    public void setGreenhouseId(int greenhouseId) {
        this.greenhouseId = greenhouseId;
        updateTimestamp();
    }

    public String getDescription() { return description; }
    public void setDescription(String description) {
        this.description = description;
        updateTimestamp();
    }

    public int getStock() { return stock; }
    public void setStock(int stock) {
        this.stock = stock;
        updateTimestamp();
    }

    public int getNumber() { return number; }
    public void setNumber(int number) {
        this.number = number;
        updateTimestamp();
    }
}
