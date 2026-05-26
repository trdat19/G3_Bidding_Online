package server.network;

import shared.dto.response.BaseResponse;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * RealtimePushServer – "Tổng đài" chủ động đẩy dữ liệu xuống client.
 *
 * Có hai cách gửi:
 *
 *   1. Gửi đích danh – dùng userRegistry
 *      Server biết username → lấy ra handler → gửi thẳng.
 *      VD: thông báo riêng "bạn đã thắng phiên đấu giá".
 *
 *   2. Gửi theo phòng – dùng auctionRooms
 *      Mỗi phiên đấu giá là một "phòng". Ai đang xem phiên đó
 *      thì subscribe vào phòng. Khi có bid mới → push cho cả phòng.
 *      VD: "MacBook vừa được đặt giá 2500$" → tất cả người xem phiên đó nhận.
 *
 * Thread-safe:
 *   - ConcurrentHashMap: đọc ghi đồng thời không bị lỗi
 *   - CopyOnWriteArrayList: duyệt danh sách subscriber an toàn khi có người join/leave
 */

public class RealtimePushServer {

    // ─── KHO LƯU KẾT NỐI ────────────────────────────────────────────────────
    /**
     * Danh bạ cá nhân: username → handler của người đó.
     * Dùng để gửi tin nhắn riêng (direct message).
     *
     * Key: username (String)
     * Value: ClientConnectionHandler (đường dây socket tới client đó)
     */
    private static final Map<Long, ClientConnectionHandler> userRegistry
            = new ConcurrentHashMap<>();

    /**
     * Danh sách phòng đấu giá: auctionId → danh sách handler của người đang xem.
     * Dùng để broadcast khi có sự kiện trong phiên.
     *
     * Key: auctionId (int)
     * Value: list các ClientConnectionHandler đang xem phiên đó
     */
    private static final Map<Long, List<ClientConnectionHandler>> auctionSubscribers
            = new ConcurrentHashMap<>();

    private static final List<ClientConnectionHandler> auctionListSubscribers = new CopyOnWriteArrayList<>();


    // ---PHƯƠNG THỨC CHÍNH---
    // Gửi tin nhắn cho 1 người
    /**
     * Gửi tin nhắn trực tiếp tới 1 user (theo username).
     * Dùng cho: thông báo cá nhân ("bạn đã thắng", "tài khoản bị khóa").
     *
     * @param userId    Id của người nhận
     * @param event     Gói dữ liệu gửi đi
     */

    public static void pushToUser(Long userId, BaseResponse event) {
        ClientConnectionHandler handler = userRegistry.get(userId);
        if (handler != null) {
            handler.sendResponse(event);
            System.out.print(">>> [Realtime] Đã gửi riêng tới User ID: " + userId
                            + " Message: " + event.getMessage());
        }
        else {
            System.out.println(">>> [Realtime] User ID: " + userId + " không online!");
        }
    }


    // Gửi tin nhắn cho cả phiên
    /**
     * Gửi thông báo sự kiện tới tất cả người đang xem một phiên đấu giá.
     * Dùng cho: bid mới, gia hạn phiên, phiên kết thúc.
     *
     * @param auctionId ID phiên
     * @param event     Sự kiện xảy ra
     */
    public static void pushToAuctionSubscribers(Long auctionId, BaseResponse event) {
        List<ClientConnectionHandler> subscribers = auctionSubscribers.get(auctionId);

        if (subscribers != null && !subscribers.isEmpty()) {
            System.out.println(">>> Đang đẩy tin Realtime tới " + subscribers.size()
                                + " người đang xem phiên #" + auctionId);

            // Duyệt danh sách và bảo từng Handler đẩy dữ liệu xuống
            for (ClientConnectionHandler handler : subscribers) {
                handler.sendResponse(event);
            }
        }
    }

    // --- HÀM QUẢN LÝ (GỌI TỪ CONTROLLER/SERVICE ) ---

    //-----ĐĂNG KÍ - HUỶ ĐĂNG KÍ USER ------
    /**
     * Đăng ký user vào danh bạ khi login thành công.
     * AuthServerController gọi hàm này ngay sau khi xác thực.
     *
     * @param userId userId
     * @param handler  Đường dây socket của user đó
     */
    public static void registerUser(Long userId, ClientConnectionHandler handler) {
        userRegistry.put(userId, handler);
        System.out.printf(">>> [Realtime] User '%s' đã online. Tổng online: %d%n",
                userId, userRegistry.size());
    }

    /**
     * User subscribe vào phòng đấu giá khi họ mở màn hình xem phiên.
     * Client gửi action "SUBSCRIBE_AUCTION" → RequestRouter gọi hàm này.
     *
     * @param auctionId ID phiên muốn theo dõi
     * @param handler   Đường dây socket của người subscribe
     */

    public static void subscribeToAuction(Long auctionId, ClientConnectionHandler handler) {
        // computeIfAbsent: nếu phòng chưa có thì tạo, có rồi thì lấy ra
        List<ClientConnectionHandler> room = auctionSubscribers
                .computeIfAbsent(auctionId, k -> new CopyOnWriteArrayList<>());

        // Tránh subscribe trùng user (người cùng mở tab 2 lần)
        if (!room.contains(handler)) {
            room.add(handler);
            System.out.printf(">>> [Realtime] Phiên #%d: %d người đang xem%n",
                    auctionId, room.size());
        }
    }

    /**
     * Hủy subscribe khỏi một phòng cụ thể.(người dùng thoát màn hình xem phiên mà chưa đóng app)
     *
     * @param auctionId ID phiên muốn rời
     * @param handler   Đường dây socket của người rời phòng
     */
    public static void unsubscribeFromAuction(Long auctionId, ClientConnectionHandler handler) {
        List<ClientConnectionHandler> room = auctionSubscribers.get(auctionId);
        if (room != null) {
            room.remove(handler);
            System.out.printf(">>> [Realtime] Phiên #%d: còn %d người đang xem%n",
                    auctionId, room.size());
        }
    }

    /**
     * Dọn dẹp toàn bộ khi client ngắt kết nối (đóng app, mất mạng).
     * ClientConnectionHandler.closeConnection() gọi hàm này.
     *
     * @param handler Đường dây socket vừa đóng
     */
    public static void removeConnection(ClientConnectionHandler handler) {
        // 1. Xóa khỏi danh bạ cá nhân
        userRegistry.values().remove(handler);

        // 2. Xóa khỏi tất cả các phòng đang tham gia
        auctionSubscribers.values().forEach(room -> room.remove(handler));

        auctionListSubscribers.remove(handler);

        System.out.printf(">>> [Realtime] Một client ngắt kết nối. Còn %d online.%n",
                userRegistry.size());
    }

    //------- THỐNG KÊ / DEBUG ---------------
    /** Số lượng user đang online */
    public static int countOnlineUsers() {
        return userRegistry.size();
    }

    /** Số lượng người đang xem một phiên cụ thể */
    public static int countRoomSubscribers(int auctionId) {
        List<ClientConnectionHandler> room = auctionSubscribers.get(auctionId);
        return (room != null) ? room.size() : 0;
    }

    /** Danh sách tất cả username đang online */
    public static Set<Long> getOnlineUsers() {
        return userRegistry.keySet();
    }

    /** In trạng thái hiện tại ra console – dùng khi debug */
    public static void printStatus() {
        System.out.println("=== [Realtime] Trạng thái hiện tại ===");
        System.out.println("Online: " + userRegistry.keySet());
        auctionSubscribers.forEach((id, room) ->
                System.out.printf("  Phiên #%d: %d người xem%n", id, room.size()));
        System.out.println("======================================");
    }

    public static void subscribeToAuctionList(ClientConnectionHandler handler)
    {
        if(!auctionListSubscribers.contains(handler))
        {
            auctionListSubscribers.add(handler);
            System.out.println(">>> [Realtime] Bidder dashboard subscribers: "
                    + auctionListSubscribers.size());

        }
    }
    public static void pushToAuctionListSubscribers(BaseResponse event)
    {
        for(ClientConnectionHandler handler : auctionListSubscribers)
        {
            handler.sendResponse(event);
        }
    }
}
