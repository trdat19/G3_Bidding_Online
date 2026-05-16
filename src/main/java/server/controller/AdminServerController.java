package server.controller;

import server.dao.AuctionDAO;
import server.dao.UserDAO;
import server.model.user.User;
import server.service.AuctionService;
import server.service.UserService;
import shared.dto.common.UserDTO;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;
import shared.enums.UserStatus;

import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.Map;

/**
 * Các thao tác quản lý hệ thống - điều phối request -> gọi service -> trả response
 * không chứa logic nghiệp vụ
 */
public class AdminServerController {
    private static AdminServerController instance;

    private final UserService userService = UserService.getInstance();
    private final AuctionService auctionService = AuctionService.getInstance();

    private AdminServerController() {}

    public static AdminServerController getInstance() {
        if (instance == null) {

            synchronized (AdminServerController.class) {
                if (instance == null) {
                    instance = new AdminServerController();
                }
            }
        }
        return instance;
    }

    //----------------HELPER---------------------
    private UserDTO toDTO(User user) {
        UserDTO dto = new UserDTO();
        dto.setId(user.getId());
        dto.setUsername(user.getUsername());
        dto.setFullname(user.getFullName());
        dto.setEmail(user.getEmail());
        dto.setStatus(user.getStatus());
        dto.setRole(user.getRole());
        dto.setCreatedAt(user.getCreatedAt());
        return dto;
    }

    //--------------USER MANAGEMENT-------------------------
    public BaseResponse getAllUsers() {
        try {
            List<User> userList = userService.findAllUsers();

            List<UserDTO> users = new ArrayList<>();
            for (User user : userList) {
                users.add(toDTO(user));
            }

            return new BaseResponse(true, "Lấy danh sách người dùng thành công!", users);

        } catch (Exception e) {
            return new BaseResponse(false, "Lỗi lấy danh sách người dùng: " + e.getMessage(), null);
        }
    }

    /** EnableUser – mở khóa tài khoản người dùng
     * @param request chứa userId của người dùng cần mở khóa
     */
    public BaseResponse enableUser(BaseRequest request) {
        try {
            Long userId = Long.parseLong(request.getData().toString());

            boolean ok = userService.changeStatus(userId, UserStatus.ACTIVE);

            return ok
                    ? new BaseResponse(true,
                                String.format("Đã mở khóa tài khoản #%d", userId), null)
                    : new BaseResponse(false,
                                String.format("Không thể mở khóa tài khoản #%d", userId), null);

        } catch (IllegalArgumentException e) {
            return new BaseResponse(false, e.getMessage(), null);
        } catch (Exception e) {
            return new BaseResponse(false, "Lỗi mở khóa: " + e.getMessage(), null);
        }
    }
    /**
     * DisableUser - khoá, vô hiệu hoá tài khoản người dùng
     * @param request chứa userId của người dùng cần khoá
     */
    public BaseResponse disableUser(BaseRequest request) {
        try {
            Long userId = Long.parseLong(request.getData().toString());

            boolean ok = userService.changeStatus(userId, UserStatus.BLOCKED);

            return ok
                    ? new BaseResponse(true,
                                String.format("Đã khóa tài khoản #%d", userId), null)
                    : new BaseResponse(false,
                                String.format("Không thể khóa tài khoản #%d", userId), null);

        } catch (IllegalArgumentException e) {
            return new BaseResponse(false, e.getMessage(), null);
        } catch (Exception e) {
            return new BaseResponse(false, "Lỗi khóa tài khoản: " + e.getMessage(), null);
        }
    }

    //-----------------TỔNG HỢP THỐNG KÊ---------------------
    /**
     * getAuditData - tổng hợp thống kê (users + auctions)
      * @param request có thể chứa các tham số lọc như khoảng thời gian, loại hoạt động, v.v.
     */
    public BaseResponse getAuditData(BaseRequest request) {
        try {
            Map<String, Object> data = new HashMap<>();
            data.put("totalUsers",    userService.countAllUsers());
            data.put("totalAuctions", auctionService.countAllAuctions());

            return new BaseResponse(true, "Lấy dữ liệu thống kê thành công!", data);

        } catch (Exception e) {
            return new BaseResponse(false, "Lỗi lấy dữ liệu thống kê: " + e.getMessage(), null);
        }
    }
}
