import { useState } from "react";
import { motion } from "motion/react";
import { Send, Sparkles, User as UserIcon } from "lucide-react";
import { GlassCard } from "../components/GlassCard";

const INITIAL_MESSAGES = [
  {
    role: "assistant",
    content: "Hi! I'm VitaAI, your health expert in intermittent fasting and healthy keto. How can I help you reach your goals today?"
  }
];

export function VitaChat() {
  const [messages, setMessages] = useState(INITIAL_MESSAGES);
  const [input, setInput] = useState("");

  const handleSend = () => {
    if (!input.trim()) return;
    
    const newMessages = [
      ...messages,
      { role: "user", content: input }
    ];
    setMessages(newMessages);
    setInput("");

    // Mock response
    setTimeout(() => {
      setMessages(prev => [
        ...prev,
        {
          role: "assistant",
          content: "That's a great question! During a fast, you can definitely drink black coffee, plain tea, or water without breaking your fast. Would you like some tips on staying hydrated?"
        }
      ]);
    }, 1000);
  };

  return (
    <div className="flex min-h-full flex-col px-6 py-8">
      <header className="flex items-center gap-3 pb-6 shrink-0">
        <div className="grid h-12 w-12 place-items-center rounded-full bg-white/10 backdrop-blur-md">
          <Sparkles className="h-5 w-5 text-fuchsia-300" />
        </div>
        <div>
          <h1 className="text-[22px] font-medium tracking-[-0.04em] text-white">VitaAI</h1>
          <p className="text-[13px] text-white/60">Your Health Expert</p>
        </div>
      </header>

      <div className="flex-1 overflow-y-auto space-y-4 pb-4 custom-scrollbar" style={{ scrollbarWidth: 'none', msOverflowStyle: 'none' }}>
        {messages.map((msg, i) => (
          <motion.div
            key={i}
            initial={{ opacity: 0, y: 10 }}
            animate={{ opacity: 1, y: 0 }}
            className={`flex ${msg.role === "user" ? "justify-end" : "justify-start"}`}
          >
            <div className={`flex max-w-[85%] gap-3 ${msg.role === "user" ? "flex-row-reverse" : ""}`}>
              <div className="mt-auto grid h-8 w-8 shrink-0 place-items-center rounded-full bg-white/10">
                {msg.role === "user" ? (
                  <UserIcon className="h-4 w-4 text-white/70" />
                ) : (
                  <Sparkles className="h-4 w-4 text-fuchsia-300" />
                )}
              </div>
              <GlassCard className={`px-4 py-3 ${msg.role === "user" ? "bg-white/15" : "bg-white/5"}`}>
                <p className="text-[14px] leading-relaxed text-white/90">{msg.content}</p>
              </GlassCard>
            </div>
          </motion.div>
        ))}
      </div>

      <div className="pt-4 shrink-0">
        <div className="flex items-center gap-2 rounded-[24px] bg-white/10 p-2 pr-2 backdrop-blur-xl shadow-[inset_0_1px_0_rgba(255,255,255,0.1)]">
          <input
            type="text"
            value={input}
            onChange={(e) => setInput(e.target.value)}
            onKeyDown={(e) => e.key === "Enter" && handleSend()}
            placeholder="Ask about fasting, keto..."
            className="flex-1 bg-transparent px-4 py-3 text-[15px] text-white placeholder-white/40 outline-none"
          />
          <button
            onClick={handleSend}
            className="grid h-12 w-12 shrink-0 place-items-center rounded-[20px] bg-white text-black transition-transform active:scale-95 disabled:opacity-50"
            disabled={!input.trim()}
          >
            <Send className="h-5 w-5 ml-1" />
          </button>
        </div>
      </div>
    </div>
  );
}
