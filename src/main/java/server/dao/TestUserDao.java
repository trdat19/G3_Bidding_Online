package server.dao;

import server.model.user.Admin;
import server.model.user.Bidder;
import server.model.user.Seller;
import server.model.user.User;
import shared.enums.UserStatus;

import java.util.List;

public class TestUserDao {
    public static void main(String[] args) {
        UserDao userDao = new UserDao();

        System.out.println("===== TEST INSERT =====");
        User admin = new Admin("admin01", "123456", "System Admin", "admin01@gmail.com");
        User bidder = new Bidder("bidder01", "123456", "Nguyen Van A", "bidder01@gmail.com");
        User seller = new Seller("seller01", "123456", "Tran Van B", "seller01@gmail.com");

        boolean insertAdmin = userDao.insertUser(admin);
        boolean insertBidder = userDao.insertUser(bidder);
        boolean insertSeller = userDao.insertUser(seller);

        System.out.println("Insert admin: " + insertAdmin);
        System.out.println("Insert bidder: " + insertBidder);
        System.out.println("Insert seller: " + insertSeller);

        System.out.println("Admin id sau khi insert: " + admin.getId());
        System.out.println("Bidder id sau khi insert: " + bidder.getId());
        System.out.println("Seller id sau khi insert: " + seller.getId());

        System.out.println("\n===== TEST EXISTS =====");
        System.out.println("Exists username admin01: " + userDao.existsByUsername("admin01"));
        System.out.println("Exists email bidder01@gmail.com: " + userDao.existsByEmail("bidder01@gmail.com"));
        System.out.println("Exists username abcxyz: " + userDao.existsByUsername("abcxyz"));

        System.out.println("\n===== TEST FIND BY USERNAME =====");
        User foundBidder = userDao.findByUsername("bidder01");
        if (foundBidder != null) {
            System.out.println("Tim thay bidder: " +
                    foundBidder.getId() + " | " +
                    foundBidder.getUsername() + " | " +
                    foundBidder.getFullName() + " | " +
                    foundBidder.getEmail() + " | " +
                    foundBidder.getRole() + " | " +
                    foundBidder.getStatus());
        } else {
            System.out.println("Khong tim thay bidder01");
        }

        System.out.println("\n===== TEST FIND BY ID =====");
        if (admin.getId() != null) {
            User foundAdmin = userDao.findById(admin.getId());
            if (foundAdmin != null) {
                System.out.println("Tim thay admin theo id: " +
                        foundAdmin.getId() + " | " +
                        foundAdmin.getUsername() + " | " +
                        foundAdmin.getRole());
            } else {
                System.out.println("Khong tim thay admin theo id");
            }
        }

        System.out.println("\n===== TEST UPDATE USER =====");
        if (foundBidder != null) {
            foundBidder.setFullname("Nguyen Van A Updated");
            foundBidder.setEmail("bidder01_updated@gmail.com");
            boolean updated = userDao.updateUser(foundBidder);
            System.out.println("Update full user info: " + updated);

            User afterUpdate = userDao.findById(foundBidder.getId());
            if (afterUpdate != null) {
                System.out.println("Sau updateUser: " +
                        afterUpdate.getId() + " | " +
                        afterUpdate.getUsername() + " | " +
                        afterUpdate.getFullName() + " | " +
                        afterUpdate.getEmail());
            }
        }

        System.out.println("\n===== TEST UPDATE USERNAME =====");
        if (seller.getId() != null) {
            boolean updatedUsername = userDao.updateUsername(seller.getId(), "seller01_new");
            System.out.println("Update username seller: " + updatedUsername);

            User sellerAfterUsername = userDao.findById(seller.getId());
            if (sellerAfterUsername != null) {
                System.out.println("Username moi cua seller: " + sellerAfterUsername.getUsername());
            }
        }

        System.out.println("\n===== TEST UPDATE PASSWORD =====");
        if (admin.getId() != null) {
            boolean updatedPassword = userDao.UpdatePassword(admin.getId(), "999999");
            System.out.println("Update password admin: " + updatedPassword);

            User adminAfterPassword = userDao.findById(admin.getId());
            if (adminAfterPassword != null) {
                System.out.println("Password moi cua admin: " + adminAfterPassword.getPassword());
            }
        }

        System.out.println("\n===== TEST UPDATE STATUS =====");
        if (bidder.getId() != null) {
            boolean updatedStatus = userDao.updateStatus(bidder.getId(), UserStatus.BLOCKED);
            System.out.println("Update status bidder -> BLOCKED: " + updatedStatus);

            User bidderAfterStatus = userDao.findById(bidder.getId());
            if (bidderAfterStatus != null) {
                System.out.println("Status moi cua bidder: " + bidderAfterStatus.getStatus());
            }
        }

        System.out.println("\n===== TEST GET ALL USERS =====");
        List<User> allUsers1 = userDao.getAllUsers();
        for (User u : allUsers1) {
            System.out.println(u.getId() + " | " +
                    u.getUsername() + " | " +
                    u.getFullName() + " | " +
                    u.getEmail() + " | " +
                    u.getRole() + " | " +
                    u.getStatus());
        }

        System.out.println("\n===== TEST FIND ALL =====");
        List<User> allUsers2 = userDao.findAll();
        for (User u : allUsers2) {
            System.out.println(u.getId() + " | " +
                    u.getUsername() + " | " +
                    u.getRole() + " | " +
                    u.getStatus());
        }

        System.out.println("\n===== TEST DELETE =====");
        if (seller.getId() != null) {
            boolean deleted = userDao.deleteUser(seller.getId());
            System.out.println("Delete seller: " + deleted);
        }

        System.out.println("\n===== TEST FIND ALL SAU KHI DELETE =====");
        List<User> allUsers3 = userDao.findAll();
        for (User u : allUsers3) {
            System.out.println(u.getId() + " | " +
                    u.getUsername() + " | " +
                    u.getRole() + " | " +
                    u.getStatus());
        }
    }
}