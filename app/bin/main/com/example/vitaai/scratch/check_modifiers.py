import os
import re

source_dir = r"c:\Users\rk107\wellbeing_firebase\app"

def analyze_file(filepath):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # Find modifier chains that span multiple lines.
    # We want to match:
    # 1) Modifier or modifier or Modifier.something
    # 2) A sequence of .func(...) calls.
    # If in that sequence we find .background(...) or .border(...) followed by .clip(...) or .shadow(...)
    # under the same modifier chain.
    # Let's search for modifier expressions. Typically, a modifier chain starts with `Modifier` or `modifier` (or `ModifierExtensions` etc.)
    # and consists of chained calls.
    # Let's find matches where `Modifier` / `modifier` is followed by a chain of calls.
    # Alternatively, we can find any `.background(...)` or `.border(...)` and then look ahead up to 300 characters
    # (allowing newlines, but not allowing empty lines or another val/var definition or function definition)
    # to see if `.clip(` or `.shadow(` is called.
    
    # We will search for `.background` or `.border` and see if `.clip` or `.shadow` is called after it,
    # without crossing double newlines (which usually separates statements) or brackets that close/open blocks.
    # Let's write a scanning loop.
    lines = content.split('\n')
    for i, line in enumerate(lines):
        if '.background(' in line or '.border(' in line:
            # Look ahead up to 10 lines
            chain = []
            has_clip_or_shadow = False
            clip_or_shadow_line = -1
            # Reconstruct the block
            for offset in range(0, 12):
                idx = i + offset
                if idx >= len(lines):
                    break
                l = lines[idx].strip()
                # If we hit an empty line or something that looks like the start of a new statement/definition, stop
                if offset > 0 and (l == "" or l.startswith("val ") or l.startswith("fun ") or l.startswith("var ") or l.startswith("import ") or l.startswith("class ") or l.startswith("interface ") or l.startswith("package ")):
                    break
                chain.append(l)
                if '.clip(' in l or '.shadow(' in l:
                    has_clip_or_shadow = True
                    clip_or_shadow_line = idx + 1
                    break
            
            if has_clip_or_shadow:
                # We found a clip or shadow after background/border in the same block.
                # Let's print it.
                chain_str = " -> ".join(chain)
                print(f"BAD ORDER: {filepath}:{i+1} (clip/shadow at {clip_or_shadow_line}) -> {chain_str}")

    # Check for shadow/glow colors that are not monochrome.
    shadow_matches = re.finditer(r'shadow\s*\((.*?)\)', content, re.DOTALL)
    for match in shadow_matches:
        args = match.group(1)
        # Check if the colors in shadow are not monochrome.
        # Monochrome colors: Color.Black, Color.Black.copy(...), Color.Transparent, Color(0xFF0F172A).copy(...)
        # Wait, Color(0xFF0F172A) is Slate/Black (very dark monochrome), but let's list it anyway to be safe.
        # Colored colors: Primary, accentColor, accentAmberColor, glowColor, Color.Cyan, Color.Red, etc.
        # If it doesn't contain Black, Slate, or 0x0F172A / 0x1E293B, it might be colored.
        # Let's list all shadows containing Color.
        if 'Color' in args:
            # Let's check if it is colored
            is_colored = True
            for mono in ['Black', 'Black.copy', 'Transparent', '0xFF0F172A', '0xFF1E293B', '0x1F0F172A', '0xbaFFFFFF', 'White', 'Color.White']:
                if mono in args:
                    is_colored = False
            if is_colored:
                print(f"COLORED SHADOW: {filepath}:{content[:match.start()].count(chr(10))+1} -> {match.group(0)}")

    # setShadowLayer
    shadow_layer_matches = re.finditer(r'setShadowLayer\s*\((.*?)\)', content, re.DOTALL)
    for match in shadow_layer_matches:
        args = match.group(1)
        is_colored = True
        for mono in ['Black', 'shadowColor', 'statShadowColor', 'Color.Black', '0xFF0F172A', '0xFF1E293B']:
            if mono in args:
                is_colored = False
        if is_colored:
            print(f"COLORED SHADOW_LAYER: {filepath}:{content[:match.start()].count(chr(10))+1} -> {match.group(0)}")

# Find all kt files
for root, dirs, files in os.walk(source_dir):
    for file in files:
        if file.endswith('.kt'):
            analyze_file(os.path.join(root, file))
