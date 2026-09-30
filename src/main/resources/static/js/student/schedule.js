function formatStatus(status) {
    const map = {
        SCHEDULED: '<span class="px-2 py-1 text-xs rounded-full bg-blue-100 text-blue-700">Sắp học</span>',
        COMPLETED: '<span class="px-2 py-1 text-xs rounded-full bg-green-100 text-green-700">Đã học</span>',
        CANCELLED: '<span class="px-2 py-1 text-xs rounded-full bg-gray-100 text-gray-500">Đã hủy</span>',
    };
    return map[status] || status;
}

// ===== Schedule List Page =====
if (document.getElementById('scheduleTableBody')) {
    const scheduleList = new ListHelper('/api/student/schedule', {
        defaultSort: 'date',
        defaultSortDir: 'asc',
        renderItem: (s) => {
            const dateStr = new Date(s.date).toLocaleDateString('vi-VN', { weekday: 'short', day: '2-digit', month: '2-digit', year: 'numeric' });
            return `
            <tr class="hover:bg-gray-50 transition-colors">
                <td class="px-4 py-3 font-medium text-gray-800">${dateStr}</td>
                <td class="px-4 py-3 text-gray-600">${s.startTime ? s.startTime.substring(0, 5) : ''} - ${s.endTime ? s.endTime.substring(0, 5) : ''}</td>
                <td class="px-4 py-3 text-gray-700">${s.groupName || '—'}</td>
                <td class="px-4 py-3 text-gray-600">${s.courseName || '—'}</td>
                <td class="px-4 py-3 text-gray-500">Phòng ${s.room || '—'}</td>
                <td class="px-4 py-3 text-right">
                    <a href="/student/session-detail?id=${s.id}" class="text-blue-600 hover:underline text-sm">
                        <i class="fas fa-eye mr-1"></i> Chi tiết
                    </a>
                </td>
            </tr>`;
        },
        renderEmpty: () => `<tr><td colspan="6" class="px-4 py-10 text-center text-gray-500"><i class="fas fa-calendar-times text-3xl mb-2 text-gray-300 block"></i>Không có lịch học nào</td></tr>`,
        tbodyId: 'scheduleTableBody'
    });

    document.addEventListener('DOMContentLoaded', async () => {
        // Load course filter
        try {
            const courses = await ApiClient.get('/api/student/courses?size=100&page=0');
            const sel = document.getElementById('filterCourse');
            (courses.content || []).forEach(c => {
                sel.innerHTML += `<option value="${c.id}">${c.name}</option>`;
            });
        } catch (e) { /* ignore */ }

        // Set default range: current month
        const now = new Date();
        const from = new Date(now.getFullYear(), now.getMonth(), 1).toISOString().split('T')[0];
        const to = new Date(now.getFullYear(), now.getMonth() + 2, 0).toISOString().split('T')[0];
        document.getElementById('filterFrom').value = from;
        document.getElementById('filterTo').value = to;

        // Apply course filter from URL param if present
        const params = new URLSearchParams(window.location.search);
        const courseId = params.get('courseId');
        if (courseId) {
            document.getElementById('filterCourse').value = courseId;
            scheduleList.init({ from, to, courseId });
        } else {
            scheduleList.init({ from, to });
        }
    });
}

// ===== Session Detail Page =====
if (document.getElementById('sessionInfo')) {
    document.addEventListener('DOMContentLoaded', async () => {
        const params = new URLSearchParams(window.location.search);
        const sessionId = params.get('id');
        if (!sessionId) return;

        try {
            const session = await ApiClient.get(`/api/student/schedule/session/${sessionId}`);

            document.getElementById('sessionInfo').innerHTML = `
                <div class="space-y-4">
                    <div class="flex justify-between py-3 border-b border-gray-100">
                        <span class="text-gray-500">Khóa học</span>
                        <span class="font-bold text-gray-800">${session.courseName || '—'}</span>
                    </div>
                    <div class="flex justify-between py-3 border-b border-gray-100">
                        <span class="text-gray-500">Nhóm</span>
                        <span class="font-bold text-gray-800">${session.groupName || '—'}</span>
                    </div>
                    <div class="flex justify-between py-3 border-b border-gray-100">
                        <span class="text-gray-500">Giáo viên</span>
                        <span class="font-bold text-gray-800">${session.teacherName || '—'}</span>
                    </div>
                    <div class="flex justify-between py-3 border-b border-gray-100">
                        <span class="text-gray-500">Ngày</span>
                        <span class="font-bold text-gray-800">${new Date(session.date).toLocaleDateString('vi-VN', {weekday:'long', day:'2-digit', month:'2-digit', year:'numeric'})}</span>
                    </div>
                    <div class="flex justify-between py-3 border-b border-gray-100">
                        <span class="text-gray-500">Thời gian</span>
                        <span class="font-bold text-gray-800">${session.startTime ? session.startTime.substring(0,5) : ''} - ${session.endTime ? session.endTime.substring(0,5) : ''}</span>
                    </div>
                    <div class="flex justify-between py-3">
                        <span class="text-gray-500">Phòng</span>
                        <span class="font-bold text-gray-800">${session.room || 'Chưa xếp phòng'}</span>
                    </div>
                </div>`;

            // Status
            const attStatus = session.myAttendanceStatus;
            const attNote = session.myAttendanceNote;
            const statusMap = {
                PRESENT: { icon: 'fa-check-circle', color: 'text-green-600', bg: 'bg-green-50 border-green-200', label: 'Có mặt' },
                ABSENT: { icon: 'fa-times-circle', color: 'text-red-600', bg: 'bg-red-50 border-red-200', label: 'Vắng mặt' },
                EXCUSED: { icon: 'fa-exclamation-circle', color: 'text-yellow-600', bg: 'bg-yellow-50 border-yellow-200', label: 'Vắng có phép' },
                CANCELLED: { icon: 'fa-minus-circle', color: 'text-gray-500', bg: 'bg-gray-50 border-gray-200', label: 'Buổi đã hủy' },
            };
            const s = attStatus && statusMap[attStatus];
            document.getElementById('attendanceStatus').innerHTML = attStatus ? `
                <div class="border rounded-xl p-6 text-center ${s.bg}">
                    <i class="fas ${s.icon} text-5xl mb-3 ${s.color}"></i>
                    <h3 class="text-xl font-bold ${s.color}">${s.label}</h3>
                    ${attNote ? `<p class="mt-3 text-gray-600 text-sm bg-white p-3 rounded-lg border"><strong>Ghi chú GV:</strong> ${attNote}</p>` : ''}
                </div>` : `
                <div class="border border-dashed border-gray-300 rounded-xl p-6 text-center text-gray-400">
                    <i class="fas fa-question-circle text-4xl mb-2 block"></i>
                    <p>Chưa có thông tin điểm danh</p>
                </div>`;
        } catch (e) {
            document.getElementById('sessionInfo').innerHTML = `<p class="text-red-500">Lỗi tải dữ liệu: ${e.message}</p>`;
        }
    });
}
