document.addEventListener("DOMContentLoaded", () => {
    window.authService.requireAuth(['ADMIN']);
    const params = new URLSearchParams(window.location.search);
    ['month','year','status','studentId'].forEach(k => { if (params.has(k)) window.ListHelper.state.additionalParams[k] = params.get(k); });
    window.ListHelper.init("/api/admin/invoices", renderTable, "id", "desc");
});

function renderTable(response) {
    const tbody = document.getElementById("tableBody");
    tbody.innerHTML = "";
    if (!response || !response.content || response.content.length === 0) {
        tbody.innerHTML = `<tr><td colspan="9" class="px-6 py-12 text-center text-gray-500 text-sm">Không tìm thấy hóa đơn nào.</td></tr>`;
        return;
    }
    const { page, size, content } = response;
    const fmt = v => v != null ? Number(v).toLocaleString('vi-VN') + ' đ' : '0 đ';
    content.forEach((inv, i) => {
        const tr = document.createElement("tr");
        tr.className = "hover:bg-gray-50 transition-colors";
        const debt = (inv.totalAmount || 0) - (inv.paidAmount || 0);
        let statusBadge;
        switch(inv.status) {
            case 'PAID': statusBadge = `<span class="px-2 py-1 text-xs font-semibold rounded-full bg-green-100 text-green-800">Đã trả đủ</span>`; break;
            case 'PARTIAL': statusBadge = `<span class="px-2 py-1 text-xs font-semibold rounded-full bg-yellow-100 text-yellow-800">Trả 1 phần</span>`; break;
            case 'OVERDUE': statusBadge = `<span class="px-2 py-1 text-xs font-semibold rounded-full bg-red-100 text-red-800">Quá hạn</span>`; break;
            default: statusBadge = `<span class="px-2 py-1 text-xs font-semibold rounded-full bg-gray-100 text-gray-800">Chưa trả</span>`;
        }
        tr.innerHTML = `
            <td class="px-6 py-4 text-sm text-gray-500">${page * size + i + 1}</td>
            <td class="px-6 py-4 text-sm font-medium text-gray-900">${inv.studentName || '-'}</td>
            <td class="px-6 py-4 text-sm text-gray-700">${inv.month}/${inv.year}</td>
            <td class="px-6 py-4 text-sm text-gray-700">${inv.totalSessions || 0}</td>
            <td class="px-6 py-4 text-sm text-right font-medium text-gray-900">${fmt(inv.totalAmount)}</td>
            <td class="px-6 py-4 text-sm text-right text-green-700">${fmt(inv.paidAmount)}</td>
            <td class="px-6 py-4 text-sm text-right text-red-700 font-semibold">${fmt(debt)}</td>
            <td class="px-6 py-4">${statusBadge}</td>
            <td class="px-6 py-4 text-right text-sm font-medium space-x-1">
                <a href="/admin/invoice-detail?id=${inv.id}" class="text-blue-600 hover:text-blue-900">Chi tiết</a>
            </td>`;
        tbody.appendChild(tr);
    });
}

function openGenerateModal() {
    const month = prompt("Nhập tháng cần tạo hóa đơn (1-12):");
    const year = prompt("Nhập năm:");
    if (!month || !year) return;
    generateInvoices(parseInt(month), parseInt(year));
}

async function generateInvoices(month, year) {
    try {
        await window.apiFetch("/api/admin/invoices/generate", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify({ month, year })
        });
        alert(`Đã tạo hóa đơn tháng ${month}/${year} thành công!`);
        window.ListHelper.load();
    } catch (e) { alert("Lỗi: " + (e.message || e)); }
}
