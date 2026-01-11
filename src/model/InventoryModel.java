package model;

public class InventoryModel extends BaseModel{
    private int greenhouseId;
    private String description;
    private int stock;
    private int number;
    private String category;
    private double unitPrice;

    public InventoryModel(int greenhouseId,String description,int stock,int number){
        super();
        this.greenhouseId=greenhouseId;
        this.description=description;
        this.stock=stock;
        this.number=number;
    }

    //Getter & Setters
    public int getGreenhouseId(){
        return greenhouseId;
    }
    public void setGreenhouseId(int greenhouseId){
        this.greenhouseId=greenhouseId;
        updateTimestamp();
    }

    public String getDescription(){
        return description;
    }
    public void setDescription(String description){
        this.description=description;
        updateTimestamp();
    }

    public int getStock(){
        return stock;
    }
    public void setStock(int stock){
        this.stock=stock;
        updateTimestamp();
    }

    public int getNumber(){
        return number;
    }
    public void setNumber(int number){
        this.number=number;
        updateTimestamp();
    }

    public String getCategory(){
        return category;
    }
    public void setCategory(String category){
        this.category=category;
        updateTimestamp();
    }

    public double getUnitPrice(){
        return unitPrice;
    }
    public void setUnitPrice(double unitPrice){
        this.unitPrice=unitPrice;
        updateTimestamp();
    }
}
