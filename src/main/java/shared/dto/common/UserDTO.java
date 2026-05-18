package shared.dto.common;

import shared.enums.UserRole;
import shared.enums.UserStatus;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * UserDTO – dữ liệu người dùng truyền giữa client và server.
 *
 * KHÔNG chứa password để đảm bảo bảo mật.
 *
 * Dùng khi:
 *   - Server trả về thông tin user sau khi login thành công
 *   - Admin xem danh sách người dùng
 *   - Hiển thị tên người đặt giá cao nhất
 */
public class UserDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long id;
    private String username;
    private String fullname;
    private String email;
    private UserRole role;
    private UserStatus status;
    private LocalDateTime createdAt;

    // ─── CONSTRUCTORS ─────────────────────────────────────────────────────────

    public UserDTO() {}

    /** Constructor đầy đủ */
    public UserDTO(Long id, String username, String fullname,
                   String email, UserRole role, UserStatus status,
                   LocalDateTime createdAt) {
        this.id = id;
        this.username = username;
        this.fullname = fullname;
        this.email = email;
        this.role = role;
        this.status = status;
        this.createdAt = createdAt;
    }

    /** Constructor rút gọn – dùng khi chỉ cần hiển thị tên */
    public UserDTO(Long id, String username, String fullname) {
        this.id = id;
        this.username = username;
        this.fullname = fullname;
    }

    // ─── GETTERS ──────────────────────────────────────────────────────────────

    public Long getId()               { return id; }
    public String getUsername()       { return username; }
    public String getFullname()       { return fullname; }
    public String getEmail()          { return email; }
    public UserRole getRole()         { return role; }
    public UserStatus getStatus()     { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }

    // ─── SETTERS ──────────────────────────────────────────────────────────────

    public void setId(Long id)                   { this.id = id; }
    public void setUsername(String username)     { this.username = username; }
    public void setFullname(String fullname)     { this.fullname = fullname; }
    public void setEmail(String email)           { this.email = email; }
    public void setRole(UserRole role)           { this.role = role; }
    public void setStatus(UserStatus status)     { this.status = status; }
    public void setCreatedAt(LocalDateTime t)    { this.createdAt = t; }

    // ─── HELPERS ──────────────────────────────────────────────────────────────

    /** Tài khoản có đang hoạt động không */
    public boolean isActive() {
        return status == UserStatus.ACTIVE;
    }

    @Override
    public String toString() {
        return String.format("UserDTO[id=%d, username=%s, role=%s, status=%s]",
                id, username, role, status);
    }
}