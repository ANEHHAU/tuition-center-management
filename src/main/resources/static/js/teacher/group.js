const GroupManager = {
    init() {
        window.ListHelper.init(
            '/api/teacher/groups',
            (res) => this.renderTable(res),
            'name',
            'asc'
        );
        this.loadCourses();
    },

    async loadCourses() {
        try {
            const res = await window.apiFetch('/api/teacher/courses?size=100');
            const select = document.getElementById('filterCourse');
            if (select && res.content) {
                res.content.forEach(c => {
                    const opt = document.createElement('option');
                    opt.value = c.id;
                    opt.textContent = c.name;
                    select.appendChild(opt);
                });
            }
        } catch (e) {
            console.error(e);
        }
    },

    renderTable(res) {
        const tbody = document.getElementById('tableBody');
        if (!tbody) return;
        
        if (!res.content || res.content.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="text-center py-4 text-gray-500">Không tìm thấy nhóm học nào.</td></tr>`;
            return;
        }

        tbody.innerHTML = res.content.map(g => `
            <tr class="border-b hover:bg-gray-50 transition-colors">
                <td class="px-4 py-3">
                    <div class="font-medium text-gray-800">${g.name}</div>
                </td>
                <td class="px-4 py-3 text-gray-600">${g.courseName}</td>
                <td class="px-4 py-3">${g.studentCount || 0}</td>
                <td class="px-4 py-3">${g.startDate ? g.startDate : ''}</td>
                <td class="px-4 py-3">
                    <span class="px-2 py-1 text-xs font-semibold rounded-full ${
                        g.status === 'ACTIVE' ? 'bg-green-100 text-green-800' : 'bg-gray-100 text-gray-800'
                    }">${g.status}</span>
                </td>
                <td class="px-4 py-3 space-x-2">
                    <a href="/teacher/group-detail?id=${g.id}" class="text-indigo-600 hover:text-indigo-800 font-medium text-sm">Chi tiết</a>
                    <a href="/teacher/group-form?id=${g.id}" class="text-blue-600 hover:text-blue-800 font-medium text-sm">Sửa</a>
                    <button onclick="GroupManager.deleteGroup(${g.id})" class="text-red-600 hover:text-red-800 font-medium text-sm">Xóa</button>
                </td>
            </tr>
        `).join('');
    },

    async deleteGroup(id) {
        if (!confirm('Bạn chắc chắn muốn xóa nhóm học này?')) return;
        try {
            await window.apiFetch('/api/teacher/groups/' + id, { method: 'DELETE' });
            window.ListHelper.load();
        } catch (e) {
            alert(e.message);
        }
    }
};

window.GroupManager = GroupManager;
