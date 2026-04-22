package shared.dto;

import shared.enums.UserRole;
import shared.enums.UserStatus;

import java.io.Serializable;
import java.sql.Timestamp;

public class UserDTO implements Serializable {
    private Long id;
    private String username;
    private String fullname;
    private String email;
    private UserRole role; // Sử dụng Enum Role: BIDDER, SELLER, ADMIN
    private UserStatus status;

    public UserDTO() {}

    public UserDTO(Long id, String username,String fullname, String email, UserRole role, UserStatus status) {
        this.id = id;
        this.username = username;
        this.fullname = fullname;
        this.email = email;
        this.role = role;
        this.status = status;
    }

    // Getters and Setters
    //getter
    public Long getId() { return id; }
    public String getUsername() {
        return username;
    }
    public String getEmail() { return email; }
    public String getFullName() { return fullname; }
    public UserRole getRole() {return role;}
    public UserStatus getStatus() {return status;}

    //setter
    public void setId(Long id) { this.id = id; }
    public void setUsername(String username) {this.username = username;}
    public void setFullname(String fullname) { this. fullname = fullname; }
    public void setEmail(String email) {this.email = email;}
    public void setRole(UserRole role) {this.role = role;}
    public void setStatus(UserStatus status) {this.status = status;}
}
