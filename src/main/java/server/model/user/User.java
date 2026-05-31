package server.model.user;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

import server.model.Entity;
import shared.enums.UserRole;
import shared.enums.UserStatus;

public abstract class User extends Entity implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    protected String username;
    protected String password;
    protected String fullname;
    protected String email;
    protected UserRole role;
    protected UserStatus status;
    protected LocalDateTime createdAt;

    public User() {}

    public User(String username, String password, String fullname, String email) {
        super();
        this.username = username;
        this.password = password;
        this.fullname = fullname;
        this.email = email;
        this.status = UserStatus.ACTIVE;
    }

    //getter
    public String getUsername() {
        return username;
    }
    public String getPassword() { return password; }
    public String getFullName() { return fullname; }
    public String getEmail() { return email; }
    public UserRole getRole() {return role;}
    public UserStatus getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    //setter
    public void setUsername(String username) {this.username = username;}
    public void setPassword(String password) {this.password = password;}
    public void setFullname(String fullname) {this.fullname = fullname;}
    public void setEmail(String email) {this.email = email;}
    public void setRole(UserRole role) {this.role = role;}
    public void setStatus(UserStatus status) {this.status = status;}
    public void setCreatedAt(LocalDateTime createdAt) {this.createdAt = createdAt;}

    @Override
    public String getInfo() {
        return String.format("[%s] %s (%s)| Status: %s",
                role,
                username,
                email,
                status != null ? status.name() : "UNKNOWN");
    }

}
