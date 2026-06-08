import json

transcript_path = r"C:\Users\rk107\.gemini\antigravity\brain\550ad5e0-cb44-4002-855c-9eeae139f1ad\.system_generated\logs\transcript.jsonl"

with open(transcript_path, "r", encoding="utf-8", errors="ignore") as f:
    for line in f:
        try:
            data = json.loads(line)
            step_index = data.get("step_index", 0)
            if step_index >= 1900:
                continue
            line_str = json.dumps(data)
            if "AnalyticsScreen.kt" in line_str:
                print(f"Step {step_index}: Found AnalyticsScreen.kt")
                if "tool_calls" in data:
                    for tc in data["tool_calls"]:
                        args = tc.get("args", {})
                        target = args.get("TargetFile", args.get("Target", ""))
                        if "AnalyticsScreen.kt" in target:
                            content = args.get("CodeContent", args.get("ReplacementContent", ""))
                            print(f"  Tool: {tc.get('name', '')}, Size: {len(content)}")
                            if len(content) > 0:
                                print(f"  Content preview: {content[:1000]}...")
        except Exception as e:
            pass
