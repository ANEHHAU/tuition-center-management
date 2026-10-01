import re

with open('src/main/resources/static/js/teacher/student.js', 'r', encoding='utf-8') as f:
    c = f.read()

c = c.replace(
    '${s.status === \'ACTIVE\' ? <button onclick="StudentManager.deleteStudent(${s.id})" class="text-red-600 hover:text-red-800 font-medium text-sm">Khóa</button> : <button onclick="StudentManager.restoreStudent(${s.id})" class="text-green-600 hover:text-green-800 font-medium text-sm">Mở khóa</button>}',
    '${s.status === \'ACTIVE\' ? `<button onclick="StudentManager.deleteStudent(${s.id})" class="text-red-600 hover:text-red-800 font-medium text-sm">Khóa</button>` : `<button onclick="StudentManager.restoreStudent(${s.id})" class="text-green-600 hover:text-green-800 font-medium text-sm">Mở khóa</button>`}'
)

with open('src/main/resources/static/js/teacher/student.js', 'w', encoding='utf-8') as f:
    f.write(c)
