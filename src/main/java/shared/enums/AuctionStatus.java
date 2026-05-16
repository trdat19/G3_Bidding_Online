package shared.enums;

public enum AuctionStatus {
    PREPARING,   // Vừa tạo, chờ bắt đầu
    OPEN,        // Đang mở, nhận bid
    RUNNING,     // Đang diễn ra sôi nổi (có ít nhất 1 bid)
    FINISHED,    // Hết giờ, đã xác định winner
    PAID,        // Người thắng đã thanh toán
    CLOSED,      // Đóng (kế thừa legacy)
    CANCELLED    // Bị hủy bởi Admin/Seller
}