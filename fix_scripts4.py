import os
import re

dirs = ['src/main/resources/templates/student']

for d in dirs:
    if not os.path.exists(d): continue
    for f in os.listdir(d):
        if not f.endswith('.html'): continue
        path = os.path.join(d, f)
        with open(path, 'r', encoding='utf-8') as file:
            content = file.read()
        
        match = re.search(r'(</div>)\s*<th:block th:fragment="pageScript">(.*?)</th:block>\s*</body>\s*</html>$', content, re.IGNORECASE | re.DOTALL)
        if match:
            script_tags = match.group(2).strip()
            new_end = "\n" + script_tags + "\n</div>\n</body>\n</html>"
            content = content[:match.start()] + new_end
            
            with open(path, 'w', encoding='utf-8') as file:
                file.write(content)
            print(f"Fixed {path}")

