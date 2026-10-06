const StudentManager = {
    allGroups: [],
    allCourses: [],

    init() {
        window.ListHelper.init(
            '/api/teacher/students',
            (res) => this.renderTable(res),
            'name',
            'asc'
        );
        this.bindModalEvents();
        this.loadGroupsAndCourses();
    },

    async loadGroupsAndCourses() {
        try {
            const res = await window.apiFetch('/api/teacher/groups?size=200');
            if (res && Array.isArray(res.content)) {
                this.allGroups = res.content;
                // Extract unique courses
                const courseMap = {};
                res.content.forEach(g => {
                    if (g.courseId && !courseMap[g.courseId]) {
                        courseMap[g.courseId] = g.courseName;
                    }
                });
                this.allCourses = Object.entries(courseMap).map(([id, name]) => ({id: parseInt(id), name}));
            }
        } catch (e) {
            console.error('Lỗi tải nhóm/khóa', e);
        }
    },

    renderTable(res) {
        const tbody = document.getElementById('tableBody');
        if (!tbody) return;

        if (!res.content || res.content.length === 0) {
            tbody.innerHTML = `<tr><td colspan="7" class="text-center py-4 text-gray-500">Chưa có dữ liệu.</td></tr>`;
            return;
        }

        tbody.innerHTML = res.content.map(s => {
            let groupsHtml = '-';
            if (s.enrolledGroups && s.enrolledGroups.length > 0) {
                groupsHtml = s.enrolledGroups.map(g => `<div class="text-xs text-blue-600 bg-blue-50 rounded px-2 py-1 mb-1 border border-blue-100">${g}</div>`).join('');
            }

            const lockBtn = s.status === 'ACTIVE'
                ? `<button onclick="StudentManager.deleteStudent(${s.id})" class="text-yellow-600 hover:text-yellow-900" title="Khóa"><svg class="w-5 h-5 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 15v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2zm10-10V7a4 4 0 00-8 0v4h8V7a4 4 0 00-8 0v4h8z"></path></svg></button>`
                : `<button onclick="StudentManager.restoreStudent(${s.id})" class="text-green-600 hover:text-green-900" title="Mở khóa"><svg class="w-5 h-5 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8 11V7a4 4 0 118 0m-4 8v2m-6 4h12a2 2 0 002-2v-6a2 2 0 00-2-2H6a2 2 0 00-2 2v6a2 2 0 002 2z"></path></svg></button>`;

            return `
            <tr class="border-b hover:bg-gray-50 transition-colors">
                <td class="px-4 py-3">
                    <div class="flex items-center space-x-3">
                        <div class="w-8 h-8 rounded-full bg-blue-100 flex items-center justify-center text-blue-600 font-bold">
                            ${s.fullName ? s.fullName.charAt(0) : 'U'}
                        </div>
                        <div>
                            <div class="font-medium text-gray-800">${s.fullName}</div>
                        </div>
                    </div>
                </td>
                <td class="px-4 py-3 text-gray-600">${s.email || '-'}</td>
                <td class="px-4 py-3 text-gray-600">${s.phone || '-'}</td>
                <td class="px-4 py-3">${groupsHtml}</td>
                <td class="px-4 py-3">
                    <span class="px-2 py-1 text-xs font-semibold rounded-full ${
                        s.status === 'ACTIVE' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
                    }">${s.status === 'ACTIVE' ? 'Hoạt động' : 'Đã khóa'}</span>
                </td>
                <td class="px-4 py-3 text-right">
                    <div class="flex items-center justify-end space-x-1 whitespace-nowrap">
                        <a href="/teacher/student-detail?id=${s.id}" class="text-indigo-600 hover:text-indigo-900" title="Chi tiết">
                            <svg class="w-5 h-5 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 12a3 3 0 11-6 0 3 3 0 016 0z"></path><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M2.458 12C3.732 7.943 7.523 5 12 5c4.478 0 8.268 2.943 9.542 7-1.274 4.057-5.064 7-9.542 7-4.477 0-8.268-2.943-9.542-7z"></path></svg>
                        </a>
                        <button onclick="StudentManager.openEditModal(${s.id})" class="text-blue-600 hover:text-blue-900" title="Sửa">
                            <svg class="w-5 h-5 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z"></path></svg>
                        </button>
                        <button onclick="StudentManager.openResetPasswordModal(${s.id})" class="text-yellow-600 hover:text-yellow-900" title="Đặt lại mật khẩu">
                            <svg class="w-5 h-5 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 7a2 2 0 012 2m4 0a6 6 0 01-7.743 5.743L11 17H9v2H7v2H4a1 1 0 01-1-1v-2.586a1 1 0 01.293-.707l5.964-5.964A6 6 0 1121 9z"></path></svg>
                        </button>
                    </div>
                </td>
                <td class="px-4 py-3 text-right">
                    <div class="flex items-center justify-end space-x-1 whitespace-nowrap">
                        ${lockBtn}
                        <button onclick="StudentManager.openGroupModal(${s.id}, '${(s.fullName||'').replace(/'/g,"\\'")}', '${s.email||''}', '${s.phone||''}')" class="text-purple-600 hover:text-purple-900" title="Quản lý nhóm">
                            <svg class="w-5 h-5 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M17 20h5v-2a3 3 0 00-5.356-1.857M17 20H7m10 0v-2c0-.656-.126-1.283-.356-1.857M7 20H2v-2a3 3 0 015.356-1.857M7 20v-2c0-.656.126-1.283.356-1.857m0 0a5.002 5.002 0 019.288 0M15 7a3 3 0 11-6 0 3 3 0 016 0zm6 3a2 2 0 11-4 0 2 2 0 014 0zM7 10a2 2 0 11-4 0 2 2 0 014 0z"></path></svg>
                        </button>
                    </div>
                </td>
            </tr>
            `;
        }).join('');
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
        document.getElementById('modalTitle').textContent = 'Tạo Mới Tài Khoản Học Sinh';
        
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
            phone: form.phone.value,
            role: 'STUDENT'
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
            let msg = e.message || 'Có lỗi xảy ra';
            if (e.errors && typeof e.errors === 'object') {
                const errDetails = Object.values(e.errors).join('\n');
                msg += '\n\nChi tiết:\n' + errDetails;
            }
            alert(msg);
        }
    },
    
    async restoreStudent(id) {
        if (!confirm('Bạn chắc chắn muốn mở khóa học sinh này?')) return;
        try {
            await window.apiFetch('/api/teacher/students/' + id + '/restore', { method: 'POST' });
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
    },

    // ========== Modal Quản lý Nhóm ==========
    currentGroupModalStudentId: null,

    async openGroupModal(studentId, fullName, email, phone) {
        this.currentGroupModalStudentId = studentId;
        document.getElementById('foundName').textContent = fullName;
        document.getElementById('foundContact').textContent = (email || '') + (email && phone ? ' - ' : '') + (phone || '');
        document.getElementById('foundStudentId').value = studentId;

        // Populate course dropdown
        const courseSelect = document.getElementById('enrollCourseId');
        courseSelect.innerHTML = '<option value="">-- Chọn Khóa học --</option>' +
            this.allCourses.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
        document.getElementById('enrollGroupId').innerHTML = '<option value="">-- Chọn nhóm --</option>';

        // Load current enrollments
        await this.loadStudentEnrollments(studentId);

        document.getElementById('searchStudentModal').classList.remove('hidden');
    },

    async loadStudentEnrollments(studentId) {
        const tbody = document.getElementById('enrolledGroupsTable');
        try {
            const enrollments = await window.apiFetch('/api/teacher/students/' + studentId + '/enrollments');
            if (!enrollments || enrollments.length === 0) {
                tbody.innerHTML = '<tr><td colspan="3" class="px-4 py-3 text-center text-gray-400 text-sm">Chưa tham gia nhóm nào</td></tr>';
            } else {
                tbody.innerHTML = enrollments.map(e => `
                    <tr class="border-b hover:bg-gray-50">
                        <td class="px-4 py-2 text-sm text-gray-700">${e.courseName}</td>
                        <td class="px-4 py-2 text-sm text-gray-700">${e.groupName}</td>
                        <td class="px-4 py-2 text-right">
                            <button onclick="StudentManager.unenrollStudent(${studentId}, ${e.groupId})" class="text-red-600 hover:text-red-900" title="Xóa khỏi nhóm">
                                <svg class="w-5 h-5 inline" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"></path></svg>
                            </button>
                        </td>
                    </tr>
                `).join('');
            }
            // Store enrolled group ids for filtering
            this._enrolledGroupIds = (enrollments || []).map(e => e.groupId);
        } catch (e) {
            tbody.innerHTML = '<tr><td colspan="3" class="px-4 py-3 text-center text-red-500 text-sm">Lỗi tải dữ liệu</td></tr>';
            this._enrolledGroupIds = [];
        }
    },

    onCourseChange() {
        const courseId = parseInt(document.getElementById('enrollCourseId').value);
        const groupSelect = document.getElementById('enrollGroupId');
        if (!courseId) {
            groupSelect.innerHTML = '<option value="">-- Chọn nhóm --</option>';
            return;
        }
        const enrolledIds = this._enrolledGroupIds || [];
        const available = this.allGroups.filter(g => g.courseId === courseId && !enrolledIds.includes(g.id));
        groupSelect.innerHTML = '<option value="">-- Chọn nhóm --</option>' +
            available.map(g => `<option value="${g.id}">${g.name}</option>`).join('');
    },

    closeSearchModal() {
        document.getElementById('searchStudentModal').classList.add('hidden');
    },

    async enrollStudent() {
        const studentId = this.currentGroupModalStudentId;
        const groupId = document.getElementById('enrollGroupId').value;
        if (!groupId) {
            alert('Vui lòng chọn nhóm để thêm.');
            return;
        }

        try {
            await window.apiFetch('/api/teacher/students/enroll/' + studentId + '/group/' + groupId, {
                method: 'POST'
            });
            await this.loadStudentEnrollments(studentId);
            // Reset dropdown
            this.onCourseChange();
            window.ListHelper.load();
        } catch (e) {
            alert('Lỗi: ' + (e.message || e));
        }
    },

    async unenrollStudent(studentId, groupId) {
        if (!confirm('Bạn chắc chắn muốn xóa học sinh khỏi nhóm này?')) return;
        try {
            await window.apiFetch('/api/teacher/students/enroll/' + studentId + '/group/' + groupId, {
                method: 'DELETE'
            });
            await this.loadStudentEnrollments(studentId);
            this.onCourseChange();
            window.ListHelper.load();
        } catch (e) {
            alert('Lỗi: ' + (e.message || e));
        }
    },

    // ========== Tìm & Thêm Học sinh vào nhóm (hiếm dùng) ==========
    openSearchModal(autoQuery = '') {
        document.getElementById('searchStudentModal').classList.remove('hidden');
        document.getElementById('foundName').textContent = '';
        document.getElementById('foundContact').textContent = '';
        document.getElementById('foundStudentId').value = '';
        document.getElementById('enrolledGroupsTable').innerHTML = '';
        document.getElementById('enrollCourseId').innerHTML = '<option value="">-- Chọn Khóa học --</option>' +
            this.allCourses.map(c => `<option value="${c.id}">${c.name}</option>`).join('');
        document.getElementById('enrollGroupId').innerHTML = '<option value="">-- Chọn nhóm --</option>';
    },

    async searchStudent() {
        const query = document.getElementById('searchQuery').value.trim();
        if (!query) {
            alert('Vui lòng nhập email hoặc SĐT');
            return;
        }

        try {
            const student = await window.apiFetch('/api/teacher/students/find?query=' + encodeURIComponent(query));
            this.openGroupModal(student.id, student.fullName || student.username, student.email, student.phone);
        } catch (e) {
            alert(e.message || 'Không tìm thấy học sinh');
        }
    }
};

window.StudentManager = StudentManager;
