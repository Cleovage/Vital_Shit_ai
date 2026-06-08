import json

transcript_path = r"C:\Users\rk107\.gemini\antigravity\brain\550ad5e0-cb44-4002-855c-9eeae139f1ad\.system_generated\logs\transcript.jsonl"

with open(transcript_path, "r", encoding="utf-8", errors="ignore") as f:
    for line in f:
        try:
            data = json.loads(line)
            if data.get("step_index") == 1244:
                print(json.dumps(data, indent=2))
        except Exception as e:
            pass
