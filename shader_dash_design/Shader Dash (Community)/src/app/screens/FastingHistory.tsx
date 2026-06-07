import { motion } from "motion/react";
import { AreaChart, Area, XAxis, YAxis, ResponsiveContainer, Tooltip, BarChart, Bar } from "recharts";
import { GlassCard } from "../components/GlassCard";
import { Calendar, TrendingDown } from "lucide-react";

const weightData = [
  { day: "Mon", weight: 75.5 },
  { day: "Tue", weight: 75.2 },
  { day: "Wed", weight: 74.8 },
  { day: "Thu", weight: 74.9 },
  { day: "Fri", weight: 74.5 },
  { day: "Sat", weight: 74.2 },
  { day: "Sun", weight: 73.8 },
];

const fastDuration = [
  { day: "Mon", hours: 16 },
  { day: "Tue", hours: 16.5 },
  { day: "Wed", hours: 18 },
  { day: "Thu", hours: 15.5 },
  { day: "Fri", hours: 17 },
  { day: "Sat", hours: 14 },
  { day: "Sun", hours: 16 },
];

export function FastingHistory() {
  return (
    <div className="flex min-h-full flex-col px-7 py-8">
      <header className="pb-6">
        <p className="text-[12px] uppercase tracking-[0.38em] text-white/60">Your Progress</p>
        <h1 className="mt-2 text-[30px] font-medium tracking-[-0.06em] text-white">Statistics</h1>
      </header>

      <motion.div
        initial={{ opacity: 0, y: 15 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
        className="space-y-6 pb-6"
      >
        <div className="grid grid-cols-2 gap-4">
          <GlassCard className="p-5">
            <p className="text-[12px] uppercase tracking-[0.1em] text-white/50">Total Fasts</p>
            <p className="mt-2 text-[32px] font-light leading-none tracking-[-0.04em]">24</p>
          </GlassCard>
          <GlassCard className="p-5">
            <p className="text-[12px] uppercase tracking-[0.1em] text-white/50">Longest Streak</p>
            <p className="mt-2 text-[32px] font-light leading-none tracking-[-0.04em]">12<span className="text-[16px] text-white/60 ml-1">days</span></p>
          </GlassCard>
        </div>

        <GlassCard className="p-6">
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center gap-2 text-[14px] text-white/80">
              <Calendar className="h-4 w-4" />
              Fasting Hours (This Week)
            </div>
          </div>
          <div className="h-[180px] w-full min-h-[180px]">
            <ResponsiveContainer width="100%" height={180} minWidth={1} minHeight={1}>
              <BarChart data={fastDuration} margin={{ top: 0, right: 0, left: -20, bottom: 0 }}>
                <XAxis dataKey="day" stroke="none" tick={{ fill: "rgba(255,255,255,0.5)", fontSize: 11 }} dy={10} />
                <Tooltip
                  cursor={{ fill: "rgba(255,255,255,0.1)" }}
                  contentStyle={{ backgroundColor: "rgba(0,0,0,0.8)", border: "1px solid rgba(255,255,255,0.1)", borderRadius: "12px" }}
                  itemStyle={{ color: "#fff" }}
                />
                <Bar dataKey="hours" fill="rgba(255,255,255,0.8)" radius={[4, 4, 4, 4]} barSize={12} />
              </BarChart>
            </ResponsiveContainer>
          </div>
        </GlassCard>

        <GlassCard className="p-6">
          <div className="flex items-center justify-between mb-6">
            <div className="flex items-center gap-2 text-[14px] text-white/80">
              <TrendingDown className="h-4 w-4" />
              Weight Trend
            </div>
            <span className="text-[14px] font-medium text-emerald-300">-1.7 kg</span>
          </div>
          <div className="h-[180px] w-full min-h-[180px]">
            <ResponsiveContainer width="100%" height={180} minWidth={1} minHeight={1}>
              <AreaChart data={weightData} margin={{ top: 10, right: 0, left: -20, bottom: 0 }}>
                <defs>
                  <linearGradient id="colorWeight" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#ff4bdf" stopOpacity={0.4} />
                    <stop offset="95%" stopColor="#1228ff" stopOpacity={0} />
                  </linearGradient>
                </defs>
                <XAxis dataKey="day" stroke="none" tick={{ fill: "rgba(255,255,255,0.5)", fontSize: 11 }} dy={10} />
                <YAxis domain={['dataMin - 1', 'dataMax + 1']} stroke="none" tick={{ fill: "rgba(255,255,255,0.5)", fontSize: 11 }} />
                <Tooltip
                  contentStyle={{ backgroundColor: "rgba(0,0,0,0.8)", border: "1px solid rgba(255,255,255,0.1)", borderRadius: "12px" }}
                  itemStyle={{ color: "#fff" }}
                />
                <Area type="monotone" dataKey="weight" stroke="#ff4bdf" strokeWidth={3} fillOpacity={1} fill="url(#colorWeight)" />
              </AreaChart>
            </ResponsiveContainer>
          </div>
        </GlassCard>
      </motion.div>
    </div>
  );
}
