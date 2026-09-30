let sessionId = null;
let attendanceData = [];

document.addEventListener("DOMContentLoaded", () => {
    window.authService.requireAuth(['ADMIN']);
    const params = new URLSearchParams(window.location.search);
    sessionId = params.get("sessionId");
    if (!sessionId) { alert("Thiếu sessionId"); return; }
    loadSessionInfo();
    loadAttendance();
});

async function loadSessionInfo() {
    try {
        const s = await window.apiFetch(`/api/admin/sessions/${sessionId}`);
        document.getElementById("infoGroup").textContent = s.groupName || '-';
        document.getElementById("infoDate").textContent = s.date || '-';
        document.getElementById("infoTime").textContent = `${s.startTime || ''} - ${s.endTime || ''}`;
        document.getElementById("infoRoom").textContent = s.room || '-';
    } catch (e) { console.error("Lỗi tải thông tin session:", e); }
}

async function loadAttendance() {
    const tbody = document.getElementById("tableBody");
    try {
        const data = await window.apiFetch(`/api/admin/attendance?sessionId=${sessionId}`);
        attendanceData = data.content || data || [];
        tbody.innerHTML = "";
        if (attendanceData.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="px-6 py-8 text-center text-gray-500">Chưa có dữ liệu điểm danh. Hãy chạy điểm danh từ Session.</td></tr>`;
            return;
        }
        attendanceData.forEach((a, i) => {
            const tr = document.createElement("tr");
            tr.className = "hover:bg-gray-50";
            const price = a.priceSnapshot ? Number(a.priceSnapshot).toLocaleString('vi-VN') + ' đ' : '-';
            tr.innerHTML = `
                <td class="px-6 py-4 text-sm text-gray-500">${i + 1}</td>
                <td class="px-6 py-4 text-sm font-medium text-gray-900">${a.studentName || '-'}</td>
                <td class="px-6 py-4 text-sm text-gray-500">${a.studentEmail || '-'}</td>
                <td class="px-6 py-4">
                    <select data-id="${a.id}" class="attendance-status border border-gray-300 rounded-md py-1 px-2 text-sm">
                        <option value="PRESENT" ${a.status === 'PRESENT' ? 'selected' : ''}>Có mặt</option>
                        <option value="ABSENT" ${a.status === 'ABSENT' ? 'selected' : ''}>Vắng</option>
                        <option value="EXCUSED" ${a.status === 'EXCUSED' ? 'selected' : ''}>Có phép</option>
                        <option value="CANCELLED" ${a.status === 'CANCELLED' ? 'selected' : ''}>Hủy</option>
                    </select>
                </td>
                <td class="px-6 py-4 text-sm text-gray-700">${price}</td>
                <td class="px-6 py-4">
                    <input type="text" data-id="${a.id}" class="attendance-note border border-gray-300 rounded-md py-1 px-2 text-sm w-full" value="${a.note || ''}" placeholder="Ghi chú...">
                </td>`;
            tbody.appendChild(tr);
        });
    } catch (e) { tbody.innerHTML = `<tr><td colspan="6" class="px-6 py-8 text-center text-red-500">Lỗi tải điểm danh.</td></tr>`; }
}

async function saveAttendance() {
    const selects = document.querySelectorAll(".attendance-status");
    const notes = document.querySelectorAll(".attendance-note");
    const updates = [];
    selects.forEach(sel => {
        const id = sel.getAttribute("data-id");
        const noteInput = document.querySelector(`.attendance-note[data-id="${id}"]`);
        updates.push({ id: parseInt(id), status: sel.value, note: noteInput ? noteInput.value : '' });
    });
    try {
        for (const u of updates) {
            await window.apiFetch(`/api/admin/attendance/${u.id}`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ status: u.status, note: u.note })
            });
        }
        alert("Đã lưu điểm danh thành công!");
        loadAttendance();
    } catch (e) { alert("Lỗi lưu: " + (e.message || e)); }
}
