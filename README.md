# Tuition Center Management

Hệ thống Quản lý Trung tâm Dạy thêm toàn diện, hỗ trợ 3 phân quyền: **Admin, Teacher, Student** với các chức năng chính xoay quanh Quản lý học sinh, Lớp học, Điểm danh, Hóa đơn và Thanh toán.

## 🚀 Cài đặt và Chạy ứng dụng

### Yêu cầu
- Java 17+
- MySQL / PostgreSQL (mặc định cấu hình dev dùng MySQL)
- Maven

### Bước 1: Clone kho mã nguồn
```bash
git clone https://github.com/your-username/tuition-center-management.git
cd tuition-center-management
```

### Bước 2: Cấu hình Secret
Tạo file `src/main/resources/application-secret.yml` dựa trên mẫu (có thể xem trong `.example` hoặc sử dụng biến môi trường):
```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/TuitionCenterManagementApplicationDatabase?createDatabaseIfNotExist=true
    username: root
    password: password

jwt:
  secret: day-la-chuoi-bi-mat-dai-hon-64-ky-tu-de-dam-bao-an-toan-tuyet-doi-cho-ung-dung

cloudinary:
  cloud-name: demo
  api-key: ...
  api-secret: ...
  url: cloudinary://...
```

### Bước 3: Chạy ứng dụng
Dùng Maven wrapper để chạy Spring Boot application (với profile dev):
```bash
# Windows
.\mvnw.cmd spring-boot:run "-Dspring-boot.run.profiles=dev"

# Linux / Mac
./mvnw spring-boot:run -Dspring-boot.run.profiles=dev
```
Hệ thống sẽ chạy tại: `http://localhost:8080/`

## 👥 Tài khoản test

| Vai trò | Username | Password |
|---------|----------|----------|
| Admin | `admin` | `admin123` |
| Teacher | `teacher1` | `123456` |
| Student | `student1` | `123456` |

*(Note: Data seeder sẽ tự động sinh tài khoản mẫu nếu DB trống)*

## 🛠 Cấu trúc Công nghệ
- **Backend**: Spring Boot 3.4.3, Spring Data JPA, Spring Security
- **Frontend**: Thymeleaf, HTML5, Vanilla JavaScript, Tailwind CSS (CDN)
- **Database**: H2 (cho Test), MySQL/PostgreSQL (cho Môi trường thật)
- **Storage**: Tích hợp Cloudinary để upload ảnh (Avatar, v.v)
- **Authentication**: Stateless qua JSON Web Token (JWT)

## 📚 API Documentation
Hệ thống tuân thủ REST API chuẩn, tài liệu được lưu tại:
→ Xem [docs/api-test.md](docs/api-test.md) (hoặc `docs/api-test-student.md`) để có danh sách các endpoint và kịch bản gọi bằng cURL.

## 🧪 Kiểm thử (Testing)
Dự án được bao phủ Unit Test và E2E Test:
- **Chạy Unit Test & Integration Test**: 
  ```bash
  .\mvnw.cmd test
  ```
- **Chạy E2E Test (PowerShell)**: 
  ```powershell
  .\test-e2e.ps1
  ```

## ☁️ Deployment
→ Hướng dẫn triển khai xem chi tiết tại [docs/deployment.md](docs/deployment.md).
