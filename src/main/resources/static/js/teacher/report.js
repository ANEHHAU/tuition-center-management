const ReportManager = {
    init() {
        this.setDefaultDates();
        this.loadAll();
    },

    setDefaultDates() {
        const now = new Date();
        const firstDay = new Date(now.getFullYear(), now.getMonth(), 1);
        
        document.getElementById('fromDate').value = firstDay.toISOString().split('T')[0];
        document.getElementById('toDate').value = now.toISOString().split('T')[0];
    },

    async loadAll() {
        this.loadRevenue();
        this.loadRevenueByCourse();
        this.loadAttendanceRate();
        this.loadTopStudents();
    },

    async loadRevenue() {
        const from = document.getElementById('fromDate').value;
        const to = document.getElementById('toDate').value;
        try {
            const res = await window.apiFetch(`/api/teacher/reports/revenue?from=${from}T00:00:00&to=${to}T23:59:59`);
            document.getElementById('totalRevenue').textContent = Number(res.totalRevenue).toLocaleString('vi-VN') + ' đ';
        } catch (e) {
            console.error(e);
        }
    },

    async loadRevenueByCourse() {
        const from = document.getElementById('fromDate').value;
        const to = document.getElementById('toDate').value;
        try {
            const res = await window.apiFetch(`/api/teacher/reports/revenue-by-course?from=${from}T00:00:00&to=${to}T23:59:59`);
            const container = document.getElementById('courseRevenueList');
            if (res.length === 0) {
                container.innerHTML = '<div class="text-gray-500 py-2">Không có dữ liệu</div>';
                return;
            }
            container.innerHTML = res.map(item => `
                <div class="flex justify-between items-center py-2 border-b last:border-0">
                    <span class="text-gray-700">${item.courseName}</span>
                    <span class="font-semibold text-gray-900">${Number(item.revenue).toLocaleString('vi-VN')} đ</span>
                </div>
            `).join('');
        } catch (e) {
            console.error(e);
        }
    },

    async loadAttendanceRate() {
        const from = document.getElementById('fromDate').value;
        const to = document.getElementById('toDate').value;
        try {
            const res = await window.apiFetch(`/api/teacher/reports/attendance-rate?from=${from}&to=${to}`);
            const tbody = document.getElementById('attendanceRateBody');
            if (res.length === 0) {
                tbody.innerHTML = '<tr><td colspan="6" class="text-center py-4 text-gray-500">Không có dữ liệu</td></tr>';
                return;
            }
            tbody.innerHTML = res.map(item => `
                <tr class="border-b">
                    <td class="px-4 py-2 font-medium">${item.studentName}</td>
                    <td class="px-4 py-2 text-center text-green-600">${item.present}</td>
                    <td class="px-4 py-2 text-center text-red-600">${item.absent}</td>
                    <td class="px-4 py-2 text-center text-yellow-600">${item.excused}</td>
                    <td class="px-4 py-2 text-center font-bold">${item.total}</td>
                    <td class="px-4 py-2 text-right">
                        <div class="w-full bg-gray-200 rounded-full h-2.5">
                          <div class="bg-blue-600 h-2.5 rounded-full" style="width: ${item.rate}%"></div>
                        </div>
                        <span class="text-xs text-gray-500 mt-1">${item.rate}%</span>
                    </td>
                </tr>
            `).join('');
        } catch (e) {
            console.error(e);
        }
    },

    async loadTopStudents() {
        try {
            const res = await window.apiFetch('/api/teacher/reports/top-students?limit=5');
            const container = document.getElementById('topStudentsList');
            if (res.length === 0) {
                container.innerHTML = '<div class="text-gray-500 py-2">Không có dữ liệu</div>';
                return;
            }
            container.innerHTML = res.map((item, index) => `
                <div class="flex justify-between items-center py-3 border-b last:border-0">
                    <div class="flex items-center space-x-3">
                        <div class="w-8 h-8 rounded-full ${index < 3 ? 'bg-yellow-100 text-yellow-600' : 'bg-gray-100 text-gray-600'} flex items-center justify-center font-bold">
                            ${index + 1}
                        </div>
                        <span class="text-gray-700 font-medium">${item.studentName}</span>
                    </div>
                    <span class="text-green-600 font-semibold">${item.rate}%</span>
                </div>
            `).join('');
        } catch (e) {
            console.error(e);
        }
    }
};

window.ReportManager = ReportManager;
