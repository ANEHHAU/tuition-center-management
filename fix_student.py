import re

with open('src/main/resources/static/js/teacher/student.js', 'r', encoding='utf-8') as f:
    c = f.read()

c = c.replace(
    '                    <button onclick="StudentManager.deleteStudent(${s.id})" class="text-red-600 hover:text-red-800 font-medium text-sm">Khóa</button>\n                </td>',
    '                </td>\n                <td class="px-4 py-3 space-x-2 text-right border-l border-gray-100">\n                    ${s.status === \'ACTIVE\' ? <button onclick="StudentManager.deleteStudent(${s.id})" class="text-red-600 hover:text-red-800 font-medium text-sm">Khóa</button> : <button onclick="StudentManager.restoreStudent(${s.id})" class="text-green-600 hover:text-green-800 font-medium text-sm">Mở khóa</button>}\n                    <button onclick="StudentManager.openSearchModal(\'${s.email || s.phone || \'\'}\')" class="text-indigo-600 hover:text-indigo-800 font-medium text-sm">Nhóm</button>\n                </td>'
)

c = c.replace(
    '    openSearchModal() {',
    '    openSearchModal(autoQuery = \'\') {'
)
c = c.replace(
    '        document.getElementById(\'searchStudentModal\').classList.remove(\'hidden\');\n    },',
    '        document.getElementById(\'searchStudentModal\').classList.remove(\'hidden\');\n        if (autoQuery) {\n            document.getElementById(\'searchQuery\').value = autoQuery;\n            this.searchStudent();\n        }\n    },'
)

restore = '''
    async restoreStudent(id) {
        if (!confirm('Bạn chắc chắn muốn mở khóa học sinh này?')) return;
        try {
            await window.apiFetch('/api/teacher/students/' + id + '/restore', { method: 'POST' });
            window.ListHelper.load();
        } catch (e) {
            alert(e.message);
        }
    },
'''
c = c.replace('async deleteStudent(id) {', restore + '\n    async deleteStudent(id) {')

with open('src/main/resources/static/js/teacher/student.js', 'w', encoding='utf-8') as f:
    f.write(c)

