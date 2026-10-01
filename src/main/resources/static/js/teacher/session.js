const SessionManager = {
    init() {
        window.ListHelper.init(
            '/api/teacher/sessions',
            (res) => this.renderTable(res),
            'date',
            'desc'
        );
        this.loadGroups();
    },

    async loadGroups() {
        try {
            const res = await window.apiFetch('/api/teacher/groups?size=100');
            const selectGroup = document.getElementById('filterGroup');
            const bulkGroup = document.getElementById('bulkGroupId');
            
            if (res.content) {
                res.content.forEach(g => {
                    const opt1 = document.createElement('option');
                    opt1.value = g.id;
                    opt1.textContent = g.name + ' (' + g.courseName + ')';
                    if(selectGroup) selectGroup.appendChild(opt1);
                    
                    const opt2 = document.createElement('option');
                    opt2.value = g.id;
                    opt2.textContent = g.name;
                    if(bulkGroup) bulkGroup.appendChild(opt2);
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
            tbody.innerHTML = `<tr><td colspan="8" class="text-center py-4 text-gray-500">Không tìm thấy buổi học nào.</td></tr>`;
            return;
        }

        tbody.innerHTML = res.content.map(s => {
            let statusClass = 'bg-gray-100 text-gray-800';
            if (s.status === 'COMPLETED') statusClass = 'bg-green-100 text-green-800';
            else if (s.status === 'SCHEDULED') statusClass = 'bg-blue-100 text-blue-800';
            else if (s.status === 'CANCELLED') statusClass = 'bg-red-100 text-red-800';

            return `
            <tr class="border-b hover:bg-gray-50 transition-colors">
                <td class="px-4 py-3 font-medium text-gray-800">${s.groupName || '-'}</td>
                <td class="px-4 py-3 text-sm text-gray-700">${s.courseName || '-'}</td>
                <td class="px-4 py-3">${s.date}</td>
                <td class="px-4 py-3">${s.startTime} - ${s.endTime}</td>
                <td class="px-4 py-3">${s.room || '-'}</td>
                <td class="px-4 py-3">
                    <span class="px-2 py-1 text-xs font-semibold rounded-full ${statusClass}">${s.status}</span>
                </td>
                <td class="px-4 py-3 text-center">${s.attendanceCount || 0}</td>
                <td class="px-4 py-3 space-x-2 text-right border-l border-gray-100">
                    ${s.status === 'SCHEDULED' ? `<a href="/teacher/attendance?sessionId=${s.id}" class="text-indigo-600 hover:text-indigo-800 font-medium text-sm">Điểm danh</a>` : ''}
                    ${s.status === 'COMPLETED' ? `<a href="/teacher/attendance?sessionId=${s.id}" class="text-orange-600 hover:text-orange-800 font-medium text-sm">Sửa điểm danh</a>` : ''}
                    ${s.status === 'SCHEDULED' ? `<button onclick="SessionManager.cancelSession(${s.id})" class="text-yellow-600 hover:text-yellow-800 font-medium text-sm">Hủy</button>` : ''}
                    <button onclick="SessionManager.deleteSession(${s.id})" class="text-red-600 hover:text-red-800 font-medium text-sm">Xóa</button>
                </td>
            </tr>
            `;
        }).join('');
    },

    async cancelSession(id) {
        if (!confirm('Bạn chắc chắn muốn hủy buổi học này?')) return;
        try {
            await window.apiFetch('/api/teacher/sessions/' + id + '/cancel', { method: 'POST' });
            window.ListHelper.load();
        } catch (e) {
            alert(e.message);
        }
    },

    async deleteSession(id) {
        if (!confirm('Bạn chắc chắn muốn xóa buổi học này?')) return;
        try {
            await window.apiFetch('/api/teacher/sessions/' + id, { method: 'DELETE' });
            window.ListHelper.load();
        } catch (e) {
            alert(e.message);
        }
    },
    
    openBulkModal() {
        document.getElementById('bulkForm').reset();
        document.getElementById('bulkModal').classList.remove('hidden');
    },
    
    closeBulkModal() {
        document.getElementById('bulkModal').classList.add('hidden');
    },
    
    async submitBulkCreate(e) {
        e.preventDefault();
        const form = e.target;
        
        const daysOfWeek = [];
        form.querySelectorAll('input[name="daysOfWeek"]:checked').forEach(cb => {
            daysOfWeek.push(parseInt(cb.value));
        });
        
        if (daysOfWeek.length === 0) {
            alert('Vui lòng chọn ít nhất 1 ngày trong tuần');
            return;
        }
        
        const body = {
            groupId: parseInt(form.groupId.value),
            startDate: form.startDate.value,
            endDate: form.endDate.value,
            daysOfWeek: daysOfWeek,
            startTime: form.startTime.value,
            endTime: form.endTime.value,
            room: form.room.value
        };
        
        try {
            await window.apiFetch('/api/teacher/sessions/bulk-create', {
                method: 'POST',
                body: JSON.stringify(body)
            });
            alert('Tạo lịch học hàng loạt thành công!');
            this.closeBulkModal();
            window.ListHelper.load();
        } catch (error) {
            alert(error.message);
        }
    }
};

window.SessionManager = SessionManager;
