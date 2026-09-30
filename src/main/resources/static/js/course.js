/**
 * course.js - Quản lý khóa học (CRUD)
 */
const CourseManager = {
    apiBase: '',

    init(role) {
        this.apiBase = role === 'ADMIN' ? '/api/admin/courses' : '/api/teacher/courses';
        this.loadCourses();
    },

    async loadCourses() {
        try {
            const courses = await apiFetch(this.apiBase);
            this.renderTable(courses);
        } catch (e) {
            document.getElementById('error-msg').textContent = e.message || 'Lỗi tải khóa học';
        }
    },

    renderTable(courses) {
        const tbody = document.getElementById('courses-tbody');
        if (!tbody) return;
        tbody.innerHTML = courses.map(c => `
            <tr class="border-b hover:bg-gray-50">
                <td class="px-4 py-3 text-sm">${c.id}</td>
                <td class="px-4 py-3 text-sm font-medium">${c.name}</td>
                <td class="px-4 py-3 text-sm">${Number(c.pricePerSession).toLocaleString('vi-VN')}đ</td>
                <td class="px-4 py-3 text-sm">${c.status || 'ACTIVE'}</td>
                <td class="px-4 py-3 text-sm">${c.groupCount || 0}</td>
                <td class="px-4 py-3 text-sm space-x-2">
                    <a href="course-form?id=${c.id}" class="text-blue-600 hover:underline">Sửa</a>
                    <button onclick="CourseManager.deleteCourse(${c.id})" class="text-red-600 hover:underline">Xóa</button>
                </td>
            </tr>
        `).join('');
    },

    async submitForm(e) {
        e.preventDefault();
        const form = e.target;
        const id = form.dataset.id;
        const body = {
            name: form.name.value,
            pricePerSession: parseFloat(form.pricePerSession.value),
            description: form.description.value,
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

    async deleteCourse(id) {
        if (!confirm('Bạn chắc chắn muốn xóa khóa học này?')) return;
        try {
            await apiFetch(this.apiBase + '/' + id, { method: 'DELETE' });
            this.loadCourses();
        } catch (e) {
            alert(e.message);
        }
    },

    async loadFormData(id) {
        if (!id) return;
        try {
            const c = await apiFetch(this.apiBase + '/' + id);
            const form = document.getElementById('course-form');
            form.dataset.id = c.id;
            form.name.value = c.name;
            form.pricePerSession.value = c.pricePerSession;
            form.description.value = c.description || '';
            form.status.value = c.status || 'ACTIVE';
        } catch (e) {
            document.getElementById('error-msg').textContent = e.message;
        }
    }
};
window.CourseManager = CourseManager;
