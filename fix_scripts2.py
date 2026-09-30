import os
import re

dirs = ['src/main/resources/templates/admin', 'src/main/resources/templates/teacher']

for d in dirs:
    if not os.path.exists(d): continue
    for f in os.listdir(d):
        if not f.endswith('.html'): continue
        path = os.path.join(d, f)
        with open(path, 'r', encoding='utf-8') as file:
            content = file.read()
        
        # Match only the script at the very end. No dotall for the whole thing.
        # Find </div>\s*(<!--.*?-->\s*)?<script src=".*?"></script>\s*</body>\s*</html>
        
        match = re.search(r'(</div>)\s*(?:<!--.*?-->\s*)?(<script[^>]*>(?:.*?</script>)?)\s*</body>\s*</html>$', content, re.IGNORECASE)
        if match:
            script_tag = match.group(2)
            # Replace the matched part with the script INSIDE the div
            new_end = "\n" + script_tag + "\n</div>\n</body>\n</html>"
            content = content[:match.start()] + new_end
            
            with open(path, 'w', encoding='utf-8') as file:
                file.write(content)
            print(f"Fixed {path}")

