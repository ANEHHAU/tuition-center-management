/**
 * attendance.js - Quản lý điểm danh
 */
const AttendanceManager = {
    apiBase: '/api/teacher/attendance',

    // Tải danh sách HS cần điểm danh cho 1 session
    async loadAttendance(sessionId) {
        try {
            const data = await apiFetch(this.apiBase + '/session/' + sessionId);
            this.renderForm(data);
        } catch (e) {
            document.getElementById('error-msg').textContent = e.message;
        }
    },

    renderForm(data) {
        const container = document.getElementById('attendance-list');
        if (!container || !data.items) return;
        container.innerHTML = data.items.map((item, i) => `
            <tr class="border-b">
                <td class="px-4 py-2 text-sm">${item.studentName}</td>
                <td class="px-4 py-2 text-sm">
                    <div class="flex flex-wrap gap-2">
                        <label class="inline-flex items-center">
                            <input type="radio" name="status_${item.studentId}" value="PRESENT" ${item.status === 'PRESENT' ? 'checked' : ''} class="mr-1 text-green-600"> Có mặt
                        </label>
                        <label class="inline-flex items-center">
                            <input type="radio" name="status_${item.studentId}" value="ABSENT" ${item.status === 'ABSENT' ? 'checked' : ''} class="mr-1 text-red-600"> Vắng
                        </label>
                        <label class="inline-flex items-center">
                            <input type="radio" name="status_${item.studentId}" value="EXCUSED" ${item.status === 'EXCUSED' ? 'checked' : ''} class="mr-1 text-yellow-600"> Có phép
                        </label>
                    </div>
                </td>
                <td class="px-4 py-2 text-sm">
                    <input type="text" name="note_${item.studentId}" value="${item.note || ''}" class="w-full border rounded px-2 py-1 text-sm" placeholder="Ghi chú">
                </td>
                <input type="hidden" name="studentId_${i}" value="${item.studentId}">
            </tr>
        `).join('');
        // Lưu danh sách studentIds
        container.dataset.students = JSON.stringify(data.items.map(i => i.studentId));
    },

    async saveAttendance(sessionId) {
        const container = document.getElementById('attendance-list');
        const studentIds = JSON.parse(container.dataset.students || '[]');
        
        const items = studentIds.map(sid => {
            const statusEl = document.querySelector(`input[name="status_${sid}"]:checked`);
            const noteEl = document.querySelector(`input[name="note_${sid}"]`);
            return {
                studentId: sid,
                status: statusEl ? statusEl.value : 'ABSENT',
                note: noteEl ? noteEl.value : ''
            };
        });

        try {
            await apiFetch(this.apiBase + '/session/' + sessionId, {
                method: 'POST',
                body: JSON.stringify({ items: items })
            });
            alert('Lưu điểm danh thành công!');
        } catch (e) {
            alert(e.message);
        }
    },

    // Lịch sử điểm danh của HS (dùng cho teacher xem hoặc student tự xem)
    async loadStudentHistory(studentId, from, to, apiUrl) {
        const url = (apiUrl || this.apiBase + '/student/' + studentId) + '?from=' + from + '&to=' + to;
        try {
            const items = await apiFetch(url);
            this.renderHistory(items);
        } catch (e) {
            document.getElementById('error-msg').textContent = e.message;
        }
    },

    renderHistory(items) {
        const tbody = document.getElementById('history-tbody');
        if (!tbody) return;
        tbody.innerHTML = items.map(a => `
            <tr class="border-b">
                <td class="px-4 py-2 text-sm">${a.studentName}</td>
                <td class="px-4 py-2 text-sm">
                    <span class="px-2 py-1 rounded-full text-xs font-medium ${a.status === 'PRESENT' ? 'bg-green-100 text-green-800' : a.status === 'ABSENT' ? 'bg-red-100 text-red-800' : a.status === 'EXCUSED' ? 'bg-yellow-100 text-yellow-800' : 'bg-gray-100 text-gray-800'}">${a.status}</span>
                </td>
                <td class="px-4 py-2 text-sm">${a.note || ''}</td>
                <td class="px-4 py-2 text-sm">${a.recordedAt ? new Date(a.recordedAt).toLocaleString('vi-VN') : ''}</td>
            </tr>
        `).join('');
    }
};
window.AttendanceManager = AttendanceManager;
