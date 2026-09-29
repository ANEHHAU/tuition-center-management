/**
 * Wrapper xung quanh Fetch API tự động đính kèm JWT Bearer Token và xử lý HTTP 401 Unauthorized
 */
async function apiFetch(url, options = {}) {
    const token = localStorage.getItem("token");

    // Khởi tạo headers
    options.headers = options.headers || {};

    // Gắn Bearer Token nếu người dùng đã đăng nhập
    if (token) {
        options.headers["Authorization"] = "Bearer " + token;
    }

    // Tự động thêm Content-Type JSON nếu body không phải FormData
    if (options.body && !(options.body instanceof FormData) && !options.headers["Content-Type"]) {
        options.headers["Content-Type"] = "application/json";
    }

    try {
        const response = await fetch(url, options);

        // Nếu Token hết hạn hoặc không hợp lệ (HTTP 401) -> Tự động đăng xuất
        if (response.status === 401) {
            localStorage.removeItem("token");
            localStorage.removeItem("user");
            window.location.href = "/login";
            return Promise.reject("Unauthorized");
        }

        // Xử lý đọc response JSON hoặc Text
        const contentType = response.headers.get("content-type");
        let data;
        if (contentType && contentType.includes("application/json")) {
            data = await response.json();
        } else {
            data = await response.text();
        }

        if (!response.ok) {
            const errorMessage = (data && data.message) ? data.message : "Đã có lỗi xảy ra (Status: " + response.status + ")";
            return Promise.reject(new Error(errorMessage));
        }

        return data;
    } catch (error) {
        console.error("API Fetch Error:", error);
        throw error;
    }
}

// Export ra window toàn cục
window.apiFetch = apiFetch;
