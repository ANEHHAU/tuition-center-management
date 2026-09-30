const InvoiceManager = {
    init() {
        // Set default month/year for filter
        const now = new Date();
        const m = now.getMonth() + 1;
        const y = now.getFullYear();
        document.getElementById('filterMonth').value = m;
        document.getElementById('filterYear').value = y;
        
        // ListHelper needs additionalParams for month/year initially
        window.ListHelper.state.additionalParams = { month: m, year: y };
        
        window.ListHelper.init(
            '/api/teacher/invoices',
            (res) => this.renderTable(res),
            'name',
            'asc'
        );
        
        this.bindEvents();
    },

    bindEvents() {
        document.getElementById('filterMonth').addEventListener('change', (e) => {
            window.ListHelper.setFilter('month', e.target.value);
        });
        document.getElementById('filterYear').addEventListener('change', (e) => {
            window.ListHelper.setFilter('year', e.target.value);
        });
    },

    renderTable(res) {
        const tbody = document.getElementById('tableBody');
        if (!tbody) return;
        
        if (!res.content || res.content.length === 0) {
            tbody.innerHTML = `<tr><td colspan="7" class="text-center py-4 text-gray-500">Không tìm thấy hóa đơn nào.</td></tr>`;
            return;
        }

        tbody.innerHTML = res.content.map(i => {
            let statusClass = 'bg-gray-100 text-gray-800';
            if (i.status === 'PAID') statusClass = 'bg-green-100 text-green-800';
            else if (i.status === 'PARTIAL') statusClass = 'bg-yellow-100 text-yellow-800';
            else if (i.status === 'UNPAID') statusClass = 'bg-red-100 text-red-800';

            const remaining = i.totalAmount - i.paidAmount;

            return `
            <tr class="border-b hover:bg-gray-50 transition-colors">
                <td class="px-4 py-3">
                    <div class="font-medium text-gray-800">${i.studentName}</div>
                </td>
                <td class="px-4 py-3">${i.month}/${i.year}</td>
                <td class="px-4 py-3 font-semibold text-gray-900">${Number(i.totalAmount).toLocaleString('vi-VN')} đ</td>
                <td class="px-4 py-3 text-green-600">${Number(i.paidAmount).toLocaleString('vi-VN')} đ</td>
                <td class="px-4 py-3 text-red-600">${Number(remaining).toLocaleString('vi-VN')} đ</td>
                <td class="px-4 py-3">
                    <span class="px-2 py-1 text-xs font-semibold rounded-full ${statusClass}">${i.status}</span>
                </td>
                <td class="px-4 py-3 space-x-2 text-right border-l border-gray-100">
                    <a href="/teacher/invoice-detail?id=${i.id}" class="text-indigo-600 hover:text-indigo-800 font-medium text-sm">Chi tiết</a>
                    ${i.status === 'DRAFT' ? `<button onclick="InvoiceManager.finalizeInvoice(${i.id})" class="text-blue-600 hover:text-blue-800 font-medium text-sm">Chốt</button>` : ''}
                    ${(i.status === 'UNPAID' || i.status === 'PARTIAL') ? `<button onclick="InvoiceManager.openPaymentModal(${i.id}, ${remaining})" class="text-green-600 hover:text-green-800 font-medium text-sm">Thu tiền</button>` : ''}
                </td>
            </tr>
            `;
        }).join('');
    },
    
    async generateInvoices() {
        const month = document.getElementById('filterMonth').value;
        const year = document.getElementById('filterYear').value;
        
        if (!confirm(`Bạn muốn tự động tạo hóa đơn cho tháng ${month}/${year} đối với tất cả học sinh?`)) return;
        
        try {
            await window.apiFetch('/api/teacher/invoices/generate', {
                method: 'POST',
                body: JSON.stringify({ month: parseInt(month), year: parseInt(year) })
            });
            alert('Tạo hóa đơn thành công!');
            window.ListHelper.load();
        } catch (e) {
            alert(e.message);
        }
    },
    
    async finalizeInvoice(id) {
        if (!confirm('Sau khi chốt, học sinh sẽ nhìn thấy hóa đơn này. Tiếp tục?')) return;
        try {
            await window.apiFetch('/api/teacher/invoices/' + id + '/finalize', { method: 'POST' });
            window.ListHelper.load();
        } catch (e) {
            alert(e.message);
        }
    },
    
    openPaymentModal(invoiceId, remaining) {
        document.getElementById('paymentForm').reset();
        document.getElementById('paymentInvoiceId').value = invoiceId;
        document.getElementById('paymentAmount').value = remaining;
        document.getElementById('paymentModal').classList.remove('hidden');
    },
    
    closePaymentModal() {
        document.getElementById('paymentModal').classList.add('hidden');
    },
    
    async submitPayment(e) {
        e.preventDefault();
        const form = e.target;
        const invoiceId = form.invoiceId.value;
        const body = {
            invoiceId: parseInt(invoiceId),
            amount: parseFloat(form.amount.value),
            method: form.method.value,
            paidAt: form.paidAt.value,
            note: form.note.value
        };
        
        try {
            await window.apiFetch('/api/teacher/invoices/payments', {
                method: 'POST',
                body: JSON.stringify(body)
            });
            alert('Ghi nhận thanh toán thành công!');
            this.closePaymentModal();
            window.ListHelper.load();
        } catch (error) {
            alert(error.message);
        }
    }
};

window.InvoiceManager = InvoiceManager;
