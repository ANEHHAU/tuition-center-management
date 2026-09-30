/**
 * group.js - Quản lý nhóm/lớp học (CRUD + public link + thêm/xóa HS)
 */
const GroupManager = {
    apiBase: '',

    init(role) {
        this.apiBase = role === 'ADMIN' ? '/api/admin/groups' : '/api/teacher/groups';
        this.loadGroups();
    },

    async loadGroups() {
        try {
            const groups = await apiFetch(this.apiBase);
            this.renderTable(groups);
        } catch (e) {
            document.getElementById('error-msg').textContent = e.message;
        }
    },

    renderTable(groups) {
        const tbody = document.getElementById('groups-tbody');
        if (!tbody) return;
        tbody.innerHTML = groups.map(g => `
            <tr class="border-b hover:bg-gray-50">
                <td class="px-4 py-3 text-sm">${g.id}</td>
                <td class="px-4 py-3 text-sm font-medium">${g.name}</td>
                <td class="px-4 py-3 text-sm">${g.courseName || ''}</td>
                <td class="px-4 py-3 text-sm">${g.status || 'ACTIVE'}</td>
                <td class="px-4 py-3 text-sm">${g.studentCount || 0}</td>
                <td class="px-4 py-3 text-sm space-x-2">
                    <a href="group-detail?id=${g.id}" class="text-blue-600 hover:underline">Chi tiết</a>
                    <a href="group-form?id=${g.id}" class="text-yellow-600 hover:underline">Sửa</a>
                    <button onclick="GroupManager.deleteGroup(${g.id})" class="text-red-600 hover:underline">Xóa</button>
                </td>
            </tr>
        `).join('');
    },

    async deleteGroup(id) {
        if (!confirm('Xóa nhóm?')) return;
        try {
            await apiFetch(this.apiBase + '/' + id, { method: 'DELETE' });
            this.loadGroups();
        } catch (e) { alert(e.message); }
    },

    async submitForm(e) {
        e.preventDefault();
        const form = e.target;
        const id = form.dataset.id;
        const body = {
            courseId: parseInt(form.courseId.value),
            name: form.name.value,
            startDate: form.startDate.value || null,
            endDate: form.endDate.value || null,
            status: form.status.value
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

    async loadFormData(id) {
        if (!id) return;
        try {
            const g = await apiFetch(this.apiBase + '/' + id);
            const form = document.getElementById('group-form');
            form.dataset.id = g.id;
            form.name.value = g.name;
            form.courseId.value = g.courseId;
            form.startDate.value = g.startDate || '';
            form.endDate.value = g.endDate || '';
            form.status.value = g.status || 'ACTIVE';
        } catch (e) {
            document.getElementById('error-msg').textContent = e.message;
        }
    },

    // ===== Group Detail: Public Link =====
    async regenerateLink(groupId) {
        try {
            const res = await apiFetch(this.apiBase + '/' + groupId + '/public-link/regenerate', { method: 'POST' });
            document.getElementById('public-link-url').textContent = window.location.origin + res.url;
            document.getElementById('public-link-section').classList.remove('hidden');
            alert('Tạo link thành công!');
        } catch (e) { alert(e.message); }
    },

    async revokeLink(groupId) {
        try {
            await apiFetch(this.apiBase + '/' + groupId + '/public-link/revoke', { method: 'PUT' });
            document.getElementById('public-link-url').textContent = 'Đã vô hiệu hóa';
            alert('Đã hủy link!');
        } catch (e) { alert(e.message); }
    },

    copyLink() {
        const url = document.getElementById('public-link-url').textContent;
        navigator.clipboard.writeText(url).then(() => alert('Đã sao chép!'));
    },

    // ===== Group Detail: Students =====
    async loadStudents(groupId) {
        try {
            const students = await apiFetch(this.apiBase + '/' + groupId + '/students');
            const tbody = document.getElementById('students-tbody');
            if (!tbody) return;
            tbody.innerHTML = students.map(s => `
                <tr class="border-b">
                    <td class="px-4 py-2 text-sm">${s.studentId}</td>
                    <td class="px-4 py-2 text-sm">${s.studentName}</td>
                    <td class="px-4 py-2 text-sm">${s.joinDate}</td>
                    <td class="px-4 py-2 text-sm">${s.status}</td>
                    <td class="px-4 py-2 text-sm">
                        <button onclick="GroupManager.removeStudent(${groupId}, ${s.studentId})" class="text-red-600 hover:underline">Xóa</button>
                    </td>
                </tr>
            `).join('');
        } catch (e) {
            document.getElementById('error-msg').textContent = e.message;
        }
    },

    async addStudent(groupId) {
        const studentId = document.getElementById('add-student-id').value;
        if (!studentId) return alert('Nhập mã học sinh');
        try {
            await apiFetch(this.apiBase + '/' + groupId + '/students', {
                method: 'POST',
                body: JSON.stringify({ studentId: parseInt(studentId), joinDate: new Date().toISOString().split('T')[0] })
            });
            this.loadStudents(groupId);
            document.getElementById('add-student-id').value = '';
        } catch (e) { alert(e.message); }
    },

    async removeStudent(groupId, studentId) {
        if (!confirm('Xóa học sinh khỏi nhóm?')) return;
        try {
            await apiFetch(this.apiBase + '/' + groupId + '/students/' + studentId, { method: 'DELETE' });
            this.loadStudents(groupId);
        } catch (e) { alert(e.message); }
    }
};
window.GroupManager = GroupManager;
