# Checklist Hoàn Thiện Dự Án (Tuition Center Management)

## □ Backend
- [x] Tất cả endpoint có `@PreAuthorize` phân quyền đúng role (Admin, Teacher, Student).
- [x] Tất cả list endpoint đều dùng `PageResponse<T>` từ Phase F1.
- [x] Tất cả tính năng tìm kiếm (search) đều tích hợp 3-in-1 (name/email/phone) nơi phù hợp.
- [x] Tất cả sort theo tên chính xác.
- [x] Xóa dữ liệu áp dụng Soft Delete (trạng thái INACTIVE/CANCELLED/LEFT) thay vì xóa cứng.
- [x] Mọi thao tác quan trọng đều được ghi lại trong Audit Log.
- [x] `GlobalExceptionHandler` trả về JSON error message thân thiện (400, 401, 403, 404, 500).
- [x] Không lộ password trong bất kỳ DTO response nào.
- [x] Bảo mật API không trạng thái (stateless) qua JWT; hết hạn trả về 401 Unauthorized.
- [x] Hỗ trợ quy trình token (nếu cấu hình thêm).

## □ Frontend
- [x] Cấu trúc file HTML tái sử dụng layout gốc (`layout/main.html`) cho cả 3 role.
- [x] Sidebar thông minh, tự động đổi các mục menu theo Role của JWT.
- [x] Header hiển thị đúng User Info (Tên, Role) đọc từ local storage.
- [x] Thanh tìm kiếm (Search Bar) đồng bộ hoạt động với ListHelper trên mọi trang danh sách.
- [x] Tiêu đề cột Sort (Sort header) phản hồi thao tác click.
- [x] Chức năng phân trang (Pagination) mượt mà không load lại toàn trang.
- [x] Thiết kế CSS bằng Tailwind tương thích Responsive (Mobile 1 cột, Desktop nhiều cột).
- [x] Trang lỗi chuẩn 404 Not Found / 403 Forbidden.
- [x] Xử lý State mượt mà: Loading State (khi gọi API), Empty State (khi không có dữ liệu), Error State.

## □ Nghiệp vụ Kế toán / Quản lý
- [x] Học sinh đổi nhóm giữa tháng → Tính toán logic gộp 2 khoảng thời gian chính xác.
- [x] Học sinh học 2 nhóm/khóa cùng lúc → Hệ thống tính tiền đúng số buổi tham gia.
- [x] Snapshot giá (Price snapshot) để lưu vết lịch sử: Giữ nguyên giá lúc tạo Session, không thay đổi hồi tố khi sửa giá Course.
- [x] Trạng thái điểm danh EXCUSED (có phép) KHÔNG bị tính tiền hóa đơn.
- [x] Trạng thái điểm danh ABSENT (không phép) VẪN tính tiền hóa đơn (mặc định).
- [x] Buổi học CANCELLED KHÔNG bị tính tiền.
- [x] Hóa đơn ở dạng DRAFT vẫn cho phép cập nhật điểm danh. Hóa đơn FINALIZED khóa quyền cập nhật.
- [x] Hỗ trợ thanh toán nhiều lần qua cơ chế chia nhỏ Payment.
- [x] Lịch học công khai (Public Link) an toàn, không rò rỉ dữ liệu nhạy cảm của Học sinh.

## □ Data & Cơ sở dữ liệu
- [x] Seed data tự động khởi tạo dữ liệu mẫu (Phase F4).
- [x] Database bảo mật: Toàn bộ mật khẩu được mã hóa BCrypt.
- [x] Cấu hình linh hoạt: Không hard-code các Secret Key/DB config trong code (dùng `application.yml`).
