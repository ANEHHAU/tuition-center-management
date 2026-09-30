/**
 * Quản lý trang hồ sơ cá nhân và đổi mật khẩu
 */

document.addEventListener("DOMContentLoaded", function () {
    const profileForm = document.getElementById("profile-form");
    const passwordForm = document.getElementById("password-form");
    const avatarInput = document.getElementById("avatar-file-input");

    // Chỉ thực thi nếu đang ở trang profile
    if (profileForm) {
        loadProfileData();

        profileForm.addEventListener("submit", async function (e) {
            e.preventDefault();
            await handleUpdateProfile();
        });
    }

    if (passwordForm) {
        passwordForm.addEventListener("submit", async function (e) {
            e.preventDefault();
            await handleChangePassword();
        });
    }

    if (avatarInput) {
        avatarInput.addEventListener("change", async function (e) {
            if (e.target.files && e.target.files[0]) {
                await handleUploadAvatar(e.target.files[0]);
            }
        });
    }
});

/**
 * Lấy dữ liệu profile từ Backend và fill vào form
 */
async function loadProfileData() {
    try {
        const user = await window.apiFetch("/api/profile");

        if (document.getElementById("profile-username")) document.getElementById("profile-username").textContent = user.username;
        if (document.getElementById("profile-role")) document.getElementById("profile-role").textContent = user.role;
        if (document.getElementById("profile-fullname-display")) document.getElementById("profile-fullname-display").textContent = user.fullName;

        if (document.getElementById("fullName")) document.getElementById("fullName").value = user.fullName || "";
        if (document.getElementById("email")) document.getElementById("email").value = user.email || "";
        if (document.getElementById("phone")) document.getElementById("phone").value = user.phone || "";
        if (document.getElementById("address")) document.getElementById("address").value = user.address || "";
        if (document.getElementById("note")) document.getElementById("note").value = user.note || "";
        if (document.getElementById("avatarUrl")) document.getElementById("avatarUrl").value = user.avatarUrl || "";

        if (user.avatarUrl && document.getElementById("profile-avatar-img")) {
            document.getElementById("profile-avatar-img").src = user.avatarUrl;
        }
        
        if (user.avatarUrl && document.getElementById("header-user-avatar")) {
            document.getElementById("header-user-avatar").src = user.avatarUrl;
        }
    } catch (error) {
        console.error("Lỗi khi tải thông tin profile:", error);
    }
}

/**
 * Xử lý cập nhật thông tin cá nhân
 */
async function handleUpdateProfile() {
    const errorMsg = document.getElementById("profile-error-msg");
    const successMsg = document.getElementById("profile-success-msg");

    if (errorMsg) errorMsg.classList.add("hidden");
    if (successMsg) successMsg.classList.add("hidden");

    const payload = {
        fullName: document.getElementById("fullName").value,
        email: document.getElementById("email").value,
        phone: document.getElementById("phone").value,
        address: document.getElementById("address").value,
        note: document.getElementById("note").value,
        avatarUrl: document.getElementById("avatarUrl").value
    };

    try {
        const updatedUser = await window.apiFetch("/api/profile", {
            method: "PUT",
            body: JSON.stringify(payload)
        });

        // Cập nhật lại user trong localStorage
        const localUser = window.authService.getCurrentUser() || {};
        localUser.fullName = updatedUser.fullName;
        localUser.avatarUrl = updatedUser.avatarUrl;
        localStorage.setItem("user", JSON.stringify(localUser));

        if (successMsg) {
            successMsg.textContent = "Cập nhật hồ sơ cá nhân thành công!";
            successMsg.classList.remove("hidden");
        }
        
        // Reload lại hiển thị
        loadProfileData();
    } catch (error) {
        if (errorMsg) {
            errorMsg.textContent = error.message || "Cập nhật hồ sơ thất bại";
            errorMsg.classList.remove("hidden");
        }
    }
}

/**
 * Xử lý đổi mật khẩu
 */
async function handleChangePassword() {
    const errorMsg = document.getElementById("password-error-msg");
    const successMsg = document.getElementById("password-success-msg");

    if (errorMsg) errorMsg.classList.add("hidden");
    if (successMsg) successMsg.classList.add("hidden");

    const oldPassword = document.getElementById("oldPassword").value;
    const newPassword = document.getElementById("newPassword").value;

    try {
        const res = await window.apiFetch("/api/profile/password", {
            method: "PUT",
            body: JSON.stringify({ oldPassword, newPassword })
        });

        if (successMsg) {
            successMsg.textContent = res.message || "Đổi mật khẩu thành công!";
            successMsg.classList.remove("hidden");
        }

        document.getElementById("password-form").reset();
    } catch (error) {
        if (errorMsg) {
            errorMsg.textContent = error.message || "Đổi mật khẩu thất bại";
            errorMsg.classList.remove("hidden");
        }
    }
}

/**
 * Xử lý Upload Avatar - Có debug chi tiết nếu lỗi
 */
async function handleUploadAvatar(file) {
    // Validate phía client trước khi gửi
    if (!file) {
        alert("Vui lòng chọn file ảnh.");
        return;
    }
    if (file.size > 2 * 1024 * 1024) {
        alert("Ảnh quá lớn! Tối đa 2MB. File hiện tại: " + (file.size / 1024 / 1024).toFixed(2) + "MB");
        return;
    }
    if (!file.type.startsWith("image/")) {
        alert("Chỉ chấp nhận file ảnh (JPEG, PNG, GIF, WEBP).");
        return;
    }

    const formData = new FormData();
    formData.append("file", file);

    const token = localStorage.getItem("token");
    if (!token) {
        alert("Bạn chưa đăng nhập. Vui lòng đăng nhập lại.");
        window.location.href = "/login";
        return;
    }

    try {
        // Gọi trực tiếp fetch thay vì qua apiFetch để kiểm soát lỗi tốt hơn
        const response = await fetch("/api/files/avatar", {
            method: "POST",
            headers: {
                "Authorization": "Bearer " + token
                // KHÔNG set Content-Type – browser tự thêm multipart boundary
            },
            body: formData
        });

        if (response.status === 401) {
            alert("Token hết hạn. Vui lòng đăng nhập lại.");
            localStorage.removeItem("token");
            localStorage.removeItem("user");
            window.location.href = "/login";
            return;
        }

        // Đọc response body
        const contentType = response.headers.get("content-type");
        let data;
        if (contentType && contentType.includes("application/json")) {
            data = await response.json();
        } else {
            data = await response.text();
        }

        if (!response.ok) {
            // Hiển thị chi tiết lỗi từ server
            const errMsg = (data && data.message) ? data.message
                         : (data && data.error) ? data.error
                         : (typeof data === 'string' && data.length > 0) ? data
                         : "Lỗi upload (HTTP " + response.status + ")";
            alert("Upload thất bại: " + errMsg);
            console.error("Upload error response:", data);
            return;
        }

        if (data.url) {
            document.getElementById("avatarUrl").value = data.url;
            if (document.getElementById("profile-avatar-img")) {
                document.getElementById("profile-avatar-img").src = data.url;
            }
            if (document.getElementById("header-user-avatar")) {
                document.getElementById("header-user-avatar").src = data.url;
            }
            alert("Upload ảnh thành công! Bạn nhớ bấm 'Lưu thay đổi' để cập nhật hồ sơ nhé.");
        } else {
            alert("Upload thành công nhưng không nhận được URL ảnh. Response: " + JSON.stringify(data));
        }
    } catch (error) {
        // Lỗi mạng / Failed to fetch => hiển thị chi tiết
        console.error("Upload network error:", error);
        alert("Lỗi kết nối khi upload ảnh.\n\n" +
              "Chi tiết: " + (error.message || error) + "\n\n" +
              "Nguyên nhân có thể:\n" +
              "1. Server chưa khởi động hoặc đang restart\n" +
              "2. Cấu hình Cloudinary trong application-secret.yml chưa đúng\n" +
              "3. File ảnh quá lớn (max 2MB)\n" +
              "4. Mạng bị gián đoạn");
    }
}
