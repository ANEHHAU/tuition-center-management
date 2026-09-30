# Kiểm thử API Teacher (Phase F5)

## Các thông số chung
- Base URL: `http://localhost:8080`
- Các request cần Header: `Authorization: Bearer <TOKEN>`

## 1. Xác thực (Lấy Token)
```bash
curl -X POST http://localhost:8080/api/auth/login \
     -H "Content-Type: application/json" \
     -d '{"username":"teacher1", "password":"123456"}'
# Cần lưu token vào biến để test các bước sau
```

## 2. Quản lý Học sinh (TeacherStudentController)

### Tạo học sinh
```bash
curl -X POST http://localhost:8080/api/teacher/students \
     -H "Authorization: Bearer <TOKEN>" \
     -H "Content-Type: application/json" \
     -d '{"username":"student20", "password":"password123", "fullName":"Nguyễn Học Sinh Mới", "email":"student20@tuition.com"}'
```

### Tìm kiếm học sinh (3-in-1, sort tên)
```bash
curl -X GET "http://localhost:8080/api/teacher/students?keyword=Nguyễn&sortBy=name&sortDir=asc&page=0&size=10" \
     -H "Authorization: Bearer <TOKEN>"
```

### Xem chi tiết học sinh
```bash
curl -X GET http://localhost:8080/api/teacher/students/1/detail \
     -H "Authorization: Bearer <TOKEN>"
```

## 3. Khóa học & Nhóm

### Tìm kiếm khóa học
```bash
curl -X GET "http://localhost:8080/api/teacher/courses?keyword=Toán&sortBy=name&sortDir=asc&page=0&size=10" \
     -H "Authorization: Bearer <TOKEN>"
```

### Tìm kiếm nhóm
```bash
curl -X GET "http://localhost:8080/api/teacher/groups?keyword=Lớp&sortBy=name&sortDir=asc&page=0&size=10" \
     -H "Authorization: Bearer <TOKEN>"
```

## 4. Buổi học (Sessions)

### Tìm kiếm lịch học
```bash
curl -X GET "http://localhost:8080/api/teacher/sessions?from=2026-09-01&to=2026-09-30&sortBy=date&sortDir=desc&page=0&size=20" \
     -H "Authorization: Bearer <TOKEN>"
```

### Tạo lịch học hàng loạt
```bash
curl -X POST http://localhost:8080/api/teacher/sessions/bulk-create \
     -H "Authorization: Bearer <TOKEN>" \
     -H "Content-Type: application/json" \
     -d '{"groupId":1, "startDate":"2026-10-01", "endDate":"2026-10-31", "daysOfWeek":[2,5], "startTime":"18:00:00", "endTime":"19:30:00", "room":"Phòng A1"}'
```

## 5. Điểm danh

### Lấy danh sách cần điểm danh cho buổi học
```bash
curl -X GET http://localhost:8080/api/teacher/attendance/session/1 \
     -H "Authorization: Bearer <TOKEN>"
```

### Lưu điểm danh hàng loạt
```bash
curl -X POST http://localhost:8080/api/teacher/attendance/session/1 \
     -H "Authorization: Bearer <TOKEN>" \
     -H "Content-Type: application/json" \
     -d '{"items":[{"studentId":1, "status":"PRESENT", "note":"Đi học đầy đủ"}, {"studentId":2, "status":"ABSENT", "note":"Ốm"}]}'
```

### Xuất CSV điểm danh
```bash
curl -X GET http://localhost:8080/api/teacher/attendance/export?sessionId=1 \
     -H "Authorization: Bearer <TOKEN>" \
     --output attendance_1.csv
```

## 6. Hóa đơn & Thanh toán

### Tìm kiếm hóa đơn
```bash
curl -X GET "http://localhost:8080/api/teacher/invoices?month=9&year=2026&status=UNPAID&keyword=Bình&sortBy=name&sortDir=asc&page=0&size=10" \
     -H "Authorization: Bearer <TOKEN>"
```

### Ghi nhận thanh toán
```bash
curl -X POST http://localhost:8080/api/teacher/invoices/payments \
     -H "Authorization: Bearer <TOKEN>" \
     -H "Content-Type: application/json" \
     -d '{"invoiceId":1, "amount":500000, "method":"BANK_TRANSFER", "paidAt":"2026-09-30T10:00:00", "note":"Chuyển khoản VCB"}'
```

## 7. Báo cáo & Thống kê

### Dashboard Stats
```bash
curl -X GET http://localhost:8080/api/teacher/dashboard/stats \
     -H "Authorization: Bearer <TOKEN>"
```

### Báo cáo tỷ lệ đi học (Top Học sinh)
```bash
curl -X GET "http://localhost:8080/api/teacher/reports/attendance-rate?from=2026-09-01&to=2026-09-30" \
     -H "Authorization: Bearer <TOKEN>"
```
