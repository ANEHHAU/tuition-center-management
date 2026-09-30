# Student API Test 

File này chứa danh sách các curl commands để test Student API. Lưu ý thay thế `<token>` bằng JWT token của Student1.

## 1. Dashboard
```bash
# Lấy thống kê tổng quan
curl -X GET "http://localhost:8080/api/student/dashboard/stats" \
     -H "Authorization: Bearer <token>"

# Lấy buổi học hôm nay
curl -X GET "http://localhost:8080/api/student/dashboard/today-sessions" \
     -H "Authorization: Bearer <token>"

# Lấy buổi học 7 ngày tới
curl -X GET "http://localhost:8080/api/student/dashboard/upcoming-sessions?days=7" \
     -H "Authorization: Bearer <token>"
```

## 2. Schedule
```bash
# Xem lịch học (phân trang, filter)
curl -X GET "http://localhost:8080/api/student/schedule?from=2024-01-01&to=2024-12-31&page=0&size=10" \
     -H "Authorization: Bearer <token>"

# Chi tiết 1 buổi học
curl -X GET "http://localhost:8080/api/student/schedule/session/1" \
     -H "Authorization: Bearer <token>"

# Xem lịch học hôm nay
curl -X GET "http://localhost:8080/api/student/schedule/today" \
     -H "Authorization: Bearer <token>"

# Xem lịch học tuần (dựa vào một ngày bất kỳ trong tuần)
curl -X GET "http://localhost:8080/api/student/schedule/week?date=2024-10-15" \
     -H "Authorization: Bearer <token>"
```

## 3. Attendance
```bash
# Xem lịch sử điểm danh (có filter và phân trang)
curl -X GET "http://localhost:8080/api/student/attendance?from=2024-01-01&to=2024-12-31&status=PRESENT&page=0&size=10" \
     -H "Authorization: Bearer <token>"

# Lấy thống kê điểm danh
curl -X GET "http://localhost:8080/api/student/attendance/stats?from=2024-01-01&to=2024-12-31" \
     -H "Authorization: Bearer <token>"
```

## 4. Courses
```bash
# Xem khóa học của tôi
curl -X GET "http://localhost:8080/api/student/courses?page=0&size=10" \
     -H "Authorization: Bearer <token>"

# Xem chi tiết khóa học + group đang tham gia
curl -X GET "http://localhost:8080/api/student/courses/1" \
     -H "Authorization: Bearer <token>"
```

## 5. Invoices
```bash
# Lấy danh sách hóa đơn (có filter tháng, năm, trạng thái)
curl -X GET "http://localhost:8080/api/student/invoices?year=2024&status=UNPAID&page=0&size=10" \
     -H "Authorization: Bearer <token>"

# Lấy tổng nợ hiện tại
curl -X GET "http://localhost:8080/api/student/invoices/current-debt" \
     -H "Authorization: Bearer <token>"

# Xem chi tiết 1 hóa đơn
curl -X GET "http://localhost:8080/api/student/invoices/1" \
     -H "Authorization: Bearer <token>"

# Xem lịch sử nợ
curl -X GET "http://localhost:8080/api/student/invoices/debt-history" \
     -H "Authorization: Bearer <token>"

# Học sinh thông báo đã chuyển khoản thanh toán
curl -X POST "http://localhost:8080/api/student/invoices/1/notify-payment" \
     -H "Authorization: Bearer <token>" \
     -H "Content-Type: application/json" \
     -d '{
           "amount": 200000,
           "method": "BANK_TRANSFER",
           "note": "Nguyễn Văn A chuyển khoản học phí tháng 10"
         }'
```
