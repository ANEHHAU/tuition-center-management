document.addEventListener("DOMContentLoaded", () => {
    window.authService.requireAuth(['ADMIN']);
    loadDashboardStats();
    loadRecentActivities();
});

async function loadDashboardStats() {
    try {
        // MOCK data for now
        // In reality, this would hit /api/admin/dashboard/stats
        const res = await window.apiFetch('/api/admin/users?size=1');
        document.getElementById("statUsers").textContent = res.totalElements || '0';
        
        const resT = await window.apiFetch('/api/admin/users?role=TEACHER&size=1');
        document.getElementById("statTeachers").textContent = resT.totalElements || '0';
        
        const resS = await window.apiFetch('/api/admin/users?role=STUDENT&size=1');
        document.getElementById("statStudents").textContent = resS.totalElements || '0';
        
        const resC = await window.apiFetch('/api/admin/courses?size=1');
        document.getElementById("statCourses").textContent = Array.isArray(resC) ? resC.length : (resC.totalElements || '0');
        
        const resG = await window.apiFetch('/api/admin/groups?size=1');
        document.getElementById("statGroups").textContent = Array.isArray(resG) ? resG.length : (resG.totalElements || '0');
    } catch (e) {
        console.error("Lỗi tải thống kê", e);
    }
}

async function loadRecentActivities() {
    const container = document.getElementById("recentActivities");
    try {
        const res = await window.apiFetch('/api/admin/audit-logs?size=10&page=0');
        if (!res.content || res.content.length === 0) {
            container.innerHTML = '<p class="text-sm text-gray-500 text-center mt-5">Không có hoạt động nào.</p>';
            return;
        }
        
        container.innerHTML = res.content.map(log => `
            <div class="mb-3 pb-3 border-b border-gray-100 last:border-0 last:mb-0 last:pb-0">
                <div class="flex justify-between items-start">
                    <p class="text-sm font-medium text-gray-800">${log.performedByUsername || 'System'}</p>
                    <span class="text-xs text-gray-400">${new Date(log.performedAt).toLocaleDateString('vi-VN')}</span>
                </div>
                <p class="text-xs text-gray-600 mt-1">
                    <span class="font-semibold text-blue-600">${log.action}</span> 
                    trên <span class="font-semibold">${log.entityType}</span> 
                    (ID: ${log.entityId || 'N/A'})
                </p>
            </div>
        `).join('');
    } catch (e) {
        container.innerHTML = '<p class="text-sm text-red-500 text-center mt-5">Lỗi tải dữ liệu.</p>';
    }
}
