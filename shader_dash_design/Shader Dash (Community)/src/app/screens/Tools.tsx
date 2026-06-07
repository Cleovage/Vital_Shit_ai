import { useState } from "react";
import { Link, useNavigate } from "react-router";
import { motion } from "motion/react";
import { GlassCard } from "../components/GlassCard";
import { 
  Smile, Droplets, Zap, Utensils, BookOpen, Users, 
  ChevronLeft, Plus, Save
} from "lucide-react";

export const ScreenHeader = ({ title, backTo }: { title: string; backTo: string }) => {
  const navigate = useNavigate();
  return (
    <header className="flex items-center gap-4 pb-6 shrink-0">
      <button onClick={() => navigate(backTo)} className="grid h-10 w-10 place-items-center rounded-full bg-white/10 hover:bg-white/20 transition-colors text-white shadow-[inset_0_1px_0_rgba(255,255,255,0.1)]">
        <ChevronLeft className="h-5 w-5" />
      </button>
      <h1 className="text-[22px] font-medium tracking-[-0.04em] text-white">{title}</h1>
    </header>
  );
};

export function ToolsGrid() {
  const tools = [
    { id: "check-in", label: "Check-In", icon: Smile, color: "text-blue-300" },
    { id: "ketosis", label: "Ketosis", icon: Zap, color: "text-fuchsia-300" },
    { id: "hydration", label: "Hydration", icon: Droplets, color: "text-cyan-300" },
    { id: "meals", label: "Meals", icon: Utensils, color: "text-emerald-300" },
    { id: "learn", label: "Learn", icon: BookOpen, color: "text-amber-300" },
    { id: "community", label: "Community", icon: Users, color: "text-purple-300" },
  ];

  return (
    <div className="flex min-h-full flex-col px-7 py-8">
      <header className="pb-6">
        <h1 className="text-[30px] font-medium tracking-[-0.06em] text-white">Tools</h1>
        <p className="text-[14px] text-white/60 mt-1">Track your progress and learn</p>
      </header>
      <motion.div initial={{ opacity: 0, y: 15 }} animate={{ opacity: 1, y: 0 }} className="grid grid-cols-2 gap-4">
        {tools.map((t) => (
          <Link key={t.id} to={`/tools/${t.id}`}>
            <GlassCard className="p-5 flex flex-col items-center justify-center gap-3 aspect-square hover:bg-white/10 transition-colors active:scale-95">
              <t.icon className={`h-8 w-8 ${t.color}`} strokeWidth={1.5} />
              <span className="text-[15px] font-medium text-white">{t.label}</span>
            </GlassCard>
          </Link>
        ))}
      </motion.div>
    </div>
  );
}

export function CheckIn() {
  const [mood, setMood] = useState(7);
  const [symptoms, setSymptoms] = useState<string[]>([]);
  const options = ["Tired", "Energetic", "Headache", "Focused", "Hungry", "Calm"];

  return (
    <div className="flex min-h-full flex-col px-7 py-8">
      <ScreenHeader title="Daily Check-In" backTo="/tools" />
      <motion.div initial={{opacity:0,y:10}} animate={{opacity:1,y:0}} className="space-y-8 flex-1">
        <section>
          <h2 className="text-[15px] text-white/80 mb-4 ml-1">How are you feeling today?</h2>
          <GlassCard className="p-8 text-center">
            <div className="text-[56px] font-light mb-6 tracking-[-0.04em]">{mood}</div>
            <input type="range" min="1" max="10" value={mood} onChange={e=>setMood(parseInt(e.target.value))} className="w-full h-2 bg-white/10 rounded-full appearance-none outline-none accent-fuchsia-400" />
            <div className="flex justify-between mt-3 text-[12px] text-white/40">
              <span>Poor</span>
              <span>Excellent</span>
            </div>
          </GlassCard>
        </section>
        <section>
          <h2 className="text-[15px] text-white/80 mb-4 ml-1">Symptoms & State</h2>
          <div className="flex flex-wrap gap-3">
            {options.map(opt => (
              <button key={opt} onClick={() => setSymptoms(s=>s.includes(opt)?s.filter(x=>x!==opt):[...s,opt])} className={`px-5 py-2.5 rounded-full text-[14px] transition-colors ${symptoms.includes(opt) ? "bg-white text-black font-medium" : "bg-white/10 text-white/80 hover:bg-white/20 backdrop-blur-md shadow-[inset_0_1px_0_rgba(255,255,255,0.1)]"}`}>
                {opt}
              </button>
            ))}
          </div>
        </section>
      </motion.div>
      <button className="w-full h-14 rounded-full bg-white text-black font-medium text-[16px] mt-6 shadow-[0_4px_20px_rgba(255,255,255,0.2)] active:scale-95 transition-transform">Submit Check-In</button>
    </div>
  );
}

export function KetosisTracker() {
  const [level, setLevel] = useState(0.8);
  return (
    <div className="flex min-h-full flex-col px-7 py-8">
      <ScreenHeader title="Ketosis Tracker" backTo="/tools" />
      <motion.div initial={{opacity:0,y:10}} animate={{opacity:1,y:0}} className="space-y-6">
        <GlassCard className="p-8 text-center">
          <p className="text-[12px] uppercase tracking-[0.2em] text-white/50 mb-2">Current Status</p>
          <h2 className="text-[22px] font-medium text-fuchsia-300">Mild Ketosis</h2>
          <div className="mt-8 flex items-baseline justify-center gap-1.5">
            <span className="text-[56px] font-light leading-none tracking-[-0.04em]">{level.toFixed(1)}</span>
            <span className="text-[16px] text-white/50">mmol/L</span>
          </div>
          <div className="mt-8 h-2.5 w-full bg-white/10 rounded-full overflow-hidden flex">
            <div className="h-full bg-blue-400" style={{width:'30%'}} />
            <div className="h-full bg-fuchsia-400" style={{width:'40%'}} />
            <div className="h-full bg-purple-400" style={{width:'30%'}} />
          </div>
          <div className="flex justify-between mt-3 text-[11px] text-white/40 font-medium uppercase tracking-wider">
            <span>None</span>
            <span>Light</span>
            <span>Optimal</span>
            <span>High</span>
          </div>
        </GlassCard>
        
        <GlassCard className="p-6">
          <h3 className="text-[15px] mb-4 text-white/90">Add Reading</h3>
          <div className="flex items-center gap-4">
            <input type="number" step="0.1" value={level} onChange={(e)=>setLevel(parseFloat(e.target.value))} className="flex-1 bg-white/10 shadow-[inset_0_1px_0_rgba(255,255,255,0.1)] rounded-[16px] px-5 py-4 text-white text-[18px] outline-none focus:bg-white/20 transition-colors" />
            <button className="h-[56px] px-6 rounded-[16px] bg-white text-black font-medium flex items-center gap-2 active:scale-95 transition-transform">
              <Save className="h-5 w-5" strokeWidth={1.5} /> Save
            </button>
          </div>
        </GlassCard>
      </motion.div>
    </div>
  );
}

export function HydrationTracker() {
  const [ml, setMl] = useState(1200);
  const goal = 2500;
  const pct = Math.min(100, (ml / goal) * 100);
  return (
    <div className="flex min-h-full flex-col px-7 py-8 items-center">
      <div className="w-full"><ScreenHeader title="Hydration" backTo="/tools" /></div>
      <motion.div initial={{opacity:0,scale:0.95}} animate={{opacity:1,scale:1}} className="flex-1 flex flex-col items-center justify-center w-full">
        <div className="relative grid place-items-center mb-12">
          <svg className="absolute inset-0 h-[280px] w-[280px] -rotate-90 transform" viewBox="0 0 100 100">
            <circle cx="50" cy="50" r="48" fill="none" stroke="rgba(255,255,255,0.1)" strokeWidth="1.5" />
            <circle cx="50" cy="50" r="48" fill="none" stroke="#67e8f9" strokeWidth="2.5" strokeDasharray="301.59" strokeDashoffset={301.59 - (301.59 * pct / 100)} className="transition-all duration-1000 ease-out" strokeLinecap="round" />
          </svg>
          <div className="flex flex-col items-center justify-center text-center mt-2">
            <Droplets className="h-8 w-8 text-cyan-300 mb-3" strokeWidth={1.5} />
            <span className="text-[52px] font-light leading-none tracking-[-0.04em]">{ml/1000}L</span>
            <span className="text-[14px] text-white/50 mt-1">of {goal/1000}L goal</span>
          </div>
        </div>
        
        <button onClick={() => setMl(m => m + 250)} className="h-16 px-10 rounded-full bg-white/10 text-white font-medium text-[16px] flex items-center gap-3 hover:bg-white/20 active:scale-95 transition-all shadow-[inset_0_1px_0_rgba(255,255,255,0.2)] backdrop-blur-md">
          <Plus className="h-6 w-6" strokeWidth={1.5} /> Add 250ml
        </button>
      </motion.div>
    </div>
  );
}

export function MealLogger() {
  return (
    <div className="flex min-h-full flex-col px-7 py-8">
      <ScreenHeader title="Meal Logger" backTo="/tools" />
      <motion.div initial={{opacity:0,y:10}} animate={{opacity:1,y:0}} className="space-y-5 flex-1 mt-2">
        <h2 className="text-[14px] uppercase tracking-[0.15em] text-white/60 ml-2">Today's Meals</h2>
        <GlassCard className="p-4 flex justify-between items-center group cursor-pointer hover:bg-white/10 transition-colors">
          <div className="flex items-center gap-4">
            <div className="h-12 w-12 rounded-full bg-white/10 shadow-[inset_0_1px_0_rgba(255,255,255,0.2)] grid place-items-center text-[20px]">🥑</div>
            <div>
              <p className="text-[16px] font-medium text-white/90">Avocado Salad</p>
              <p className="text-[13px] text-white/50 mt-0.5">12:30 PM</p>
            </div>
          </div>
          <span className="text-[15px] font-medium text-emerald-300 pr-2">320 kcal</span>
        </GlassCard>
        <GlassCard className="p-4 flex justify-between items-center group cursor-pointer hover:bg-white/10 transition-colors">
          <div className="flex items-center gap-4">
            <div className="h-12 w-12 rounded-full bg-white/10 shadow-[inset_0_1px_0_rgba(255,255,255,0.2)] grid place-items-center text-[20px]">🍗</div>
            <div>
              <p className="text-[16px] font-medium text-white/90">Grilled Chicken</p>
              <p className="text-[13px] text-white/50 mt-0.5">6:45 PM</p>
            </div>
          </div>
          <span className="text-[15px] font-medium text-emerald-300 pr-2">450 kcal</span>
        </GlassCard>
      </motion.div>
      <button className="w-full h-14 rounded-full bg-white/10 text-white font-medium flex items-center justify-center gap-2 hover:bg-white/20 active:scale-95 transition-all shadow-[inset_0_1px_0_rgba(255,255,255,0.2)] backdrop-blur-md mt-6">
        <Plus className="h-5 w-5" strokeWidth={1.5} /> Add Meal
      </button>
    </div>
  );
}

export function KnowledgeBase() {
  const articles = [
    { title: "The Science of Autophagy", readTime: "5 min read" },
    { title: "Electrolytes 101", readTime: "3 min read" },
    { title: "Breaking Your Fast Safely", readTime: "4 min read" },
    { title: "Keto vs Low Carb", readTime: "6 min read" },
  ];
  return (
    <div className="flex min-h-full flex-col px-7 py-8">
      <ScreenHeader title="Learn" backTo="/tools" />
      <motion.div initial={{opacity:0,y:10}} animate={{opacity:1,y:0}} className="space-y-4 mt-2">
        {articles.map(a => (
          <GlassCard key={a.title} className="p-5 flex justify-between items-center cursor-pointer hover:bg-white/10 transition-colors">
            <div>
              <h3 className="text-[16px] font-medium text-white/90 mb-1.5">{a.title}</h3>
              <p className="text-[13px] text-white/50 flex items-center gap-1.5">
                <BookOpen className="h-3.5 w-3.5" /> {a.readTime}
              </p>
            </div>
            <ChevronLeft className="h-5 w-5 text-white/30 rotate-180" />
          </GlassCard>
        ))}
      </motion.div>
    </div>
  );
}

export function Community() {
  return (
    <div className="flex min-h-full flex-col px-7 py-8">
      <ScreenHeader title="Community" backTo="/tools" />
      <motion.div initial={{opacity:0,y:10}} animate={{opacity:1,y:0}} className="space-y-6 mt-2">
        <GlassCard className="p-8 text-center bg-gradient-to-br from-fuchsia-500/10 to-blue-500/10 shadow-[inset_0_0_0_1px_rgba(255,255,255,0.05)]">
          <div className="mx-auto h-14 w-14 rounded-full bg-white/10 grid place-items-center mb-5 shadow-[inset_0_1px_0_rgba(255,255,255,0.2)]">
            <Users className="h-6 w-6 text-white" strokeWidth={1.5} />
          </div>
          <h2 className="text-[22px] font-medium text-white mb-2 tracking-tight">Global Challenge</h2>
          <p className="text-[14px] text-white/70 mb-6">100k Hours Fasted Together</p>
          <div className="h-2.5 w-full bg-white/10 rounded-full overflow-hidden">
            <div className="h-full bg-white" style={{width:'75%'}} />
          </div>
          <p className="text-[13px] text-white/50 mt-3 font-medium">75,000 / 100,000 hrs</p>
        </GlassCard>
        <h3 className="text-[14px] uppercase tracking-[0.15em] text-white/60 mt-8 mb-4 ml-2">Leaderboard</h3>
        <GlassCard className="p-2 flex flex-col gap-1">
          {["Sarah M.", "David K.", "Elena V."].map((name, i) => (
            <div key={name} className="flex items-center justify-between p-4 border-b border-white/5 last:border-0 hover:bg-white/5 rounded-[20px] transition-colors cursor-pointer">
              <div className="flex items-center gap-4">
                <span className="text-[14px] font-medium text-white/40 w-4 text-center">{i+1}</span>
                <div className="h-10 w-10 rounded-full bg-white/10 shadow-[inset_0_1px_0_rgba(255,255,255,0.1)]" />
                <span className="text-[15px] font-medium text-white/90">{name}</span>
              </div>
              <span className="text-[14px] font-medium text-cyan-300 pr-2">{48 - i*5}h</span>
            </div>
          ))}
        </GlassCard>
      </motion.div>
    </div>
  );
}
