document.addEventListener("DOMContentLoaded", () => {
    window.authService.requireAuth(['ADMIN']);
    
    // Sort log theo performedAt desc
    window.ListHelper.init("/api/admin/audit-logs", renderTable, "performedAt", "desc");
});

function renderTable(response) {
    const tbody = document.getElementById("tableBody");
    tbody.innerHTML = "";

    if (!response || !response.content || response.content.length === 0) {
        tbody.innerHTML = `<tr><td colspan="6" class="px-6 py-12 text-center text-gray-500 text-sm">Không có dữ liệu log.</td></tr>`;
        return;
    }

    response.content.forEach((log) => {
        const tr = document.createElement("tr");
        tr.className = "hover:bg-gray-50 transition-colors";
        
        const date = new Date(log.performedAt).toLocaleString('vi-VN');
        const user = log.performedByUsername || 'System';
        
        let actionBadge = `<span class="px-2 py-1 text-xs font-semibold rounded bg-gray-100">${log.action}</span>`;
        if (log.action.includes('CREATE')) actionBadge = `<span class="px-2 py-1 text-xs font-semibold rounded bg-green-100 text-green-800">${log.action}</span>`;
        if (log.action.includes('UPDATE')) actionBadge = `<span class="px-2 py-1 text-xs font-semibold rounded bg-blue-100 text-blue-800">${log.action}</span>`;
        if (log.action.includes('DELETE')) actionBadge = `<span class="px-2 py-1 text-xs font-semibold rounded bg-red-100 text-red-800">${log.action}</span>`;

        // Format newValue if it's JSON
        let newValueDisplay = log.newValue || '';
        try {
            if (newValueDisplay.startsWith('{') || newValueDisplay.startsWith('[')) {
                const jsonObj = JSON.parse(newValueDisplay);
                newValueDisplay = `<pre class="text-xs text-gray-600 max-h-24 overflow-y-auto bg-gray-50 p-1 border rounded">${JSON.stringify(jsonObj, null, 2)}</pre>`;
            }
        } catch(e) {}
        
        let oldValueDisplay = log.oldValue ? `<div class="text-xs text-gray-400 line-through mb-1">${log.oldValue}</div>` : '';

        tr.innerHTML = `
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-500">${date}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm font-medium text-gray-900">${user}</td>
            <td class="px-6 py-4 whitespace-nowrap">${actionBadge}</td>
            <td class="px-6 py-4 whitespace-nowrap text-sm text-gray-700">
                <span class="font-semibold">${log.entityType}</span><br>
                <span class="text-xs text-gray-500">ID: ${log.entityId || '-'}</span>
            </td>
            <td class="px-6 py-4 text-sm text-gray-700 max-w-xs">
                ${oldValueDisplay}
                ${newValueDisplay}
            </td>
            <td class="px-6 py-4 whitespace-nowrap text-xs text-gray-500">${log.ipAddress || '-'}</td>
        `;
        tbody.appendChild(tr);
    });
}
