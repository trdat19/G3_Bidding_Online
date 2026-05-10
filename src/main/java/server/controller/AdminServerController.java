package server.controller;

import server.dao.UserDAO;
import server.model.user.User;
import shared.dto.common.UserDTO;
import shared.dto.response.BaseResponse;

import java.util.List;
import java.util.ArrayList;
public class AdminServerController {
    private static AdminServerController instance;
    public static synchronized AdminServerController getInstance() {
        if (instance == null) {
            instance = new AdminServerController();
        }
        return instance;
    }
    public BaseResponse getAllUsers() {
        List<User> userList = UserDAO.getInstance().getAllUsers();
        List<UserDTO> users = new ArrayList<>();
        for (User user : userList) {
            UserDTO dto = toDTO(user);
            users.add(dto);
        }
        return new BaseResponse(true, "Load Users Succesfully", users);
    }
    private UserDTO toDTO(User user) {
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getFullName(),
                user.getEmail(),
                user.getRole(),
                user.getStatus(),
                user.getCreatedAt());
    }


    //CODE CUA DUONG
    public void disableUser()
    {

    }
    public void cancelAuction()
    {

    }
    public void getAuditData()
    {

    }
}
