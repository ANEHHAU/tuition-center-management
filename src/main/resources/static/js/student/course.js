const courseList = new ListHelper('/api/student/courses', {
    defaultSort: 'name',
    renderItem: (item) => {
        const priceFormatted = new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(item.pricePerSession);
        
        return `
            <tr class="hover:bg-gray-50 transition-colors group">
                <td class="px-4 py-3">
                    <div class="flex items-center space-x-3">
                        <div class="w-10 h-10 rounded-lg bg-blue-100 flex items-center justify-center text-blue-600 font-bold overflow-hidden shrink-0">
                            ${item.coverUrl ? `<img src="${item.coverUrl}" class="w-full h-full object-cover">` : item.name.charAt(0)}
                        </div>
                        <div>
                            <p class="font-bold text-gray-800">${item.name}</p>
                            <p class="text-xs text-gray-500 line-clamp-1">${item.description || 'Không có mô tả'}</p>
                        </div>
                    </div>
                </td>
                <td class="px-4 py-3">
                    <span class="inline-flex items-center px-2 py-1 rounded-md bg-gray-100 text-gray-700 text-xs font-medium">
                        <i class="fas fa-chalkboard-teacher mr-1"></i> ${item.teacherName}
                    </span>
                </td>
                <td class="px-4 py-3 font-medium text-green-600">${priceFormatted}</td>
                <td class="px-4 py-3 text-right">
                    <button class="text-blue-600 hover:text-blue-900 px-3 py-1 bg-blue-50 hover:bg-blue-100 rounded transition-colors text-sm font-medium" onclick="viewCourseDetail(${item.id})">
                        Chi tiết
                    </button>
                </td>
            </tr>
        `;
    },
    renderEmpty: () => `
        <tr>
            <td colspan="4" class="px-4 py-8 text-center text-gray-500">
                <div class="flex flex-col items-center justify-center">
                    <i class="fas fa-box-open text-4xl mb-3 text-gray-300"></i>
                    <p>Bạn chưa tham gia khóa học nào</p>
                </div>
            </td>
        </tr>
    `,
    tbodyId: 'courseTableBody'
});

courseList.init();

function viewCourseDetail(id) {
    // Navigate to groups or show modal. For simplicity, just alert for now, or redirect to schedule filtered by course.
    window.location.href = `/student/schedule?courseId=${id}`;
}
