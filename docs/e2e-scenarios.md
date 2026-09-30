# End-to-End Test Scenarios

Tài liệu này mô tả chi tiết các kịch bản kiểm thử End-to-End (E2E) xuyên suốt các quy trình nghiệp vụ của hệ thống quản lý trung tâm dạy thêm.

## Scenario 1: Teacher tạo lớp → HS học → Điểm danh → Tính tiền → HS thanh toán
1. Admin tạo tài khoản `teacher1`.
2. Teacher1 đăng nhập.
3. Teacher1 tạo khóa học "Toán 9", giá `100,000`/buổi.
4. Teacher1 tạo nhóm học "Toán 9A" thuộc khóa học trên.
5. Teacher1 tạo 3 học sinh: `student1`, `student2`, `student3`.
6. Teacher1 thêm 3 học sinh vào nhóm "Toán 9A".
7. Teacher1 tạo 8 buổi học (session) trong tháng 10.
8. Teacher1 điểm danh cho 8 buổi:
   - `student1`: 8 PRESENT
   - `student2`: 6 PRESENT, 2 ABSENT
   - `student3`: 7 PRESENT, 1 EXCUSED
9. Teacher1 chốt điểm danh (generate invoice) tháng 10:
   - `student1`: 8 × 100k = 800,000đ
   - `student2`: 8 × 100k = 800,000đ (ABSENT vẫn bị tính phí)
   - `student3`: 7 × 100k = 700,000đ (EXCUSED không bị tính phí)
10. Teacher1 finalize invoice (phát hành hóa đơn).
11. Student1 đăng nhập, vào trang Hóa đơn thấy hóa đơn 800,000đ, liên hệ chuyển khoản.
12. Teacher1 tạo ghi nhận thanh toán (Payment) 800,000đ cho hóa đơn của `student1` qua BANK_TRANSFER.
13. Trạng thái hóa đơn của `student1` chuyển sang PAID.
14. Student1 đăng nhập, thấy hóa đơn đã được thanh toán.

## Scenario 2: Học sinh đổi nhóm giữa tháng
1. `student1` học nhóm A (giá 100k/buổi) từ 1/10 đến 15/10 (được 5 buổi).
2. `student1` chuyển sang nhóm B (giá 120k/buổi) từ 16/10 đến 31/10 (được 5 buổi).
3. Cuối tháng 10, Teacher generate invoice cho `student1`.
4. Hệ thống quét qua các bản ghi invoice detail và cộng dồn: 5 × 100k + 5 × 120k = 1,100,000đ.
5. Kiểm tra chi tiết hóa đơn (invoice details) thấy hiển thị cả 2 khóa học.

## Scenario 3: Học sinh học 2 nhóm cùng lúc
1. `student1` ghi danh vào nhóm A (Toán, 100k) và nhóm B (Lý, 120k) cùng lúc.
2. Cả 2 nhóm đều phát sinh buổi học (session) trong tháng.
3. Khi generate invoice, tổng tiền = (số buổi Toán × 100k) + (số buổi Lý × 120k).
4. Chi tiết hóa đơn nhóm theo từng môn học rõ ràng.

## Scenario 4: Thay đổi giá khóa học sau khi đã điểm danh
1. Khóa học có giá 100k, đã điểm danh 5 buổi trong tháng 10.
2. Quản lý/Giáo viên quyết định tăng giá khóa học lên 150k.
3. Khi generate hóa đơn cho tháng 10, hệ thống vẫn áp dụng giá 100k dựa trên `price_snapshot` đã được lưu lúc tạo session/điểm danh.
4. Khi tạo session mới cho tháng 11, giá mới 150k sẽ được áp dụng.

## Scenario 5: Hủy buổi học (Cancel Session)
1. Teacher tạo một buổi học vào ngày 15/10.
2. Teacher điểm danh 3 học sinh.
3. Sau đó, Teacher quyết định hủy buổi học (chuyển status session sang CANCELLED).
4. Các bản ghi điểm danh (attendance) của buổi đó tự động chuyển thành CANCELLED.
5. Khi generate invoice cuối tháng, hệ thống bỏ qua buổi học này, học sinh không bị tính tiền.

## Scenario 6: Public Link (Chia sẻ lịch học)
1. Teacher tạo public link cho nhóm "Toán 9A".
2. Bất kỳ ai mở link này đều xem được lịch học (không cần đăng nhập).
3. Teacher revoke public link.
4. Truy cập lại link cũ báo lỗi 404 hoặc thông báo link không hợp lệ.
5. Teacher regenerate public link mới, link mới hoạt động bình thường.

## Scenario 7: Phân quyền (Security & RBAC)
1. Student cố gắng truy cập `/api/admin/users` → Nhận lỗi 403 Forbidden.
2. Teacher cố gắng truy cập `/api/admin/courses` → Nhận lỗi 403 Forbidden.
3. Teacher A cố gắng sửa hoặc xóa khóa học của Teacher B → Nhận lỗi 403 Forbidden hoặc thao tác bị từ chối.
4. Student cố gắng xem chi tiết hóa đơn của một Student khác → Nhận lỗi 403 Forbidden.
