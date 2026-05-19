[![Java CI with Maven](https://github.com/trdat19/G3_Bidding_Online/actions/workflows/maven.yml/badge.svg)](https://github.com/trdat19/G3_Bidding_Online/actions/workflows/maven.yml)

# HỆ THỐNG ĐẤU GIÁ TRỰC TUYẾN - BIDZONE
## Nhóm 3 - 2526II_UET.CS2043_3

---

## 1. Giới thiệu hệ thống
Hệ thống đấu giá trực tuyến được xây dựng theo mô hình Client-Server bằng Java Socket Programming.
Người dùng có thể đăng nhập, tham gia đấu giá sản phẩm theo thời gian thực và nhận cập nhật giá ngay lập tức.

Hệ thống gồm 3 vai trò chính:
- **Admin**: Quản lý sản phẩm, người dùng, phiên đấu giá.
- **Seller**: Đăng bán sản phẩm, quản lý sản phẩm của mình.
- **Bidder**: Tham gia đấu giá, đặt giá thầu.

Phạm vi chức năng:
- Đăng nhập/đăng ký người dùng.
- Quản lý sản phẩm (thêm/sửa/xóa).
- Tham gia đấu giá, đặt giá thầu.
- Cập nhật giá thầu theo thời gian thực.
- Thông báo kết quả đấu giá.
- Quản lý phiên đấu giá (bắt đầu/kết thúc).

## 2. Cấu trúc hệ thống
- **Server**: Xử lý logic chính, quản lý kết nối, lưu trữ dữ liệu.
- **Client**: Giao diện người dùng, gửi yêu cầu đến server, nhận phản hồi và hiển thị thông tin.
- **Database**: Lưu trữ thông tin người dùng, sản phẩm, phiên đấu giá, lịch sử đấu giá.

## 3. Công nghệ sử dụng
