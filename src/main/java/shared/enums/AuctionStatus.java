package shared.enums;

public enum AuctionStatus {
    PREPARING, /** seller đã gửi request, đang chờ admin duyệt */
    WAITING_APPROVAL,
    OPEN ,  /** admin đã duyệt, phiên sắp diễn ra, chưa cho bid nếu chưa tới startTime */
    RUNNING, /** đã tới startTime, được đặt giá */
    FINISHED, /** đã kết thức */
    CLOSED, /** đã đóng */
    CANCELLED, /** bị hủy */
}
