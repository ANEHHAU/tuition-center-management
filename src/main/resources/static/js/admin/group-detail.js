document.addEventListener("DOMContentLoaded", () => {
    window.authService.requireAuth(['ADMIN']);
    const params = new URLSearchParams(window.location.search);
    const groupId = params.get("id");
    if (!groupId) { alert("Thiếu ID nhóm học"); return; }
    loadGroupDetail(groupId);
    loadStudents(groupId);
});

async function loadGroupDetail(groupId) {
    try {
        const g = await window.apiFetch(`/api/admin/groups/${groupId}`);
        document.getElementById("groupTitle").textContent = `Nhóm: ${g.name || ''}`;
        document.getElementById("infoCourseName").textContent = g.courseName || '-';
        document.getElementById("infoTeacherName").textContent = g.teacherName || '-';
        document.getElementById("infoStatusCount").textContent = `${g.status || '-'} / ${g.studentCount != null ? g.studentCount : 0} HS`;
        document.getElementById("editGroupBtn").href = `/admin/group-form?id=${groupId}`;
        
        if (g.publicLinkToken) {
            document.getElementById("publicLinkUrl").value = `${window.location.origin}/public/enroll/${g.publicLinkToken}`;
        } else {
            document.getElementById("publicLinkUrl").value = "Chưa có public link";
        }
    } catch (e) { console.error("Lỗi tải chi tiết nhóm:", e); }
}

async function loadStudents(groupId) {
    const tbody = document.getElementById("studentTableBody");
    try {
        const data = await window.apiFetch(`/api/admin/groups/${groupId}/students`);
        tbody.innerHTML = "";
        const students = data.content || data;
        if (!students || students.length === 0) {
            tbody.innerHTML = `<tr><td colspan="6" class="px-6 py-8 text-center text-gray-500">Chưa có học sinh nào trong nhóm này.</td></tr>`;
            return;
        }
        students.forEach((s, i) => {
            const tr = document.createElement("tr");
            tr.className = "hover:bg-gray-50";
            tr.innerHTML = `
                <td class="px-6 py-4 text-sm text-gray-500">${i + 1}</td>
                <td class="px-6 py-4 text-sm font-medium text-gray-900">${s.fullName || s.studentName || '-'}</td>
                <td class="px-6 py-4 text-sm text-gray-500">${s.email || '-'}</td>
                <td class="px-6 py-4 text-sm text-gray-500">${s.phone || '-'}</td>
                <td class="px-6 py-4 text-sm text-gray-500">${s.enrollDate || s.enrolledAt || '-'}</td>
                <td class="px-6 py-4 text-right text-sm">
                    <button onclick="removeStudent(${s.id || s.studentId})" class="text-red-600 hover:text-red-900">Xóa khỏi nhóm</button>
                </td>`;
            tbody.appendChild(tr);
        });
    } catch (e) { tbody.innerHTML = `<tr><td colspan="6" class="px-6 py-8 text-center text-red-500">Lỗi tải danh sách HS.</td></tr>`; }
}

function copyPublicLink() {
    const input = document.getElementById("publicLinkUrl");
    input.select();
    navigator.clipboard.writeText(input.value).then(() => alert("Đã copy link!"));
}

async function regenerateLink() {
    const groupId = new URLSearchParams(window.location.search).get("id");
    if (!confirm("Tạo lại link mới? Link cũ sẽ không còn dùng được.")) return;
    try {
        await window.apiFetch(`/api/admin/groups/${groupId}/public-link/regenerate`, { method: "POST" });
        loadGroupDetail(groupId);
    } catch (e) { alert("Lỗi: " + (e.message || e)); }
}

async function revokeLink() {
    const groupId = new URLSearchParams(window.location.search).get("id");
    if (!confirm("Thu hồi link? Học sinh sẽ không thể tự đăng ký nữa.")) return;
    try {
        await window.apiFetch(`/api/admin/groups/${groupId}/public-link/revoke`, { method: "PUT" });
        loadGroupDetail(groupId);
    } catch (e) { alert("Lỗi: " + (e.message || e)); }
}

async function removeStudent(studentId) {
    const groupId = new URLSearchParams(window.location.search).get("id");
    if (!confirm("Xóa học sinh khỏi nhóm?")) return;
    try {
        await window.apiFetch(`/api/admin/groups/${groupId}/students/${studentId}`, { method: "DELETE" });
        loadStudents(groupId);
    } catch (e) { alert("Lỗi: " + (e.message || e)); }
}

function openAddStudentModal() {
    alert("Chức năng thêm HS vào nhóm đang được phát triển.");
}
