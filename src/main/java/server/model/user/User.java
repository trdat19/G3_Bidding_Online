package server.model.user;

import java.sql.Timestamp;
import server.model.Entity;
import shared.enums.UserRole;
import shared.enums.UserStatus;

public abstract class User extends Entity{
    protected String username;
    protected String password;
    protected String fullname;
    protected String email;
    protected UserRole role;
    protected Timestamp createdAt;
    protected UserStatus status ;

    public User(){}

    // dùng khi mới tạo user để insert Database
    public User(String username, String password, String fullname, String email,
                UserRole role, UserStatus status) {
        super();
        this.username = username;
        this.password = password;
        this.fullname = fullname;
        this.email = email;
        this.role = role;
        this.status = status;
    }

    // dùng khi đọc user từ DB
    public User(Long id, String username, String password, String fullname, String email,
                UserRole role, UserStatus status, Timestamp createdAt) {
        super(id);
        this.username = username;
        this.password = password;
        this.fullname = fullname;
        this.email = email;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
    }


    //getter
    public String getUsername() {
        return username;
    }
    public String getPassword() { return password; }
    public String getEmail() { return email; }
    public String getFullName() { return fullname; }
    public UserRole getRole() {return role;}
    public UserStatus getStatus() {return status;}
    public Timestamp getCreatedAt() {return createdAt;}
    //setter
    public void setUsername(String username) {this.username = username;}
    public void setPassword(String password) {this.password = password;}
    public void setEmail(String email) {this.password = password;}
    public void setFullname(String fullname) {this.fullname = fullname;}
    public void setRole(UserRole role) {this.role = role;}
    public void setStatus(UserStatus status) {this.status = status;}
    public void setCreatedAt(Timestamp createdAt) {this.createdAt = createdAt;}
}
