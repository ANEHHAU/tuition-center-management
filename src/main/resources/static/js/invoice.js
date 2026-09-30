/**
 * invoice.js - Quản lý hóa đơn và thanh toán
 */
const InvoiceManager = {
    apiBase: '',

    init(role) {
        if (role === 'ADMIN') this.apiBase = '/api/admin/invoices';
        else if (role === 'TEACHER') this.apiBase = '/api/teacher/invoices';
        else this.apiBase = '/api/student/invoices';
    },

    async loadInvoices(month, year) {
        try {
            const m = month || new Date().getMonth() + 1;
            const y = year || new Date().getFullYear();
            const url = this.apiBase.includes('student') ? this.apiBase : `${this.apiBase}?month=${m}&year=${y}`;
            const invoices = await apiFetch(url);
            this.renderTable(invoices);
        } catch (e) {
            document.getElementById('error-msg').textContent = e.message;
        }
    },

    renderTable(invoices) {
        const tbody = document.getElementById('invoices-tbody');
        if (!tbody) return;
        tbody.innerHTML = invoices.map(inv => `
            <tr class="border-b hover:bg-gray-50">
                <td class="px-4 py-3 text-sm">${inv.id}</td>
                <td class="px-4 py-3 text-sm font-medium">${inv.studentName || 'Bạn'}</td>
                <td class="px-4 py-3 text-sm">${inv.month}/${inv.year}</td>
                <td class="px-4 py-3 text-sm">${Number(inv.totalAmount).toLocaleString('vi-VN')}đ</td>
                <td class="px-4 py-3 text-sm">${Number(inv.paidAmount).toLocaleString('vi-VN')}đ</td>
                <td class="px-4 py-3 text-sm">
                    <span class="px-2 py-1 rounded-full text-xs font-medium ${inv.status === 'PAID' ? 'bg-green-100 text-green-800' : inv.status === 'PARTIAL' ? 'bg-yellow-100 text-yellow-800' : 'bg-red-100 text-red-800'}">${inv.status}</span>
                </td>
                <td class="px-4 py-3 text-sm space-x-2">
                    <a href="invoice-detail?id=${inv.id}" class="text-blue-600 hover:underline">Chi tiết</a>
                </td>
            </tr>
        `).join('');
    },

    async generateInvoices() {
        const month = parseInt(document.getElementById('gen-month').value);
        const year = parseInt(document.getElementById('gen-year').value);
        if (!month || !year) return alert('Vui lòng nhập tháng và năm');
        
        try {
            await apiFetch(this.apiBase + '/generate', {
                method: 'POST',
                body: JSON.stringify({ month, year })
            });
            alert('Đã tạo hóa đơn!');
            this.loadInvoices(month, year);
        } catch (e) { alert(e.message); }
    },

    async loadInvoiceDetail(id) {
        try {
            const inv = await apiFetch(this.apiBase + '/' + id);
            document.getElementById('inv-id').textContent = inv.id;
            document.getElementById('inv-student').textContent = inv.studentName;
            document.getElementById('inv-period').textContent = `${inv.month}/${inv.year}`;
            document.getElementById('inv-total').textContent = Number(inv.totalAmount).toLocaleString('vi-VN') + 'đ';
            document.getElementById('inv-paid').textContent = Number(inv.paidAmount).toLocaleString('vi-VN') + 'đ';
            document.getElementById('inv-remain').textContent = Number(inv.remainingAmount).toLocaleString('vi-VN') + 'đ';
            document.getElementById('inv-status').textContent = inv.status;
            document.getElementById('inv-doc-status').textContent = inv.invoiceStatus;
            
            if (inv.needRegenerate && document.getElementById('regenerate-btn')) {
                document.getElementById('regenerate-btn').classList.remove('hidden');
            }

            const tbody = document.getElementById('details-tbody');
            if (tbody) {
                tbody.innerHTML = (inv.details || []).map(d => `
                    <tr class="border-b">
                        <td class="px-4 py-2 text-sm">${d.sessionDate}</td>
                        <td class="px-4 py-2 text-sm">${d.courseName}</td>
                        <td class="px-4 py-2 text-sm">${d.status}</td>
                        <td class="px-4 py-2 text-sm">${Number(d.price).toLocaleString('vi-VN')}đ</td>
                    </tr>
                `).join('');
            }
        } catch (e) { document.getElementById('error-msg').textContent = e.message; }
    },

    async finalizeInvoice(id) {
        if (!confirm('Chốt hóa đơn? Sau khi chốt sẽ không thể sửa điểm danh hoặc tạo lại hóa đơn.')) return;
        try {
            await apiFetch(this.apiBase + '/' + id + '/finalize', { method: 'POST' });
            alert('Đã chốt hóa đơn!');
            location.reload();
        } catch (e) { alert(e.message); }
    },

    async regenerateInvoice(id) {
        if (!confirm('Tạo lại hóa đơn? (Sẽ xóa chi tiết cũ và tính toán lại dựa trên điểm danh hiện tại)')) return;
        try {
            await apiFetch(this.apiBase + '/' + id + '/regenerate', { method: 'POST' });
            alert('Đã tính toán lại!');
            location.reload();
        } catch (e) { alert(e.message); }
    },

    async recordPayment(e) {
        e.preventDefault();
        const form = e.target;
        const body = {
            invoiceId: parseInt(form.invoiceId.value),
            amount: parseFloat(form.amount.value),
            method: form.method.value,
            note: form.note.value
        };
        try {
            await apiFetch(this.apiBase + '/payments', { method: 'POST', body: JSON.stringify(body) });
            alert('Ghi nhận thanh toán thành công!');
            location.reload();
        } catch (err) { alert(err.message); }
    }
};
window.InvoiceManager = InvoiceManager;
