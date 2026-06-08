import { useState } from "react";
import { AnimatePresence, motion } from "motion/react";
import { Link } from "react-router";
import { toast } from "sonner";
import { Area, AreaChart, Bar, BarChart, CartesianGrid, ResponsiveContainer, Tooltip, XAxis, YAxis } from "recharts";
import { Activity, Apple, Award, Bot, CalendarDays, ChevronLeft, ChevronRight, Coffee, Droplets, Dumbbell, Edit3, Flame, HeartPulse, Lightbulb, LogOut, Moon, Plus, Salad, Send, Settings, ShieldCheck, Sparkles, TrendingUp, User, Utensils, Zap } from "lucide-react";
import { ActionRow } from "../components/ActionRow";
import { GlassCard } from "../components/GlassCard";
import { PageHeader } from "../components/PageHeader";
import { SectionHeader } from "../components/SectionHeader";

const week = [
  { day: "5", kcal: 0, protein: 0, water: 0, min: 0, hr: 0, sleep: 0 },
  { day: "11", kcal: 0, protein: 0, water: 0, min: 0, hr: 0, sleep: 0 },
  { day: "17", kcal: 0, protein: 0, water: 0, min: 0, hr: 0, sleep: 0 },
  { day: "23", kcal: 0, protein: 0, water: 0, min: 0, hr: 0, sleep: 0 },
  { day: "29", kcal: 586, protein: 61, water: 0.92, min: 24, hr: 78, sleep: 6.8 },
  { day: "1", kcal: 210, protein: 27, water: 0.34, min: 12, hr: 72, sleep: 7.4 },
  { day: "3", kcal: 512, protein: 74, water: 1.1, min: 19, hr: 74, sleep: 7.1 },
];

type AnalyticRange = "day" | "week" | "month" | "year";
type AnalyticKey = "hr" | "sleep" | "water" | "kcal" | "protein" | "min";

type AnalyticDefinition = {
  key: AnalyticKey;
  title: string;
  shortTitle: string;
  value: string;
  unit: string;
  color: string;
  icon: any;
  insight: string;
  target: string;
};

const analyticSeries: Record<AnalyticKey, Record<AnalyticRange, Array<{ label: string; value: number }>>> = {
  hr: {
    day: [
      { label: "6A", value: 68 }, { label: "9A", value: 74 }, { label: "12P", value: 82 }, { label: "3P", value: 77 }, { label: "6P", value: 88 }, { label: "9P", value: 71 },
    ],
    week: week.map((item) => ({ label: item.day, value: item.hr })),
    month: [
      { label: "W1", value: 73 }, { label: "W2", value: 76 }, { label: "W3", value: 72 }, { label: "W4", value: 74 },
    ],
    year: [
      { label: "Jan", value: 75 }, { label: "Mar", value: 73 }, { label: "May", value: 74 }, { label: "Jul", value: 71 }, { label: "Sep", value: 72 }, { label: "Nov", value: 70 },
    ],
  },
  sleep: {
    day: [
      { label: "Deep", value: 1.4 }, { label: "REM", value: 1.8 }, { label: "Light", value: 3.4 }, { label: "Awake", value: 0.5 },
    ],
    week: week.map((item) => ({ label: item.day, value: item.sleep })),
    month: [
      { label: "W1", value: 6.6 }, { label: "W2", value: 7.2 }, { label: "W3", value: 6.9 }, { label: "W4", value: 7.1 },
    ],
    year: [
      { label: "Jan", value: 6.5 }, { label: "Mar", value: 6.9 }, { label: "May", value: 7.2 }, { label: "Jul", value: 7.4 }, { label: "Sep", value: 7.0 }, { label: "Nov", value: 7.1 },
    ],
  },
  water: {
    day: [
      { label: "8A", value: 0.22 }, { label: "10A", value: 0.42 }, { label: "12P", value: 0.64 }, { label: "2P", value: 0.78 }, { label: "5P", value: 1.1 }, { label: "8P", value: 1.28 },
    ],
    week: week.map((item) => ({ label: item.day, value: item.water })),
    month: [
      { label: "W1", value: 1.45 }, { label: "W2", value: 1.72 }, { label: "W3", value: 1.28 }, { label: "W4", value: 1.1 },
    ],
    year: [
      { label: "Jan", value: 1.25 }, { label: "Mar", value: 1.41 }, { label: "May", value: 1.68 }, { label: "Jul", value: 1.82 }, { label: "Sep", value: 1.5 }, { label: "Nov", value: 1.38 },
    ],
  },
  kcal: { day: [], week: week.map((item) => ({ label: item.day, value: item.kcal })), month: [], year: [] },
  protein: { day: [], week: week.map((item) => ({ label: item.day, value: item.protein })), month: [], year: [] },
  min: { day: [], week: week.map((item) => ({ label: item.day, value: item.min })), month: [], year: [] },
};

const analyticsCatalog: AnalyticDefinition[] = [
  { key: "hr", title: "Average heart-rate trend", shortTitle: "Heart rate", value: "74", unit: "BPM", color: "#f43f5e", icon: HeartPulse, insight: "Peak load is at 6 PM; recovery heart rate is improving.", target: "Target 62-78 bpm" },
  { key: "sleep", title: "Sleep duration trend", shortTitle: "Sleep", value: "7.1", unit: "H", color: "#3b82f6", icon: Moon, insight: "Consistency is up 12% with fewer wake events this week.", target: "Goal 7.5 h" },
  { key: "water", title: "Health Connect plus logged drinks", shortTitle: "Hydration", value: "1.10", unit: "L", color: "#06b6d4", icon: Droplets, insight: "You usually lag before lunch. VitaAI recommends 650 ml by noon.", target: "Goal 2.4 L" },
];

const rangeLabels: Record<AnalyticRange, string> = { day: "Day", week: "Week", month: "Month", year: "Year" };

function Metric({ icon: Icon, label, value, tone }: { icon: any; label: string; value: string; tone: string }) {
  return (
    <GlassCard className="p-5 shadow-[0_4px_20px_rgba(0,0,0,0.04)] hover:shadow-[0_8px_28px_rgba(0,0,0,0.07)] transition-shadow">
      <div className="flex items-center gap-4">
        <div className={`flex h-12 w-12 items-center justify-center rounded-[18px] ${tone} shadow-sm`}><Icon className="h-6 w-6" /></div>
        <div>
          <p className="text-[14px] font-medium text-black/50">{label}</p>
          <p className="text-[24px] font-semibold tracking-[-0.04em] text-[#0f172a]">{value}</p>
        </div>
      </div>
    </GlassCard>
  );
}

function ChartCard({ title, value, unit, color, dataKey, range = "week" }: { title: string; value: string; unit: string; color: string; dataKey: string; range?: AnalyticRange }) {
  const gradientId = `vita-${dataKey}-${range}-gradient`;
  const chartData = analyticSeries[dataKey as AnalyticKey]?.[range] ?? [];

  return (
    <GlassCard className="relative min-h-[220px] overflow-hidden border border-black/[0.06] p-6 shadow-[0_4px_24px_rgba(0,0,0,0.05)] hover:shadow-[0_10px_36px_rgba(0,0,0,0.08)] transition-all">
      <motion.div
        animate={{ opacity: [0.08, 0.18, 0.08], scale: [1, 1.05, 1] }}
        transition={{ duration: 5, repeat: Infinity, ease: "easeInOut" }}
        className="absolute -right-12 -top-12 h-40 w-40 rounded-full blur-3xl pointer-events-none"
        style={{ backgroundColor: color }}
      />
      <div className="relative z-10 mb-4 flex items-start justify-between">
        <p className="max-w-[280px] text-[15px] font-medium text-black/60">{title}</p>
        <div className="text-right">
          <p className="text-[28px] font-semibold leading-none tracking-[-0.05em]" style={{ color }}>{value}</p>
          <p className="text-[12px] font-bold text-black/50 mt-1">{unit}</p>
        </div>
      </div>
      <div className="relative z-10 h-[140px] w-full min-h-[140px]">
        <ResponsiveContainer width="100%" height={140} minWidth={1} minHeight={1}>
          <AreaChart key={`chart-${dataKey}-${range}`} data={chartData} margin={{ top: 8, right: 6, left: -22, bottom: 0 }}>
            <defs key={`defs-${dataKey}`}>
              <linearGradient key={`gradient-${dataKey}`} id={gradientId} x1="0" x2="0" y1="0" y2="1">
                <stop key="s1" offset="0%" stopColor={color} stopOpacity={0.45} />
                <stop key="s2" offset="100%" stopColor={color} stopOpacity={0.03} />
              </linearGradient>
            </defs>
            <XAxis key={`x-${dataKey}`} dataKey="label" tick={{ fill: "rgba(15,23,42,.45)", fontSize: 12 }} axisLine={false} tickLine={false} />
            <YAxis key={`y-${dataKey}`} tick={{ fill: "rgba(15,23,42,.38)", fontSize: 12 }} axisLine={false} tickLine={false} />
            <Tooltip key={`tooltip-${dataKey}`} contentStyle={{ background: "rgba(255,255,255,.94)", border: "1px solid rgba(0,0,0,.07)", borderRadius: 18, color: "#0f172a", fontSize: "14px", backdropFilter: "blur(12px)", boxShadow: "0 8px 24px rgba(0,0,0,0.08)" }} />
            <Area key={`area-${dataKey}`} type="monotone" dataKey="value" stroke={color} fill={`url(#${gradientId})`} strokeWidth={3} dot={false} activeDot={{ r: 6, fill: color, strokeWidth: 2, stroke: "#fff" }} />
          </AreaChart>
        </ResponsiveContainer>
      </div>
    </GlassCard>
  );
}

export function Home() {
  return (
    <div className="flex flex-1 flex-col pb-6 w-full">
      <PageHeader title="VitaAI" kicker="Health companion" />

      {/* Readiness card */}
      <GlassCard className="relative mb-6 overflow-hidden p-6 border border-black/[0.06] shadow-[0_8px_36px_rgba(6,182,212,0.08)] transition-all">
        {/* Ambient glows — lighter for white bg */}
        <motion.div
          animate={{ opacity: [0.18, 0.38, 0.18], scale: [1, 1.15, 1] }}
          transition={{ duration: 6, repeat: Infinity, ease: "easeInOut" }}
          className="absolute -left-10 -top-10 h-48 w-48 rounded-full bg-cyan-300/25 blur-3xl pointer-events-none"
        />
        <motion.div
          animate={{ opacity: [0.12, 0.28, 0.12], scale: [1, 1.25, 1] }}
          transition={{ duration: 7, repeat: Infinity, ease: "easeInOut", delay: 1 }}
          className="absolute -bottom-10 -right-10 h-48 w-48 rounded-full bg-blue-300/20 blur-3xl pointer-events-none"
        />

        <div className="relative z-10">
          <div className="flex items-center justify-between">
            <div className="flex flex-col">
              <div className="flex items-center gap-2">
                <p className="text-[13px] font-bold uppercase tracking-[0.15em] text-black/45">Readiness</p>
                <span className="rounded-full border border-emerald-200/80 bg-emerald-50/90 px-2.5 py-1 text-[10px] font-bold uppercase tracking-wider text-emerald-700 backdrop-blur-md shadow-[0_2px_8px_rgba(34,197,94,0.12)]">
                  Optimal
                </span>
              </div>

              <div className="mt-2 flex items-baseline gap-1.5">
                <h2 className="text-[64px] font-semibold leading-none tracking-[-0.06em] text-[#0f172a]">86</h2>
                <span className="text-[24px] font-medium text-black/30">%</span>
              </div>

              <div className="mt-4 flex gap-4 text-[13px] font-semibold text-black/40">
                <div className="flex items-center gap-1.5"><Moon className="h-4 w-4 text-blue-500" /> Sleep <span className="text-[#0f172a]">92%</span></div>
                <div className="flex items-center gap-1.5"><HeartPulse className="h-4 w-4 text-rose-500" /> HRV <span className="text-[#0f172a]">81%</span></div>
              </div>
            </div>

            <div className="relative flex h-36 w-36 items-center justify-center p-1">
              <motion.svg
                animate={{ rotate: 360 }}
                transition={{ duration: 40, repeat: Infinity, ease: "linear" }}
                className="absolute inset-0 h-full w-full opacity-20"
                viewBox="0 0 100 100"
              >
                <circle cx="50" cy="50" r="48" stroke="rgba(15,23,42,1)" strokeWidth="1.5" strokeDasharray="1 6" fill="none" strokeLinecap="round" />
              </motion.svg>

              <svg className="absolute inset-0 h-full w-full -rotate-90 drop-shadow-md" viewBox="0 0 100 100">
                <defs>
                  <linearGradient id="readinessGrad" x1="0%" y1="0%" x2="100%" y2="100%">
                    <stop offset="0%" stopColor="#06b6d4" />
                    <stop offset="100%" stopColor="#3b82f6" />
                  </linearGradient>
                </defs>
                <circle cx="50" cy="50" r="40" stroke="rgba(15,23,42,.06)" strokeWidth="9" fill="none" />
                <motion.circle
                  cx="50" cy="50" r="40"
                  stroke="url(#readinessGrad)" strokeWidth="9" fill="none"
                  strokeLinecap="round"
                  strokeDasharray="251.2"
                  initial={{ strokeDashoffset: 251.2 }}
                  animate={{ strokeDashoffset: 251.2 - (0.86 * 251.2) }}
                  transition={{ duration: 1.8, ease: "easeOut", delay: 0.1 }}
                />
              </svg>

              <div className="relative z-10 flex h-[84px] w-[84px] items-center justify-center rounded-full bg-white/80 backdrop-blur-2xl shadow-[inset_0_2px_8px_rgba(255,255,255,1),0_4px_20px_rgba(0,0,0,0.08)] border border-white/90">
                <motion.div initial={{ scale: 0.5, opacity: 0 }} animate={{ scale: 1, opacity: 1 }} transition={{ delay: 0.8, type: "spring", stiffness: 200, damping: 12 }}>
                  <Sparkles className="h-10 w-10 text-cyan-500" />
                </motion.div>
              </div>
            </div>
          </div>

          <div className="mt-6 rounded-[20px] bg-black/[0.03] p-4 border border-black/[0.05] backdrop-blur-sm">
            <p className="text-[14px] leading-relaxed text-[#0f172a]/80">
              <strong className="font-semibold text-[#0f172a]">Prime condition.</strong> Workout load is balanced. Add a protein-rich meal and 900 ml water to close your Vita ring.
            </p>
          </div>
        </div>
      </GlassCard>

      <div className="mb-6 grid grid-cols-2 gap-4">
        <Metric icon={Flame} label="Active energy" value="512 kcal" tone="bg-amber-100/80 text-amber-700" />
        <Metric icon={Dumbbell} label="Training" value="12 min" tone="bg-cyan-100/80 text-cyan-700" />
        <Metric icon={Utensils} label="Protein" value="74 g" tone="bg-yellow-100/80 text-yellow-700" />
        <Metric icon={Moon} label="Sleep" value="7.1 h" tone="bg-blue-100/80 text-blue-700" />
      </div>

      <div className="mt-2 flex items-center justify-between mb-4">
        <h3 className="text-[16px] font-semibold tracking-tight text-[#0f172a]">Today's Insights & Tips</h3>
      </div>

      <div className="flex flex-col gap-4 pb-4">
        <GlassCard className="p-5 transition-all duration-300 hover:scale-[1.015] hover:shadow-[0_8px_24px_rgba(0,0,0,0.06)]">
          <div className="flex gap-4">
            <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-[18px] bg-rose-100/80 text-rose-600 shadow-sm">
              <Lightbulb className="h-6 w-6" />
            </div>
            <div>
              <p className="text-[15px] font-semibold text-[#0f172a]">Recovery Prioritization</p>
              <p className="mt-1.5 text-[14px] leading-relaxed text-black/55">Your deep sleep was slightly lower last night. Consider winding down 30 mins earlier today and avoiding screens before bed.</p>
            </div>
          </div>
        </GlassCard>

        <GlassCard className="p-5 transition-all duration-300 hover:scale-[1.015] hover:shadow-[0_8px_24px_rgba(0,0,0,0.06)]">
          <div className="flex gap-4">
            <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-[18px] bg-emerald-100/80 text-emerald-600 shadow-sm">
              <Coffee className="h-6 w-6" />
            </div>
            <div>
              <p className="text-[15px] font-semibold text-[#0f172a]">Afternoon Energy Dip</p>
              <p className="mt-1.5 text-[14px] leading-relaxed text-black/55">Based on your activity patterns, you might feel a dip around 3 PM. Try substituting coffee with a quick 10-min brisk walk or stretching session.</p>
            </div>
          </div>
        </GlassCard>

        <GlassCard className="p-5 transition-all duration-300 hover:scale-[1.015] hover:shadow-[0_8px_24px_rgba(0,0,0,0.06)]">
          <div className="flex gap-4">
            <div className="flex h-12 w-12 shrink-0 items-center justify-center rounded-[18px] bg-blue-100/80 text-blue-600 shadow-sm">
              <Droplets className="h-6 w-6" />
            </div>
            <div>
              <p className="text-[15px] font-semibold text-[#0f172a]">Hydration Check-in</p>
              <p className="mt-1.5 text-[14px] leading-relaxed text-black/55">You're currently 400ml behind your daily hydration pace. Grab a glass of water now to stay on track for your 2.4L goal.</p>
            </div>
          </div>
        </GlassCard>
      </div>
    </div>
  );
}

export function Health() {
  return (
    <div className="space-y-6 pb-6">
      <PageHeader title="Health" kicker="Movement & fuel" />

      <div className="grid grid-cols-2 gap-4">
        {/* Steps Animated Card */}
        <GlassCard className="relative overflow-hidden p-5 border border-cyan-100/60 shadow-[0_4px_20px_rgba(6,182,212,0.06)] hover:shadow-[0_8px_30px_rgba(6,182,212,0.10)] transition-all">
          <motion.div
            animate={{ opacity: [0.08, 0.22, 0.08] }}
            transition={{ duration: 3, repeat: Infinity, ease: "easeInOut" }}
            className="absolute -right-4 -top-4 h-32 w-32 rounded-full bg-cyan-300/40 blur-3xl pointer-events-none"
          />
          <div className="relative z-10 flex items-start justify-between">
            <div className="flex flex-col">
              <p className="text-[14px] font-medium text-black/50 flex items-center gap-2">
                <Activity className="h-4 w-4 text-cyan-500" /> Steps
              </p>
              <div className="mt-2 flex items-baseline gap-1">
                <p className="text-[32px] font-semibold tracking-[-0.04em] text-[#0f172a]">4,820</p>
              </div>
              <p className="text-[12px] font-medium text-black/40 mt-1 uppercase tracking-wider">Goal: 10,000</p>
            </div>
            <div className="relative flex h-[52px] w-[52px] items-center justify-center shrink-0">
              <svg className="absolute inset-0 h-full w-full -rotate-90 drop-shadow-sm" viewBox="0 0 46 46">
                <circle cx="23" cy="23" r="20" stroke="rgba(6,182,212,0.12)" strokeWidth="4" fill="none" />
                <motion.circle
                  cx="23" cy="23" r="20"
                  stroke="#06b6d4" strokeWidth="4" fill="none" strokeLinecap="round"
                  strokeDasharray="125.6"
                  initial={{ strokeDashoffset: 125.6 }}
                  animate={{ strokeDashoffset: 125.6 - (0.482 * 125.6) }}
                  transition={{ duration: 1.5, ease: "easeOut", delay: 0.2 }}
                />
              </svg>
              <motion.div animate={{ y: [-2, 2, -2] }} transition={{ duration: 2.5, repeat: Infinity, ease: "easeInOut" }}>
                <Activity className="h-5 w-5 text-cyan-600" />
              </motion.div>
            </div>
          </div>
          <div className="mt-6 flex gap-2 items-center w-full">
            {[...Array(8)].map((_, i) => (
              <motion.div
                key={`step-dot-${i}`}
                animate={{ opacity: [0.2, 1, 0.2], scale: [0.9, 1, 0.9] }}
                transition={{ duration: 1.5, repeat: Infinity, delay: i * 0.15, ease: "easeInOut" }}
                className={`h-2 flex-1 rounded-full ${i < 4 ? "bg-cyan-400" : "bg-cyan-100"}`}
              />
            ))}
          </div>
        </GlassCard>

        {/* Heart Rate Animated Card */}
        <GlassCard className="relative overflow-hidden p-5 border border-rose-100/60 shadow-[0_4px_20px_rgba(244,63,94,0.06)] hover:shadow-[0_8px_30px_rgba(244,63,94,0.10)] transition-all">
          <motion.div
            animate={{ scale: [1, 1.1, 1], opacity: [0.10, 0.24, 0.10] }}
            transition={{ duration: 2.5, repeat: Infinity, ease: "easeInOut" }}
            className="absolute -left-6 -bottom-6 h-36 w-36 rounded-full bg-rose-300/40 blur-3xl pointer-events-none"
          />
          <div className="relative z-10 flex items-start justify-between">
            <div className="flex flex-col">
              <p className="text-[14px] font-medium text-black/50 flex items-center gap-2">
                <HeartPulse className="h-4 w-4 text-rose-500" /> Avg heart
              </p>
              <div className="mt-2 flex items-baseline gap-1">
                <p className="text-[32px] font-semibold tracking-[-0.04em] text-[#0f172a]">74</p>
                <span className="text-[15px] font-medium text-black/40 mb-1">bpm</span>
              </div>
              <p className="text-[12px] font-medium text-rose-500/80 mt-1 uppercase tracking-wider">Resting: 58 bpm</p>
            </div>
            <div className="flex h-12 w-12 items-center justify-center shrink-0 rounded-[18px] bg-rose-100/80 border border-rose-200/40 backdrop-blur-sm shadow-sm">
              <motion.div
                animate={{ scale: [1, 1.25, 1, 1.25, 1] }}
                transition={{ duration: 1.5, repeat: Infinity, ease: "easeInOut", times: [0, 0.1, 0.2, 0.3, 1] }}
              >
                <HeartPulse className="h-6 w-6 text-rose-600" />
              </motion.div>
            </div>
          </div>
          <div className="mt-6 flex items-end gap-1 h-[20px] w-full">
            {[14, 18, 10, 28, 16, 8, 22, 16, 10, 18, 14].map((h, i) => (
              <motion.div
                key={`hr-bar-${i}`}
                animate={{ height: [h, Math.max(6, h - 8), h + 6, h] }}
                transition={{ duration: 1.5, repeat: Infinity, delay: i * 0.08, ease: "easeInOut" }}
                className="flex-1 rounded-t-sm bg-rose-400"
                style={{ height: `${h}px` }}
              />
            ))}
          </div>
        </GlassCard>
      </div>

      <ChartCard title="Recorded training time" value="12.0" unit="MIN" color="#06b6d4" dataKey="min" range="week" />
      <ChartCard title="Food logged in VitaAI" value="512" unit="KCAL" color="#eab308" dataKey="kcal" range="week" />
      <ChartCard title="Protein intake distribution" value="74" unit="G" color="#f59e0b" dataKey="protein" range="week" />

      <ActionRow icon={Plus} title="Start adaptive session" subtitle="20 min full-body strength" onClick={() => toast.success("Preparing your adaptive session...")} />
      <ActionRow icon={Zap} title="Recovery mobility" subtitle="Breathing, hips, shoulders" onClick={() => toast.success("Loading mobility exercises...")} />
      <ActionRow icon={Salad} title="Log smart meal" subtitle="Snap a plate or type ingredients" to="/chat" />
      <ActionRow icon={Apple} title="Macro coach" subtitle="Balanced targets for training days" onClick={() => toast("Your macros are currently optimized for a training day.")} />
    </div>
  );
}

function AnalyticDetail({ analytic, range, onRangeChange, onBack }: { analytic: AnalyticDefinition; range: AnalyticRange; onRangeChange: (range: AnalyticRange) => void; onBack: () => void }) {
  const data = analyticSeries[analytic.key][range];
  const gradientId = `detail-${analytic.key}-${range}`;
  const Icon = analytic.icon;

  return (
    <motion.div initial={{ opacity: 0, x: 24 }} animate={{ opacity: 1, x: 0 }} className="space-y-6">
      <button type="button" onClick={onBack} className="flex items-center gap-2 text-[14px] font-semibold uppercase tracking-[0.22em] text-black/45 transition-colors hover:text-[#0f172a]">
        <ChevronLeft className="h-5 w-5" /> All analytics
      </button>
      <GlassCard className="relative overflow-hidden p-6 shadow-[0_8px_30px_rgba(0,0,0,0.05)]">
        <motion.div
          animate={{ opacity: [0.06, 0.16, 0.06], scale: [1, 1.1, 1] }}
          transition={{ duration: 5, repeat: Infinity, ease: "easeInOut" }}
          className="absolute -right-20 -top-20 h-64 w-64 rounded-full blur-[60px] pointer-events-none"
          style={{ backgroundColor: analytic.color }}
        />
        <div className="relative z-10 mb-6 flex items-start justify-between gap-4">
          <div className="flex items-center gap-4">
            <div className="flex h-16 w-16 items-center justify-center rounded-[24px] border border-black/[0.07] bg-white/80 shadow-sm backdrop-blur-sm" style={{ color: analytic.color }}>
              <Icon className="h-8 w-8" />
            </div>
            <div>
              <p className="text-[13px] font-semibold uppercase tracking-[0.24em] text-black/45">{analytic.shortTitle}</p>
              <h2 className="mt-1 text-[42px] font-semibold leading-none tracking-[-0.07em] text-[#0f172a]">
                {analytic.value}<span className="ml-1 text-[16px] tracking-normal text-black/40">{analytic.unit}</span>
              </h2>
            </div>
          </div>
          <div className="rounded-full border border-black/[0.07] bg-white/70 px-4 py-2.5 text-[13px] font-bold text-black/70 shadow-sm backdrop-blur-md">{analytic.target}</div>
        </div>

        {/* Range selector */}
        <div className="relative z-10 mb-6 grid grid-cols-4 gap-1.5 rounded-full border border-black/[0.06] bg-black/[0.03] p-1.5 backdrop-blur-sm">
          {(Object.keys(rangeLabels) as AnalyticRange[]).map((item) => (
            <button
              key={item}
              type="button"
              onClick={() => onRangeChange(item)}
              className={`rounded-full px-2 py-2.5 text-[13px] font-bold transition-all ${range === item ? "bg-[#0f172a] text-white shadow-[0_6px_16px_rgba(15,23,42,0.18)]" : "text-black/50 hover:bg-black/[0.06] hover:text-black/80"}`}
            >
              {rangeLabels[item]}
            </button>
          ))}
        </div>

        <div className="relative z-10 h-[300px] w-full min-h-[300px]">
          <ResponsiveContainer width="100%" height={300} minWidth={1} minHeight={1}>
            {range === "day" && analytic.key === "sleep" ? (
              <BarChart key={`bar-${analytic.key}-${range}`} data={data} margin={{ top: 14, right: 2, left: -24, bottom: 0 }}>
                <CartesianGrid key="grid" stroke="rgba(15,23,42,.05)" vertical={false} />
                <XAxis key="xaxis" dataKey="label" tick={{ fill: "rgba(15,23,42,.45)", fontSize: 12 }} axisLine={false} tickLine={false} />
                <YAxis key="yaxis" tick={{ fill: "rgba(15,23,42,.38)", fontSize: 12 }} axisLine={false} tickLine={false} />
                <Tooltip key="tooltip" cursor={{ fill: "rgba(15,23,42,.02)" }} contentStyle={{ background: "rgba(255,255,255,.96)", border: "1px solid rgba(0,0,0,.07)", borderRadius: 20, color: "#0f172a", fontSize: "14px", padding: "12px", backdropFilter: "blur(12px)", boxShadow: "0 8px 24px rgba(0,0,0,0.08)" }} />
                <Bar key="bar" dataKey="value" fill={analytic.color} radius={[12, 12, 6, 6]} />
              </BarChart>
            ) : (
              <AreaChart key={`area-${analytic.key}-${range}`} data={data} margin={{ top: 14, right: 4, left: -24, bottom: 0 }}>
                <defs key="defs">
                  <linearGradient key="grad" id={gradientId} x1="0" x2="0" y1="0" y2="1">
                    <stop key="s1" offset="0%" stopColor={analytic.color} stopOpacity={0.45} />
                    <stop key="s2" offset="100%" stopColor={analytic.color} stopOpacity={0.03} />
                  </linearGradient>
                </defs>
                <CartesianGrid key="grid" stroke="rgba(15,23,42,.05)" vertical={false} />
                <XAxis key="xaxis" dataKey="label" tick={{ fill: "rgba(15,23,42,.45)", fontSize: 12 }} axisLine={false} tickLine={false} />
                <YAxis key="yaxis" tick={{ fill: "rgba(15,23,42,.38)", fontSize: 12 }} axisLine={false} tickLine={false} />
                <Tooltip key="tooltip" contentStyle={{ background: "rgba(255,255,255,.96)", border: "1px solid rgba(0,0,0,.07)", borderRadius: 20, color: "#0f172a", fontSize: "14px", padding: "12px", backdropFilter: "blur(12px)", boxShadow: "0 8px 24px rgba(0,0,0,0.08)" }} />
                <Area key="area" type="monotone" dataKey="value" stroke={analytic.color} fill={`url(#${gradientId})`} strokeWidth={3} dot={{ r: 4, fill: analytic.color, strokeWidth: 0 }} activeDot={{ r: 7, stroke: "#fff", strokeWidth: 2 }} />
              </AreaChart>
            )}
          </ResponsiveContainer>
        </div>
      </GlassCard>

      <GlassCard className="p-5">
        <div className="flex gap-4">
          <TrendingUp className="mt-1 h-6 w-6 shrink-0" style={{ color: analytic.color }} />
          <div>
            <p className="text-[16px] font-semibold text-[#0f172a]">VitaAI insight</p>
            <p className="mt-1.5 text-[14px] leading-relaxed text-black/55">{analytic.insight}</p>
          </div>
        </div>
      </GlassCard>
    </motion.div>
  );
}

export function Analytics() {
  const [selectedAnalytic, setSelectedAnalytic] = useState<AnalyticDefinition | null>(null);
  const [detailRange, setDetailRange] = useState<AnalyticRange>("week");
  const [summaryRange, setSummaryRange] = useState<AnalyticRange>("week");

  const openAnalytic = (analytic: AnalyticDefinition) => {
    setSelectedAnalytic(analytic);
    setDetailRange(summaryRange);
    toast(`Showing ${analytic.shortTitle} analytics`);
  };

  return (
    <div className="space-y-6 pb-6">
      <PageHeader title="Analytics" kicker="Vitals & recovery" />
      {selectedAnalytic ? (
        <AnalyticDetail analytic={selectedAnalytic} range={detailRange} onRangeChange={setDetailRange} onBack={() => setSelectedAnalytic(null)} />
      ) : (
        <>
          {/* Time range selector */}
          <div className="grid grid-cols-4 gap-1.5 rounded-full border border-black/[0.06] bg-black/[0.03] p-1.5 backdrop-blur-sm">
            {(Object.keys(rangeLabels) as AnalyticRange[]).map((item) => (
              <button
                key={item}
                type="button"
                onClick={() => setSummaryRange(item)}
                className={`rounded-full px-2 py-2.5 text-[13px] font-bold transition-all ${summaryRange === item ? "bg-[#0f172a] text-white shadow-[0_6px_16px_rgba(15,23,42,0.18)]" : "text-black/50 hover:bg-black/[0.06] hover:text-black/80"}`}
              >
                {rangeLabels[item]}
              </button>
            ))}
          </div>

          <div className="space-y-4">
            {analyticsCatalog.map((analytic) => (
              <button key={analytic.key} type="button" onClick={() => openAnalytic(analytic)} className="block w-full text-left transition-transform active:scale-[0.985]">
                <ChartCard title={analytic.title} value={analytic.value} unit={analytic.unit} color={analytic.color} dataKey={analytic.key} range={summaryRange} />
              </button>
            ))}
          </div>
        </>
      )}
    </div>
  );
}

export function VitaChat() {
  const [inputValue, setInputValue] = useState("");
  const [isThinking, setIsThinking] = useState(false);
  const [messages, setMessages] = useState([
    ["VitaAI", "You are under-fueled for today's workout. Want a 35g protein meal idea?"],
    ["You", "Yes, make it quick and not boring."],
    ["VitaAI", "Try a lemon yogurt bowl with whey, berries, chia, and toasted almonds. 430 kcal, 39g protein."],
  ]);

  const handleSend = () => {
    if (!inputValue.trim()) return;
    setMessages((prev) => [...prev, ["You", inputValue]]);
    setInputValue("");
    setIsThinking(true);

    setTimeout(() => {
      setIsThinking(false);
      setMessages((prev) => [...prev, ["VitaAI", "I can certainly help with that. Let's adjust your macros for the day to balance your recovery."]]);
    }, 3000);
  };

  return (
    <div className="flex min-h-[calc(100vh-8rem)] flex-col pb-6">
      <PageHeader title="VitaAI" kicker="Coach chat" />
      <div className="flex-1 space-y-4">
        {messages.map(([who, text], i) => (
          <GlassCard
            key={i}
            className={`max-w-[86%] p-5 shadow-[0_4px_16px_rgba(0,0,0,0.05)] ${
              who === "You"
                ? "ml-auto bg-[#0f172a] text-white shadow-[0_8px_28px_rgba(15,23,42,0.18)] border-[#0f172a]/80"
                : "border-black/[0.07]"
            }`}
          >
            <p className="mb-2 text-[12px] font-bold uppercase tracking-[0.18em] opacity-55">{who}</p>
            <p className="text-[16px] leading-relaxed">{text}</p>
          </GlassCard>
        ))}

        <AnimatePresence>
          {isThinking && (
            <motion.div
              initial={{ opacity: 0, y: 10, scale: 0.95 }}
              animate={{ opacity: 1, y: 0, scale: 1 }}
              exit={{ opacity: 0, scale: 0.95, filter: "blur(4px)" }}
              transition={{ duration: 0.6, ease: "easeOut" }}
              className="flex w-fit max-w-[86%] items-center gap-4 rounded-[24px] border border-black/[0.07] bg-white/80 p-4 pr-6 shadow-[0_8px_24px_rgba(0,0,0,0.06)] backdrop-blur-3xl"
            >
              <div className="relative flex h-10 w-14 items-center justify-center">
                <motion.div
                  animate={{ scale: [1, 1.8, 1], opacity: [0.3, 0.7, 0.3] }}
                  transition={{ duration: 3.5, repeat: Infinity, ease: "easeInOut" }}
                  className="absolute h-8 w-8 rounded-full bg-cyan-300 mix-blend-multiply blur-[8px]"
                />
                <motion.div
                  animate={{ scale: [1, 1.6, 1], opacity: [0.3, 0.6, 0.3], x: [-8, 8, -8] }}
                  transition={{ duration: 4, repeat: Infinity, ease: "easeInOut", delay: 0.5 }}
                  className="absolute h-6 w-6 rounded-full bg-blue-300 mix-blend-multiply blur-[6px]"
                />
                <motion.div
                  animate={{ scale: [1, 1.9, 1], opacity: [0.2, 0.5, 0.2], x: [8, -8, 8] }}
                  transition={{ duration: 4.5, repeat: Infinity, ease: "easeInOut", delay: 1 }}
                  className="absolute h-6 w-6 rounded-full bg-emerald-200 mix-blend-multiply blur-[8px]"
                />
                <motion.div
                  animate={{ opacity: [0.4, 1, 0.4] }}
                  transition={{ duration: 3.5, repeat: Infinity, ease: "easeInOut" }}
                  className="relative h-2 w-2 rounded-full bg-[#0f172a] shadow-[0_0_10px_rgba(15,23,42,0.4)]"
                />
              </div>
              <motion.p
                animate={{ opacity: [0.4, 0.9, 0.4] }}
                transition={{ duration: 3.5, repeat: Infinity, ease: "easeInOut" }}
                className="text-[14px] font-semibold tracking-wide text-[#0f172a]/70"
              >
                Tuning in...
              </motion.p>
            </motion.div>
          )}
        </AnimatePresence>
      </div>

      <div className="mt-6 flex items-center gap-3 rounded-full border border-black/[0.08] bg-white/85 p-2.5 backdrop-blur-2xl shadow-[0_4px_20px_rgba(0,0,0,0.06)] focus-within:border-black/[0.14] focus-within:shadow-[0_8px_32px_rgba(0,0,0,0.09)] transition-all">
        <input
          type="text"
          value={inputValue}
          onChange={(e) => setInputValue(e.target.value)}
          onKeyDown={(e) => e.key === "Enter" && handleSend()}
          placeholder="Ask about workouts, meals, recovery..."
          className="flex-1 bg-transparent px-5 text-[15px] text-[#0f172a] placeholder:text-black/40 outline-none"
        />
        <button
          onClick={handleSend}
          className="flex h-12 w-12 items-center justify-center rounded-full bg-gradient-to-tr from-[#1e293b] to-[#0f172a] text-white shadow-[0_6px_20px_rgba(15,23,42,0.22)] transition-transform hover:scale-105 active:scale-95 disabled:opacity-40"
          disabled={!inputValue.trim() || isThinking}
        >
          <Send className="h-5 w-5" />
        </button>
      </div>
    </div>
  );
}

function ProfileStatusChip({ label, active, tone }: { label: string; active: boolean; tone: "cyan" | "green" }) {
  const activeStyles =
    tone === "green"
      ? "border-emerald-300/60 bg-emerald-50 text-emerald-700"
      : "border-cyan-300/60 bg-cyan-50 text-cyan-700";
  const inactiveStyles = "border-black/[0.08] bg-black/[0.03] text-black/45";

  return (
    <span className={`inline-flex items-center gap-1.5 rounded-full border px-3 py-1.5 text-[11px] font-bold ${active ? activeStyles : inactiveStyles}`}>
      <span className={`h-1.5 w-1.5 rounded-full ${active ? (tone === "green" ? "bg-emerald-500" : "bg-cyan-500") : "bg-black/20"}`} />
      {label}
    </span>
  );
}

function ProfileStat({ value, label }: { value: string; label: string }) {
  return (
    <div className="text-center">
      <p className="text-[18px] font-bold tracking-[-0.03em] text-[#0f172a]">{value}</p>
      <p className="mt-0.5 text-[11px] text-black/45">{label}</p>
    </div>
  );
}

export function Profile() {
  return (
    <div className="space-y-5 pb-6">
      <PageHeader title="Profile" kicker="Personalization" />

      <GlassCard className="border-cyan-500/20 p-6 shadow-[0_8px_32px_rgba(6,182,212,0.08)]">
        <div className="flex items-center gap-5">
          <div className="flex h-20 w-20 items-center justify-center rounded-[28px] bg-gradient-to-br from-[#1e293b] to-[#0f172a] text-white shadow-[0_8px_28px_rgba(15,23,42,0.18)]">
            <User className="h-10 w-10" />
          </div>
          <div>
            <h2 className="text-[22px] font-semibold tracking-[-0.04em] text-[#0f172a]">Alex</h2>
            <button type="button" className="mt-1 inline-flex items-center gap-1 text-[12px] font-medium text-cyan-600">
              <Edit3 className="h-3.5 w-3.5" />
              Tap to edit name
            </button>
          </div>
        </div>

        <div className="mt-4 flex flex-wrap gap-2">
          <ProfileStatusChip label="Health Connect" active tone="green" />
          <ProfileStatusChip label="Premium trial" active tone="cyan" />
        </div>

        <div className="my-4 h-px bg-black/[0.06]" />

        <div className="grid grid-cols-3 gap-3">
          <ProfileStat value="Lv 10" label="Level" />
          <ProfileStat value="24" label="Workouts" />
          <ProfileStat value="2.4 L" label="Water goal" />
        </div>
      </GlassCard>

      <SectionHeader title="Your progress" />
      <div className="space-y-3">
        <ActionRow icon={Award} title="Achievements" subtitle="8 streaks, 3 nutrition badges" onClick={() => toast("You're in the top 5% of users this week!")} />
      </div>

      <SectionHeader title="Health & goals" subtitle="Calibrate biometrics and daily targets" />
      <div className="space-y-3">
        <ActionRow icon={ShieldCheck} title="Health Connect" subtitle="Vitals, workouts, and nutrition synced" onClick={() => toast.success("Health Connect is fully synced.")} />
        <ActionRow icon={Settings} title="Biometric settings" subtitle="Weight, height, BMR, and activity targets" onClick={() => toast("Calibration saved.")} />
        <ActionRow icon={Droplets} title="Hydration goal" subtitle="2.4 L daily target" onClick={() => toast("Water goal updated.")} />
      </div>

      <SectionHeader title="Account" />
      <div className="space-y-3">
        <ActionRow icon={LogOut} title="Sign out" subtitle="Sign out and clear local records" style="destructive" onClick={() => toast("Signed out.")} />
      </div>
    </div>
  );
}
