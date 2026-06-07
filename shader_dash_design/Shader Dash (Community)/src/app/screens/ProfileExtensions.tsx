import { useState } from "react";
import { Link, useNavigate } from "react-router";
import { motion } from "motion/react";
import { GlassCard } from "../components/GlassCard";
import { ChevronLeft, Award, Star, Settings, Shield, Bell, CheckCircle2, User } from "lucide-react";
import { ScreenHeader } from "./Tools";

export function Achievements() {
  const badges = [
    { title: "First Fast", desc: "Completed 1st fast", icon: Award, color: "text-blue-300", earned: true },
    { title: "7-Day Streak", desc: "Fasted 7 days in a row", icon: Star, color: "text-amber-300", earned: true },
    { title: "Keto Master", desc: "Logged optimal ketosis", icon: Shield, color: "text-fuchsia-300", earned: false },
    { title: "Early Bird", desc: "Started fast before 6PM", icon: Award, color: "text-emerald-300", earned: false },
  ];
  return (
    <div className="flex min-h-full flex-col px-7 py-8">
      <ScreenHeader title="Achievements" backTo="/profile" />
      <motion.div initial={{opacity:0,y:10}} animate={{opacity:1,y:0}} className="grid grid-cols-2 gap-4 mt-2">
        {badges.map(b => (
          <GlassCard key={b.title} className={`p-6 flex flex-col items-center text-center gap-3 transition-all hover:bg-white/10 ${b.earned ? 'opacity-100' : 'opacity-50 grayscale'}`}>
            <div className={`h-14 w-14 rounded-full bg-white/10 grid place-items-center shadow-[inset_0_1px_0_rgba(255,255,255,0.2)]`}>
              <b.icon className={`h-7 w-7 ${b.color}`} strokeWidth={1.5} />
            </div>
            <div>
              <h3 className="text-[15px] font-medium text-white/90 leading-tight mb-1">{b.title}</h3>
              <p className="text-[12px] text-white/50 leading-snug">{b.desc}</p>
            </div>
          </GlassCard>
        ))}
      </motion.div>
    </div>
  );
}

export function PlanSettings() {
  const plans = ["16:8", "18:6", "OMAD", "Custom"];
  const [active, setActive] = useState("16:8");
  return (
    <div className="flex min-h-full flex-col px-7 py-8">
      <ScreenHeader title="Fasting Plan" backTo="/profile" />
      <motion.div initial={{opacity:0,y:10}} animate={{opacity:1,y:0}} className="mt-2">
        <h2 className="text-[14px] uppercase tracking-[0.15em] text-white/60 mb-4 ml-2">Select your protocol</h2>
        <GlassCard className="p-2 flex flex-col gap-1">
          {plans.map(p => (
            <div key={p} onClick={()=>setActive(p)} className="flex items-center justify-between p-5 rounded-[22px] cursor-pointer hover:bg-white/5 transition-colors">
              <span className={`text-[16px] font-medium ${active === p ? 'text-white' : 'text-white/70'}`}>{p}</span>
              {active === p && <CheckCircle2 className="h-5 w-5 text-emerald-400" strokeWidth={2} />}
            </div>
          ))}
        </GlassCard>
      </motion.div>
    </div>
  );
}

export function AccountSettings() {
  return (
    <div className="flex min-h-full flex-col px-7 py-8">
      <ScreenHeader title="Account Settings" backTo="/profile" />
      <motion.div initial={{opacity:0,y:10}} animate={{opacity:1,y:0}} className="mt-2">
        <GlassCard className="p-2 flex flex-col gap-1">
          {[
            { icon: User, label: "Personal Info" },
            { icon: Bell, label: "Notifications" },
            { icon: Shield, label: "Data & Privacy" },
          ].map((item, i) => (
            <div key={i} className="flex items-center justify-between p-5 rounded-[22px] hover:bg-white/5 transition-colors cursor-pointer border-b border-white/5 last:border-0">
              <div className="flex items-center gap-4">
                <item.icon className="h-5 w-5 text-white/70" strokeWidth={1.5} />
                <span className="text-[15px] font-medium text-white/90">{item.label}</span>
              </div>
              <ChevronLeft className="h-5 w-5 text-white/30 rotate-180" />
            </div>
          ))}
        </GlassCard>
      </motion.div>
    </div>
  );
}

export function Premium() {
  return (
    <div className="flex min-h-full flex-col px-7 py-8">
      <ScreenHeader title="Premium" backTo="/profile" />
      <motion.div initial={{opacity:0, scale:0.95}} animate={{opacity:1, scale:1}} className="flex-1 flex flex-col items-center text-center mt-6">
        <div className="h-24 w-24 rounded-full bg-gradient-to-tr from-amber-400 to-amber-200 grid place-items-center mb-6 shadow-[0_0_40px_rgba(251,191,36,0.3)]">
          <Star className="h-12 w-12 text-black fill-black" />
        </div>
        <h2 className="text-[32px] font-medium text-white mb-3 tracking-[-0.04em]">Unlock VitaAI Pro</h2>
        <p className="text-[15px] text-white/70 mb-10 px-4 leading-relaxed">Get the most out of your fasting journey with advanced features.</p>
        
        <GlassCard className="p-7 w-full text-left space-y-5 mb-10">
          {[
            "Advanced analytics & insights",
            "Personalized AI coaching",
            "Priority support & unlimited chat",
            "Custom meal plans & recipes"
          ].map((feat, i) => (
            <div key={i} className="flex items-center gap-4">
              <div className="h-6 w-6 rounded-full bg-amber-400/20 grid place-items-center shrink-0">
                <CheckCircle2 className="h-4 w-4 text-amber-400" strokeWidth={2.5} />
              </div>
              <span className="text-[15px] font-medium text-white/90">{feat}</span>
            </div>
          ))}
        </GlassCard>
        
        <button className="w-full h-16 rounded-full bg-gradient-to-r from-amber-400 to-amber-200 text-black font-semibold text-[16px] shadow-[0_4px_20px_rgba(251,191,36,0.4)] active:scale-95 transition-all mt-auto">
          Start 7-Day Free Trial
        </button>
      </motion.div>
    </div>
  );
}
