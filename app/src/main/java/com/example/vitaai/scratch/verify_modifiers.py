import os

src_dir = r"c:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai"

def check_file(path):
    with open(path, 'r', encoding='utf-8', errors='ignore') as f:
        lines = f.read().splitlines()
    
    # 1. Check for modifier ordering
    # Look for .background( or .border( and check if .clip( or .shadow( appears in subsequent lines
    # of the same modifier chain. A modifier chain continues as long as lines start with "." or contain "."
    # and there are no blank lines or other variable assignments.
    for i in range(len(lines)):
        line = lines[i].strip()
        if '.background(' in line or '.border(' in line:
            # We found a background/border. Let's look ahead to see if there's a clip/shadow in the same chain
            is_chain = True
            for j in range(i + 1, min(i + 15, len(lines))):
                next_line = lines[j].strip()
                if not next_line:
                    break
                # If it starts with another statement, it's not the same chain
                if next_line.startswith('val ') or next_line.startswith('var ') or next_line.startswith('fun ') or next_line.startswith('private '):
                    break
                
                # Check for clip or shadow
                if '.clip(' in next_line or '.shadow(' in next_line:
                    # Let's print this potential violation
                    print(f"MODIFIER ORDER ALERT in {os.path.basename(path)} around line {i+1}:")
                    for k in range(max(0, i-2), min(len(lines), j+3)):
                        prefix = "--> " if k == i or k == j else "    "
                        print(f"{prefix}{k+1}: {lines[k]}")
                    print("-" * 50)
                    break

    # 2. Check for shadow/graphicsLayer colors
    # We will search for shadow( or graphicsLayer( and check if it contains any color reference
    # that is colored.
    content = "\n".join(lines)
    import re
    
    # Check shadow(...)
    for match in re.finditer(r'shadow\s*\((.*?)\)', content, re.DOTALL):
        args = match.group(1)
        if 'Color' in args or 'color' in args:
            # Check if it contains monochrome patterns
            is_mono = False
            for mono in ['Black', 'Transparent', 'White', '0xFF0F172A', '0xFF1E293B', '0x1F0F172A', '0xbaFFFFFF']:
                if mono in args:
                    is_mono = True
            # Let's also check if it is just a color variable that starts with "Color.Black" or similar
            if not is_mono:
                line_no = content[:match.start()].count('\n') + 1
                print(f"COLORED SHADOW ALERT in {os.path.basename(path)} at line {line_no}: {match.group(0)}")

    # Check graphicsLayer(...)
    for match in re.finditer(r'graphicsLayer\s*\((.*?)\)', content, re.DOTALL):
        args = match.group(1)
        if 'Color' in args or 'color' in args:
            is_mono = False
            for mono in ['Black', 'Transparent', 'White', '0xFF0F172A', '0xFF1E293B', '0x1F0F172A', '0xbaFFFFFF']:
                if mono in args:
                    is_mono = True
            if not is_mono:
                line_no = content[:match.start()].count('\n') + 1
                print(f"COLORED GRAPHICS_LAYER SHADOW ALERT in {os.path.basename(path)} at line {line_no}: {match.group(0)}")

for root, _, files in os.walk(src_dir):
    for file in files:
        if file.endswith(".kt"):
            check_file(os.path.join(root, file))
