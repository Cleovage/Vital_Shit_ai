import os
import re

src_dir = r"c:\Users\rk107\wellbeing_firebase\app\src\main\java\com\example\vitaai"

def analyze_modifier_chains(path):
    with open(path, 'r', encoding='utf-8', errors='ignore') as f:
        content = f.read()

    # Find all Modifier chains.
    # A chain starts with Modifier or modifier and is followed by one or more .<ctrl42>(...) calls.
    # Let's find patterns like Modifier or modifier, then match all consecutive .something(...) calls.
    # We can do this by scanning for "Modifier" (case-insensitive or local variable name)
    # and reading tokens.
    
    # An easier way is line-by-line scanning, but keeping track of chains.
    lines = content.splitlines()
    chain = []
    in_chain = False
    chain_start = 0
    
    for idx, line in enumerate(lines):
        trimmed = line.strip()
        # Check if line continues a modifier chain
        if in_chain:
            # A line continues a chain if it starts with '.' or contains '.' and doesn't start a new statement
            if (trimmed.startswith('.') or (chain and trimmed.startswith('Modifier.')) or (chain and trimmed.startswith('modifier.'))):
                chain.append((idx + 1, trimmed))
            else:
                # Chain ended. Analyze it.
                analyze_chain(chain, path)
                chain = []
                in_chain = False
        
        # Check if line starts a new chain
        if not in_chain:
            if 'Modifier.' in trimmed or 'modifier.' in trimmed or (trimmed == 'Modifier' or trimmed == 'modifier'):
                in_chain = True
                chain_start = idx + 1
                chain = [(idx + 1, trimmed)]

    if in_chain and chain:
        analyze_chain(chain, path)

def analyze_chain(chain, path):
    # Let's see if we have .background before .clip
    background_idx = -1
    clip_idx = -1
    border_idx = -1
    
    for i, (line_no, text) in enumerate(chain):
        # We need to look for calls specifically: .background(..., .clip(..., .border(...
        # Using regex to find them as distinct calls, not inside string literals or comments
        if re.search(r'\.background\s*\(', text):
            # Check if background specifies a shape (e.g. RoundedCornerShape)
            # if it has a shape, it might be safe, but if followed by a clip, it might override or look weird
            background_idx = i
        if re.search(r'\.clip\s*\(', text):
            clip_idx = i
        if re.search(r'\.border\s*\(', text):
            border_idx = i
            
    # Check 1: .background before .clip
    if background_idx != -1 and clip_idx != -1 and background_idx < clip_idx:
        # Check if background has a shape parameter
        bg_text = chain[background_idx][1]
        # If background has a shape, it's usually safe, but let's check
        print(f"WARNING: .background before .clip in {os.path.basename(path)}:")
        print_chain(chain, background_idx, clip_idx)
        print("-" * 60)
        
    # Check 2: .border before .clip
    if border_idx != -1 and clip_idx != -1 and border_idx < clip_idx:
        print(f"WARNING: .border before .clip in {os.path.basename(path)}:")
        print_chain(chain, border_idx, clip_idx)
        print("-" * 60)

def print_chain(chain, idx1, idx2):
    for i, (line_no, text) in enumerate(chain):
        prefix = "--> " if i == idx1 or i == idx2 else "    "
        print(f"{prefix}{line_no}: {text}")

for root, _, files in os.walk(src_dir):
    for file in files:
        if file.endswith(".kt"):
            analyze_modifier_chains(os.path.join(root, file))
