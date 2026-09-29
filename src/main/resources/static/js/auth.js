/**
 * Quản lý Đăng nhập, Đăng ký, Đăng xuất và Bảo vệ trang phía Frontend
 */

const AUTH_KEY_TOKEN = "token";
const AUTH_KEY_USER = "user";

/**
 * Đăng nhập tài khoản
 */
async function login(username, password) {
    try {
        const data = await window.apiFetch("/api/auth/login", {
            method: "POST",
            body: JSON.stringify({ username, password })
        });

        // Lưu thông tin xác thực vào localStorage
        localStorage.setItem(AUTH_KEY_TOKEN, data.token);
        localStorage.setItem(AUTH_KEY_USER, JSON.stringify(data));

        // Điều hướng theo Role
        redirectByRole(data.role);
    } catch (error) {
        throw error;
    }
}

/**
 * Đăng ký tài khoản công khai
 */
async function register(registerData) {
    try {
        await window.apiFetch("/api/auth/register", {
            method: "POST",
            body: JSON.stringify(registerData)
        });
        alert("Đăng ký tài khoản thành công! Vui lòng đăng nhập.");
        window.location.href = "/login";
    } catch (error) {
        throw error;
    }
}

/**
 * Đăng xuất khỏi hệ thống
 */
function logout() {
    localStorage.removeItem(AUTH_KEY_TOKEN);
    localStorage.removeItem(AUTH_KEY_USER);
    window.location.href = "/login";
}

/**
 * Lấy thông tin user đăng nhập hiện tại từ localStorage
 */
function getCurrentUser() {
    const userJson = localStorage.getItem(AUTH_KEY_USER);
    return userJson ? JSON.parse(userJson) : null;
}

/**
 * Điều hướng trang dựa trên Role của User
 */
function redirectByRole(role) {
    switch (role) {
        case "ADMIN":
            window.location.href = "/admin";
            break;
        case "TEACHER":
            window.location.href = "/teacher";
            break;
        case "STUDENT":
            window.location.href = "/student";
            break;
        default:
            window.location.href = "/login";
    }
}

/**
 * Hàm bảo vệ trang - Yêu cầu xác thực & đúng Role
 */
function requireAuth(allowedRoles = []) {
    const token = localStorage.getItem(AUTH_KEY_TOKEN);
    const user = getCurrentUser();

    if (!token || !user) {
        window.location.href = "/login";
        return null;
    }

    if (allowedRoles.length > 0 && !allowedRoles.includes(user.role)) {
        window.location.href = "/error/403";
        return null;
    }

    return user;
}

// Export các hàm ra window
window.authService = {
    login,
    register,
    logout,
    getCurrentUser,
    requireAuth,
    redirectByRole
};
