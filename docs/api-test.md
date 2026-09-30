# API Testing Guide (Self-Test bằng CURL)

Lưu ý: Bạn cần cài đặt [jq](https://stedolan.github.io/jq/) để extract JWT token tự động (nếu dùng trên Bash/Zsh), hoặc copy token thủ công và gán vào biến môi trường.

## 1. Đăng nhập và lấy Token

### Đăng nhập Admin
```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}' | jq -r '.token')
echo $TOKEN
```

### Đăng nhập Teacher
```bash
TOKEN_TEACHER=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"teacher1","password":"123456"}' | jq -r '.token')
```

### Đăng nhập Student
```bash
TOKEN_STUDENT=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"student1","password":"123456"}' | jq -r '.token')
```

---

## 2. API Quản lý User (Admin)

### Lấy danh sách user (có phân trang, tìm kiếm)
```bash
curl -X GET "http://localhost:8080/api/admin/users?keyword=an&page=0&size=10&sortBy=name&sortDir=asc" \
  -H "Authorization: Bearer $TOKEN"
```

### Tạo user mới
```bash
curl -X POST http://localhost:8080/api/admin/users \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"username":"newstudent","password":"password","fullName":"Nguyen Van Test","email":"test@test.com","role":"STUDENT"}'
```

### Khóa user (Soft delete / Đổi status)
```bash
curl -X PUT "http://localhost:8080/api/admin/users/4/status?status=INACTIVE" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 3. API Quản lý Khóa học & Nhóm học

### Lấy danh sách khóa học
```bash
curl -X GET "http://localhost:8080/api/admin/courses" \
  -H "Authorization: Bearer $TOKEN"
```

### Tạo khóa học mới
```bash
curl -X POST http://localhost:8080/api/admin/courses \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"name":"Tiếng Anh Giao Tiếp","pricePerSession":150000,"teacherId":2}'
```

### Lấy danh sách nhóm học
```bash
curl -X GET "http://localhost:8080/api/admin/groups" \
  -H "Authorization: Bearer $TOKEN"
```

### Xem học sinh trong nhóm
```bash
curl -X GET "http://localhost:8080/api/admin/groups/1/students" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 4. API Buổi học & Điểm danh

### Lấy danh sách session
```bash
curl -X GET "http://localhost:8080/api/admin/sessions?groupId=1" \
  -H "Authorization: Bearer $TOKEN"
```

### Lấy dữ liệu điểm danh của 1 session
```bash
curl -X GET "http://localhost:8080/api/admin/attendance?sessionId=1" \
  -H "Authorization: Bearer $TOKEN"
```

### Điểm danh (Cập nhật trạng thái)
```bash
curl -X PUT "http://localhost:8080/api/admin/attendance/1" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"status":"PRESENT","note":"Đến đúng giờ"}'
```

---

## 5. API Hóa đơn & Thanh toán

### Tạo hóa đơn hàng tháng (Tạo tự động cho toàn bộ HS)
```bash
curl -X POST "http://localhost:8080/api/admin/invoices/generate" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d '{"month":9,"year":2026}'
```

### Lấy danh sách hóa đơn
```bash
curl -X GET "http://localhost:8080/api/admin/invoices" \
  -H "Authorization: Bearer $TOKEN"
```

### Lấy chi tiết hóa đơn
```bash
curl -X GET "http://localhost:8080/api/admin/invoices/1" \
  -H "Authorization: Bearer $TOKEN"
```

---

## 6. Public API (Không cần Auth)

### Lấy thông tin Group qua public token
```bash
curl -X GET "http://localhost:8080/api/public/groups/token-t9a"
```

### Đăng ký học (Public Enroll)
```bash
curl -X POST "http://localhost:8080/api/public/groups/token-t9a/enroll" \
  -H "Content-Type: application/json" \
  -d '{"fullName":"Học sinh mới","email":"new@test.com","phone":"0999999999"}'
```
