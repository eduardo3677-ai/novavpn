import os

replacements = {
    "EchoSmart": "NovaVPN",
    "echosmart": "novavpn",
    "EchoAssets": "NovaAssets",
    "EchoSmartTheme": "NovaTheme",
    "EchoColors": "NovaColors",
    "EchoSmartViewModel": "NovaViewModel",
    "EchoSmartApp": "NovaApp",
    "EchoSplashScreen": "NovaSplashScreen"
}

def replace_in_file(filepath):
    try:
        with open(filepath, 'r', encoding='utf-8') as f:
            content = f.read()
            
        original = content
        for k, v in replacements.items():
            if k == 'echosmart' and 'me/echosmart' in filepath:
                # Be careful not to break the package path right away, or wait, if we change 'echosmart' to 'novavpn', we must move the directory!
                # Let's skip lower case 'echosmart' for now to avoid breaking the package structure without moving folders.
                pass
            else:
                content = content.replace(k, v)
                
        # Safe package replacement
        content = content.replace('me.echosmart', 'me.novavpn')
        
        if content != original:
            with open(filepath, 'w', encoding='utf-8') as f:
                f.write(content)
            print(f"Updated {filepath}")
    except Exception as e:
        print(f"Error reading {filepath}: {e}")

for root, dirs, files in os.walk('app'):
    for file in files:
        if file.endswith('.kt') or file.endswith('.xml') or file.endswith('.kts'):
            replace_in_file(os.path.join(root, file))

