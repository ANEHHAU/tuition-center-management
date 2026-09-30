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
        
        # Check if there is a script just before </body>
        script_match = re.search(r'(<!--.*?-->\s*)?(<script[^>]*>.*?</script>)\s*</body>\s*</html>', content, re.DOTALL)
        if script_match:
            script_tag = script_match.group(2)
            # Remove the script from the end
            content = content.replace(script_match.group(0), '</body>\n</html>')
            # Insert the script before the last </div> which is before </body>
            # We can find the last </div> before </body>
            last_div_idx = content.rfind('</div>')
            if last_div_idx != -1:
                content = content[:last_div_idx] + script_tag + '\n' + content[last_div_idx:]
            
            with open(path, 'w', encoding='utf-8') as file:
                file.write(content)
            print(f"Fixed {path}")

