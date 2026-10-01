html_content = '''<!DOCTYPE html>
<html xmlns:th="http://www.thymeleaf.org"
      th:replace="~{layout/main :: layout('Quản lý học sinh', ~{::main})}">
<head>
<meta charset="UTF-8">
</head>
<body>
  <main class="space-y-6">
    <div class="flex justify-between items-center">
      <div>
        <h1 class="text-2xl font-bold text-gray-900">Quản lý học sinh</h1>
        <p class="text-gray-500 mt-1">Quản lý danh sách học sinh của bạn</p>
      </div>
      <div class="flex space-x-3">
        <button onclick="StudentManager.loadGroups()" class="px-4 py-2 border border-gray-300 rounded-lg text-gray-700 bg-white hover:bg-gray-50 transition flex items-center space-x-2">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4 4v5h.582m15.356 2A8.001 8.001 0 004.582 9m0 0H9m11 11v-5h-.581m0 0a8.003 8.003 0 01-15.357-2m15.357 2H15"></path></svg>
          <span>Làm mới</span>
        </button>
        <button onclick="StudentManager.openCreateModal()" class="px-4 py-2 bg-blue-600 text-white rounded-lg shadow hover:bg-blue-700 transition flex items-center space-x-2">
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M18 9v3m0 0v3m0-3h3m-3 0h-3m-2-5a4 4 0 11-8 0 4 4 0 018 0zM3 20a6 6 0 0112 0v1H3v-1z"></path></svg>
          <span>Tạo mới tài khoản học sinh</span>
        </button>
      </div>
    </div>

    <!-- Search & Filter -->
    <div class="bg-white p-4 rounded-xl shadow-sm border border-gray-100 flex flex-col md:flex-row md:items-center justify-between gap-4">
      <div th:replace="~{layout/fragments/search-bar :: searchBar('Tìm tên, email, SĐT...')}"></div>
    </div>

    <!-- Table -->
    <div class="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden">
      <div class="overflow-x-auto">
        <table class="w-full text-left border-collapse">
          <thead>
            <tr class="bg-gray-50 text-gray-600 text-sm uppercase tracking-wider border-b">
              <th th:replace="~{layout/fragments/sort-header :: sortHeader('Họ tên', 'name')}"></th>
              <th th:replace="~{layout/fragments/sort-header :: sortHeader('Email', 'email')}"></th>
              <th class="px-4 py-3 font-medium">Số điện thoại</th>
              <th class="px-4 py-3 font-medium">Nhóm - Khóa học</th>
              <th class="px-4 py-3 font-medium">Trạng thái</th>
              <th class="px-4 py-3 font-medium text-right">Thông tin cá nhân</th>
              <th class="px-4 py-3 font-medium text-right">Quản lý</th>
            </tr>
          </thead>
          <tbody id="tableBody">
            <!-- Render by JS -->
          </tbody>
        </table>
      </div>
      
      <!-- Pagination -->
      <div th:replace="~{layout/fragments/pagination :: pagination}"></div>
    </div>

    <!-- Modal Tạo/Sửa Học sinh -->
    <div id="studentModal" class="hidden fixed inset-0 z-50 overflow-y-auto bg-black bg-opacity-50">
        <div class="flex items-center justify-center min-h-screen px-4">
            <div class="bg-white rounded-xl shadow-lg w-full max-w-md overflow-hidden">
                <div class="px-6 py-4 border-b border-gray-200 flex justify-between items-center">
                    <h3 class="text-lg font-bold text-gray-900" id="modalTitle">Thêm Học Sinh</h3>
                    <button type="button" onclick="StudentManager.closeModal()" class="text-gray-400 hover:text-gray-500">
                        <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                        </svg>
                    </button>
                </div>
                <form id="studentForm" class="p-6 space-y-4">
                    <input type="hidden" id="studentId" name="id">
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1">Họ tên *</label>
                        <input type="text" id="studentFullName" name="fullName" required class="w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500">
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1">Tên đăng nhập *</label>
                        <input type="text" id="studentUsername" name="username" required class="w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500">
                    </div>
                    <div id="passwordGroup">
                        <label class="block text-sm font-medium text-gray-700 mb-1">Mật khẩu *</label>
                        <input type="password" id="studentPassword" name="password" class="w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500">
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1">Email</label>
                        <input type="email" id="studentEmail" name="email" class="w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500">
                    </div>
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1">Số điện thoại</label>
                        <input type="text" id="studentPhone" name="phone" class="w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500">
                    </div>
                    <div class="pt-4 flex justify-end space-x-3">
                        <button type="button" onclick="StudentManager.closeModal()" class="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 transition">Hủy</button>
                        <button type="submit" class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition shadow-sm">Lưu</button>
                    </div>
                </form>
            </div>
        </div>
    </div>
    
    <!-- Modal Reset Password -->
    <div id="passwordModal" class="hidden fixed inset-0 z-50 overflow-y-auto bg-black bg-opacity-50">
        <div class="flex items-center justify-center min-h-screen px-4">
            <div class="bg-white rounded-xl shadow-lg w-full max-w-sm overflow-hidden">
                <div class="px-6 py-4 border-b border-gray-200 flex justify-between items-center">
                    <h3 class="text-lg font-bold text-gray-900">Đặt lại mật khẩu</h3>
                    <button type="button" onclick="StudentManager.closePasswordModal()" class="text-gray-400 hover:text-gray-500">
                        <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor"><path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/></svg>
                    </button>
                </div>
                <form id="passwordForm" class="p-6 space-y-4">
                    <input type="hidden" id="resetStudentId">
                    <div>
                        <label class="block text-sm font-medium text-gray-700 mb-1">Mật khẩu mới</label>
                        <input type="password" id="newPassword" required class="w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500">
                    </div>
                    <div class="pt-4 flex justify-end space-x-3">
                        <button type="button" onclick="StudentManager.closePasswordModal()" class="px-4 py-2 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-50 transition">Hủy</button>
                        <button type="submit" class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition shadow-sm">Xác nhận</button>
                    </div>
                </form>
            </div>
        </div>
    </div>

    <!-- Modal Tìm và Thêm Học sinh vào nhóm -->
    <div id="searchStudentModal" class="hidden fixed inset-0 z-50 overflow-y-auto bg-black bg-opacity-50">
        <div class="flex items-center justify-center min-h-screen px-4">
            <div class="bg-white rounded-xl shadow-lg w-full max-w-2xl overflow-hidden">
                <div class="px-6 py-4 border-b border-gray-200 flex justify-between items-center">
                    <h3 class="text-lg font-bold text-gray-900">Thêm / Sửa Nhóm Học Sinh</h3>
                    <button type="button" onclick="StudentManager.closeSearchModal()" class="text-gray-400 hover:text-gray-500">
                        <svg class="h-6 w-6" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12"/>
                        </svg>
                    </button>
                </div>
                <div class="p-6 space-y-4">
                    <div id="searchResult" class="border border-blue-100 bg-blue-50 p-4 rounded-lg">
                        <p class="font-bold text-gray-900" id="foundName"></p>
                        <p class="text-sm text-gray-600" id="foundContact"></p>
                        <input type="hidden" id="foundStudentId">
                        
                        <div class="mt-4 flex space-x-2">
                            <div class="flex-1">
                                <label class="block text-sm font-medium text-gray-700 mb-1">Chọn Khóa Học</label>
                                <select id="enrollCourseId" onchange="StudentManager.onCourseChange()" class="w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500">
                                    <option value="">-- Chọn Khóa học --</option>
                                </select>
                            </div>
                            <div class="flex-1">
                                <label class="block text-sm font-medium text-gray-700 mb-1">Chọn Nhóm</label>
                                <select id="enrollGroupId" class="w-full border-gray-300 rounded-md shadow-sm focus:ring-blue-500 focus:border-blue-500">
                                    <option value="">-- Chọn nhóm --</option>
                                </select>
                            </div>
                            <div class="flex items-end">
                                <button type="button" id="btnEnroll" onclick="StudentManager.enrollStudent()" class="px-4 py-2 bg-blue-600 text-white rounded-md hover:bg-blue-700 transition shadow-sm">Thêm vào nhóm</button>
                            </div>
                        </div>
                    </div>
                    
                    <div class="mt-4">
                        <h4 class="font-bold text-gray-800 mb-2">Các khóa học / nhóm đã tham gia:</h4>
                        <div class="border rounded-md">
                            <table class="w-full text-left text-sm">
                                <thead class="bg-gray-50 border-b">
                                    <tr>
                                        <th class="px-4 py-2 font-medium text-gray-600">Khóa học</th>
                                        <th class="px-4 py-2 font-medium text-gray-600">Nhóm</th>
                                        <th class="px-4 py-2 font-medium text-right text-gray-600">Hành động</th>
                                    </tr>
                                </thead>
                                <tbody id="enrolledGroupsTable">
                                    <!-- Render by JS -->
                                </tbody>
                            </table>
                        </div>
                    </div>
                    
                    <div class="pt-4 flex justify-end space-x-3">
                        <button type="button" onclick="StudentManager.closeSearchModal()" class="px-4 py-2 bg-gray-100 border border-gray-300 rounded-md text-gray-700 hover:bg-gray-200 transition">Đóng</button>
                    </div>
                </div>
            </div>
        </div>
    </div>

    <script th:src="@{/js/teacher/student.js}"></script>
    <script>
      document.addEventListener("DOMContentLoaded", function() {
        window.authService.requireAuth(['TEACHER']);
        StudentManager.init();
      });
    </script>
  </main>
</body>
</html>
'''

with open('src/main/resources/templates/teacher/students.html', 'w', encoding='utf-8') as f:
    f.write(html_content)
