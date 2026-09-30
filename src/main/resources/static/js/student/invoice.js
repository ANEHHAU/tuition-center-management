const currencyFmt = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' });

function formatInvoiceStatus(status) {
    const map = {
        UNPAID: '<span class="px-2 py-1 text-xs rounded-full bg-red-100 text-red-700 font-medium">Chưa thanh toán</span>',
        PARTIAL: '<span class="px-2 py-1 text-xs rounded-full bg-yellow-100 text-yellow-700 font-medium">Thanh toán một phần</span>',
        PAID: '<span class="px-2 py-1 text-xs rounded-full bg-green-100 text-green-700 font-medium">Đã thanh toán</span>',
        OVERDUE: '<span class="px-2 py-1 text-xs rounded-full bg-red-200 text-red-800 font-bold">Quá hạn</span>',
    };
    return map[status] || `<span>${status}</span>`;
}

// ===== Invoice List Page =====
if (document.getElementById('invoiceTableBody')) {
    const invoiceList = new ListHelper('/api/student/invoices', {
        defaultSort: 'month',
        defaultSortDir: 'desc',
        renderItem: (item) => {
            const remaining = item.remainingAmount || 0;
            return `
            <tr class="hover:bg-gray-50 transition-colors">
                <td class="px-4 py-3 font-medium text-gray-800">Tháng ${item.month}/${item.year}</td>
                <td class="px-4 py-3 text-gray-700">${currencyFmt.format(item.totalAmount)}</td>
                <td class="px-4 py-3 text-green-600 font-medium">${currencyFmt.format(item.paidAmount || 0)}</td>
                <td class="px-4 py-3 ${remaining > 0 ? 'text-red-600 font-bold' : 'text-gray-400'}">${currencyFmt.format(remaining)}</td>
                <td class="px-4 py-3">${formatInvoiceStatus(item.status)}</td>
                <td class="px-4 py-3 text-right">
                    <a href="/student/invoice-detail?id=${item.id}" class="text-blue-600 hover:text-blue-900 px-3 py-1 bg-blue-50 hover:bg-blue-100 rounded transition-colors text-sm font-medium">
                        Chi tiết
                    </a>
                </td>
            </tr>`;
        },
        renderEmpty: () => `<tr><td colspan="6" class="px-4 py-10 text-center text-gray-500"><i class="fas fa-box-open text-3xl mb-2 text-gray-300 block"></i>Chưa có hóa đơn nào</td></tr>`,
        tbodyId: 'invoiceTableBody'
    });

    document.addEventListener('DOMContentLoaded', async () => {
        // Fill year filter
        const currentYear = new Date().getFullYear();
        const yearSelect = document.getElementById('filterYear');
        for (let y = currentYear; y >= currentYear - 3; y--) {
            yearSelect.innerHTML += `<option value="${y}">${y}</option>`;
        }

        invoiceList.init();

        // Load debt info
        try {
            const debt = await ApiClient.get('/api/student/invoices/current-debt');
            if (debt.totalDebt > 0) {
                document.getElementById('debtCard').classList.remove('hidden');
                document.getElementById('totalDebtAmount').textContent = currencyFmt.format(debt.totalDebt);
            } else {
                document.getElementById('noDebtCard').classList.remove('hidden');
            }
        } catch (e) { console.error(e); }
    });
}

// ===== Invoice Detail Page =====
let currentInvoiceId = null;

if (document.getElementById('invoiceHeader')) {
    document.addEventListener('DOMContentLoaded', async () => {
        const params = new URLSearchParams(window.location.search);
        currentInvoiceId = params.get('id');
        if (!currentInvoiceId) return;

        try {
            const inv = await ApiClient.get(`/api/student/invoices/${currentInvoiceId}`);

            // Header
            document.getElementById('invoiceHeader').innerHTML = `
                <div class="grid grid-cols-2 md:grid-cols-4 gap-4">
                    <div><p class="text-xs text-gray-500 uppercase font-medium">Tháng</p><p class="text-2xl font-bold text-gray-800">${inv.month}/${inv.year}</p></div>
                    <div><p class="text-xs text-gray-500 uppercase font-medium">Tổng tiền</p><p class="text-2xl font-bold text-blue-600">${currencyFmt.format(inv.totalAmount)}</p></div>
                    <div><p class="text-xs text-gray-500 uppercase font-medium">Đã trả</p><p class="text-2xl font-bold text-green-600">${currencyFmt.format(inv.paidAmount || 0)}</p></div>
                    <div><p class="text-xs text-gray-500 uppercase font-medium">Còn nợ</p><p class="text-2xl font-bold ${(inv.remainingAmount || 0) > 0 ? 'text-red-600' : 'text-gray-400'}">${currencyFmt.format(inv.remainingAmount || 0)}</p></div>
                </div>
                <div class="mt-4 pt-4 border-t border-gray-100">
                    ${formatInvoiceStatus(inv.status)}
                </div>`;

            // Breakdown
            const tbody = document.getElementById('breakdownTableBody');
            if (inv.details && inv.details.length > 0) {
                tbody.innerHTML = inv.details.map(d => `
                    <tr class="hover:bg-gray-50">
                        <td class="px-4 py-3">${d.sessionDate || '—'}</td>
                        <td class="px-4 py-3 text-gray-600">${d.groupName || '—'}</td>
                        <td class="px-4 py-3">${d.status ? formatAttStatus(d.status) : '—'}</td>
                        <td class="px-4 py-3 text-right font-medium">${currencyFmt.format(d.priceSnapshot || 0)}</td>
                    </tr>`).join('');
            } else {
                tbody.innerHTML = `<tr><td colspan="4" class="px-4 py-6 text-center text-gray-400">Không có chi tiết</td></tr>`;
            }

            // Payments
            const payDiv = document.getElementById('paymentHistory');
            if (inv.payments && inv.payments.length > 0) {
                payDiv.innerHTML = `<div class="space-y-3">${inv.payments.map(p => `
                    <div class="flex items-center justify-between p-3 bg-green-50 border border-green-100 rounded-lg">
                        <div>
                            <p class="font-bold text-green-700">${currencyFmt.format(p.amount)}</p>
                            <p class="text-xs text-gray-500">${p.method} • ${new Date(p.paidAt).toLocaleDateString('vi-VN')} • ${p.note || ''}</p>
                        </div>
                        <i class="fas fa-check-circle text-green-500 text-xl"></i>
                    </div>`).join('')}</div>`;
            }

            // Show notify button if not fully paid
            if (inv.status !== 'PAID') {
                document.getElementById('notifySection').classList.remove('hidden');
                document.getElementById('notifyAmount').value = inv.remainingAmount || 0;
            }
        } catch (e) {
            document.getElementById('invoiceHeader').innerHTML = `<p class="text-red-500">Không tải được hóa đơn: ${e.message}</p>`;
        }
    });
}

function formatAttStatus(status) {
    const map = {
        PRESENT: '<span class="text-xs text-green-600 font-medium">✓ Có mặt</span>',
        ABSENT: '<span class="text-xs text-red-600 font-medium">✗ Vắng</span>',
        EXCUSED: '<span class="text-xs text-yellow-600 font-medium">⚠ Có phép</span>',
        CANCELLED: '<span class="text-xs text-gray-400">— Hủy</span>',
    };
    return map[status] || status;
}

async function submitNotifyPayment() {
    const amount = document.getElementById('notifyAmount').value;
    const method = document.getElementById('notifyMethod').value;
    const note = document.getElementById('notifyNote').value;

    if (!amount || parseFloat(amount) <= 0) {
        alert('Vui lòng nhập số tiền hợp lệ');
        return;
    }

    try {
        await ApiClient.post(`/api/student/invoices/${currentInvoiceId}/notify-payment`, { amount: parseFloat(amount), method, note });
        alert('Đã gửi thông báo thanh toán! Giáo viên sẽ xác nhận sớm.');
        location.reload();
    } catch (e) {
        alert('Lỗi: ' + e.message);
    }
}
