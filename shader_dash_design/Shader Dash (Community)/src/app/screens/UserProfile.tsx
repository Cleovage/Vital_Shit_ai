import { motion } from "motion/react";
import { User, Target, ChevronRight, Award, Star, Settings, Clock, Link as LinkIcon } from "lucide-react";
import { GlassCard } from "../components/GlassCard";
import { Link } from "react-router";

export function UserProfile() {
  return (
    <div className="flex min-h-full flex-col px-7 py-8">
      <header className="flex items-center gap-5 pb-8">
        <div className="grid h-20 w-20 place-items-center rounded-full bg-white/10 shadow-[inset_0_1px_0_rgba(255,255,255,0.2)]">
          <User className="h-8 w-8 text-white/70" strokeWidth={1.5} />
        </div>
        <div>
          <h1 className="text-[24px] font-medium tracking-[-0.04em] text-white">Alex</h1>
          <p className="text-[14px] text-white/60">Fasting since June 2026</p>
        </div>
      </header>

      <motion.div
        initial={{ opacity: 0, y: 15 }}
        animate={{ opacity: 1, y: 0 }}
        transition={{ duration: 0.6 }}
        className="space-y-8 pb-6"
      >
        <section>
          <h2 className="mb-4 ml-2 text-[13px] uppercase tracking-[0.2em] text-white/50">Your Journey</h2>
          <GlassCard className="p-2 flex flex-col gap-1">
            <Link to="/profile/achievements" className="flex items-center justify-between rounded-[24px] p-4 border-b border-white/5 transition-colors hover:bg-white/5">
              <div className="flex items-center gap-4">
                <div className="h-8 w-8 rounded-full bg-fuchsia-400/20 grid place-items-center">
                  <Award className="h-4 w-4 text-fuchsia-300" />
                </div>
                <span className="text-[15px] font-medium text-white/90">Achievements</span>
              </div>
              <ChevronRight className="h-5 w-5 text-white/30" />
            </Link>
            <Link to="/profile/plan" className="flex items-center justify-between rounded-[24px] p-4 transition-colors hover:bg-white/5">
              <div className="flex items-center gap-4">
                <div className="h-8 w-8 rounded-full bg-cyan-400/20 grid place-items-center">
                  <Clock className="h-4 w-4 text-cyan-300" />
                </div>
                <span className="text-[15px] font-medium text-white/90">Fasting Plan</span>
              </div>
              <div className="flex items-center gap-2">
                <span className="text-[13px] text-white/50">16:8</span>
                <ChevronRight className="h-5 w-5 text-white/30" />
              </div>
            </Link>
          </GlassCard>
        </section>

        <section>
          <h2 className="mb-4 ml-2 text-[13px] uppercase tracking-[0.2em] text-white/50">Settings & More</h2>
          <GlassCard className="p-2 flex flex-col gap-1">
            <div className="flex items-center justify-between rounded-[24px] p-4 border-b border-white/5 transition-colors hover:bg-white/5 cursor-pointer">
              <div className="flex items-center gap-4">
                <div className="h-8 w-8 rounded-full bg-white/10 grid place-items-center">
                  <Target className="h-4 w-4 text-white/70" />
                </div>
                <span className="text-[15px] font-medium text-white/90">Weight Goal</span>
              </div>
              <div className="flex items-center gap-2">
                <span className="text-[13px] text-white/50">70.0 kg</span>
                <ChevronRight className="h-5 w-5 text-white/30" />
              </div>
            </div>
            <Link to="/profile/settings" className="flex items-center justify-between rounded-[24px] p-4 border-b border-white/5 transition-colors hover:bg-white/5">
              <div className="flex items-center gap-4">
                <div className="h-8 w-8 rounded-full bg-white/10 grid place-items-center">
                  <Settings className="h-4 w-4 text-white/70" />
                </div>
                <span className="text-[15px] font-medium text-white/90">Account Settings</span>
              </div>
              <ChevronRight className="h-5 w-5 text-white/30" />
            </Link>
            <Link to="/profile/premium" className="flex items-center justify-between rounded-[24px] p-4 transition-colors hover:bg-white/5">
              <div className="flex items-center gap-4">
                <div className="h-8 w-8 rounded-full bg-amber-400/20 grid place-items-center">
                  <Star className="h-4 w-4 text-amber-400" />
                </div>
                <span className="text-[15px] font-medium text-amber-400">Unlock Pro</span>
              </div>
              <ChevronRight className="h-5 w-5 text-white/30" />
            </Link>
          </GlassCard>
        </section>
      </motion.div>
    </div>
  );
}
