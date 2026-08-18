import os

def replace_in_file(filepath):
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
            
        original = content
        # Replace occurrences of echo_ with nova_ in code files
        content = content.replace('echo_', 'nova_')
        
        if content != original:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"Updated {filepath}")
    except Exception as e:
        print(f"Error reading {filepath}: {e}")

# Rename files in res/drawable and res/values
res_dir = 'app/src/main/res/drawable'
if os.path.exists(res_dir):
    for f in os.listdir(res_dir):
        if f.startswith('echo_'):
            new_name = f.replace('echo_', 'nova_')
            os.rename(os.path.join(res_dir, f), os.path.join(res_dir, new_name))
            print(f"Renamed {f} to {new_name}")

# Also rename files in res/values if needed (like echo_brand_colors.xml, but it's brand_colors.xml)
# Now update references in all Kotlin and XML files
for root, dirs, files in os.walk('app'):
    for file in files:
        if file.endswith('.kt') or file.endswith('.xml') or file.endswith('.kts'):
            replace_in_file(os.path.join(root, file))
