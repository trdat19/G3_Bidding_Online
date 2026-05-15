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

    private final UserSerivce userSerivce = UserService.getInstance();
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

    //--------------USER MANAGEMENT-------------------------
    public BaseResponse getAllUsers() {
        List<User> userList = userDAO.getAllUsers();
        List<UserDTO> users = new ArrayList<>();
        for (User user : userList) {
            UserDTO dto = toDTO(user);
            users.add(dto);
        }
        return new BaseResponse(true, "Load Users Succesfully", users);
    }
    /** EnableUser – mở khóa tài khoản người dùng
     * @param request chứa userId của người dùng cần mở khóa
     */
    public BaseResponse enableUser(BaseRequest request) {
        try {
            Long userId = Long.parseLong(request.getData().toString());
            boolean ok = userDAO.updateStatus(userId, UserStatus.ACTIVE);
            return ok
                    ? new BaseResponse(true,
                                        String.format("Đã mở khóa tài khoản #%d", userId),
                                    null)
                    : new BaseResponse(false,
                                        String.format("Không tìm thấy người dùng #%d", userId),
                                    null);
        }
        catch (Exception e) {
            return new BaseResponse(false,
                            String.format("Lỗi mở khóa: %s ", e.getMessage()),
                    null);
        }
    }
    /**
     * DisableUser - khoá, vô hiệu hoá tài khoản người dùng
     * @param request chứa userId của người dùng cần khoá
     */
    public BaseResponse disableUser(BaseRequest request) {
        try {

            Long userId = Long.parseLong(request.getData().toString());
            boolean ok = userDAO.updateStatus(userId, UserStatus.BLOCKED);

            return ok
                    ? new BaseResponse(true,
                                    String.format("Đã khoá tài khoản #%d", userId),
                                    null)
                    : new BaseResponse(false,
                                    String.format("Không thể khoá tài khoản #%d", userId),
                                    null);
        }
        catch (Exception e) {
            return new BaseResponse(false,
                            String.format("Lỗi khoá tài khoản: %s ", e.getMessage()),
                        null);
        }
    }

    //--------------AUCTION MANAGEMENT--------------------------
    /**
     * createAuction - tạo phiên đấu giá mới
     *
     */

    /**
     * cancelAuction - admin huỷ phiên đấu giá
     * @param request chứa auctionId của phiên đấu giá cần huỷ
     */
    public BaseResponse cancelAuction(BaseRequest request) {
        try {
            Long auctionId = Long.parseLong(request.getData().toString());

            boolean ok = auctionDAO.cancelAuction(auctionId);
            return ok
                    ? new BaseResponse(true,
                                        String.format("Đã huỷ phiên đấu giá #%d", auctionId),
                                    null)
                    : new BaseResponse(false,
                                        String.format("Không tìm thấy phiên đấu giá #%d", auctionId),
                                    null);
        }
        catch (Exception e) {
            return new BaseResponse(false,
                            String.format("Lỗi huỷ phiên đấu giá: %s ", e.getMessage()),
                    null);
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
            data.put("totalUsers", userDAO.getAllUsers().size());
            data.put("totalAuctions", auctionDAO.getAllAuctions().size());
            // Có thể thêm các thống kê khác

            return new BaseResponse(true, "Lấy dữ liệu thành công!", data);
        }
        catch (Exception e) {
            return new BaseResponse(false,
                            String.format("Lỗi lấy dữ liệu: %s ", e.getMessage()),
                             null);
        }
    }
}
