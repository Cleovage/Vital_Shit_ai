import { motion } from "motion/react";
import { Play, Square, Flame, Droplets, Heart } from "lucide-react";
import { GlassCard } from "../components/GlassCard";
import { useState } from "react";

export function FastingTimer() {
  const [isFasting, setIsFasting] = useState(true);

  return (
    <div className="flex min-h-full flex-col px-7 py-8">
      <header className="flex items-center justify-between">
        <div>
          <p className="text-[12px] uppercase tracking-[0.38em] text-white/60">Intermittent Fasting</p>
          <h1 className="mt-2 text-[30px] font-medium tracking-[-0.06em] text-white">16:8 Protocol</h1>
        </div>
      </header>

      <motion.div
        initial={{ opacity: 0, scale: 0.95 }}
        animate={{ opacity: 1, scale: 1 }}
        transition={{ duration: 0.8, ease: [0.16, 1, 0.3, 1] }}
        className="mt-6 flex flex-col items-center justify-center py-6"
      >
        <div className="relative grid place-items-center">
          <svg className="absolute inset-0 h-[280px] w-[280px] -rotate-90 transform" viewBox="0 0 100 100">
            <circle cx="50" cy="50" r="48" fill="none" stroke="rgba(255,255,255,0.1)" strokeWidth="1.5" />
            <motion.circle
              cx="50"
              cy="50"
              r="48"
              fill="none"
              stroke="url(#gradient)"
              strokeWidth="2.5"
              strokeDasharray="301.59"
              initial={{ strokeDashoffset: 301.59 }}
              animate={{ strokeDashoffset: isFasting ? 120 : 301.59 }}
              transition={{ duration: 1.5, ease: "easeOut" }}
            />
            <defs>
              <linearGradient id="gradient" x1="0%" y1="0%" x2="100%" y2="0%">
                <stop offset="0%" stopColor="#ff4bdf" />
                <stop offset="100%" stopColor="#1228ff" />
              </linearGradient>
            </defs>
          </svg>

          <div className="flex h-[280px] w-[280px] flex-col items-center justify-center rounded-full bg-white/5 backdrop-blur-md shadow-[inset_0_2px_20px_rgba(255,255,255,0.1)]">
            <Flame className="mb-4 h-6 w-6 text-fuchsia-300" strokeWidth={1.5} />
            <span className="text-[72px] font-light leading-none tracking-[-0.06em]">
              {isFasting ? "14:23" : "00:00"}
            </span>
            <span className="mt-2 text-[14px] text-white/60">
              {isFasting ? "Fasting Time" : "Ready to start"}
            </span>
          </div>
        </div>
        
        <div className="mt-10">
          <button
            onClick={() => setIsFasting(!isFasting)}
            className={`flex h-14 w-48 items-center justify-center gap-2 rounded-full transition-transform active:scale-95 ${
              isFasting 
                ? "bg-white/10 backdrop-blur-md text-white shadow-[inset_0_1px_0_rgba(255,255,255,0.2)]"
                : "bg-white text-black"
            }`}
          >
            {isFasting ? (
              <>
                <Square className="h-5 w-5 fill-white" strokeWidth={1.5} />
                <span className="font-medium">End Fast</span>
              </>
            ) : (
              <>
                <Play className="h-5 w-5 fill-black" strokeWidth={1.5} />
                <span className="font-medium">Start Fasting</span>
              </>
            )}
          </button>
        </div>
      </motion.div>

      <div className="mt-6 flex-1 space-y-4 mb-4">
        <GlassCard className="p-5">
          <div className="flex items-center gap-4">
            <div className="grid h-12 w-12 place-items-center rounded-full bg-white/10">
              <Droplets className="h-5 w-5 text-white/80" />
            </div>
            <div>
              <p className="text-[12px] uppercase tracking-[0.2em] text-white/50">Current Stage</p>
              <h2 className="mt-1 text-[18px] font-medium tracking-[-0.03em]">Fat Burning</h2>
              <p className="mt-1 text-[13px] text-white/60">Insulin levels are low. Body is using stored fat for energy.</p>
            </div>
          </div>
        </GlassCard>

        <GlassCard className="p-5">
          <div className="flex items-center gap-4">
            <div className="grid h-12 w-12 place-items-center rounded-full bg-white/10">
              <Heart className="h-5 w-5 text-white/80" />
            </div>
            <div className="flex-1">
              <div className="flex items-center justify-between">
                <p className="text-[12px] uppercase tracking-[0.2em] text-white/50">Next Stage</p>
                <span className="text-[12px] text-white/40">in 2 hrs</span>
              </div>
              <h2 className="mt-1 text-[18px] font-medium tracking-[-0.03em]">Ketosis</h2>
            </div>
          </div>
        </GlassCard>
      </div>
    </div>
  );
}
