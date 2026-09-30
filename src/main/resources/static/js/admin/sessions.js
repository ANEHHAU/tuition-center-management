document.addEventListener("DOMContentLoaded", () => {
    window.authService.requireAuth(['ADMIN']);
    const params = new URLSearchParams(window.location.search);
    if (params.has("groupId")) window.ListHelper.state.additionalParams.groupId = params.get("groupId");
    if (params.has("status")) { document.getElementById("filterStatus").value = params.get("status"); window.ListHelper.state.additionalParams.status = params.get("status"); }
    window.ListHelper.init("/api/admin/sessions", renderTable, "date", "desc");
});

function renderTable(response) {
    const tbody = document.getElementById("tableBody");
    tbody.innerHTML = "";
    if (!response || !response.content || response.content.length === 0) {
        tbody.innerHTML = `<tr><td colspan="7" class="px-6 py-12 text-center text-gray-500 text-sm">Không tìm thấy buổi học nào.</td></tr>`;
        return;
    }
    const { page, size, content } = response;
    content.forEach((s, i) => {
        const tr = document.createElement("tr");
        tr.className = "hover:bg-gray-50 transition-colors";
        let statusBadge;
        switch(s.status) {
            case 'SCHEDULED': statusBadge = `<span class="px-2 py-1 text-xs font-semibold rounded-full bg-blue-100 text-blue-800">Đã lên lịch</span>`; break;
            case 'COMPLETED': statusBadge = `<span class="px-2 py-1 text-xs font-semibold rounded-full bg-green-100 text-green-800">Hoàn thành</span>`; break;
            case 'CANCELLED': statusBadge = `<span class="px-2 py-1 text-xs font-semibold rounded-full bg-red-100 text-red-800">Đã hủy</span>`; break;
            default: statusBadge = `<span class="px-2 py-1 text-xs font-semibold rounded-full bg-gray-100 text-gray-800">${s.status}</span>`;
        }
        tr.innerHTML = `
            <td class="px-6 py-4 text-sm text-gray-500">${page * size + i + 1}</td>
            <td class="px-6 py-4 text-sm font-medium text-gray-900">${s.date || '-'}</td>
            <td class="px-6 py-4 text-sm text-gray-700">${s.startTime || ''} - ${s.endTime || ''}</td>
            <td class="px-6 py-4 text-sm text-gray-700">${s.groupName || '-'}</td>
            <td class="px-6 py-4 text-sm text-gray-700">${s.room || '-'}</td>
            <td class="px-6 py-4">${statusBadge}</td>
            <td class="px-6 py-4 text-right text-sm font-medium space-x-2">
                <a href="/admin/attendance?sessionId=${s.id}" class="text-green-600 hover:text-green-900" title="Điểm danh">📋</a>
                <a href="/admin/session-form?id=${s.id}" class="text-blue-600 hover:text-blue-900" title="Sửa">✏️</a>
                ${s.status === 'SCHEDULED' ? `<button onclick="cancelSession(${s.id})" class="text-yellow-600 hover:text-yellow-900" title="Hủy buổi">⚠️</button>` : ''}
                <button onclick="deleteSession(${s.id})" class="text-red-600 hover:text-red-900" title="Xóa">🗑️</button>
            </td>`;
        tbody.appendChild(tr);
    });
}

async function cancelSession(id) {
    if (!confirm("Hủy buổi học này?")) return;
    try { await window.apiFetch(`/api/admin/sessions/${id}/cancel`, { method: "POST" }); window.ListHelper.load(); }
    catch (e) { alert("Lỗi: " + (e.message || e)); }
}
async function deleteSession(id) {
    if (!confirm("Xóa buổi học này?")) return;
    try { await window.apiFetch(`/api/admin/sessions/${id}`, { method: "DELETE" }); window.ListHelper.load(); }
    catch (e) { alert("Lỗi: " + (e.message || e)); }
}
