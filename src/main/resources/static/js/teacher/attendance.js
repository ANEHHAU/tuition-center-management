const AttendanceManager = {
    currentSessionId: null,

    init() {
        this.loadTodaySessions();
        
        // Tự chọn session từ URL parameter nếu có
        const params = new URLSearchParams(window.location.search);
        if (params.has('sessionId')) {
            const sid = params.get('sessionId');
            // Load attendance ngay, đồng thời thêm session vào dropdown nếu chưa có
            this.loadSessionIntoDropdown(sid);
            this.loadAttendance(sid);
        }
    },

    async loadSessionIntoDropdown(sessionId) {
        try {
            const s = await window.apiFetch('/api/teacher/sessions/' + sessionId);
            const select = document.getElementById('sessionSelect');
            if (select) {
                // Kiểm tra nếu option chưa có thì thêm
                let exists = false;
                for (let opt of select.options) {
                    if (opt.value == sessionId) { exists = true; break; }
                }
                if (!exists) {
                    const opt = document.createElement('option');
                    opt.value = s.id;
                    opt.textContent = `${s.date} ${s.startTime} - ${s.groupName} [${s.status}]`;
                    select.appendChild(opt);
                }
                select.value = sessionId;
            }
        } catch (e) {
            console.error('Không tải được thông tin buổi học', e);
        }
    },

    async loadTodaySessions() {
        try {
            const res = await window.apiFetch('/api/teacher/dashboard/today-sessions');
            const select = document.getElementById('sessionSelect');
            if (res && select) {
                res.forEach(s => {
                    const opt = document.createElement('option');
                    opt.value = s.id;
                    opt.textContent = `${s.startTime} - ${s.groupName} (${s.room || 'Không ghi rõ phòng'})`;
                    select.appendChild(opt);
                });
            }
        } catch (e) {
            console.error(e);
        }
    },

    async loadAttendance(sessionId) {
        if (!sessionId) {
            document.getElementById('attendanceList').innerHTML = '';
            document.getElementById('btnSave').classList.add('hidden');
            return;
        }
        
        this.currentSessionId = sessionId;
        try {
            const res = await window.apiFetch('/api/teacher/attendance/session/' + sessionId);
            const container = document.getElementById('attendanceList');
            document.getElementById('btnSave').classList.remove('hidden');
            document.getElementById('btnExport').href = '/api/teacher/attendance/export?sessionId=' + sessionId;
            
            if (!res.items || res.items.length === 0) {
                container.innerHTML = `<div class="p-6 text-center text-gray-500">Chưa có học sinh nào trong nhóm này.</div>`;
                return;
            }

            container.innerHTML = res.items.map(item => `
                <div class="flex flex-col md:flex-row md:items-center justify-between p-4 border-b hover:bg-gray-50 transition student-row" data-id="${item.studentId}">
                    <div class="mb-3 md:mb-0">
                        <div class="font-medium text-gray-800">${item.studentName}</div>
                    </div>
                    <div class="flex flex-col md:flex-row items-start md:items-center space-y-3 md:space-y-0 md:space-x-6">
                        <div class="flex space-x-4">
                            <label class="flex items-center space-x-1 cursor-pointer">
                                <input type="radio" name="status_${item.studentId}" value="PRESENT" class="text-green-600 focus:ring-green-500" ${item.status === 'PRESENT' ? 'checked' : ''} ${!item.status ? 'checked' : ''}>
                                <span class="text-sm text-green-700">Có mặt</span>
                            </label>
                            <label class="flex items-center space-x-1 cursor-pointer">
                                <input type="radio" name="status_${item.studentId}" value="ABSENT" class="text-red-600 focus:ring-red-500" ${item.status === 'ABSENT' ? 'checked' : ''}>
                                <span class="text-sm text-red-700">Vắng</span>
                            </label>
                            <label class="flex items-center space-x-1 cursor-pointer">
                                <input type="radio" name="status_${item.studentId}" value="EXCUSED" class="text-yellow-600 focus:ring-yellow-500" ${item.status === 'EXCUSED' ? 'checked' : ''}>
                                <span class="text-sm text-yellow-700">Có phép</span>
                            </label>
                        </div>
                        <input type="text" class="note-input border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500 text-sm w-full md:w-48" placeholder="Ghi chú..." value="${item.note || ''}">
                    </div>
                </div>
            `).join('');
        } catch (e) {
            alert(e.message);
        }
    },

    async saveAttendance() {
        if (!this.currentSessionId) return;
        
        const items = [];
        document.querySelectorAll('.student-row').forEach(row => {
            const studentId = row.getAttribute('data-id');
            const status = row.querySelector(`input[name="status_${studentId}"]:checked`).value;
            const note = row.querySelector('.note-input').value;
            
            items.push({
                studentId: parseInt(studentId),
                status: status,
                note: note
            });
        });
        
        try {
            await window.apiFetch('/api/teacher/attendance/session/' + this.currentSessionId, {
                method: 'POST',
                body: JSON.stringify({ items })
            });
            alert('Lưu điểm danh thành công!');
        } catch (e) {
            alert(e.message);
        }
    }
};

window.AttendanceManager = AttendanceManager;
