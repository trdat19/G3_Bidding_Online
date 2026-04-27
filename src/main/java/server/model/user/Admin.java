package server.model.user;

import shared.enums.UserRole;
import shared.enums.UserStatus;

import java.sql.Timestamp;

public class Admin extends User {

    private static final long serialVersionUID = 1L;

    public Admin() {}

    public Admin(String username, String password, String fullname, String email) {
        super(username, password, fullname, email);
        this.role = UserRole.ADMIN;
    }

    public void suspendUser(String id) {
        // lấy user từ database ra, đặt status = blocked;
    }

    public void viewReport() {
        //xem report;
    }

    @Override
    public String getInfo() {
        return super.getInfo() + " | [ADMIN]";
    }
}