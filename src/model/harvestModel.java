package model;

public class harvestModel extends BaseModel{
    private int greenhouseId;
    private String description;
    private String stock1;
    private String harvestDate;
    private double quantity;
    private String quality;

    public harvestModel(int greenhouseId, String description, String stock1) {
        super();
        this.greenhouseId = greenhouseId;
        this.description = description;
        this.stock1 = stock1;
    }

    //Getter & Setters
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

    public String getStock1() { return stock1; }
    public void setStock1(String stock1) {
        this.stock1 = stock1;
        updateTimestamp();
    }

    public String getHarvestDate() { return harvestDate; }
    public void setHarvestDate(String harvestDate) {
        this.harvestDate = harvestDate;
        updateTimestamp();
    }

    public double getQuantity() { return quantity; }
    public void setQuantity(double quantity) {
        this.quantity = quantity;
        updateTimestamp();
    }

    public String getQuality() { return quality; }
    public void setQuality(String quality) {
        this.quality = quality;
        updateTimestamp();
    }

    public int getgid() { return greenhouseId; }
    public void setgid(int greenhouseId) { setGreenhouseId(greenhouseId); }

    public String getdesc() { return description; }
    public void setdesc(String description) { setDescription(description); }

    public String getstock1() { return stock1; }
    public void setstock1(String stock1) { setStock1(stock1); }
}
