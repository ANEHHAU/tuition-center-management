document.addEventListener("DOMContentLoaded", () => {
    window.authService.requireAuth(['ADMIN']);
    
    // Khôi phục filter từ URL
    const params = new URLSearchParams(window.location.search);
    if (params.has("courseId")) window.ListHelper.state.additionalParams.courseId = params.get("courseId");
    if (params.has("status")) {
        document.getElementById("filterStatus").value = params.get("status");
        window.ListHelper.state.additionalParams.status = params.get("status");
    }
    
    window.ListHelper.init("/api/admin/groups", renderTable, "name", "asc");
});

function renderTable(response) {
    const tbody = document.getElementById("tableBody");
    tbody.innerHTML = "";
    if (!response || !response.content || response.content.length === 0) {
        tbody.innerHTML = `<tr><td colspan="8" class="px-6 py-12 text-center text-gray-500 text-sm">Không tìm thấy nhóm học nào.</td></tr>`;
        return;
    }
    const { page, size, content } = response;
    content.forEach((g, i) => {
        const tr = document.createElement("tr");
        tr.className = "hover:bg-gray-50 transition-colors";
        const statusBadge = g.status === 'ACTIVE'
            ? `<span class="px-2 py-1 text-xs font-semibold rounded-full bg-green-100 text-green-800">Hoạt động</span>`
            : `<span class="px-2 py-1 text-xs font-semibold rounded-full bg-gray-100 text-gray-800">Ngừng</span>`;
        tr.innerHTML = `
            <td class="px-6 py-4 text-sm text-gray-500">${page * size + i + 1}</td>
            <td class="px-6 py-4 text-sm font-medium text-gray-900">${g.name || '-'}</td>
            <td class="px-6 py-4 text-sm text-gray-700">${g.courseName || '-'}</td>
            <td class="px-6 py-4 text-sm text-gray-700">${g.teacherName || '-'}</td>
            <td class="px-6 py-4 text-sm text-gray-700">${g.studentCount != null ? g.studentCount : '-'}</td>
            <td class="px-6 py-4 text-sm text-gray-700">${g.startDate || '-'}</td>
            <td class="px-6 py-4">${statusBadge}</td>
            <td class="px-6 py-4 text-right text-sm font-medium space-x-2">
                <a href="/admin/group-detail?id=${g.id}" class="text-blue-600 hover:text-blue-900" title="Chi tiết">👁️</a>
                <a href="/admin/group-form?id=${g.id}" class="text-yellow-600 hover:text-yellow-900" title="Sửa">✏️</a>
                <button onclick="deleteGroup(${g.id})" class="text-red-600 hover:text-red-900" title="Xóa">🗑️</button>
            </td>`;
        tbody.appendChild(tr);
    });
}

async function deleteGroup(id) {
    if (!confirm("Bạn có chắc chắn muốn xóa nhóm này?")) return;
    try {
        await window.apiFetch(`/api/admin/groups/${id}`, { method: "DELETE" });
        window.ListHelper.load();
    } catch (e) { alert("Lỗi: " + (e.message || e)); }
}
