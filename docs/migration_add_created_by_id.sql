-- Migration bổ sung created_by_id cho bảng Users
-- Dùng để phân quyền Giáo viên chỉ xem/sửa được những Học sinh do chính mình tạo ra

ALTER TABLE users 
ADD COLUMN created_by_id BIGINT NULL;

ALTER TABLE users 
ADD CONSTRAINT fk_users_created_by 
FOREIGN KEY (created_by_id) REFERENCES users(id);

-- Lưu ý: Nếu database dùng Hibernate ddl-auto=update thì cột này sẽ tự động được tạo.
-- Bạn có thể chạy script này nếu muốn thêm thủ công vào DB đang chạy.
