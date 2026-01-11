package model;

import java.time.LocalDateTime;

public abstract class BaseModel {
    protected int id;
    protected LocalDateTime createdAt;
    protected LocalDateTime updatedAt;
    protected boolean isActive;


    //Constructors
    public BaseModel(){
        this.createdAt= LocalDateTime.now();
        this.updatedAt= LocalDateTime.now();
        this.isActive=true;
    }

    public BaseModel(int id){
        this();
        this.id=id;
    }

    //Getter & Setter Methods
    public int getId(){
        return id;
    }
    public void setId(int id){
        this.id=id;
        this.updatedAt=LocalDateTime.now();
    }

    public LocalDateTime getCreatedAt(){
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt){
        this.createdAt=createdAt;
    }

    public LocalDateTime getUpdatedAt(){
        return updatedAt;
    }
    public void setUpdatedAt(LocalDateTime updatedAt){
        this.updatedAt=updatedAt;
    }

    public boolean isActive(){
        return isActive;
    }
    public void setIsActive(boolean active){
        this.isActive=active;
        this.updatedAt=LocalDateTime.now();
    }

    public void updateTimestamp(){
        this.updatedAt=LocalDateTime.now();
    }

    @Override
    public String toString(){
        return this.getClass().getSimpleName() + "[id=" + id + ", created At=" + createdAt +"]";
    }

}
