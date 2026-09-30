# Hướng dẫn Triển khai (Deployment Guide)

Tài liệu này cung cấp các bước cơ bản để đưa ứng dụng **Tuition Center Management** lên môi trường Production.

## 1. Yêu cầu Hệ thống
- VPS/Server chạy Linux (Ubuntu/CentOS).
- Java 17 JRE/JDK cài đặt sẵn.
- MySQL 8.x hoặc PostgreSQL 14+ cài đặt sẵn.
- Nginx hoặc Apache làm Reverse Proxy.

## 2. Biên dịch Ứng dụng
Tạo file `.jar` có thể thực thi:
```bash
./mvnw clean package -DskipTests
```
Kết quả sẽ nằm ở `target/tuition-center-management-0.0.1-SNAPSHOT.jar`.

## 3. Cấu hình Biến môi trường
Không nên lưu mật khẩu cứng trên server. Cấu hình các biến môi trường cho Spring Boot:
```bash
export SPRING_PROFILES_ACTIVE=prod
export SPRING_DATASOURCE_URL=jdbc:mysql://localhost:3306/tuition_prod
export SPRING_DATASOURCE_USERNAME=prod_user
export SPRING_DATASOURCE_PASSWORD=StrongPasswordHere
export JWT_SECRET=tao-mot-chuoi-random-key-rat-dai-tai-day
export CLOUDINARY_URL=cloudinary://your-api-key-here
```

## 4. Chạy dưới dạng Systemd Service (Linux)
Tạo file service `/etc/systemd/system/tuition-app.service`:
```ini
[Unit]
Description=Tuition Center Management Spring Boot Application
After=syslog.target network.target

[Service]
User=appuser
EnvironmentFile=/etc/tuition-app/env.conf
ExecStart=/usr/bin/java -jar /opt/tuition-app/tuition-center-management-0.0.1-SNAPSHOT.jar
SuccessExitStatus=143

[Install]
WantedBy=multi-user.target
```

Sau đó khởi động ứng dụng:
```bash
sudo systemctl daemon-reload
sudo systemctl enable tuition-app
sudo systemctl start tuition-app
```

## 5. Nginx Reverse Proxy
Cấu hình Nginx để dẫn traffic vào ứng dụng (Port 8080) và cấp chứng chỉ SSL:
```nginx
server {
    listen 80;
    server_name yourdomain.com;

    location / {
        proxy_pass http://127.0.0.1:8080;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
}
```
Khuyên dùng `certbot` để cung cấp Let's Encrypt HTTPS.
