// Attendance Helper
function formatAttendanceStatus(status) {
    const map = {
        PRESENT: '<span class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-green-100 text-green-700"><i class="fas fa-check mr-1"></i>Có mặt</span>',
        ABSENT: '<span class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-red-100 text-red-700"><i class="fas fa-times mr-1"></i>Vắng</span>',
        EXCUSED: '<span class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-yellow-100 text-yellow-700"><i class="fas fa-exclamation mr-1"></i>Có phép</span>',
        CANCELLED: '<span class="inline-flex items-center px-2 py-1 rounded-full text-xs font-medium bg-gray-100 text-gray-500"><i class="fas fa-minus mr-1"></i>Hủy</span>',
    };
    return map[status] || `<span>${status}</span>`;
}

const attendanceList = new ListHelper('/api/student/attendance', {
    defaultSort: 'sessionDate',
    defaultSortDir: 'desc',
    renderItem: (item) => {
        const dateStr = new Date(item.sessionDate).toLocaleDateString('vi-VN', { weekday: 'short', year: 'numeric', month: '2-digit', day: '2-digit' });
        return `
        <tr class="hover:bg-gray-50 transition-colors">
            <td class="px-4 py-3 text-gray-700 font-medium">${dateStr}</td>
            <td class="px-4 py-3 text-gray-600">${item.courseName}</td>
            <td class="px-4 py-3 text-gray-500 text-xs">${item.teacherName}</td>
            <td class="px-4 py-3">${formatAttendanceStatus(item.status)}</td>
            <td class="px-4 py-3 text-gray-500 text-xs">${item.note || '—'}</td>
        </tr>`;
    },
    renderEmpty: () => `<tr><td colspan="5" class="px-4 py-10 text-center text-gray-500"><i class="fas fa-box-open text-3xl mb-2 text-gray-300 block"></i>Chưa có dữ liệu điểm danh</td></tr>`,
    tbodyId: 'attendanceTableBody'
});

// Set default date range (this month)
document.addEventListener('DOMContentLoaded', () => {
    const now = new Date();
    const from = new Date(now.getFullYear(), now.getMonth() - 2, 1).toISOString().split('T')[0];
    const to = new Date(now.getFullYear(), now.getMonth() + 1, 0).toISOString().split('T')[0];
    
    document.getElementById('filterFrom').value = from;
    document.getElementById('filterTo').value = to;
    
    attendanceList.init({ from, to });
    loadStats(from, to);
    
    document.getElementById('filterFrom').addEventListener('change', function() {
        loadStats(this.value, document.getElementById('filterTo').value);
    });
    document.getElementById('filterTo').addEventListener('change', function() {
        loadStats(document.getElementById('filterFrom').value, this.value);
    });
});

async function loadStats(from, to) {
    try {
        const stats = await ApiClient.get(`/api/student/attendance/stats?from=${from}&to=${to}`);
        document.getElementById('statTotal').textContent = stats.total;
        document.getElementById('statPresent').textContent = stats.present;
        document.getElementById('statAbsent').textContent = stats.absent;
        document.getElementById('statRate').textContent = stats.rate + '%';
    } catch (e) {
        console.error('Failed to load stats', e);
    }
}

async function exportAttendance() {
    const from = document.getElementById('filterFrom').value;
    const to = document.getElementById('filterTo').value;
    
    try {
        const items = await ApiClient.get(`/api/student/attendance?from=${from}&to=${to}&size=1000&page=0`);
        const content = items.content || [];
        
        const csvRows = [
            ['Ngày', 'Khóa học', 'Giáo viên', 'Trạng thái', 'Ghi chú'],
            ...content.map(r => [r.sessionDate, r.courseName, r.teacherName, r.status, r.note || ''])
        ];
        const csvContent = csvRows.map(r => r.map(v => `"${v}"`).join(',')).join('\n');
        const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' });
        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.download = `diem_danh_${from}_${to}.csv`;
        link.click();
    } catch (e) {
        alert('Lỗi xuất file: ' + e.message);
    }
}
