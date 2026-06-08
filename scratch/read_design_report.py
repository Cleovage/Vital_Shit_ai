import json

transcript_path = r"C:\Users\rk107\.gemini\antigravity\brain\9dfee833-38a0-4990-b7de-6c1ef2ed5663\.system_generated\logs\transcript.jsonl"

with open(transcript_path, "r", encoding="utf-8", errors="ignore") as f:
    for line in f:
        try:
            data = json.loads(line)
            if data.get("source") == "MODEL" and data.get("type") == "PLANNER_RESPONSE":
                content = data.get("content", "")
                if "COLOR" in content or "palette" in content.lower() or "GlassCard" in content:
                    print(f"--- STEP {data.get('step_index')} ---")
                    print(content[:3000])
                    print("--------------------")
        except Exception as e:
            pass
