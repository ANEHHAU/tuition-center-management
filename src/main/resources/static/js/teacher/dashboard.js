const DashboardManager = {
    init() {
        this.loadStats();
        this.loadTodaySessions();
        this.loadRecentPayments();
    },

    async loadStats() {
        try {
            const res = await window.apiFetch('/api/teacher/dashboard/stats');
            document.getElementById('statCourses').textContent = res.totalCourses;
            document.getElementById('statGroups').textContent = res.totalGroups;
            document.getElementById('statStudents').textContent = res.totalStudents;
            document.getElementById('statRevenue').textContent = Number(res.monthRevenue).toLocaleString('vi-VN') + ' đ';
            
            // Format welcome
            const user = JSON.parse(localStorage.getItem('user'));
            if (user) {
                document.getElementById('welcomeName').textContent = user.fullName || user.username;
            }
        } catch (e) {
            console.error(e);
        }
    },

    async loadTodaySessions() {
        try {
            const res = await window.apiFetch('/api/teacher/dashboard/today-sessions');
            const container = document.getElementById('todaySessions');
            if (res.length === 0) {
                container.innerHTML = '<div class="text-center text-gray-500 py-4">Không có buổi học nào hôm nay.</div>';
                return;
            }
            
            container.innerHTML = res.map(s => `
                <div class="flex items-center justify-between p-3 border rounded-lg hover:border-blue-300 transition">
                    <div class="flex items-center space-x-3">
                        <div class="w-10 h-10 rounded-full bg-blue-100 flex items-center justify-center text-blue-600">
                            <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                        </div>
                        <div>
                            <div class="font-medium text-gray-800">${s.startTime} - ${s.endTime}</div>
                            <div class="text-sm text-gray-500">${s.groupName} (${s.room || 'Phòng học'})</div>
                        </div>
                    </div>
                    <a href="/teacher/attendance?sessionId=${s.id}" class="px-3 py-1 text-sm bg-blue-50 text-blue-600 rounded hover:bg-blue-100 transition">Điểm danh</a>
                </div>
            `).join('');
        } catch (e) {
            console.error(e);
        }
    },

    async loadRecentPayments() {
        try {
            const res = await window.apiFetch('/api/teacher/dashboard/recent-payments');
            const container = document.getElementById('recentPayments');
            if (res.length === 0) {
                container.innerHTML = '<div class="text-center text-gray-500 py-4">Chưa có giao dịch nào gần đây.</div>';
                return;
            }
            
            container.innerHTML = res.map(p => `
                <div class="flex items-center justify-between p-3 border-b last:border-0">
                    <div>
                        <div class="font-medium text-gray-800">${p.studentName}</div>
                        <div class="text-xs text-gray-500">${new Date(p.paidAt).toLocaleString('vi-VN')} - Tháng ${p.month}/${p.year}</div>
                    </div>
                    <div class="text-right">
                        <div class="font-bold text-green-600">+${Number(p.amount).toLocaleString('vi-VN')} đ</div>
                        <div class="text-xs text-gray-500">${p.method}</div>
                    </div>
                </div>
            `).join('');
        } catch (e) {
            console.error(e);
        }
    }
};

window.DashboardManager = DashboardManager;
