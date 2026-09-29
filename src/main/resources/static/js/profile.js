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
 * Xử lý Upload Avatar
 */
async function handleUploadAvatar(file) {
    const formData = new FormData();
    formData.append("file", file);

    try {
        const response = await window.apiFetch("/api/files/avatar", {
            method: "POST",
            body: formData
        });

        if (response.url) {
            document.getElementById("avatarUrl").value = response.url;
            if (document.getElementById("profile-avatar-img")) {
                document.getElementById("profile-avatar-img").src = response.url;
            }
            alert("Upload ảnh đại diện thành công!");
        }
    } catch (error) {
        alert("Lỗi upload ảnh: " + error.message);
    }
}
