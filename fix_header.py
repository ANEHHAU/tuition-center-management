import re
with open('src/main/resources/templates/teacher/students.html', 'r', encoding='utf-8') as f:
    c = f.read()
c = re.sub(r'<th class=\"px-4 py-3 font-medium text-right\">Hành động</th>', '<th class=\"px-4 py-3 font-medium text-right\">Thông tin cá nhân</th>\n                <th class=\"px-4 py-3 font-medium text-right\">Quản lý</th>', c)
with open('src/main/resources/templates/teacher/students.html', 'w', encoding='utf-8') as f:
    f.write(c)
