document.addEventListener("DOMContentLoaded", () => {
    // Yêu cầu quyền ADMIN
    window.authService.requireAuth(['ADMIN']);

    // Thiết lập ListHelper
    window.ListHelper.init("/api/admin/users", renderTable, "name", "asc");

    // Khôi phục filter từ URL nếu có
    const params = new URLSearchParams(window.location.search);
    if (params.has("role")) {
        document.getElementById("filterRole").value = params.get("role");
        window.ListHelper.state.additionalParams.role = params.get("role");
    }
    if (params.has("status")) {
        document.getElementById("filterStatus").value = params.get("status");
        window.ListHelper.state.additionalParams.status = params.get("status");
    }
});

function renderTable(response) {
    const tbody = document.getElementById("tableBody");
    tbody.innerHTML = "";

    if (!response || !response.content || response.content.length === 0) {
        tbody.innerHTML = `<tr><td colspan="9" class="px-6 py-12 text-center text-gray-500 text-sm">Không tìm thấy dữ liệu.</td></tr>`;
        return;
    }

    const { page, size, content } = response;
    const startIndex = page * size + 1;

    content.forEach((user, index) => {
        const tr = document.createElement("tr");
        tr.className = "hover:bg-gray-50 transition-colors";

        const avatar = user.avatarUrl 
            ? `<img src="${user.avatarUrl}" alt="Avatar" class="h-10 w-10 rounded-full object-cover border border-gray-200">`
            : `<div class="h-10 w-10 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 font-bold text-sm border border-blue-200">${user.fullName ? user.fullName.charAt(0) : user.username.charAt(0)}</div>`;

        const roleBadge = getRoleBadge(user.role);
        const statusBadge = getStatusBadge(user.status);

        tr.innerHTML = `
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${startIndex + index}</td>
            <td class="px-6 py-4 whitespace-nowrap">${avatar}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">${user.username}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-700">${user.fullName || '-'}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${user.email || '-'}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${user.phone || '-'}</td>
            <td class="px-6 py-4 whitespace-nowrap">${roleBadge}</td>
            <td class="px-6 py-4 whitespace-nowrap">${statusBadge}</td>
            <td class="px-6 py-4 whitespace-nowrap text-right text-sm font-medium space-x-2">
                <button onclick="editUser(${user.id})" class="text-blue-600 hover:text-blue-900" title="Sửa">
                    <svg class="w-5 h-5 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path></svg>
                </button>
                ${user.status === 'ACTIVE' 
                    ? `<button onclick="lockUser(${user.id})" class="text-yellow-600 hover:text-yellow-900" title="Khóa"><svg class="w-5 h-5 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8V7a4 4 0 00-8 0v4h8z"></path></svg></button>`
                    : `<button onclick="unlockUser(${user.id})" class="text-green-600 hover:text-green-900" title="Mở khóa"><svg class="w-5 h-5 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 11V7a4 4 0 118 0m-4 8v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2z"></path></svg></button>`
                }
                <button onclick="deleteUser(${user.id})" class="text-red-600 hover:text-red-900" title="Xóa">
                    <svg class="w-5 h-5 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                </button>
            </td>
        `;
        tbody.appendChild(tr);
    });
}

function getRoleBadge(role) {
    switch (role) {
        case 'ADMIN': return `<span class="px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full bg-red-100 text-red-800">Admin</span>`;
        case 'TEACHER': return `<span class="px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full bg-green-100 text-green-800">Giáo viên</span>`;
        case 'STUDENT': return `<span class="px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full bg-blue-100 text-blue-800">Học sinh</span>`;
        default: return `<span class="px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full bg-gray-100 text-gray-800">${role}</span>`;
    }
}

function getStatusBadge(status) {
    if (status === 'ACTIVE') return `<span class="px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full bg-green-100 text-green-800">Hoạt động</span>`;
    if (status === 'INACTIVE') return `<span class="px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full bg-gray-100 text-gray-800">Đã khóa</span>`;
    return `<span class="px-2 py-1 inline-flex text-xs leading-5 font-semibold rounded-full bg-red-100 text-red-800">${status}</span>`;
}

// Chức năng thao tác
async function lockUser(id) {
    if (!confirm("Bạn có chắc chắn muốn khóa tài khoản này?")) return;
    try {
        await window.apiFetch(`/api/admin/users/${id}/status?status=INACTIVE`, { method: "PUT" });
        window.ListHelper.load(); // Reload table
    } catch (e) {
        alert("Lỗi: " + e.message);
    }
}

async function unlockUser(id) {
    try {
        await window.apiFetch(`/api/admin/users/${id}/status?status=ACTIVE`, { method: "PUT" });
        window.ListHelper.load();
    } catch (e) {
        alert("Lỗi: " + e.message);
    }
}

async function deleteUser(id) {
    if (!confirm("Bạn có chắc chắn muốn xóa (chuyển sang trạng thái khóa vĩnh viễn) tài khoản này?")) return;
    try {
        await window.apiFetch(`/api/admin/users/${id}`, { method: "DELETE" });
        window.ListHelper.load();
    } catch (e) {
        alert("Lỗi: " + e.message);
    }
}

function editUser(id) {
    alert("Chức năng sửa thông tin user đang được phát triển.");
}
function openModal(id) {
    alert("Chức năng thêm mới user đang được phát triển.");
}
