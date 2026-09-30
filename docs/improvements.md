# Đề xuất Cải tiến (Future Improvements)

Sau khi hoàn thành Phase E2E, dự án hiện tại đã đáp ứng tốt các yêu cầu về quản lý, nghiệp vụ kế toán, điểm danh. Dưới đây là 10 đề xuất cải tiến về kiến trúc, hiệu năng và tính năng cho giai đoạn tương lai:

## 1. Swagger UI & OpenAPI
- Tích hợp `springdoc-openapi-starter-webmvc-ui` để tự động hóa tài liệu API.
- Cho phép DEV/QA dễ dàng test và tương tác trực tiếp với API từ trình duyệt mà không cần nhớ cURL.

## 2. Docker Compose cho Dev / Cấu trúc hóa môi trường
- Cung cấp `docker-compose.yml` định nghĩa sẵn MySQL, Redis, Adminer (hoặc phpMyAdmin).
- Đơn giản hóa quá trình cài đặt (chỉ cần chạy `docker-compose up` là có môi trường sạch sẽ).

## 3. CI/CD với GitHub Actions / GitLab CI
- Xây dựng workflow chạy tự động (Unit Test, Integration Test) mỗi khi có Pull Request.
- Workflow deploy tự động lên môi trường Staging/Production khi merge vào nhánh `main`.

## 4. Monitoring & Báo cáo hệ thống (Actuator + Prometheus + Grafana)
- Bật module Spring Boot Actuator để thu thập metrics.
- Cấu hình Prometheus kéo (pull) metrics và dựng biểu đồ trên Grafana để theo dõi RAM, CPU, số lượng request/giây, và tỷ lệ lỗi HTTP 500.

## 5. Cache Redis cho Search / Sort
- API search, đặc biệt là thống kê Dashboard hoặc các Query nặng (báo cáo) sẽ chậm dần khi DB phình to. Cấu hình Spring Data Redis Cache (`@Cacheable`) để tăng tốc và giảm tải Database.

## 6. Tích hợp Full-text Search bằng Elasticsearch
- Khi dữ liệu hàng nghìn sinh viên, tìm kiếm `name/email/phone` bằng lệnh `LIKE %...%` sẽ gây thắt cổ chai ở DB. 
- Di chuyển Index tìm kiếm qua Elasticsearch để query tốc độ cao.

## 7. Sao lưu Cơ sở dữ liệu Tự động (Backup)
- Viết cron job (script) dump Database hàng ngày lúc 2h sáng, nén file và tự động gửi lên Amazon S3 hoặc Google Drive lưu trữ an toàn.

## 8. Hỗ trợ Đa ngôn ngữ (i18n)
- Chuyển tất cả thông báo lỗi và giao diện ra các file `messages.properties` / `messages_en.properties`.
- Cho phép người dùng chuyển đổi Tiếng Việt / Tiếng Anh thông qua header `Accept-Language` hoặc Cookie.

## 9. Multi-tenant (Kiến trúc Nhiều Trung tâm)
- Thay đổi kiến trúc Database thêm cột `tenant_id` để hệ thống hỗ trợ bán dưới dạng SaaS (Software as a Service) cho nhiều trung tâm dạy thêm sử dụng chung bộ code.

## 10. Cổng thanh toán Trực tuyến (Payment Gateway Integration)
- Tích hợp VNPay, MoMo, hoặc ZaloPay vào bước sinh hóa đơn.
- Học sinh bấm nút "Thanh toán", quét mã QR và trả tiền. Hệ thống nhận Webhook/IPN và tự động đổi trạng thái hóa đơn thành `PAID` thay vì phải có thao tác xác nhận thủ công từ Giáo viên/Admin.
