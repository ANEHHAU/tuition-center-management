/**
 * session.js - Quản lý buổi học (CRUD + calendar view)
 */
const SessionManager = {
    apiBase: '',

    init(role) {
        this.apiBase = role === 'ADMIN' ? '/api/admin/sessions' : '/api/teacher/sessions';
    },

    async loadSessions(groupId) {
        try {
            const sessions = await apiFetch(this.apiBase + '?groupId=' + groupId);
            this.renderTable(sessions);
        } catch (e) {
            document.getElementById('error-msg').textContent = e.message;
        }
    },

    renderTable(sessions) {
        const tbody = document.getElementById('sessions-tbody');
        if (!tbody) return;
        tbody.innerHTML = sessions.map(s => `
            <tr class="border-b hover:bg-gray-50">
                <td class="px-4 py-3 text-sm">${s.date}</td>
                <td class="px-4 py-3 text-sm">${s.startTime} - ${s.endTime}</td>
                <td class="px-4 py-3 text-sm">${s.room || ''}</td>
                <td class="px-4 py-3 text-sm">
                    <span class="px-2 py-1 rounded-full text-xs font-medium ${s.status === 'CANCELLED' ? 'bg-red-100 text-red-800' : s.status === 'COMPLETED' ? 'bg-green-100 text-green-800' : 'bg-blue-100 text-blue-800'}">${s.status}</span>
                </td>
                <td class="px-4 py-3 text-sm">${s.attendanceCount || 0}</td>
                <td class="px-4 py-3 text-sm space-x-2">
                    <a href="session-form?id=${s.id}&groupId=${s.groupId}" class="text-blue-600 hover:underline">Sửa</a>
                    <button onclick="SessionManager.cancelSession(${s.id})" class="text-orange-600 hover:underline">Hủy</button>
                    <button onclick="SessionManager.deleteSession(${s.id})" class="text-red-600 hover:underline">Xóa</button>
                </td>
            </tr>
        `).join('');
    },

    async submitForm(e) {
        e.preventDefault();
        const form = e.target;
        const id = form.dataset.id;
        const body = {
            groupId: parseInt(form.groupId.value),
            date: form.date.value,
            startTime: form.startTime.value,
            endTime: form.endTime.value,
            room: form.room.value,
            note: form.note.value
        };
        try {
            if (id) {
                await apiFetch(this.apiBase + '/' + id, { method: 'PUT', body: JSON.stringify(body) });
            } else {
                await apiFetch(this.apiBase, { method: 'POST', body: JSON.stringify(body) });
            }
            alert('Lưu thành công!');
            window.history.back();
        } catch (e) {
            document.getElementById('error-msg').textContent = e.message;
        }
    },

    async cancelSession(id) {
        if (!confirm('Hủy buổi học này? Attendance cũng sẽ bị hủy.')) return;
        try {
            await apiFetch(this.apiBase + '/' + id + '/cancel', { method: 'POST' });
            alert('Đã hủy buổi học!');
            location.reload();
        } catch (e) { alert(e.message); }
    },

    async deleteSession(id) {
        if (!confirm('Xóa buổi học?')) return;
        try {
            await apiFetch(this.apiBase + '/' + id, { method: 'DELETE' });
            location.reload();
        } catch (e) { alert(e.message); }
    },

    async loadFormData(id) {
        if (!id) return;
        try {
            const s = await apiFetch(this.apiBase + '/' + id);
            const form = document.getElementById('session-form');
            form.dataset.id = s.id;
            form.groupId.value = s.groupId;
            form.date.value = s.date;
            form.startTime.value = s.startTime;
            form.endTime.value = s.endTime;
            form.room.value = s.room || '';
            form.note.value = s.note || '';
        } catch (e) {
            document.getElementById('error-msg').textContent = e.message;
        }
    }
};
window.SessionManager = SessionManager;
