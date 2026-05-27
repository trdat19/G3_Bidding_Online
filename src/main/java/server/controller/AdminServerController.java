package server.controller;

import server.model.user.User;
import server.dao.AdminDashboardDAO;
import server.service.AuctionService;
import server.service.UserService;
import shared.dto.AdminDashboardDTO;
import shared.dto.common.AuctionDTO;
import shared.dto.common.UserDTO;
import shared.dto.request.BaseRequest;
import shared.dto.response.BaseResponse;
import shared.enums.UserStatus;
import server.model.item.Item;
import server.service.ItemService;
import shared.dto.common.ItemDTO;
import server.dao.AuctionDAO;
import server.model.core.Auction;
import shared.enums.ItemStatus;
import shared.enums.UserRole;

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
    private final ItemService itemService = ItemService.getInstance();
    private final AuctionDAO auctionDAO = new AuctionDAO();
    private final AdminDashboardDAO adminDashboardDAO = new AdminDashboardDAO();

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



    public BaseResponse getDashboardSummary() {
        AdminDashboardDTO summary = adminDashboardDAO.getSummary();
        if (summary == null) {
            return new BaseResponse(false, "Không thể tải dữ liệu tổng quan quản trị.", null);
        }

        return new BaseResponse(true, "Lấy dữ liệu tổng quan quản trị thành công.", summary);
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

    public BaseResponse getAllItems() {
        try {
            List<Item> itemList = itemService.findAllItems();

            List<ItemDTO> items = new ArrayList<>();
            for (Item item : itemList) {
                items.add(toDTO(item));
            }

            return new BaseResponse(true,
                    "Lấy danh sách sản phẩm thành công!",
                    items);

        } catch (Exception e) {
            return new BaseResponse(false,
                    "Lỗi lấy danh sách sản phẩm: " + e.getMessage(),
                    null);
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

            User targetUser = userService.findUser(userId);

            if (targetUser.getRole() == UserRole.ADMIN) {
                return new BaseResponse(false,
                        "Không thể khóa tài khoản Admin!",
                        null);
            }

            boolean ok = userService.changeStatus(userId, UserStatus.BLOCKED);

            return ok
                    ? new BaseResponse(true,
                    String.format("Đã khóa tài khoản #%d", userId),
                    null)
                    : new BaseResponse(false,
                    String.format("Không thể khóa tài khoản #%d", userId),
                    null);

        } catch (IllegalArgumentException e) {
            return new BaseResponse(false, e.getMessage(), null);
        } catch (Exception e) {
            return new BaseResponse(false, "Lỗi khóa: " + e.getMessage(), null);
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

    public BaseResponse getCreateAuctionRequests() {
        return new BaseResponse(
                true,
                "Danh sách yêu cầu tạo đấu giá",
                auctionService.getAuctionApprovalRequests()
        );
    }

    public BaseResponse acceptCreateAuctionRequest(BaseRequest request) {
        Long auctionId = Long.parseLong(request.getData().toString());

        boolean ok = auctionService.approveAuctionRequest(auctionId);

        return ok
                ? new BaseResponse(true, "Đã duyệt yêu cầu tạo đấu giá", null)
                : new BaseResponse(false, "Không thể duyệt yêu cầu này", null);

    }

    public BaseResponse rejectCreateAuctionRequest(BaseRequest request) {
        Long auctionId = Long.parseLong(request.getData().toString());

        boolean ok = auctionService.rejectAuctionRequest(auctionId);

        return ok
                ? new BaseResponse(true, "Đã từ chối yêu cầu tạo đấu giá", null)
                : new BaseResponse(false, "Không thể từ chối yêu cầu này", null);
    }

    public BaseResponse getAllAuctions() {
        try {
            List<AuctionDTO> auctions = auctionService.getAllAuctions();
            return new BaseResponse(true, "Lấy danh sách phiên đấu giá thành công", auctions);
        } catch (Exception e) {
            return new BaseResponse(false,
                    "Lỗi lấy danh sách phiên đấu giá: " + e.getMessage(),
                    null);
        }
    }

    private ItemDTO toDTO(Item item) {
        ItemDTO dto = new ItemDTO();

        dto.setId(item.getId());
        dto.setName(item.getNameItem());
        dto.setDescription(item.getDescription());
        dto.setCategory(item.getCategory());
        dto.setStatus(item.getStatusItem());
        dto.setSellerId(item.getSellerId());
        dto.setCreatedAt(item.getCreatedAtItem());

        try {
            dto.setSellerName(userService.findUser(item.getSellerId()).getFullName());
        } catch (Exception e) {
            dto.setSellerName("Không xác định");
        }

        if (item.getStatusItem() == ItemStatus.PENDING) {
            dto.setPriceStart(null);
            dto.setCurrentPrice(null);
            return dto;
        }

        List<Auction> auctions = auctionDAO.getAllAuctionsByItemId(item.getId());

        if (auctions != null && !auctions.isEmpty()) {
            Auction latestAuction = auctions.get(0);

            dto.setPriceStart(latestAuction.getStartPrice());
            dto.setCurrentPrice(latestAuction.getMaxPrice() != null
                    ? latestAuction.getMaxPrice()
                    : latestAuction.getStartPrice());
        }

        return dto;
    }
}
