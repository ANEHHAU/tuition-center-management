const CourseManager = {
    init() {
        window.ListHelper.init(
            '/api/teacher/courses',
            (res) => this.renderTable(res),
            'name',
            'asc'
        );
    },

    renderTable(res) {
        const tbody = document.getElementById('tableBody');
        if (!tbody) return;
        
        if (!res.content || res.content.length === 0) {
            tbody.innerHTML = `<tr><td colspan="5" class="text-center py-4 text-gray-500">Không tìm thấy khóa học nào.</td></tr>`;
            return;
        }

        tbody.innerHTML = res.content.map(c => `
            <tr class="border-b hover:bg-gray-50 transition-colors">
                <td class="px-4 py-3">
                    <div class="font-medium text-gray-800">${c.name}</div>
                </td>
                <td class="px-4 py-3">${Number(c.pricePerSession).toLocaleString('vi-VN')} đ</td>
                <td class="px-4 py-3">${c.groupCount || 0}</td>
                <td class="px-4 py-3">
                    <span class="px-2 py-1 text-xs font-semibold rounded-full ${
                        c.status === 'ACTIVE' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                    }">${c.status}</span>
                </td>
                <td class="px-4 py-3 space-x-2">
                    <a href="/teacher/course-form?id=${c.id}" class="text-blue-600 hover:text-blue-800 font-medium text-sm">Sửa</a>
                    <button onclick="CourseManager.deleteCourse(${c.id})" class="text-red-600 hover:text-red-800 font-medium text-sm">Xóa</button>
                    <a href="/teacher/groups?courseId=${c.id}" class="text-indigo-600 hover:text-indigo-800 font-medium text-sm">Xem nhóm</a>
                </td>
            </tr>
        `).join('');
    },

    async deleteCourse(id) {
        if (!confirm('Bạn chắc chắn muốn xóa khóa học này?')) return;
        try {
            await window.apiFetch('/api/teacher/courses/' + id, { method: 'DELETE' });
            window.ListHelper.load();
        } catch (e) {
            alert(e.message);
        }
    }
};

window.CourseManager = CourseManager;
