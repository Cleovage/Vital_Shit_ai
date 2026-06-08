import os
import filecmp

dir_fresh = r"c:\Users\rk107\wellbeing_fresh\app\src\main"
dir_firebase = r"c:\Users\rk107\wellbeing_firebase\app\src\main"

only_fresh = []
only_firebase = []
differing_files = []

for root, dirs, files in os.walk(dir_fresh):
    rel_path = os.path.relpath(root, dir_fresh)
    target_dir = os.path.join(dir_firebase, rel_path)
    
    for f in files:
        fresh_file = os.path.join(root, f)
        firebase_file = os.path.join(target_dir, f)
        
        if not os.path.exists(firebase_file):
            only_fresh.append(os.path.join(rel_path, f))
        else:
            if not filecmp.cmp(fresh_file, firebase_file, shallow=False):
                differing_files.append(os.path.join(rel_path, f))

for root, dirs, files in os.walk(dir_firebase):
    rel_path = os.path.relpath(root, dir_firebase)
    source_dir = os.path.join(dir_fresh, rel_path)
    for f in files:
        if not os.path.exists(os.path.join(source_dir, f)):
            only_firebase.append(os.path.join(rel_path, f))

print(f"Only in fresh ({len(only_fresh)}):")
for f in only_fresh:
    print(f"  - {f}")

print(f"\nOnly in firebase ({len(only_firebase)}):")
for f in only_firebase:
    print(f"  - {f}")

print(f"\nDiffering files ({len(differing_files)}):")
for f in differing_files:
    print(f"  - {f}")
