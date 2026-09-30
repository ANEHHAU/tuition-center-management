document.addEventListener("DOMContentLoaded", () => {
    window.authService.requireAuth(['ADMIN']);
    window.ListHelper.init("/api/admin/courses", renderTable, "name", "asc");

    const params = new URLSearchParams(window.location.search);
    if (params.has("status")) {
        document.getElementById("filterStatus").value = params.get("status");
        window.ListHelper.state.additionalParams.status = params.get("status");
    }
});

function renderTable(response) {
    const tbody = document.getElementById("tableBody");
    tbody.innerHTML = "";
    if (!response || !response.content || response.content.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="px-6 py-12 text-center text-gray-500 text-sm">Không tìm thấy khóa học nào.</td></tr>`;
        return;
    }
    const { page, size, content } = response;
    content.forEach((c, i) => {
        const tr = document.createElement("tr");
        tr.className = "hover:bg-gray-50 transition-colors";
        const statusBadge = c.status === 'ACTIVE'
            ? `<span class="px-2 py-1 text-xs font-semibold rounded-full bg-green-100 text-green-800">Hoạt động</span>`
            : `<span class="px-2 py-1 text-xs font-semibold rounded-full bg-gray-100 text-gray-800">Ngừng</span>`;
        const price = c.pricePerSession ? Number(c.pricePerSession).toLocaleString('vi-VN') + ' đ' : '-';
        tr.innerHTML = `
            <td class="px-6 py-4 text-sm text-gray-500">${page * size + i + 1}</td>
            <td class="px-6 py-4 text-sm font-medium text-gray-900">${c.name || '-'}</td>
            <td class="px-6 py-4 text-sm text-gray-700">${c.teacherName || '-'}</td>
            <td class="px-6 py-4 text-sm text-gray-700">${price}</td>
            <td class="px-6 py-4 text-sm text-gray-700">${c.groupCount != null ? c.groupCount : '-'}</td>
            <td class="px-6 py-4">${statusBadge}</td>
            <td class="px-6 py-4 text-right text-sm font-medium space-x-2">
                <a href="/admin/course-form?id=${c.id}" class="text-blue-600 hover:text-blue-900" title="Sửa">✏️</a>
                <a href="/admin/groups?courseId=${c.id}" class="text-green-600 hover:text-green-900" title="Xem group">📋</a>
                <button onclick="deleteCourse(${c.id})" class="text-red-600 hover:text-red-900" title="Xóa">🗑️</button>
            </td>`;
        tbody.appendChild(tr);
    });
}

async function deleteCourse(id) {
    if (!confirm("Bạn có chắc chắn muốn xóa khóa học này?")) return;
    try {
        await window.apiFetch(`/api/admin/courses/${id}`, { method: "DELETE" });
        window.ListHelper.load();
    } catch (e) { alert("Lỗi: " + (e.message || e)); }
}
