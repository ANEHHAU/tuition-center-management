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
        
        # Match <th:block th:fragment="pageScript"> ... </th:block> at the end
        match = re.search(r'(</div>)\s*<th:block th:fragment="pageScript">\s*(<script[^>]*>(?:.*?</script>)?)\s*</th:block>\s*</body>\s*</html>$', content, re.IGNORECASE)
        if match:
            script_tag = match.group(2)
            new_end = "\n" + script_tag + "\n</div>\n</body>\n</html>"
            content = content[:match.start()] + new_end
            
            with open(path, 'w', encoding='utf-8') as file:
                file.write(content)
            print(f"Fixed {path}")

