## Phân chia công việc
Tổng quát có: GUI, Database, Backend

1. Việt Anh: Phụ trách Giao diện GUI , controller, logic Client

• JavaFX: Kéo thả thiết kế toàn bộ màn hình bằng Scene Builder.

• Logic UI: Viết các class Controller để xử lý sự kiện bấm nút, nhập text và logic chuyễn cảnh (Scene Switching).

2. Thái Dương: Phụ trách Mạng (Socket) & Logic Dữ liệu chung

• Socket: Viết code khởi tạo kết nối mạng (Socket ở Client và ServerSocket ở Server) khởi động server, kết nối database

• Logic đóng gói: Thiết kế bộ khung giao tiếp giữa 2 bên gồm các class Request, Response.

3. Tiến Đạt: Phụ trách Máy chủ (Server) & Cơ sở dữ liệu (Database)

• Server. Xây dựng máy chủ đa luồng (ClientHandler) để xử lý nhiều người dùng cùng kết nỗi và đầu giá một lúc. Viết logic kiểm tra giá và đồng hồ đếm ngược.

• Database: Kết nổi và thao tác với Cơ sở dữ liệu (lưu tài khoản, lích sử đặt giá, kết quả phiên đầu giá). Xử lý khóa luồng (synchronized) để dữ liệu lưu vào DB không bị lỗi khi nhiều người cùng đặt giá.

4. Thành Đạt (Nhóm trưởng): Hỗ trợ logic, merge code, cấu trúc dữ liệu
• T: đóng góp ý tưởng và xây dựng các hàm logic nền tảng trong giai đoạn khởi tạo dự án.
