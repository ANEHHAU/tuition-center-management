const StudentManager = {
    init() {
        window.ListHelper.init(
            '/api/teacher/students',
            (res) => this.renderTable(res),
            'name',
            'asc'
        );
        this.bindModalEvents();
    },

    renderTable(res) {
        const tbody = document.getElementById('tableBody');
        if (!tbody) return;
        
        if (!res.content || res.content.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="text-center py-4 text-gray-500">Không tìm thấy học sinh nào.</td></tr>`;
            return;
        }

        tbody.innerHTML = res.content.map(s => `
            <tr class="border-b hover:bg-gray-50 transition-colors">
                <td class="px-4 py-3">
                    <div class="flex items-center space-x-3">
                        <div class="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 font-bold">
                            ${s.fullName ? s.fullName.charAt(0) : 'U'}
                        </div>
                        <div>
                            <div class="font-medium text-gray-800">${s.fullName}</div>
                            <div class="text-xs text-gray-500">${s.username}</div>
                        </div>
                    </div>
                </td>
                <td class="px-4 py-3 text-gray-600">${s.email || '-'}</td>
                <td class="px-4 py-3 text-gray-600">${s.phone || '-'}</td>
                <td class="px-4 py-3">
                    <span class="px-2 py-1 text-xs font-semibold rounded-full ${
                        s.status === 'ACTIVE' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                    }">${s.status}</span>
                </td>
                <td class="px-4 py-3 space-x-2">
                    <a href="/teacher/student-detail?id=${s.id}" class="text-indigo-600 hover:text-indigo-800 font-medium text-sm">Chi tiết</a>
                    <button onclick="StudentManager.openEditModal(${s.id})" class="text-blue-600 hover:text-blue-800 font-medium text-sm">Sửa</button>
                    <button onclick="StudentManager.openResetPasswordModal(${s.id})" class="text-yellow-600 hover:text-yellow-800 font-medium text-sm">Pass</button>
                    <button onclick="StudentManager.deleteStudent(${s.id})" class="text-red-600 hover:text-red-800 font-medium text-sm">Khóa</button>
                </td>
            </tr>
        `).join('');
    },

    bindModalEvents() {
        const form = document.getElementById('studentForm');
        if (form) {
            form.addEventListener('submit', async (e) => {
                e.preventDefault();
                await this.saveStudent();
            });
        }
        const pwForm = document.getElementById('passwordForm');
        if (pwForm) {
            pwForm.addEventListener('submit', async (e) => {
                e.preventDefault();
                await this.savePassword();
            });
        }
    },

    openCreateModal() {
        const form = document.getElementById('studentForm');
        form.reset();
        document.getElementById('studentId').value = '';
        document.getElementById('modalTitle').textContent = 'Thêm Học Sinh Mới';
        
        // Hiện password field khi tạo
        document.getElementById('passwordGroup').classList.remove('hidden');
        document.getElementById('studentPassword').required = true;

        document.getElementById('studentModal').classList.remove('hidden');
    },

    async openEditModal(id) {
        try {
            const student = await window.apiFetch('/api/teacher/students/' + id + '/detail');
            const form = document.getElementById('studentForm');
            document.getElementById('studentId').value = student.id;
            document.getElementById('studentUsername').value = student.username;
            document.getElementById('studentFullName').value = student.fullName;
            document.getElementById('studentEmail').value = student.email || '';
            document.getElementById('studentPhone').value = student.phone || '';
            
            // Ẩn password field khi sửa
            document.getElementById('passwordGroup').classList.add('hidden');
            document.getElementById('studentPassword').required = false;

            document.getElementById('modalTitle').textContent = 'Cập Nhật Học Sinh';
            document.getElementById('studentModal').classList.remove('hidden');
        } catch (e) {
            alert(e.message);
        }
    },

    closeModal() {
        document.getElementById('studentModal').classList.add('hidden');
    },

    async saveStudent() {
        const form = document.getElementById('studentForm');
        const id = document.getElementById('studentId').value;
        const body = {
            username: form.username.value,
            fullName: form.fullName.value,
            email: form.email.value,
            phone: form.phone.value
        };

        try {
            if (id) {
                await window.apiFetch('/api/teacher/students/' + id, {
                    method: 'PUT',
                    body: JSON.stringify(body)
                });
            } else {
                body.password = form.password.value;
                await window.apiFetch('/api/teacher/students', {
                    method: 'POST',
                    body: JSON.stringify(body)
                });
            }
            this.closeModal();
            window.ListHelper.load();
        } catch (e) {
            alert(e.message);
        }
    },

    async deleteStudent(id) {
        if (!confirm('Bạn chắc chắn muốn khóa học sinh này?')) return;
        try {
            await window.apiFetch('/api/teacher/students/' + id, { method: 'DELETE' });
            window.ListHelper.load();
        } catch (e) {
            alert(e.message);
        }
    },

    openResetPasswordModal(id) {
        document.getElementById('resetStudentId').value = id;
        document.getElementById('passwordForm').reset();
        document.getElementById('passwordModal').classList.remove('hidden');
    },
    
    closePasswordModal() {
        document.getElementById('passwordModal').classList.add('hidden');
    },
    
    async savePassword() {
        const id = document.getElementById('resetStudentId').value;
        const newPassword = document.getElementById('newPassword').value;
        try {
            await window.apiFetch('/api/teacher/students/' + id + '/reset-password', {
                method: 'PUT',
                body: JSON.stringify({ newPassword })
            });
            alert('Đặt lại mật khẩu thành công!');
            this.closePasswordModal();
        } catch (e) {
            alert(e.message);
        }
    }
};

window.StudentManager = StudentManager;
