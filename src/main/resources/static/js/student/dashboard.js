document.addEventListener('DOMContentLoaded', () => {
    loadStats();
    loadTodaySessions();
    loadUpcomingSessions();
});

async function loadStats() {
    try {
        const res = await ApiClient.get('/api/student/dashboard/stats');
        
        document.getElementById('totalCourses').textContent = res.totalCourses;
        document.getElementById('totalGroups').textContent = res.totalGroups;
        document.getElementById('totalSessions').textContent = res.totalSessionsAttended;
        document.getElementById('unreadInvoices').textContent = res.unreadInvoices;

        if (res.currentDebt > 0) {
            document.getElementById('debtAlert').classList.remove('hidden');
            document.getElementById('currentDebtAmount').textContent = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(res.currentDebt);
        }
    } catch (error) {
        console.error('Error loading stats', error);
    }
}

async function loadTodaySessions() {
    try {
        const sessions = await ApiClient.get('/api/student/dashboard/today-sessions');
        const container = document.getElementById('todaySessions');
        
        if (sessions.length === 0) {
            container.innerHTML = `<div class="text-center p-6 text-gray-500 bg-gray-50 rounded-lg border border-dashed border-gray-200">
                <i class="fas fa-bed text-3xl mb-2 text-gray-400"></i>
                <p>Hôm nay bạn không có lịch học nào.</p>
            </div>`;
            return;
        }

        container.innerHTML = sessions.map(s => `
            <div class="flex items-start p-4 border border-gray-100 rounded-lg hover:bg-blue-50 transition-colors">
                <div class="flex-shrink-0 w-16 h-16 bg-blue-100 text-blue-600 rounded-lg flex flex-col items-center justify-center font-bold">
                    <span class="text-lg">${s.startTime.substring(0, 5)}</span>
                </div>
                <div class="ml-4 flex-1">
                    <h4 class="font-bold text-gray-800">${s.groupName}</h4>
                    <p class="text-sm text-gray-600"><i class="fas fa-book mr-1"></i> ${s.courseName}</p>
                    <div class="mt-2 flex items-center text-xs text-gray-500 space-x-4">
                        <span><i class="fas fa-map-marker-alt mr-1"></i> Phòng: ${s.room || 'Chưa xếp'}</span>
                        <span><i class="fas fa-chalkboard-teacher mr-1"></i> GV: ${s.teacherName}</span>
                    </div>
                </div>
            </div>
        `).join('');
    } catch (error) {
        document.getElementById('todaySessions').innerHTML = '<p class="text-red-500 text-sm">Lỗi tải dữ liệu</p>';
    }
}

async function loadUpcomingSessions() {
    try {
        const sessions = await ApiClient.get('/api/student/dashboard/upcoming-sessions?days=7');
        const container = document.getElementById('upcomingSessions');
        
        if (sessions.length === 0) {
            container.innerHTML = `<div class="text-center p-6 text-gray-500 bg-gray-50 rounded-lg border border-dashed border-gray-200">
                <p>Không có lịch học nào trong 7 ngày tới.</p>
            </div>`;
            return;
        }

        container.innerHTML = sessions.map(s => {
            const dateObj = new Date(s.date);
            const dateStr = dateObj.toLocaleDateString('vi-VN', { weekday: 'short', day: '2-digit', month: '2-digit' });
            return `
            <div class="flex items-center justify-between p-3 border-b border-gray-100 last:border-0 hover:bg-gray-50">
                <div class="flex items-center">
                    <div class="w-12 h-12 bg-indigo-50 text-indigo-600 rounded flex flex-col items-center justify-center mr-3">
                        <span class="text-xs font-medium uppercase">${dateStr.split(',')[0]}</span>
                        <span class="font-bold">${dateStr.split(' ')[1] || dateStr.split(', ')[1]}</span>
                    </div>
                    <div>
                        <p class="font-bold text-gray-800 text-sm">${s.groupName}</p>
                        <p class="text-xs text-gray-500">${s.startTime.substring(0, 5)} - ${s.endTime.substring(0, 5)} | P.${s.room || '??'}</p>
                    </div>
                </div>
                <a href="/student/session-detail?id=${s.id}" class="text-indigo-500 hover:text-indigo-700 p-2 rounded-full hover:bg-indigo-50 transition-colors">
                    <i class="fas fa-chevron-right"></i>
                </a>
            </div>
        `}).join('');
    } catch (error) {
        document.getElementById('upcomingSessions').innerHTML = '<p class="text-red-500 text-sm">Lỗi tải dữ liệu</p>';
    }
}
