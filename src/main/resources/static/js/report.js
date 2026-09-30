/**
 * report.js - Báo cáo doanh thu & công nợ
 */
const ReportManager = {
    apiBase: '',

    init(role) {
        this.apiBase = role === 'ADMIN' ? '/api/admin/invoices/reports' : '/api/teacher/invoices/reports';
    },

    async loadRevenue(from, to) {
        try {
            const f = from || new Date(new Date().getFullYear(), new Date().getMonth(), 1).toISOString().split('T')[0] + 'T00:00:00';
            const t = to || new Date().toISOString().split('T')[0] + 'T23:59:59';
            
            const data = await apiFetch(this.apiBase + `/revenue?from=${f}&to=${t}`);
            document.getElementById('total-revenue').textContent = Number(data.totalRevenue).toLocaleString('vi-VN') + 'đ';
            
            const tbody = document.getElementById('revenue-tbody');
            if (tbody && data.byTeacher) {
                tbody.innerHTML = data.byTeacher.map(tr => `
                    <tr class="border-b">
                        <td class="px-4 py-2">${tr.teacherName}</td>
                        <td class="px-4 py-2 font-bold text-green-600">${Number(tr.revenue).toLocaleString('vi-VN')}đ</td>
                    </tr>
                `).join('');
            }
        } catch (e) {
            document.getElementById('error-msg').textContent = e.message;
        }
    },

    async loadDebt() {
        try {
            const data = await apiFetch(this.apiBase + '/debt');
            const tbody = document.getElementById('debt-tbody');
            if (!tbody) return;
            tbody.innerHTML = data.map(d => `
                <tr class="border-b">
                    <td class="px-4 py-2">${d.studentId}</td>
                    <td class="px-4 py-2 font-medium">${d.studentName}</td>
                    <td class="px-4 py-2">${d.invoiceCount}</td>
                    <td class="px-4 py-2 text-red-600 font-bold">${Number(d.totalDebt).toLocaleString('vi-VN')}đ</td>
                    <td class="px-4 py-2 text-sm text-gray-500">${d.oldestUnpaidMonth}</td>
                </tr>
            `).join('');
        } catch (e) {
            document.getElementById('error-msg').textContent = e.message;
        }
    }
};
window.ReportManager = ReportManager;
