package model;

public class WorkerModel extends BaseModel{
    private String name;
    private String email;
    private String username;
    private String password;
    private String role;
    private String phone;
    private String hireDate;

    public WorkerModel(String name, String email, String username, String password) {
        super();
        this.name = name;
        this.email = email;
        this.username = username;
        this.password = password;
    }

    // Getters and Setters
    public String getName() { return name; }
    public void setName(String name) {
        this.name = name;
        updateTimestamp();
    }

    public String getEmail() { return email; }
    public void setEmail(String email) {
        this.email = email;
        updateTimestamp();
    }

    public String getUsername() { return username; }
    public void setUsername(String username) {
        this.username = username;
        updateTimestamp();
    }

    public String getPassword() { return password; }
    public void setPassword(String password) {
        this.password = password;
        updateTimestamp();
    }

    public String getRole() { return role; }
    public void setRole(String role) {
        this.role = role;
        updateTimestamp();
    }

    public String getPhone() { return phone; }
    public void setPhone(String phone) {
        this.phone = phone;
        updateTimestamp();
    }

    public String getHireDate() { return hireDate; }
    public void setHireDate(String hireDate) {
        this.hireDate = hireDate;
        updateTimestamp();
    }
}
